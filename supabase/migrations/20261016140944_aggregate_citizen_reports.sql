-- Requires the secure v3 schema. Run as the database owner after review.
-- Only future INSERT statements are processed: no seeds/backfill or public RPC.
BEGIN;

CREATE SCHEMA IF NOT EXISTS pac_private;
REVOKE ALL ON SCHEMA pac_private FROM PUBLIC, anon, authenticated;

-- Pure classification helper. Report type wins over a contradictory hasPower.
CREATE OR REPLACE FUNCTION pac_private.citizen_report_status(p_report_type TEXT, p_has_power BOOLEAN)
RETURNS TEXT LANGUAGE sql IMMUTABLE PARALLEL SAFE
SET search_path = ''
AS $function$
    SELECT CASE
        WHEN pg_catalog.upper(pg_catalog.btrim(p_report_type)) IN
            ('FALLA', 'FALLA_IRREGULAR', 'IRREGULAR_OUTAGE', 'IRREGULAR',
             'AVERIA', 'AVERÍA', 'BAJON', 'BAJÓN') THEN 'IRREGULAR_OUTAGE'
        WHEN pg_catalog.upper(pg_catalog.btrim(p_report_type)) IN
            ('SIN_LUZ', 'SCHEDULED_OUTAGE', 'OUTAGE') THEN 'SCHEDULED_OUTAGE'
        WHEN p_has_power THEN 'NORMAL'
        ELSE 'SCHEDULED_OUTAGE'
    END;
$function$;
ALTER FUNCTION pac_private.citizen_report_status(TEXT, BOOLEAN) OWNER TO postgres;
REVOKE ALL ON FUNCTION pac_private.citizen_report_status(TEXT, BOOLEAN) FROM PUBLIC, anon, authenticated;

-- VOLATILE matters: each SELECT obtains a fresh READ COMMITTED snapshot after
-- the sector row lock, so concurrent inserts cannot lose each other's counts.
-- Transition rows contain only rows actually inserted; DO NOTHING replays have
-- an empty transition table and do not produce sector updates/Realtime events.
CREATE OR REPLACE FUNCTION pac_private.aggregate_citizen_reports()
RETURNS TRIGGER LANGUAGE plpgsql VOLATILE SECURITY DEFINER
SET search_path = ''
AS $function$
DECLARE
    v_sector_id TEXT;
    v_sector public.sectors%ROWTYPE;
    v_event RECORD;
    v_now_ms BIGINT;
    v_cutoff_ms BIGINT;
    v_upper_ms BIGINT;
    v_count BIGINT;
    v_without_power BIGINT;
    v_status TEXT;
    v_voltage DOUBLE PRECISION;
BEGIN
    -- This is an internal trigger entry point, never a general write API.
    IF TG_TABLE_SCHEMA <> 'public' OR TG_TABLE_NAME <> 'citizen_reports'
       OR TG_OP <> 'INSERT' OR TG_LEVEL <> 'STATEMENT' THEN
        RAISE EXCEPTION 'Unsupported trigger context' USING ERRCODE = '42501';
    END IF;

    -- Lock known sectors in one order, once per statement (including bulk
    -- inserts). Unknown IDs remain historical reports and never create sectors.
    FOR v_sector_id IN SELECT DISTINCT inserted."sectorId"
        FROM inserted_citizen_reports AS inserted ORDER BY inserted."sectorId"
    LOOP
        SELECT sector_row.* INTO v_sector FROM public.sectors AS sector_row
        WHERE sector_row.id = v_sector_id FOR UPDATE;
        IF NOT FOUND THEN CONTINUE; END IF;

        -- Calculate the moving window after waiting for the lock. Two hours
        -- avoids classifying today's map from an offline queue of old events.
        -- The upper bound matches v3's permitted device clock skew (+10 min).
        v_now_ms := pg_catalog.floor(EXTRACT(EPOCH FROM pg_catalog.clock_timestamp()) * 1000)::BIGINT;
        v_cutoff_ms := v_now_ms - 2 * 60 * 60 * 1000;
        v_upper_ms := v_now_ms + 10 * 60 * 1000;
        IF NOT EXISTS (SELECT FROM inserted_citizen_reports AS inserted
            WHERE inserted."sectorId" = v_sector_id
              AND inserted.timestamp BETWEEN v_cutoff_ms AND v_upper_ms) THEN
            CONTINUE;
        END IF;

        SELECT pg_catalog.count(*), pg_catalog.count(*) FILTER (
            WHERE report."hasPower" = false OR pac_private.citizen_report_status(report."reportType", report."hasPower") = 'SCHEDULED_OUTAGE'
        ) INTO v_count, v_without_power
        FROM public.citizen_reports AS report
        WHERE report."sectorId" = v_sector_id
          AND report.timestamp BETWEEN v_cutoff_ms AND v_upper_ms;

        SELECT report.* INTO v_event FROM public.citizen_reports AS report
        WHERE report."sectorId" = v_sector_id
          AND report.timestamp BETWEEN v_cutoff_ms AND v_upper_ms
        ORDER BY report.timestamp DESC, report.id DESC LIMIT 1;
        IF NOT FOUND THEN CONTINUE; END IF;

        v_status := pac_private.citizen_report_status(v_event."reportType", v_event."hasPower");
        v_voltage := CASE WHEN v_event.voltage BETWEEN 0 AND 300 THEN v_event.voltage
            WHEN v_event."hasPower" = false OR v_status = 'SCHEDULED_OUTAGE' THEN 0.0
            ELSE v_sector.voltage END;

        UPDATE public.sectors AS sector_row SET
            "confirmedReportsCount" = v_count::INTEGER,
            "withoutPowerPercentage" = pg_catalog.round(100.0 * v_without_power / v_count)::INTEGER,
            -- An admin edit with a newer/equal event time wins. Old uploads may
            -- update the sample, but never roll the sector's state/time back.
            status = CASE WHEN v_event.timestamp > v_sector."lastUpdatedMillis" THEN v_status ELSE sector_row.status END,
            voltage = CASE WHEN v_event.timestamp > v_sector."lastUpdatedMillis" THEN v_voltage ELSE sector_row.voltage END,
            "lastUpdatedMillis" = GREATEST(sector_row."lastUpdatedMillis", v_event.timestamp)
        WHERE sector_row.id = v_sector_id;
    END LOOP;
    RETURN NULL;
END;
$function$;
ALTER FUNCTION pac_private.aggregate_citizen_reports() OWNER TO postgres;
REVOKE ALL ON FUNCTION pac_private.aggregate_citizen_reports() FROM PUBLIC, anon, authenticated;

-- Reapplying this file changes neither reports nor the current sector data.
DROP TRIGGER IF EXISTS aggregate_citizen_reports_after_insert ON public.citizen_reports;
CREATE TRIGGER aggregate_citizen_reports_after_insert
AFTER INSERT ON public.citizen_reports
REFERENCING NEW TABLE AS inserted_citizen_reports
FOR EACH STATEMENT EXECUTE FUNCTION pac_private.aggregate_citizen_reports();

-- v3 already provides this prefix index; keep standalone upgrades idempotent.
CREATE INDEX IF NOT EXISTS citizen_reports_sector_timestamp_idx
    ON public.citizen_reports ("sectorId", timestamp DESC);

COMMENT ON FUNCTION pac_private.aggregate_citizen_reports() IS
    'On INSERT only: exact sector ID; 2-hour event-time sample; latest event wins only after lastUpdatedMillis; real report counts. No historical backfill.';
NOTIFY pgrst, 'reload schema';
COMMIT;

-- PAC Barinas v3. Run as database owner after deployment review.
-- No administrator is created here. Provision Auth separately and enable a
-- verified user UUID through a privileged connection, never through a client.
BEGIN;

ALTER TABLE public.sectors ADD COLUMN IF NOT EXISTS estado TEXT DEFAULT 'Barinas';
ALTER TABLE public.sectors ADD COLUMN IF NOT EXISTS municipio TEXT DEFAULT 'Barinas';
ALTER TABLE public.sectors ADD COLUMN IF NOT EXISTS parroquia TEXT;
ALTER TABLE public.sectors ADD COLUMN IF NOT EXISTS sector TEXT;
ALTER TABLE public.sectors ADD COLUMN IF NOT EXISTS barrio TEXT DEFAULT '';
ALTER TABLE public.sectors ADD COLUMN IF NOT EXISTS notes TEXT DEFAULT '';
ALTER TABLE public.sectors ADD COLUMN IF NOT EXISTS lat FLOAT8;
ALTER TABLE public.sectors ADD COLUMN IF NOT EXISTS lon FLOAT8;
DO $community_flag$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'sectors' AND column_name = 'isCommunity') THEN
        ALTER TABLE public.sectors ADD COLUMN "isCommunity" BOOLEAN NOT NULL DEFAULT false;
        UPDATE public.sectors SET "isCommunity" = true
            WHERE id LIKE 'sec\_com\_%' ESCAPE '\' OR id LIKE 'sec\_community\_%' ESCAPE '\';
    END IF;
END;
$community_flag$;
ALTER TABLE public.community_locations ADD COLUMN IF NOT EXISTS estado TEXT DEFAULT 'Barinas';
ALTER TABLE public.community_locations ADD COLUMN IF NOT EXISTS municipio TEXT DEFAULT 'Barinas';
ALTER TABLE public.community_locations ADD COLUMN IF NOT EXISTS parroquia TEXT;
ALTER TABLE public.community_locations ADD COLUMN IF NOT EXISTS sector TEXT;
ALTER TABLE public.community_locations ADD COLUMN IF NOT EXISTS barrio TEXT DEFAULT '';
ALTER TABLE public.community_locations ADD COLUMN IF NOT EXISTS notes TEXT DEFAULT '';
ALTER TABLE public.community_locations ADD COLUMN IF NOT EXISTS lat FLOAT8;
ALTER TABLE public.community_locations ADD COLUMN IF NOT EXISTS lon FLOAT8;
ALTER TABLE public.citizen_reports ADD COLUMN IF NOT EXISTS observation TEXT;

CREATE TABLE IF NOT EXISTS public.admin_users (
    user_id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    enabled BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE IF NOT EXISTS public.push_alert_limits (
    scope TEXT PRIMARY KEY,
    last_claim_at TIMESTAMPTZ NOT NULL
);
ALTER TABLE public.admin_users ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.push_alert_limits ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.sectors ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.app_config ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.citizen_reports ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.community_locations ENABLE ROW LEVEL SECURITY;

-- Permissive policies combine with OR, including manually named old policies.
DO $policies$
DECLARE p RECORD;
BEGIN
    FOR p IN SELECT schemaname, tablename, policyname FROM pg_catalog.pg_policies
        WHERE schemaname = 'public' AND tablename IN
        ('admin_users', 'push_alert_limits', 'sectors', 'app_config', 'citizen_reports', 'community_locations')
    LOOP
        EXECUTE pg_catalog.format('DROP POLICY %I ON %I.%I', p.policyname, p.schemaname, p.tablename);
    END LOOP;
END;
$policies$;

REVOKE ALL ON TABLE public.admin_users, public.push_alert_limits, public.sectors,
    public.app_config, public.citizen_reports, public.community_locations
    FROM PUBLIC, anon, authenticated;

CREATE OR REPLACE FUNCTION public.is_admin()
RETURNS BOOLEAN LANGUAGE sql STABLE SECURITY DEFINER
SET search_path = ''
AS $function$
    SELECT EXISTS (SELECT 1 FROM public.admin_users a
        WHERE a.user_id = (SELECT auth.uid()) AND a.enabled = true);
$function$;
ALTER FUNCTION public.is_admin() OWNER TO postgres;
REVOKE ALL ON FUNCTION public.is_admin() FROM PUBLIC, anon, authenticated;
GRANT EXECUTE ON FUNCTION public.is_admin() TO authenticated;

GRANT SELECT ON public.sectors, public.app_config, public.citizen_reports TO anon, authenticated;
GRANT INSERT ON public.citizen_reports, public.community_locations TO anon, authenticated;
GRANT INSERT, UPDATE, DELETE ON public.sectors, public.app_config TO authenticated;
GRANT SELECT, UPDATE, DELETE ON public.community_locations TO authenticated;
GRANT UPDATE, DELETE ON public.citizen_reports TO authenticated;

CREATE POLICY sectors_public_read ON public.sectors FOR SELECT TO anon, authenticated USING (true);
CREATE POLICY app_config_public_read ON public.app_config FOR SELECT TO anon, authenticated USING (true);
CREATE POLICY citizen_reports_public_read ON public.citizen_reports FOR SELECT TO anon, authenticated USING (true);
CREATE POLICY sectors_admin ON public.sectors FOR ALL TO authenticated
    USING ((SELECT public.is_admin())) WITH CHECK ((SELECT public.is_admin()));
CREATE POLICY app_config_admin ON public.app_config FOR ALL TO authenticated
    USING ((SELECT public.is_admin())) WITH CHECK ((SELECT public.is_admin()));
CREATE POLICY community_locations_admin ON public.community_locations FOR ALL TO authenticated
    USING ((SELECT public.is_admin())) WITH CHECK ((SELECT public.is_admin()));
CREATE POLICY citizen_reports_admin ON public.citizen_reports FOR ALL TO authenticated
    USING ((SELECT public.is_admin())) WITH CHECK ((SELECT public.is_admin()));

-- Citizens submit events/proposals only; never publish an active sector.
CREATE POLICY citizen_reports_submit ON public.citizen_reports FOR INSERT TO anon, authenticated
WITH CHECK (
    pg_catalog.char_length(pg_catalog.btrim("sectorId")) BETWEEN 1 AND 200
    AND pg_catalog.char_length(pg_catalog.btrim("sectorName")) BETWEEN 1 AND 200
    AND "reportType" IN ('NORMAL', 'SIN_LUZ', 'BAJON', 'FALLA_IRREGULAR', 'IRREGULAR_OUTAGE')
    AND (voltage IS NULL OR voltage BETWEEN 0 AND 300)
    AND (observation IS NULL OR pg_catalog.char_length(observation) <= 2000)
    AND ("deviceOrigin" IS NULL OR pg_catalog.char_length("deviceOrigin") <= 100)
    AND timestamp > 0
    AND timestamp <= (EXTRACT(EPOCH FROM now() + INTERVAL '10 minutes') * 1000)::BIGINT
);
CREATE POLICY community_locations_submit ON public.community_locations FOR INSERT TO anon, authenticated
WITH CHECK (
    pg_catalog.char_length(id) BETWEEN 1 AND 200
    AND pg_catalog.char_length(pg_catalog.btrim(name)) BETWEEN 2 AND 200
    AND pg_catalog.char_length(pg_catalog.btrim(municipio)) BETWEEN 1 AND 150
    AND pg_catalog.char_length(pg_catalog.btrim(parroquia)) BETWEEN 1 AND 150
    AND block ~* '^(Bloque )?[ABCD]$'
    AND ("rotationBlock" IS NULL OR "rotationBlock" ~* '^(Bloque )?[ABCD]$')
    AND pg_catalog.char_length(pg_catalog.btrim("circuitCode")) BETWEEN 1 AND 200
    AND status = 'NORMAL' AND voltage BETWEEN 0 AND 300
    AND "confirmedReportsCount" BETWEEN 0 AND 1 AND "withoutPowerPercentage" = 0
    AND (estado IS NULL OR pg_catalog.char_length(estado) <= 150)
    AND (sector IS NULL OR pg_catalog.char_length(sector) <= 200)
    AND (barrio IS NULL OR pg_catalog.char_length(barrio) <= 200)
    AND (notes IS NULL OR pg_catalog.char_length(notes) <= 2000)
    AND (lat IS NULL OR lat BETWEEN -90 AND 90)
    AND (lon IS NULL OR lon BETWEEN -180 AND 180)
    AND "submittedAt" > 0
    AND "submittedAt" <= (EXTRACT(EPOCH FROM now() + INTERVAL '10 minutes') * 1000)::BIGINT
);

-- Durable cooldown across serverless cold starts/replicas. Failed delivery
-- still consumes the claim; the client cannot reset or delete this state.
CREATE OR REPLACE FUNCTION public.claim_push_alert(p_block TEXT DEFAULT NULL)
RETURNS BOOLEAN LANGUAGE plpgsql SECURITY DEFINER
SET search_path = ''
AS $function$
DECLARE
    v_uid UUID := auth.uid();
    v_now TIMESTAMPTZ;
    v_last TIMESTAMPTZ;
    v_scope TEXT;
BEGIN
    IF NOT public.is_admin() THEN
        RAISE EXCEPTION 'Administrative access required' USING ERRCODE = '42501';
    END IF;
    IF p_block IS NOT NULL AND p_block NOT IN ('A', 'B', 'C', 'D') THEN
        RAISE EXCEPTION 'Invalid block' USING ERRCODE = '22023';
    END IF;
    PERFORM pg_catalog.pg_advisory_xact_lock(pg_catalog.hashtextextended('pac_push_alert_cooldown', 0));
    v_now := pg_catalog.clock_timestamp();
    SELECT last_claim_at INTO v_last FROM public.push_alert_limits WHERE scope = 'global';
    IF v_last > v_now - INTERVAL '30 seconds' THEN RETURN false; END IF;
    v_scope := 'user:' || v_uid::TEXT;
    SELECT last_claim_at INTO v_last FROM public.push_alert_limits WHERE scope = v_scope;
    IF v_last > v_now - INTERVAL '60 seconds' THEN RETURN false; END IF;
    INSERT INTO public.push_alert_limits(scope, last_claim_at)
        VALUES ('global', v_now), (v_scope, v_now)
        ON CONFLICT (scope) DO UPDATE SET last_claim_at = EXCLUDED.last_claim_at;
    RETURN true;
END;
$function$;
ALTER FUNCTION public.claim_push_alert(TEXT) OWNER TO postgres;
REVOKE ALL ON FUNCTION public.claim_push_alert(TEXT) FROM PUBLIC, anon, authenticated;
GRANT EXECUTE ON FUNCTION public.claim_push_alert(TEXT) TO authenticated;

CREATE INDEX IF NOT EXISTS citizen_reports_timestamp_idx ON public.citizen_reports(timestamp DESC);
CREATE INDEX IF NOT EXISTS citizen_reports_sector_timestamp_idx ON public.citizen_reports("sectorId", timestamp DESC);

DO $realtime$
DECLARE table_name TEXT;
BEGIN
    IF EXISTS (SELECT 1 FROM pg_catalog.pg_publication WHERE pubname = 'supabase_realtime') THEN
        FOREACH table_name IN ARRAY ARRAY['sectors', 'app_config', 'citizen_reports', 'community_locations'] LOOP
            IF NOT EXISTS (SELECT 1 FROM pg_catalog.pg_publication_tables
                WHERE pubname = 'supabase_realtime' AND schemaname = 'public' AND tablename = table_name) THEN
                EXECUTE pg_catalog.format('ALTER PUBLICATION supabase_realtime ADD TABLE public.%I', table_name);
            END IF;
        END LOOP;
    END IF;
END;
$realtime$;
NOTIFY pgrst, 'reload schema';
COMMIT;

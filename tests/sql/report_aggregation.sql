-- ONLY an empty, disposable PostgreSQL 16+ test database, as postgres.
-- Fixture Auth/roles model SQL/RLS; this does not connect to Supabase services.
-- psql -X -v ON_ERROR_STOP=1 -f tests/sql/report_aggregation.sql
\set ON_ERROR_STOP on
DO $$ BEGIN IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname='anon') THEN CREATE ROLE anon NOLOGIN; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname='authenticated') THEN CREATE ROLE authenticated NOLOGIN; END IF; END $$;
CREATE SCHEMA auth;
CREATE TABLE auth.users(id UUID PRIMARY KEY, email TEXT);
CREATE FUNCTION auth.uid() RETURNS UUID LANGUAGE sql STABLE AS $$
    SELECT NULLIF(current_setting('request.jwt.claim.sub', true), '')::UUID;
$$;
GRANT USAGE ON SCHEMA auth TO anon, authenticated;
GRANT EXECUTE ON FUNCTION auth.uid() TO anon, authenticated;
CREATE PUBLICATION supabase_realtime;
\ir ../../supabase_schema.sql

CREATE SCHEMA test_support;
CREATE TABLE test_support.clock(now_ms BIGINT NOT NULL);
INSERT INTO test_support.clock VALUES(floor(EXTRACT(EPOCH FROM clock_timestamp()) * 1000)::BIGINT);
CREATE FUNCTION test_support.assert_true(ok BOOLEAN, label TEXT) RETURNS VOID LANGUAGE plpgsql AS $$
BEGIN
    IF ok IS DISTINCT FROM true THEN RAISE EXCEPTION 'FAIL: %', label; END IF;
END;
$$;
CREATE FUNCTION test_support.expect_error(command TEXT, expected TEXT) RETURNS VOID LANGUAGE plpgsql AS $$
BEGIN
    BEGIN
        EXECUTE command;
    EXCEPTION WHEN OTHERS THEN
        IF SQLSTATE = expected THEN RETURN; END IF;
        RAISE;
    END;
    RAISE EXCEPTION 'Expected SQLSTATE % for %', expected, command;
END;
$$;
GRANT USAGE ON SCHEMA test_support TO anon, authenticated;
GRANT SELECT ON test_support.clock TO anon, authenticated;
INSERT INTO public.sectors(id,name,voltage,"confirmedReportsCount","lastUpdatedMillis") VALUES
 ('aggregation-install','No backfill',117,7,0),
 ('aggregation-main','Main',117,0,0),
 ('aggregation-admin','Admin override',121,0,0),
 ('aggregation-null','Missing voltage',116,0,0),
 ('aggregation-tie','Equal timestamps',116,0,0),
 ('aggregation-bulk','Bulk insert',116,0,0),
 ('aggregation-other','Other sector',116,0,0);
INSERT INTO public.citizen_reports(id,"sectorId","sectorName","hasPower","reportType",voltage,timestamp)
SELECT '10000000-0000-0000-0000-000000000001','aggregation-install','No backfill',false,'SIN_LUZ',0,now_ms-10800000 FROM test_support.clock;
\ir ../../supabase/migrations/20261016140944_aggregate_citizen_reports.sql
\ir ../../supabase/migrations/20261016140944_aggregate_citizen_reports.sql

SELECT test_support.assert_true((SELECT status='NORMAL' AND voltage=117 AND "confirmedReportsCount"=7 AND "lastUpdatedMillis"=0 FROM public.sectors WHERE id='aggregation-install'), 'migration/reapplication do not backfill or seed sector values');
SELECT test_support.assert_true((SELECT count(*)=1 FROM public.citizen_reports), 'migration never invents reports');
SELECT test_support.assert_true(NOT has_schema_privilege('anon','pac_private','USAGE') AND NOT has_schema_privilege('authenticated','pac_private','USAGE'), 'internal schema inaccessible to client roles');
SELECT test_support.assert_true(NOT has_function_privilege('anon','pac_private.aggregate_citizen_reports()','EXECUTE') AND NOT has_function_privilege('authenticated','pac_private.aggregate_citizen_reports()','EXECUTE'), 'trigger function has no public/client EXECUTE');
SELECT test_support.assert_true(NOT has_function_privilege('anon','pac_private.citizen_report_status(text,boolean)','EXECUTE'), 'classification helper not a public RPC');
SELECT test_support.assert_true((SELECT prosecdef AND provolatile='v' AND proconfig @> ARRAY['search_path=""']::TEXT[] FROM pg_proc WHERE oid='pac_private.aggregate_citizen_reports()'::REGPROCEDURE), 'definer uses empty path and VOLATILE snapshots');
SELECT test_support.assert_true((SELECT count(*)=1 FROM pg_trigger WHERE tgrelid='public.citizen_reports'::REGCLASS AND tgname='aggregate_citizen_reports_after_insert' AND NOT tgisinternal), 'one INSERT trigger after repeated migration');
SELECT test_support.assert_true(NOT has_table_privilege('anon','public.citizen_reports','UPDATE') AND NOT has_table_privilege('anon','public.sectors','UPDATE'), 'aggregation adds no direct client UPDATE grant');
SELECT test_support.assert_true(pac_private.citizen_report_status('  bajón  ',true)='IRREGULAR_OUTAGE' AND pac_private.citizen_report_status('FALLA',true)='IRREGULAR_OUTAGE' AND pac_private.citizen_report_status('IRREGULAR',true)='IRREGULAR_OUTAGE', 'legacy type aliases precede hasPower');

SET ROLE anon;
INSERT INTO public.citizen_reports(id,"sectorId","sectorName","hasPower","reportType",voltage,timestamp)
SELECT '20000000-0000-0000-0000-000000000001','aggregation-main','Main',true,'NORMAL',119,now_ms-120000 FROM test_support.clock
ON CONFLICT (id) DO NOTHING;
SELECT test_support.assert_true((SELECT status='NORMAL' AND voltage=119 AND "confirmedReportsCount"=1 AND "withoutPowerPercentage"=0 AND "lastUpdatedMillis"=(SELECT now_ms-120000 FROM test_support.clock) FROM public.sectors WHERE id='aggregation-main'), 'anonymous INSERT updates sector through secured trigger and preserves event time');
INSERT INTO public.citizen_reports(id,"sectorId","sectorName","hasPower","reportType",voltage,timestamp)
SELECT '20000000-0000-0000-0000-000000000002','aggregation-main','Main',true,'FALLA_IRREGULAR',92,now_ms-90000 FROM test_support.clock;
SELECT test_support.assert_true((SELECT status='IRREGULAR_OUTAGE' AND voltage=92 AND "confirmedReportsCount"=2 AND "withoutPowerPercentage"=0 FROM public.sectors WHERE id='aggregation-main'), 'irregular with electricity stays irregular and does not invent a blackout percentage');
INSERT INTO public.citizen_reports(id,"sectorId","sectorName","hasPower","reportType",voltage,timestamp)
SELECT '20000000-0000-0000-0000-000000000003','aggregation-main','Main',true,'NORMAL',120,now_ms-60000 FROM test_support.clock;
SELECT test_support.assert_true((SELECT status='NORMAL' AND voltage=120 AND "confirmedReportsCount"=3 FROM public.sectors WHERE id='aggregation-main'), 'newer NORMAL restores sector independent of previous irregular event');
INSERT INTO public.citizen_reports(id,"sectorId","sectorName","hasPower","reportType",voltage,timestamp)
SELECT '20000000-0000-0000-0000-000000000004','aggregation-main','Main',true,'BAJON',80,now_ms-30000 FROM test_support.clock;
SELECT test_support.assert_true((SELECT status='IRREGULAR_OUTAGE' AND voltage=80 AND "confirmedReportsCount"=4 FROM public.sectors WHERE id='aggregation-main'), 'BAJON has priority over hasPower true');
INSERT INTO public.citizen_reports(id,"sectorId","sectorName","hasPower","reportType",voltage,timestamp)
SELECT '20000000-0000-0000-0000-000000000005','aggregation-main','Main',true,'SIN_LUZ',0,now_ms-20000 FROM test_support.clock;
SELECT test_support.assert_true((SELECT status='SCHEDULED_OUTAGE' AND voltage=0 AND "confirmedReportsCount"=5 AND "withoutPowerPercentage"=20 FROM public.sectors WHERE id='aggregation-main'), 'explicit SIN_LUZ wins contradictory power flag and percentage is one of five');
INSERT INTO public.citizen_reports(id,"sectorId","sectorName","hasPower","reportType",voltage,timestamp)
SELECT '20000000-0000-0000-0000-000000000006','aggregation-main','Main',false,'NORMAL',0,now_ms-10000 FROM test_support.clock;
SELECT test_support.assert_true((SELECT status='SCHEDULED_OUTAGE' AND "confirmedReportsCount"=6 AND "withoutPowerPercentage"=33 FROM public.sectors WHERE id='aggregation-main'), 'fallback without electricity and rounded real two of six percentage');
INSERT INTO public.citizen_reports(id,"sectorId","sectorName","hasPower","reportType",voltage,timestamp)
SELECT '20000000-0000-0000-0000-000000000007','aggregation-main','Main',false,'IRREGULAR_OUTAGE',0,now_ms-5000 FROM test_support.clock;
SELECT test_support.assert_true((SELECT status='IRREGULAR_OUTAGE' AND "confirmedReportsCount"=7 AND "withoutPowerPercentage"=43 AND "lastUpdatedMillis"=(SELECT now_ms-5000 FROM test_support.clock) FROM public.sectors WHERE id='aggregation-main'), 'irregular without electricity counts the actual third outage of seven');

-- Stable id replay: changing duplicate contents cannot change the existing row
-- or produce another aggregation/Realtime UPDATE. No UPDATE privilege is needed.
INSERT INTO public.citizen_reports(id,"sectorId","sectorName","hasPower","reportType",voltage,timestamp)
SELECT '20000000-0000-0000-0000-000000000007','aggregation-main','Main',true,'NORMAL',130,now_ms FROM test_support.clock ON CONFLICT (id) DO NOTHING;
SELECT test_support.assert_true((SELECT status='IRREGULAR_OUTAGE' AND voltage=0 AND "confirmedReportsCount"=7 AND "withoutPowerPercentage"=43 FROM public.sectors WHERE id='aggregation-main'), 'ON CONFLICT DO NOTHING replay does not run aggregation or overwrite report');
SELECT test_support.assert_true((SELECT "reportType"='IRREGULAR_OUTAGE' AND "hasPower"=false FROM public.citizen_reports WHERE id='20000000-0000-0000-0000-000000000007'), 'stable id leaves original report intact');

INSERT INTO public.citizen_reports(id,"sectorId","sectorName","hasPower","reportType",voltage,timestamp)
SELECT '20000000-0000-0000-0000-000000000008','aggregation-main','Main',true,'NORMAL',125,now_ms-3600000 FROM test_support.clock;
SELECT test_support.assert_true((SELECT status='IRREGULAR_OUTAGE' AND voltage=0 AND "confirmedReportsCount"=8 AND "withoutPowerPercentage"=38 AND "lastUpdatedMillis"=(SELECT now_ms-5000 FROM test_support.clock) FROM public.sectors WHERE id='aggregation-main'), 'delayed event in sample changes counts but never newer state or voltage');
INSERT INTO public.citizen_reports(id,"sectorId","sectorName","hasPower","reportType",voltage,timestamp)
SELECT '20000000-0000-0000-0000-000000000009','aggregation-main','Main',true,'NORMAL',126,now_ms-10800000 FROM test_support.clock;
SELECT test_support.assert_true((SELECT status='IRREGULAR_OUTAGE' AND "confirmedReportsCount"=8 AND "withoutPowerPercentage"=38 FROM public.sectors WHERE id='aggregation-main'), 'three-hour offline upload stays historical and cannot change current sample/status');

-- Anonymous callers retain v3 validation and cannot invoke a write helper.
SELECT test_support.expect_error($$UPDATE public.sectors SET status='NORMAL' WHERE id='aggregation-main'$$,'42501');
SELECT test_support.expect_error($$UPDATE public.citizen_reports SET voltage=200 WHERE id='20000000-0000-0000-0000-000000000007'$$,'42501');
SELECT test_support.expect_error($$SELECT pac_private.aggregate_citizen_reports()$$,'42501');
SELECT test_support.expect_error($$INSERT INTO public.citizen_reports("sectorId","sectorName","hasPower","reportType",voltage) VALUES('aggregation-main','Main',false,'NOT_ALLOWED',0)$$,'42501');
SELECT test_support.expect_error($$INSERT INTO public.citizen_reports("sectorId","sectorName","hasPower","reportType",voltage) VALUES('aggregation-main','Main',false,'NORMAL',301)$$,'42501');
SELECT test_support.expect_error($$INSERT INTO public.citizen_reports("sectorId","sectorName","hasPower","reportType",voltage,timestamp) VALUES('aggregation-main','Main',false,'NORMAL',0,9223372036854775807)$$,'42501');
INSERT INTO public.citizen_reports("sectorId","sectorName","hasPower","reportType",voltage,timestamp)
SELECT 'unknown-id','Main',false,'FALLA_IRREGULAR',0,now_ms FROM test_support.clock;
SELECT test_support.assert_true(NOT EXISTS(SELECT FROM public.sectors WHERE id='unknown-id'), 'unknown sector never creates official data or falls back by name');
RESET ROLE;

-- Operator corrections are a watermark: older/equal event times cannot undo
-- them, while a strictly later observation is allowed to replace current state.
UPDATE public.sectors SET status='NORMAL',voltage=121,"lastUpdatedMillis"=(SELECT now_ms-10000 FROM test_support.clock) WHERE id='aggregation-admin';
SET ROLE anon;
INSERT INTO public.citizen_reports("sectorId","sectorName","hasPower","reportType",voltage,timestamp)
SELECT 'aggregation-admin','Admin override',false,'FALLA_IRREGULAR',0,now_ms-15000 FROM test_support.clock;
SELECT test_support.assert_true((SELECT status='NORMAL' AND voltage=121 AND "confirmedReportsCount"=1 AND "lastUpdatedMillis"=(SELECT now_ms-10000 FROM test_support.clock) FROM public.sectors WHERE id='aggregation-admin'), 'older event cannot overwrite administrator watermark');
INSERT INTO public.citizen_reports("sectorId","sectorName","hasPower","reportType",voltage,timestamp)
SELECT 'aggregation-admin','Admin override',false,'FALLA_IRREGULAR',0,now_ms-10000 FROM test_support.clock;
SELECT test_support.assert_true((SELECT status='NORMAL' AND voltage=121 AND "confirmedReportsCount"=2 FROM public.sectors WHERE id='aggregation-admin'), 'equal timestamp preserves administrator state');
INSERT INTO public.citizen_reports("sectorId","sectorName","hasPower","reportType",voltage,timestamp)
SELECT 'aggregation-admin','Admin override',true,'BAJON',85,now_ms FROM test_support.clock;
SELECT test_support.assert_true((SELECT status='IRREGULAR_OUTAGE' AND voltage=85 AND "confirmedReportsCount"=3 AND "withoutPowerPercentage"=67 AND "lastUpdatedMillis"=(SELECT now_ms FROM test_support.clock) FROM public.sectors WHERE id='aggregation-admin'), 'later event supersedes admin state with its original event time');

INSERT INTO public.citizen_reports("sectorId","sectorName","hasPower","reportType",voltage,timestamp)
SELECT 'aggregation-null','Missing voltage',true,'FALLA_IRREGULAR',NULL,now_ms-1000 FROM test_support.clock;
SELECT test_support.assert_true((SELECT status='IRREGULAR_OUTAGE' AND voltage=116 FROM public.sectors WHERE id='aggregation-null'), 'unknown irregular voltage retains previous value instead of inventing 118V');
INSERT INTO public.citizen_reports("sectorId","sectorName","hasPower","reportType",voltage,timestamp)
SELECT 'aggregation-null','Missing voltage',false,'SIN_LUZ',NULL,now_ms FROM test_support.clock;
SELECT test_support.assert_true((SELECT status='SCHEDULED_OUTAGE' AND voltage=0 FROM public.sectors WHERE id='aggregation-null'), 'absent electricity infers zero only when no measured voltage supplied');

-- A multi-row INSERT sees all its rows; count is recomputed, never count+1.
INSERT INTO public.citizen_reports(id,"sectorId","sectorName","hasPower","reportType",voltage,timestamp)
SELECT ('30000000-0000-0000-0000-' || lpad(n::TEXT,12,'0'))::UUID,'aggregation-bulk','Bulk',n%2=0,CASE WHEN n=20 THEN 'BAJON' WHEN n%2=0 THEN 'NORMAL' ELSE 'SIN_LUZ' END,CASE WHEN n%2=0 THEN 100 ELSE 0 END,now_ms-21000+n*1000
FROM generate_series(1,20) AS n CROSS JOIN test_support.clock;
SELECT test_support.assert_true((SELECT status='IRREGULAR_OUTAGE' AND voltage=100 AND "confirmedReportsCount"=20 AND "withoutPowerPercentage"=50 AND "lastUpdatedMillis"=(SELECT now_ms-1000 FROM test_support.clock) FROM public.sectors WHERE id='aggregation-bulk'), '20-row batch has exact sample and newest BAJON state');
INSERT INTO public.citizen_reports(id,"sectorId","sectorName","hasPower","reportType",voltage,timestamp)
SELECT ('30000000-0000-0000-0000-' || lpad(n::TEXT,12,'0'))::UUID,'aggregation-bulk','Bulk',true,'NORMAL',125,now_ms
FROM generate_series(1,20) AS n CROSS JOIN test_support.clock ON CONFLICT (id) DO NOTHING;
SELECT test_support.assert_true((SELECT status='IRREGULAR_OUTAGE' AND "confirmedReportsCount"=20 AND "withoutPowerPercentage"=50 FROM public.sectors WHERE id='aggregation-bulk'), 'duplicate-only batch has empty transition table and makes no aggregation changes');

-- A mixed statement (old/new, unknown/known) cannot leak counts across sectors.
INSERT INTO public.citizen_reports("sectorId","sectorName","hasPower","reportType",voltage,timestamp)
SELECT 'aggregation-other','Other',true,'NORMAL',122,now_ms-1000 FROM test_support.clock UNION ALL
SELECT 'aggregation-other','Other',false,'FALLA_IRREGULAR',0,now_ms-10800000 FROM test_support.clock UNION ALL
SELECT 'another-unknown','Other',false,'FALLA_IRREGULAR',0,now_ms FROM test_support.clock;
SELECT test_support.assert_true((SELECT status='NORMAL' AND voltage=122 AND "confirmedReportsCount"=1 AND "withoutPowerPercentage"=0 FROM public.sectors WHERE id='aggregation-other'), 'window and exact sector ID isolate mixed batch samples');
RESET ROLE;

-- UPDATE and DELETE are intentionally not aggregated: administrator cleanup
-- does not silently reinterpret all historical reports/current overrides.
SELECT test_support.assert_true((SELECT count(*)=4 FROM pg_publication_tables WHERE pubname='supabase_realtime'), 'v3 Realtime publication remains intact for sectors UPDATE delivery');
SELECT 'report aggregation fixture passed' AS result;

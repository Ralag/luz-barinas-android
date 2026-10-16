-- Destructive fixture setup: run ONLY in an empty, disposable PostgreSQL test DB.
-- This models Supabase roles/Auth, not a deployed Supabase Auth server.
-- Command from repository root: psql -X -v ON_ERROR_STOP=1 -f tests/sql/admin_security.sql
\set ON_ERROR_STOP on
CREATE ROLE anon NOLOGIN;
CREATE ROLE authenticated NOLOGIN;
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

-- Fresh installation already denies anonymous administrative writes.
SELECT test_support.assert_true(NOT has_table_privilege('anon', 'public.sectors', 'INSERT'), 'fresh anon sector INSERT denied');
SELECT test_support.assert_true(NOT has_table_privilege('anon', 'public.app_config', 'UPDATE'), 'fresh anon config UPDATE denied');
SELECT test_support.assert_true(NOT has_table_privilege('authenticated', 'public.admin_users', 'INSERT'), 'fresh no self-enrollment');

-- Simulate manually named historical policies/grants, then upgrade twice.
GRANT ALL ON public.sectors, public.app_config, public.community_locations TO anon;
CREATE POLICY legacy_manual_sector_policy ON public.sectors FOR ALL TO anon USING (true) WITH CHECK (true);
CREATE POLICY legacy_manual_config_policy ON public.app_config FOR ALL TO anon USING (true) WITH CHECK (true);
CREATE POLICY legacy_manual_proposal_policy ON public.community_locations FOR ALL TO anon USING (true) WITH CHECK (true);
ALTER TABLE public.sectors DROP COLUMN "isCommunity";
ALTER TABLE public.citizen_reports DROP COLUMN observation CASCADE;
INSERT INTO public.sectors(id,name) VALUES('sec_com_legacy','Legacy community'),('legacy-official','Legacy official');
\ir ../../supabase_migration_v3_admin_security.sql
SELECT test_support.assert_true((SELECT "isCommunity" FROM public.sectors WHERE id='sec_com_legacy'), 'upgrade derives old community prefix');
SELECT test_support.assert_true((SELECT "isCommunity"=false FROM public.sectors WHERE id='legacy-official'), 'upgrade leaves official classification');
UPDATE public.sectors SET "isCommunity"=false WHERE id='sec_com_legacy';
\ir ../../supabase_migration_v3_admin_security.sql
SELECT test_support.assert_true((SELECT "isCommunity"=false FROM public.sectors WHERE id='sec_com_legacy'), 'repeated upgrade preserves deliberate classification');
SELECT test_support.assert_true(EXISTS(SELECT FROM information_schema.columns WHERE table_schema='public' AND table_name='citizen_reports' AND column_name='observation'), 'upgrade restores missing observation column');
DELETE FROM public.sectors WHERE id IN ('sec_com_legacy','legacy-official');
SELECT test_support.assert_true(NOT EXISTS(SELECT FROM pg_policies WHERE policyname LIKE 'legacy_manual_%'), 'all old policies removed');
SELECT test_support.assert_true((SELECT count(*) = 4 FROM pg_publication_tables WHERE pubname='supabase_realtime'), 'realtime publication includes all four tables exactly once');
SELECT test_support.assert_true((SELECT count(*) = 0 FROM public.admin_users), 'installation never seeds an admin');
SELECT test_support.assert_true(NOT has_function_privilege('anon', 'public.is_admin()', 'EXECUTE'), 'anon cannot call admin RPC');
SELECT test_support.assert_true(NOT has_table_privilege('anon', 'public.sectors', 'TRUNCATE,REFERENCES,TRIGGER'), 'anonymous elevated table privileges revoked');
SELECT test_support.assert_true(NOT has_table_privilege('authenticated', 'public.sectors', 'TRUNCATE,REFERENCES,TRIGGER'), 'authenticated role has no truncate or DDL-like grants');

INSERT INTO auth.users VALUES
 ('00000000-0000-0000-0000-000000000001', 'admin@example.invalid'),
 ('00000000-0000-0000-0000-000000000002', 'citizen@example.invalid'),
 ('00000000-0000-0000-0000-000000000003', 'disabled@example.invalid'),
 ('00000000-0000-0000-0000-000000000004', 'second-admin@example.invalid');
INSERT INTO public.admin_users(user_id, enabled) VALUES
 ('00000000-0000-0000-0000-000000000001', true),
 ('00000000-0000-0000-0000-000000000003', false),
 ('00000000-0000-0000-0000-000000000004', true);
INSERT INTO public.sectors(id,name) VALUES ('fixture-sector','Fixture sector');
INSERT INTO public.app_config VALUES ('pac_schedule','{}');

BEGIN;
SET LOCAL ROLE anon;
SELECT test_support.assert_true((SELECT count(*) FROM public.sectors)=1, 'anon can read sectors');
SELECT test_support.assert_true((SELECT count(*) FROM public.app_config)=1, 'anon can read PAC');
SELECT test_support.expect_error($q$INSERT INTO public.sectors(id,name) VALUES('attack','bad')$q$, '42501');
SELECT test_support.expect_error($q$UPDATE public.sectors SET name='bad'$q$, '42501');
SELECT test_support.expect_error($q$DELETE FROM public.sectors$q$, '42501');
SELECT test_support.expect_error($q$INSERT INTO public.app_config VALUES('bad','{}')$q$, '42501');
SELECT test_support.expect_error($q$UPDATE public.app_config SET config_value='{}'$q$, '42501');
SELECT test_support.expect_error($q$DELETE FROM public.app_config$q$, '42501');
SELECT test_support.expect_error($q$SELECT * FROM public.admin_users$q$, '42501');
SELECT test_support.expect_error($q$SELECT public.claim_push_alert(NULL)$q$, '42501');
INSERT INTO public.citizen_reports("sectorId","sectorName","hasPower","reportType",voltage)
    VALUES('fixture-sector','Fixture sector',true,'NORMAL',118);
SELECT test_support.assert_true((SELECT count(*) FROM public.citizen_reports)=1, 'anonymous report accepted');
SELECT test_support.expect_error($q$INSERT INTO public.citizen_reports("sectorId","sectorName","hasPower","reportType",voltage) VALUES('x','x',false,'MADE_UP',0)$q$, '42501');
SELECT test_support.expect_error($q$INSERT INTO public.citizen_reports("sectorId","sectorName","hasPower","reportType",voltage) VALUES('x','x',true,'NORMAL',-1)$q$, '42501');
INSERT INTO public.community_locations(id,name,parroquia,block,"circuitCode","rotationBlock","confirmedReportsCount")
    VALUES('proposal-one','Community one','Barinas','Bloque C','Urban','C',1);
SELECT test_support.expect_error($q$INSERT INTO public.community_locations(id,name,parroquia,block,"circuitCode") VALUES('bad','Invalid','Barinas','E','Urban')$q$, '42501');
SELECT test_support.expect_error($q$SELECT * FROM public.community_locations$q$, '42501');
SELECT test_support.expect_error($q$UPDATE public.community_locations SET name='bad'$q$, '42501');
SELECT test_support.expect_error($q$DELETE FROM public.community_locations$q$, '42501');
ROLLBACK;

BEGIN;
SET LOCAL ROLE authenticated;
SET LOCAL request.jwt.claim.sub='00000000-0000-0000-0000-000000000002';
SET LOCAL request.jwt.claims='{"user_metadata":{"admin":true},"role":"authenticated"}';
SELECT test_support.assert_true(public.is_admin()=false, 'signed-in citizen is not admin');
SELECT test_support.expect_error($q$INSERT INTO public.sectors(id,name) VALUES('attack','bad')$q$, '42501');
SELECT test_support.expect_error($q$INSERT INTO public.app_config VALUES('bad','{}')$q$, '42501');
WITH changed AS (UPDATE public.sectors SET name='bad' RETURNING id)
    SELECT test_support.assert_true(count(*)=0, 'nonadmin UPDATE affects zero rows') FROM changed;
WITH removed AS (DELETE FROM public.app_config RETURNING config_key)
    SELECT test_support.assert_true(count(*)=0, 'nonadmin DELETE affects zero rows') FROM removed;
SELECT test_support.expect_error($q$SELECT * FROM public.admin_users$q$, '42501');
SELECT test_support.expect_error($q$INSERT INTO public.admin_users(user_id,enabled) VALUES(auth.uid(),true)$q$, '42501');
SELECT test_support.expect_error($q$UPDATE public.admin_users SET enabled=true$q$, '42501');
SELECT test_support.expect_error($q$DELETE FROM public.admin_users$q$, '42501');
SELECT test_support.expect_error($q$SELECT public.claim_push_alert('A')$q$, '42501');
INSERT INTO public.citizen_reports("sectorId","sectorName","hasPower","reportType",voltage)
    VALUES('fixture-sector','Fixture sector',false,'FALLA_IRREGULAR',0);
SELECT test_support.assert_true((SELECT count(*) FROM public.community_locations)=0, 'nonadmin cannot see pending proposals');
ROLLBACK;

BEGIN;
SET LOCAL ROLE authenticated;
SET LOCAL request.jwt.claim.sub='';
SELECT test_support.assert_true(public.is_admin()=false, 'authenticated role without a user UUID is not an administrator');
SELECT test_support.expect_error($q$SELECT public.claim_push_alert(NULL)$q$, '42501');
ROLLBACK;

BEGIN;
SET LOCAL ROLE authenticated;
SET LOCAL request.jwt.claim.sub='00000000-0000-0000-0000-000000000003';
SELECT test_support.assert_true(public.is_admin()=false, 'disabled administrator is denied');
SELECT test_support.expect_error($q$INSERT INTO public.sectors(id,name) VALUES('attack','bad')$q$, '42501');
ROLLBACK;

BEGIN;
SET LOCAL ROLE authenticated;
SET LOCAL request.jwt.claim.sub='00000000-0000-0000-0000-000000000001';
SELECT test_support.assert_true(public.is_admin()=true, 'enabled registry UUID authorizes administrator');
INSERT INTO public.sectors(id,name,"isCommunity") VALUES('admin-new','Created by admin',true);
UPDATE public.sectors SET name='Changed by admin' WHERE id='admin-new';
SELECT test_support.assert_true((SELECT name='Changed by admin' FROM public.sectors WHERE id='admin-new'), 'admin sector UPDATE');
DELETE FROM public.sectors WHERE id='admin-new';
SELECT test_support.assert_true(NOT EXISTS(SELECT FROM public.sectors WHERE id='admin-new'), 'admin sector DELETE');
INSERT INTO public.app_config VALUES('admin-new','{"active":true}');
UPDATE public.app_config SET config_value='{"active":false}' WHERE config_key='admin-new';
SELECT test_support.assert_true((SELECT config_value='{"active":false}'::jsonb FROM public.app_config WHERE config_key='admin-new'), 'admin config UPDATE');
DELETE FROM public.app_config WHERE config_key='admin-new';
INSERT INTO public.community_locations(id,name,parroquia,block,"circuitCode") VALUES('admin-proposal','Admin proposal','Barinas','A','Urban');
UPDATE public.community_locations SET name='Reviewed proposal' WHERE id='admin-proposal';
SELECT test_support.assert_true((SELECT name='Reviewed proposal' FROM public.community_locations WHERE id='admin-proposal'), 'admin proposal SELECT/UPDATE');
DELETE FROM public.community_locations WHERE id='admin-proposal';
SELECT test_support.expect_error($q$SELECT * FROM public.admin_users$q$, '42501');
SELECT test_support.expect_error($q$INSERT INTO public.admin_users(user_id,enabled) VALUES('00000000-0000-0000-0000-000000000002',true)$q$, '42501');
SELECT test_support.expect_error($q$SELECT public.claim_push_alert('arbitrary/topic')$q$, '22023');
SELECT test_support.assert_true(public.claim_push_alert('A')=true, 'first durable push claim allowed');
SELECT test_support.assert_true(public.claim_push_alert('A')=false, 'repeat push claim denied');
SELECT test_support.expect_error($q$DELETE FROM public.push_alert_limits$q$, '42501');
RESET ROLE;
UPDATE public.push_alert_limits SET last_claim_at=clock_timestamp()-INTERVAL '31 seconds' WHERE scope='global';
SET LOCAL ROLE authenticated;
SELECT test_support.assert_true(public.claim_push_alert(NULL)=false, 'per-admin cooldown persists after global cooldown');
SET LOCAL request.jwt.claim.sub='00000000-0000-0000-0000-000000000004';
SELECT test_support.assert_true(public.claim_push_alert('D')=true, 'second admin allowed after global cooldown');
SET LOCAL request.jwt.claim.sub='00000000-0000-0000-0000-000000000001';
RESET ROLE;
UPDATE public.admin_users SET enabled=false WHERE user_id='00000000-0000-0000-0000-000000000001';
SET LOCAL ROLE authenticated;
SELECT test_support.assert_true(public.is_admin()=false, 'revocation applies to existing JWT identity');
SELECT test_support.expect_error($q$SELECT public.claim_push_alert(NULL)$q$, '42501');
ROLLBACK;

SELECT 'All fixture authorization and quota tests passed.' AS result;

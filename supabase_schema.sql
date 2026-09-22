-- Supabase SQL Schema for Luz Barinas

-- 1. Create tables

CREATE TABLE public.sectors (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    "circuitCode" TEXT,
    status TEXT NOT NULL DEFAULT 'NORMAL',
    voltage FLOAT8 NOT NULL DEFAULT 118.0,
    "confirmedReportsCount" INT NOT NULL DEFAULT 0,
    "withoutPowerPercentage" INT NOT NULL DEFAULT 0,
    "rotationBlock" TEXT,
    parroquia TEXT,
    "lastUpdatedMillis" BIGINT NOT NULL DEFAULT (extract(epoch from now()) * 1000)
);

CREATE TABLE public.app_config (
    config_key TEXT PRIMARY KEY,
    config_value JSONB NOT NULL
);

CREATE TABLE public.citizen_reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    "sectorId" TEXT NOT NULL,
    "sectorName" TEXT NOT NULL,
    "hasPower" BOOLEAN NOT NULL,
    "reportType" TEXT NOT NULL,
    voltage FLOAT8,
    "deviceOrigin" TEXT,
    timestamp BIGINT NOT NULL DEFAULT (extract(epoch from now()) * 1000)
);

CREATE TABLE public.community_locations (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    municipio TEXT NOT NULL,
    parroquia TEXT NOT NULL,
    block TEXT NOT NULL,
    "circuitCode" TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'NORMAL',
    voltage FLOAT8 NOT NULL DEFAULT 118.0,
    "confirmedReportsCount" INT NOT NULL DEFAULT 0,
    "withoutPowerPercentage" INT NOT NULL DEFAULT 0,
    "rotationBlock" TEXT,
    "submittedAt" BIGINT NOT NULL DEFAULT (extract(epoch from now()) * 1000)
);

-- 2. Enable Row Level Security (RLS) but allow anonymous access for now (matching loose Firebase rules)
ALTER TABLE public.sectors ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.app_config ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.citizen_reports ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.community_locations ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Allow anonymous read access on sectors" ON public.sectors FOR SELECT USING (true);
CREATE POLICY "Allow anonymous update on sectors" ON public.sectors FOR UPDATE USING (true);
CREATE POLICY "Allow anonymous insert on sectors" ON public.sectors FOR INSERT WITH CHECK (true);

CREATE POLICY "Allow anonymous read access on app_config" ON public.app_config FOR SELECT USING (true);
CREATE POLICY "Allow anonymous update on app_config" ON public.app_config FOR UPDATE USING (true);
CREATE POLICY "Allow anonymous insert on app_config" ON public.app_config FOR INSERT WITH CHECK (true);

CREATE POLICY "Allow anonymous read access on citizen_reports" ON public.citizen_reports FOR SELECT USING (true);
CREATE POLICY "Allow anonymous insert on citizen_reports" ON public.citizen_reports FOR INSERT WITH CHECK (true);

CREATE POLICY "Allow anonymous read access on community_locations" ON public.community_locations FOR SELECT USING (true);
CREATE POLICY "Allow anonymous insert on community_locations" ON public.community_locations FOR INSERT WITH CHECK (true);

-- 3. Enable Realtime on sectors and app_config
alter publication supabase_realtime add table public.sectors;
alter publication supabase_realtime add table public.app_config;

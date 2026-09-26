-- ====================================================================
-- PAC BARINAS — Migración SQL v2.0 (Retrocompatibilidad & Schema Cache)
-- ====================================================================
-- Este script realiza 3 tareas fundamentales:
-- 1. Añade las nuevas columnas granulares (estado, municipio, parroquia,
--    sector, barrio, notes, lat, lon) con valores por defecto seguros.
-- 2. Migra/retroalimenta los sectores comunitarios y oficiales antiguos
--    para evitar campos nulos que rompan la plataforma.
-- 3. Fuerza a Supabase (PostgREST) a recargar la caché del esquema
--    eliminando de raíz el error: "Could not find 'barrio' in schema cache".
-- ====================================================================

-- 1. Asegurar columnas en tabla 'sectors'
ALTER TABLE public.sectors ADD COLUMN IF NOT EXISTS estado TEXT DEFAULT 'Barinas';
ALTER TABLE public.sectors ADD COLUMN IF NOT EXISTS municipio TEXT DEFAULT 'Barinas';
ALTER TABLE public.sectors ADD COLUMN IF NOT EXISTS parroquia TEXT DEFAULT 'Barinas';
ALTER TABLE public.sectors ADD COLUMN IF NOT EXISTS sector TEXT;
ALTER TABLE public.sectors ADD COLUMN IF NOT EXISTS barrio TEXT DEFAULT '';
ALTER TABLE public.sectors ADD COLUMN IF NOT EXISTS notes TEXT DEFAULT '';
ALTER TABLE public.sectors ADD COLUMN IF NOT EXISTS lat FLOAT8;
ALTER TABLE public.sectors ADD COLUMN IF NOT EXISTS lon FLOAT8;

-- 2. Asegurar columnas en tabla 'community_locations' (Bandeja de entrada)
ALTER TABLE public.community_locations ADD COLUMN IF NOT EXISTS estado TEXT DEFAULT 'Barinas';
ALTER TABLE public.community_locations ADD COLUMN IF NOT EXISTS municipio TEXT NOT NULL DEFAULT 'Barinas';
ALTER TABLE public.community_locations ADD COLUMN IF NOT EXISTS parroquia TEXT NOT NULL DEFAULT 'Barinas';
ALTER TABLE public.community_locations ADD COLUMN IF NOT EXISTS sector TEXT;
ALTER TABLE public.community_locations ADD COLUMN IF NOT EXISTS barrio TEXT DEFAULT '';
ALTER TABLE public.community_locations ADD COLUMN IF NOT EXISTS notes TEXT DEFAULT '';
ALTER TABLE public.community_locations ADD COLUMN IF NOT EXISTS lat FLOAT8;
ALTER TABLE public.community_locations ADD COLUMN IF NOT EXISTS lon FLOAT8;

-- 3. Asegurar columna 'observation' en 'citizen_reports'
ALTER TABLE public.citizen_reports ADD COLUMN IF NOT EXISTS observation TEXT;

-- 4. RETROCOMPATIBILIDAD: Actualizar registros antiguos preexistentes
-- Rellena sectores que fueron aprobados antes de crear estas columnas
UPDATE public.sectors 
SET 
    estado = COALESCE(NULLIF(TRIM(estado), ''), 'Barinas'),
    municipio = COALESCE(NULLIF(TRIM(municipio), ''), 'Barinas'),
    parroquia = COALESCE(NULLIF(TRIM(parroquia), ''), 'Barinas'),
    sector = COALESCE(NULLIF(TRIM(sector), ''), name),
    barrio = COALESCE(barrio, ''),
    notes = COALESCE(notes, '')
WHERE estado IS NULL 
   OR municipio IS NULL 
   OR parroquia IS NULL 
   OR sector IS NULL 
   OR barrio IS NULL;

UPDATE public.community_locations
SET 
    estado = COALESCE(NULLIF(TRIM(estado), ''), 'Barinas'),
    municipio = COALESCE(NULLIF(TRIM(municipio), ''), 'Barinas'),
    parroquia = COALESCE(NULLIF(TRIM(parroquia), ''), 'Barinas'),
    sector = COALESCE(NULLIF(TRIM(sector), ''), name),
    barrio = COALESCE(barrio, ''),
    notes = COALESCE(notes, '')
WHERE estado IS NULL 
   OR municipio IS NULL 
   OR parroquia IS NULL 
   OR sector IS NULL 
   OR barrio IS NULL;

-- 5. RECARGAR CACHÉ DE POSTGREST INMEDIATAMENTE
-- Esto le notifica al motor HTTP de Supabase que actualice su schema cache
NOTIFY pgrst, 'reload schema';
NOTIFY pgrst, 'reload config';

-- Confirmación visual
SELECT 'Migración PAC Barinas v2 completada con éxito. Schema cache recargado.' AS status;

import './pac-schedule-core.js';
import { createPacApi } from './router.mjs';

// This gateway contains public legacy reads. Sensitive routes independently
// verify the caller through Supabase Auth and the database admin/quota RPCs.
const env = {
    SUPABASE_URL: Deno.env.get('SUPABASE_URL'),
    SUPABASE_ANON_KEY: Deno.env.get('SUPABASE_ANON_KEY'),
    SUPABASE_PUBLISHABLE_KEY: Deno.env.get('SUPABASE_PUBLISHABLE_KEY'),
    PAC_ALLOWED_ORIGINS: Deno.env.get('PAC_ALLOWED_ORIGINS'),
    FIREBASE_SERVICE_ACCOUNT: Deno.env.get('FIREBASE_SERVICE_ACCOUNT')
};

Deno.serve(createPacApi({ env, scheduleCore: Reflect.get(globalThis, 'PacScheduleCore') }));

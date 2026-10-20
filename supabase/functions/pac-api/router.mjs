import { createFirebaseSender, FirebaseUnavailableError } from './firebase.mjs';

const encoder = new TextEncoder();
const PUSH_FIELDS = new Set(['title', 'message', 'is_alarm', 'block']);
const REPORT_FIELDS = new Set(['sector_id', 'has_power', 'timestamp', 'report_type', 'voltage_reading', 'observation', 'client_device_id']);
const REPORT_TYPES = new Set(['NORMAL', 'SIN_LUZ', 'BAJON', 'FALLA_IRREGULAR', 'IRREGULAR_OUTAGE']);
const DEFAULT_ORIGINS = ['https://www.pacbarinas.site', 'https://pacbarinas.site'];

export function parsePushPayload(body) {
    if (!body || typeof body !== 'object' || Array.isArray(body) || Object.keys(body).some(key => !PUSH_FIELDS.has(key))) return null;
    if (typeof body.title !== 'string' || typeof body.message !== 'string') return null;
    const title = body.title.trim();
    const message = body.message.trim();
    if (!title || title.length > 120 || !message || message.length > 2000) return null;
    if (/[\u0000-\u001f\u007f]/.test(title) || /[\u0000-\u0008\u000b\u000c\u000e-\u001f\u007f]/.test(message)) return null;
    if (body.is_alarm !== undefined && typeof body.is_alarm !== 'boolean') return null;
    if (body.block !== undefined && !['A', 'B', 'C', 'D'].includes(body.block)) return null;
    const data = { title, message, is_alarm: String(body.is_alarm ?? false) };
    if (encoder.encode(JSON.stringify(data)).byteLength > 3500) return null;
    return { data, block: body.block ?? null, topic: body.block ? `block_${body.block}` : 'barinas_global' };
}

export function parseTelemetry(body, nowMs) {
    if (!body || typeof body !== 'object' || Array.isArray(body) || Object.keys(body).some(key => !REPORT_FIELDS.has(key))) return null;
    if (typeof body.sector_id !== 'string' || !body.sector_id.trim() || body.sector_id.length > 200 || /[\u0000-\u001f\u007f]/.test(body.sector_id)) return null;
    if (typeof body.has_power !== 'boolean' || !REPORT_TYPES.has(body.report_type)) return null;
    if (!Number.isSafeInteger(body.timestamp) || body.timestamp <= 0 || body.timestamp > nowMs + 600_000) return null;
    if (body.voltage_reading != null && (typeof body.voltage_reading !== 'number' || !Number.isFinite(body.voltage_reading) || body.voltage_reading < 0 || body.voltage_reading > 300)) return null;
    if (body.observation != null && (typeof body.observation !== 'string' || body.observation.length > 2000)) return null;
    if (body.client_device_id != null && (typeof body.client_device_id !== 'string' || body.client_device_id.length > 100)) return null;
    return {
        sectorId: body.sector_id.trim(), hasPower: body.has_power, reportType: body.report_type,
        timestamp: body.timestamp, voltage: body.voltage_reading ?? null,
        observation: body.observation?.trim() || null, deviceOrigin: body.client_device_id || 'barinas_citizen_app'
    };
}

export function sectorDto(sector) {
    const points = typeof sector.polygonPointsRaw === 'string' ? sector.polygonPointsRaw :
        Number.isFinite(sector.lat) && Number.isFinite(sector.lon) ? `${sector.lat},${sector.lon}` : '';
    return {
        id: sector.id, name: sector.name, circuit_code: sector.circuitCode || '',
        status: sector.status || 'NORMAL', voltage: Number(sector.voltage) || 0,
        confirmed_reports: Number(sector.confirmedReportsCount) || 0,
        without_power_percentage: Number(sector.withoutPowerPercentage) || 0,
        last_updated: Number(sector.lastUpdatedMillis) || 0,
        rotation_block: sector.rotationBlock || '', polygon_points: points
    };
}

function readConfig(env) {
    const url = env.SUPABASE_URL;
    const key = env.SUPABASE_PUBLISHABLE_KEY || env.SUPABASE_ANON_KEY;
    if (!url || !key || key.startsWith('sb_secret_')) return null;
    try {
        const parsed = new URL(url);
        if (parsed.protocol !== 'https:' && !(parsed.protocol === 'http:' && ['localhost', '127.0.0.1'].includes(parsed.hostname))) return null;
        if (key.split('.').length === 3) {
            const claims = JSON.parse(atob(key.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')));
            if (claims.role !== 'anon') return null;
        }
        return { url: parsed.origin, key };
    } catch { return null; }
}

function routePath(url) {
    let path = url.pathname.replace(/\/$/, '');
    for (const prefix of ['/functions/v1/pac-api', '/pac-api']) {
        if (path === prefix || path.startsWith(`${prefix}/`)) { path = path.slice(prefix.length); break; }
    }
    if (path && !path.startsWith('/api/')) path = `/api${path}`;
    return path;
}

async function readJson(request) {
    const length = request.headers.get('content-length');
    if (length && (!/^\d+$/.test(length) || Number(length) > 8192)) return null;
    // Limit bytes while reading chunked requests as well as Content-Length bodies.
    const reader = request.body?.getReader();
    if (!reader) return null;
    const chunks = [];
    let size = 0;
    try {
        while (true) {
            const chunk = await reader.read();
            if (chunk.done) break;
            size += chunk.value.byteLength;
            if (size > 8192) { await reader.cancel(); return null; }
            chunks.push(chunk.value);
        }
        const bytes = new Uint8Array(size);
        let offset = 0;
        for (const chunk of chunks) { bytes.set(chunk, offset); offset += chunk.byteLength; }
        return JSON.parse(new TextDecoder('utf-8', { fatal: true }).decode(bytes));
    } catch { return null; }
}

/** Legacy routes run against the caller's anon/JWT role; never service_role. */
export function createPacApi({ env = {}, fetchImpl = fetch, now = Date.now, scheduleCore, sendPushImpl, logger = console } = {}) {
    const sendPush = sendPushImpl || createFirebaseSender({ fetchImpl, now });
    const allowedOrigins = new Set((env.PAC_ALLOWED_ORIGINS || DEFAULT_ORIGINS.join(',')).split(',').map(value => value.trim()).filter(Boolean));
    const config = readConfig(env);
    let cachedSchedule = null;

    async function rest(path, { token, method = 'GET', body, prefer } = {}) {
        const headers = { apikey: config.key, 'Content-Type': 'application/json' };
        // Publishable keys belong in apikey, not Authorization. Anon JWT keys
        // remain valid as Authorization for legacy Supabase projects.
        if (token) headers.Authorization = `Bearer ${token}`;
        else if (config.key.split('.').length === 3) headers.Authorization = `Bearer ${config.key}`;
        if (prefer) headers.Prefer = prefer;
        const response = await fetchImpl(`${config.url}${path}`, { method, headers, body: body === undefined ? undefined : JSON.stringify(body), signal: AbortSignal.timeout(10_000) });
        if (!response.ok) {
            const error = new Error('upstream_failed');
            error.status = response.status;
            throw error;
        }
        return response.status === 204 || prefer === 'return=minimal' ? null : response.json();
    }

    async function schedule() {
        if (cachedSchedule?.expiresAt > now()) return cachedSchedule.value;
        const rows = await rest('/rest/v1/app_config?select=config_value&config_key=eq.pac_schedule&limit=1');
        if (!scheduleCore) throw new Error('schedule_unavailable');
        const value = scheduleCore.validateSchedule(rows[0]?.config_value);
        cachedSchedule = { value, expiresAt: now() + 60_000 };
        return value;
    }

    async function sector(id) {
        const params = new URLSearchParams({ select: '*', id: `eq.${id}`, limit: '1' });
        const rows = await rest(`/rest/v1/sectors?${params}`);
        return rows[0] || null;
    }

    return async function pacApi(request) {
        const url = new URL(request.url);
        const path = routePath(url);
        const push = path === '/api/sendPushAlert';
        const report = path === '/api/v1/telemetry/report';
        const read = path === '/api/getMonthlyWeeks' || path === '/api/getOutagePrediction' || path === '/api/v1/sectors' || /^\/api\/v1\/sectors\/[^/]+$/.test(path);
        const methods = push || report ? 'POST, OPTIONS' : 'GET, OPTIONS';
        const headers = new Headers({ 'Content-Type': 'application/json; charset=utf-8', 'Cache-Control': 'no-store', Vary: 'Origin', Allow: methods, 'X-Content-Type-Options': 'nosniff' });
        const respond = (status, data) => new Response(status === 204 ? null : JSON.stringify(data), { status, headers });
        if (!push && !report && !read) return respond(404, { error: 'Ruta no encontrada.' });
        if (request.method !== 'OPTIONS' && request.method !== (push || report ? 'POST' : 'GET')) return respond(405, { error: 'Método no permitido.' });
        const origin = request.headers.get('origin');
        if ((push || report) && origin && !allowedOrigins.has(origin)) return respond(403, { error: 'Origen no permitido.' });
        headers.set('Access-Control-Allow-Origin', push || report ? (origin || DEFAULT_ORIGINS[0]) : '*');
        headers.set('Access-Control-Allow-Methods', methods);
        headers.set('Access-Control-Allow-Headers', 'Authorization, Content-Type, apikey, x-client-info');
        if (request.method === 'OPTIONS') return respond(204);
        if (!config) return respond(503, { error: 'Servicio temporalmente no disponible.' });

        try {
            if (push) {
                const match = /^Bearer ([A-Za-z0-9._~-]+)$/i.exec(request.headers.get('authorization') || '');
                if (!match || match[1].length > 8192) return respond(401, { error: 'Inicia sesión para continuar.' });
                const token = match[1];
                let user;
                try { user = await rest('/auth/v1/user', { token }); }
                catch (error) { return respond(error.status === 401 || error.status === 403 ? 401 : 503, { error: 'No se pudo validar la sesión.' }); }
                if (!user?.id) return respond(401, { error: 'La sesión no es válida.' });
                let admin;
                try { admin = await rest('/rest/v1/rpc/is_admin', { token, method: 'POST', body: {} }); }
                catch (error) { return respond(error.status === 403 ? 403 : 503, { error: 'No se pudo verificar el permiso administrativo.' }); }
                if (admin !== true) return respond(403, { error: 'Acceso administrativo requerido.' });
                if (!/^application\/json(?:\s*;|$)/i.test(request.headers.get('content-type') || '')) return respond(415, { error: 'Envía contenido JSON.' });
                const payload = parsePushPayload(await readJson(request));
                if (!payload) return respond(400, { error: 'Revisa título, mensaje, alarma y bloque.' });
                let claim;
                try { claim = await rest('/rest/v1/rpc/claim_push_alert', { token, method: 'POST', body: { p_block: payload.block } }); }
                catch (error) { return respond(error.status === 403 ? 403 : 503, { error: 'No se pudo verificar el límite de avisos.' }); }
                if (claim !== true) { headers.set('Retry-After', '60'); return respond(429, { error: 'Espera un minuto antes de emitir otro aviso.' }); }
                try { return respond(200, { success: true, messageId: await sendPush(payload, env) }); }
                catch (error) {
                    logger.error('pac_push_delivery_unavailable');
                    return respond(error instanceof FirebaseUnavailableError ? 503 : 502, { error: 'No se pudo enviar el aviso.' });
                }
            }
            if (report) {
                if (!/^application\/json(?:\s*;|$)/i.test(request.headers.get('content-type') || '')) return respond(415, { error: 'Envía contenido JSON.' });
                const payload = parseTelemetry(await readJson(request), now());
                if (!payload) return respond(400, { error: 'Revisa los datos del reporte.' });
                const target = await sector(payload.sectorId);
                if (!target) return respond(404, { error: 'Sector no encontrado.' });
                await rest('/rest/v1/citizen_reports', { method: 'POST', body: { ...payload, sectorName: target.name }, prefer: 'return=minimal' });
                // The database trigger owns aggregation. If this optional read
                // fails, the already accepted report must not be retried merely
                // because its display metrics were temporarily unavailable.
                let updated = null;
                try { updated = await sector(payload.sectorId); } catch { /* nullable legacy DTO fields */ }
                return respond(200, { success: true, message: 'Reporte recibido.', updated_sector_status: updated?.status ?? null, total_sector_reports: updated?.confirmedReportsCount ?? null });
            }
            if (path === '/api/v1/sectors') {
                const rows = await rest('/rest/v1/sectors?select=*&order=name.asc&limit=1000');
                headers.set('Cache-Control', 'public, max-age=30, s-maxage=60, stale-while-revalidate=120');
                return respond(200, rows.map(sectorDto));
            }
            if (path.startsWith('/api/v1/sectors/')) {
                let id;
                try { id = decodeURIComponent(path.slice('/api/v1/sectors/'.length)); } catch { return respond(400, { error: 'Sector inválido.' }); }
                if (!id || id.length > 200 || /[\u0000-\u001f\u007f]/.test(id)) return respond(400, { error: 'Sector inválido.' });
                const target = await sector(id);
                if (!target) return respond(404, { error: 'Sector no encontrado.' });
                headers.set('Cache-Control', 'public, max-age=30, s-maxage=60, stale-while-revalidate=120');
                return respond(200, sectorDto(target));
            }
            if (path === '/api/getMonthlyWeeks') {
                const monthName = url.searchParams.get('monthName');
                const rawDays = url.searchParams.get('totalDays');
                if (!monthName || monthName.length > 30 || /[\u0000-\u001f\u007f]/.test(monthName) || !/^(28|29|30|31)$/.test(rawDays || '')) return respond(400, { error: 'Mes y número de días inválidos.' });
                const weeks = scheduleCore.monthlyWeeks(await schedule(), monthName, Number(rawDays));
                headers.set('Cache-Control', 'public, max-age=30, s-maxage=60, stale-while-revalidate=120');
                return respond(200, { weeks });
            }
            const id = url.searchParams.get('sectorId');
            if (!id || id.length > 200 || /[\u0000-\u001f\u007f]/.test(id)) return respond(400, { error: 'Sector inválido.' });
            const target = await sector(id);
            if (!target) return respond(404, { error: 'Sector no encontrado.' });
            const block = String(target.rotationBlock || '').toUpperCase().replace(/^BLOQUE\s*/, '').trim();
            const window = scheduleCore.findWindow(await schedule(), block, now());
            if (!window) return respond(404, { error: 'No hay cortes programados para este bloque.', code: 'NO_SCHEDULED_WINDOW' });
            headers.set('Cache-Control', 'public, max-age=15, s-maxage=30, stale-while-revalidate=30');
            return respond(200, {
                sectorId: target.id, sectorName: target.name, rotationBlock: target.rotationBlock,
                nextEstimatedStartMillis: window.startMillis, nextEstimatedEndMillis: window.endMillis,
                estimatedDurationHours: (window.endMillis - window.startMillis) / 3_600_000,
                confidencePercentage: 0,
                algorithmDetail: `Cronograma referencial PAC ${block}: ${window.timeLabel || ''}`.trim(),
                hoursUntilWindow: Math.max(0, (window.startMillis - now()) / 3_600_000)
            });
        } catch {
            logger.error('pac_api_upstream_unavailable');
            return respond(503, { error: 'Servicio temporalmente no disponible.' });
        }
    };
}

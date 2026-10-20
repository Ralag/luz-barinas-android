const TOKEN_URL = 'https://oauth2.googleapis.com/token';
const SCOPE = 'https://www.googleapis.com/auth/firebase.messaging';
const encoder = new TextEncoder();

export class FirebaseUnavailableError extends Error {}

function base64url(bytes) {
    let binary = '';
    for (const byte of bytes) binary += String.fromCharCode(byte);
    return btoa(binary).replace(/=/g, '').replace(/\+/g, '-').replace(/\//g, '_');
}

function encodeJson(value) {
    return base64url(encoder.encode(JSON.stringify(value)));
}

function readServiceAccount(env) {
    try {
        const value = JSON.parse(env.FIREBASE_SERVICE_ACCOUNT || 'null');
        if (!value || value.type !== 'service_account' ||
            !/^[a-z][a-z0-9-]{4,61}[a-z0-9]$/.test(value.project_id) ||
            typeof value.client_email !== 'string' || !/^[^\s@]+@[^\s@]+\.iam\.gserviceaccount\.com$/.test(value.client_email) ||
            typeof value.private_key !== 'string' || !value.private_key.includes('-----BEGIN PRIVATE KEY-----') ||
            (value.token_uri && value.token_uri !== TOKEN_URL)) return null;
        return value;
    } catch {
        return null;
    }
}

/** OAuth JWTs and FCM HTTP v1 use WebCrypto; no Firebase Admin bundle or key in a client. */
export function createFirebaseSender({ fetchImpl = fetch, cryptoImpl = globalThis.crypto, now = Date.now } = {}) {
    let cachedToken = null;
    return async function sendFirebase(payload, env) {
        // This function is called only after Auth, role, validation and quota.
        const account = readServiceAccount(env);
        if (!account || !cryptoImpl?.subtle) throw new FirebaseUnavailableError('firebase_not_configured');
        const cacheKey = `${account.client_email}:${account.private_key}`;
        let accessToken = cachedToken?.key === cacheKey && cachedToken.expiresAt > now() + 60_000 ? cachedToken.value : null;
        if (!accessToken) {
            try {
                const pem = account.private_key.replace(/-----BEGIN PRIVATE KEY-----|-----END PRIVATE KEY-----|\s/g, '');
                const keyBytes = Uint8Array.from(atob(pem), char => char.charCodeAt(0));
                const key = await cryptoImpl.subtle.importKey('pkcs8', keyBytes, { name: 'RSASSA-PKCS1-v1_5', hash: 'SHA-256' }, false, ['sign']);
                const issuedAt = Math.floor(now() / 1000);
                const jwt = `${encodeJson({ alg: 'RS256', typ: 'JWT' })}.${encodeJson({ iss: account.client_email, scope: SCOPE, aud: TOKEN_URL, iat: issuedAt, exp: issuedAt + 3600 })}`;
                const signature = await cryptoImpl.subtle.sign('RSASSA-PKCS1-v1_5', key, encoder.encode(jwt));
                const response = await fetchImpl(TOKEN_URL, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: new URLSearchParams({ grant_type: 'urn:ietf:params:oauth:grant-type:jwt-bearer', assertion: `${jwt}.${base64url(new Uint8Array(signature))}` }),
                    signal: AbortSignal.timeout(10_000)
                });
                if (!response.ok) throw new Error('oauth_failed');
                const token = await response.json();
                if (typeof token.access_token !== 'string' || !token.access_token || !Number.isFinite(token.expires_in) || token.expires_in <= 0) throw new Error('oauth_invalid');
                accessToken = token.access_token;
                cachedToken = { key: cacheKey, value: accessToken, expiresAt: now() + Math.min(token.expires_in, 3600) * 1000 };
            } catch {
                throw new FirebaseUnavailableError('firebase_authorization_unavailable');
            }
        }
        const response = await fetchImpl(`https://fcm.googleapis.com/v1/projects/${account.project_id}/messages:send`, {
            method: 'POST',
            headers: { Authorization: `Bearer ${accessToken}`, 'Content-Type': 'application/json' },
            body: JSON.stringify({ message: { topic: payload.topic, data: payload.data } }),
            signal: AbortSignal.timeout(10_000)
        });
        if (!response.ok) {
            if (response.status === 401) cachedToken = null;
            throw new Error('firebase_delivery_failed');
        }
        const result = await response.json();
        if (typeof result.name !== 'string' || !result.name) throw new Error('firebase_delivery_invalid');
        return result.name;
    };
}

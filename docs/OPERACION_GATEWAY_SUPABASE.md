# Operación del gateway compatible en Supabase

Estado: 16 de octubre de 2026. `pac-api` está desplegada y se comprobaron GET de cronograma/119 sectores y rechazo 401 de avisos sin sesión. El titular eligió mantener por ahora **solo el envío administrativo de alertas en Vercel**. FIREBASE_SERVICE_ACCOUNT no se trasladó a Supabase: la ruta push Edge permanece preparada y cerrada sin esa configuración; no es la ruta pública seleccionada.

## Qué cambia y qué tráfico permanece

`pac-api` reemplaza las funciones Node de Vercel mediante una Edge Function de Supabase. Conserva las rutas que conocen las versiones Android anteriores, traduciendo sus DTO al esquema actual. Una reescritura externa puede mantener `/api/...` en el dominio público mientras procesa la petición Supabase.

Esto elimina las invocaciones de **Vercel Functions** para esas rutas cuando se retiran los archivos de `api/` y se activa la reescritura. La solicitud inicial, transferencia, caché y contenido estático siguen pasando por Vercel si el cliente utiliza ese dominio. Supabase también tiene límites de invocaciones, transferencia y base de datos. No convierte el sistema en un servicio sin cuotas. Las versiones nuevas deben leer datos públicos directamente de Supabase y calcular el calendario con el núcleo compartido, para reducir llamadas de cálculo.

Las versiones anteriores se benefician del cambio de servidor si conservan el mismo dominio y ruta y aceptan la respuesta compatible. Una reescritura no modifica el APK instalado, sus anuncios, su interfaz ni el mapa que lleva empaquetado. Si todo el proyecto o dominio queda suspendido, la reescritura también deja de atender; cambiar el destino no puede evitar esa suspensión.

## Archivos y responsabilidades

| Archivo | Responsabilidad |
|---|---|
| `supabase/functions/pac-api/index.ts` | Carga el núcleo de horarios y variables del entorno y registra `Deno.serve`. |
| `router.mjs` | Router HTTP con `Request`/`Response`, validación, acceso REST bajo RLS y DTO compatibles. Sus dependencias se inyectan en las pruebas. |
| `firebase.mjs` | OAuth de cuenta de servicio, firma RS256 con WebCrypto y envío mediante FCM HTTP v1. No importa Firebase Admin ni un SDK adicional. |
| `pac-schedule-core.js` | Copia exacta de `admin-web/public/pac-schedule-core.js`. Calcula fechas y turnos en Caracas. Una prueba exige igualdad de bytes. |
| `supabase/config.toml` | Declara `verify_jwt=false` para este router mixto público/administrativo. |
| `admin-web/tests/pac-edge-api.test.cjs` | Pruebas HTTP, permisos, contratos, límites y firma criptográfica sin enviar mensajes reales. |

Los archivos de despliegue tienen copia en `admin-web/supabase/` para que ambos repositorios incluyan el contrato del servidor. Actualizar el núcleo exige copiarlo también a ambas carpetas de la función; la prueba de igualdad detecta divergencias en el repositorio web.

## Contratos HTTP

El router acepta el prefijo de despliegue `/functions/v1/pac-api/api/`, el prefijo `/pac-api/api/` y `/api/`. También puede recibir las rutas sin el segundo prefijo `api`. Las rutas desconocidas producen 404 y los métodos incorrectos 405. OPTIONS responde antes de contactar a Supabase.

| Ruta después de `/api/` | Método | Respuesta |
|---|---|---|
| `v1/sectors` | GET | Array de `SectorDto` con nombres `snake_case`. |
| `v1/sectors/{sectorId}` | GET | Un `SectorDto` o 404. El ID se codifica como un valor de filtro REST. |
| `getMonthlyWeeks?monthName=Octubre&totalDays=31` | GET | `{weeks:[...]}` con las cuatro semanas del núcleo compartido. |
| `getOutagePrediction?sectorId=...` | GET | Ventana programada compatible con `OutagePrediction`. Si no existe, 404 con `code:NO_SCHEDULED_WINDOW`. |
| `v1/telemetry/report` | POST | Resultado compatible con `TelemetryReportResponse`. Solo inserta eventos ciudadanos. |
| `sendPushAlert` | POST | Requiere JWT administrativo, permiso vigente y reserva de cuota. Retorna `success` y `messageId` únicamente tras una respuesta válida de FCM. |

`sectorDto` traduce `circuitCode`, `confirmedReportsCount`, `withoutPowerPercentage`, `lastUpdatedMillis` y `rotationBlock` a los campos que Moshi espera. Usa `polygonPointsRaw` cuando existe o una coordenada `lat,lon` cuando está disponible. No inventa una ubicación si faltan ambas. El estado y métricas proceden de la fila actual, no de una medición nueva del gateway.

Las lecturas de sectores y mes permiten caché pública de 30 segundos en cliente y 60 en caché compartida; la predicción usa 15 y 30 segundos. La configuración PAC se conserva 60 segundos en memoria por instancia. Solo se guardan respuestas correctas; errores, reportes y avisos son `no-store`. Estas cachés breves pueden demorar la visibilidad de un cambio hasta su vencimiento.

## Funciones del router

`createPacApi({env,fetchImpl,now,scheduleCore,sendPushImpl,logger})` crea el manejador `pacApi(request)`. Las dependencias permiten verificar rutas completas sin Supabase ni Google reales. `readConfig` valida URL y clave pública: rechaza `sb_secret_` y JWT con rol distinto de `anon`. El acceso REST interno usa `apikey` y, para operaciones administrativas, el JWT del usuario. Nunca utiliza `SUPABASE_SERVICE_ROLE_KEY`.

`routePath` retira solamente los prefijos conocidos; no ofrece un proxy de URL arbitraria. `rest` centraliza cabeceras, cuerpo JSON, timeout de 10 segundos y errores sin revelar el contenido privado de la respuesta. `schedule` carga `app_config.pac_schedule`, lo valida mediante `validateSchedule` y conserva su copia normalizada durante un minuto. `sector` consulta un ID específico. El callback local `respond` aplica las cabeceras a todas las respuestas, incluidas las de error.

`readJson` limita el cuerpo a 8192 bytes, tanto por Content-Length como leyendo el flujo por fragmentos. Rechaza JSON y UTF-8 inválidos. `parsePushPayload` admite únicamente título, mensaje, alarma booleana y bloque A–D. Recorta los textos, restringe tamaños y caracteres de control y evita que el llamador elija un topic arbitrario: solo `barinas_global` o `block_A/B/C/D`.

`parseTelemetry` acepta los campos de `TelemetryReportRequest`: `sector_id`, `has_power`, `timestamp`, `report_type`, `voltage_reading`, `observation` y `client_device_id`. Exige tipo booleano real, timestamp entero positivo no más de diez minutos en el futuro, voltaje 0–300, observación de hasta 2000 caracteres y origen de hasta 100. Valida que el sector exista antes de insertar `citizen_reports`, completando su nombre desde la base de datos. La agregación corresponde al trigger de base de datos; el router no actualiza sectores con permisos públicos. Después intenta leer las métricas resultantes. Si esa lectura opcional falla, confirma el reporte ya insertado y devuelve métricas null para no provocar un reintento innecesario.

La predicción utiliza exclusivamente `findWindow` del núcleo compartido, calcula la duración real de la ventana y devuelve confianza 0: el código no ha realizado un análisis estadístico que justifique un porcentaje de certeza. Ya no existe el respaldo modular que inventaba un corte para un bloque sin programación.

## Autorización, CORS y FCM

Las lecturas públicas permiten CORS `*` sin credenciales. Reportes y avisos aceptan los orígenes exactos de `PAC_ALLOWED_ORIGINS`; por defecto `https://www.pacbarinas.site` y `https://pacbarinas.site`. No se permiten automáticamente todos los previews de Vercel. Las aplicaciones nativas sin Origin siguen sujetas a las mismas validaciones; ausencia de Origin no concede permiso administrativo.

`verify_jwt=false` es necesario porque las lecturas antiguas no envían una sesión Auth. Por eso `sendPushAlert` realiza su propia cadena obligatoria: Bearer válido → `GET /auth/v1/user` → RPC `is_admin` → validación JSON → RPC `claim_push_alert` → FCM. La cuota es persistente en PostgreSQL, compartida entre instancias. 401 identifica una sesión inválida, 403 falta de permiso/origen, 415 contenido no JSON y 429 cuota agotada con `Retry-After:60`. Errores de autorización o servicio cierran el acceso. Una reserva consumida no se revierte por un fallo posterior de entrega.

`createFirebaseSender` produce `sendFirebase`, llamado únicamente después de esos controles. `readServiceAccount` valida tipo, proyecto, correo y clave PKCS8 de la cuenta de servicio; rechaza un `token_uri` distinto del endpoint fijo de Google. `base64url` y `encodeJson` construyen la codificación del JWT. WebCrypto importa la clave y firma RS256 con scope `https://www.googleapis.com/auth/firebase.messaging`, audiencia `https://oauth2.googleapis.com/token` y duración máxima de una hora. Intercambia la aserción por un token OAuth y lo conserva solo en memoria hasta un minuto antes de expirar. FCM recibe el topic y los datos ya validados. Una respuesta FCM 401 invalida la caché del token, sin reenvío automático que pudiera duplicar un aviso.

`FirebaseUnavailableError` identifica configuración o autorización Google no disponible y produce 503. Un fallo de envío produce 502. Nunca se devuelven claves, tokens ni mensajes de error internos de Google. Si falta `FIREBASE_SERVICE_ACCOUNT`, los avisos fallan de forma explícita con 503; las rutas públicas pueden funcionar.

## Activación actual y posible traslado futuro

La configuración actual conserva `/api/sendPushAlert` como función Node de Vercel y reescribe las demás rutas `/api/:path*` hacia Supabase. Los dos endpoints de cálculo Node se movieron a `server/legacy`, fuera del directorio desplegable. El futuro traslado del envío requiere una decisión posterior y guardar el secreto en Supabase; no copiarlo a clientes ni código.

1. `pac-api` está desplegada y las consultas públicas se comprobaron directamente. La verificación JWT de plataforma está desactivada según `config.toml`; la ruta administrativa implementa su propia autorización.
2. Los dos cálculos históricos están en `server/legacy/`. La excepción `/api/sendPushAlert` precede a la reescritura general `/api/:path*` en Vercel; esa excepción conserva el envío Node y su secreto existente.
3. En el preview protegido se comprobaron consulta mensual 200, archivo compartido del mapa 200 y GET de envío rechazado con 405. El procedimiento de publicación comprueba también las rutas históricas y el POST sin sesión, sin enviar notificaciones reales.
4. Revisar disponibilidad y consumo en ambos proveedores. Para volver al backend previo, restaurar sus archivos y la configuración anterior y desplegar nuevamente; no basta con cambiar un destino si se retiraron sus funciones.

**Solo si se decide trasladar el envío en otra entrega:** configurar `FIREBASE_SERVICE_ACCOUNT` como secreto de Edge Functions mediante un canal privado, comprobar autenticación/rol/cuota y entrega con destinatario de prueba autorizado, y después retirar la excepción Node. Ese secreto no se ha copiado y ese traslado no forma parte de la decisión actual. No anunciar entrega FCM desde Supabase mientras no se haya comprobado.

## Pruebas y límites pendientes

Ejecutar `node --test tests/pac-edge-api.test.cjs` desde `admin-web`. Las pruebas usan solicitudes HTTP completas y respuestas Supabase simuladas; la prueba de firma genera un par RSA efímero y verifica criptográficamente audiencia, scope y firma, con las respuestas Google simuladas. No envía avisos ni escribe datos reales. La igualdad de bytes asegura que la copia del núcleo que se despliega coincide con la del navegador.

Todavía deben controlarse el volumen de reportes anónimos, la idempotencia de reintentos antiguos, el tamaño del catálogo si supera 1000 sectores y las cuotas de Supabase. El gateway no autentica la veracidad física de un reporte. El trigger de agregación, RLS y los controles del cliente siguen siendo piezas distintas que requieren sus propias pruebas.

Referencias oficiales: [cabeceras y verificación de Edge Functions](https://supabase.com/docs/guides/functions/auth-headers), [autorización de funciones](https://supabase.com/docs/guides/functions/auth), [OAuth de cuenta de servicio](https://developers.google.com/identity/protocols/oauth2/service-account), [FCM HTTP v1](https://firebase.google.com/docs/cloud-messaging/send/v1-api).

## Funciones del núcleo de calendario incluido

`installPacScheduleCore` instala la API UMD/CommonJS; `pacScheduleFactory` crea sus cálculos puros. `civilParts` obtiene fecha/hora civil de Caracas y `civilEpoch` la convierte en milisegundos. `normalizeBlock` admite A–D y devuelve guion para desconocido. `weekOffset` selecciona rotación 0–3 y `editorOffset` la calcula para el instante actual. `nextRevision` genera una marca numérica creciente aun con reloj atrasado. `rotateMatrix` copia filas con desplazamiento positivo o inverso. `validateSchedule` rechaza estructura/horas/bloques inválidos antes de calcular. `windows` produce las ventanas reales de ayer a catorce días adelante; conserva la fecha de inicio de turnos nocturnos. `findWindow` elige la primera activa o futura para el bloque, o null. `monthlyWeeks` construye cuatro proyecciones y sus etiquetas. `pacTime` produce franja activa/futura, cuenta regresiva, todayBlocks y activeSlotIsToday; los huecos conservan índice -1 y guion. El [capítulo web](codigo/04-web-react-y-api.md) detalla parámetros y límites de cada función. Las copias de src, public y ambas funciones Edge deben ser idénticas.

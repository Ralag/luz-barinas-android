# Activación y mantenimiento de la seguridad administrativa

Revisión: 16 de octubre de 2026. Esta guía corresponde a los cambios preparados en `supabase_migration_v3_admin_security.sql` y `admin-web/api/sendPushAlert.js`. La existencia de esos archivos no demuestra su aplicación en un servidor. La ejecución y las comprobaciones de producción se registran al final de este documento por el responsable del despliegue.

La cuenta solicitada, `jorluis255@gmail.com`, ya fue creada y confirmada en Supabase Auth durante la preparación. Crear una cuenta no la convierte en administradora: la autorización requiere además su UUID real habilitado en `public.admin_users`. La migración no inventa usuarios ni concede permisos por una dirección de correo.

## 1. Qué cambia

La contraseña incluida en JavaScript y la bandera de `localStorage` dejan de representar autorización. Los paneles usan Supabase Auth y consultan `is_admin()`. La base de datos y el endpoint vuelven a comprobar permisos independientemente de la interfaz.

| Actor | Lectura | Escritura permitida |
|---|---|---|
| Visitante sin sesión (`anon`) | Sectores activos, configuración pública y reportes ciudadanos | INSERT validado de reporte o propuesta comunitaria. No crea/actualiza sectores activos, configuración o permisos. |
| Usuario con sesión sin registro administrativo habilitado | Las mismas lecturas públicas; las solicitudes pendientes no son visibles | Los mismos INSERT ciudadanos validados. Tener cuenta o modificar `user_metadata` no otorga administración. |
| Usuario con sesión y UUID habilitado en `admin_users` | Datos administrativos y públicos | CRUD en sectores/configuración/reportes/propuestas, más emisión de avisos dentro de la cuota. |
| Conexión privilegiada del operador | Acceso de operación del proyecto | Crear/deshabilitar registros administrativos y aplicar migraciones revisadas. Esta capacidad no se entrega al navegador. |

Incluso un administrador de la aplicación no puede leer, insertar, editar o borrar `admin_users` desde el cliente. La tabla no tiene grants de cliente ni políticas de acceso. Los estados de cuota tampoco están expuestos a clientes. `is_admin()` consulta únicamente el UUID del solicitante; no acepta un UUID ajeno como argumento.

La lectura pública conservada es compatible con los clientes actuales. Esto no resuelve por sí solo la privacidad de todas las columnas históricas: reportes libres y notas de `sectors` siguen formando parte de tablas con lectura pública. No almacenar información privada allí; una separación futura requerirá vistas/contratos públicos y adaptación de clientes. Tampoco se ha creado un límite de frecuencia por dispositivo para los INSERT anónimos: la cuota descrita aquí protege la emisión de avisos administrativos.

## 2. Archivos canónicos y copias

| Archivo | Uso |
|---|---|
| [Migración v3](https://github.com/Ralag/luz-barinas-android/blob/7a81b07fd68e1d63b7f7b343ed9babc0ea239aca/supabase_migration_v3_admin_security.sql) | Actualizar una base existente. Es transaccional y puede repetirse; elimina todas las políticas anteriores de las seis tablas controladas y reinstala grants/políticas previstos. |
| [Esquema completo](https://github.com/Ralag/luz-barinas-android/blob/7a81b07fd68e1d63b7f7b343ed9babc0ea239aca/supabase_schema.sql) | Instalación nueva sobre un entorno Supabase con `auth.users`, `anon` y `authenticated`. Incluye tablas y la misma seguridad. No usarlo como sustituto de revisar el estado de una base productiva. |
| [missing_policies.sql](https://github.com/Ralag/luz-barinas-android/blob/7a81b07fd68e1d63b7f7b343ed9babc0ea239aca/missing_policies.sql) | Archivo retirado: únicamente informa qué migración usar; no restaura permisos de escritura anónima. |
| Copias en `admin-web/` | Mismo esquema, migración y archivo retirado para que el repositorio web independiente conserve su despliegue y pruebas. Deben mantenerse idénticos. |
| [Endpoint](https://github.com/Ralag/pac-barinas/blob/7d5b4b57bc36ff9eb62df2ff6fb94945407ce031/api/sendPushAlert.js) | Verificación de usuario/rol/cuota antes de inicializar Firebase o enviar. |
| [Pruebas API](https://github.com/Ralag/pac-barinas/blob/7d5b4b57bc36ff9eb62df2ff6fb94945407ce031/tests/sendPushAlert.test.cjs) | Escenarios sin red ni credenciales reales, con dependencias simuladas. |
| [Fixture SQL](https://github.com/Ralag/luz-barinas-android/blob/7a81b07fd68e1d63b7f7b343ed9babc0ea239aca/tests/sql/admin_security.sql) | Prueba de PostgreSQL en una base vacía desechable; existe copia en `admin-web/tests/sql/` para CI de ese repositorio. Nunca ejecutarla en Supabase de producción. |

Los scripts v2 anteriores no son el punto de entrada de seguridad. En particular, no copiar recetas históricas `FOR ALL TO anon` ni recrear sus políticas después de aplicar v3. Las políticas permisivas se combinan; una regla antigua abierta podría anular la intención de una nueva restricción. Los grants y RLS deben revisarse juntos según la [guía oficial de RLS](https://supabase.com/docs/guides/database/postgres/row-level-security).

## 3. Orden seguro de activación

1. Revisar el diff y ejecutar pruebas en un entorno desechable. Guardar copia de datos y de la configuración de permisos actual antes de la ventana de despliegue. No introducir secretos en Git ni en mensajes de error.
2. Preparar los paneles y API nuevos y sus variables. Durante la transición, una interfaz antigua puede dejar de escribir al cerrarse RLS; esto es un cierre de permisos esperado, no una razón para abrir escritura anónima.
3. Confirmar la cuenta Auth elegida y su correo verificado. En esta preparación ya existe; no enviar otra invitación ni crear otra identidad. Para otro entorno, crear la cuenta desde el panel Auth y dejar que su titular establezca la contraseña por un canal seguro.
4. Aplicar **la migración v3**, con el propietario de la base, al proyecto correcto. En instalación nueva aplicar el esquema completo en vez de recrear manualmente las tablas. El script debe completarse o deshacerse dentro de su transacción; no aplicar solo fragmentos de grants.
5. Localizar el UUID real y habilitarlo mediante la conexión privilegiada indicada abajo. Hasta ese paso, el registro vacío mantiene denegada la administración.
6. Comprobar lectura pública, rechazo de escritura de visitante/usuario no administrador y funcionamiento del administrador mediante sesiones reales. Revisar los asesores de seguridad del proyecto y resolver advertencias relevantes.
7. Publicar/verificar la web y la aplicación Android coordinadas. La app corregida solo envía una propuesta comunitaria con INSERT; los APK antiguos que intentan upsert de propuestas o escritura directa de sectores pueden recibir rechazo. Los reportes ciudadanos válidos y las consultas públicas siguen permitidos.
8. Registrar proyecto, versión desplegada, migración, comprobaciones realizadas y cualquier limitación pendiente. Una prueba de SQL en un motor local no certifica que un APK reciba un push FCM.

La migración añade `isCommunity` cuando falta y deriva su valor inicial de los prefijos comunitarios conocidos. No reescribe ese valor en sucesivas ejecuciones si la columna ya existe. Añade `observation` y las columnas territoriales faltantes, índices de reportes y las cuatro tablas a Realtime si no estaban publicadas; conserva los datos existentes.

## 4. Habilitar y retirar un administrador

Ejecutar la búsqueda siguiente únicamente desde el SQL Editor/conexión privilegiada del operador, nunca con la clave pública del navegador:

```sql
SELECT id, email, email_confirmed_at
FROM auth.users
WHERE lower(email) = lower('jorluis255@gmail.com');
```

Comprobar que devuelve exactamente la identidad prevista y que `email_confirmed_at` no es nulo. Copiar el UUID real de ese resultado. No usar un ID de ejemplo ni un campo enviado por el usuario. El siguiente fragmento contiene un marcador intencionalmente inválido: reemplazarlo solo tras verificar la consulta anterior.

```sql
BEGIN;
INSERT INTO public.admin_users(user_id, enabled)
VALUES ('UUID_REAL_VERIFICADO'::uuid, true)
ON CONFLICT (user_id) DO UPDATE SET enabled = true;
COMMIT;
```

No se guarda una contraseña en esta tabla. `created_at` registra la primera creación del registro. Para retirar acceso, usar la misma identidad verificada:

```sql
UPDATE public.admin_users
SET enabled = false
WHERE user_id = 'UUID_REAL_VERIFICADO'::uuid;
```

La comprobación de `enabled` ocurre en la base en cada operación administrativa; no depende de que el usuario renueve un claim de administrador en su JWT. Revocar sesiones también es apropiado ante una cuenta comprometida. Eliminar una cuenta y esperar que todos sus tokens desaparezcan inmediatamente no es un mecanismo suficiente por sí solo; este diseño consulta el registro vigente.

Un usuario autenticado ordinario no debe recibir permisos para editar `admin_users`, aunque se le permita cambiar su contraseña o perfil de Auth. No usar `user_metadata`, un correo incluido en el body, un parámetro `is_admin` del cliente o la bandera de localStorage para autorizar. La fila administrativa se relaciona con `auth.users(id)` y se elimina en cascada al borrar esa identidad.

## 5. Variables del endpoint

Configurar en el entorno servidor de las funciones, apuntando al mismo proyecto usado por los paneles:

| Variable | Contrato |
|---|---|
| `SUPABASE_URL` | URL HTTPS del proyecto. Como compatibilidad también se acepta `VITE_SUPABASE_URL`. HTTP se admite solo para localhost/127.0.0.1 de desarrollo. |
| `SUPABASE_PUBLISHABLE_KEY` o `SUPABASE_ANON_KEY` | Clave pública/publishable del proyecto para verificar el JWT y consultar RPC como ese usuario. También se admite `VITE_SUPABASE_ANON_KEY`. No hace falta service_role. |
| `FIREBASE_SERVICE_ACCOUNT` | Si la infraestructura usa este método, JSON de credencial disponible solo en servidor. Si falta, Firebase intenta credenciales predeterminadas del entorno; si tampoco existen, se devuelve error genérico de entrega. |

El verificador rechaza una clave `sb_secret_…` o un JWT de API cuyo rol no sea `anon` en esas variables públicas. Las claves de servicio no deben ponerse en variables `VITE_*`, JavaScript, HTML, APK, respuestas HTTP ni tests. La API no reutiliza la sesión de un administrador entre peticiones: crea un cliente sin persistencia y añade el Bearer específico de la petición.

## 6. Contrato HTTP de avisos

`POST /api/sendPushAlert` exige `Content-Type: application/json` y `Authorization: Bearer <access_token>` vigente. El usuario se verifica con `auth.getUser(token)` contra Auth; después se consulta `is_admin()` con el mismo token. No se usa `getSession()` como prueba de identidad de un cliente externo. Esta elección sigue el [contrato oficial de getUser](https://supabase.com/docs/reference/javascript/auth-getuser).

```json
{
  "title": "Actualización del cronograma",
  "message": "Consulta las nuevas franjas en la aplicación.",
  "is_alarm": false
}
```

`title` y `message` deben ser strings no vacíos tras trim, de hasta 120 y 2000 unidades de longitud JavaScript. `is_alarm` es booleano opcional; no se admite el texto `"false"`. La carga de datos FCM serializada no puede superar 3500 bytes UTF-8. Se rechazan caracteres de control inapropiados y campos adicionales. Los datos de query string no sustituyen el JSON.

Omitir `block` conserva el tema global histórico `barinas_global`. Si se utiliza segmentación, `block` solo admite A/B/C/D y se traduce a `block_A`, `block_B`, `block_C` o `block_D`. No se acepta `topic` ni un tema arbitrario. Los paneles nuevos usan explícitamente envío global mientras no se haya comprobado un receptor móvil por bloque. Las alarmas locales por bloque no requieren FCM y son otro recorrido.

La API acepta preflight OPTIONS del mismo origen sin inicializar servicios. Los demás métodos reciben 405. Las respuestas CORS no permiten wildcard ni credenciales de cookies; el Bearer sigue siendo obligatorio para POST incluso desde herramientas que no envían Origin. En producción se exige mismo host, puerto y HTTPS; en localhost se admite el esquema HTTP de desarrollo. CORS no sustituye la autorización.

| Estado | Significado |
|---|---|
| 200 | Firebase aceptó el mensaje y devolvió messageId. No garantiza entrega o lectura en cada dispositivo. |
| 204 | Preflight permitido; no se envió mensaje. |
| 400 / 415 | Campos inválidos o contenido distinto de JSON. No se consume cuota ni se inicializa Firebase. |
| 401 | Bearer ausente, inválido o usuario no verificado por Auth. |
| 403 | Origen no permitido o identidad sin administración habilitada. |
| 405 | Método diferente de POST/OPTIONS. |
| 429 | Cuota persistente denegada; `Retry-After: 60`. |
| 503 | Configuración/verificación de autorización o cuota no disponible. Se cierra el acceso en vez de permitir un envío provisional. |
| 502 | Error de inicialización/entrega Firebase tras haber autorizado y consumido cuota. No se exponen detalles de credenciales. |

## 7. Todas las funciones nuevas del endpoint

Fuente: [sendPushAlert.js](https://github.com/Ralag/pac-barinas/blob/7d5b4b57bc36ff9eb62df2ff6fb94945407ce031/api/sendPushAlert.js). Los límites y decisiones de seguridad deben probarse al cambiar cualquiera de ellas.

| Función | Entradas, retorno y pasos |
|---|---|
| `getFirebaseMessaging(env)` | Recibe entorno; retorna instancia Messaging. Importa Firebase Admin de forma diferida, inicializa solo si no hay app y obtiene Messaging. Solo se llama después de usuario, rol y cuota aprobados. Sus excepciones se traducen a 502 por el handler. |
| `readSupabaseConfig(env)` | Retorna `{url,key}` o `null`. Lee variables compatibles, valida protocolo/localidad y rechaza claves secretas/service_role utilizadas como públicas. No imprime valores. `null` lleva a 503 sin llamar a Firebase. |
| `hasSameOrigin(req)` | Retorna booleano. Sin Origin permite seguir con verificación Bearer; con Origin exige formato/origen exacto y host/puerto/esquema permitido. No concede permisos administrativos. |
| `parsePayload(body)` | Retorna `{data,block,topic}` o `null`. Acepta objeto JSON o string JSON acotado, rechaza arrays/campos extra/tipos/límites inválidos, serializa booleano para datos FCM y calcula tema desde allowlist. No realiza red ni aplica cuota. |
| `createHandler(dependencies = {})` | Retorna función asíncrona `sendPushAlert`. Permite inyectar `createClientImpl`, `getMessagingImpl`, `env` y `logger` en tests; producción usa SDK y entorno reales. Inyectar dependencias evita tocar servidores para probar rechazos. |
| `sendPushAlert(req,res)` | Handler asíncrono devuelto por la factoría. Orden: cabeceras/método/origen → Bearer/configuración → getUser → is_admin → JSON validado → claim_push_alert → Firebase. Devuelve la respuesta HTTP; ninguna rama de rechazo llega a Firebase. Logs usan códigos genéricos, sin token ni error de credencial. |

## 8. Contratos SQL

| Objeto | Comportamiento y autorización |
|---|---|
| `admin_users(user_id,enabled,created_at)` | Tabla interna de asignación. RLS habilitado, sin grants/políticas cliente. Solo el operador privilegiado la gestiona. `enabled` comienza false. |
| `is_admin()` → boolean | SQL STABLE SECURITY DEFINER, propietario postgres y search_path vacío; referencias calificadas. Busca `auth.uid()` habilitado y retorna únicamente ese estado. Se revoca EXECUTE de PUBLIC/anon y se concede a authenticated. No permite comprobar/elevar un UUID arbitrario. |
| `push_alert_limits(scope,last_claim_at)` | Tabla interna sin grants/políticas cliente; contiene instante de último claim global y por UUID. No es una cola de envío ni historial de entrega. |
| `claim_push_alert(p_block text default null)` → boolean | SECURITY DEFINER con search_path vacío y EXECUTE solo authenticated. Vuelve a comprobar `is_admin`, valida bloque y toma advisory lock transaccional común. Exige al menos 30 segundos entre avisos de todo el sistema y 60 por administrador, actualiza ambas marcas atómicamente y retorna true; retorna false si no pasó el intervalo. Rol denegado produce SQLSTATE42501 y bloque inválido22023. |
| Políticas `*_admin` | Solo authenticated y predicado is_admin en USING/WITH CHECK. Protegen lecturas/escrituras administrativas aunque se salte la interfaz. |
| `citizen_reports_submit` | Solo INSERT validado para anon/authenticated: identidad y nombre no vacíos/acotados, tipo permitido, voltaje0–300 o null, observación/origen acotados y timestamp positivo no superior a diez minutos en el futuro. |
| `community_locations_submit` | Solo propuesta INSERT: nombre/territorio/circuito acotados, bloque A–D con o sin prefijo Bloque, estado NORMAL y métricas iniciales acotadas, coordenadas/notas válidas y timestamp limitado. No permite actualizar/reemplazar propuesta de otra persona. |

Estas son comprobaciones RLS de entrada ciudadana; no se presentan como CHECK universales para todos los cambios administrativos ni como verificación física de una medición. No se añadió una FK de sector a reportes que pudiera invalidar registros históricos durante la activación. Los operadores deben revisar calidad territorial y agregados aparte.

La cuota persiste entre procesos/instancias del endpoint; no hay un contador local que pueda reiniciarse con cada arranque. Un claim se consume aunque el proveedor falle después, para que un fallo no se convierta en una vía de abuso. Una llamada directa a esa RPC por un administrador también consume cuota, pero no envía un push por sí sola. No borrar manualmente el estado para eludir límites durante operación ordinaria.

Las funciones privilegiadas tienen una razón limitada: consultar el registro cerrado y actualizar el estado de cuota cerrado. Su EXECUTE y search_path se fijan expresamente; no deben usarse como solución genérica a errores de permisos. Véanse [funciones de base de datos](https://supabase.com/docs/guides/database/functions) y [CREATE FUNCTION / SECURITY DEFINER](https://www.postgresql.org/docs/current/sql-createfunction.html).

## 9. Verificar permisos después de activar

Revisión de catálogo, con conexión privilegiada:

```sql
SELECT tablename, policyname, roles, cmd, qual, with_check
FROM pg_policies
WHERE schemaname='public'
  AND tablename IN ('sectors','app_config','citizen_reports','community_locations','admin_users','push_alert_limits')
ORDER BY tablename, policyname;

SELECT grantee, table_name, privilege_type
FROM information_schema.role_table_grants
WHERE table_schema='public'
  AND grantee IN ('anon','authenticated','PUBLIC')
  AND table_name IN ('sectors','app_config','citizen_reports','community_locations','admin_users','push_alert_limits')
ORDER BY table_name, grantee, privilege_type;

SELECT p.proname, p.prosecdef, p.provolatile, p.proconfig, p.proacl
FROM pg_proc p JOIN pg_namespace n ON n.oid=p.pronamespace
WHERE n.nspname='public' AND p.proname IN ('is_admin','claim_push_alert');
```

Además del catálogo, probar con clientes separados: visitante, usuario no habilitado y administrador. El primero lee datos públicos e inserta propuesta/reporte válidos, pero no escribe sectores/configuración ni lee solicitudes. El segundo obtiene false en is_admin y no obtiene permisos por tener cuenta. El tercero realiza una edición reversible y verifica el resultado persistido. Ninguno lee o modifica la tabla de administradores desde el navegador.

Para comprobar cuota sin enviar FCM puede llamarse `claim_push_alert` desde una sesión administrativa real: primera llamada true y la inmediata false. Consumirá el intervalo de prueba. En cambio, no usar `testGlobalPushAlert` como una simple prueba de conectividad: sí solicita un envío real. Un resultado positivo de registro de publicación Realtime tampoco certifica entrega de eventos WebSocket; esa prueba requiere la suscripción de un cliente.

## 10. Pruebas locales y CI

API, desde el repositorio web:

```powershell
node --test tests/sendPushAlert.test.cjs
```

`fixture(overrides)` prepara petición/respuesta y dobles de Supabase/Firebase; `noDelivery(f)` verifica que ninguna inicialización o envío del proveedor haya ocurrido. Se prueban métodos, origen, token ausente/expirado, usuario sin rol, rol de tipo incorrecto, errores de Auth/RPC, payload/query string, cuota, revocación entre comprobaciones, temas permitidos, éxito administrativo y ausencia de detalles secretos. Las 50 pruebas del endpoint pasaron en esta revisión. Ninguna prueba llama a Auth, Firebase o producción.

SQL, desde la raíz de cualquiera de los dos repositorios, con `psql` conectado como postgres a **una base vacía desechable**:

```powershell
psql -X -v ON_ERROR_STOP=1 -f tests/sql/admin_security.sql
```

La fixture crea roles y un esquema Auth mínimo, implementa auth.uid desde una variable de sesión de prueba y aplica el esquema. Añade políticas heredadas deliberadamente abiertas y aplica dos veces v3 para comprobar eliminación/idempotencia. `test_support.assert_true(ok,label)` falla ante condición no verdadera; `test_support.expect_error(sql,estado)` ejecuta como rol actual y exige SQLSTATE esperado. No son funciones del producto ni se instalan en producción.

Casos incluidos: lectura pública, INSERT ciudadano válido/invalidado, rechazo de CRUD administrativo anónimo, rechazo de usuario autenticado ordinario/deshabilitado/sin UUID, metadata de usuario que no concede rol, CRUD del administrador, prohibición de autoasignarse permisos, cuota global/por usuario y revocación. Los cambios de los escenarios se revierten por transacción.

La revisión local ejecutó la fixture satisfactoriamente en PostgreSQL18.6 compilado a WebAssembly mediante PGlite0.5.10, instalado temporalmente fuera de los repositorios; se ejecutaron 298 sentencias respetando la separación/autocommit de psql. También se probaron columnas ausentes durante upgrade, clasificación inicial comunitaria y conservación de una clasificación manual al repetir la migración. No equivale a probar Supabase Auth HTTP ni contención entre varias conexiones. CI debe repetir el mismo archivo en PostgreSQL16 con psql; los bloqueos/catálogos reales y la autenticación remota se comprueban además en el entorno objetivo. La instalación local no añadió PGlite a las dependencias del producto.

## 11. Recuperación y rollback seguro

Si falla una sentencia de migración dentro de la transacción, revisar el error y corregir hacia delante; no continuar pegando grants aislados. Si ya se aplicó la seguridad y falla el panel, conservar RLS y grants mínimos, deshabilitar temporalmente la emisión o dejar la administración sin acceso mientras se corrige UI/configuración. Las consultas públicas siguen funcionando por sus permisos explícitos.

Para una cuenta comprometida, deshabilitar su registro verificado y revocar sesiones. Para detener todos los avisos, retirar temporalmente disponibilidad del endpoint o su configuración, en vez de permitir un fallback sin autorización/cuota. Si la función de cuota no existe o falla, el comportamiento previsto es 503.

No restaurar políticas `USING(true)` de escritura, `FOR ALL TO anon`, TRUNCATE público ni una contraseña cliente como rollback. No borrar admin_users, cuotas, columnas o reportes para “arreglar” un login. Si una versión anterior de UI requiere permisos inseguros, mantener la versión nueva/cerrada hasta disponer de una corrección compatible. Una nueva migración revisada es preferible a reabrir la vulnerabilidad.

## 12. Registro de ejecución del entorno

El 16 de octubre de 2026 a las12:57UTC se aplicó **20261016125703 / admin_security_v3** al proyecto `ikttyjojubtredehtneb`, PostgreSQL17.6. Se habilitó la identidad Auth confirmada de `jorluis255@gmail.com`. Las cuatro tablas operativas están publicadas en Realtime.

Las comprobaciones remotas permitieron lectura pública e INSERT ciudadano válido, rechazaron cambios anónimos y autoasignación, y permitieron UPDATE administrativo reversible. La reserva de cuota admitió la primera y rechazó la inmediata. Todas las escrituras de prueba se revirtieron: permanecen119sectores,5configuraciones,2643reportes y19propuestas. No se enviaron notificaciones. Los roles se ejercitaron desde SQL privilegiado con claims controlados; falta comprobar el login desde un navegador con la contraseña privada del titular. El [registro de cambios](CAMBIOS_SEGURIDAD_Y_ALERTAS.md) recoge asesores, pruebas y límites de despliegue web/Android. No restaurar permisos públicos para acceder con una interfaz antigua.

Los PR1 de web y Android se integraron en sus respectivas ramas main con autorización expresa. Vercel desplegó la web nueva y se verificó el panel público en https://www.pacbarinas.site/admin. El endpoint respondió405 a GET,401 a POST sin sesión o con token inválido y204 al preflight del mismo origen, sin entregas FCM. Esta comprobación de producción complementa las pruebas SQL; el titular debe usar su contraseña privada para el login interactivo. No se publicó una actualización Android.

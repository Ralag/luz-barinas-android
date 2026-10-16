# 06. Base de datos, configuración, compilación y mantenimiento

Este capítulo explica los archivos que preparan el sistema y los scripts de mantenimiento presentes en el workspace. Leer un script no implica que se deba ejecutarlo: varios escriben datos remotos o reemplazan código antiguo. No se ejecutaron durante esta documentación.

## 1. Dos repositorios y una carpeta de trabajo

| Repositorio | Carpeta | Remoto configurado al revisar |
|---|---|---|
| Android | `E:\LUZ BARINAS` | `https://github.com/Ralag/luz-barinas-android.git` |
| Web | `E:\LUZ BARINAS\admin-web` | `https://github.com/Ralag/pac-barinas.git` |

Las referencias anteriores a `luz-barinas-admin-web` están desactualizadas respecto del remoto local. Un commit Android no incluye automáticamente cambios web: `.gitignore` raíz excluye `admin-web/`. Los commits de referencia al iniciar esta actualización documental son `c890853` y `19a76ce`; el contenido no versionado que estaba presente también fue leído cuando corresponde.

La documentación se edita en el repositorio Android y se mantiene como copia en el web. El [documento del sistema](../../DOCUMENTACION_SISTEMA.md) y el [manual de código](../../DOCUMENTACION_CODIGO.md) son las entradas. Las rutas `app/...` pertenecen al Android; las que comienzan `admin-web/...` se interpretan respecto de esta carpeta conjunta.

## 2. Gradle y construcción Android

| Archivo/bloque | Qué configura y cómo influye |
|---|---|
| `settings.gradle.kts` | Nombre Luz Barinas, módulo único `:app`, repositorios Google/Maven Central/Plugin Portal y resolución de toolchains Foojay. No incluye el proyecto React como módulo Gradle. |
| `build.gradle.kts` raíz | Declara alias de plugins con apply false; permite que el módulo app los aplique. No contiene pantallas ni reglas PAC. |
| `gradle/libs.versions.toml` | Catálogo central de versiones y coordenadas. Los alias `libs.*` se generan a partir de este archivo. Cambiar un comentario de documentación no cambia una dependencia. |
| `gradle/wrapper/gradle-wrapper.properties` | Descarga/selecciona Gradle9.3.1, directorios bajo GRADLE_USER_HOME y timeout de red. El wrapper debe poder escribir caché y encontrar la distribución. |
| `gradlew`, `gradlew.bat`, wrapper JAR | Arrancadores para Unix/Windows. Se documenta su función, no se reimplementa código de terceros. |
| `gradle.properties` | Memoria JVM2560m, cachés, configuration-cache, máximo2 workers, compilación Kotlin in-process y ajustes Android. `googleServices.missing.passthrough` no instala por sí mismo Firebase Messaging. |
| `local.properties` | Configuración local de la máquina, normalmente ubicación SDK. Está ignorado y no debe tratarse como archivo transportable del proyecto. |
| `app/build.gradle.kts` · plugins | Android application, Compose, KSP, Roborazzi, secrets y serialization. KSP genera implementación de Room y adaptadores Moshi. |
| `android` | namespace `com.example`; applicationId `com.ralag.pacbarinas`; minSdk24, compile/target36; versionName1.3.1 y versionCode7. Namespace organiza clases/R; applicationId identifica la aplicación instalada. |
| `compileOptions` | Compatibilidad Java11 del código. No especifica por sí sola la versión de JVM necesaria para ejecutar todas las herramientas actuales. |
| `buildFeatures` | Activa Compose y generación de BuildConfig. La app consulta DEBUG, APPLICATION_ID y VERSION_* generados. |
| `signingConfigs` | Release toma STORE_PASSWORD y KEY_PASSWORD exclusivamente del entorno. KEYSTORE_PATH y KEY_ALIAS seleccionan archivo/alias. Debug utiliza su firma estándar. |
| `buildTypes.release` | Minificación R8, reducción de recursos, reglas ProGuard y firma. La ejecución debug no prueba que R8 conserve todas las clases necesarias. |
| `testOptions` | Incluye recursos Android y valores por defecto para tests, ajusta java.library.path. Permite Robolectric/Roborazzi; no demuestra que las pruebas se hayan ejecutado. |
| `androidComponents.onVariants` | Recorre salidas y renombra APK como `PAC-BARINAS-v<version>-<variant>.apk`, usando una clase interna de AGP. |
| `secrets` | Lee `.env` con `.env.example` como respaldo. Debe revisarse además el uso real de cada constante: el manifest placeholder de AdMob está asignado explícitamente en el build. |
| `dependencies` | Declara bibliotecas para UI, ciclo de vida, Room, WorkManager, Glance, anuncios, Supabase, Ktor, Retrofit, Moshi y pruebas. No incluye Firebase Messaging en la versión examinada. |
| `tasks.withType<KotlinCompile>` | Añade el argumento de destino predeterminado de anotaciones param-property. Es configuración del compilador, no una tarea de negocio. |

Versiones relevantes declaradas: AGP9.1.1, Kotlin2.2.10, KSP2.3.5, Compose BOM2024.09.00, Room2.7.0, WorkManager2.10.0, Supabase3.0.2, Ktor3.0.0, Retrofit2.12.0, Moshi1.15.2, OkHttp4.10.0, Glance1.1.1 y Ads23.6.0. El catálogo distingue versiones de librerías de la versión de producto.

### ProGuard/R8

`app/proguard-rules.pro` conserva información de línea, anotaciones y adaptadores Moshi, firmas/métodos Retrofit, entidades Room, widgets Glance y clases públicas de anuncios. `dontwarn` suprime ciertos avisos, pero no crea una dependencia ni implementa una función ausente. La regla comentada como Firebase Cleanup no acredita que FCM esté integrado.

## 3. Variables y secretos sin duplicar credenciales

| Configuración | Dónde se lee | Qué significa |
|---|---|---|
| URL/clave pública Supabase | Kotlin y cliente web/API según archivo | Seleccionan proyecto y rol público. Su publicación es compatible con un cliente público; las restricciones deben estar en permisos/políticas. |
| `FIREBASE_SERVICE_ACCOUNT` | Función `sendPushAlert` | JSON de credencial de servidor para Firebase Admin. Debe residir en el entorno del servidor, no en archivos del cliente. |
| Variables de firma | Gradle | Seleccionan archivo/alias/contraseñas de firma; necesarias para continuidad de actualizaciones. |
| IDs AdMob | Build, `.env.example` y componentes de anuncios | Identifican aplicación/unidades publicitarias; no son contraseñas de administrador. La configuración actual debe leerse completa. |
| `.env.example` | Plugin secrets como defaults | Contiene mezcla de IDs de producción/test aunque su comentario general diga prueba. No asumir que copiarlo convierte todo a entorno de desarrollo. |
| Credenciales del panel | Supabase Auth | Correo/contraseña personales; getUser verifica identidad e is_admin consulta el registro privado. Sin contraseña embebida. |
| SUPABASE_URL y SUPABASE_PUBLISHABLE_KEY o SUPABASE_ANON_KEY | sendPushAlert servidor | Requeridas; admite alias VITE_ de URL/anon. Usar clave pública, nunca service_role/sb_secret. Sin configuración devuelve503. |

No se copian valores de cuenta de servicio, contraseñas de firma ni de panel a este manual. El archivo keystore local está ignorado; su presencia local no demuestra exposición en Git.

## 4. Esquema remoto Supabase: campos y diferencias

supabase_schema.sql prepara una instalación vacía y supabase_migration_v3_admin_security.sql actualiza las tablas existentes sin borrar datos. La inspección remota previa confirmó 119 sectores, 5 configuraciones, 2643 reportes y 19 propuestas con escritura pública. La [guía de activación](../ACTIVACION_SEGURIDAD.md) explica Auth/roles y el [registro de cambios](../CAMBIOS_SEGURIDAD_Y_ALERTAS.md) identifica el estado remoto. No repetir el esquema completo sobre producción.

### `sectors`

| Campo | Tipo/uso |
|---|---|
| `id` | TEXT clave primaria. Identidad del sector, conservada por reportes y preferencias. |
| `name` | TEXT obligatorio. Rótulo humano; no debería sustituir identidad. |
| `circuitCode` | TEXT. Circuito/alimentador descriptivo. En SQL las mayúsculas requieren comillas. |
| `status` | TEXT con NORMAL inicial. La aplicación reconoce NORMAL, SCHEDULED_OUTAGE e IRREGULAR_OUTAGE. El SQL no limita esos valores con CHECK. |
| `voltage` | FLOAT8 predeterminado118. Es un valor informado/asignado, no prueba de sensor. |
| `confirmedReportsCount` | INT inicial0. Contador almacenado; no hay trigger de agregación definido en estos SQL. |
| `withoutPowerPercentage` | INT inicial0. No tiene CHECK0–100 en los scripts. |
| `rotationBlock` | TEXT. Código de bloque; Android normaliza A–D y Bloque X. |
| `isCommunity` | BOOLEAN NOT NULL DEFAULT false, añadido en v3; completa DTO y clasificación comunitaria. |
| `estado`, `municipio`, `parroquia`, `sector`, `barrio` | Datos territoriales; defaults y nulabilidad difieren entre esquema inicial y migración. |
| `lastUpdatedMillis` | BIGINT, epoch milisegundos. Default SQL con now; los clientes pueden enviarlo. |
| `notes`, `lat`, `lon` | Añadidos por migración v2. Notas y coordenadas simples; no se almacenan polígonos como los de Room. |

`isCommunity` se envía desde el panel, pero **no se crea** en el esquema ni la migración v2 revisados. El fallback del panel omite ese y otros campos, por lo que un éxito puede ser parcial.

### `app_config`

Dos columnas: `config_key TEXT PRIMARY KEY` y `config_value JSONB NOT NULL`. Cada fila guarda un documento de configuración. Un upsert que reemplaza `config_value` no garantiza conservar propiedades internas omitidas.

| Clave | Contenido y consumidores |
|---|---|
| `pac_schedule` | version/updatedAt, slots, matrixRows[].cells o matrix heredada, sectorsA-D. La leen app/panel/telemetría/API. El panel React actual omite sectorsA-D al guardar. |
| `broadcast_notice` | title/message/level/timestamp/active. Banner Android y panel. No equivale a envío FCM. |
| `donations_config` | paypal/binance/pmBank/pmPhone/pmId. Métodos de colaboración representados en app/panel. |
| `donations_url` | Formato heredado `{url}` con soporte parcial/residual; no confundir con configuración completa. |
| `barinas_geography` | Catálogo territorial, municipios, cantidades y updatedAt escrito por seed web. |
| `version` | versionCode/versionName/releaseNotes/apkDownloadUrl/isMandatory leídos por sync Android. El botón actual de actualización abre Play. |

Los timestamps tienen inconsistencia de formato: el clásico escribe milisegundos y React escribe updatedAt ISO además de version numérica. Android intenta leer updatedAt como long y usa version como respaldo. Si se unifica el contrato, todos los productores/consumidores deben cambiar conjuntamente.

### `citizen_reports`

`id UUID` se genera por defecto en servidor. `sectorId`, `sectorName`, `hasPower` y `reportType` son obligatorios. `voltage`, `observation` y `deviceOrigin` agregan contexto. `timestamp BIGINT` indica el instante enviado/por defecto. No existen `reportedAtMillis`, `voltageObserved` ni `created_at` en esta definición. Tampoco hay FK sectorId, restricción única de evento ciudadano ni procedimiento remoto de deduplicación.

La falta de ID compartido entre Room y remoto permite duplicación. El cliente actual fija timestamp al enviar, por lo que pierde el instante offline. Estos son comportamientos pendientes, no diseño recomendado.

### `community_locations`

`id/name` identifican propuesta; `estado/municipio/parroquia/sector/barrio` describen lugar; `block/circuitCode` son obligatorios; `rotationBlock` puede duplicar el bloque; `status/voltage/confirmedReportsCount/withoutPowerPercentage` inicializan datos; `submittedAt BIGINT` da fecha de propuesta. Migración añade notes/lat/lon. No hay estados pending/approved/rejected ni revisor/fecha de aprobación en el esquema actual. La eliminación de la propuesta representa su salida de bandeja.

## 5. Políticas, Realtime y migraciones

| Bloque SQL | Efecto real |
|---|---|
| ENABLE ROW LEVEL SECURITY | Activa evaluación de políticas por tabla. No significa que sus reglas sean restrictivas. |
| Grants y policies v3 | Revoca ALL de PUBLIC/anon/authenticated, elimina políticas heredadas y concede solo operaciones necesarias. Lectura pública de sectores/config/reportes; propuestas privadas; escrituras administrativas sujetas a is_admin. |
| Inserciones ciudadanas | citizen_reports y community_locations aceptan INSERT público con validación de texto, bloque, voltaje, fecha y coordenadas. No concede UPDATE/DELETE/TRUNCATE anónimos. |
| Publicación `supabase_realtime` | Incluye sectors, app_config, citizen_reports y community_locations sin duplicar miembros existentes. Las políticas siguen controlando visibilidad. |
| ALTER finales del esquema | Repite ADD COLUMN IF NOT EXISTS para algunas columnas y observation. Evita error por columna existente; no corrige todos sus tipos/defaults/restricciones previos. |
| Migración v2 | Añade territorio/notas/coordenadas, rellena algunos nulos y envía NOTIFY a PostgREST. No instala un motor predictivo ni agregación. |
| WHERE del saneamiento | Considera algunos IS NULL, no todos los vacíos/notes nulas prometidos. Una fila excluida no se beneficia del COALESCE del SET. |
| NOTIFY reload schema/config | Solicita refrescar caché/config del servicio. No crea columnas ni concede permisos. |
| SELECT final | Mensaje de finalización del script; no es prueba de todos los flujos clientes. |
| `missing_policies.sql` | Script retirado: informa qué migración v3 utilizar y no modifica permisos ni restaura escrituras anónimas. |
| `copyMigrationSql` | Muestra instrucciones para usar el archivo v3 revisado; ya no genera FOR ALL TO anon ni SQL que abra la base. |

Las funciones SQL v3 is_admin() y claim_push_alert(p_block) se explican abajo. No hay triggers de agregación automática ni estrategia completa de backups/rollback del negocio. No restaurar políticas públicas para resolver fallos de login: comprobar Auth, registro administrador y entorno servidor. El fixture de seguridad se ejecuta solo en una base vacía aislada.

### Funciones y tablas privadas v3

`admin_users(user_id, enabled, created_at)` referencia auth.users y solo se administra desde una conexión privilegiada. No permite al navegador consultar ni modificar su registro. `is_admin()` es SQL STABLE SECURITY DEFINER, con search_path vacío y referencias calificadas: compara auth.uid() con un registro habilitado. Devuelve boolean; no recibe correo, UUID ni rol del cliente. Solo authenticated tiene EXECUTE. Las policies usan `(SELECT public.is_admin())` para evitar evaluaciones innecesarias por fila. Una cuenta Auth válida sin registro habilitado sigue sin ser administradora.

`push_alert_limits(scope,last_claim_at)` guarda el último intento autorizado global y por usuario. `claim_push_alert(p_block DEFAULT NULL)` verifica primero is_admin, rechaza bloques ajenos a A–D y toma un advisory lock transaccional global. Usa clock_timestamp después del bloqueo, devuelve false si hubo una reserva global en30s o del usuario en60s, y en otro caso actualiza ambas filas con INSERT ON CONFLICT y devuelve true. La reserva también se consume cuando Firebase falla; los clientes no pueden reiniciarla. El bloque valida audiencia, pero no abre una cuota independiente por bloque. Las funciones pertenecen a postgres, revocan EXECUTE de PUBLIC/anon y nunca confían en user_metadata.

La migración es transaccional, añade columnas ausentes, marca como comunitarios IDs con prefijos reconocidos y crea índices de reportes por timestamp y sector/timestamp. Es repetible; no crea cuentas ni asigna el primer administrador. Para eso se usa la guía de activación con una cuenta Auth verificada. El cliente no puede aprobarse por correo.

### Pruebas SQL y automatización

`tests/sql/admin_security.sql` crea roles/esquema Auth mínimos en PostgreSQL vacío; instala el esquema, simula políticas antiguas y ejecuta v3 dos veces. Sus aserciones comprueban privilegios reales al cambiar anon/authenticated y claims JWT: lectura pública, inserción válida/inválida, denegación de cambios a no administradores/deshabilitados, CRUD de administrador, registro privado y cuota/revocación. Los auxiliares test_support ejecutan operaciones esperadas y abortan ante un resultado distinto. No envía alertas ni usa cuentas reales. Ejecutar desde la raíz de cualquiera de los dos repositorios con `psql -X -v ON_ERROR_STOP=1 -f tests/sql/admin_security.sql` exclusivamente en una base de pruebas vacía.

`.github/workflows/verify-web.yml` ejecuta npm ci/test/build con Node22 y otro job con PostgreSQL16 para ese fixture. `.github/workflows/verify-android.yml` prepara Java21 y SDK36 y ejecuta testDebugUnitTest/lintDebug; conserva reportes. Las ramas main y codex/** y las solicitudes de cambios activan los checks. No despliegan APK ni incluyen contraseñas de firma.

## 6. Persistencia local y compatibilidad

Room v3 contiene `sectors`, `pending_reports` y `outage_records`; sus campos/mapeos/consultas están en capítulo02. `MIGRATION_2_3` agrega isCommunity local y observation a pendientes. Esto no crea isCommunity remoto. Se registra solo esa migración y hay fallback destructivo: una ruta ausente puede borrar tablas. `exportSchema=false` impide conservar automáticamente el historial JSON de esquemas para pruebas de migración.

SharedPreferences guarda PAC/configuración y Room sectores/reportes. Scheduler, receiver y worker cargan el PAC persistido antes de calcular alarmas, incluso en un proceso nuevo. El resolver consulta Room para reparar el bloque guardado y separar Casa/Trabajo. Editar una capa no garantiza sincronizar la otra.

Las reglas backup/extraction son plantillas con secciones comentadas y la aplicación permite backup. No hay una selección granular completa configurada. `file_paths.xml` conserva rutas para FileProvider aunque el flujo actual de actualización abre Play; su presencia no demuestra instalación de APK activa.

## 7. Scripts Python: cada función

### `seed_supabase.py`

Es una utilidad local de carga remota basada en urllib, no un servicio que arranca con la app. Contiene catálogos, franjas, matriz y propuestas embebidos. El `.gitignore` raíz ignora `*.py`, por lo que estos archivos presentes pueden no venir en otro clon.

| Función | Contrato y pasos |
|---|---|
| `supabase_upsert(table,records)` · 16 | Recibe nombre de tabla y lista de diccionarios. Elige conflicto id o config_key, construye POST JSON con Prefer merge-duplicates y headers de cliente. Devuelve true solo para HTTP200/201; ante error imprime detalle y devuelve false. Escribe remotamente y no ofrece transacción entre llamadas. |
| `main()` · 162 | Obtiene timestamp, publica PAC y aviso, construye sectores con IDs por nombre, publica propuestas y las añade además como sectores activos. Introduce estados/contadores prefijados y puede reemplazar datos existentes. El guard `if __name__ == '__main__'` la ejecuta al lanzar el archivo; importar no llama main, aunque inicializa constantes. |

El texto final de éxito no demuestra que cada envío haya tenido éxito: algunos resultados se imprimen o ignoran y el proceso no revierte los previos. No usarlo para validar una consulta ni como migración incremental.

### `seed_firestore.py`

Utilidad heredada para otra base de datos. El Android actual de datos utiliza Supabase; este script no sincroniza automáticamente ambos sistemas.

| Función | Contrato y pasos |
|---|---|
| `firestore_patch(collection_path,doc_id,fields)` · 21 | PATCH a documento Firestore con campos ya convertidos. Devuelve JSON recibido o None con log. Puede reemplazar datos remotos. |
| `firestore_create(collection_path,doc_id,fields)` · 33 | POST de documento con ID. Si recibe409 delega en patch; otros errores producen None. La semántica de crear o reemplazar depende de esa respuesta. |
| `to_firestore_value(v)` · 50 | Convierte recursivamente string/bool/int/float/list/dict al formato tipado Firestore. Bool se comprueba antes de int para no tratarlo como número. Tipos restantes se convierten a string, no a un null tipado. |
| `to_firestore_fields(d)` · 66 | Transforma cada valor del diccionario con la función anterior y conserva nombres. Devuelve mapa fields sin red. |
| `main()` · 236 | Escribe PAC, aviso, sectores, propuestas y reportes de ejemplo con fechas recientes escalonadas. Usa IDs y datos prefijados. Hay pausa entre ciertos envíos; no es una transacción y no alimenta Supabase actual. |

### Otros Python locales

- `update_script.py`: ejecuta lógica de nivel superior que lee `PacScheduleView.kt`, busca dos marcadores textuales y sustituye un bloque extenso por una vista con AnimatedContent. Si no encuentra marcadores imprime fallo; si encuentra escribe el archivo. No tiene funciones Python de negocio ni es una migración segura/repetible del proyecto.
- `test.py`: contiene texto con sintaxis ajena a Python (`println` y escapes). No es la suite de pruebas del sistema.
- `admin-web/update_index.py`: reemplaza referencias CDN Firebase antiguas por Supabase en index.html, con ruta absoluta local. Hoy index.html pertenece al portal React; no debe ejecutarse para reconstruir el panel.
- `admin-web/update_text.py`: sustituciones textuales globales de Firebase/Firestore en index.html, también con ruta local. Cambiar palabras no migra un backend.

## 8. Scripts JavaScript de reemplazo

Estos archivos no se importan en el producto. Al ejecutarse leen un archivo, aplican reemplazos literales y lo reescriben. Su ruta relativa presupone el directorio de trabajo adecuado; algunos apuntan a archivos que ya no existen. No detectan de forma general si el código resultante sigue siendo válido.

| Archivo | Transformación que intenta |
|---|---|
| `patch_appupdater.js` | Mover un Toast de error del instalador antiguo al hilo principal. El instalador referido ya no es el flujo actual. |
| `patch_appupdater2.js` | Cambiar descarga pública por directorio externo privado de la app en un instalador antiguo. |
| `patch_cloudsync.js` | Insertar flow de donations_url y rama de configuración por coincidencias literales. Repetirlo puede duplicar contenido. |
| `patch_cloudsync_fix.js` | Intentar esa inserción solo si no encuentra `val donationUrlFlow`; depende de una firma textual exacta. |
| `patch_icons.js` | Reemplazar una referencia Settings por otra expresión de icono. |
| `patch_icons2.js` | Agregar import Settings junto a Close. |
| `patch_icons_final.js` | Cambiar de nuevo la expresión Settings a filled.Settings. |
| `patch_icons_finally.js` | Revertir esa expresión a Icons.Default.Settings. Los nombres final/finally no certifican cuál es correcto hoy. |
| `patch_imports.js` | Insertar imports de alarma/cerrar y reemplazar una referencia de icono. |
| `patch_settings_dialog.js` | Añadir parámetro y fila de donación a `SystemSettingsDialog.kt`, archivo que no figura en el código actual. Puede fallar al leerlo. |
| `patch_vm.js` | Insertar campo donationUrl y un colector en una forma antigua del ViewModel, con nombres que pueden haber cambiado. |
| `patch_vm2.js` | Añadir campo donationUrl por reemplazo simple; puede duplicarlo si ya existe. |
| `admin-web/patch_donations.js` | Insertar HTML y funciones antiguas saveDonationUrl/loadDonationUrl en index/app.js. El formato histórico es donations_url, distinto de donations_config actual. No reproduce el sitio React. |

Las funciones escritas dentro de strings de esos parches son **texto que se pretende inyectar**, no funciones ejecutadas por el propio script en ese momento. Por eso el inventario separa código real y reemplazos históricos.

## 9. Archivos auxiliares y de distribución

| Archivo/grupo | Papel |
|---|---|
| `metadata.json` | Describe el proyecto para herramientas externas. La mención a una capacidad Gemini no demuestra una integración implementada: no se encontró un servicio Gemini en el flujo actual. |
| `.gitignore` | Evita versionar IDE/build/APK/env/keystore/scripts Python y el repo web anidado. Ignorar un archivo no elimina una versión ya comprometida del historial; aquí no se auditó todo ese historial. |
| `.zcodeignore` | Configuración de exclusiones para otra herramienta; no cambia compilación ni permisos del backend. |
| `DESIGN.md`, `MERCURY.md` | Referencias visuales externas presentes, no especificación funcional ni garantía de componentes implementados. |
| APK raíz y `build/`, `.gradle/`, `.kotlin/`, `.idea/` | Artefactos, cachés y configuración local. No son fuente canónica del comportamiento ni deben documentarse como APIs propias. |
| `admin-web/dist/` | Salida de Vite y prerender, se regenera. No editar como sustituto de fuente. |
| `public/robots.txt` | Instrucciones para rastreadores y referencia a sitemap; no controla acceso. |
| `ads.txt`, `app-ads.txt` | Declaraciones de publicidad para verificadores. No implementan cobros ni notificaciones. |
| Iconos/imágenes/fuentes | Recursos referenciados por páginas/Android. Sustituir respetando nombre/tamaño y revisar representación. |
| Leaflet JS/CSS y extensión calor | Dependencias locales del mapa Android; el manual explica su uso y puente, no cada función de su implementación de terceros. |

## 10. Construir, comprobar y publicar sin confundir etapas

### Android

Abrir la raíz en Android Studio con SDK36 y JDK compatible. Debug usa la firma estándar y no requiere credenciales de publicación; release necesita variables externas. Rotar las contraseñas retiradas del código sin perder la continuidad de firma.

Pruebas locales que un mantenedor puede lanzar, cuando su entorno esté preparado:

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:lintDebug
```

Los intentos locales tuvieron problemas de caché y dependencias offline. La [verificación GitHub de 7a81b07](https://github.com/Ralag/luz-barinas-android/actions/runs/38904594403) sí completó testDebugUnitTest y lintDebug:16 tests aprobados, ningún fallo/error/omitido y76 advertencias lint sin errores. Verificar SDK/JVM/caché antes de atribuir fallos de arranque al producto. No se generó ni publicó release.

Para distribución, el procedimiento habitual del proyecto es Android Studio → Generate Signed Bundle/APK, con credenciales gestionadas fuera de los documentos. Incrementar versionCode y versionName cuando se publique un binario nuevo. El actualizador compara GitHub pero abre Google Play: publicar solo en GitHub puede anunciar una versión aún no disponible en el destino. Hay que definir y comprobar un canal coherente antes de emitir una actualización obligatoria.

### Web

Desde admin-web, npm ci instala el lockfile, npm test ejecuta pruebas de seguridad sin red, npm run dev inicia Vite, npm run build genera dist/blog y npm run preview sirve la salida. Vite no implementa las Vercel Functions de api/. El registro distingue pruebas locales y checks de GitHub.

Vercel utiliza npm run build, publica dist, aplica rewrites y headers del archivo vercel.json. Firebase Hosting conserva una configuración heredada public='.'; no es equivalente ni se verificó un despliegue allí. El comportamiento de `/admin` debe probarse tanto navegando como recargando.

### Comprobación de documentación

```powershell
node docs/codigo/herramientas/inventario.mjs
node docs/codigo/herramientas/verificar-documentacion.mjs
```

El primer script genera el índice/JSON de funciones y examina código propio; el segundo comprueba capítulos, nombres cubiertos, enlaces locales y sincronización de copias. Ninguno envía datos al backend. Requieren Node y las dependencias web locales para el parser. Su detalle de implementación figura en el siguiente apartado.

## 11. Herramientas de esta documentación

`inventario.mjs` importa `parseAst` del Rolldown instalado. `walk(dir,accept)` recorre archivos locales; `relative(p)` normaliza rutas; `chapter(file)` asigna cada fuente a un capítulo; `lineAt(source,offset)` calcula línea; `add(...)` registra un símbolo sin duplicar misma identidad; `parseJavaScript(file,source,offset)` analiza sintaxis y su función interna `visit` recorre el árbol para encontrar declaraciones, arrows asignadas, métodos/callbacks nombrados. Kotlin/Python se reconocen mediante patrones de declaración; no hay inferencia completa de tipos. También analiza scripts inline propios del mapa/HTML. Escribe JSON y Markdown y retorna error si un archivo JS no se pudo analizar.

El inventario guarda también SHA-256 y cantidad de líneas de cada fuente. `verificar-documentacion.mjs` usa su propio `walk(dir)` para listar documentos; `syncCopies()` copia archivos documentales a web si se pasa `--sincronizar`; `headingIds(content)` obtiene anclas de encabezados Markdown ignorando bloques de código y distingue títulos repetidos. El código de nivel superior lee inventario, detecta fuentes cuyo hash cambió, busca nombres en las explicaciones antes de índices finales, verifica enlaces y números de línea, detecta patrones de credenciales, compara copias byte a byte y emite un informe JSON. Si encuentra problemas finaliza con código1. Copiar no elimina fuentes ni cambia operaciones del producto. El flag de sincronización está pensado para la disposición conjunta de repositorios de este workspace.

El inventario consulta git ls-files en ambos repositorios para marcar localOnly en las utilidades presentes fuera de Git. El verificador tolera su ausencia en un clon y sigue verificando sus hashes si existen. Ambos normalizan CRLF a LF antes de SHA-256 para que checkout en Windows/Linux no produzca falsos cambios; el JSON declara hashNormalization=LF. Incluye admin-security.js y tests CJS. La sincronización copia también guía, registro y cuatro fuentes SQL; el informe comprueba su igualdad. Los enlaces versionados apuntan a commits concretos de GitHub.

La comprobación de cobertura es estructural: que un nombre aparezca en el capítulo no prueba que su explicación sea correcta. Las explicaciones se redactaron leyendo código y se revisan con esa limitación. Al agregar archivos, revisar también que estén incluidos en el alcance del inventario.

## 12. Procedimiento de diagnóstico

| Síntoma | Qué comprobar primero |
|---|---|
| No compila Android | Wrapper/JVM/SDK, resolución de dependencias, firma, error exacto y variante. |
| No llegan reportes | Room pendiente, conectividad, worker, respuesta de INSERT, fecha/ID y RLS. |
| Cambió pantalla pero no servidor | Acción solo de memoria, error devuelto no comprobado o stub que devuelve éxito. |
| Horario erróneo | Perfil, normalización Bloque, versión PAC, carga persistente, día de inicio y zona horaria. |
| Sector reaparece | DELETE real, permisos, listas PAC y ocultación local de IDs. |
| Feed vacío | Nombre real de tabla/columnas, `{error}`, publicación Realtime y límite de consulta. |
| Falta campo geográfico | Migración real, columna ausente, fallback de guardado parcial y DTO. |
| Diferencia al recargar web | Rewrite de Vercel y panel servido antes de montar React. |
| Métrica extraña | Seed, valor sustituto, doble incremento y edad/tamaño de la muestra. |

Registrar el dato observado y el esperado antes de cambiarlo. La recuperación de un fallo de lectura debe conservar la última información válida; sembrar o sobrescribir datos para que desaparezca el error puede destruir la evidencia y el estado correcto.

# PAC Barinas: documentación del sistema

**Versión documental: 21 de octubre de 2026.** Esta guía describe el código de ambos repositorios con seguridad y alertas por bloque, mapa común, publicidad opcional de doce horas, pantallas compactas y sincronización PAC corregida. La [guía release 1.4.0](docs/RELEASE_1.4.0.md) añade frecuencia publicitaria, diagnóstico de apertura y comprobación de APK/AAB. Publicar código no acredita su distribución en Google Play.

El código prepara Android **1.4.0, versionCode 8**; todavía no se ha publicado el binario. Las instalaciones anteriores conservan su mapa y anuncios hasta actualizar. Los commits c890853 en Android y 19a76ce en web corresponden al análisis inicial. Los defectos corregidos se identifican expresamente; el [registro actual](docs/CAMBIOS_MAPA_ANUNCIOS_Y_SINCRONIZACION.md) explica límites y validación, y la [guía del gateway](docs/OPERACION_GATEWAY_SUPABASE.md) detalla rutas compatibles y cuotas.

## Índice

1. [Propósito y alcance](#1-propósito-y-alcance)
2. [Repositorios e identidad técnica](#2-repositorios-e-identidad-técnica)
3. [Arquitectura y responsabilidades](#3-arquitectura-y-responsabilidades)
4. [Aplicación Android](#4-aplicación-android)
5. [Portal web, blog, TV y administración](#5-portal-web-blog-tv-y-administración)
6. [Datos y contratos compartidos](#6-datos-y-contratos-compartidos)
7. [Reglas PAC y estimación ciudadana](#7-reglas-pac-y-estimación-ciudadana)
8. [Flujos de sincronización y propuestas](#8-flujos-de-sincronización-y-propuestas)
9. [Notificaciones y actualización](#9-notificaciones-y-actualización)
10. [Configuración, construcción y publicación](#10-configuración-construcción-y-publicación)
11. [Pruebas y estado de mantenimiento](#11-pruebas-y-estado-de-mantenimiento)
12. [Manual del código](#12-manual-del-código)

## 1. Propósito y alcance

PAC Barinas es un proyecto comunitario independiente para consultar horarios referenciales del Plan de Administración de Cargas, registrar reportes voluntarios de suministro y representar información eléctrica por sectores y circuitos del estado Barinas. Combina una aplicación Android, un portal público y herramientas administrativas.

Su utilidad consiste en reunir programación, observaciones ciudadanas y una estimación del próximo corte. Esas fuentes tienen significados diferentes: una matriz indica un turno esperado; un reporte describe lo que alguien informó; una predicción calcula una ventana probable mediante reglas. El sistema no controla subestaciones, no mide voltaje mediante sensores propios y no representa a CORPOELEC ni a una entidad gubernamental.

No se exige crear una cuenta para consultar horarios. Las preferencias Casa/Trabajo pertenecen al dispositivo y no son identidades remotas. El formulario web sí permite entregar voluntariamente nombre y correo. Los datos geográficos pueden identificar un sector propuesto; los modelos admiten coordenadas aunque la aplicación no declare permisos de GPS. Los textos de privacidad deben reflejar estas diferencias.

La operación sin conexión se apoya en datos locales y cronogramas persistidos. Recibir novedades, enviar reportes y obtener publicaciones recientes requiere conectividad. Una animación de “en vivo” o un número de respaldo no acredita que el servidor esté disponible ni que una muestra sea reciente.

## 2. Repositorios e identidad técnica

La carpeta conjunta contiene dos repositorios Git independientes:

| Componente | Carpeta | Remoto configurado |
|---|---|---|
| Android y documentación canónica | E:\LUZ BARINAS | [Ralag/luz-barinas-android](https://github.com/Ralag/luz-barinas-android) |
| Portal y funciones web | E:\LUZ BARINAS\admin-web | [Ralag/pac-barinas](https://github.com/Ralag/pac-barinas) |

El repositorio raíz excluye la subcarpeta web de su seguimiento. Cada uno requiere revisar y versionar sus propios cambios. El nombre de paquete npm “luz-barinas-admin-web” y referencias históricas a ese nombre no cambian el remoto web verificado.

Android utiliza namespace **com.example** y applicationId **com.ralag.pacbarinas**. El primero organiza clases y recursos; el segundo identifica la aplicación instalada. La configuración examinada declara minSdk **24**, compileSdk **36** y targetSdk **36**. La numeración comercial del README histórico no sustituye versionName/versionCode del módulo Android.

Este documento es la fuente canónica de descripción del sistema. La copia del repositorio web debe sincronizarse con ella. Las contraseñas, claves privadas, cuentas de servicio y valores de firma se administran fuera de la documentación; no se reproducen aquí.

## 3. Arquitectura y responsabilidades

~~~mermaid
flowchart LR
    UI[Android: Compose y ViewModel] --> R[EnergyRepository]
    R --> L[Room y preferencias]
    R --> C[CloudSyncRepository]
    C <--> S[Supabase: PostgREST y Realtime]
    W[Portal React y panel clásico] <--> S
    W --> A[Funciones serverless]
    A --> S
    A --> F[Firebase Admin: envío FCM]
    B[Vite y generador de blog] --> H[HTML y recursos estáticos]
~~~

Android sigue un patrón MVVM: los composables representan el estado, LuzBarinasViewModel coordina acciones y los repositorios separan almacenamiento y red. Room conserva sectores, historial y pendientes; SharedPreferences conserva ubicación, perfiles, temas, alertas y configuración PAC. StateFlow lleva cambios a la interfaz.

EnergyRepository combina datos locales y operaciones de negocio. CloudSyncRepository usa Supabase PostgREST para consultas/escrituras y Realtime para suscripciones. El código Retrofit y sus DTO permanecen en el proyecto, pero no se encontraron llamadas activas a ApiClient.api: no debe describirse como un respaldo de envío actualmente operativo.

La web pública usa React y consulta Supabase directamente. La telemetría se refresca por polling; el panel clásico también abre canales Realtime. Las funciones serverless proyectan matrices, calculan una predicción alternativa y solicitan envíos FCM. No forman un backend único por el que pase toda lectura o escritura.

Vite compila páginas y recursos. Un plugin propio genera HTML estático del blog para buscadores y lectores sin JavaScript. Vercel tiene configuración de rutas y publicación; Firebase Hosting conserva una configuración alternativa histórica. Su existencia local no demuestra que ambos estén desplegados.

## 4. Aplicación Android

### Navegación que realmente se monta

MainActivity crea canales, inicia publicidad, agenda trabajo periódico y monta MainAppScreen. La navegación usa estado y selección de pestaña, sin NavHost:

| Destino | Contenido y acciones |
|---|---|
| **Hoy** | DashboardScreen: sector seleccionado, estado informado, estimación, pendientes, aviso comunitario, búsqueda y botones de reporte. Permite cambiar perfil Casa/Trabajo cuando corresponde. |
| **Horarios** | PacScheduleView: vistas semanal, mensual y circuitos; selección de día/mes y consulta de asignaciones A–D. Pulsar un nombre intenta seleccionar ese sector. |
| **Mapa** | MapScreen con InteractiveBarinasMap: WebView y HTML/JavaScript local, marcadores o representación térmica, filtros y tarjeta de selección con reporte rápido. |
| **Configuración** | Pantalla superpuesta con dirección, bienvenida, tema, escala, alertas, aportes, actualización y páginas legales. |

En pantallas superiores a 600 dp se usa NavigationRail; en las demás hay navegación inferior. Los diálogos incluyen bienvenida, dirección y registro comunitario, personalización, ajustes PAC, donaciones y actualización. Los temas permiten modo claro/oscuro/sistema, paleta y escala visual; esta última afecta densidad y texto.

AdminScreen y TelemetryScreen existen y tienen callbacks asociados en el ViewModel, pero no están montadas en la navegación principal actual. Cambiar isAdminOpen no crea por sí mismo una pantalla administrativa accesible. La existencia de un archivo o acción tampoco demuestra un recorrido terminado para el usuario.

El mapa Android combina un recurso HTML con un puente JavaScript hacia Kotlin. Las coordenadas o polígonos pueden ser aproximados y los valores visibles incluyen respaldos. El mapa no es una conexión directa a equipamiento eléctrico. Deben conservarse límites de navegación, escape de textos y permisos del puente al mantenerlo.

El widget Glance permite ver estado y reportar desde la pantalla de inicio. Actualmente consulta un identificador de sector fijo y no sigue automáticamente el perfil Casa/Trabajo seleccionado. Hay publicidad de banner y apertura; el intersticial se precarga, pero no se encontró una llamada de producción que lo muestre. Los nombres “adaptable” o “offline” de componentes no sustituyen comprobar su comportamiento concreto.

## 5. Portal web, blog, TV y administración

### Sitio público React

index.html contiene el contenedor y metadatos iniciales; src/main.jsx monta App. El enrutador manual lee pathname, cambia historial con pushState y escucha popstate. Carga varias páginas con lazy/Suspense y oculta barra/pie al entrar en administración o TV.

La portada reúne Hero, cronograma y buscador, contador de bloques, reportes, estabilidad por circuito, características, adelanto del blog, donaciones y contacto. Algunas descargas apuntan a un APK concreto en GitHub, por lo que no siguen automáticamente la última versión.

useTelemetry conserva una caché de sesión y consulta reportes, sectores del municipio Barinas, configuración PAC y conteo. Refresca aproximadamente cada 60 segundos mientras la pestaña está visible y actualiza el reloj local cada segundo. No consulta red cada segundo ni usa Realtime para ese feed público. Los circuitos se agrupan por código y su “estabilidad” deriva de porcentajes almacenados, con voltajes y conteos de respaldo cuando faltan muestras.

Las rutas públicas incluyen privacidad, términos, transparencia, créditos/desarrollador y reembolsos. Son contenido estático: describen condiciones, pero no aplican permisos, retención o devoluciones. ContactSection intenta guardar mensajes en citizen_feedback; esa tabla no aparece en el esquema SQL entregado y el formulario no comprueba todos los errores devueltos.

### Blog educativo y SEO

/blog muestra artículos y filtros por etiquetas; /blog/:slug muestra contenido, índice de h2, autor, fecha, tiempo estimado y relacionados. Los cinco temas editoriales son historia del sistema eléctrico/Guri, protección de electrodomésticos, anatomía del PAC, telemetría ciudadana y conceptos de electricidad doméstica.

Los textos viven en src/data/articles y se registran en articles.js. No hay CMS remoto. El HTML del artículo se considera contenido local confiable. Un plugin de compilación genera el índice y cada artículo con texto, metas, canónicos, JSON-LD y sitemap. React reemplaza esa presentación al montarse; un hook mantiene metadatos durante navegación interna. Añadir o cambiar artículos requiere recompilar.

### Modo TV

/tv y el alias inicial /kiosk montan una pantalla completa con reloj de Caracas, selección de bloque, preferencias locales, controles y seis temas: **Radar, Claro, Rendimiento, Web, App y Mercury**. Las vistas muestran combinaciones de estado programado, cuenta regresiva, franjas, circuitos y banda de reportes.

El contenedor solicita pantalla completa y Wake Lock cuando el navegador lo permite. Admite teclas 1–6, T/F2 para tema, B para bloque, F para pantalla completa y Escape para cerrar el selector. La conservación de pantalla depende del navegador; no constituye un servicio nativo permanente.

Los temas comparten datos. El tema App imita Material 3, pero sus textos de batería, 5G y Room son decorativos. La etiqueta de 60 FPS del tema Rendimiento no es una medición. La selección de bloque personaliza la vista; no modifica asignaciones remotas.

### Dos paneles administrativos

La navegación interna a /admin puede montar **AdminPortal React**; una solicitud directa o recarga de /admin se reescribe a **admin.html**, el panel clásico. El comportamiento cambia según cómo se llega a la misma dirección.

React ofrece matriz, sectores, reportes, donaciones, push y diagnóstico. Guardar la matriz conserva el documento PAC cargado y sus asignaciones sectorsA–D; las altas/bajas del gestor de sectores solo modifican memoria. Persisten contratos de datos pendientes de corregir. Ambos formularios push envían audiencia global; el endpoint admite block opcional, que las interfaces actuales no exponen. Android todavía no recibe esos temas FCM.

El clásico incorpora franjas, proyección mensual, telemetría, bloques, directorio/mapa, propuestas, duplicados, Nominatim, avisos, consola y donaciones. public/app.js y app.js raíz están sincronizados. Ambos paneles comparten admin-security.js: Supabase Auth, getUser() y comprobación is_admin(). Un marcador local o una contraseña embebida ya no concede acceso. El clásico escapa textos remotos y usa acciones delegadas para las propuestas.

## 6. Datos y contratos compartidos

### Persistencia local

Room abre luz_barinas.db, versión 3:

| Tabla local | Responsabilidad |
|---|---|
| sectors | Sectores, estado, bloque, geografía y representación local. |
| pending_reports | Eventos por enviar, observación y estado/reintentos de sincronización. |
| outage_records | Intervalos históricos usados por consultas y predicción. |

La migración 2→3 añade isCommunity local y observation en pendientes. Hay fallback destructivo para rutas no cubiertas y exportSchema está desactivado. Una migración remota no actualiza Room automáticamente.

Las preferencias se separan entre luz_barinas_user_prefs —dirección, selección, perfiles y tema—, pac_schedule_prefs —matriz, franjas, listas y versión—, pac_alert_settings —opciones de alertas—, pac_alert_deliveries —deduplicación— y cloud_sync_prefs —marcas remotas—. El navegador utiliza almacenamiento local para caché, tema y sesión administrada por Supabase; un valor local no concede el rol administrador.

### Esquema remoto versionado

| Tabla | Campos y función principal |
|---|---|
| sectors | id, name, circuitCode, status, voltage, confirmedReportsCount, withoutPowerPercentage, rotationBlock, territorio, lastUpdatedMillis y ampliaciones notes/lat/lon. |
| citizen_reports | UUID remoto, sectorId, sectorName, hasPower, reportType, voltage, observation, deviceOrigin y timestamp en milisegundos. |
| community_locations | Propuestas con identidad, nombre, territorio, block/circuitCode, datos de estado, submittedAt y ampliaciones geográficas. |
| app_config | config_key y config_value JSONB; configuración común por documento. |

Las mayúsculas son parte del contrato. created_at, reportedAtMillis, voltageObserved y community_sectors no pertenecen a ese contrato. La migración v3 añade sectors.isCommunity, citizen_reports.observation, el registro privado admin_users y el control privado push_alert_limits. La migración v4 agrega eventos recientes al sector mediante un trigger privado, con IDs exactos, fecha original y prioridad de ediciones administrativas más recientes.

app_config contiene pac_schedule, broadcast_notice, donations_config, barinas_geography y version; donations_url es un formato heredado. pac_schedule incluye versión, marca temporal, slots y matrixRows con cells, más listas sectorsA–D cuando el productor las conserva. Los timestamps difieren entre productores: milisegundos en clásico y updatedAt ISO en React con version numérica.

La migración v3 elimina políticas heredadas y revoca permisos amplios antes de conceder los mínimos necesarios. El público conserva lectura de sectores/configuración/reportes e inserción validada de reportes/propuestas. Solo los administradores habilitados modifican sectores/configuración y moderan. Las propuestas no se leen anónimamente. admin_users y push_alert_limits permanecen privados. Realtime contempla las cuatro tablas operativas. La inspección remota previa confirmó escrituras públicas y permisos incluso de TRUNCATE; el registro de cambios indica la activación y verificación posterior.

### APIs web

| Ruta | Entrada | Salida y regla |
|---|---|---|
| /api/getMonthlyWeeks | monthName, totalDays | Cuatro grupos de semanas con matrices rotadas. |
| /api/getOutagePrediction | sectorId | Ventana estimada, duración, confianza, explicación y horas restantes. |
| /api/sendPushAlert | Bearer JWT, title, message, is_alarm, block opcional | POST administrativo; verifica identidad/rol/contenido/cuota antes de solicitar FCM al tema elegido. |

Los nombres reales de predicción son nextEstimatedStartMillis y nextEstimatedEndMillis. Las APIs de lectura conservan caché compartida. sendPushAlert restringe método y origen, verifica sesión y rol, valida campos/tamaños y aplica una espera global de 30 segundos y por administrador de 60 segundos mediante bloqueo transaccional PostgreSQL. Firebase se inicializa después de las comprobaciones.

## 7. Reglas PAC y estimación ciudadana

La matriz usa filas de franjas y columnas lunes–domingo. Las letras A/B/C/D representan bloques; el guion indica ausencia de corte asignado. Los valores iniciales contienen seis franjas de cuatro horas, incluida 23:00–03:00, y las listas territoriales son datos editables o catálogos de respaldo.

El desplazamiento mensual es 0 para días 1–7, 1 para 8–14, 2 para 15–21 y 3 para 22–fin. Se aplica al índice de fila mediante módulo y reinicia al comenzar el mes. No es una rotación continua independiente del calendario ni una división por semanas ISO.

En Android, OutagePredictionEngine combina una ventana teórica con duración media del historial del sector, admitida entre dos y ocho horas, o cuatro horas de respaldo. Agrupa reportes sin luz por hora de Caracas; con al menos tres en la hora dominante puede ajustar el inicio cuando difiere de la teoría. Usa cifras heurísticas 88/94 para teoría, 78 para respaldo modular y una fórmula ciudadana limitada a 99.

No hay entrenamiento ni calibración estadística de esas cifras. Repeticiones antiguas y concentración de reportes pueden alterar la estimación. Los nuevos envíos preservan identidad y fecha original. El motor Android conserva heurísticas; la API compatible nueva utiliza ventanas reales del calendario compartido, duración publicada y confianza cero cuando no hay una probabilidad calibrada.

Las alarmas Android usan findScheduledWindowForBlock: busca desde ayer, calcula la rotación con la fecha de inicio en Caracas, excluye el instante final y devuelve null sin programación. PacBlock acepta A–D y «Bloque X», sin extraer la B de BLOQUE. El contrato predictivo heredado findNextWindowForBlock aún ofrece una estimación de respaldo; las alarmas no lo utilizan. Queda unificar las variantes web/API y el motor ciudadano.

## 8. Flujos de sincronización y propuestas

Al iniciar, el ViewModel carga preferencias/PAC, prepara repositorios y observa Room y flujos remotos. CloudSync lee configuración y sectores y mantiene suscripciones mientras está activo. Aplica campos presentes, guarda PAC local y comunica cambios al estado. Los catálogos precargados permiten empezar sin nube, pero no garantizan actualidad.

Un reporte desde Dashboard pasa por ViewModel y EnergyRepository.submitReport: se registra en Room, modifica estado local de forma optimista, encola ReportPowerWorker e intenta un envío inmediato. NotificationActionReceiver y la acción del widget escriben Room y encolan trabajo directamente, sin pasar por ese ViewModel/repositorio ni iniciar ese envío inmediato. El worker, con requisito de conectividad, recorre pendientes e intenta insertarlos mediante CloudSync; también existe una ruta manual de sincronización.

Las rutas conservan el ID concreto que devuelve Room y la fecha original del evento. El envío deriva un UUID estable del identificador de instalación, ID local y fecha; un reintento usa `ON CONFLICT DO NOTHING`. La confirmación marca ese pendiente preciso y el worker no vuelve a incrementar contadores. El trigger privado agrega únicamente las inserciones nuevas. Esta identidad evita duplicar el mismo evento pendiente, sin detectar reportes distintos que una persona envíe varias veces.

Una propuesta se envía únicamente a community_locations; Android dejó de publicar sectores directamente. El administrador revisa y publica. La eliminación de la propuesta representa su salida de bandeja, sin historial formal de decisiones ni transacción única de aprobación. La publicación PAC clásica propaga errores y una lectura fallida conserva el horario anterior, sin guardar respaldos. La función Android de publicar PAC todavía devuelve éxito sin escritura real.

## 9. Notificaciones y actualización

Las alertas operativas Android son **locales**. PacAlarmScheduler utiliza AlarmManager para anticipación, comprobación de inicio y retorno previsto; PacAlarmReceiver muestra notificaciones y puede activar PacAlarmPlayer. NotificationActionReceiver convierte respuestas rápidas en pendientes y trabajo de sincronización. PredictiveNotificationWorker se agenda cada 30 minutos con flexibilidad de 15 y consulta horarios; no ejecuta el motor ciudadano completo.

El permiso de notificaciones se solicita donde corresponde. Scheduler, receiver y worker cargan el PAC persistido; el resolver obtiene de Room el bloque del sector del perfil activo y repara preferencias antiguas. Cambiar perfil, ajustes u horario remoto cancela y recalcula alarmas; el receiver descarta intents de otra selección o ventana. AlarmManager y worker comparten deduplicación persistente. Sin permiso de alarmas exactas se agenda una aproximación sujeta a Android. El retorno señala el final previsto, no una medición eléctrica.

broadcast_notice llega por sincronización y puede producir un banner/notificación local. Esto es distinto de FCM. Aunque sendPushAlert usa Firebase Admin en servidor, el Android revisado no incluye dependencia Firebase Messaging, servicio receptor propio ni suscripción al tema global. No está acreditado el recorrido de push a app cerrada descrito históricamente.

AppUpdater consulta la última release de GitHub y compara nombres de versión, pero el botón actual abre **Google Play**, con alternativa HTTPS de la ficha. No descarga ni instala un APK. La configuración remota version todavía admite metadatos y URL heredada; las notas GitHub pueden marcar obligatoriedad. Publicar solo en GitHub podría anunciar algo aún no disponible en Play, por lo que ambos canales deben coordinarse. Los enlaces web a APK siguen siendo otro canal de distribución.

## 10. Configuración, construcción y publicación

Gradle usa el módulo :app y wrapper 9.3.1. Release toma contraseñas exclusivamente de STORE_PASSWORD y KEY_PASSWORD; KEYSTORE_PATH y KEY_ALIAS seleccionan archivo y alias. Debug usa su firma estándar y no exige el keystore de publicación. Las contraseñas retiradas del código deben rotarse según el procedimiento de firma.

Para preparar Android se necesita SDK 36, JVM compatible, dependencias y directorios de caché escribibles. Compilar debug no valida minificación/firma de release. La guía release registra generación y comprobación de los artefactos firmados; publicar en Google Play sigue siendo un paso separado y requiere un código de versión disponible en ese canal.

En admin-web, npm ci instala el lockfile, npm run dev inicia Vite, npm run build genera dist/blog/sitemap y npm run preview sirve la salida. Vite local no ejecuta automáticamente las funciones Vercel. vercel.json configura build, publicación de dist, recursos con hash, rutas del blog y rewrite administrativo.

firebase.json aún publica desde la carpeta fuente con public="."; no equivale al procedimiento Vercel y requiere revisión antes de usarse. FIREBASE_SERVICE_ACCOUNT debe residir en el entorno servidor. Seeds y scripts de reemplazo antiguos escriben datos o código: no son pasos de lectura ni reparaciones genéricas. La operación detallada está en el capítulo 06.

## 11. Pruebas y estado de mantenimiento

Existen pruebas Android para motor, Room, recursos, captura Robolectric/Roborazzi y contexto instrumentado. Esta corrección fija el reloj del test predictivo y añade regresiones para bloques, madrugada, mes, zona horaria, preferencias, perfiles, cancelación y deduplicación. El registro de cambios detalla qué se ejecutó.

La entrega anterior aprobó 75 pruebas web y 16 Android en GitHub. La entrega actual aprobó 132 pruebas web y compilación; la agregación pasó 168 sentencias PostgreSQL18.6/PGlite y el fixture PostgreSQL16 de GitHub, incluida concurrencia de dos conexiones reales. Los resultados definitivos Android y CI se mantienen en el [registro actual](docs/CAMBIOS_MAPA_ANUNCIOS_Y_SINCRONIZACION.md). Ambos repositorios incorporan GitHub Actions y conservan reportes.

| Área pendiente | Estado actual y trabajo necesario |
|---|---|
| Identidad y permisos | Auth y RLS v3 implementados; mantener registro de administradores y revisar nuevas tablas/policies. |
| Emisión de alertas | Endpoint protegido y audiencia por bloque corregida; falta recepción FCM Android y suscripción a temas. |
| Textos remotos | Panel clásico escapado y puente Android con origen/navegación restringidos; mantener estas garantías al añadir HTML y enlaces. |
| Integridad de reportes | ID estable, fecha original y confirmación precisa implementados; quedan IDs históricos sin correspondencia y controles adicionales de abuso. |
| Horarios y alarmas | PAC coherente en Android y núcleo compartido web/API; queda calibrar y unificar las heurísticas predictivas. |
| Doble administración | Unificar rutas/contratos, persistir sectores React y controlar concurrencia al guardar JSON PAC. |
| Estado de carga | Lectura/publicación PAC clásica corregida; revisar formularios y otras operaciones con éxito aparente. |
| Esquema y evolución | Resolver campos/tablas divergentes, migraciones, restricciones y respaldo/recuperación. |
| Estimaciones y presentación | Evaluar confianza, diferenciar datos reales de respaldos y actualizar textos legales/promocionales. |
| Distribución y calidad | Coordinar GitHub/Play, revisar Firebase heredado y ejecutar pruebas reproducibles. |

Esta tabla distingue las correcciones actuales de las limitaciones restantes. Consultar el registro de cambios y la guía de activación para conocer la revisión y permisos efectivos.

## 12. Manual del código

La explicación función por función se encuentra en [DOCUMENTACION_CODIGO.md](DOCUMENTACION_CODIGO.md). Su recorrido está dividido en:

1. [Guía de lectura y flujos](docs/codigo/01-guia-de-lectura-y-flujos.md).
2. [Android: datos, almacenamiento y motor](docs/codigo/02-android-datos-y-motor.md).
3. [Android: interfaz, estado y segundo plano](docs/codigo/03-android-interfaz-y-segundo-plano.md).
4. [Web React, TV, blog y APIs](docs/codigo/04-web-react-y-api.md).
5. [Panel administrativo clásico](docs/codigo/05-panel-clasico.md).
6. [Base de datos, configuración y operación](docs/codigo/06-base-de-datos-configuracion-y-operacion.md).
7. [Índice de funciones con archivo y línea](docs/codigo/07-indice-funciones.md).

Los capítulos describen parámetros, retornos, llamadores, persistencia, red y límites. Al cambiar un contrato, actualizar tanto su ficha como los consumidores y esta visión general. Regenerar el inventario comprueba cobertura estructural; no reemplaza leer ni verificar el significado del código.

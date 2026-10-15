# PAC Barinas: documentación del sistema

**Versión documental: 15 de octubre de 2026.** Esta guía describe el código y la configuración presentes en la carpeta de trabajo. Distingue capacidades implementadas, componentes disponibles sin conexión a la navegación y trabajo pendiente. No certifica qué revisión está publicada, permisos efectivos de servidores, disponibilidad de Google Play ni funcionamiento de servicios en producción.

La referencia Android es **1.3.1, versionCode 7**. Los commits de referencia al comenzar la actualización documental fueron **c890853** en Android y **19a76ce** en web; también se examinó el contenido local disponible. Esta actualización reemplaza la descripción histórica y no implica que los problemas identificados hayan sido corregidos.

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

React ofrece matriz, lista de sectores, reportes, donaciones, push y diagnóstico. Sus altas/bajas de sectores solo cambian memoria; consulta nombres de tabla/campos incompatibles con el esquema entregado. Guardar la matriz reemplaza config_value y omite sectorsA–D. Su selector de audiencia push tampoco limita el envío: la API usa el tema global.

El clásico incorpora edición de franjas, proyección mensual, telemetría, bloques, directorio/mapa, propuestas, agrupación de duplicados, validación Nominatim, avisos, consola y donaciones. La fuente distribuida es public/app.js; la copia app.js raíz presenta diferencias. Ambos paneles usan acceso visual en el navegador, sin identidad administrativa comprobada por el servidor.

## 6. Datos y contratos compartidos

### Persistencia local

Room abre luz_barinas.db, versión 3:

| Tabla local | Responsabilidad |
|---|---|
| sectors | Sectores, estado, bloque, geografía y representación local. |
| pending_reports | Eventos por enviar, observación y estado/reintentos de sincronización. |
| outage_records | Intervalos históricos usados por consultas y predicción. |

La migración 2→3 añade isCommunity local y observation en pendientes. Hay fallback destructivo para rutas no cubiertas y exportSchema está desactivado. Una migración remota no actualiza Room automáticamente.

Las preferencias se separan entre luz_barinas_user_prefs —dirección, selección, perfiles y tema—, pac_schedule_prefs —matriz, franjas, listas y versión—, pac_alert_settings —anticipación, notificación, sirena, retorno y vibración— y cloud_sync_prefs —marcas de configuración/avisos recibidos—. El navegador usa sessionStorage para caché y localStorage para tema/bloque TV y el marcador visual del panel.

### Esquema remoto versionado

| Tabla | Campos y función principal |
|---|---|
| sectors | id, name, circuitCode, status, voltage, confirmedReportsCount, withoutPowerPercentage, rotationBlock, territorio, lastUpdatedMillis y ampliaciones notes/lat/lon. |
| citizen_reports | UUID remoto, sectorId, sectorName, hasPower, reportType, voltage, observation, deviceOrigin y timestamp en milisegundos. |
| community_locations | Propuestas con identidad, nombre, territorio, block/circuitCode, datos de estado, submittedAt y ampliaciones geográficas. |
| app_config | config_key y config_value JSONB; configuración común por documento. |

Las mayúsculas de algunos campos son parte del contrato. created_at, reportedAtMillis, voltageObserved y community_sectors no aparecen en ese esquema. isCommunity enviado por algunos clientes tampoco se crea en la migración remota examinada. No se encontraron triggers que agreguen automáticamente todos los reportes en porcentajes o contadores.

app_config contiene pac_schedule, broadcast_notice, donations_config, barinas_geography y version; donations_url es un formato heredado. pac_schedule incluye versión, marca temporal, slots y matrixRows con cells, más listas sectorsA–D cuando el productor las conserva. Los timestamps difieren entre productores: milisegundos en clásico y updatedAt ISO en React con version numérica.

Los scripts activan RLS, pero permiten lectura/escrituras anónimas amplias. El alcance efectivo depende también de grants y configuración desplegada, no inspeccionados aquí. La publicación Realtime versionada añade sectors y app_config; escuchar otras tablas requiere configuración adicional. No se presentan estos scripts como una instalación segura ya validada.

### APIs web

| Ruta | Entrada | Salida y regla |
|---|---|---|
| /api/getMonthlyWeeks | monthName, totalDays | Cuatro grupos de semanas con matrices rotadas. |
| /api/getOutagePrediction | sectorId | Ventana estimada, duración, confianza, explicación y horas restantes. |
| /api/sendPushAlert | title, message, is_alarm | Solicita FCM global y responde con éxito/identificador o error. |

Los nombres reales de predicción son nextEstimatedStartMillis y nextEstimatedEndMillis; no devuelve isCurrentlyActive como campo público. Los handlers responden OPTIONS, pero no restringen los demás métodos ni comprueban autorización administrativa. Las APIs de lectura usan caché compartida: un cambio remoto puede tardar en reflejarse.

## 7. Reglas PAC y estimación ciudadana

La matriz usa filas de franjas y columnas lunes–domingo. Las letras A/B/C/D representan bloques; el guion indica ausencia de corte asignado. Los valores iniciales contienen seis franjas de cuatro horas, incluida 23:00–03:00, y las listas territoriales son datos editables o catálogos de respaldo.

El desplazamiento mensual es 0 para días 1–7, 1 para 8–14, 2 para 15–21 y 3 para 22–fin. Se aplica al índice de fila mediante módulo y reinicia al comenzar el mes. No es una rotación continua independiente del calendario ni una división por semanas ISO.

En Android, OutagePredictionEngine combina una ventana teórica con duración media del historial del sector, admitida entre dos y ocho horas, o cuatro horas de respaldo. Agrupa reportes sin luz por hora de Caracas; con al menos tres en la hora dominante puede ajustar el inicio cuando difiere de la teoría. Usa cifras heurísticas 88/94 para teoría, 78 para respaldo modular y una fórmula ciudadana limitada a 99.

No hay entrenamiento ni calibración estadística de esas cifras. Repeticiones, fechas de envío tardías y concentración de reportes pueden alterar la estimación. El final calculado puede diferir del final real de una franja personalizada. La API JavaScript utiliza una variante más simple y fija cuatro horas.

Los casos de medianoche, franjas iniciadas ayer, cambio de mes, listas vacías y zonas horarias no son uniformes entre capas. Algunas búsquedas devuelven una ventana de respaldo incluso sin programación válida. Para interpretar la pantalla se debe conservar la separación entre turno previsto, información ciudadana y estimación.

## 8. Flujos de sincronización y propuestas

Al iniciar, el ViewModel carga preferencias/PAC, prepara repositorios y observa Room y flujos remotos. CloudSync lee configuración y sectores y mantiene suscripciones mientras está activo. Aplica campos presentes, guarda PAC local y comunica cambios al estado. Los catálogos precargados permiten empezar sin nube, pero no garantizan actualidad.

Un reporte desde Dashboard pasa por ViewModel y EnergyRepository.submitReport: se registra en Room, modifica estado local de forma optimista, encola ReportPowerWorker e intenta un envío inmediato. NotificationActionReceiver y la acción del widget escriben Room y encolan trabajo directamente, sin pasar por ese ViewModel/repositorio ni iniciar ese envío inmediato. El worker, con requisito de conectividad, recorre pendientes e intenta insertarlos mediante CloudSync; también existe una ruta manual de sincronización.

Actualmente esas rutas no comparten una idempotencia remota completa. La confirmación inmediata puede elegir el último pendiente en vez del evento original; el uploader usa hora de envío y el worker puede aplicar otra vez cambios de contadores. Por tanto, guardar localmente no equivale a entrega única confirmada.

Una comunidad propuesta se envía a community_locations. El panel clásico permite revisar/editar, agrupar variantes y consultar geocodificación antes de crear o actualizar el sector. La salida de bandeja se representa mediante eliminación, sin un historial formal de decisiones. La aprobación agrupa varias operaciones y no es una transacción remota única. Su publicación PAC captura errores sin propagarlos al llamador: esperar esa función no confirma éxito para una aprobación o fusión posterior. En otro camino clásico, añadir un sector al bloque construye un upsert sin despacharlo, aunque sí publica el PAC. La función Android de publicar PAC conserva un retorno de éxito sin escritura real.

## 9. Notificaciones y actualización

Las alertas operativas Android son **locales**. PacAlarmScheduler utiliza AlarmManager para anticipación, comprobación de inicio y retorno previsto; PacAlarmReceiver muestra notificaciones y puede activar PacAlarmPlayer. NotificationActionReceiver convierte respuestas rápidas en pendientes y trabajo de sincronización. PredictiveNotificationWorker se agenda cada 30 minutos con flexibilidad de 15 y consulta horarios; no ejecuta el motor ciudadano completo.

El permiso de notificaciones se solicita en versiones que lo requieren. El sonido/vibración dependen de ajustes, canales y condiciones del sistema. El retorno avisado es el final programado, no una medición de que volvió la energía. Un cambio remoto de horario no garantiza reprogramación inmediata en todos los caminos, y un proceso frío puede no haber cargado el PAC persistido.

broadcast_notice llega por sincronización y puede producir un banner/notificación local. Esto es distinto de FCM. Aunque sendPushAlert usa Firebase Admin en servidor, el Android revisado no incluye dependencia Firebase Messaging, servicio receptor propio ni suscripción al tema global. No está acreditado el recorrido de push a app cerrada descrito históricamente.

AppUpdater consulta la última release de GitHub y compara nombres de versión, pero el botón actual abre **Google Play**, con alternativa HTTPS de la ficha. No descarga ni instala un APK. La configuración remota version todavía admite metadatos y URL heredada; las notas GitHub pueden marcar obligatoriedad. Publicar solo en GitHub podría anunciar algo aún no disponible en Play, por lo que ambos canales deben coordinarse. Los enlaces web a APK siguen siendo otro canal de distribución.

## 10. Configuración, construcción y publicación

Gradle usa un único módulo :app. El wrapper declara Gradle 9.3.1; el catálogo fija herramientas y bibliotecas. Android usa Kotlin/Compose, Room, WorkManager, Glance, Supabase/Ktor, publicidad y contratos Retrofit residuales. La firma depende de variables KEYSTORE_PATH, STORE_PASSWORD, KEY_ALIAS y KEY_PASSWORD con respaldos locales actuales; incluso debug usa la firma release configurada. No se documentan sus valores.

Para preparar Android se necesita SDK 36, JVM compatible, dependencias y directorios de caché escribibles. Compilar debug no valida minificación/firma de release. Publicar requiere incrementar versión, generar artefacto firmado y verificar el canal; esta documentación no construyó ni publicó una versión nueva.

En admin-web, npm ci instala el lockfile, npm run dev inicia Vite, npm run build genera dist/blog/sitemap y npm run preview sirve la salida. Vite local no ejecuta automáticamente las funciones Vercel. vercel.json configura build, publicación de dist, recursos con hash, rutas del blog y rewrite administrativo.

firebase.json aún publica desde la carpeta fuente con public="."; no equivale al procedimiento Vercel y requiere revisión antes de usarse. FIREBASE_SERVICE_ACCOUNT debe residir en el entorno servidor. Seeds y scripts de reemplazo antiguos escriben datos o código: no son pasos de lectura ni reparaciones genéricas. La operación detallada está en el capítulo 06.

## 11. Pruebas y estado de mantenimiento

Existen pruebas Android: motor predictivo, recurso de nombre, inserción/consulta Room, captura Robolectric/Roborazzi y contexto instrumentado del paquete. Su existencia corrige la afirmación histórica de ausencia de tests. No constituyen cobertura completa de migraciones, horarios, offline, permisos o integración. El test predictivo usa reloj real y supuestos sensibles a la ventana activa.

No se validó una ejecución completa de la suite en esta actualización; un intento previo quedó limitado por la preparación de caché Gradle. No corresponde declarar tests aprobados ni fallidos a partir de un error de arranque. La web no declara scripts test/lint/typecheck en su manifiesto. Las comprobaciones locales con servicios simulados no sustituyen una prueba de producción.

| Área pendiente | Estado actual y trabajo necesario |
|---|---|
| Identidad y permisos | Sustituir acceso visual por autenticación/roles de servidor y restringir RLS; no se ha aplicado. |
| Emisión de alertas | Proteger sendPushAlert, validar método/payload/cuota y resolver la recepción FCM Android. |
| Textos remotos | Escapar contenido del panel clásico y del mapa; limitar navegación/puentes. |
| Integridad de reportes | ID estable, fecha original, confirmación precisa e idempotencia; separar transporte de contadores. |
| Horarios y alarmas | Unificar regla temporal, madrugada/mes/zona, duración y rearmado con PAC persistido. |
| Doble administración | Unificar rutas/contratos; conservar campos completos y persistir sectores React. |
| Estado de carga | Comprobar errores Supabase/HTTP y evitar éxito aparente o sobrescritura tras lectura fallida. |
| Esquema y evolución | Resolver campos/tablas divergentes, migraciones, restricciones y respaldo/recuperación. |
| Estimaciones y presentación | Evaluar confianza, diferenciar datos reales de respaldos y actualizar textos legales/promocionales. |
| Distribución y calidad | Coordinar GitHub/Play, revisar Firebase heredado y ejecutar pruebas reproducibles. |

Esta tabla registra trabajo abierto; actualizar documentos no lo corrige. Los permisos y operaciones reales del servidor requieren una revisión específica del entorno antes de afirmar su estado.

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

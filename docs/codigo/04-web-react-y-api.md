# 04. Manual de mantenimiento del sitio React, modo TV y APIs

Fecha de lectura del código: **15 de octubre de 2026**. Este capítulo describe la implementación local existente, sus conexiones y sus límites. No representa una comprobación del despliegue ni del contenido actual de Supabase, Firebase, Google Play o Vercel. Los ejemplos HTTP son ilustrativos y no se ejecutaron contra servicios reales. Se omiten deliberadamente contraseñas, claves y datos de pago.

El alcance incluye **todos los archivos de `admin-web/src`**, los tres archivos de `admin-web/api`, el generador `scripts/blog-prerender.mjs`, `vite.config.mjs`, `index.html`, `package.json`, `vercel.json` y `firebase.json`. El panel antiguo `admin.html`/`public/app.js`, su copia raíz y el SQL se explican en otros capítulos; aquí aparecen únicamente cuando condicionan al código React. `dist`, dependencias instaladas y recursos binarios no son fuentes de esta explicación.

## Índice de lectura

1. [Cómo arranca el sitio y cómo se mueve entre páginas](#1-arranque-y-navegación).
2. [Datos, caché, cálculo de horarios y SEO](#2-bibliotecas-compartidas).
3. [Inicio y componentes públicos](#3-página-principal-y-componentes-públicos).
4. [Blog y contenido editorial](#4-blog-y-contenido-editorial).
5. [Páginas legales, transparencia y créditos](#5-páginas-legales-transparencia-y-créditos).
6. [Panel administrativo React](#6-panel-administrativo-react).
7. [Modo TV y sus seis temas](#7-modo-tv-y-sus-seis-temas).
8. [Contratos de las APIs](#8-apis-serverless).
9. [Prerender, compilación y alojamiento](#9-prerender-compilación-y-alojamiento).
10. [Recetas de mantenimiento y verificación](#10-recetas-de-mantenimiento-y-verificación).
11. [Índice de funciones y cobertura](#11-índice-de-funciones-y-cobertura).

## Convenciones para leer este capítulo

Un **componente** es una función que devuelve JSX: la descripción de una parte de la pantalla. Sus **props** son los datos o callbacks que recibe de su padre. El **estado** (`useState`) conserva información mientras ese componente está montado; no equivale a guardar en la nube. Un **efecto** (`useEffect`) conecta el componente con temporizadores, eventos, el documento o servicios y puede devolver una limpieza. `useMemo` conserva un cálculo derivado y `useCallback` conserva una función entre renders mientras sus dependencias no cambian.

Salvo indicación expresa, los handlers devuelven `undefined`; sus variantes `async` devuelven `Promise<void>`. Una función de formato devuelve texto; un componente devuelve un elemento React o árbol JSX. Los números de línea indican el inicio de la función o del bloque relevante en la copia leída y pueden desplazarse después de futuras ediciones.

La mayoría del sitio público lee directamente Supabase mediante su cliente JavaScript. Que una pantalla utilice un estado llamado `isAuthenticated` no concede permisos en Supabase ni autentica las APIs. Cada autorización real debe comprobarse en el servicio que recibe la operación.

## 1. Arranque y navegación

### 1.1 `index.html`: documento que recibe el navegador

Fuente: [index.html](<E:/LUZ BARINAS/admin-web/index.html:1>), 46 líneas.

Este archivo no contiene el panel administrativo antiguo. Es la plantilla del sitio público: idioma español, codificación UTF-8, viewport adaptable, título inicial, descripción, palabras clave, favicon y enlace canónico a `pacbarinas.site`. Las líneas 20–30 aportan Open Graph y Twitter para compartir enlaces. Las líneas 33–35 cargan fuentes de Google y 38–40 incluyen AdSense. Son solicitudes de recursos de terceros realizadas por el navegador; no son lecturas de Supabase.

El cuerpo contiene `div#root` en la línea 43. El módulo `/src/main.jsx`, línea 44, monta la aplicación dentro de ese contenedor. En desarrollo Vite sirve ese módulo; al compilar reemplaza las referencias por recursos con nombres generados. El generador del blog reutiliza el HTML compilado y rellena ese mismo contenedor con contenido estático antes de publicarlo.

**Límites:** los metadatos de esta plantilla pertenecen al inicio. Las páginas legales solo cambian el título, de modo que una navegación interna hacia ellas puede conservar descripción o canónico de otra página. No todas las páginas reciben una salida HTML específica durante la compilación: el prerender propio se centra en el blog.

### 1.2 `src/main.jsx`: punto de entrada

Fuente: [main.jsx](<E:/LUZ BARINAS/admin-web/src/main.jsx:1>), 10 líneas.

Importa React, `react-dom/client`, `App` y `index.css`. En la línea 6 ejecuta `ReactDOM.createRoot(document.getElementById('root')).render(...)` y envuelve `App` en `React.StrictMode`. No declara funciones propias ni recibe parámetros. El resultado es el montaje de la interfaz; no crea usuarios, tablas ni datos iniciales.

`createRoot` sustituye el contenido prerenderizado al montar React; no usa `hydrateRoot`. En desarrollo, Strict Mode puede repetir montajes y efectos para detectar problemas de limpieza. Esto importa al interpretar consultas duplicadas durante desarrollo: no implica por sí mismo que producción las ejecute dos veces.

### 1.3 `src/App.jsx`: enrutador manual

Fuente: [App.jsx](<E:/LUZ BARINAS/admin-web/src/App.jsx:17>), 112 líneas.

**`App()` — línea 17.** No recibe props. Mantiene `currentRoute`, calcula si hay artículo o modo TV y devuelve el armazón de navegación, página actual y pie. Carga el inicio de forma directa y las otras páginas con `React.lazy`; `Suspense` muestra un indicador mientras llega el módulo. Solo la página correspondiente a la ruta se monta. Oculta `Navbar` y `Footer` en `/admin` y `/tv`.

**`getInitialRoute()` — línea 19.** Es una función interna sin parámetros; devuelve una cadena de ruta normalizada a partir de `window.location.pathname.toLowerCase()`. Comprueba prefijos y conserva el slug de blog quitando barras finales. La inicialización de `useState` y `handlePopState` la llaman. No consulta red ni almacena nada.

| Dirección reconocida al abrir o volver con el navegador | Estado de ruta resultante | Componente |
|---|---|---|
| Prefijo `/admin` | `/admin` | `AdminPortal` si se está ejecutando React |
| Prefijo `/tv` o `/kiosk` | `/tv` | `TvKioskMode` |
| `/blog` y `/blog/...` | Ruta de blog sin barra final | `Blog` o `BlogPost` |
| Prefijo `/privacidad` o `/politica` | `/privacidad` | `PrivacyPolicy` |
| Prefijo `/terminos` o `/condiciones` | `/terminos` | `TermsConditions` |
| Prefijo `/transparencia` | `/transparencia` | `Transparencia` |
| Prefijo `/desarrollador` | `/desarrollador` | `DeveloperInfo` |
| Prefijo `/reembolsos` | `/reembolsos` | `RefundPolicy` |
| Cualquier otra dirección | `/` | `LandingPage` |

**`handlePopState()` — línea 39.** Callback sin parámetros registrado por el efecto de la línea 38. Recalcula `currentRoute` al usar Atrás/Adelante. El efecto quita el listener al desmontarse. Su retorno no se utiliza y no escribe historial.

**`navigate(route)` — línea 47.** Recibe la ruta de destino. Si difiere de `window.location.pathname`, ejecuta `history.pushState`; después guarda `route` en el estado React. Se entrega como `setCurrentRoute` a páginas, barra y pie. No hace una navegación HTTP ni valida la ruta contra una lista. Las variantes de navegación de cada componente deciden además si desplazan la pantalla.

`blogSlug` deriva de `currentRoute.slice('/blog/'.length)` y `BlogPost` recibe `key={blogSlug}`, por lo que cambiar de artículo produce una instancia nueva. Las rutas desconocidas que entran por `getInitialRoute` caen al inicio, pero un `navigate('/ruta-desconocida')` puede dejar el área principal sin página porque no hay un caso general para ese estado. Tampoco existe un enrutador declarativo con rutas, resolución de errores y página 404 común.

### 1.4 La misma ruta administrativa llega a dos implementaciones

Es una condición de mantenimiento esencial. Desde `Navbar` o `Footer`, `navigate('/admin')` cambia el estado e historial y monta `src/pages/AdminPortal.jsx`. En cambio, las reescrituras de [vercel.json](<E:/LUZ BARINAS/admin-web/vercel.json:8>) sirven `admin.html` cuando una solicitud HTTP llega a `/admin` o `/admin/...`. Refrescar la página puede cambiar de panel aunque la dirección parezca igual.

El panel antiguo carga `app.js`; en la distribución Vite ese archivo procede de `public/app.js`. La copia `admin-web/app.js` presenta diferencias respecto de `public/app.js` en el manejo de tipos de fallas y bloques sin asignar; no debe asumirse que modificar la copia raíz cambia el archivo distribuido. Esta explicación no extiende el alcance a las funciones internas de ese panel.

## 2. Bibliotecas compartidas

### 2.1 `src/lib/supabase.js`: cliente y valores iniciales

Fuente: [supabase.js](<E:/LUZ BARINAS/admin-web/src/lib/supabase.js:1>), 75 líneas. No declara funciones propias.

En la línea 6 exporta `supabase = createClient(supabaseUrl, supabaseKey)`. Utiliza una clave pública de cliente; los valores concretos se omiten aquí. El objeto se comparte entre formularios públicos, telemetría y panel React. El módulo no inicia una sesión administrativa con Supabase Auth. Las políticas del servidor determinan qué operaciones puede realizar ese cliente.

Exportaciones de datos:

| Exportación y línea | Estructura | Consumidores y significado |
|---|---|---|
| `DEFAULT_SLOTS`, 8 | Seis objetos `{index,timeLabel,startHour,endHour}` | Franjas iniciales de cuatro horas, comenzando a las 03:00; la última cruza de 23:00 a 03:00. Telemetría y panel las usan cuando no han cargado configuración. |
| `DEFAULT_CORPOELEC_MATRIX`, 17 | Matriz de seis filas por siete días | Cada celda contiene un bloque. Los valores iniciales usan A/B/C; que exista el bloque D en otras pantallas no significa que este valor inicial lo programe. |
| `DAYS_NAMES`, 26 | Array lunes–domingo | Convención de columnas de las matrices. No coincide directamente con `Date.getDay()`, que comienza por domingo. |
| `DEFAULT_SECTORS_BY_BLOCK`, 28 | Objeto con arrays A/B/C/D | Búsqueda de respaldo y estado inicial del gestor React. Es un catálogo estático, no una lectura de las asignaciones remotas. Puede contener nombres repetidos o parecidos en bloques distintos. |

Un cambio de estas constantes modifica los valores de respaldo empaquetados para todos los usuarios futuros; no migra registros remotos ni reemplaza automáticamente una configuración que ya se haya guardado en Supabase.

### 2.2 `src/lib/telemetry.js`: consultas y reloj compartidos

Fuente: [telemetry.js](<E:/LUZ BARINAS/admin-web/src/lib/telemetry.js:1>), 335 líneas. Lo consumen `PacLiveWidget`, `TvKioskMode` y los componentes de reportes. No usa las APIs serverless para obtener el cronograma público: consulta Supabase directamente.

#### `formatRelativeTime(timestampMillis)` — línea 10

Recibe una marca de tiempo en milisegundos o un valor convertible a número y devuelve texto en español: `Reciente` cuando no hay valor, `Hace un momento`, minutos, horas o fecha localizada. Calcula la diferencia respecto de `Date.now()` con umbrales de 45 s, 90 s, una hora y un día. `CitizenReportsTicker` lo llama por reporte. No tiene estado ni persistencia. Una fecha futura también puede caer en `Hace un momento`; no valida marcas de tiempo inválidas o futuras.

#### `calculatePacTime(slots = DEFAULT_SLOTS, matrix = DEFAULT_CORPOELEC_MATRIX)` — línea 30

Recibe franjas y matriz; devuelve el objeto `pacTime` que alimenta las pantallas. Sus pasos son:

1. Crea una fecha con la hora civil de `America/Caracas` mediante `toLocaleString`, que vuelve a interpretar como fecha local.
2. Convierte domingo 0 a la columna 6 y lunes 1 a la columna 0.
3. Define `weekShift`: 0 para días 1–7, 1 para 8–14, 2 para 15–21 y 3 para 22 hasta fin de mes. Son grupos de días del mes, no semanas ISO.
4. Busca la franja actual. Para una franja normal comprueba `startHour <= hora < endHour`; para una nocturna comprueba hora posterior al inicio **o** anterior al final.
5. Desplaza la fila `(activeSlotIdx + weekShift) % matrix.length` y lee la columna del día. Usa B como bloque activo de respaldo si no encuentra celda.
6. Calcula la siguiente franja con vuelta al índice 0. Para esa vuelta incrementa el día y recalcula el desplazamiento; utiliza A como bloque siguiente de respaldo.
7. Construye el final de la franja y resta la hora actual para obtener `countdown`, limitado a valores no negativos.

| Campo devuelto | Uso |
|---|---|
| `caracasDate`, `dayIndex`, `dayName` | Fecha civil e identificación de la columna visible. |
| `activeSlotIdx`, `activeSlot`, `activeCutBlock` | Franja y bloque actual calculados desde el cronograma. |
| `nextSlotIdx`, `nextSlot`, `nextCutBlock` | Relevo siguiente. |
| `countdown` | `{hours,minutes,seconds,totalSeconds,formatted}`; `formatted` forma `00h 00m 00s`. |

**Quién la llama:** `useTelemetry` mediante un `useMemo` que depende del reloj de un segundo, las franjas y la matriz. No consulta la red ni mide electricidad. Un bloque teóricamente activo no confirma un apagón físico.

**Límites actuales:** arrays de franjas vacíos pueden dejar `activeSlot` indefinida; no hay validación de cobertura, solapamiento, longitud de filas ni rango de horas. Si ningún intervalo coincide conserva el índice 0. La selección de columna y de turno siguiente durante la madrugada requiere revisar el criterio de qué día es dueño de la franja 23:00–03:00: el código usa el día civil actual y puede tratar las 03:00 de la misma mañana como mañana. El incremento de `nextDayOfMonth` no normaliza por sí mismo un cambio de mes. No conviene copiar esta lógica sin definir primero esos casos de calendario.

#### `processCircuitsSummary(sectors = [])` — línea 114

Helper interno; recibe sectores y devuelve un array de resúmenes de circuitos. Agrupa por `circuitCode.trim()` o `Circuito Urbano` si falta, asigna a cada grupo el bloque del primer sector o A, acumula reportes, porcentajes y voltajes positivos, y conserva hasta tres nombres de ejemplo.

Calcula `stabilityPercent = max(0, 100 - promedio(withoutPowerPercentage))`; no es una medición directa de estabilidad de voltaje ni un cálculo de probabilidad. El voltaje medio excluye los valores cero y usa 118 si no hay voltajes válidos. Clasifica `<45` como `CORTE_ACTIVO`, `<80` como `INESTABLE`, y el resto `ESTABLE`. Cuenta además sectores con un estado que contiene `OUTAGE` o porcentaje sin luz superior a 50. Ordena el resultado por reportes confirmados y después número de sectores, en orden descendente.

Cada elemento devuelve `{name,block,sectorsCount,confirmedReports,stabilityPercent,avgVoltage,outageSectors,normalSectors,sampleSectors,status}`. `fetchTelemetryFromSupabase` lo llama y los widgets/temas lo muestran. No persiste el resumen. Si distintos circuitos comparten código genérico quedan agrupados; si un circuito tiene sectores de varios bloques, solo queda el bloque del primero. Los voltajes de respaldo deben distinguirse de observaciones reales al interpretar la pantalla.

#### `fetchTelemetryFromSupabase()` — línea 181

Función interna asíncrona sin parámetros. Ejecuta **cuatro consultas secuenciales**:

| Orden | Tabla y selección | Filtro/orden/límite |
|---|---|---|
| 1 | `citizen_reports`: `id,sectorId,sectorName,hasPower,reportType,voltage,timestamp` | Últimos 20 por `timestamp` descendente. No filtra por municipio. |
| 2 | `sectors`: identificador, nombre, circuito, estado, porcentajes, reportes, voltaje, bloque y municipio | `municipio = 'Barinas'`. Este conjunto no equivale a todo el estado Barinas. |
| 3 | `app_config.config_value` | `config_key = 'pac_schedule'`, una fila. |
| 4 | Conteo exacto de `citizen_reports` con `head:true` | No descarga las filas del conteo. |

Reconstruye la matriz desde `matrixRows[].cells` y toma `slots` del JSON si existen; de lo contrario conserva las constantes locales. Llama a `processCircuitsSummary` y devuelve `{reports,sectors,circuits,matrix,slots,totalReportsCount,timestamp}`. Los arrays faltantes se sustituyen por vacíos. El conteo usa alternativas locales cuando no obtiene uno; los números de respaldo no prueban un volumen real de reportes.

No recoge ni lanza explícitamente los campos `error` que devuelve Supabase. Muchas fallas de consulta se convierten así en listas vacías o valores por defecto en lugar de llegar al `catch` del hook. Tampoco valida el JSON remoto ni cancela una petición si el usuario abandona la página. El comentario «verified citizen reports» es descriptivo: estas consultas no aplican un filtro de verificación o moderación.

#### `useTelemetry()` — línea 235

Hook público sin parámetros. Devuelve los arrays públicos, matriz, franjas, `pacTime`, `totalReportsCount`, `lastUpdated`, `isLoading`, `error` y `refresh`. Mantiene los siguientes estados:

| Estado/recurso | Inicialización y duración |
|---|---|
| `data` | Lee JSON de `sessionStorage['pac_telemetry_cache_v1']`, si puede. Vive durante el montaje y la sesión de esa pestaña. |
| `isLoading` | `!data`; indica una actualización solicitada en primer plano. |
| `lastUpdated` | Marca temporal de la caché o `null`. |
| `error` | Última excepción capturada por `refresh`; no todos los errores Supabase llegan aquí. |
| `nowTick` | `Date.now()`, actualizado cada segundo. |

El efecto de la línea 257 crea un temporizador de un segundo y lo elimina al desmontar. No consulta la red cada segundo: únicamente fuerza recalcular el reloj y la cuenta regresiva. El efecto de la línea 283 comprueba si la caché tiene menos de 60 segundos; si no, pide datos. Después programa consultas cada **60 000 ms** cuando `document.visibilityState === 'visible'` y registra el retorno a la pestaña. Limpia intervalo y listener; vuelve a establecerlos cuando cambia `data` o `lastUpdated`.

**`refresh(isBackground = false)` — línea 262.** Callback asíncrono memorizado. En primer plano activa `isLoading`; pide el conjunto de telemetría, actualiza datos y fecha, elimina el error y escribe la caché de sesión. Si hay una excepción la registra y guarda `error`; al terminar desactiva la carga de primer plano. Lo llaman el efecto inicial, el polling, el evento de visibilidad y el botón Actualizar del sitio. No comparte una solicitud pendiente entre instancias del hook.

**`handleVisibilityChange()` — línea 297.** Callback del efecto de polling; al volver a una pestaña visible calcula cuánto pasó desde `lastUpdated` y llama `refresh(true)` si supera 60 segundos. No hace nada mientras está oculta.

**`pacTime`, `useMemo` — línea 317.** No es una función pública separada: es el resultado memorizado de `calculatePacTime(slots,matrix)` con dependencia de `nowTick`. Los datos pueden ser antiguos mientras la cuenta regresiva sigue moviéndose. El hook no abre una suscripción Realtime, aunque la interfaz use expresiones como «en vivo».

### 2.3 `src/lib/seo.js`: metadatos al navegar dentro del sitio

Fuente: [seo.js](<E:/LUZ BARINAS/admin-web/src/lib/seo.js:11>), 85 líneas. Lo importan las páginas del blog.

**`upsert(selector, create, attr, value, restorers)` — línea 11.** Busca un elemento en `document.head`; si no existe lo obtiene llamando a `create()` y lo inserta. Guarda el atributo anterior, coloca el nuevo y añade a `restorers` una función que restaura el atributo o elimina el elemento recién creado. No devuelve un valor útil. El hook lo llama para metas y canónico. Solo modifica el documento de esa pestaña.

**`metaByName(name)` — línea 30.** Recibe el nombre de una meta y devuelve una función sin argumentos que crea `<meta name="...">`. Es una fábrica que se pasa como `create` a `upsert`, no una inserción inmediata.

**`metaByProperty(property)` — línea 36.** Igual a la anterior, pero crea `<meta property="...">`, apropiada para Open Graph. Devuelve la fábrica; no consulta red.

**`useDocumentMeta({title, description, canonical, keywords, type = 'website', jsonLd})` — línea 42.** Hook sin retorno útil. Su efecto guarda el título anterior y actualiza título, descripción, palabras clave, Open Graph, Twitter y enlace canónico según los valores recibidos. Para `jsonLd` elimina scripts previos marcados `data-blog-jsonld`, crea un `application/ld+json` y usa `textContent = JSON.stringify(jsonLd)`. Al desmontar restaura el título, ejecuta los restauradores en orden inverso y quita el script creado.

La restauración cubre metas que el propio efecto modificó, pero no reconstruye los JSON-LD prerenderizados que eliminó. No gestiona todas las metas posibles —por ejemplo imágenes sociales o fecha de publicación— ni sustituye al HTML estático para lectores que no ejecutan JavaScript. Los enlaces a `build/blog-prerender.mjs` en comentarios son antiguos: la ruta real del generador es `scripts/blog-prerender.mjs`.

### 2.4 `src/index.css`: presentación común

Fuente: [index.css](<E:/LUZ BARINAS/admin-web/src/index.css:1>), 176 líneas. Es importado por `main.jsx`; no contiene funciones JavaScript ni red/persistencia.

Importa Tailwind en la línea 1. `@theme` define colores, familias tipográficas y radios, mientras `:root` repite parte de las variables como propiedades CSS. El cuerpo fija fondo, tinta, tipografía y suavizado. `.notion-border`, `.notion-nav-shadow` y `.notion-mockup-shadow` dan borde y sombra a tarjetas y navegación.

Las reglas `.article-prose` de las líneas 71–134 dan formato a HTML editorial: títulos, párrafos, enlaces, listas, citas y adaptación por debajo de 640 px. `scroll-margin-top` mantiene visibles los títulos al seguir el índice del artículo. Las reglas de scrollbar de 137–150 son presentación del navegador.

`@keyframes radar-sweep` (153) rota un radar; `.animate-radar-sweep` (163) lo repite cada 6 s. `@keyframes kiosk-marquee` (158) desplaza hasta -50%; `.animate-kiosk-marquee` (168) lo repite cada 35 s y se pausa al pasar el cursor. La duplicación de reportes realizada por `TvKioskMode` permite que ese -50% forme un bucle visual. No existe aquí una excepción de movimiento reducido; el tema de rendimiento cambia efectos de las vistas, pero no demuestra una tasa real de 60 FPS.

## 3. Página principal y componentes públicos

### 3.1 `LandingPage({setCurrentRoute})` — `src/pages/LandingPage.jsx:9`

Fuente: [LandingPage.jsx](<E:/LUZ BARINAS/admin-web/src/pages/LandingPage.jsx:9>), 24 líneas.

Devuelve la página de inicio con este orden: `Hero`, `PacLiveWidget`, `Features`, `BlogPreviewSection`, `DonationsSection`, `ContactSection`. Entrega la navegación a los dos componentes que cambian de ruta. El efecto de la línea 10 fija `document.title` al montar y no lo restaura. No tiene estado propio ni red: las consultas y formularios pertenecen a los hijos. Lo monta `App` para `/`.

### 3.2 `Navbar({currentRoute,setCurrentRoute})` — `src/components/Navbar.jsx:4`

Fuente: [Navbar.jsx](<E:/LUZ BARINAS/admin-web/src/components/Navbar.jsx:4>), 196 líneas.

Muestra marca, enlaces a secciones del inicio, Blog, modo TV, descarga de APK y acceso administrativo, con variantes de escritorio y menú móvil. `mobileMenuOpen` comienza en `false`; el botón de menú lo alterna. No lee Supabase ni guarda preferencias.

**`navigateTo(route, anchorId = null)` — línea 7.** Primero llama `setCurrentRoute(route)` y cierra el menú. Si recibe ancla, espera 50 ms, busca el identificador y ejecuta `scrollIntoView({behavior:'smooth'})`; si no la recibe desplaza a la parte superior. Lo llaman los enlaces y botones. Las secciones de destino son `inicio`, `proyecto`, `cronograma`, `donaciones` y `contacto`. No almacena el ancla en la URL ni espera formalmente a que termine una carga diferida; el plazo fijo puede ser insuficiente en un montaje lento.

Los enlaces de descarga apuntan a un APK concreto de GitHub Releases y no preguntan a la API de última versión. Cambiar la versión de la app requiere revisar también esas direcciones. La presencia del candado solo permite llegar al panel React; no comprueba la autorización.

### 3.3 `Footer({setCurrentRoute})` — `src/components/Footer.jsx:4`

Fuente: [Footer.jsx](<E:/LUZ BARINAS/admin-web/src/components/Footer.jsx:4>), 127 líneas.

Muestra identidad, aviso comunitario, enlaces internos —inicio, blog, TV, privacidad, términos, transparencia, desarrollador, reembolsos—, código fuente, descarga y entrada administrativa. Usa el año actual del navegador para el copyright. No tiene estado, efectos ni consultas.

**`navigateTo(route)` — línea 5.** Cambia la ruta mediante la prop y desplaza suavemente al inicio. Retorno sin uso; lo llaman los controles del pie. Los enlaces externos quedan a cargo del navegador. La descarga vuelve a estar vinculada a una versión concreta de APK.

### 3.4 `Hero()` — `src/components/Hero.jsx:4`

Fuente: [Hero.jsx](<E:/LUZ BARINAS/admin-web/src/components/Hero.jsx:4>), 166 líneas.

Devuelve la cabecera promocional: titular, explicación del proyecto, distintivos y botones de descarga. Solo mantiene `playStoreModal`, inicialmente `false`. El botón de Google Play abre un modal informativo; sus botones lo cierran y uno permite descargar el APK concreto desde GitHub. Esos callbacks son anónimos y solo invocan `setPlayStoreModal`.

No busca una publicación real de Google Play ni confirma disponibilidad. Tampoco detecta actualizaciones. Los gráficos, avatares, indicadores y textos de esta sección son contenido de presentación; no proceden de la telemetría.

### 3.5 `Features()` — `src/components/Features.jsx:4`

Fuente: [Features.jsx](<E:/LUZ BARINAS/admin-web/src/components/Features.jsx:4>), 148 líneas.

No recibe parámetros. Construye el array local `cards` y devuelve tarjetas explicativas sobre reportes anónimos, predicción y características de la aplicación, además de avisos de independencia y reconocimiento al origen de la idea. El mapeo de tarjetas es puramente visual. Incluye un enlace externo de reconocimiento. No tiene estado, hooks ni peticiones.

El identificador de sección permite llegar a `proyecto` desde la barra. Las promesas que aparecen en el texto —filtrado, anonimato, predicción— no añaden controles técnicos; deben comprobarse por separado en los repositorios y servicios que realmente procesan datos.

### 3.6 `PacLiveWidget({setCurrentRoute})` — `src/components/PacLiveWidget.jsx:9`

Fuente: [PacLiveWidget.jsx](<E:/LUZ BARINAS/admin-web/src/components/PacLiveWidget.jsx:9>), 248 líneas.

Es el contenedor público de horarios y telemetría. Llama `useTelemetry()` y recibe reportes, sectores, circuitos, conteo, matriz, franjas, `pacTime`, fecha de actualización y `refresh`. Mantiene únicamente `searchQuery` como estado propio.

Muestra `LiveBlockCountdown`, `CitizenReportsTicker`, tabla del día, buscador de sectores, acceso al modo TV e `CircuitStabilityWidget`. El botón Actualizar invoca `refresh(false)` y se desactiva mientras `isLoading`. El cálculo de celdas de hoy usa la columna de `pacTime` y el desplazamiento por grupo de siete días. No llama a `getMonthlyWeeks` ni a `getOutagePrediction`.

**Búsqueda — líneas 30–55.** Cuando la consulta recortada supera un carácter, recorre primero los sectores remotos y después el catálogo `DEFAULT_SECTORS_BY_BLOCK`. Coincide con `includes` en minúsculas. Normaliza el bloque desde `rotationBlock` o `rotation_block`, o usa A. Un `Set` de nombres en minúsculas evita añadir de nuevo esos nombres al recorrer el catálogo. El resultado contiene `{name,block,circuit}`. Es un cálculo durante render, no una función nombrada ni una consulta al servidor. El resultado remoto solo contiene los sectores del municipio Barinas obtenidos por el hook, mientras el catálogo de respaldo puede incluir otros municipios.

**Límites:** datos remotos y catálogo estático pueden discrepar; no lee las asignaciones `sectorsA-D` de `pac_schedule` para esta búsqueda. Indicadores «en vivo» pueden convivir con caché o datos por defecto. El error expuesto por `useTelemetry` no se utiliza como error visible en esta vista.

### 3.7 `LiveBlockCountdown({pacTime})` — `src/components/telemetry/LiveBlockCountdown.jsx:24`

Fuente: [LiveBlockCountdown.jsx](<E:/LUZ BARINAS/admin-web/src/components/telemetry/LiveBlockCountdown.jsx:4>), 150 líneas.

Recibe el objeto calculado por el hook, desestructura día, franja, bloque actual, siguiente y cuenta regresiva y devuelve una tarjeta con esos datos. No tiene estado, temporizadores ni consultas propios. Lo llama `PacLiveWidget`; el paso de los segundos lo provoca el padre.

**`getBlockBadgeClass(block)` — línea 4.** Recibe A/B/C/D y devuelve clases Tailwind para el distintivo: azul, ámbar, morado, verde o gris si no se reconoce. La reutilizan tabla, tarjetas, TV y widgets.

**`getBlockBgSoft(block)` — línea 14.** Recibe el mismo bloque y devuelve clases para fondo suave, texto y borde con la misma familia de color. Se exporta, pero no se encontraron llamadas en las fuentes React actuales; es un helper disponible sin uso efectivo. Ambos helpers son deterministas y no convierten automáticamente texto como `Bloque A` en A.

La tarjeta representa el cronograma referencial. No valida por sí misma que haya llegado información reciente de campo; la animación del punto de conexión es presentación.

### 3.8 `CitizenReportsTicker({reports = [],totalCount = 2002})` — `src/components/telemetry/CitizenReportsTicker.jsx:5`

Fuente: [CitizenReportsTicker.jsx](<E:/LUZ BARINAS/admin-web/src/components/telemetry/CitizenReportsTicker.jsx:5>), 184 líneas.

Mantiene `modalOpen`. Muestra hasta diez reportes en una banda horizontal y un botón que abre un modal de historial con los reportes ya recibidos. El historial no hace una consulta nueva ni descarga toda la historia de la base: con el hook actual dispone de un máximo de veinte. Usa `formatRelativeTime` para sus marcas temporales. Cerrar el modal modifica el estado local; no elimina datos.

**`getStatusBadge(report)` — línea 8.** Recibe un reporte y devuelve `{label,icon,badgeClass,dotClass}`. Primero considera corte cuando falta energía o `reportType === 'SIN_LUZ'`; después fluctuación si `reportType === 'IRREGULAR'` o voltaje positivo fuera de 100–130; de lo contrario servicio. Esta prioridad significa que un reporte simultáneamente sin luz e irregular se presenta como corte. Los tipos `FALLA_IRREGULAR`/`IRREGULAR_OUTAGE` usados en otras capas no se reconocen aquí por nombre, salvo que el voltaje o `hasPower` lo hagan caer en otra rama.

No tiene persistencia ni red. El conteo por defecto y la palabra «validados» son presentación, no una comprobación de moderación. El modal no implementa por sí mismo una gestión completa de foco, Escape y restauración de foco.

### 3.9 `CircuitStabilityWidget({circuits = [],activeCutBlock = 'B'})` — `src/components/telemetry/CircuitStabilityWidget.jsx:5`

Fuente: [CircuitStabilityWidget.jsx](<E:/LUZ BARINAS/admin-web/src/components/telemetry/CircuitStabilityWidget.jsx:5>), 216 líneas.

Recibe el resultado agregado del hook. Conserva `circuitFilter` para un buscador local y muestra métricas generales, tarjetas de circuitos y nota de interpretación comunitaria. Resalta la tarjeta cuyo bloque coincide con el corte calculado, sin modificar su estado observado.

**`summary`, `useMemo` — línea 9.** Cuenta grupos `ESTABLE`, `INESTABLE` y restantes, y promedia `stabilityPercent`. Devuelve `{total,stable,unstable,outage,avgStability}`. Si no hay circuitos presenta `avgStability:100`; ese 100 no prueba servicio normal, porque también representa ausencia de muestras.

**`filteredCircuits`, `useMemo` — línea 35.** Devuelve todos o filtra el nombre por subcadena en minúsculas. La vista muestra los primeros 18 y explica cómo buscar el resto. No pagina ni vuelve a consultar.

**`getStatusConfig(status)` — línea 41.** Devuelve etiqueta, clases del distintivo, color de barra e icono para `CORTE_ACTIVO`, `INESTABLE` y `ESTABLE`; desconocidos caen a servicio estable. Lo llama el mapeo de tarjetas. Las barras usan `stabilityPercent`; el voltaje con prefijo `~` proviene del resumen, incluidos sus valores de respaldo. No tiene efectos ni persistencia.

### 3.10 `DonationsSection()` — `src/components/DonationsSection.jsx:5`

Fuente: [DonationsSection.jsx](<E:/LUZ BARINAS/admin-web/src/components/DonationsSection.jsx:5>), 250 líneas.

Devuelve una sección de aportes con pestañas Binance, Pago Móvil y PayPal. Los estados son `activeTab` (comienza en Binance), `copiedField` y `donationConfig`, inicialmente un objeto con valores estáticos de respaldo. No se copian aquí los datos de pago.

**`loadRemoteDonations()` — línea 18.** Función asíncrona interna al efecto de la línea 17, invocada una vez al montar. Lee una fila `app_config` con `config_key='donations_config'` y fusiona `config_value` con la configuración inicial. Si no hay fila o hay excepción conserva valores iniciales; solo registra la excepción. No comprueba el `error` devuelto por Supabase. No abre Realtime: editar donaciones en otra pantalla no actualiza esta instancia hasta recargar o remontar.

**`copyToClipboard(text,fieldKey)` — línea 39.** Ignora texto vacío, llama a `navigator.clipboard.writeText(text)`, marca el campo copiado y lo limpia 2,5 s después. La llaman botones para cada dato y para un resumen de Pago Móvil compuesto en un callback anónimo. No espera el resultado de la promesa ni muestra fallo si se deniega el portapapeles. Requiere compatibilidad y contexto seguro del navegador.

El botón PayPal usa la URL remota o el respaldo en un enlace externo. La sección no procesa ni comprueba pagos, no genera recibos y no verifica que las cuentas correspondan a una identidad determinada. Para mantenerla, separar «configuración no cargada» de «configuración válida» evita presentar un destino de respaldo como si acabara de verificarse.

### 3.11 `ContactSection()` — `src/components/ContactSection.jsx:5`

Fuente: [ContactSection.jsx](<E:/LUZ BARINAS/admin-web/src/components/ContactSection.jsx:5>), 228 líneas.

Muestra enlaces directos de contacto y un formulario de nombre, sector, correo y mensaje. Conserva `formData`, `submitted` y `loading`. Los campos `onChange` actualizan una propiedad mediante copia del objeto. La sección recopila los valores opcionales que el visitante escriba, por lo que sus textos deben distinguir el uso anónimo básico de la entrega voluntaria de datos de contacto.

**`handleSubmit(e)` — línea 15.** Previene el envío HTML; retorna si el mensaje recortado está vacío. Activa carga y ejecuta un `insert` en `citizen_feedback` con `{sender_name,sector,contact_email,message,created_at}`. Sustituye nombre/sector vacíos por valores genéricos y correo vacío por `null`. Luego marca éxito y vacía el formulario. En `catch` registra «fallback to local mailto» y también marca éxito, pero **no abre un correo ni implementa otro envío**. `finally` desactiva carga.

No comprueba `{error}` de Supabase, así que una respuesta fallida puede mostrarse como enviada incluso sin excepción. La tabla `citizen_feedback` no se define en el SQL versionado revisado; su existencia en producción no se comprobó. No hay captcha, límite de frecuencia ni historial del envío en este componente. Tras el estado de éxito, el botón de la línea 147 ejecuta `setSubmitted(false)` y permite redactar otro mensaje; no reintenta ni verifica el anterior. Sus enlaces de correo/teléfono/WhatsApp son acciones independientes y sí delegan al navegador o aplicación externa cuando el usuario los pulsa.

## 4. Blog y contenido editorial

El blog no usa Supabase, una API de noticias ni un CMS remoto. Su contenido está escrito en archivos JavaScript del repositorio. Tanto React como el generador estático importan los mismos datos, lo que evita mantener dos versiones del texto pero obliga a compilar/publicar de nuevo para cambiar un artículo.

### 4.1 `src/data/articles.js`: catálogo y utilidades

Fuente: [articles.js](<E:/LUZ BARINAS/admin-web/src/data/articles.js:17>), 92 líneas.

Importa los cinco objetos editoriales de `src/data/articles/`. Exporta `SITE_URL` (23), `BLOG_AUTHOR` (24), `BLOG_META` (26), `EDITORIAL_DISCLAIMER` (32) y `OFFICIAL_SOURCES` (35). Estos valores construyen canónicos, firmas, advertencias y enlaces de consulta. Son constantes de producto, no datos de usuario. `WORDS_PER_MINUTE = 200` es la velocidad convencional para estimar lectura.

**`stripHtml(html)` — línea 42.** Recibe una cadena HTML y devuelve texto simplificado. Sustituye etiquetas por espacios, cambia `&nbsp;`, compacta espacios y recorta. Lo llaman `countWords` y `getTableOfContents`. Es una utilidad de conteo, **no un sanitizador de HTML**: no analiza un DOM, no decodifica todas las entidades y no debe utilizarse como defensa al insertar contenido no confiable.

**`countWords(html)` — línea 49.** Helper interno: llama `stripHtml`, separa por espacios, elimina elementos vacíos y devuelve el número de palabras. `enrich` lo usa al importar el catálogo. No persiste el resultado fuera del objeto exportado.

**`enrich(article,index)` — línea 51.** Recibe un objeto de artículo y su posición. Calcula palabras y devuelve `{id:index+1,...article,wordCount,readTime}`; `readTime` es al menos 1 y usa redondeo hacia arriba de palabras/200. Lo llama `.map(enrich)` en la definición de `articles`, línea 61. El orden actual determina los identificadores y la selección de destacados.

**`getArticleBySlug(slug)` — línea 69.** Devuelve el primer artículo cuyo `slug` coincide exactamente o `null`. Lo usa `BlogPost`. No normaliza mayúsculas, acentos o codificación; el enrutador normaliza a minúsculas cuando interpreta la URL inicial.

**`getRelatedArticles(slug,limit = 3)` — línea 71.** Excluye el artículo actual y devuelve los primeros `limit` del catálogo. Lo usan página y prerender. No calcula similitud por categoría o etiquetas; «relacionados» significa actualmente selección por orden, no recomendación semántica.

**`getTableOfContents(html)` — línea 75.** Aplica una expresión regular global a encabezados con forma exacta `<h2 id="...">...</h2>`. Devuelve `[{id,title}]` y limpia el título mediante `stripHtml`. Lo usa `BlogPost`. No detecta encabezados sin id, comillas simples ni otras formas de atributos; al editar un artículo es necesario mantener ese formato si debe aparecer en el índice.

**`formatDate(iso)` — línea 85.** Recibe una fecha `AAAA-MM-DD`, le añade `T12:00:00` y devuelve fecha extensa de `es-VE`. La hora de mediodía evita algunos cambios de día asociados a interpretar una fecha sin hora como UTC. Lo usan tarjetas, artículos y generador. No valida una fecha arbitraria ni admite de manera general una cadena que ya incluya hora.

### 4.2 Los cinco archivos de artículo: exportaciones por defecto anónimas

Cada archivo empieza en la línea 1 con `export default { ... }`. **No es una función**: es un objeto sin nombre de declaración; los nombres `historiaSenGuri`, `anatomiaPac`, etc., se asignan al importarlo en `articles.js`. Todos contienen `slug`, `title`, `excerpt`, `metaDescription`, `keywords`, `tags`, `category`, `cover`, `date` y `content`. `content` es una cadena de HTML editorial escrita localmente; `wordCount` y `readTime` se añaden después.

| Archivo completo | Tema, clasificación y estructura interna |
|---|---|
| [historia-sen-guri.js](<E:/LUZ BARINAS/admin-web/src/data/articles/historia-sen-guri.js:1>) | `slug='historia-sistema-electrico-nacional-guri'`; categoría Historia y contexto; portada `waves`. Sus h2 cubren primeros pasos (23), interconexión (27), Guri (31), transmisión (36), vulnerabilidades (46) y relación con Barinas (50). |
| [proteccion-electrodomesticos.js](<E:/LUZ BARINAS/admin-web/src/data/articles/proteccion-electrodomesticos.js:1>) | `slug='proteccion-electrodomesticos-barinas'`; categoría Guías prácticas; portada `shield`. Secciones sobre perturbaciones (23), diagnóstico (32), protectores de voltaje (42), DPS (50), monitores de fase (53), tierra (56) y protocolo de cortes (65). |
| [anatomia-pac.js](<E:/LUZ BARINAS/admin-web/src/data/articles/anatomia-pac.js:1>) | `slug='anatomia-plan-administracion-cargas-pac'`; categoría Ingeniería de distribución; portada `grid`. Explica motivo del PAC (24), circuito (27), bloques (31), franjas (41), rotación (45), reconexión (49) y desviaciones (52). |
| [telemetria-ciudadana.js](<E:/LUZ BARINAS/admin-web/src/data/articles/telemetria-ciudadana.js:1>) | `slug='telemetria-ciudadana-modelos-predictivos'`; categoría Datos y tecnología; portada `activity`. Secciones problema (23), concepto (27), transformación a señal (36), modelo (46), limitaciones y ética (54), contribución (63). |
| [conceptos-energia.js](<E:/LUZ BARINAS/admin-web/src/data/articles/conceptos-energia.js:1>) | `slug='conceptos-energia-electrica-domestica'`; categoría Fundamentos; portada `gauge`. Magnitudes (23), sistema residencial (33), protecciones/cables (37), caídas de fase (46), transformadores (54), acciones domésticas (64). |

Todos llevan actualmente fecha editorial de 3 de octubre de 2026 en la línea 19. Sus enlaces internos conectan artículos. El texto explica conceptos y modelos de forma divulgativa: no se debe inferir que cada filtro o método descrito esté implementado en las APIs actuales. Este manual explica cómo se procesa ese contenido; no valida técnicamente cada recomendación eléctrica del artículo.

### 4.3 `BlogPreviewSection({setCurrentRoute})` — `src/components/BlogPreviewSection.jsx:6`

Fuente: [BlogPreviewSection.jsx](<E:/LUZ BARINAS/admin-web/src/components/BlogPreviewSection.jsx:6>), 79 líneas.

Lo monta `LandingPage`. Toma `articles.slice(0,3)` como `featured`; muestra las tres primeras tarjetas con `ArticleCover`, título, resumen, fecha y tiempo estimado. No ordena por fecha ni solicita datos. El botón «Ver todos» abre `/blog`; cada tarjeta abre su slug.

**`navigateTo(route)` — línea 9.** Llama a `setCurrentRoute` y desplaza suavemente al inicio. Lo llaman el botón y las tarjetas. No tiene retorno útil, efectos adicionales ni persistencia. Al añadir un artículo al final de la lista no pasará automáticamente a destacados.

### 4.4 `ArticleCover({cover,category,size = 'card'})` — `src/components/blog/ArticleCover.jsx:13`

Fuente: [ArticleCover.jsx](<E:/LUZ BARINAS/admin-web/src/components/blog/ArticleCover.jsx:13>), 47 líneas.

Devuelve una portada decorativa construida con CSS e iconos Lucide; no descarga una imagen. El diccionario `ICONS` de la línea 4 relaciona `waves`, `shield`, `grid`, `activity` y `gauge` con sus componentes. Una clave desconocida utiliza `BookOpen`. `size==='hero'` cambia altura, radios, tamaño de patrón e icono. `category` agrega la etiqueta si está presente; `cover.gradient` define las clases del fondo o se usa el degradado inicial.

Lo usan previsualización, índice y artículo. No tiene estado ni efectos. `aria-hidden='true'` considera la portada decorativa: título y categoría legibles deben aparecer también en el contenido exterior.

### 4.5 `EditorialDisclaimer({compact = false})` — `src/components/blog/EditorialDisclaimer.jsx:6`

Fuente: [EditorialDisclaimer.jsx](<E:/LUZ BARINAS/admin-web/src/components/blog/EditorialDisclaimer.jsx:6>), 42 líneas.

Devuelve un `aside` con el aviso de independencia y enlaces de `OFFICIAL_SOURCES`, abiertos externamente con `noopener noreferrer nofollow`. `compact` solo cambia espaciado. Índice y artículo lo montan; no consulta fuentes al renderizar ni comprueba que estén disponibles. No tiene estado, red propia o persistencia.

### 4.6 `Blog({setCurrentRoute})` — `src/pages/Blog.jsx:8`

Fuente: [Blog.jsx](<E:/LUZ BARINAS/admin-web/src/pages/Blog.jsx:8>), 147 líneas.

Es el índice de artículos. Mantiene `activeTag`, inicialmente `null`; calcula `allTags` con `Set` sobre todas las etiquetas y filtra `visible` por coincidencia exacta. Muestra tarjetas con portada, fecha, tiempo, categoría, resumen y enlaces. Un botón puede quitar el filtro. No modifica el catálogo ni la dirección al filtrar; al remontarse se pierde la selección.

El efecto de la línea 11 desplaza al inicio. El `useMemo` de la línea 15 crea JSON-LD tipo `Blog`, con el listado de `BlogPosting`. `useDocumentMeta` en la línea 33 instala título, descripción, canónico y JSON-LD. Los datos se calculan desde el catálogo local, no desde una respuesta HTTP.

**`open(e,slug)` — línea 43.** Previene la navegación predeterminada del enlace y llama `setCurrentRoute('/blog/'+slug)`. Lo llaman las tarjetas y enlaces del índice. Retorna sin valor. No conserva por sí mismo los comportamientos especiales de enlaces con teclas modificadoras: al interceptar siempre el clic puede limitar abrir en otra pestaña según el evento.

### 4.7 `BlogPost({slug,setCurrentRoute})` — `src/pages/BlogPost.jsx:15`

Fuente: [BlogPost.jsx](<E:/LUZ BARINAS/admin-web/src/pages/BlogPost.jsx:15>), 213 líneas.

Busca el artículo por slug. Memoriza `toc` (17), `related` (18) y JSON-LD (25). Su efecto (20) desplaza al inicio cada vez que cambia el slug. `useDocumentMeta` (50) establece título, descripción, palabras clave, canónico, tipo `article` y datos `BlogPosting`; si no existe el artículo instala un título de «no encontrado» y devuelve una pantalla con enlace al índice.

Si existe, muestra ruta de navegación, portada hero, título, resumen, autor, fecha, duración y palabras. El cuerpo se inserta en la línea 136 mediante `dangerouslySetInnerHTML={{__html:article.content}}`. Aquí la fuente es HTML local de los autores, no texto ciudadano. Mantener esa frontera es esencial: importar contenido remoto sin sanitizar cambiaría el riesgo de esta operación.

La barra lateral muestra h2 extraídos y relacionados. Un callback anónimo del índice, línea 168, previene el enlace y desplaza suavemente al elemento por id. El contenido de texto de cada artículo y sus ids deben permanecer alineados para que funcione.

**`handleContentClick(e)` — línea 60.** Busca el ancestro enlace del elemento pulsado con `e.target.closest('a')`. Lee `href`; si comienza por `/` y no por `//`, previene el navegador y pasa esa ruta al enrutador. Los enlaces externos y anclas locales quedan a cargo del navegador. No valida que la ruta interna esté implementada, ni separa de forma general query/hash antes de entregarla a `App`.

**`go(e,route)` — línea 70.** Previene el comportamiento del enlace y cambia a la ruta suministrada. Lo usan migas de navegación, relacionados y regreso al blog. No tiene estado propio ni persistencia.

**Límites:** la pantalla React «no encontrado» no determina por sí misma el estado HTTP del servidor. Con los rewrites actuales, un slug sin archivo estático requiere revisar el comportamiento real del alojamiento. El JSON-LD del cliente no contiene la lista de migas que sí genera el prerender. La fecha de modificación se iguala a la publicación porque no existe un campo editorial separado de revisión.

## 5. Páginas legales, transparencia y créditos

Las cinco páginas de este apartado comparten una estructura sencilla. Cada una declara **una sola función de componente**, `({setCurrentRoute})`, devuelve JSX estático y usa un efecto de la línea 5 para desplazar al inicio y fijar su título. El botón de regreso invoca un callback anónimo `setCurrentRoute('/')`. No crean cuentas, formularios, consultas ni cambios de configuración; los enlaces externos se abren cuando el visitante los pulsa. No usan `useDocumentMeta`, no restauran el título y no tienen estado propio.

### 5.1 `PrivacyPolicy({setCurrentRoute})` — `src/pages/PrivacyPolicy.jsx:4`

Fuente: [PrivacyPolicy.jsx](<E:/LUZ BARINAS/admin-web/src/pages/PrivacyPolicy.jsx:4>), 145 líneas.

Muestra identidad independiente, acceso sin cuentas, datos y reportes, almacenamiento local, terceros que prestan servicios, cambios y canales de contacto. El título es `Política de Privacidad — PAC Barinas`. La página describe tanto sitio como aplicación Android, aunque sus mecanismos concretos sean diferentes: Room/SharedPreferences pertenecen a Android; el sitio usa también `sessionStorage`, `localStorage` y el formulario de contacto.

**Mantenimiento:** reconciliar el texto con lo que efectivamente se recopila. Decir que no se exige correo para consultar horarios puede ser correcto y coexistir con el correo voluntario de `ContactSection`; afirmar de manera general que nunca se procesa ese dato sería otra cosa. Esta página no impone políticas de retención ni permisos de la base, solo las comunica.

### 5.2 `TermsConditions({setCurrentRoute})` — `src/pages/TermsConditions.jsx:4`

Fuente: [TermsConditions.jsx](<E:/LUZ BARINAS/admin-web/src/pages/TermsConditions.jsx:4>), 134 líneas.

Muestra términos de independencia del proyecto, carácter referencial de los horarios, condiciones de reportes comunitarios, uso responsable, límites de responsabilidad, aportes voluntarios y contacto. El título se establece a `Términos y Condiciones — PAC Barinas`. Explica que no hay garantía de cumplimiento matemático del cronograma.

No solicita una aceptación ni guarda versión/fecha aceptada. Las facultades de moderación que describe no equivalen a validaciones aplicadas automáticamente por el código. Si cambia el producto debe revisarse la correspondencia del texto, especialmente donde habla de validación geográfica o eliminación de reportes.

### 5.3 `RefundPolicy({setCurrentRoute})` — `src/pages/RefundPolicy.jsx:4`

Fuente: [RefundPolicy.jsx](<E:/LUZ BARINAS/admin-web/src/pages/RefundPolicy.jsx:4>), 79 líneas.

Devuelve la política de gratuidad, naturaleza voluntaria de las donaciones, condiciones y canales para consultar reembolsos. El efecto fija `Política de Reembolsos — PAC Barinas`. No procesa una devolución ni se integra con PayPal, Binance o entidades bancarias. Los datos de contacto son texto/enlaces; una modificación de esta página no modifica destinos de donación de `DonationsSection`.

### 5.4 `Transparencia({setCurrentRoute})` — `src/pages/Transparencia.jsx:4`

Fuente: [Transparencia.jsx](<E:/LUZ BARINAS/admin-web/src/pages/Transparencia.jsx:4>), 84 líneas.

Muestra proveedores —Supabase, GitHub, Vercel y publicidad— y clasifica preferencias locales, reportes y datos publicitarios. Contiene enlaces a dominio principal y espejo; no verifica su configuración. El efecto fija `Transparencia y Gestión de Datos — PAC Barinas`.

Hay afirmaciones que requieren mantenimiento frente al código vigente: habla de descarga OTA y de no recopilar ubicaciones GPS exactas. El actual actualizador Android abre Google Play; los modelos de solicitudes y sectores sí admiten coordenadas. La explicación pública debe precisar cuándo una coordenada corresponde a un sector propuesto y cuándo sería ubicación personal, y describir los flujos realmente vigentes. El componente no consulta ni audita esos datos.

### 5.5 `DeveloperInfo({setCurrentRoute})` — `src/pages/DeveloperInfo.jsx:4`

Fuente: [DeveloperInfo.jsx](<E:/LUZ BARINAS/admin-web/src/pages/DeveloperInfo.jsx:4>), 163 líneas.

Muestra equipo/desarrollador, reconocimiento a ElMatrushka, explicación del proyecto, enlaces al sitio y al repositorio Android, contacto y deslinde legal. Utiliza un recurso gráfico público para el reconocimiento y enlaces externos de redes/correo. El título es `Equipo, Desarrollador y Créditos — PAC Barinas`. No obtiene perfiles, repositorios ni estadísticas en tiempo real. Un enlace o fotografía visible no acredita técnicamente mantenimiento activo del servicio.

## 6. Panel administrativo React

### 6.1 Componente, sesión y pantalla

Fuente: [AdminPortal.jsx](<E:/LUZ BARINAS/admin-web/src/pages/AdminPortal.jsx:13>), 897 líneas.

**`AdminPortal({setCurrentRoute})` — línea 13.** Devuelve una pantalla de acceso o el panel con seis pestañas: Matriz PAC, Bloques y Sectores, Feed de Reportes, Cuentas de Donación, Transmisión Push y Diagnóstico. No es equivalente funcional al panel clásico de `admin.html`. Lo monta `App` después de una navegación interna a `/admin`.

| Estado | Uso y almacenamiento |
|---|---|
| `isAuthenticated`, `passwordInput`, `loginError` | Acceso visual, texto introducido y error. La sesión visual se recupera de `localStorage`. |
| `activeTab` | Pestaña visible; inicia en `pac`, sin persistencia. |
| `pacMatrix`, `pacSlots` | Matriz y franjas cargadas desde `app_config` o valores iniciales. |
| `selectedDayIdx` | Se declara, pero no interviene en la edición visible actual. |
| `isSavingPac`, `savePacSuccess` | Bloqueo del guardado e indicador temporal de éxito. |
| `sectorsByBlock`, `selectedBlock`, `newSectorInput` | Catálogo editable únicamente en memoria en esta implementación. |
| `sectorSearchQuery` | Se declara, pero no filtra la lista actual. |
| `communitySectors` | Se intenta cargar, pero no hay una pestaña de aprobación que lo utilice. |
| `citizenReports`, `reportsFilter` | Lista de reportes y filtro de visualización. |
| `donationsForm`, `isSavingDonations`, `saveDonationsSuccess` | Configuración de cuentas y estado de guardado. |
| `broadcastData`, `isBroadcasting`, `broadcastResult` | Título, mensaje, supuesto destino y resultado de la API de notificaciones. |
| `diagnosticLog` | Entradas de diagnóstico conservadas durante el montaje. |

El efecto de la línea 63 lee `localStorage['luz_admin_session']`; si tiene el valor esperado pone `isAuthenticated=true`. El efecto de la línea 71 no actúa antes de eso; cuando hay acceso visual llama `fetchAdminData`. Esta condición no aporta un token al cliente Supabase ni a las APIs. La colección de contraseñas aceptadas se encuentra en el propio JavaScript; sus valores no se reproducen en este manual.

### 6.2 Carga inicial: `fetchAdminData()` — línea 74

Es una función asíncrona interna del efecto de acceso. No recibe parámetros, no devuelve datos al llamador y actualiza estados del componente. Realiza estas cuatro cargas secuenciales con bloques `try/catch` separados:

1. Lee `app_config/pac_schedule`; extrae `matrixRows[].cells` y `slots` si existen.
2. Lee `app_config/donations_config`; reconstruye el formulario con cadenas vacías como alternativa.
3. Lee hasta cien `citizen_reports`, ordenados por `created_at`.
4. Lee `community_sectors`, también ordenados por `created_at`.

No extrae ni comprueba el `error` de Supabase. Los `catch` se activan ante excepciones, no ante todas las respuestas fallidas. El SQL local define `community_locations`, no `community_sectors`, y usa `timestamp` en reportes. Por eso esta pantalla puede quedar vacía aun cuando existan datos en el modelo que usa Android. El encabezado «Supabase Sincronizado» es texto fijo, no el resultado de validar las cuatro consultas. Tampoco hay suscripciones Realtime para mantener estas listas actualizadas mientras el panel permanece abierto.

### 6.3 Acceso: `handleLogin(e)` y `handleLogout()`

**`handleLogin(e)` — línea 140.** Lo llama `onSubmit` del formulario. Previene el envío, recorta y convierte a minúsculas la entrada y la compara con `VALID_PASSWORDS`. Si coincide escribe el marcador de sesión en `localStorage`, abre la interfaz y limpia el error; si no, muestra una frase de acceso denegado. No llama al servidor, no tiene expiración, límite de intentos ni identidad de administrador. Su retorno no se usa.

**`handleLogout()` — línea 152.** Borra el marcador local, cierra la vista y navega a `/`. Lo llama el botón Cerrar Sesión. No revoca un token porque aquí no se crea ninguno; tampoco comunica un cierre a otras pestañas mediante un listener `storage`.

### 6.4 Matriz: `cycleCell(slotIdx,dayIdx)` y `handleSavePac()`

**`cycleCell(slotIdx,dayIdx)` — línea 159.** Recibe índices de fila y columna. Obtiene la celda, calcula el siguiente bloque en A→B→C→D→A, copia filas para conservar actualización inmutable y aplica `setPacMatrix`. Los botones de la tabla, línea 513, la invocan. No escribe a Supabase. Un valor desconocido pasa a A por el cálculo de índice. No valida índices fuera de rango.

**`handleSavePac()` — línea 175.** Botón Guardar y Publicar. Activa `isSavingPac`, convierte cada fila en `{cells:row}` y forma `{updatedAt:fechaISO,version:Date.now(),slots:pacSlots,matrixRows}`. Hace `upsert` de `app_config` con `config_key='pac_schedule'`. Comprueba `error`, muestra éxito durante tres segundos o una alerta con el error; `finally` libera el botón. Devuelve una promesa de finalización.

El `upsert` **reemplaza `config_value` completo** con ese objeto. No fusiona las propiedades del JSON remoto. Las asignaciones `sectorsA-D` que guarda el panel clásico quedan fuera de este payload y pueden desaparecer. Tampoco conserva campos adicionales que futuras versiones añadan. `updatedAt` es una cadena ISO aquí, mientras el panel clásico utiliza milisegundos; el contrato debería ser único. Un `version` basado en el reloj del equipo no evita que dos administradores se sobrescriban mutuamente.

El botón Restablecer de la línea 544 solo ejecuta `setPacMatrix(DEFAULT_CORPOELEC_MATRIX)`; no guarda hasta pulsar publicación. Las franjas se muestran, pero esta pantalla no contiene un editor completo de franjas comparable al clásico. Las frases de «publicado en todos los teléfonos» no esperan confirmaciones individuales de los dispositivos.

### 6.5 Donaciones: `handleSaveDonations(e)` — línea 201

Lo llama el formulario de la pestaña. Previene el envío, activa carga y guarda `{config_key:'donations_config',config_value:donationsForm}` en `app_config`. Comprueba `error`, muestra éxito tres segundos y libera carga incluso ante fallo. El formulario contiene `paypal`, `binance`, `pmBank`, `pmPhone`, `pmId`; los `onChange` copian el objeto con la propiedad editada.

La escritura reemplaza ese JSON completo, sin historial ni validación de titularidad, formato bancario o host de la URL. `DonationsSection` lee la misma clave; Android también puede consumirla. El cambio requiere una autorización real del lado de datos. Los valores introducidos no se registran localmente fuera del estado ni se reproducen en este manual.

### 6.6 Sectores: `handleAddSector()` y `handleRemoveSector(blockKey,sectorName)`

**`handleAddSector()` — línea 222.** Ignora la entrada vacía, agrega el nombre recortado al principio del array de `selectedBlock` mediante una copia del objeto y vacía el campo. Lo llama Agregar. No evita duplicados, no crea un registro `sectors`, no guarda configuración y no procesa geografía.

**`handleRemoveSector(blockKey,sectorName)` — línea 232.** Filtra del array los nombres idénticos al indicado y aplica el nuevo estado. Lo llaman los botones de papelera. Quita todas las coincidencias exactas de ese nombre y no pide una operación al servidor.

La pantalla describe asignación de circuitos/comunidades, pero estos dos handlers solo cambian la lista en memoria. No existe una operación de publicación de esa lista ni se hidrata desde `pac_schedule.sectorsA-D`. Al recargar vuelve al catálogo inicial. Los cambios no afectan al `sectors.rotationBlock` remoto.

### 6.7 Reportes: filtros y campos visibles

El render de la línea 664 filtra por `reportsFilter` y examina `report_type`, `status`, `has_power`, `sector_name`, `sector`, `comment` y `created_at`. El modelo remoto disponible en el SQL y usado por `useTelemetry` utiliza `reportType`, `hasPower`, `sectorName`, `observation` y `timestamp`. Esto genera una segunda incompatibilidad además de la consulta inicial.

Las ramas de color y estado se resuelven en callbacks anónimos del `filter/map`; no llaman a una función de normalización común. En ausencia de campos esperados, puede mostrar un nombre genérico, tiempo «Reciente» o clasificar como sin luz. Los botones Todos, Con Luz, Sin Luz y Falla Irregular solo cambian `reportsFilter` y no consultan más datos. No hay moderación, eliminación ni exportación implementadas en esta pestaña React.

### 6.8 Push: `handleSendBroadcast(e)` — línea 240

Previene el formulario, activa `isBroadcasting`, limpia el resultado y hace POST a `/api/sendPushAlert` con `Content-Type: application/json` y el objeto `broadcastData`. Lee JSON; si `res.ok` presenta éxito con la respuesta, si no presenta estado/error, y si falla la comunicación presenta error de conexión. `finally` libera el formulario.

El selector ofrece `all_users`, `block_a`, `block_b`, `block_c`, `block_d`, pero la API actual **ignora `topic` y envía siempre a `barinas_global`**. Por tanto, seleccionar un solo bloque no limita realmente la audiencia. No transmite `is_alarm`; la función usará su valor por defecto. No añade `Authorization`, y la API tampoco lo exige en su implementación actual. Este flujo no escribe el aviso persistente `broadcast_notice` que maneja el panel clásico; enviar una push y publicar un aviso de configuración son operaciones distintas.

### 6.9 Diagnóstico: `pingApi(endpoint)` — línea 264

Recibe la dirección de una API, mide `performance.now()`, hace un GET, interpreta JSON y agrega al inicio de `diagnosticLog` una entrada con hora, latencia y primeros 80 caracteres del resultado. Si ocurre una excepción agrega una línea ERROR. Los botones de las líneas 865 y 871 la llaman con una predicción de `sectorId=test` y la proyección de octubre.

No comprueba `res.ok`; una respuesta JSON 404 puede terminar etiquetada como `OK`. Por eso la existencia de una línea de consola no acredita salud del servicio ni que el sector de prueba exista. No envía notificaciones ni prueba Firebase, no almacena el log en la nube y no lo limita en longitud durante una sesión extensa.

### 6.10 Colores: `getBadgeClass(b)` — línea 345

Helper interno que devuelve clases Tailwind para los bloques A/B/C/D o gris. Lo usa la tabla de matriz y se parece a `getBlockBadgeClass` del contador, pero es otra implementación. No hace red ni modifica estado. La duplicación obliga a revisar ambos si cambia la convención cromática.

## 7. Modo TV y sus seis temas

### 7.1 Contenedor `TvKioskMode({setCurrentRoute})` — `src/pages/TvKioskMode.jsx:96`

Fuente: [TvKioskMode.jsx](<E:/LUZ BARINAS/admin-web/src/pages/TvKioskMode.jsx:96>), 686 líneas.

Lo monta `App` para `/tv` y el alias inicial `/kiosk`. Ocupa el viewport, añade una barra de controles y reloj y elige una de seis vistas. Consume una instancia de `useTelemetry`; los temas reciben sus datos y no hacen consultas propias. `refresh` e `isLoading` se desestructuran pero los controles actuales no ofrecen un botón de actualización que los utilice.

`KIOSK_THEMES`, línea 33, es un catálogo de seis objetos `{id,num,name,badge,description,icon,accentColor,badgeBg}`: `radar`, `light`, `performance`, `web`, `app`, `mercury`. Describe la selección; las marcas como «60 FPS» son texto, no resultados de un monitor de rendimiento.

| Estado/referencia | Origen y efecto |
|---|---|
| `currentTheme` | Lee `localStorage['pac_kiosk_theme']` o usa `radar`. |
| `showThemeModal` | Abre/cierra la selección de tema, sin persistencia. |
| `isFullscreen` | Inicializa desde `document.fullscreenElement` y se sincroniza con eventos. |
| `myBlock` | Lee `localStorage['pac_kiosk_block']` o usa `ALL`. |
| `wakeLockRef` | Guarda el permiso temporal de mantener pantalla encendida. |
| `clockData` | Hora/fecha civil de Caracas; cambia cada segundo. |

Las lecturas y escrituras de almacenamiento se envuelven en `try/catch` para permitir el uso cuando el navegador las deniega. No se valida que un tema guardado todavía esté en `KIOSK_THEMES`: el metadato puede caer al primero, pero el render condicional no muestra un tema cuando `currentTheme` tiene un id desconocido. Un bloque desconocido también puede producir una descripción engañosa de servicio.

### 7.2 Preferencias y pantalla completa

**`handleSelectTheme(themeId)` — línea 119.** Recibe id; cambia `currentTheme`, cierra modal e intenta persistir `pac_kiosk_theme`. Lo llaman tarjetas, atajos numéricos y ciclo de temas. No devuelve un resultado ni verifica compatibilidad del dispositivo.

**`handleSelectBlock(blk)` — línea 141.** Cambia `myBlock` y guarda `pac_kiosk_block`. Lo llaman los botones Todos/A/B/C/D y el atajo B. No modifica preferencias de Android ni registros de Supabase; es una personalización de esta instalación del navegador.

**`toggleFullscreen()` — línea 151.** Si no hay elemento en pantalla completa selecciona `requestFullscreen` o una variante con prefijo y la invoca en `document.documentElement`. Si ya lo hay selecciona la operación de salida. Registra rechazos de promesa. No cambia `isFullscreen` directamente: espera el evento. Lo llaman botón y tecla F. Algunas APIs antiguas con prefijo no devuelven una promesa; el `.catch` sin comprobar ese retorno es una limitación de compatibilidad.

**`handleFsChange()` — línea 167.** Callback del efecto de la línea 166. Actualiza `isFullscreen` según `document.fullscreenElement`. Se registra para `fullscreenchange` y `webkitfullscreenchange` y se elimina al desmontar. La consulta de estado no incluye la propiedad antigua `webkitFullscreenElement`.

### 7.3 Mantener la pantalla encendida

**`requestWakeLock()` — línea 181.** Función asíncrona interna del efecto de la línea 180. Si existe `navigator.wakeLock`, solicita `'screen'` y guarda el sentinel en `wakeLockRef.current`; si falla solo registra aviso. No garantiza que el sistema operativo no bloquee la pantalla.

**`handleVisibility()` — línea 192.** Si el documento vuelve a estar visible y la referencia está vacía, llama `requestWakeLock`. El efecto registra el evento y, al desmontarse, quita el listener, libera el sentinel si existe y vacía la referencia.

**Límite:** no escucha el evento `release` para vaciar la referencia cuando el navegador libera el permiso automáticamente. Una referencia a un sentinel ya liberado puede impedir volver a solicitarlo. La disponibilidad depende del navegador y contexto; no existe un servicio nativo de Android en esta vista web.

### 7.4 Teclado y reloj

**`handleKeyDown(e)` — línea 210.** Listener de teclado del efecto de la línea 209. Ignora eventos procedentes de `INPUT`/`TEXTAREA`; convierte la tecla a minúsculas y aplica:

| Tecla | Operación |
|---|---|
| 1–6 | Selecciona directamente el tema de esa posición. |
| T o F2 | Busca el tema actual y selecciona el siguiente, con vuelta al inicio. |
| B | Cicla `ALL,A,B,C,D`. |
| F | Alterna pantalla completa. |
| Escape | Cierra el modal de temas si estaba abierto. |

El efecto reestablece el listener cuando cambian tema, bloque o modal y lo elimina al salir. No llama `preventDefault` para las teclas especiales. El texto del botón de regreso sugiere Esc, pero **Escape no navega al inicio** en este handler. El botón de regreso sí llama `setCurrentRoute('/')`. Tampoco gestiona foco con un patrón de mando de TV más allá de los botones HTML normales.

**`getCaracasClock()` — línea 265.** Función interna sin parámetros que obtiene hora de Caracas con `toLocaleString`, construye formato 12 h con segundos y AM/PM y una fecha larga `es-VE`. Devuelve `{timeStr,ampm,dateStr,hours24}`. Se usa para inicializar `clockData` y por el intervalo de la línea 260, que corre cada segundo y se elimina al desmontar. No consulta un servidor horario ni corrige un reloj incorrecto del equipo.

### 7.5 Estados derivados que reciben los temas

**`businessStatus`, `useMemo` — línea 288.** Devuelve `{type,title,badge,stateColor,subtitle}` según `myBlock`, bloque activo y siguiente:

| Condición | `type` y significado de la pantalla |
|---|---|
| `myBlock==='ALL'` | `GENERAL`; anuncia el bloque programado actual. |
| Bloque local igual al activo | `OUTAGE`; anuncia corte en el bloque local. |
| Bloque local igual al siguiente | `WARNING`; anuncia próximo turno. |
| Otro caso | `NORMAL`; anuncia servicio estable en el bloque local. |

Este estado se deduce del cronograma, no de un consenso específico de reportes del negocio. El texto de servicio normal no prueba suministro físico. Para mantenimiento conviene mantener separado «fuera del turno programado» de «energía confirmada».

**`tickerReports`, `useMemo` — línea 326.** Devuelve `[]` sin reportes o el array duplicado `[...reports,...reports]`. La duplicación es visual para el marquee y no duplica registros de base de datos.

**`currentThemeMeta`, `useMemo` — línea 332.** Busca el id del tema en el catálogo o retorna el primer objeto. Se usa en la barra y modal. No corrige el estado guardado si no lo encuentra.

`isLightTheme`, `isPerformanceTheme` e `isMercuryTheme` son booleanos derivados para estilizar la barra. No son funciones ni preferencias distintas. Los seis módulos de vista se importan de forma estática dentro del módulo TV: cambiar de tema no descarga un módulo nuevo mediante `lazy`.

### 7.6 Contrato común de los temas

Todos reciben las mismas props: `{clockData,pacTime,circuits,totalReportsCount,slots,matrix,myBlock,businessStatus,tickerReports,getBlockBadgeClass}` y devuelven JSX. Los llama exclusivamente el render condicional de `TvKioskMode` —líneas 593–680— según el tema. Algunos parámetros se reciben por uniformidad aunque una vista no los use. Ningún tema declara estado, efecto, almacenamiento ni peticiones de red; son vistas de datos suministrados.

Las seis dibujan alguna combinación de estado local, bloques actual/siguiente, cuenta regresiva, resumen de circuitos, franjas del día y banda de reportes. Sus callbacks de `map` calculan filas/tarjetas; no hacen escrituras. Las franjas recalculan `weekShift` usando **`new Date().getDate()` del navegador**, mientras `dayIndex` procede de Caracas. En equipos fuera de esa zona puede mezclar fechas distintas cerca de medianoche o cambio de semana/mes. La lógica repetida debe revisarse en las seis si cambia la regla PAC.

La cuenta regresiva común expresa final de la franja activa. Un titular que la presente como «tiempo para tu corte» sin cambiar el cálculo puede inducir a error. El ticker suele evaluar falta de energía y `SIN_LUZ`, y algunas vistas también `IRREGULAR`; no existe normalización compartida de los tipos del servidor.

### 7.7 `RadarTheme(...)` — `src/pages/tv/RadarTheme.jsx:11`

Fuente: [RadarTheme.jsx](<E:/LUZ BARINAS/admin-web/src/pages/tv/RadarTheme.jsx:11>), 287 líneas.

Tema oscuro azul con resplandores, tarjetas translúcidas y barrido de radar CSS. La columna izquierda muestra estado seleccionado, franja, bloque actual/siguiente y cifras grandes de cuenta regresiva. La derecha muestra los **primeros siete circuitos** (`slice` en 176), estado/estabilidad y la tira de franjas (`map` en 217). Abajo recorre `tickerReports` (259), con clasificación local de corte/fluctuación.

No existe un radar geográfico ni medición de posiciones: los círculos y barrido son decorativos. El orden de circuitos proviene de la ordenación por reportes del hook, no de distancia ni severidad. El contenido interno permite desplazamiento vertical en la lista; el marco global TV oculta desbordamiento. El coste de blur y animaciones depende del equipo.

### 7.8 `LightTheme(...)` — `src/pages/tv/LightTheme.jsx:12`

Fuente: [LightTheme.jsx](<E:/LUZ BARINAS/admin-web/src/pages/tv/LightTheme.jsx:12>), 289 líneas.

Presenta la misma organización funcional con fondo claro, contraste oscuro, halos y radar suave. Los primeros siete circuitos se mapean en 178; el cronograma en 219; ticker en 261. Colores del estado se deducen de `businessStatus.stateColor`. La diferencia respecto a Radar es de presentación, no una fuente de datos ni algoritmo alternativo.

Los filtros de bloque, pantalla completa y cambio de tema siguen en el padre. No hay controles propios del cuerpo. El nombre «alto contraste» no implica una auditoría automática de accesibilidad o legibilidad para cada pantalla.

### 7.9 `PerformanceTheme(...)` — `src/pages/tv/PerformanceTheme.jsx:12`

Fuente: [PerformanceTheme.jsx](<E:/LUZ BARINAS/admin-web/src/pages/tv/PerformanceTheme.jsx:12>), 264 líneas.

Tema oscuro de superficies planas, bordes fuertes y números monoespaciados; reduce desenfoques y adornos de las otras consolas. Muestra los siete primeros circuitos en 160, cronograma en 198 y ticker en 239. Se apoya en los mismos estados y mismos intervalos del padre/hook. La etiqueta `60 FPS LITE` es fija; el archivo no usa un contador de cuadros ni ajusta automáticamente el ritmo del polling a la capacidad de la TV.

No incorpora un motor distinto ni convierte el sitio en una aplicación nativa de Tizen/webOS. Las limitaciones de fullscreen, Wake Lock y teclado siguen dependiendo del navegador.

### 7.10 `WebTheme(...)` — `src/pages/tv/WebTheme.jsx:14`

Fuente: [WebTheme.jsx](<E:/LUZ BARINAS/admin-web/src/pages/tv/WebTheme.jsx:14>), 347 líneas.

Vista de documento/tarjetas claras con barra lateral decorativa y propiedades del sistema: bloque en corte, franja, relevo, zona local y volumen de reportes. El área principal muestra encabezado, aviso de estado/cuenta regresiva, tabla de franjas y galería con **cinco circuitos** (`slice` en 276). Cronograma en 224, ticker en 320.

La barra lateral se oculta en pantallas pequeñas y sus rótulos son presentación, sin navegación de documento propia. El título `Plan de Carga 2026` está fijado en código. La tabla añade `isPast = idx < activeSlotIdx` para marcar turnos anteriores; no reconstruye eventos reales de corte. No usa Notion ni guarda información en una cuenta de ese servicio.

### 7.11 `AppTheme(...)` — `src/pages/tv/AppTheme.jsx:16`

Fuente: [AppTheme.jsx](<E:/LUZ BARINAS/admin-web/src/pages/tv/AppTheme.jsx:16>), 426 líneas.

Imita una interfaz Android Material 3: rail lateral decorativo, barra de estado, tarjetas curvas, bloque actual, anillo de progreso, siguiente bloque, tira de franjas y lista de **cinco circuitos** (`slice` en 323). Cronograma en 274 y ticker en 395. Los iconos Monitor, Sectores y Alarmas del rail no implementan navegación. Los rótulos `Room SQLite Conectado`, `Sincronización Room DB (Offline)`, `5G` y `98%` son estáticos; este componente web **no abre Room ni mide batería/red móvil**.

**`getAppBlockColor(blk)` — línea 31.** Helper local. Convierte el bloque a mayúsculas y devuelve `{hex,bg,text,border,softBg,softBorder}`. Utiliza naranja para A, azul para B, amarillo para C, verde para D y azul genérico por defecto. Lo llaman la selección del tema del bloque activo/siguiente, franjas y circuitos. Su paleta difiere de la web común, por intención de esta vista; no modifica la asignación de bloques.

**Anillo — líneas 45–53.** Convierte la cuenta regresiva a segundos, asume `totalSecondsInSlot = 4*3600`, calcula segundos transcurridos y porcentaje limitado a 0–100. Obtiene circunferencia de un SVG de radio 38 y desplazamiento de trazo. No hay una función nombrada para esto. Si el administrador cambia la duración de las franjas, el anillo sigue suponiendo cuatro horas.

### 7.12 `MercuryTheme(...)` — `src/pages/tv/MercuryTheme.jsx:11`

Fuente: [MercuryTheme.jsx](<E:/LUZ BARINAS/admin-web/src/pages/tv/MercuryTheme.jsx:11>), 327 líneas.

Vista oscura de tarjetas grafito, acento cobalto y montañas hechas con SVG local. Tiene subbarra de identidad/estado, tarjeta principal con comparación de bloques y cuenta regresiva, **seis circuitos** (`slice` en 219), cronograma en 256 y ticker en 301. Las montañas no descargan una fotografía ni representan ubicación geográfica de un reporte. La clasificación visual enfatiza corte activo; la banda utiliza el estado de energía de cada reporte.

El texto del cuerpo menciona cronograma «oficial» de cuatro horas, aunque el resto del sistema insiste en independencia y carácter referencial. Ese texto no convierte la matriz en fuente oficial. El tema no valida duración, disponibilidad de datos ni dominio; recibe lo mismo que los otros cinco.

## 8. APIs serverless

Los tres módulos de `admin-web/api` usan CommonJS: la función exportada se asigna a **`module.exports = async (req,res) => ...`**. Son handlers sin nombre propio de declaración; el inventario AST puede denominarlos `exports` por el destino de la asignación. Los ejecuta el alojamiento al recibir una solicitud HTTP; importarlos no equivale a tener un servidor local escuchando.

`req.query` aporta parámetros de URL y `req.body` el cuerpo interpretado por el alojamiento. `res.setHeader`, `res.status(...).json(...)` y `res.end()` producen la respuesta. El valor de retorno de los métodos de `res` no es un contrato de negocio para otro módulo JavaScript: el contrato externo es el estado, cabeceras y JSON HTTP.

### 8.1 Reglas comunes actuales

Cada handler permite `OPTIONS` con estado 200 sin ejecutar su operación. Configura `Access-Control-Allow-Origin: *`, credenciales habilitadas, cabeceras arbitrarias y una lista amplia de métodos. **No restringe realmente los métodos recibidos** mediante una rama 405: la lista de CORS solo describe preferencias al navegador, no impide llamadas de otros clientes.

La combinación origen comodín y credenciales no constituye una configuración válida para solicitudes CORS con credenciales de navegador. Esto no protege los endpoints: una llamada sin credenciales puede seguir siendo válida y un cliente HTTP puede ignorar por completo las reglas de CORS. Ninguno de estos archivos comprueba un administrador autenticado o cuota por usuario.

Las APIs de lectura inicializan su propio cliente Supabase a nivel de módulo, separado del cliente de `src/lib/supabase.js`. Aunque contengan la misma dirección/clave pública, son instancias de ejecución distintas: servidor y navegador no comparten memoria ni sesión. Los valores de conexión no se reproducen en este manual.

### 8.2 `api/getMonthlyWeeks.js`

Fuente: [getMonthlyWeeks.js](<E:/LUZ BARINAS/admin-web/api/getMonthlyWeeks.js:7>), 92 líneas.

**Handler `module.exports(req,res)` — línea 7.** Recibe `monthName` y `totalDays` desde query o cuerpo, dando prioridad a un valor verdadero de query. Devuelve HTTP 400 si falta alguno, lee `app_config/pac_schedule` y devuelve 400 si falla o falta la configuración. Extrae `matrixRows` o `[]`, obtiene `activeMatrix = matrixRows.map(r=>r.cells||[])` y genera cuatro entradas:

| Semana | Etiqueta | Desplazamiento de fila |
|---|---|---|
| 1 | DEL 01 AL 07 DE MES | 0; usa la matriz tal cual. |
| 2 | DEL 08 AL 14 DE MES | `(r+1)%rows`. |
| 3 | DEL 15 AL 21 DE MES | `(r+2)%rows`. |
| 4 | DEL 22 AL totalDays DE MES | `(r+3)%rows`; abarca también días 29–31. |

`rows = max(1,matrixRows.length)`. La salida exitosa es `{weeks:[{weekNumber,dateRangeLabel,matrix},...]}`. No devuelve las franjas `slots`, sector ni feriados. No toma un mes/año real para calcular longitud del mes: acepta los valores indicados y transforma `monthName.toUpperCase()` para las etiquetas.

**Caché:** `Cache-Control: public, s-maxage=3600, stale-while-revalidate=86400`. Un intermediario puede conservar hasta una hora la respuesta y servir una copia antigua durante revalidación. Una publicación PAC no invalida explícitamente esas respuestas.

**Errores y límites:** falta validar tipos, rango 28–31, longitud de textos y forma de la matriz. Un `monthName` que no sea cadena puede provocar 500; una matriz vacía puede terminar en filas indefinidas/nulas al serializar las semanas rotadas. El `catch` registra el error y responde 500 con `error.message`, exponiendo detalles internos. La API no identifica una «semana cinco»: la cuarta incluye todo el final de mes por diseño actual.

**Quién la llama:** botones de diagnóstico de los paneles. El sitio público calcula las franjas mediante `useTelemetry`; no usa obligatoriamente este endpoint. El hecho de que el API exista no asegura que todas las pantallas usen su regla.

Ejemplo de solicitud, sin ejecutarla:

```http
GET /api/getMonthlyWeeks?monthName=octubre&totalDays=31
```

Para ilustrar la estructura, supóngase una configuración de prueba con una sola fila de siete celdas; con una fila todas las rotaciones son iguales:

```json
{
  "weeks": [
    {"weekNumber": 1, "dateRangeLabel": "DEL 01 AL 07 DE OCTUBRE", "matrix": [["A","B","C","D","A","B","C"]]},
    {"weekNumber": 2, "dateRangeLabel": "DEL 08 AL 14 DE OCTUBRE", "matrix": [["A","B","C","D","A","B","C"]]},
    {"weekNumber": 3, "dateRangeLabel": "DEL 15 AL 21 DE OCTUBRE", "matrix": [["A","B","C","D","A","B","C"]]},
    {"weekNumber": 4, "dateRangeLabel": "DEL 22 AL 31 DE OCTUBRE", "matrix": [["A","B","C","D","A","B","C"]]}
  ]
}
```

Ejemplo 400 por parámetros faltantes: `{"error":"monthName and totalDays are required"}`. La respuesta real depende de la configuración remota, que no se consultó para este capítulo.

### 8.3 `api/getOutagePrediction.js`

Fuente: [getOutagePrediction.js](<E:/LUZ BARINAS/admin-web/api/getOutagePrediction.js:17>), 182 líneas.

**`getDayIndex(dayOfWeek)` — línea 17.** Recibe el número de `Date.getDay()` y devuelve domingo→6 o resto→n−1. Es un helper interno puro para indexar las columnas lunes–domingo de `DAYS`. Lo llama la búsqueda de ventanas.

**Handler `module.exports(req,res)` — línea 21.** Recibe `sectorId` por query o cuerpo. Los pasos y sus efectos son:

1. Si falta el id responde 400. Busca una fila en `sectors` por `id`; ante error o falta de fila responde 404. No distingue error de servidor de sector inexistente en esa rama.
2. Toma `sector.rotationBlock || 'A'`, lo pasa a mayúsculas y quita `BLOQUE`. No valida que el valor sea una cadena antes de transformarlo.
3. Consulta hasta diez reportes de ese sector con `hasPower=false`, ordenados por `timestamp` descendente. No filtra antigüedad ni verifica el origen de esos reportes. Se obtiene `historyError`, pero no se usa para decidir un error externo.
4. Fija `calculatedDuration=4.0` y lee `app_config/pac_schedule`.
5. Si existe configuración y bloque A/B/C/D, recorre `dayOffset=0..14`: **quince fechas contando hoy**, no catorce fechas exclusivas. Para cada fecha calcula grupo semanal y, por cada franja en el orden guardado, obtiene el bloque rotado.
6. Cuando coincide el bloque, crea inicio y fin con `setHours`; si fin es menor que inicio suma un día. Si ahora está dentro devuelve una ventana activa; si el inicio es futuro toma esa primera ventana y termina. No ordena previamente las franjas por hora.
7. Si halló una ventana, devuelve sus timestamps y `hoursUntilWindow`. Elige confianza 94 si hay al menos dos reportes en `historyData`, o 88 en otro caso. No estima esa confianza por exactitud histórica de predicciones.
8. Si no halló una ventana, usa un cálculo modular por día del año, bloque y desplazamiento 0/6/12/18; devuelve una ventana sintética de cuatro horas con confianza fija 78.

**Contrato exitoso:** `{sectorId,sectorName,rotationBlock,nextEstimatedStartMillis,nextEstimatedEndMillis,estimatedDurationHours,confidencePercentage,algorithmDetail,hoursUntilWindow}`. Aunque internamente `pacWindow` tenga `isCurrentlyActive`, **la respuesta no devuelve ese booleano**. El nombre de campos `nextStartMillis`/`nextEndMillis` de documentación anterior no coincide con los nombres actuales.

**Caché:** `public, s-maxage=300, stale-while-revalidate=600`. Una respuesta de una ventana activa o tiempo restante puede quedar desactualizada por caché además de por la propia frecuencia de consulta.

**Límites del cálculo:** depende de la zona horaria local del proceso porque usa `Date.getDay`, `getDate` y `setHours` sin `America/Caracas`. El bucle empieza hoy y no comprueba una franja que comenzó ayer; a la 01:00 puede ignorar el corte 23:00–03:00. El fin usa comparaciones inclusivas en ambos extremos, a diferencia de algunos clientes. `estimatedDurationHours` sigue siendo 4 aunque la ventana real tenga otra longitud. Los porcentajes son heurísticos fijos; dos reportes antiguos o falsos también elevan la cifra. El fallback modular entrega una estimación aun sin matriz válida; el consumidor debe distinguirla de un cronograma confirmado.

**Errores:** 400 `sectorId is required`; 404 `Sector not found`; ante excepción 500 con `error.message`. No hay validación estricta del id, estructura remota o límites de frecuencia en el archivo. La consulta del historial no produce por sí misma un modelo de aprendizaje: solo afecta a la cifra fija de confianza.

Ejemplo de solicitud:

```http
GET /api/getOutagePrediction?sectorId=sector-demostracion
```

Ejemplo sintético de respuesta para un reloj de prueba fijado al 15 de octubre de 2026 a las 02:00 de Caracas, una franja A 03:00–07:00 y menos de dos reportes:

```json
{
  "sectorId": "sector-demostracion",
  "sectorName": "Sector de demostración",
  "rotationBlock": "A",
  "nextEstimatedStartMillis": 1792047600000,
  "nextEstimatedEndMillis": 1792062000000,
  "estimatedDurationHours": 4,
  "confidencePercentage": 88,
  "algorithmDetail": "PAC A: Hoy 03:00 a 07:00",
  "hoursUntilWindow": 1
}
```

Es un ejemplo de contrato, no la predicción de un sector real. Para reproducir exactamente esos timestamps el proceso de prueba debe usar la zona de Caracas o simularla; una ejecución en UTC mostraría precisamente el problema de zona descrito.

### 8.4 `api/sendPushAlert.js`

Fuente: [sendPushAlert.js](<E:/LUZ BARINAS/admin-web/api/sendPushAlert.js:1>), 59 líneas.

**Inicialización de módulo — líneas 4–19.** Conserva `initError`. Si no hay aplicaciones Firebase inicializadas, lee la variable de entorno `FIREBASE_SERVICE_ACCOUNT`, interpreta su JSON y crea credencial con `cert`; si no existe intenta `initializeApp()` con el entorno disponible. Un error de ese bloque se guarda y el handler responderá 500. Obtiene `getMessaging()` a nivel de módulo cuando no hay ese error. No almacena credenciales en el navegador ni las devuelve como respuesta normal; sus valores no aparecen aquí.

**Handler `module.exports(req,res)` — línea 21.** Después de OPTIONS, si hubo fallo de inicialización responde 500 con su mensaje. Lee `title`, `message`, `is_alarm` de query o cuerpo con prioridad de query y valores iniciales `Alerta General`, cadena vacía y `false`. Fija el tema **`barinas_global`**; no lee ni respeta `req.body.topic`. Forma un payload de **datos** de FCM, no la propiedad `notification`:

```json
{
  "data": {
    "title": "Título ilustrativo",
    "message": "Mensaje ilustrativo",
    "is_alarm": "false"
  },
  "topic": "barinas_global"
}
```

Invoca `messaging.send(payload)` y responde 200 `{success:true,messageId:response}`. Ante excepción registra el objeto y devuelve 500 `{error:error.message}`. La conversión explícita `String(isAlarm)` asegura una cadena para ese campo; los otros campos no se normalizan ni limitan en este código y pueden fallar ante tipos no admitidos por FCM.

**Quién la llama:** `AdminPortal.handleSendBroadcast` y la prueba de alarma del panel clásico. El navegador solicita al servidor; es el servidor con Firebase Admin quien autoriza técnicamente la emisión frente a FCM. El destinatario Android debe estar suscrito al tema y procesar el mensaje de datos; un 200 de esta función no confirma que todos los dispositivos lo hayan recibido o mostrado.

Ejemplo documental de la intención del formulario, **sin ejecutarlo**:

```http
POST /api/sendPushAlert
Content-Type: application/json

{"title":"Aviso de prueba documental","message":"Este ejemplo no se envió","is_alarm":false}
```

Ejemplo de forma de respuesta: `{"success":true,"messageId":"identificador-ficticio-de-mensaje"}`. El identificador es ilustrativo.

**Límites de seguridad actuales:** no valida sesión, rol, token, origen confiable, método de escritura, tamaño ni cuota; un GET también alcanza `messaging.send`. No se debe confundir el formulario protegido visualmente con un endpoint autorizado. Para una corrección de mantenimiento haría falta exigir identidad/rol en este handler, POST obligatorio, validación del payload, límites y registro del emisor; cambiar solo CORS o esconder un botón no aporta esa autorización.

## 9. Prerender, compilación y alojamiento

### 9.1 `scripts/blog-prerender.mjs`: por qué existe

Fuente: [blog-prerender.mjs](<E:/LUZ BARINAS/admin-web/scripts/blog-prerender.mjs:1>), 272 líneas.

Un lector sin JavaScript solo recibiría el contenedor vacío de la aplicación. Este plugin de Vite toma `dist/index.html` después de compilar y genera el índice y los cinco artículos con texto real, metadatos y JSON-LD, usando los mismos datos que React. También crea el sitemap. Se ejecuta en la máquina de compilación y escribe archivos; **no es una API ni una tarea programada del servidor**.

Importa `node:fs/promises`, `node:path` y el catálogo editorial. Sus cadenas `staticNav` (67) y `staticDisclaimer` (78) proporcionan navegación y aviso de independencia para el HTML sin JavaScript. No dependen de componentes React, por lo que su presentación no será idéntica. Cuando carga React, `createRoot` sustituye ese contenido con el componente interactivo.

### 9.2 Helpers de construcción HTML

**`esc(s)` — línea 28.** Convierte un valor —`null`/`undefined` se vuelven cadena vacía— a texto y escapa `&`, `<`, `>` y comillas dobles. Devuelve una cadena apta para los contextos de texto/atributo doble que utiliza el generador. Lo llaman plantillas, metas y fuentes. No analiza URLs ni permite concluir que una dirección sea segura; tampoco sanea HTML editorial deliberadamente insertado como `a.content`.

**`jsonLdTag(data)` — línea 35.** Recibe un objeto, lo serializa a JSON, sustituye `<` por su escape Unicode y lo envuelve en `<script type="application/ld+json" data-blog-jsonld="true">`. Devuelve la etiqueta como cadena. `applyHead` lo mapea para cada bloque de datos estructurados. El escape evita que un texto con `<` se interprete como cierre de script en HTML.

**`upsertTag(html,regex,tag)` — línea 38.** Si `regex.test(html)` encuentra una etiqueta, la reemplaza; si no, inserta la nueva antes de `</head>`. Devuelve HTML actualizado. No escribe archivos. Lo llaman `setMeta` y `applyHead`. Depende de un formato de plantilla con `</head>` y patrones compatibles; no es un parser DOM.

**`setMeta(html,attr,key,value)` — línea 42.** Construye una expresión regular para `<meta attr="key"...>`, compone una meta escapada y delega a `upsertTag`. Devuelve HTML. `attr` vale `name` o `property` en sus llamadas actuales; `key` viene de constantes conocidas. No debería utilizarse sin revisar los patrones para claves arbitrarias con caracteres de expresión regular.

**`applyHead(template,{title,description,keywords,canonical,type,jsonLd,extra=[]})` — línea 47.** Reemplaza título, instala descripción y palabras clave si existen, canónico, Open Graph y Twitter. Fija `twitter:card=summary`; agrega las metas `extra` y cada `jsonLdTag` antes de `</head>`. Devuelve un documento HTML; no toca disco. `closeBundle` la llama para el índice y cada artículo. `jsonLd` se espera como array; no hay validación del contrato de parámetros. Algunas metas globales de la plantilla que no se reescriben, como imagen, siguen heredadas.

**`injectRoot(html,markup)` — línea 63.** Sustituye la forma exacta `<div id="root"></div>` por esa etiqueta con el contenido `markup`. Devuelve HTML. Si cambia el formato de la plantilla y ya no coincide, la sustitución no hará nada y el build puede producir una página sin texto estático aunque termine. No detecta ni reporta esa falta de coincidencia.

### 9.3 Plantillas de contenido y datos estructurados

**`articleMarkup(a)` — línea 87.** Recibe un artículo enriquecido. Obtiene relacionados, crea navegación, migas, encabezado, autor/fecha/duración, inserta `a.content`, agrega etiquetas, aviso y enlaces de lectura. Devuelve HTML estático. `closeBundle` la invoca por artículo. Escapa los campos de texto, pero confía en `content` como HTML local. No ejecuta código del artículo ni realiza consultas.

**`blogIndexMarkup()` — línea 118.** Recorre `articles`, crea entradas con enlaces, fecha, duración, categoría y resumen, y envuelve con navegación, título y aviso. Devuelve HTML. No incluye el filtro interactivo por etiquetas que aparece después de montar React; todo el catálogo queda visible a un lector sin JavaScript.

**`breadcrumb(items)` — línea 140.** Recibe `[{name,url}]` y devuelve un objeto Schema.org `BreadcrumbList` con `ListItem.position` comenzando por 1. No devuelve HTML: `jsonLdTag` lo serializa después. `closeBundle` lo usa para índice y artículos.

**`sitemapXml()` — línea 148.** Sin parámetros. Construye las URLs de inicio, blog, todos los artículos y las cinco páginas legales/créditos, con prioridad y frecuencia sugerida. `lastmod` de artículos usa su fecha editorial; inicio y legales usan el día UTC de la compilación. Devuelve XML, no lo escribe. No incluye `/admin` ni modo TV. Cambiar un texto legal sin una fecha propia hace que su `lastmod` dependa de cuándo se compiló, no de una prueba de modificación de ese contenido.

### 9.4 `blogPrerender()`, `configResolved(config)` y `closeBundle()`

**`blogPrerender()` — línea 173.** Factoría sin parámetros. Guarda `outDir='dist'` en un cierre y devuelve el objeto plugin `{name:'pac-blog-prerender',apply:'build',configResolved,closeBundle}`. Lo llama `vite.config.mjs` al componer plugins. No genera contenido en modo `dev` por la restricción `apply:'build'`.

**`configResolved(config)` — línea 178.** Método del plugin llamado por Vite. Recibe configuración resuelta y actualiza `outDir = path.resolve(config.root,config.build.outDir)`. Retorno sin uso. Esto evita suponer que la salida es relativa al directorio desde donde se invocó la herramienta.

**`closeBundle()` — línea 181.** Método asíncrono llamado por Vite al cerrar la compilación. Lee el `index.html` compilado de `outDir` y sigue este flujo:

1. Genera `/blog/index.html`: metas de índice, objeto `Blog` con publicaciones y `BreadcrumbList`; inserta `blogIndexMarkup`.
2. Crea el directorio blog si falta y escribe el documento con `fs.writeFile`.
3. Recorre cada artículo; arma su dirección canónica y metas extra `article:published_time`, `article:section`, `article:tag`.
4. Crea JSON-LD `BlogPosting` —autor/editor, imagen, palabras, fechas— y `BreadcrumbList`; inserta `articleMarkup`.
5. Crea `blog/<slug>` y escribe su `index.html`.
6. Escribe `sitemap.xml` y registra cuántos artículos generó.

No atrapa errores de disco en un `try/catch` propio: un fallo puede interrumpir el build, que es preferible a asumir que los archivos existen. No limpia artículos antiguos por su cuenta; la limpieza del directorio la configura Vite. Si se cambia un slug hay que revisar enlaces y redirecciones: no genera automáticamente un redirect desde el anterior.

### 9.5 `vite.config.mjs`

Fuente: [vite.config.mjs](<E:/LUZ BARINAS/admin-web/vite.config.mjs:1>), 28 líneas. No declara una función propia por nombre.

Configura una variable de entorno de advertencia, importa `defineConfig`, plugins React/Tailwind, utilidades de ruta y `blogPrerender`. Obtiene `__dirname` con `fileURLToPath(new URL('.',import.meta.url))`. El objeto exportado por defecto de `defineConfig` incorpora tres plugins: `react()`, `tailwindcss()`, `blogPrerender()`.

`build.rollupOptions.input` define **dos entradas HTML**: `main` para `index.html` y `admin` para `admin.html`. `outDir='dist'` y `emptyOutDir=true` hacen que cada compilación regenere la salida. El sitio y panel clásico se empaquetan juntos, mientras las funciones de `api` son tratadas por el alojamiento. Por convención de Vite, archivos de `public` se copian a la salida; esa es la razón por la que el `app.js` clásico distribuido proviene de `public/app.js`.

No define variables `VITE_*` para centralizar la conexión Supabase del sitio ni una URL base diferente por ambiente. Tampoco configura rutas de SPA: eso se resuelve entre `App` y rewrites del alojamiento.

### 9.6 `package.json`

Fuente: [package.json](<E:/LUZ BARINAS/admin-web/package.json:1>), 27 líneas. Es metadato/configuración, sin funciones.

Declara el paquete privado `luz-barinas-admin-web`, versión 1.0.0 y descripción del portal. Ese nombre no determina el repositorio remoto: el remoto web local verificado para el proyecto apunta a `https://github.com/Ralag/pac-barinas.git`.

| Script | Operación real |
|---|---|
| `dev` | `vite`: servidor de desarrollo de las páginas; no prueba por sí mismo las funciones Vercel. |
| `build` | `vite build`: compila las entradas y ejecuta el prerender. |
| `preview` | `vite preview`: sirve la salida compilada para revisión local; no reproduce necesariamente rewrites y serverless de producción. |

Dependencias de ejecución: Supabase JS, Firebase Admin, Leaflet, Lucide React, React y React DOM. Las de desarrollo incluyen Vite, plugin React, Tailwind/plugin Vite, PostCSS y Autoprefixer. Firebase Admin pertenece a las APIs, no debe ejecutarse con credenciales de servicio en el navegador. Leaflet puede estar en el paquete aunque el mapa clásico lo cargue desde CDN; no significa que las páginas React utilicen un mapa.

Las versiones declaradas usan rangos `^`; `package-lock.json` fija una resolución de instalación y debe conservarse para reproducibilidad. No existen scripts `test`, `lint` ni `typecheck` en este manifiesto. Este manual no consultó avisos externos de vulnerabilidades ni garantiza la seguridad de las versiones instaladas.

### 9.7 `vercel.json`

Fuente: [vercel.json](<E:/LUZ BARINAS/admin-web/vercel.json:1>), 42 líneas. Es configuración declarativa, sin funciones.

Declara framework Vite, build `npm run build` y salida `dist`. Los rewrites se evalúan en el orden escrito:

| Origen | Destino | Consecuencia |
|---|---|---|
| `/api/(.*)` | `/api/$1` | Mantiene las rutas de funciones. |
| `/admin/(.*)` y `/admin` | `/admin.html` | Abre panel clásico en una solicitud HTTP directa. |
| `/blog` | `/blog/index.html` | Sirve índice prerenderizado. |
| `/blog/:slug` | `/blog/:slug/index.html` | Sirve el artículo generado para ese slug. |
| Resto | `/index.html` | Entrada SPA para las demás páginas. |

La caché de `/assets/(.*)` es de un año, pública e `immutable`, razonable para nombres de archivo con hash. Las APIs fijan sus propias cabeceras de caché. Globalmente añade `X-Content-Type-Options:nosniff`, `X-Frame-Options:DENY` y `X-XSS-Protection:1; mode=block`. Esa última no reemplaza escape de contenido y no es una CSP. No hay una `Content-Security-Policy` definida aquí.

No contiene por sí mismo usuarios ni permisos administrativos. No se examinó la configuración externa del proyecto Vercel, secretos, reglas adicionales, caché efectiva ni qué revisión está publicada.

### 9.8 `firebase.json`

Fuente: [firebase.json](<E:/LUZ BARINAS/admin-web/firebase.json:1>), 16 líneas. Configuración de Hosting alternativa, sin funciones.

Establece `hosting.public='.'`, excluye `firebase.json`, archivos ocultos y `node_modules`, y reescribe cualquier ruta a `/index.html`. No define funciones Firebase ni las mismas rutas particulares de Vercel. Si se usa tal como está, publica desde la carpeta fuente en vez de limitarse a `dist`; podría incluir documentos, scripts y otros archivos que no forman parte de la salida deseada. No se comprobó un Hosting Firebase activo.

No conviene tratar `firebase deploy` y Vercel como procedimientos equivalentes: con esta configuración son salidas, resolución de rutas y capacidades serverless diferentes. La referencia histórica a Firebase no significa que el backend de datos actual sea Firestore.

## 10. Recetas de mantenimiento y verificación

Estas secuencias explican dónde actuar y qué comportamiento revisar. No describen cambios efectuados en el producto durante la redacción del manual.

### 10.1 Cambiar un artículo o añadir otro

1. Editar o crear un objeto en `src/data/articles/` con el contrato documentado; conservar HTML local confiable.
2. Añadir su importación y posición en `articles.js` si es nuevo. El orden afecta identificadores, destacados y relacionados.
3. Definir un slug único y h2 con ids compatibles con `getTableOfContents`; revisar enlaces cruzados.
4. Revisar texto, portada, tags, metadescripción y fecha. La palabra «relacionados» no implica una búsqueda semántica.
5. Compilar y verificar que existe `dist/blog/<slug>/index.html`, contiene texto dentro de `#root`, canónico correcto, JSON-LD y entrada de sitemap.
6. Revisar la navegación con y sin JavaScript. Cambiar una URL existente requiere planificar una redirección; el plugin no la crea.

### 10.2 Cambiar reglas PAC o duración de franjas

Revisar conjuntamente `calculatePacTime`, la tabla de `PacLiveWidget`, los seis mapeos de cronograma de TV, `getMonthlyWeeks`, `getOutagePrediction` y los otros consumidores Android/clásico descritos en sus capítulos. El anillo de `AppTheme` y la duración de predicción tienen constantes de cuatro horas. Especificar antes la zona horaria, pertenencia al día de franjas nocturnas, fin exclusivo de intervalos, transición semanal y cambio de mes.

Casos de comprobación útiles: 02:59/03:00, 22:59/23:00, 00:00 dentro de turno nocturno, domingo/lunes, días 7→8 y 21→22, último día de mes y principio del siguiente, franjas distintas de cuatro horas, arrays vacíos y slots fuera de orden. Comparar bloque y timestamps entre capas es más útil que verificar solo el texto visible.

### 10.3 Evitar perder asignaciones al publicar

Antes de unificar guardados, identificar el contrato completo de `pac_schedule`: no basta con `matrixRows` y `slots` si otra pantalla conserva `sectorsA-D`. El formulario React reemplaza el JSON; el panel clásico guarda más propiedades. Para mantenimiento se debe decidir una fuente de asignaciones —campos del cronograma o registros `sectors`— y preservar coherencia. Una prueba de publicación debe incluir un campo adicional preexistente y dos editores con versiones distintas para comprobar que no se borra o pisa información silenciosamente.

### 10.4 Distinguir éxito visual de operación confirmada

Revisar `{data,error}` de Supabase, no solo excepciones. `ContactSection`, `fetchAdminData`, `loadRemoteDonations` y `fetchTelemetryFromSupabase` omiten parte de ese control; `pingApi` omite `res.ok`. Probar fallas simuladas permite confirmar que no aparece un mensaje de entrega/sincronización cuando la operación fue rechazada. No se necesita enviar reportes reales ni alertas globales para esas pruebas.

### 10.5 Almacenamiento, red y datos: mapa de referencia

| Dato | Dónde vive | Quién escribe/lee |
|---|---|---|
| Ruta actual | Estado de `App` e historial del navegador | `navigate` y `handlePopState`. |
| Caché pública | `sessionStorage.pac_telemetry_cache_v1` | `useTelemetry`; nunca sustituye una política de servidor. |
| Tema TV | `localStorage.pac_kiosk_theme` | Contenedor TV. |
| Bloque local TV | `localStorage.pac_kiosk_block` | Contenedor TV. |
| Acceso administrativo visual | Marcador `localStorage.luz_admin_session` | Ambos paneles; no es una sesión autenticada de servicio. |
| Matriz/franjas | `app_config/pac_schedule` | Paneles escriben; hook y APIs leen. |
| Cuentas de aportes | `app_config/donations_config` | Paneles escriben; sección pública y app leen. |
| Reportes ciudadanos | `citizen_reports` | Sitio muestra; Android/otros clientes aportan según sus flujos. |
| Sectores/circuitos | `sectors` | Hook y API predicción leen; panel clásico/admin Android administran según sus capítulos. |
| Solicitudes de ubicaciones | `community_locations` en modelo versionado | El panel React intenta incorrectamente otra tabla. |
| Mensaje de contacto | `citizen_feedback` intentada por el formulario | `ContactSection`; esquema remoto no comprobado. |
| Contenido de blog | Archivos locales del repositorio | Autores editan; React y prerender leen. |
| Emisión push | Solicitud HTTP → servidor → FCM | Handler `sendPushAlert`; no hay persistencia de auditoría en el archivo. |

### 10.6 Verificaciones locales que respaldan la lectura

Durante la revisión previa se ejecutaron comprobaciones aisladas en memoria, sustituyendo Supabase/Firebase por objetos simulados y sin llamadas de red:

- Handler de push con GET y sin identidad: invocó el envío simulado una vez y respondió 200.
- Predicción con hora 01:00 de Caracas y franja 23:00–03:00: devolvió un comienzo a 22 horas, omitiendo el corte que había empezado ayer.
- Lógica clásica de lectura PAC simulada con error: llamó publicación automática y mostró conexión. Se documenta en el capítulo del panel clásico, no se corrige aquí.
- Una etiqueta HTML inocua en nombre de solicitud del panel clásico llegó sin escape al render. Su análisis detallado corresponde al capítulo clásico.

Son evidencias de comportamiento del código local bajo condiciones controladas, no pruebas de explotación, disponibilidad, permisos activos o recepción de notificaciones en producción. La redacción de este capítulo solo añadió documentación; no alteró código de producto, configuración ni datos externos.

## 11. Índice de funciones y cobertura

El índice siguiente enumera las declaraciones nombradas de este ámbito detectadas por el inventario AST común, con archivo y línea. `exports` identifica el handler anónimo asignado a `module.exports`, no una función que el código haya bautizado con ese nombre. Los callbacks anónimos de efectos, temporizadores, `map/filter/reduce`, formularios y botones se explican en el bloque de su componente; no se inventa un nombre de API para cada uno.

Las exportaciones `default` de los cinco artículos son objetos, y `defineConfig(...)` exporta configuración. `index.html`, `main.jsx`, `index.css`, `supabase.js` y los JSON no añaden funciones nombradas propias, pero están cubiertos por sus apartados. Los cálculos `useMemo` relevantes también se describen aunque el inventario no los cuente como declaraciones de función con nombre.

| Archivo de origen | Función / ámbito | Línea |
|---|---|---:|
| [api/getMonthlyWeeks.js](<E:/LUZ BARINAS/admin-web/api/getMonthlyWeeks.js:7>) | `exports` | 7 |
| [api/getOutagePrediction.js](<E:/LUZ BARINAS/admin-web/api/getOutagePrediction.js:17>) | `getDayIndex` | 17 |
| [api/getOutagePrediction.js](<E:/LUZ BARINAS/admin-web/api/getOutagePrediction.js:21>) | `exports` | 21 |
| [api/sendPushAlert.js](<E:/LUZ BARINAS/admin-web/api/sendPushAlert.js:21>) | `exports` | 21 |
| [scripts/blog-prerender.mjs](<E:/LUZ BARINAS/admin-web/scripts/blog-prerender.mjs:28>) | `esc` | 28 |
| [scripts/blog-prerender.mjs](<E:/LUZ BARINAS/admin-web/scripts/blog-prerender.mjs:35>) | `jsonLdTag` | 35 |
| [scripts/blog-prerender.mjs](<E:/LUZ BARINAS/admin-web/scripts/blog-prerender.mjs:38>) | `upsertTag` | 38 |
| [scripts/blog-prerender.mjs](<E:/LUZ BARINAS/admin-web/scripts/blog-prerender.mjs:42>) | `setMeta` | 42 |
| [scripts/blog-prerender.mjs](<E:/LUZ BARINAS/admin-web/scripts/blog-prerender.mjs:47>) | `applyHead` | 47 |
| [scripts/blog-prerender.mjs](<E:/LUZ BARINAS/admin-web/scripts/blog-prerender.mjs:63>) | `injectRoot` | 63 |
| [scripts/blog-prerender.mjs](<E:/LUZ BARINAS/admin-web/scripts/blog-prerender.mjs:87>) | `articleMarkup` | 87 |
| [scripts/blog-prerender.mjs](<E:/LUZ BARINAS/admin-web/scripts/blog-prerender.mjs:118>) | `blogIndexMarkup` | 118 |
| [scripts/blog-prerender.mjs](<E:/LUZ BARINAS/admin-web/scripts/blog-prerender.mjs:140>) | `breadcrumb` | 140 |
| [scripts/blog-prerender.mjs](<E:/LUZ BARINAS/admin-web/scripts/blog-prerender.mjs:148>) | `sitemapXml` | 148 |
| [scripts/blog-prerender.mjs](<E:/LUZ BARINAS/admin-web/scripts/blog-prerender.mjs:173>) | `blogPrerender` | 173 |
| [scripts/blog-prerender.mjs](<E:/LUZ BARINAS/admin-web/scripts/blog-prerender.mjs:178>) | `blogPrerender.configResolved` | 178 |
| [scripts/blog-prerender.mjs](<E:/LUZ BARINAS/admin-web/scripts/blog-prerender.mjs:181>) | `blogPrerender.closeBundle` | 181 |
| [src/App.jsx](<E:/LUZ BARINAS/admin-web/src/App.jsx:17>) | `App` | 17 |
| [src/App.jsx](<E:/LUZ BARINAS/admin-web/src/App.jsx:19>) | `App.getInitialRoute` | 19 |
| [src/App.jsx](<E:/LUZ BARINAS/admin-web/src/App.jsx:39>) | `App.handlePopState` | 39 |
| [src/App.jsx](<E:/LUZ BARINAS/admin-web/src/App.jsx:47>) | `App.navigate` | 47 |
| [src/components/blog/ArticleCover.jsx](<E:/LUZ BARINAS/admin-web/src/components/blog/ArticleCover.jsx:13>) | `ArticleCover` | 13 |
| [src/components/blog/EditorialDisclaimer.jsx](<E:/LUZ BARINAS/admin-web/src/components/blog/EditorialDisclaimer.jsx:6>) | `EditorialDisclaimer` | 6 |
| [src/components/BlogPreviewSection.jsx](<E:/LUZ BARINAS/admin-web/src/components/BlogPreviewSection.jsx:6>) | `BlogPreviewSection` | 6 |
| [src/components/BlogPreviewSection.jsx](<E:/LUZ BARINAS/admin-web/src/components/BlogPreviewSection.jsx:9>) | `BlogPreviewSection.navigateTo` | 9 |
| [src/components/ContactSection.jsx](<E:/LUZ BARINAS/admin-web/src/components/ContactSection.jsx:5>) | `ContactSection` | 5 |
| [src/components/ContactSection.jsx](<E:/LUZ BARINAS/admin-web/src/components/ContactSection.jsx:15>) | `ContactSection.handleSubmit` | 15 |
| [src/components/DonationsSection.jsx](<E:/LUZ BARINAS/admin-web/src/components/DonationsSection.jsx:5>) | `DonationsSection` | 5 |
| [src/components/DonationsSection.jsx](<E:/LUZ BARINAS/admin-web/src/components/DonationsSection.jsx:18>) | `DonationsSection.loadRemoteDonations` | 18 |
| [src/components/DonationsSection.jsx](<E:/LUZ BARINAS/admin-web/src/components/DonationsSection.jsx:39>) | `DonationsSection.copyToClipboard` | 39 |
| [src/components/Features.jsx](<E:/LUZ BARINAS/admin-web/src/components/Features.jsx:4>) | `Features` | 4 |
| [src/components/Footer.jsx](<E:/LUZ BARINAS/admin-web/src/components/Footer.jsx:4>) | `Footer` | 4 |
| [src/components/Footer.jsx](<E:/LUZ BARINAS/admin-web/src/components/Footer.jsx:5>) | `Footer.navigateTo` | 5 |
| [src/components/Hero.jsx](<E:/LUZ BARINAS/admin-web/src/components/Hero.jsx:4>) | `Hero` | 4 |
| [src/components/Navbar.jsx](<E:/LUZ BARINAS/admin-web/src/components/Navbar.jsx:4>) | `Navbar` | 4 |
| [src/components/Navbar.jsx](<E:/LUZ BARINAS/admin-web/src/components/Navbar.jsx:7>) | `Navbar.navigateTo` | 7 |
| [src/components/PacLiveWidget.jsx](<E:/LUZ BARINAS/admin-web/src/components/PacLiveWidget.jsx:9>) | `PacLiveWidget` | 9 |
| [src/components/telemetry/CircuitStabilityWidget.jsx](<E:/LUZ BARINAS/admin-web/src/components/telemetry/CircuitStabilityWidget.jsx:5>) | `CircuitStabilityWidget` | 5 |
| [src/components/telemetry/CircuitStabilityWidget.jsx](<E:/LUZ BARINAS/admin-web/src/components/telemetry/CircuitStabilityWidget.jsx:41>) | `CircuitStabilityWidget.getStatusConfig` | 41 |
| [src/components/telemetry/CitizenReportsTicker.jsx](<E:/LUZ BARINAS/admin-web/src/components/telemetry/CitizenReportsTicker.jsx:5>) | `CitizenReportsTicker` | 5 |
| [src/components/telemetry/CitizenReportsTicker.jsx](<E:/LUZ BARINAS/admin-web/src/components/telemetry/CitizenReportsTicker.jsx:8>) | `CitizenReportsTicker.getStatusBadge` | 8 |
| [src/components/telemetry/LiveBlockCountdown.jsx](<E:/LUZ BARINAS/admin-web/src/components/telemetry/LiveBlockCountdown.jsx:4>) | `getBlockBadgeClass` | 4 |
| [src/components/telemetry/LiveBlockCountdown.jsx](<E:/LUZ BARINAS/admin-web/src/components/telemetry/LiveBlockCountdown.jsx:14>) | `getBlockBgSoft` | 14 |
| [src/components/telemetry/LiveBlockCountdown.jsx](<E:/LUZ BARINAS/admin-web/src/components/telemetry/LiveBlockCountdown.jsx:24>) | `LiveBlockCountdown` | 24 |
| [src/data/articles.js](<E:/LUZ BARINAS/admin-web/src/data/articles.js:42>) | `stripHtml` | 42 |
| [src/data/articles.js](<E:/LUZ BARINAS/admin-web/src/data/articles.js:49>) | `countWords` | 49 |
| [src/data/articles.js](<E:/LUZ BARINAS/admin-web/src/data/articles.js:51>) | `enrich` | 51 |
| [src/data/articles.js](<E:/LUZ BARINAS/admin-web/src/data/articles.js:69>) | `getArticleBySlug` | 69 |
| [src/data/articles.js](<E:/LUZ BARINAS/admin-web/src/data/articles.js:71>) | `getRelatedArticles` | 71 |
| [src/data/articles.js](<E:/LUZ BARINAS/admin-web/src/data/articles.js:75>) | `getTableOfContents` | 75 |
| [src/data/articles.js](<E:/LUZ BARINAS/admin-web/src/data/articles.js:85>) | `formatDate` | 85 |
| [src/lib/seo.js](<E:/LUZ BARINAS/admin-web/src/lib/seo.js:11>) | `upsert` | 11 |
| [src/lib/seo.js](<E:/LUZ BARINAS/admin-web/src/lib/seo.js:30>) | `metaByName` | 30 |
| [src/lib/seo.js](<E:/LUZ BARINAS/admin-web/src/lib/seo.js:36>) | `metaByProperty` | 36 |
| [src/lib/seo.js](<E:/LUZ BARINAS/admin-web/src/lib/seo.js:42>) | `useDocumentMeta` | 42 |
| [src/lib/telemetry.js](<E:/LUZ BARINAS/admin-web/src/lib/telemetry.js:10>) | `formatRelativeTime` | 10 |
| [src/lib/telemetry.js](<E:/LUZ BARINAS/admin-web/src/lib/telemetry.js:30>) | `calculatePacTime` | 30 |
| [src/lib/telemetry.js](<E:/LUZ BARINAS/admin-web/src/lib/telemetry.js:114>) | `processCircuitsSummary` | 114 |
| [src/lib/telemetry.js](<E:/LUZ BARINAS/admin-web/src/lib/telemetry.js:181>) | `fetchTelemetryFromSupabase` | 181 |
| [src/lib/telemetry.js](<E:/LUZ BARINAS/admin-web/src/lib/telemetry.js:235>) | `useTelemetry` | 235 |
| [src/lib/telemetry.js](<E:/LUZ BARINAS/admin-web/src/lib/telemetry.js:262>) | `useTelemetry.refresh` | 262 |
| [src/lib/telemetry.js](<E:/LUZ BARINAS/admin-web/src/lib/telemetry.js:297>) | `useTelemetry.handleVisibilityChange` | 297 |
| [src/pages/AdminPortal.jsx](<E:/LUZ BARINAS/admin-web/src/pages/AdminPortal.jsx:13>) | `AdminPortal` | 13 |
| [src/pages/AdminPortal.jsx](<E:/LUZ BARINAS/admin-web/src/pages/AdminPortal.jsx:74>) | `AdminPortal.fetchAdminData` | 74 |
| [src/pages/AdminPortal.jsx](<E:/LUZ BARINAS/admin-web/src/pages/AdminPortal.jsx:140>) | `AdminPortal.handleLogin` | 140 |
| [src/pages/AdminPortal.jsx](<E:/LUZ BARINAS/admin-web/src/pages/AdminPortal.jsx:152>) | `AdminPortal.handleLogout` | 152 |
| [src/pages/AdminPortal.jsx](<E:/LUZ BARINAS/admin-web/src/pages/AdminPortal.jsx:159>) | `AdminPortal.cycleCell` | 159 |
| [src/pages/AdminPortal.jsx](<E:/LUZ BARINAS/admin-web/src/pages/AdminPortal.jsx:175>) | `AdminPortal.handleSavePac` | 175 |
| [src/pages/AdminPortal.jsx](<E:/LUZ BARINAS/admin-web/src/pages/AdminPortal.jsx:201>) | `AdminPortal.handleSaveDonations` | 201 |
| [src/pages/AdminPortal.jsx](<E:/LUZ BARINAS/admin-web/src/pages/AdminPortal.jsx:222>) | `AdminPortal.handleAddSector` | 222 |
| [src/pages/AdminPortal.jsx](<E:/LUZ BARINAS/admin-web/src/pages/AdminPortal.jsx:232>) | `AdminPortal.handleRemoveSector` | 232 |
| [src/pages/AdminPortal.jsx](<E:/LUZ BARINAS/admin-web/src/pages/AdminPortal.jsx:240>) | `AdminPortal.handleSendBroadcast` | 240 |
| [src/pages/AdminPortal.jsx](<E:/LUZ BARINAS/admin-web/src/pages/AdminPortal.jsx:264>) | `AdminPortal.pingApi` | 264 |
| [src/pages/AdminPortal.jsx](<E:/LUZ BARINAS/admin-web/src/pages/AdminPortal.jsx:345>) | `AdminPortal.getBadgeClass` | 345 |
| [src/pages/Blog.jsx](<E:/LUZ BARINAS/admin-web/src/pages/Blog.jsx:8>) | `Blog` | 8 |
| [src/pages/Blog.jsx](<E:/LUZ BARINAS/admin-web/src/pages/Blog.jsx:43>) | `Blog.open` | 43 |
| [src/pages/BlogPost.jsx](<E:/LUZ BARINAS/admin-web/src/pages/BlogPost.jsx:15>) | `BlogPost` | 15 |
| [src/pages/BlogPost.jsx](<E:/LUZ BARINAS/admin-web/src/pages/BlogPost.jsx:60>) | `BlogPost.handleContentClick` | 60 |
| [src/pages/BlogPost.jsx](<E:/LUZ BARINAS/admin-web/src/pages/BlogPost.jsx:70>) | `BlogPost.go` | 70 |
| [src/pages/DeveloperInfo.jsx](<E:/LUZ BARINAS/admin-web/src/pages/DeveloperInfo.jsx:4>) | `DeveloperInfo` | 4 |
| [src/pages/LandingPage.jsx](<E:/LUZ BARINAS/admin-web/src/pages/LandingPage.jsx:9>) | `LandingPage` | 9 |
| [src/pages/PrivacyPolicy.jsx](<E:/LUZ BARINAS/admin-web/src/pages/PrivacyPolicy.jsx:4>) | `PrivacyPolicy` | 4 |
| [src/pages/RefundPolicy.jsx](<E:/LUZ BARINAS/admin-web/src/pages/RefundPolicy.jsx:4>) | `RefundPolicy` | 4 |
| [src/pages/TermsConditions.jsx](<E:/LUZ BARINAS/admin-web/src/pages/TermsConditions.jsx:4>) | `TermsConditions` | 4 |
| [src/pages/Transparencia.jsx](<E:/LUZ BARINAS/admin-web/src/pages/Transparencia.jsx:4>) | `Transparencia` | 4 |
| [src/pages/tv/AppTheme.jsx](<E:/LUZ BARINAS/admin-web/src/pages/tv/AppTheme.jsx:16>) | `AppTheme` | 16 |
| [src/pages/tv/AppTheme.jsx](<E:/LUZ BARINAS/admin-web/src/pages/tv/AppTheme.jsx:31>) | `AppTheme.getAppBlockColor` | 31 |
| [src/pages/tv/LightTheme.jsx](<E:/LUZ BARINAS/admin-web/src/pages/tv/LightTheme.jsx:12>) | `LightTheme` | 12 |
| [src/pages/tv/MercuryTheme.jsx](<E:/LUZ BARINAS/admin-web/src/pages/tv/MercuryTheme.jsx:11>) | `MercuryTheme` | 11 |
| [src/pages/tv/PerformanceTheme.jsx](<E:/LUZ BARINAS/admin-web/src/pages/tv/PerformanceTheme.jsx:12>) | `PerformanceTheme` | 12 |
| [src/pages/tv/RadarTheme.jsx](<E:/LUZ BARINAS/admin-web/src/pages/tv/RadarTheme.jsx:11>) | `RadarTheme` | 11 |
| [src/pages/tv/WebTheme.jsx](<E:/LUZ BARINAS/admin-web/src/pages/tv/WebTheme.jsx:14>) | `WebTheme` | 14 |
| [src/pages/TvKioskMode.jsx](<E:/LUZ BARINAS/admin-web/src/pages/TvKioskMode.jsx:96>) | `TvKioskMode` | 96 |
| [src/pages/TvKioskMode.jsx](<E:/LUZ BARINAS/admin-web/src/pages/TvKioskMode.jsx:119>) | `TvKioskMode.handleSelectTheme` | 119 |
| [src/pages/TvKioskMode.jsx](<E:/LUZ BARINAS/admin-web/src/pages/TvKioskMode.jsx:141>) | `TvKioskMode.handleSelectBlock` | 141 |
| [src/pages/TvKioskMode.jsx](<E:/LUZ BARINAS/admin-web/src/pages/TvKioskMode.jsx:151>) | `TvKioskMode.toggleFullscreen` | 151 |
| [src/pages/TvKioskMode.jsx](<E:/LUZ BARINAS/admin-web/src/pages/TvKioskMode.jsx:167>) | `TvKioskMode.handleFsChange` | 167 |
| [src/pages/TvKioskMode.jsx](<E:/LUZ BARINAS/admin-web/src/pages/TvKioskMode.jsx:181>) | `TvKioskMode.requestWakeLock` | 181 |
| [src/pages/TvKioskMode.jsx](<E:/LUZ BARINAS/admin-web/src/pages/TvKioskMode.jsx:192>) | `TvKioskMode.handleVisibility` | 192 |
| [src/pages/TvKioskMode.jsx](<E:/LUZ BARINAS/admin-web/src/pages/TvKioskMode.jsx:210>) | `TvKioskMode.handleKeyDown` | 210 |
| [src/pages/TvKioskMode.jsx](<E:/LUZ BARINAS/admin-web/src/pages/TvKioskMode.jsx:265>) | `TvKioskMode.getCaracasClock` | 265 |

**Cobertura comprobada:** 101 declaraciones nombradas del inventario AST de este capítulo, incluidos tres handlers asignados a `module.exports`; además, los **50 archivos** del alcance, incluidos los archivos sin funciones, y los callbacks relevantes descritos en cada apartado. Se verificó que las 152 referencias a fuentes locales apuntan a archivos existentes. El inventario global y su alcance se encuentran en `inventario-funciones.json` y `07-indice-funciones.md`.

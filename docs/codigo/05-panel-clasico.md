# 05. Panel clásico: HTML, JavaScript y administración

**Actualización del código:** 16 de octubre de 2026. Este capítulo explica `admin-web/admin.html`, `admin-web/public/app.js`, su copia `admin-web/app.js` y las hojas de estilo clásicas. Los números históricos pueden desplazarse; el [índice de funciones](07-indice-funciones.md) permite localizar las declaraciones vigentes. La autenticación, escape y lectura PAC se reforzaron en fuente; esto no acredita despliegue ni permisos efectivos del servidor.

## 1. Qué aplicación es esta y qué archivos carga

El panel clásico es una página HTML con nueve pestañas. Carga estilos, Tailwind por CDN, Leaflet/calor, SDK Supabase, **/pac-schedule-core.js**, **/pac-map-core.js**, **/admin-security.js** y **/app.js**, con rutas absolutas para recargas bajo /admin. Los controles estáticos conservan callbacks conocidos; las acciones que transportan datos ciudadanos usan ahora atributos data-* y delegación DOM, sin interpolar nombres o IDs en JavaScript ejecutable.

Vite construye `admin.html` como entrada adicional y copia `public/app.js` a la salida. Por eso la copia distribuida como `dist/app.js` procede de `public/app.js`. El archivo `admin-web/app.js` también existe y sirve al abrir la carpeta por ciertos servidores simples, pero no es la fuente de la copia estática de Vite. No se debe corregir solamente esa copia raíz esperando que cambie el panel compilado.

Las copias raíz y public/app.js están sincronizadas y una prueba exige igualdad exacta. Conservan reconocimiento de FALLA_IRREGULAR y bloque ausente representado como guion. No se deben mantener variantes de seguridad distintas en cada copia.

`public/styles.css` define variables de fondo, superficie, texto, colores de bloques/estados, scrollbar, paneles, modales y clases usadas por las tablas. El HTML contiene además utilidades Tailwind y estilos propios. Cambiar una variable CSS no modifica colores escritos directamente en clases o plantillas JavaScript.

## 2. Estado global y flujo de vida

| Variable | Significado y origen | Persistencia |
|---|---|---|
| `supabaseClient` | Cliente inicializado con URL y clave pública del proyecto | Instancia de memoria; la seguridad depende del servidor, no de ocultar esa clave. |
| `pacSlots` | Lista de franjas con índice, etiqueta, inicio y fin | Empieza con defaults, se sustituye al leer PAC y se publica en `app_config`. |
| `pacMatrix` | Matriz: fila=franja, columna=0 lunes a 6 domingo | Igual que slots; cada celda contiene A/B/C/D o `-`. |
| `pacVersion` | Versión leída o inicializada con la hora | No impide por sí misma escrituras concurrentes. |
| `sectorsA` a `sectorsD` | Listas de nombres asociados a cada bloque | Viajan dentro de `pac_schedule`; no equivalen a las filas de `sectors`. |
| `sectorsList` | Directorio activo leído de Supabase | Memoria, filtrada además por una lista local de IDs ocultos. |
| `communityList` | Solicitudes leídas de `community_locations` | Memoria; la aprobación intenta modificar servidor y listas. |
| `citizenReports` | Últimos 50 reportes, ordenados por `timestamp` | Memoria; el feed no representa todos los reportes históricos. |
| `activeEditCell` | Coordenadas de la celda cuyo modal está abierto | Solo memoria. |
| `detectedClusters` | Grupos propuestos por similitud de nombres | Se reconstruyen al cambiar directorio/solicitudes. |
| `osmCache` | Resultados de búsquedas geográficas | `Map` en memoria; se pierde al recargar. |
| Filtros/vista/mapa | Variables `telemetryFilter`, `reportFeedFilter`, `masterFilter*`, `mapFilterBlock`, `directoryViewMode`, capas Leaflet | Estado de interfaz, no configuración global remota. |
| `adminAuth` | Controlador compartido Supabase Auth/getUser/is_admin | Estado del permiso verificado; el SDK gestiona su sesión. La bandera heredada no se consulta. |
| `loadedPacConfig` | Último documento PAC validado | Memoria; se mezcla al publicar para conservar campos adicionales. |
| `adminSyncActive`, `authUnsubscribe` | Inicio/limpieza de sincronización y escucha de acceso | Evitan reabrir canales por cada renovación y ocultan administración al perder permiso. |
| `pac_community_overrides` | Clasificación manual por ID o nombre | Solo ese navegador; puede diferir de otros administradores. |
| `pac_deleted_sector_ids` | IDs que ese navegador deja de mostrar | Ocultar localmente no demuestra que el borrado remoto funcionó. |

Al llegar DOMContentLoaded, checkAuth se suscribe al permiso y registra listeners de acciones. Solo un estado admin verificado abre el panel e inicia initFirebaseSync —nombre histórico de Supabase— y precarga donaciones. Ante pérdida de acceso oculta panel y modales, invalida el documento publicable y retira canales. Los eventos Realtime siguen provocando relecturas completas. Un temporizador independiente actualiza la franja activa cada 30 segundos.

## 3. Páginas internas y acciones disponibles

| Pestaña / contenedor | Qué muestra | Qué funciones producen cambios |
|---|---|---|
| `tabPac` | Matriz semanal, proyecciones y editores de franjas/esquema | `setCellBlock`, `handleApplySchemeConfig`, `handleApplyBranchScale`, `handleSaveSlot`, `handleAddSlot`, `deleteSlot`, `publishPacSchedule`. |
| `tabTelemetry` | Estado/voltaje por sector y franja teórica activa | `updateSectorStatus`, `applyActivePacCutToSectors`, `resetAllTelemetryToNormal`, `setAllSectorsStatus`. |
| `tabBlocks` | Columnas de nombres A/B/C/D | `reassignSector`, `removeSectorFromBlock`, `handleAddSectorToBlock`. |
| `tabGeneralSectors` | Directorio, filtros, fichas/tabla y mapa | Crear, editar, borrar, geocodificar y fusionar sectores. |
| `tabCommunity` | Propuestas editables | `approveCommunityLocation` y `rejectCommunityLocation`. |
| `tabReportsFeed` | Últimos reportes y estadísticas sobre esos 50 | Filtros y actualización; no constituye agregación histórica completa. |
| `tabBroadcast` | Aviso activo y formulario | Publicar/desactivar aviso en configuración; envío FCM se hace con otro botón/API. |
| `tabVercelConsole` | Feed construido con reportes y ping | No obtiene logs reales de la plataforma Vercel. |
| `tabDonations` | Campos PayPal, Binance y Pago Móvil | saveDonationConfig; loadDonationUrl se llama al abrir la sesión autorizada. |

## 4. Contratos comunes de las funciones

Salvo que una ficha indique retorno explícito, una función normal devuelve `undefined` y una `async` resuelve una promesa sin dato útil; su resultado visible es un cambio en DOM, memoria o servidor. Los formularios reciben el evento y usan `preventDefault()` para evitar una recarga. Abrir o cerrar un modal no guarda información. Las funciones `render*` reconstruyen HTML a partir del estado actual, normalmente sin consultar red.

Las llamadas Supabase suelen devolver `{ data, error }`. En varios procedimientos de este panel solo hay `try/catch`: eso no convierte automáticamente un `error` devuelto en una excepción. Por esa razón algunas fichas advierten que el mensaje mostrado no confirma una escritura.

### 4.1 Texto no confiable, atributos y acciones

El panel importa `escapeHtml`, `normalizeBlock`, `readPacSchedule` y `createAdminAuth` del [núcleo compartido](https://github.com/Ralag/pac-barinas/blob/7d5b4b57bc36ff9eb62df2ff6fb94945407ce031/public/admin-security.js), explicado función por función en el [capítulo 04](04-web-react-y-api.md#25-publicadmin-securityjs-y-srclibadminauthjs-autenticación-compartida). `escapeHtml(value)` convierte caracteres con significado HTML en entidades. Se aplica al texto y a los valores de atributos entre comillas: nombres, notas, observaciones, ID, ubicación, circuito, estados y errores de consulta. El ID original permanece en memoria; el navegador lo recupera decodificado mediante `dataset`.

Las plantillas de `initVercelLogsSync`, `renderMatrixTable`, `renderMonthlyProjection`, `renderTelemetryList`, `renderBlockKanban`, `renderTypoClusters`, `renderOsmResult`, `renderCommunityTable`, `renderCitizenReportsFeed`, `updateBarinasMapMarkers`, `renderWikipediaCardsView`, `renderDetailedTableView` y `checkSchemaCacheStatus` representan esos valores como texto o atributos escapados. Los bloques usados para clases/etiquetas pasan por `normalizeBlock(value)`, que admite A–D y usa `-` para otros valores. Las opciones de municipio y parroquia también escapan sus etiquetas y valores.

Los botones y selects que antes componían código con nombres o ID ahora declaran `data-admin-action` y atributos de datos. `handleAdminAction(event)` recibe click/change del documento y despacha una lista fija de callbacks. No se evalúa una cadena como JavaScript; una comilla en un nombre no altera el manejador. La función valida también coordenadas numéricas antes de enfocar el mapa. Las acciones estáticas y los índices numéricos de la matriz conservan sus manejadores existentes.

Esto conserva las funciones del panel; no introduce un saneador que elimine controles. Siguen existiendo `innerHTML` para estructura estática y plantillas con datos escapados, dependencias CDN y algunos manejadores inline de acciones estáticas. No hay una CSP completa ni una prueba de navegador que cubra todas las integraciones externas. La autorización de filas sigue correspondiendo a RLS y la del envío a la API, aunque alguien invoque una función desde la consola.

## 5. Acceso, navegación, conexión y lecturas

| Función y línea | Entradas, resultado, pasos y relación con el sistema |
|---|---|
| `checkAuth()` | Instala una sola suscripción de adminAuth a renderAdminAccess y devuelve requireAdmin(). DOMContentLoaded la llama y maneja el rechazo; una bandera local falsa no abre el panel. |
| `renderAdminAccess(state)` | Recibe estado verificado; muestra panel o formulario, botón ocupado, error accesible y posibilidad de cerrar cuenta sin rol. Inicia sync/donaciones una vez por acceso; al perderlo oculta modales, borra loadedPacConfig y retira canales. No modifica roles. |
| `handleLogin(event)` | Async: previene formulario, entrega correo/contraseña exacta al controlador, limpia contraseña tras éxito y muestra error mediante textContent. No hay contraseñas maestras en fuente. |
| `handleLogout()` | Async: llama cierre local del SDK por adminAuth; la UI se cierra antes de esperar red. Comunica error si no finaliza; no afirma revocación instantánea de JWT en otros servicios. |
| `handleAdminAction(event)` | Listener de click/change. Busca data-admin-action y requiere estado admin; solo SELECT usa change. Resuelve una lista fija de operaciones y pasa dataset como datos originales, no como código. Valida lat/lon numéricas para enfoque; espera la operación y comunica errores. Las funciones RLS/API vuelven a proteger las escrituras. |
| `switchTab(tabId)` · 142 | Oculta todos los `.tab-content`, quita selección y muestra el `id` elegido. Al abrir directorio difiere 60 ms la inicialización de mapa/render; al abrir reportes repinta feed. La invocan botones HTML. No cambia la URL ni usa React. |
| `initVercelLogsSync()` · 171 | Busca 20 reportes y escucha INSERT para consola. Conserva nombres heredados reportedAtMillis/voltageObserved, pendientes de unificar; ahora escapa nombre/valores antes del HTML. No son logs de Vercel. |
| `clearVercelConsole()` · 220 | Reemplaza el contenido local de la consola por una frase de limpieza. No borra reportes ni registros en servidores. |
| `pingVercelApi()` · 225 | Llama por GET a `/api/getMonthlyWeeks?monthName=Test&totalDays=30`, representa éxito HTTP o detalle de error. Es una petición real al servidor cuando la usa un administrador; no mide todos los componentes del sistema. |
| `updateSyncStatus(status, message=null)` · 247 | Cambia texto y punto de color según connected/syncing/offline. Devuelve sin actuar si no encuentra los nodos. Es presentación; el estado conectado no prueba por sí solo la recepción de todos los canales. |
| `initConnectionMonitor()` · 265 | Instala una sola vez listeners online/offline. Al volver online solo comprueba conexión si el estado sigue siendo admin. No reinicia todas las lecturas. |
| `retryFirebaseConnection()` · 279 | Comprueba lectura PAC, lanza error si existe y muestra conectado/desconectado. El botón ahora llama esta función correcta. Es comprobación de una lectura, no garantía de todos los canales. |
| `initFirebaseSync()` · 291 | Inicia monitor, consola y cinco lecturas/canales después de acceso verificado. No espera una finalización conjunta; adminSyncActive evita duplicarlo en renovaciones. renderAdminAccess carga donaciones por separado. |
| `fetchPac()`, local | Usa readPacSchedule del núcleo, valida antes de sustituir memoria y loadedPacConfig, repinta y solo después marca lectura correcta. Ante fallo conserva la copia previa y muestra offline/error: no publica ni siembra para recuperarse. |
| `fetchSectors()`, local | Lee sectores, incluidos cero resultados sin seed automático. Excluye IDs ocultos, calcula contadores y repinta. La carga de catálogo sigue como operación explícita; el filtro local de IDs aún puede disimular un borrado remoto fallido. |
| `fetchCommunity()` · 385, local | Lee propuestas, reemplaza `communityList`, ajusta badges y ejecuta tabla/clustering. No cambia aprobaciones; requiere publicación Realtime para actualizarse por eventos. |
| `fetchCitizen()` · 413, local y `window` | Lee 50 reportes recientes por `timestamp`, reemplaza memoria y renderiza el feed. Se expone en `window.fetchCitizen` para el botón de actualizar. No usa el campo local de Room `reportedAtMillis`. |
| `fetchBroadcast()` · 429, local | Lee aviso; si está activo lo representa, si no lo oculta. Ante lectura fallida también lo oculta, por lo que oculto no distingue desactivación de error. |

## 6. Edición de PAC y proyección mensual

La matriz tiene siete columnas de lunes a domingo. Cambiar una celda puede afectar solo esa celda, su pareja situada media matriz más adelante, o toda la semana según los toggles. La pareja se calcula por **posición de fila**, no comprobando que haya exactamente doce horas entre ambas. Al usar franjas personalizadas esa distinción importa.

La proyección divide el mes en 1–7, 8–14, 15–21 y 22–fin. Rota filas con `(fila + desplazamiento) % cantidadDeFranjas`; no reconstruye semanas de lunes a domingo según el calendario real. Tampoco calcula una quinta semana independiente para los días 29–31.

| Función y línea | Contrato y operación |
|---|---|
| `renderMatrixTable()` · 452 | Lee slots/matriz, vacía tbody y crea una fila por franja, siete botones y acciones de edición/borrado. Un bloque ausente se muestra `-`. Al terminar llama a `updateDetectedSchemeBadge`. No guarda. |
| `onclick` de celda · 482 | Callback creado por cada botón: captura `slotIdx/dayIdx` y llama `openCellModal`. Esa captura conserva la identidad de la celda sin buscar índices en el HTML. |
| `getBlockBadgeClass(block)` · 514 | Devuelve clase CSS A/B/C/D tras quitar BLOQUE; entrada vacía o desconocida devuelve `badge-block-none`. Función de presentación reutilizada en tablas/mapas. |
| `renderWeeklyAudit()` · 527 | Cuerpo vacío. Las llamadas existentes no hacen auditoría ni cambian estado. No debe describirse como cálculo implementado. |
| `updateDetectedSchemeBadge()` · 529 | Cuenta apariciones por bloque en cada día y usa el máximo semanal: hasta una muestra esquema simple; más, doble. La etiqueta dice 4h/8h aunque la duración configurada pueda ser distinta. |
| `openCellModal(slotIdx, dayIdx)` · 554 | Guarda coordenadas, títulos y estado inicial del toggle de parejas según repeticiones o seis slots. Muestra modal. Requiere índices válidos. |
| `closeCellModal()` · 576 | Oculta modal y anula `activeEditCell`. No revierte una edición ya aplicada. |
| `setCellBlock(block)` · 581 | Sin celda activa termina. Lee toggles. Puede rellenar la semana, sincronizar una pareja o editar una celda; después cierra, repinta y publica automáticamente. Borrar con `-` no activa propagación/pareja. Ver algoritmo debajo. |
| `openBranchGeneratorModal()` · 673 | Muestra generador de escala. Solo modifica visibilidad del DOM. |
| `closeBranchGeneratorModal()` · 677 | Oculta el generador, sin guardar ni restaurar matriz. |
| `handleApplyBranchScale(event)` · 681 | Lee esquema, bloque inicial y paso diario. En doble usa ciclo C/A/B, seis filas y parejas 0/3,1/4,2/5; en simple distribuye A/B/C/D en las cuatro primeras filas. Sustituye matriz, repinta y publica. En la rama simple el bloque inicial leído no participa en la fórmula. |
| `setPacViewMode(mode)` · 752 | Guarda modo local; cambia visibilidad/estilos weekly/monthly y, para mensual, renderiza proyección. No cambia el PAC remoto. |
| `renderMonthlyProjection()` · 777 | Crea cuatro tarjetas con desplazamientos 0–3 sin mutar matriz base. Incluye botones de promoción para semanas posteriores. Se llama al cargar y editar. |
| `applyMonthlyWeekAsBase(offset)` · 849 | Tras confirmación copia filas rotadas como nueva base, repinta y publica. Cambia la referencia a partir de la cual otros cálculos vuelven a aplicar rotación; no es solo una selección visual. |
| `openSchemeConfigModal(defaultMode='DOUBLE_8H')` · 866 | Precarga radios, bloques y duración 4; simple activa A/B/C/D, doble A/B/C y desmarca D. Abre modal sin escritura. |
| `closeSchemeConfigModal()` · 891 | Oculta modal de configuración. |
| `onSchemeModeChanged()` · 895 | Ajusta checkboxes al alternar radio. En doble marca A/B/C pero no desmarca expresamente D en esta función. Lee DOM y no modifica matriz aún. |
| `handleApplySchemeConfig(event)` · 909 | Valida bloques, crea franjas 4/6/8h y matriz simple/parejas. La confirmación de éxito está encadenada a la promesa de publicación y no aparece cuando rechaza. No implica acuse de los dispositivos. |
| `applySingleTurnPreset()` · 983 | Abre el configurador con SINGLE_4H; no aplica inmediatamente una matriz. |
| `applyDoubleTurnPreset()` · 987 | Abre el configurador con DOUBLE_8H; mismo criterio. |
| `publishPacSchedule(silent=false)` | Exige requireAdmin y documento PAC cargado válido; mezcla sus campos con matriz/franjas/listas y versión nueva. Comprueba upsert, actualiza loadedPacConfig y retorna true. Catch informa y **relanza**; finally restaura botón. Los await de aprobación/fusión ya no continúan tras ese fallo y las llamadas de UI manejan el rechazo. No aporta concurrencia/transacción entre operaciones previas. |

### Algoritmo de `setCellBlock`, explicado paso a paso

1. Asegura la fila seleccionada y toma `slotIdx`, `dayIdx`, `syncPair` y `propagateScale`.
2. Con propagación y bloque distinto de `-`, limpia cada día antes de reconstruirlo. En esquema doble divide filas en pares y usa el ciclo C/A/B; incorpora el bloque elegido si no estaba. Calcula diferencias modularmente para anclar la celda seleccionada. En simple usa ciclo A/B/C/D y distribuye bloques desde el turno seleccionado con desplazamiento diario.
3. Con pareja y sin propagación, escribe el bloque en la fila elegida y en `(slotIdx + floor(totalSlots/2)) % totalSlots`. Elimina otras apariciones de ese mismo bloque ese día para dejar exactamente dos posiciones.
4. En el resto de casos cambia solo la celda.
5. Cierra modal, vuelve a dibujar, actualiza franja activa y llama a publicación. Esta última no bloquea el resto de la interfaz ni deshace cambios locales si falla.

Ejemplo: con seis franjas, elegir la fila 1 y activar pareja modifica filas 1 y 4. Si se han personalizado a duraciones desiguales, la relación de filas sigue siendo esa aunque la diferencia real deje de ser doce horas.

## 7. Franjas: crear, editar y eliminar

| Función y línea | Contrato y comportamiento |
|---|---|
| `openEditSlotModal(slotIdx)` · 1037 | Lee slot; si no existe retorna. Copia índice/etiqueta/horas a inputs y abre modal. No clona un borrador de toda la matriz. |
| `closeEditSlotModal()` · 1047 | Oculta modal; los inputs siguen en DOM hasta la siguiente apertura. |
| `handleSaveSlot(event)` · 1051 | Convierte horas a enteros, cambia slot si existe, cierra, repinta y publica. No verifica aquí solapamientos, huecos ni que inicio y fin sean diferentes. |
| `openAddSlotModal()` · 1069 | Muestra formulario de nueva franja. |
| `closeAddSlotModal()` · 1073 | Oculta formulario sin alterar listas. |
| `onNewSlotDurationChanged()` · 1077 | Si duración no es custom calcula `fin=(inicio+duración)%24`, escribe input fin y regenera etiqueta. No crea aún un slot. |
| `onNewSlotTimeChanged(isEnd=false)` · 1087 | Lee inicio/fin y construye etiqueta HH:00 a HH:00 con padding. El parámetro `isEnd` está declarado pero no interviene en el cuerpo. |
| `handleAddSlot(event)` · 1098 | Lee bloques y estrategia. Sin bloques usa C. ROTATE alterna seleccionados por día; FIXED_X rellena X. Añade un slot y una fila, repinta/publica. Otra estrategia conserva `-`. No inserta ordenadamente por hora. |
| `deleteSlot(slotIdx)` · 1136 | Impide dejar cero franjas, pide confirmación, elimina slot y fila y renumera índices. Repinta/publica. El índice de cada fila deja de identificar permanentemente la franja eliminada. |

## 8. Telemetría y asignación a bloques

Aplicar el PAC a sectores representa una decisión administrativa basada en un horario: no confirma físicamente un corte. Esta distinción explica por qué los estados pueden cambiar sin un reporte ciudadano nuevo.

| Función y línea | Entradas, salida y efectos |
|---|---|
| `getActiveBlockNow()` · 1152 | Sin argumentos devuelve `{block,timeLabel,slotIdx,rotatedSlot}`. Convierte hora a Caracas, busca intervalo con fin exclusivo, aplica desplazamiento por día de mes y lee bloque. Si faltan datos usa C/defaults. Para madrugada usa columna del día actual aunque la franja comenzó ayer. |
| `updateTelemetryActiveCut()` · 1198 | Consume el resultado anterior y actualiza bloque/etiqueta del resumen. Se ejecuta por intervalo de 30 s y tras lecturas/ediciones; no modifica sectores. |
| `applyActivePacCutToSectors()` · 1216 | Tras confirmar recorre sectores: omite averías, marca sin luz al bloque teórico activo y NORMAL al resto. Envía updates secuenciales y cambia memoria. No comprueba cada `{error}`; una respuesta de éxito visual puede ser parcial. |
| `setTelemetryFilter(filter)` · 1259 | Guarda filtro ALL/bloque/IRREGULAR, ajusta clases y repinta. No filtra en servidor. |
| `renderTelemetryList()` | Filtra por nombre/circuito y bloque/tipo, construye filas escapadas con estado, voltaje y acciones delegadas. Usa el helper global `isSectorCommunity`; no es necesario abrir esta vista antes que el directorio o mapa. |
| `isSectorCommunity(s)` | Helper global disponible desde el arranque. Devuelve Boolean: overrides por ID, por nombre, campo `isCommunity` y finalmente prefijos del ID. `Boolean('false')` sería true si el servidor entregara texto en lugar de Boolean. Su contrato esperado es dato tipado. |
| `updateSectorStatus(sectorId,newStatus,voltage)` · 1385 | Actualiza tres campos y timestamp por ID. No cambia contadores ni comprueba error devuelto; normalmente la recarga Realtime repinta la fila. |
| `resetAllTelemetryToNormal()` · 1398 | Pide confirmación y actualiza todos a NORMAL/118/porcentaje 0/contador 0. La frase «limpiar reportes» no equivale a DELETE de `citizen_reports`: el cuerpo solo modifica `sectors`. |
| `setAllSectorsStatus(status)` · 1420 | Confirma y recorre todas las filas, fijando voltaje 118 si NORMAL y 0 en otro caso. Conserva contadores. No es una operación atómica. |
| `renderBlockKanban()` · 1438 | Construye tarjetas de nombres de las cuatro listas y botones mover/quitar. Busca clasificación comunitaria en directorio. No reconstruye las listas automáticamente desde `rotationBlock`. |
| `reassignSector(sectorName,fromBlock,toBlock)` · 1486 | Ignora destino vacío/igual; quita de origen y añade a destino. Busca fila por nombre, lanza update de bloque sin esperar/comprobar y publica el PAC completo. Las dos escrituras pueden divergir. |
| `removeSectorFromBlock(sectorName,block)` · 1510 | Confirma, quita nombre de esa lista y publica. No borra el sector ni vacía su `rotationBlock` remoto. |
| `openAddSectorToBlockModal()` · 1522 | Abre formulario de asignación. |
| `closeAddSectorModal()` · 1526 | Oculta ese formulario. |
| `handleAddSectorToBlock(event)` · 1530 | Lee nombre/bloque, añade sin repetición a la lista y construye ID con nombre normalizado. Crea un builder upsert sin await ni then: esa petición de alta no llega a despacharse. Sí publica el PAC por otro camino. Además el ID por nombre puede colisionar; ver la tarjeta local no confirma que exista una fila remota. |

## 9. Detección y fusión de nombres similares

El clustering es una heurística textual local. No demuestra que dos sectores sean el mismo lugar ni usa municipio como barrera obligatoria. Una sugerencia necesita revisión humana antes de fusionar.

| Función y línea | Qué recibe/devuelve y cálculo |
|---|---|
| `normalizeSectorText(str)` · 1562 | Devuelve texto minúsculo sin diacríticos, puntuación ni espacios repetidos. Vacío/nulo produce cadena vacía. Es base de similitud; no crea un ID estable. |
| `getSignificantTokens(normStr)` · 1578 | Devuelve palabras sin términos genéricos de dirección; si todas se eliminan vuelve a las originales. Espera texto ya normalizado. |
| `levenshteinDistance(s1,s2)` · 1584 | Devuelve número mínimo de inserciones, borrados y sustituciones. Construye matriz `(m+1)*(n+1)`; costo en tiempo/memoria proporcional al producto de longitudes. |
| `computeLevenshteinSimilarity(s1,s2)` · 1607 | Devuelve `1 - distancia/max(longitudes)`. Dos cadenas vacías dan 1. |
| `computeTokenJaccardSimilarity(tokens1,tokens2)` · 1613 | Devuelve tamaño intersección/unión de conjuntos; no cuenta repetición de palabras. Dos conjuntos vacíos dan 1. |
| `computeSectorSimilarity(nameA,nameB)` · 1624 | Normaliza, devuelve 1 si iguales, 0 si alguno vacío, 0.85 en cierta inclusión de subcadena; de lo contrario mezcla 40% Levenshtein y 60% Jaccard. El resultado es similitud textual, no probabilidad geográfica. |
| `analyzeTypoClusters()` | Combina sectores/propuestas etiquetando cada copia con `_source` según la lista de origen; esa etiqueta sustituye cualquier campo del dato remoto. Compara candidatos y agrupa si similitud ≥0.68. Visitados usa origen+ID, por lo que un ID de propuesta igual al de un sector no confunde sus identidades. Sugiere nombre/datos y actualiza badge/render. Comparación cuadrática; no es agrupación transitiva completa. |
| `updateClusterBadge(count)` · 1714 | Escribe conteo si existe badge. No recalcula grupos. |
| `toggleClusteringPanel()` · 1719 | Alterna visibilidad; al abrir vuelve a analizar y desplaza la página al panel. |
| `renderTypoClusters()` | Construye tarjetas con variantes y campos canónicos editables, bloque/parroquia sugeridos y botón de fusión. Escapa datos y atributos; la acción usa data-admin-action. La similitud textual sigue requiriendo revisión humana. |
| `upsertSectorSafe(sectorPayload)` · 1808 | Promesa con `{data,error:null,fallbackUsed}` o excepción. Intenta upsert completo; si identifica error de columnas/caché reintenta solo campos base y omite territorio/notas/coordenadas/clasificación. Un fallback exitoso es guardado parcial. |
| `insertApprovedCommunitySector(sectorPayload)` | Async: exige administración y crypto.randomUUID disponible, genera un ID nuevo con prefijo admin_ que sustituye cualquier id del payload y hace INSERT en sectors. Comprueba error y devuelve el objeto insertado, o rechaza. Una colisión falla: nunca actualiza una fila existente. Lo llaman aprobación individual y fusión sin sector establecido; no usa fallback parcial de esquema. |
| `mergeAndApproveCluster(clusterId)` | Busca grupo/inputs y exige rol y PAC cargado antes de mutar. Separa activeItems de pendingItems por origen fijado localmente. Si hay sector establecido, su ID es el target del upsert explícito; si todas son propuestas, crea ID mediante insertApprovedCommunitySector. Solo los duplicados provenientes de sectors pueden borrarse de sectors, por ID exacto; no borra por nombre ni usa ID de propuesta como sector. Actualiza memoria/PAC y únicamente después de publicar elimina solicitudes por ID de community_locations. Comprueba errores de borrado y no anuncia éxito tras fallo. No es transacción ni reasigna reportes históricos. |

Para mantener esta parte hay que revisar también la integridad de referencias: fusionar sectores modifica filas del directorio y nombres en PAC, mientras los reportes existentes conservan sus `sectorId`. Un cambio de nombre no debe confundirse con cambiar identidad.

## 10. Geocodificación y aprobación comunitaria

| Función y línea | Contrato, red y persistencia |
|---|---|
| `validateWithOSM(query,safeId)` · 1992 | Limpia consulta, busca en cache y llama Nominatim con «Barinas, Venezuela». Devuelve resultado con found/coordenadas/dirección, `{found:false}` o null por error; actualiza badge. No guarda coordenadas en Supabase. |
| `renderOsmResult(safeId,result)` · 2047 | Representa enlace/coordenadas si found; si no, «Sin match exacto». `safeId` identifica el nodo `osm_badge_*`. No consulta red. |
| `validateMasterSectorsWithOSM()` · 2067 | Selecciona sectores sin coordenadas o primeros diez si todos tienen; confirma, consulta cada uno y usa centro municipal como fallback. Guarda con `upsertSectorSafe`, espera 1.1 s entre sectores y muestra conteos. El fallback no es ubicación precisa. No hay transacción global. |
| `validateAllWithOSM()` · 2155 | Alias async que retorna el resultado de `validateMasterSectorsWithOSM`; conserva compatibilidad con llamadas antiguas. |
| `renderCommunityTable()` | Crea filas editables con fecha `submittedAt`. Escapa ID, nombre, notas y valores de inputs; aprobar/rechazar/buscar OSM usan acciones delegadas con el ID original como dato. No inserta código procedente de la propuesta ciudadana. |
| `approveCommunityLocation(id)` | El parámetro identifica exclusivamente la solicitud. Exige rol y PAC cargado antes de leer campos/mutar. Lee edición/bloque y crea un sector mediante insertApprovedCommunitySector, con ID generado por el administrador distinto al ciudadano. Actualiza listas/override con ese nuevo ID, espera publicación PAC y después borra la solicitud original comprobando error. Un fallo de inserción o publicación conserva la cola y muestra error. La operación sigue siendo parcial, sin transacción entre las tres escrituras. |
| `rejectCommunityLocation(id)` · 2331 | Confirma, hace DELETE de solicitud, comprueba error, la quita de memoria y actualiza clusters. No elimina un sector activo que Android hubiera insertado previamente. |

## 11. Avisos, reportes, catálogo y donaciones

| Función y línea | Comportamiento y límites |
|---|---|
| `renderActiveBroadcast(notice)` · 2349 | Muestra título/mensaje con `textContent`, fecha y colores INFO/WARNING/EMERGENCY. No comprueba active: el llamador lo decide. No envía push. |
| `hideActiveBroadcast()` · 2391 | Oculta contenedor local; no desactiva aviso en servidor. |
| `handleDismissBroadcast()` | Tras confirmar exige administración y hace upsert con active false/timestamp, comprobando error antes de ocultar o avisar. Si falla mantiene visible el aviso y comunica error. Reemplaza el JSON con un contenido reducido. |
| `handleSendBroadcast(event)` | Lee título/mensaje/nivel, exige administración y escribe aviso activo. Comprueba error antes del éxito y de limpiar inputs; si falla conserva el borrador. No llama FCM: su lectura/suscripción pertenece al flujo de avisos en configuración. |
| `testGlobalPushAlert()` | Confirma un aviso global y llama `adminAuth.postJson('/api/sendPushAlert', {title,message,is_alarm:true})`. El controlador valida identidad/rol y adjunta el Bearer vigente. Comprueba `success`, muestra `messageId` o el error HTTP y siempre restaura el botón en `finally`. No envía `block` ni `topic`: la audiencia es global. Las cuotas y la autorización definitiva se comprueban en servidor. |
| `setReportFeedFilter(filter)` · 2479 | Guarda ALL/NORMAL/OUTAGE/IRREGULAR, ajusta estilos y llama render. |
| `renderCitizenReportsFeed()` | Cuenta categorías sobre los 50 cargados, filtra por tipo y texto de sector/ID/observación y crea tarjetas. Usa timestamp remoto y escapa las observaciones y nombres antes del HTML. Los tres pasos —contadores, filtros y tarjetas— clasifican las variantes irregulares de reportType/report_type/status con PacMapCore.normalizeStatus antes de hasPower, de modo que BAJON con suministro presente sigue siendo falla irregular. voltageLabel conserva cero y muestra ausencia como “Sin tensión reportada”; no sustituye un valor falso por 118 V. Las copias raíz/public son idénticas. |
| `seedAllBarinasCatalog()` · 2796 | Escribe geografía, recorre catálogo embebido y publica PAC. Introduce estados/contadores predeterminados como 18/95 o 3/0. Puede sobrescribir filas con IDs iguales y no comprueba cada error devuelto. Es una operación de datos, no un botón inocuo de refresco. |
| `saveDonationConfig()` · 2847 | Lee cinco campos y hace upsert `donations_config`, comprobando error. Cambia instrucciones de pago, no procesa una transacción financiera. |
| `loadDonationUrl()` | Lee `donations_config` y precarga inputs presentes. `renderAdminAccess` la llama al abrir la sesión autorizada. Conserva el nombre antiguo y todavía captura errores sin mostrarlos. |

Los arrays `BARINAS_MUNICIPALITIES` y `MASTER_BARINAS_SECTORS` son datos embebidos: territorio, circuitos y estados usados por el seed y formularios. No se actualizan automáticamente desde una fuente oficial. Las etiquetas «oficial» o «en vivo» del HTML no certifican por sí mismas el origen ni la vigencia.

## 12. Directorio y mapa maestro

El panel y Android usan el mismo contrato `PacMapCore` y el mismo `pac-map.css`. `admin.html` carga ambos antes de `app.js`. Los pines conservan el color del bloque y añaden una insignia de estado: una falla irregular es morada con `⚠`; los datos no reconocidos son grises. Las referencias aproximadas tienen borde punteado, texto explicativo y agrupación cuando coinciden. Los popups se construyen como nodos DOM y conservan los IDs en `dataset`, con el botón Editar atendido por la delegación administrativa existente.

El núcleo no consulta servidores. `installPacMapCore` instala la API creada por `createPacMapCore` en `globalThis` y CommonJS. `textKey` compara nombres sin tildes; `escapeHtml` hace inertes textos remotos; `normalizeBlock` admite solo A–D; `normalizeStatus` agrupa IRREGULAR_OUTAGE/FALLA_IRREGULAR/IRREGULAR/AVERIA/BAJON antes de representar el estado. `statusInfo` devuelve `{status,label,color,symbol}`; un estado desconocido no implica suministro normal. `validCoordinatePair` valida presencia, finitud y rangos; `coordinates` devuelve `{point,approximate,label}` a partir de coordenadas registradas, coincidencia exacta de nombre o centro municipal/Barinas. No usa coincidencias parciales ni hash para inventar una dirección.

`locationGroups` conserva todos los sectores de una misma referencia; `representativeSector` prioriza seleccionado, falla, corte y primero; `markerHtml` usa colores/enums validados y contador numérico. `heatPoints` incluye solo sectores con cortes/fallas y coordenadas registradas, con peso 1: el resultado es densidad de sectores, no cantidad de reportes. `voltageLabel` conserva cero y muestra ausencias como “Sin tensión reportada”, sin sustituirlas por 118 V. Las tablas locales no son delimitaciones de circuitos. Las imágenes del fondo se solicitan directamente al proveedor y su atribución permanece visible. El detalle del contrato compartido está también en el apartado 9 del capítulo Android.

| Función y línea | Contrato y efectos |
|---|---|
| `handleFilterMasterTypeChange(val)` · 2891 | Guarda ALL/COMMUNITY/OFFICIAL y repinta directorio; expuesta en window para HTML. |
| `getSectorCoordinates(sector)` | Wrapper de `PacMapCore.coordinates(sector).point`, usado por mapa y enlaces del directorio. La precisión se explica en el popup, sin hash de ubicación. |
| `applyBasemap(key)` | Valida la base solicitada, retira capas anteriores y añade Esri oscuro, OSM invertido o satélite/etiquetas. Actualiza rótulo; usa panes normales de Leaflet en lugar de llamar al inexistente `LayerGroup.bringToFront`. Los proveedores externos conservan sus condiciones de uso. |
| `changeMapBasemap(key)` · 3029 | Wrapper de UI que delega en `applyBasemap`. |
| `initBarinasMasterMap()` | Comprueba contenedor y Leaflet, reutiliza mapa con `invalidateSize` o crea uno centrado en Barinas, con atribución. Instala click que abre alta con coordenadas, crea grupo y nota accesible sobre precisión/densidad; no duplica mapa ni nota. |
| `focusBarinasMapHome()` · 3069 | Vuelve al centro/zoom 10 si mapa existe. No cambia filtros. |
| `focusSectorOnMap(lat,lon,name,attempt=0)` | Valida coordenadas. Si no hay mapa abre pestaña y reintenta hasta cuatro veces; si existe centra a zoom 14, abre popups cercanos y desplaza al contenedor. `name` se conserva por compatibilidad, sin buscar por texto. El fallo de inicialización ya no crea un bucle de reintentos indefinido. |
| `updateBarinasMapMarkers()` | Filtra por bloque normalizado, agrupa coordenadas coincidentes y crea pines/DOM seguros. La falla irregular y sus variantes se ven en insignias y popups. Calor usa solo coordenadas registradas con corte/falla; si falta el plugin muestra pines y lo explica. No representa normales como calor ni inventa tensión o ubicación precisa. |
| `toggleMapHeatmapMode()` · 3204 | Alterna Boolean, cambia botón y reconstruye capas. No modifica estados remotos. |
| `handleMapBlockFilterChange(val)` · 3219 | Guarda filtro exclusivo del mapa y actualiza marcadores; no cambia necesariamente filtros del directorio. |
| `initMasterFilterDropdowns()` · 3224 | Rellena municipios si select todavía tiene como máximo una opción; prepara municipios/parroquias del modal. Evita añadir todas las opciones repetidamente. |
| `handleFilterMasterMunChange(mun)` · 3249 | Guarda municipio, reconstruye parroquias, reinicia parroquia ALL y repinta. |
| `handleFilterMasterChange()` · 3270 | Lee selects de bloque/parroquia y repinta con sus nuevos valores. |
| `handleSearchMasterInput(q)` · 3278 | Recorta y pone en minúsculas el texto, guarda y repinta sin consultar servidor. |
| `setDirectoryViewMode(mode)` · 3283 | Guarda cards/table, cambia botones y repinta. |
| `renderGeneralSectorsMaster()` · 3299 | Calcula contadores globales, filtra por bloque, territorio, tipo y texto en varios campos, ordena por nombre y delega en cards/table. El conteo de municipios usa 12 como sustituto si resulta cero. |
| `renderWikipediaCardsView(list,container)` · 3379 | Recibe lista ya filtrada y nodo; construye fichas con clasificación, estado, territorio, mapa, notas y acciones. Calcula coordenadas con el helper; no es una integración con Wikipedia. |
| `renderDetailedTableView(list,container)` | Misma lista/destino; produce tabla compacta con enlaces/acciones. Ambas vistas escapan datos ciudadanos y atributos; editar, borrar y enfocar usan datos y callbacks delegados. El mapa sigue usando coordenadas reales o aproximadas según el helper. |

## 13. Alta, edición y borrado de sectores

| Función y línea | Contrato y pasos |
|---|---|
| `editSector(id)` · 3604 | Abre modal llamando a `openCreateSectorModal(null,null,id)`. No cambia datos por sí sola. |
| `openCreateSectorModal(lat,lon,editId)` · 3611 | Acepta coordenadas para alta o ID para edición; admite además un único string como ID. Busca por ID/nombre, precarga territorio/estado/clasificación y botones; si no encuentra, cae en modo nuevo. No persiste. |
| `onclick` de borrar · 3693 | Callback del modal que captura ID y nombre de la fila editada y llama `deleteMasterSector`. En alta se sustituye por null. |
| `closeCreateSectorModal()` · 3731 | Oculta modal sin escritura ni reversión. |
| `handleModalMunicipioChange(mun)` · 3736 | Sustituye las opciones de parroquia con las del municipio embebido. No consulta catálogo remoto. |
| `handleGeocodeForManualSector()` · 3751 | Requiere nombre; busca nombre/parroquia/municipio en Nominatim y escribe inputs lat/lon. Si no encuentra usa centro del municipio y lo indica. No guarda hasta enviar formulario. |
| `handleSaveManualSector(e)` · 3796 | Lee campos y valida nombre. Mantiene ID al editar o genera nombre+últimos dígitos de hora al crear. Guarda override local antes de red, usa upsert seguro, actualiza listas/directorio/mapa y publica PAC. Recalcula contadores a valores prefijados; `parseFloat(voltage)||118` reemplaza un cero legítimo por118. |
| `deleteMasterSector(id,name)` · 3900 | Resuelve fila y confirma. Oculta ID localmente, intenta borrar por ID y nombre en dos tablas, elimina memoria/listas y publica. No comprueba errores Supabase en borrados; puede mostrar eliminación que solo funcionó localmente. Borrar por nombre puede alcanzar homónimos. |

## 14. Asistente de esquema y migración

| Función y línea | Qué hace realmente |
|---|---|
| `openDbMigrationHelper()` · 3958 | Muestra modal y llama la comprobación de columnas. No ejecuta SQL por sí misma. |
| `closeDbMigrationHelper()` · 3966 | Oculta asistente. |
| `checkSchemaCacheStatus()` · 3971 | Hace SELECT limitado de ciertas columnas y representa diagnóstico. El mensaje «100% sincronizados» solo prueba esa consulta; no comprueba isCommunity, políticas, Realtime ni integridad completa. |
| `copyMigrationSql()` | Async: copia únicamente la referencia `supabase_migration_v3_admin_security.sql` o abre un prompt como alternativa al portapapeles. El asistente indica aplicación manual por el responsable de base de datos. No contiene una política anónima de escritura ni ejecuta SQL. La migración, el alta administrativa y las variables de servidor se explican en el capítulo 06. |
| `runClientSideLegacyMigration()` · 4082 | Confirma, recorre sectores cargados y rellena territorio/notes/barrio cuando detecta algunos faltantes, guardando con upsert seguro. No crea columnas ni recarga caché. Su condición no detecta todos los casos de nulos/vacíos. |

## 15. Eventos sin nombre y conexiones entre vistas

Los callbacks de `forEach`, `map`, `filter`, `.then`, Realtime y listeners se entienden dentro de la función que los registra. No son procesos permanentes en servidor. Los de render capturan el elemento/índice actual; los de Realtime vuelven a consultar; los de online/offline actualizan presentación. `window.*` permite llamar funciones desde atributos HTML, pero expone operaciones en el navegador y no aporta autorización.

`setInterval(updateTelemetryActiveCut,30000)` vive mientras el documento está abierto. `DOMContentLoaded` inicia acceso y registra la delegación de click/change. Los formularios/editables dependen de IDs acordados entre HTML y JS; cambiar uno exige actualizar ambos. El botón de reconexión llama ahora a `retryFirebaseConnection`.

## 16. Ejemplos para mantenerlo sin adivinar

**Cambiar solo una celda:** localizar `renderMatrixTable` → callback `onclick` → `openCellModal` → `setCellBlock` → `publishPacSchedule`. Antes de probar verificar toggles de pareja y escala, porque pueden modificar más celdas. Confirmar después el JSON persistido, no solo la alerta.

**Añadir un campo de sector:** crear migración, actualizar formulario `admin.html`, carga de modal, `handleSaveManualSector`, `upsertSectorSafe` y su fallback, render de fichas/tabla/mapa y DTO Android si debe viajar allí. Si el fallback lo omite, una escritura puede aparentar éxito pero perder el campo.

**Investigar un sector que reaparece:** revisar respuesta de DELETE, permisos, listas A–D y `pac_deleted_sector_ids`. La lista local puede estar ocultando el problema. No borrar las preferencias de un usuario como sustituto de corregir servidor.

**Cambiar mapa:** diferenciar reglas compartidas (`PacMapCore`), capas (`applyBasemap`) y representación (`updateBarinasMapMarkers`). Mantener el núcleo y CSS idénticos entre web y Android. El centro municipal o una referencia por nombre son aproximaciones; no tratarlos como límites reales de un circuito.

**Modificar donaciones:** `saveDonationConfig` escribe los cinco campos; Android lee `donations_config`. `renderAdminAccess` inicia la precarga mediante `loadDonationUrl`. Al añadir un campo, actualizar tanto guardado como carga y consumidores; que el input exista no garantiza su persistencia.

## 17. Cobertura y límites

Este capítulo describe las declaraciones propias de ambas copias idénticas de `app.js`, incluidos los cinco fetch locales, el helper global de clasificación y callbacks `onclick`. El capítulo 04 documenta las funciones del núcleo compartido y las pruebas. Las funciones vacías y los parámetros sin uso se mantienen descritos como tales; las dependencias CDN/Leaflet/Supabase se explican desde sus llamadas, sin reproducir su implementación interna.

Las pruebas locales de [admin-ui.test.cjs](https://github.com/Ralag/pac-barinas/blob/7d5b4b57bc36ff9eb62df2ff6fb94945407ce031/tests/admin-ui.test.cjs) ejercitan autenticación, expiración, revocación de rol, Bearer renovado, lectura PAC sin escrituras, rechazo de publicación fallida, cargas HTML inertes y acciones delegadas. Usan SDK y DOM simulados, no un navegador real ni servicios activos. `npm test` ejecuta también las pruebas del endpoint; el registro de cierre figura en el capítulo 04. La compilación no despliega el producto.

Los cambios implementados cierran el acceso local por contraseña incrustada, retiran las escrituras de recuperación tras fallar una lectura y propagan el fallo al publicar. Aún requieren seguimiento el tratamiento de `{error}` en otras operaciones heredadas, la atomicidad de aprobaciones/fusiones/borrados, las coordenadas aproximadas y la concurrencia al sustituir un JSON PAC completo. La protección en producción depende de las políticas y configuración del servidor, cuyo estado no se certifica en este capítulo.

Una aprobación puede insertar el sector y luego fallar al publicar o retirar la propuesta. La solicitud permanece disponible cuando falla la publicación; el sector recién creado puede existir ya. Antes de repetir, revisar directorio y PAC para evitar duplicados. La fusión puede modificar un target o borrar duplicados establecidos antes de un fallo posterior. Convertir estos pasos en una operación transaccional con idempotencia requiere un contrato de servidor adicional; no se simula una reversión automática en el cliente.
## Publicación coherente de semana y cronograma de hoy

`initFirebaseSync.fetchPac` valida primero los datos remotos y calcula `pacEditorWeekOffset` en Caracas. Proyecta la base para editar la semana actual. `publishPacSchedule` deshace esa proyección antes de guardar matrix/matrixRows, conserva asignaciones y campos previos, valida todo y utiliza `nextRevision` para version/updatedAt. El mensaje confirma publicación; no asegura que todos los teléfonos hayan descargado ya.

`renderMonthlyProjection` calcula cada semana con desplazamiento `semanaObjetivo-editorOffset`. `applyMonthlyWeekAsBase` utiliza el mismo cálculo antes de convertir de nuevo a base al publicar; así no suma una segunda rotación. La semana señalada como actual es la que corresponde al editor, no siempre la primera del mes.

`getActiveBlockNow` conserva su contrato de objeto `{block,timeLabel,slotIdx,rotatedSlot}` y utiliza el núcleo compartido para el corte activo, incluidos noche y cambio de mes. Si no hay bloque publicado devuelve guion. La acción de sincronizar sectores rechaza ese caso en lugar de marcar a todos como normales. Mantener el contrato de objeto es necesario para el panel de telemetría y sus callbacks.

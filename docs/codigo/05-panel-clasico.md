# 05. Panel clásico: HTML, JavaScript y administración

**Corte del código:** 15 de octubre de 2026. Este capítulo explica `admin-web/admin.html`, `admin-web/public/app.js`, su copia `admin-web/app.js` y las hojas de estilo clásicas. Las líneas indicadas corresponden a `public/app.js`, salvo indicación contraria. El [índice de funciones](07-indice-funciones.md) permite localizar cada declaración.

## 1. Qué aplicación es esta y qué archivos carga

El panel clásico es una página HTML con nueve pestañas. El navegador carga `admin.html`, estilos, Tailwind por CDN, Leaflet y su extensión de calor, el SDK web de Supabase y finalmente `app.js`. Cada `id` del HTML es un punto que JavaScript busca mediante `getElementById`. Los atributos `onclick`, `onchange` y `onsubmit` llaman funciones visibles en el ámbito global del navegador.

Vite construye `admin.html` como entrada adicional y copia `public/app.js` a la salida. Por eso la copia distribuida como `dist/app.js` procede de `public/app.js`. El archivo `admin-web/app.js` también existe y sirve al abrir la carpeta por ciertos servidores simples, pero no es la fuente de la copia estática de Vite. No se debe corregir solamente esa copia raíz esperando que cambie el panel compilado.

Las dos copias tienen las mismas funciones, con cuatro diferencias observadas: `renderCitizenReportsFeed` en `public` reconoce también `FALLA_IRREGULAR`; `updateBarinasMapMarkers`, `renderWikipediaCardsView` y `renderDetailedTableView` usan `-` como bloque ausente en `public`, mientras la raíz asume C. Los índices y las explicaciones siguientes cubren ambas, con esas diferencias explícitas.

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
| `luz_admin_session` | Bandera de acceso | `localStorage`; no es una sesión autenticada en servidor. |
| `pac_community_overrides` | Clasificación manual por ID o nombre | Solo ese navegador; puede diferir de otros administradores. |
| `pac_deleted_sector_ids` | IDs que ese navegador deja de mostrar | Ocultar localmente no demuestra que el borrado remoto funcionó. |

Al llegar `DOMContentLoaded`, `checkAuth` decide qué contenedor mostrar. Si hay acceso local, llama a `initFirebaseSync`, que a pesar de su nombre trabaja con **Supabase**. Realiza lecturas iniciales y suscribe canales. Ante un evento, vuelve a consultar la tabla y reconstruye las vistas; no aplica solo el cambio recibido. Un temporizador independiente actualiza la franja activa cada 30 segundos.

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
| `tabDonations` | Campos PayPal, Binance y Pago Móvil | `saveDonationConfig`; la carga inicial no está conectada. |

## 4. Contratos comunes de las funciones

Salvo que una ficha indique retorno explícito, una función normal devuelve `undefined` y una `async` resuelve una promesa sin dato útil; su resultado visible es un cambio en DOM, memoria o servidor. Los formularios reciben el evento y usan `preventDefault()` para evitar una recarga. Abrir o cerrar un modal no guarda información. Las funciones `render*` reconstruyen HTML a partir del estado actual, normalmente sin consultar red.

Las llamadas Supabase suelen devolver `{ data, error }`. En varios procedimientos de este panel solo hay `try/catch`: eso no convierte automáticamente un `error` devuelto en una excepción. Por esa razón algunas fichas advierten que el mensaje mostrado no confirma una escritura.

## 5. Acceso, navegación, conexión y lecturas

| Función y línea | Entradas, resultado, pasos y relación con el sistema |
|---|---|
| `checkAuth()` · 102 | Lee la bandera `luz_admin_session`. Muestra panel o formulario y, en el primer caso, inicia sincronización. Sin sesión enfoca el campo después de 100 ms. La llama DOMContentLoaded. No consulta una identidad remota. |
| `handleLogin(event)` · 117 | Lee el campo, recorta y convierte a minúsculas, compara con alternativas incrustadas, guarda bandera local y arranca sync; si falla muestra error. No se reproducen aquí esas contraseñas. No genera token administrativo. |
| `handleLogout()` · 136 | Borra la bandera y recarga la página. La recarga reinicia memoria y conexiones del documento. No revoca una sesión de Supabase porque este mecanismo no la crea. |
| `switchTab(tabId)` · 142 | Oculta todos los `.tab-content`, quita selección y muestra el `id` elegido. Al abrir directorio difiere 60 ms la inicialización de mapa/render; al abrir reportes repinta feed. La invocan botones HTML. No cambia la URL ni usa React. |
| `initVercelLogsSync()` · 171 | Busca 20 reportes y escucha INSERT para añadir filas a la consola. Usa los nombres `reportedAtMillis` y `voltageObserved`, diferentes del esquema remoto actual, y concatena datos en HTML. No son logs de Vercel. La inicia `initFirebaseSync`. |
| `clearVercelConsole()` · 220 | Reemplaza el contenido local de la consola por una frase de limpieza. No borra reportes ni registros en servidores. |
| `pingVercelApi()` · 225 | Llama por GET a `/api/getMonthlyWeeks?monthName=Test&totalDays=30`, representa éxito HTTP o detalle de error. Es una petición real al servidor cuando la usa un administrador; no mide todos los componentes del sistema. |
| `updateSyncStatus(status, message=null)` · 247 | Cambia texto y punto de color según connected/syncing/offline. Devuelve sin actuar si no encuentra los nodos. Es presentación; el estado conectado no prueba por sí solo la recepción de todos los canales. |
| `initConnectionMonitor()` · 265 | Instala listeners online/offline y muestra desconexión si `navigator.onLine` es falso. Al volver online llama a `retryFirebaseConnection`. No reinicia automáticamente todas las lecturas de datos. |
| `retryFirebaseConnection()` · 279 | Comprueba con `single()` si puede leer el PAC, lanza el `error` si existe y pinta conectado/desconectado. Nombre heredado; usa Supabase y no Firebase. El botón HTML llama por error a `retrySupabaseConnection`, que no existe. |
| `initFirebaseSync()` · 291 | Inicializa monitor, consola, cinco lecturas locales y sus canales. Las invoca sin esperar una finalización conjunta; puede haber actualizaciones en distinto orden. No llama a `loadDonationUrl`. Si se ejecuta repetidamente puede instalar conexiones/listeners otra vez. |
| `fetchPac()` · 295, local | Lee `app_config/pac_schedule`; acepta `matrixRows[].cells` o `matrix`, reconstruye slots con defaults y sustituye listas presentes. Repinta matriz, proyección y kanban. Si falla la lectura llama a `publishPacSchedule(true)` y después marca conexión exitosa: una lectura fallida puede provocar escritura. |
| `fetchSectors()` · 335, local | Lee todos los sectores; si la tabla está vacía invoca la carga de catálogo. Excluye IDs ocultos en este navegador, calcula contadores y repinta vistas/filtros/mapa. La llaman inicialización y eventos de `sectors`. Un borrado local puede disimular un fallo remoto. |
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
| `handleApplySchemeConfig(event)` · 909 | Valida al menos un bloque, crea slots de 4/6/8h, ordena bloques empezando por el elegido y construye matriz. Simple asigna una vez por día con desplazamiento; doble usa parejas de filas. Publica y muestra texto de éxito sin esperar comprobar la publicación final. |
| `applySingleTurnPreset()` · 983 | Abre el configurador con SINGLE_4H; no aplica inmediatamente una matriz. |
| `applyDoubleTurnPreset()` · 987 | Abre el configurador con DOUBLE_8H; mismo criterio. |
| `publishPacSchedule(silent=false)` · 991 | Deshabilita botón, construye JSON completo, hace upsert de una fila `app_config`, comprueba su error y restaura botón en finally. Silencioso elimina solo aviso de éxito. El catch informa pero no relanza ni retorna estado: la promesa resuelve undefined incluso al fallar. Por ello un llamador que haga await puede seguir y anunciar éxito. No espera confirmaciones de cada teléfono. |

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
| `renderTelemetryList()` · 1276 | Filtra por nombre/circuito y bloque/tipo, construye filas con estado, voltaje y botones de update. Define y expone el helper `isSectorCommunity`; si retorna antes por no haber filas, esa exposición puede no ocurrir todavía. |
| `isSectorCommunity(s)` · 1306, local expuesta | Devuelve Boolean: overrides por ID, por nombre, campo `isCommunity` y finalmente prefijos del ID. `Boolean('false')` sería true si el servidor entregara texto en lugar de Boolean. Su contrato esperado es dato tipado. |
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
| `analyzeTypoClusters()` · 1648 | Combina sectores/propuestas, compara cada candidato con siguientes y agrupa si similitud ≥0.68 y IDs distintos. Marca visitados, sugiere nombre largo y datos disponibles, actualiza badge/render. Comparación cuadrática; no es agrupación transitiva completa. |
| `updateClusterBadge(count)` · 1714 | Escribe conteo si existe badge. No recalcula grupos. |
| `toggleClusteringPanel()` · 1719 | Alterna visibilidad; al abrir vuelve a analizar y desplaza la página al panel. |
| `renderTypoClusters()` · 1729 | Construye tarjetas con variantes y campos canónicos editables, bloque/parroquia sugeridos y botón de fusión. Usa plantillas HTML; no constituye saneamiento de datos. |
| `upsertSectorSafe(sectorPayload)` · 1808 | Promesa con `{data,error:null,fallbackUsed}` o excepción. Intenta upsert completo; si identifica error de columnas/caché reintenta solo campos base y omite territorio/notas/coordenadas/clasificación. Un fallback exitoso es guardado parcial. |
| `mergeAndApproveCluster(clusterId)` · 1853 | Busca grupo, toma inputs, elige primer ID, reúne notas/barrio/coordenadas y guarda consolidado. Después oculta IDs duplicados localmente, intenta borrarlos y solicitudes, actualiza cuatro listas, repinta y publica. No es transacción: un fallo intermedio deja cambios parciales. No reasigna aquí reportes históricos a un ID canónico. |

Para mantener esta parte hay que revisar también la integridad de referencias: fusionar sectores modifica filas del directorio y nombres en PAC, mientras los reportes existentes conservan sus `sectorId`. Un cambio de nombre no debe confundirse con cambiar identidad.

## 10. Geocodificación y aprobación comunitaria

| Función y línea | Contrato, red y persistencia |
|---|---|
| `validateWithOSM(query,safeId)` · 1992 | Limpia consulta, busca en cache y llama Nominatim con «Barinas, Venezuela». Devuelve resultado con found/coordenadas/dirección, `{found:false}` o null por error; actualiza badge. No guarda coordenadas en Supabase. |
| `renderOsmResult(safeId,result)` · 2047 | Representa enlace/coordenadas si found; si no, «Sin match exacto». `safeId` identifica el nodo `osm_badge_*`. No consulta red. |
| `validateMasterSectorsWithOSM()` · 2067 | Selecciona sectores sin coordenadas o primeros diez si todos tienen; confirma, consulta cada uno y usa centro municipal como fallback. Guarda con `upsertSectorSafe`, espera 1.1 s entre sectores y muestra conteos. El fallback no es ubicación precisa. No hay transacción global. |
| `validateAllWithOSM()` · 2155 | Alias async que retorna el resultado de `validateMasterSectorsWithOSM`; conserva compatibilidad con llamadas antiguas. |
| `renderCommunityTable()` · 2160 | Crea filas con campos editables y botones aprobar/rechazar/buscar OSM, usando `submittedAt` para fecha. Escapa apóstrofos para ciertos atributos, lo cual no sanea HTML completo. |
| `approveCommunityLocation(id)` · 2228 | Lee edición desde DOM, normaliza bloque, actualiza listas/override, guarda sector activo con flag comunitario y elimina solicitud comprobando su error. Actualiza memoria y publica. Es una secuencia parcial, no una aprobación atómica garantizada. |
| `rejectCommunityLocation(id)` · 2331 | Confirma, hace DELETE de solicitud, comprueba error, la quita de memoria y actualiza clusters. No elimina un sector activo que Android hubiera insertado previamente. |

## 11. Avisos, reportes, catálogo y donaciones

| Función y línea | Comportamiento y límites |
|---|---|
| `renderActiveBroadcast(notice)` · 2349 | Muestra título/mensaje con `textContent`, fecha y colores INFO/WARNING/EMERGENCY. No comprueba active: el llamador lo decide. No envía push. |
| `hideActiveBroadcast()` · 2391 | Oculta contenedor local; no desactiva aviso en servidor. |
| `handleDismissBroadcast()` · 2396 | Tras confirmar hace upsert con active false y timestamp, luego oculta y avisa. No comprueba `{error}` y reemplaza el JSON con un contenido reducido. |
| `handleSendBroadcast(event)` · 2415 | Lee título/mensaje/nivel, escribe aviso activo y limpia inputs. No llama la función FCM; la entrega en app depende de lectura/suscripción Realtime. No verifica `{error}`. |
| `testGlobalPushAlert()` · 2442 | Confirma y envía POST JSON a `/api/sendPushAlert`; comprueba HTTP y `success`. Muestra messageId y restaura botón en camino normal. En excepción puede dejar el botón con texto «Enviando». La app actual carece del cliente FCM descrito antiguamente. |
| `setReportFeedFilter(filter)` · 2479 | Guarda ALL/NORMAL/OUTAGE/IRREGULAR, ajusta estilos y llama render. |
| `renderCitizenReportsFeed()` · 2495 | Cuenta categorías sobre los 50 cargados, filtra por tipo y texto de sector/ID/observación y crea tarjetas. Usa timestamp remoto. Las observaciones se interpolan como HTML; deben tratarse como entrada no confiable. La versión raíz no reconoce FALLA_IRREGULAR en las mismas condiciones. |
| `seedAllBarinasCatalog()` · 2796 | Escribe geografía, recorre catálogo embebido y publica PAC. Introduce estados/contadores predeterminados como 18/95 o 3/0. Puede sobrescribir filas con IDs iguales y no comprueba cada error devuelto. Es una operación de datos, no un botón inocuo de refresco. |
| `saveDonationConfig()` · 2847 | Lee cinco campos y hace upsert `donations_config`, comprobando error. Cambia instrucciones de pago, no procesa una transacción financiera. |
| `loadDonationUrl()` · 2862 | Lee esa configuración y precarga inputs presentes. Captura errores sin mostrarlos. El nombre es antiguo y actualmente no se encontró llamada de inicialización: entrar a la pestaña no garantiza precarga. |

Los arrays `BARINAS_MUNICIPALITIES` y `MASTER_BARINAS_SECTORS` son datos embebidos: territorio, circuitos y estados usados por el seed y formularios. No se actualizan automáticamente desde una fuente oficial. Las etiquetas «oficial» o «en vivo» del HTML no certifican por sí mismas el origen ni la vigencia.

## 12. Directorio y mapa maestro

| Función y línea | Contrato y efectos |
|---|---|
| `handleFilterMasterTypeChange(val)` · 2891 | Guarda ALL/COMMUNITY/OFFICIAL y repinta directorio; expuesta en window para HTML. |
| `getSectorCoordinates(sector)` · 2940 | Devuelve `[lat,lon]`: primero valores válidos, luego coincidencia con nombres conocidos y finalmente centro municipal más desplazamiento determinista por hash. Ese último valor es aproximado/artificial, no medición. |
| `applyBasemap(key)` · 2969 | Retira capas anteriores y añade Esri oscuro, OSM invertido o satélite/etiquetas. Actualiza rótulo y eleva marcadores. Consume tiles externos. Los comentarios de gratuidad no son garantía contractual de terceros. |
| `changeMapBasemap(key)` · 3029 | Wrapper de UI que delega en `applyBasemap`. |
| `initBarinasMasterMap()` · 3033 | Comprueba contenedor y Leaflet, reutiliza mapa con `invalidateSize` o crea uno centrado en Barinas. Instala click que abre alta con coordenadas, crea grupo de marcadores y renderiza. No duplica el mapa si ya existe. |
| `focusBarinasMapHome()` · 3069 | Vuelve al centro/zoom 10 si mapa existe. No cambia filtros. |
| `focusSectorOnMap(lat,lon,name)` · 3074 | Si no hay mapa abre pestaña y reintenta tras 150 ms; si existe centra a zoom 14, abre popups cercanos y desplaza al contenedor. `name` no se usa para buscar el marcador. Si el mapa no puede inicializarse, el reintento puede repetirse. |
| `updateBarinasMapMarkers()` · 3095 | Limpia grupo/capa calor, filtra bloque y crea calor o pines. Calor usa peso 1/0.25 y radio25; pines muestran estado y acciones. Usa coordenadas reales o aproximadas; no adapta intensidad a cantidad de reportes. |
| `toggleMapHeatmapMode()` · 3204 | Alterna Boolean, cambia botón y reconstruye capas. No modifica estados remotos. |
| `handleMapBlockFilterChange(val)` · 3219 | Guarda filtro exclusivo del mapa y actualiza marcadores; no cambia necesariamente filtros del directorio. |
| `initMasterFilterDropdowns()` · 3224 | Rellena municipios si select todavía tiene como máximo una opción; prepara municipios/parroquias del modal. Evita añadir todas las opciones repetidamente. |
| `handleFilterMasterMunChange(mun)` · 3249 | Guarda municipio, reconstruye parroquias, reinicia parroquia ALL y repinta. |
| `handleFilterMasterChange()` · 3270 | Lee selects de bloque/parroquia y repinta con sus nuevos valores. |
| `handleSearchMasterInput(q)` · 3278 | Recorta y pone en minúsculas el texto, guarda y repinta sin consultar servidor. |
| `setDirectoryViewMode(mode)` · 3283 | Guarda cards/table, cambia botones y repinta. |
| `renderGeneralSectorsMaster()` · 3299 | Calcula contadores globales, filtra por bloque, territorio, tipo y texto en varios campos, ordena por nombre y delega en cards/table. El conteo de municipios usa 12 como sustituto si resulta cero. |
| `renderWikipediaCardsView(list,container)` · 3379 | Recibe lista ya filtrada y nodo; construye fichas con clasificación, estado, territorio, mapa, notas y acciones. Calcula coordenadas con el helper; no es una integración con Wikipedia. |
| `renderDetailedTableView(list,container)` · 3500 | Misma lista/destino; produce tabla compacta con enlaces/acciones. Ambas vistas interpolan datos en HTML y comparten riesgos de escape. |

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
| `copyMigrationSql()` · 4031 | Copia texto SQL al portapapeles o abre prompt de fallback. El texto agrega columnas, sanea algunos datos, recarga PostgREST y además propone FOR ALL TO anon. No se debe ejecutar como receta segura: amplía permisos y no crea todas las columnas usadas. |
| `runClientSideLegacyMigration()` · 4082 | Confirma, recorre sectores cargados y rellena territorio/notes/barrio cuando detecta algunos faltantes, guardando con upsert seguro. No crea columnas ni recarga caché. Su condición no detecta todos los casos de nulos/vacíos. |

## 15. Eventos sin nombre y conexiones entre vistas

Los callbacks de `forEach`, `map`, `filter`, `.then`, Realtime y listeners se entienden dentro de la función que los registra. No son procesos permanentes en servidor. Los de render capturan el elemento/índice actual; los de Realtime vuelven a consultar; los de online/offline actualizan presentación. `window.*` permite llamar funciones desde atributos HTML, pero expone operaciones en el navegador y no aporta autorización.

`setInterval(updateTelemetryActiveCut,30000)` vive mientras el documento está abierto. `DOMContentLoaded` inicia acceso. Los formularios/editables dependen de IDs acordados entre HTML y JS; cambiar uno exige actualizar ambos. El defecto del botón de reconexión muestra el efecto de romper ese contrato.

## 16. Ejemplos para mantenerlo sin adivinar

**Cambiar solo una celda:** localizar `renderMatrixTable` → callback `onclick` → `openCellModal` → `setCellBlock` → `publishPacSchedule`. Antes de probar verificar toggles de pareja y escala, porque pueden modificar más celdas. Confirmar después el JSON persistido, no solo la alerta.

**Añadir un campo de sector:** crear migración, actualizar formulario `admin.html`, carga de modal, `handleSaveManualSector`, `upsertSectorSafe` y su fallback, render de fichas/tabla/mapa y DTO Android si debe viajar allí. Si el fallback lo omite, una escritura puede aparentar éxito pero perder el campo.

**Investigar un sector que reaparece:** revisar respuesta de DELETE, permisos, listas A–D y `pac_deleted_sector_ids`. La lista local puede estar ocultando el problema. No borrar las preferencias de un usuario como sustituto de corregir servidor.

**Cambiar mapa:** diferenciar fuente de coordenadas (`getSectorCoordinates`), capas (`applyBasemap`) y representación (`updateBarinasMapMarkers`). El centro municipal y el hash son aproximaciones; no tratarlos como límites reales de un circuito.

**Modificar donaciones:** `saveDonationConfig` escribe los cinco campos; el Android lee `donations_config`. La función de precarga existe pero no se llama. Cambiar el formulario no cambia automáticamente su carga inicial.

## 17. Cobertura y límites

Este capítulo describe todas las declaraciones propias detectadas en ambas copias de `app.js`, incluidos los cinco fetch locales, el helper de clasificación y callbacks `onclick`. Las funciones vacías y los parámetros sin uso se mantienen documentados como tales. Las bibliotecas CDN/Leaflet/Supabase son dependencias: se explica cómo se invocan, sin reproducir su implementación interna. Las limitaciones descritas siguen presentes; esta entrega modifica documentación, no el comportamiento del producto.

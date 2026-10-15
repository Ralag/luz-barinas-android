# Android: datos, almacenamiento, sincronización y motor PAC

Manual de mantenimiento del código disponible en `E:\LUZ BARINAS`, revisado el **15 de octubre de 2026**. Describe lo que ejecutan estos archivos, incluidos sus límites actuales. Los valores iniciales, comentarios y etiquetas de la aplicación no constituyen evidencia de mediciones eléctricas reales. Esta revisión no consultó ni modificó el servicio desplegado y no certifica que un APK distribuido corresponda a este árbol de fuentes.

Este capítulo cubre íntegramente los archivos Kotlin de `app/src/main/java/com/example/data/**`, `engine/**`, `utils/**`, `app/src/test/**` y `app/src/androidTest/**`. Para pantallas, ViewModel, alarmas, workers y widget, véase el capítulo `03-android-interfaz-y-segundo-plano.md`; aquí se explican sus conexiones con la capa de datos. Las ubicaciones `Lnn` de cada ficha pertenecen al archivo enlazado en su apartado y son las líneas del código revisado, no las de este documento.

## 1. Cómo recorrer esta parte del sistema

La aplicación maneja tres clases de información que conviene distinguir al hacer cambios:

| Información | Dónde vive | Cómo llega a la interfaz | Qué representa actualmente |
|---|---|---|---|
| Sectores y su estado | Tabla local Room `sectors`, alimentada por semillas, reportes optimistas y Supabase | `SectorDao.getAllSectors` → `EnergyRepository.allSectorsFlow` → ViewModel | Estado publicado o estimado; el voltaje y porcentaje también pueden proceder de constantes |
| Reportes del teléfono | Room `pending_reports` | `allReportsFlow` y `unsyncedCountFlow` | Eventos ciudadanos locales, enviados o pendientes |
| Histórico de cortes | Room `outage_records` | Consulta puntual durante la predicción | En este código se inicializa con seis ejemplos; no existe construcción automática del histórico a partir de todos los reportes |
| Cronograma PAC | Singleton mutable `PacScheduleData`, copia en SharedPreferences y configuración Supabase | Snapshots del singleton y evento `pacScheduleUpdatedFlow` | Matriz de bloques por franja/día con rotación mensual calculada |
| Avisos, donaciones y versión | Supabase `app_config`; algunos marcadores en preferencias | `StateFlow` de `CloudSyncRepository` | Configuración de la aplicación; aviso y donaciones no tienen una copia local completa en esta capa |
| Direcciones y municipios | Listas Kotlin de `BarinasLocationsCatalog`; altas adicionales en memoria | Selector de direcciones | Catálogo textual que vincula direcciones a sectores; no es geolocalización GPS ni un geocodificador |

El recorrido principal es `Room → Flow → modelo de dominio → ViewModel → Compose`. Las escrituras a Room invalidan consultas observadas y provocan nuevas emisiones. Leer un `Flow` no es lo mismo que suscribirse: el consumidor debe colectarlo. Los `StateFlow` de la nube retienen el último valor en el proceso; el `SharedFlow` PAC anuncia cambios y no tiene historial de reproducción configurado.

La mayoría de operaciones suspendidas de los repositorios cambia a `Dispatchers.IO`. Eso evita ejecutar sus lecturas y peticiones en el hilo de la interfaz, pero no las convierte en operaciones instantáneas ni independientes: `submitReport` espera su intento de red antes de devolver el resultado. La actualización local ocurre antes, por lo que la interfaz observadora puede reaccionar mientras esa función continúa.

La ruta Retrofit definida en `data/remote` es residual: **no se encontraron llamadas a `ApiClient.api` en `app/src`**. El envío activo usa Supabase PostgREST; la recepción continua usa Supabase Realtime; el cálculo predictivo y mensual mostrado se hace localmente.

## 2. Tipos de dominio: qué significa cada campo

### 2.1 `PowerStatus.kt`

Fuente: [PowerStatus.kt](<E:/LUZ BARINAS/app/src/main/java/com/example/data/model/PowerStatus.kt:1>).

Son modelos de transporte dentro de la aplicación. Sus constructores no consultan red ni disco y no validan por sí mismos rangos, relaciones o consistencia. Las funciones generadas de las `data class` —`copy`, igualdad, desestructuración y representación textual— son las de Kotlin; no añaden persistencia. `@Immutable` informa a Compose de la intención de inmutabilidad, sin convertir otros objetos globales en seguros para concurrencia.

**`ServiceStatus` — L5.** Enumeración con dos propiedades por estado: `label: String`, texto de presentación, y `hexColor: Long`, color ARGB. Valores:

| Constante | Etiqueta | Color |
|---|---|---|
| `NORMAL` | Servicio Normal | `0xFF10B981` |
| `SCHEDULED_OUTAGE` | Corte Programado (PAC) | `0xFFEF4444` |
| `IRREGULAR_OUTAGE` | Corte Irregular (Avería) | `0xFF9333EA` |

Agregar otro estado afecta conversiones Room, validación remota, filtros, insignias y las decisiones optimistas del worker. No basta con agregar el elemento a esta enumeración.

**`Sector` — L12.** Unidad eléctrica presentada en listas, mapas y selección.

| Campo | Tipo y significado |
|---|---|
| `id` | `String`; identificador estable para unir reportes, selección y fila remota/local |
| `name` | `String`; nombre mostrado |
| `circuitCode` | `String`; descripción/código de circuito; actualmente contiene textos libres |
| `status` | `ServiceStatus`; estado ya convertido desde la cadena almacenada |
| `voltage` | `Float`; referencia o valor reportado en voltios; esta capa no garantiza una medición instrumental |
| `confirmedReportsCount` | `Int`; contador presentado; las actualizaciones optimistas lo incrementan con reglas locales |
| `withoutPowerPercentage` | `Int`; porcentaje presentado, sin denominador estadístico calculado en este modelo |
| `lastUpdatedMillis` | `Long`; fecha de actualización en milisegundos desde época Unix |
| `rotationBlock` | `String`; bloque. Coexisten códigos `A` y etiquetas `Bloque A` según origen |
| `coordinates` | `List<Pair<Double, Double>>`; vértices del polígono, primero latitud y luego longitud |
| `isCommunity` | `Boolean = false`; marca de sector comunitario |

**`OutageRecord` — L26.** Un intervalo histórico, utilizado para estimar duración.

| Campo | Tipo y significado |
|---|---|
| `id` | `Long = 0`; identidad local |
| `sectorId`, `sectorName` | `String`; vínculo al sector y nombre guardado con el registro |
| `statusType` | `ServiceStatus`; tipo de corte |
| `startTimeMillis` | `Long`; inicio del evento en milisegundos |
| `endTimeMillis` | `Long?`; final o `null` si no se indicó |
| `durationHours` | `Float`; duración declarada; no se deriva automáticamente del inicio/final |
| `notes` | `String`; observación histórica |

Un final nulo no crea un mecanismo de cierre automático. El motor promedia `durationHours` aunque el registro no haya finalizado.

**`CitizenReport` — L38.** Vista de un reporte registrado por este dispositivo.

| Campo | Tipo y significado |
|---|---|
| `id` | `Long = 0`; ID Room, distinto del eventual identificador de una fila remota |
| `sectorId`, `sectorName` | `String`; sector al que se atribuye el reporte |
| `hasPower` | `Boolean`; respuesta del ciudadano sobre disponibilidad |
| `reportedAtMillis` | `Long`; hora local original del reporte |
| `reportType` | `String`; texto de categoría, por ejemplo `NORMAL`, `SIN_LUZ`, `BAJON`, `FALLA_IRREGULAR` |
| `voltageObserved` | `Float?`; referencia declarada; `null` significa que no se aportó |
| `observation` | `String? = null`; comentario libre del ciudadano |
| `isSynced` | `Boolean = false`; confirmación local de envío |

La observación viaja de la UI al repositorio, se persiste en Room y se incluye en Supabase. No participa en el cálculo predictivo. El modelo de dominio no expone `retryCount`, que existe solamente en la entidad local.

**`OutagePrediction` — L51.** Resultado explicativo del algoritmo, sin persistencia propia.

| Campo | Tipo y significado |
|---|---|
| `sectorId`, `sectorName`, `rotationBlock` | `String`; contexto del resultado |
| `nextEstimatedStartMillis` | `Long`; inicio estimado, que puede ser pasado cuando el algoritmo reconoce un corte en curso |
| `nextEstimatedEndMillis` | `Long`; inicio estimado más duración calculada |
| `estimatedDurationHours` | `Float`; media histórica admitida o valor de respaldo |
| `confidencePercentage` | `Int`; puntuación heurística; no probabilidad calibrada con validación estadística |
| `algorithmDetail` | `String`; explicación breve de la rama utilizada |
| `hoursUntilWindow` | `Float`; espera en horas, acotada a cero si el inicio ya pasó |

### 2.2 Configuración económica y respuesta mensual

Fuente: [DonationConfig.kt](<E:/LUZ BARINAS/app/src/main/java/com/example/data/model/DonationConfig.kt:3>).

**`DonationConfig` — L3.** Cinco cadenas con valor inicial vacío: `paypal` y `binance` identifican los datos configurados de esos métodos; `pmBank`, `pmPhone` y `pmId` son banco, teléfono e identificación del pago móvil. `CloudSyncRepository.processConfig` construye este objeto desde `donations_config`; el ViewModel lo observa y el diálogo lo presenta. No comprueba titularidad, formato bancario ni que un enlace sea accesible; no hace pagos ni guarda comprobantes.

Fuente: [MonthlyWeeksResponse.kt](<E:/LUZ BARINAS/app/src/main/java/com/example/data/model/MonthlyWeeksResponse.kt:9>).

**`MonthlyWeeksResponse` — L9.** Un campo `weeks: List<PacWeekPlan>`. `@Keep` y `@JsonClass(generateAdapter = true)` preparan el modelo para conservación/serialización Moshi en el contrato Retrofit. La vista mensual actual llama al cálculo local, no a este endpoint.

## 3. Room: tablas, conversiones y operaciones

### 3.1 Entidades y sus convertidores

Fuente: [SectorEntity.kt](<E:/LUZ BARINAS/app/src/main/java/com/example/data/local/entity/SectorEntity.kt:8>).

**`SectorEntity` — L9, tabla `sectors`.** Su clave primaria es `id: String`, no autogenerada. Guarda `name`, `circuitCode`, `status: String`, `voltage`, `confirmedReportsCount`, `withoutPowerPercentage`, `lastUpdatedMillis`, `rotationBlock`, `polygonPointsRaw: String` e `isCommunity: Boolean = false`. Salvo `status` y el polígono, equivalen a los campos de `Sector`. `polygonPointsRaw` codifica `lat,lng;lat,lng;...`. No hay claves foráneas declaradas a reportes o históricos, índices geográficos ni municipio/parroquia persistidos en esta entidad.

**`SectorEntity.toDomain()` — L22.** Sin parámetros; devuelve `Sector`.

1. Separa el polígono por `;` y cada punto por `,`.
2. Conserva únicamente pares de dos componentes que puedan convertirse a `Double`.
3. Convierte `status` con `ServiceStatus.valueOf`; una cadena no reconocida se reemplaza por `NORMAL`.
4. Copia todos los demás campos al modelo de dominio.

La llaman el flujo de sectores, la consulta de predicción y el alta comunitaria del repositorio. No escribe ni usa red. Los vértices inválidos se descartan silenciosamente; no comprueba límites de latitud/longitud ni que haya suficientes puntos para un polígono. Un estado remoto nuevo o mal escrito se presenta como servicio normal, lo cual puede ocultar un problema de contrato.

Fuente: [OutageRecordEntity.kt](<E:/LUZ BARINAS/app/src/main/java/com/example/data/local/entity/OutageRecordEntity.kt:9>).

**`OutageRecordEntity` — L13, tabla `outage_records`.** `id: Long = 0` es clave autogenerada. Los otros campos son `sectorId`, `sectorName`, `statusType: String`, `startTimeMillis`, `endTimeMillis: Long?`, `durationHours` y `notes`. Hay un índice compuesto por `sectorId` y `startTimeMillis`, útil para las consultas recientes por sector. No se fuerza que la duración coincida con la diferencia de fechas.

**`OutageRecordEntity.toDomain()` — L23.** Sin parámetros; devuelve `OutageRecord`. Intenta convertir `statusType` a `ServiceStatus`; si falla utiliza `SCHEDULED_OUTAGE` y después copia el resto. La usa `EnergyRepository.getPredictionForSector`. No persiste ni consulta red. Al cambiar los nombres de estados hay que revisar este fallback, diferente del de `SectorEntity`.

Fuente: [PendingReportEntity.kt](<E:/LUZ BARINAS/app/src/main/java/com/example/data/local/entity/PendingReportEntity.kt:8>).

**`PendingReportEntity` — L12, tabla `pending_reports`.** `id: Long = 0` es clave autogenerada. Persiste todos los campos de `CitizenReport`, incluidos `observation: String? = null` e `isSynced: Boolean = false`, más `retryCount: Int = 0`. Índice compuesto por `isSynced` y `reportedAtMillis`, adecuado para buscar pendientes en orden temporal. La identidad local no se envía al servidor en el contrato activo, por lo que no aporta idempotencia remota.

**`PendingReportEntity.toDomain()` — L24.** Sin parámetros; devuelve `CitizenReport` con copia directa de todos sus campos correspondientes. Omite `retryCount`. La usa `EnergyRepository.allReportsFlow`. Sin red, escritura o conversión de categoría; categorías y observaciones se conservan literalmente.

### 3.2 `AppDatabase`: creación y evolución

Fuente: [AppDatabase.kt](<E:/LUZ BARINAS/app/src/main/java/com/example/data/local/AppDatabase.kt:14>).

**`AppDatabase` — L23.** Subclase abstracta de `RoomDatabase`; declara las tres entidades anteriores, `version = 3` y `exportSchema = false`. Room genera la implementación en compilación. El archivo es `luz_barinas.db`, creado con el contexto de aplicación.

| Declaración | Contrato, pasos y consumidores |
|---|---|
| `sectorDao()` — L24 | Sin parámetros, devuelve `SectorDao`. Accessor generado por Room; usado por los dos repositorios, worker, widget, receiver de acciones y test de DAO. Obtener el DAO no descarga sectores |
| `outageRecordDao()` — L25 | Sin parámetros, devuelve `OutageRecordDao`. Lo utiliza el repositorio para semilla y predicción |
| `pendingReportDao()` — L26 | Sin parámetros, devuelve `PendingReportDao`. Usado por repositorio, worker, widget y receiver para la cola de reportes |
| `MIGRATION_2_3.migrate(db)` — L33 | Recibe `SupportSQLiteDatabase`, devuelve `Unit`. Room la invoca al pasar de esquema 2 a 3. Ejecuta dos `ALTER TABLE`: agrega `sectors.isCommunity INTEGER NOT NULL DEFAULT 0` y `pending_reports.observation TEXT DEFAULT NULL`. Modifica SQLite; no reconstruye el histórico ni contacta Supabase |
| `getInstance(context)` — L39 | Recibe un `Context`, devuelve `AppDatabase`. Si `INSTANCE` existe lo devuelve; si no entra en `synchronized(this)`, construye con `Room.databaseBuilder`, registra la migración 2→3, habilita fallback destructivo, guarda `INSTANCE` y devuelve. Los consumidores normalmente usan esta fábrica mediante parámetros por defecto |

`INSTANCE` es `@Volatile`; dentro del bloque sincronizado no vuelve a comprobar si otro hilo ya lo construyó. Dos llamadas iniciales que hayan superado simultáneamente la comprobación exterior pueden construir instancias sucesivas; no hay un cierre o reinicio expuesto para pruebas. `fallbackToDestructiveMigration(dropAllTables = true)` permite recrear las tablas cuando no exista una ruta de migración aplicable: eso puede borrar reportes pendientes. No equivale a recuperar datos si un SQL de migración registrado falla. No está definida una migración 1→2 en este archivo y no se exporta el esquema para compararlo automáticamente.

### 3.3 `SectorDao`: todas las consultas

Fuente: [SectorDao.kt](<E:/LUZ BARINAS/app/src/main/java/com/example/data/local/dao/SectorDao.kt:11>). Cada método es un contrato abstracto implementado por Room. Sus errores SQL/disco se propagan al llamador; no hay captura dentro del DAO. Las consultas `Flow` se ejecutan al colectarse y vuelven a emitir ante invalidaciones de la tabla. Ninguna usa la red.

| Función y línea | Parámetros / retorno | SQL o pasos internos | Quién la usa y límites |
|---|---|---|---|
| `getAllSectors()` — L14 | Ninguno → `Flow<List<SectorEntity>>` | `SELECT * FROM sectors ORDER BY name ASC` | `EnergyRepository.allSectorsFlow`. Todos los sectores, sin paginar ni separar comunidad/oficial; orden según SQLite, no búsqueda lingüística configurada |
| `getAllSectorsList()` — L17 | Ninguno → lista suspendida | Mismo `SELECT` y orden; lectura puntual | Sin llamador encontrado en fuentes Android actuales; útil para exportación, pero no hay exportador implementado aquí |
| `getSectorById(id)` — L20 | `String` ID → `SectorEntity?` suspendido | `WHERE id = :id LIMIT 1` | Repositorios, worker, widget, receiver y prueba Room. Devuelve `null` si falta; no intenta descargarlo |
| `observeSectorById(id)` — L23 | `String` ID → `Flow<SectorEntity?>` | Misma búsqueda por ID, reactiva | Sin llamador encontrado; la aplicación observa la lista completa en su lugar |
| `insertOrUpdateSectors(sectors)` — L26 | Lista de entidades → `Unit` suspendido | `@Insert(onConflict = REPLACE)` por lote | Semilla y descarga global de nube. Reemplaza filas con igual PK; no elimina filas locales ausentes de la lista recibida |
| `insertOrUpdateSector(sector)` — L29 | Una entidad → `Unit` suspendido | `@Insert(onConflict = REPLACE)` | Alta comunitaria y nuevo sector remoto; prueba DAO. No resuelve conflictos por fecha/versiones |
| `updateSector(sector)` — L32 | Una entidad → `Unit` suspendido | `@Update`, identifica fila por PK | Actualizaciones de nube y optimistas. No inserta si no existe la fila y no devuelve cantidad afectada |
| `getSectorsCount()` — L35 | Ninguno → `Int` suspendido | `SELECT COUNT(*) FROM sectors` | Condición de precarga. Un recuento menor a 60 provoca reemplazo de los 63 sectores incluidos en la semilla, aunque la base no esté vacía |

### 3.4 `OutageRecordDao`: todas las consultas

Fuente: [OutageRecordDao.kt](<E:/LUZ BARINAS/app/src/main/java/com/example/data/local/dao/OutageRecordDao.kt:10>).

| Función y línea | Parámetros / retorno | SQL o escritura | Uso y límites |
|---|---|---|---|
| `getRecentOutagesBySector(sectorId, limit = 15)` — L13 | ID y máximo de filas → lista suspendida | Filtra `sectorId`, ordena `startTimeMillis DESC`, aplica `LIMIT :limit` | Predicción. No distingue programados/averías ni cerrados/abiertos; no valida un `limit` proporcionado por otro consumidor |
| `getAllOutages()` — L16 | Ninguno → `Flow<List<OutageRecordEntity>>` | Todas las filas por inicio descendente | Sin llamador encontrado. No hay limpieza ni paginación |
| `insertOutages(records)` — L19 | Lista → `Unit` suspendido | `@Insert(REPLACE)` | Inicialización del repositorio; IDs cero se autogeneran |
| `insertOutage(record)` — L22 | Un registro → `Unit` suspendido | `@Insert(REPLACE)` | Sin llamador encontrado. Su mera existencia no significa que cada reporte cree un intervalo histórico |
| `getCount()` — L25 | Ninguno → `Int` suspendido | `SELECT COUNT(*) FROM outage_records` | Si es cero, el repositorio inserta los seis ejemplos históricos |

### 3.5 `PendingReportDao`: todas las consultas

Fuente: [PendingReportDao.kt](<E:/LUZ BARINAS/app/src/main/java/com/example/data/local/dao/PendingReportDao.kt:11>).

| Función y línea | Parámetros / retorno | SQL o escritura | Uso y límites |
|---|---|---|---|
| `getUnsyncedReports()` — L14 | Ninguno → lista suspendida | `WHERE isSynced = 0 ORDER BY reportedAtMillis ASC` | Envío inmediato para buscar la última fila, envío manual y worker. Lee una instantánea; no reserva filas frente a otro emisor |
| `getAllReportsFlow()` — L17 | Ninguno → `Flow<List<PendingReportEntity>>` | Todas las filas por fecha descendente | Historial local en el repositorio/UI. Incluye enviados y pendientes; no es descarga del historial de todos los ciudadanos |
| `getUnsyncedReportsCountFlow()` — L20 | Ninguno → `Flow<Int>` | `COUNT(*) WHERE isSynced = 0` | Contador de pendientes en interfaz; no significa que la red esté disponible |
| `insertReport(report)` — L23 | Entidad → `Long` ID insertado, suspendido | `@Insert(REPLACE)` | Repositorio, widget y receiver. El ID devuelto permitiría confirmar exactamente esa fila, pero los caminos actuales no lo conservan |
| `updateReport(report)` — L26 | Entidad → `Unit` suspendido | `@Update` por PK | Worker incrementa `retryCount`; escribe la entidad completa, por lo que una copia vieja puede sobreescribir otro cambio concurrente |
| `markAsSynced(id)` — L29 | ID local `Long` → `Unit` suspendido | `UPDATE pending_reports SET isSynced = 1 WHERE id = :id` | Repositorio/worker tras resultado exitoso. No valida que el servidor haya recibido esa misma identidad |
| `deleteOldSyncedReports(olderThanMillis)` — L32 | Fecha límite en ms → `Unit` suspendido | Borra solo `isSynced = 1 AND reportedAtMillis < :olderThanMillis` | Sin llamador encontrado. No existe una política activa de limpieza al declarar este método |

No hay `@Transaction` que agrupe insertar evento, cambiar sector y programar trabajo. Un fallo entre pasos puede dejar el reporte guardado sin actualización visual o sin worker; el intento manual puede recuperarlo si continúa pendiente, pero no hay una tarea global de arranque que recorra siempre la outbox.

## 4. Preferencias: cronograma y ajustes de alertas

### 4.1 `PacSchedulePrefs`

Fuente: [PacSchedulePrefs.kt](<E:/LUZ BARINAS/app/src/main/java/com/example/data/PacSchedulePrefs.kt:16>).

El objeto guarda una instantánea del singleton, no una tabla Room. Nombre de archivo lógico: `pac_schedule_prefs`. Claves: `pac_matrix`, `pac_slots`, `sectors_bloque_a`, `sectors_bloque_b`, `sectors_bloque_c`, `sectors_bloque_d` y `schedule_version`. `TAG` solo identifica mensajes de diagnóstico.

**`getPrefs(context)` — L27, privada.** Recibe `Context`, devuelve `SharedPreferences` con `applicationContext` y `MODE_PRIVATE`. La llaman exclusivamente `saveSchedule` y `loadSchedule`; no lee todavía una matriz ni abre la red.

**`saveSchedule(context)` — L31.** Recibe contexto y devuelve `Unit`.

1. Obtiene preferencias y abre un editor con la extensión `edit`.
2. Serializa `activeMatrix` como array JSON de filas.
3. Serializa cada `activeSlots` con `timeLabel`, `startHour` y `endHour`; no escribe el `index`, que se reconstruye por posición.
4. Guarda las cuatro listas de nombres como `StringSet` y la versión como `Long`.

La llaman las acciones de edición del ViewModel y la aplicación de PAC remoto. Efecto: persistencia local mediante `apply` de preferencias; sin red y sin programación automática de alarmas. Los conjuntos eliminan duplicados y no conservan el orden original; la escritura a disco no es un acuse remoto. No devuelve confirmación ni captura errores propios.

**`loadSchedule(context)` — L62.** Recibe contexto y devuelve `Boolean`: `false` si no existe `pac_matrix`, si falta su cadena o si una excepción impide completar; `true` si termina la carga.

1. Lee y convierte la matriz JSON y la asigna al singleton.
2. Si hay slots, crea `PacSlot` con índices consecutivos, vacía la lista activa y copia los reconstruidos.
3. Si cada conjunto de sectores existe, sustituye su lista por el conjunto ordenado alfabéticamente.
4. Restaura `scheduleVersion`, con respaldo cero.
5. Ante error registra un warning y devuelve `false`.

La única llamada encontrada está en el `init` del ViewModel. No valida que filas/columnas y cantidad de franjas coincidan, ni horas en 0–23; una excepción a mitad de carga no deshace asignaciones anteriores. Receivers y workers que nacen en un proceso nuevo no llaman a esta carga: pueden calcular con los valores iniciales hasta que se cree el ViewModel. Si se cambia el formato, mantener compatibilidad o realizar una migración explícita de preferencias.

### 4.2 `PacAlertSettings` y `PacAlertPrefs`

Fuente: [PacAlertSettings.kt](<E:/LUZ BARINAS/app/src/main/java/com/example/data/model/PacAlertSettings.kt:5>).

**`PacAlertSettings` — L5.** Modelo con `advanceMinutes: Int = 10`, minutos de anticipación; `isNotificationEnabled = true`, permiso lógico del usuario para notificaciones PAC; `isAlarmEnabled = false`, modo de sirena; `isRestoreAlarmEnabled = false`, aviso de retorno; `isVibrationEnabled = true`; `useLegacyAlarmSiren = false`, compatibilidad con el ajuste antiguo. Estos valores no son los permisos del sistema Android. Guardar `true` no concede `POST_NOTIFICATIONS` ni permiso de alarma exacta.

**`PacAlertPrefs` — L14.** Archivo `pac_alert_settings`. Sus constantes privadas mapean las claves `advance_minutes`, `notification_enabled`, `alarm_enabled`, `restore_alarm_enabled`, `vibration_enabled` y `use_legacy_alarm`.

**`getSettings(context)` — L23.** Recibe contexto; devuelve `PacAlertSettings`. Lee primero el booleano legacy. Recupera cada valor con sus respaldos; `isAlarmEnabled` usa el legacy como respaldo si no existe la clave moderna. No acota `advanceMinutes` durante la lectura. La llaman scheduler, receiver, worker, configuración y tarjeta de alertas del dashboard. Lee disco/caché de preferencias; no programa ni muestra nada y no hace red.

**`saveSettings(context, settings)` — L36.** Recibe contexto y objeto; devuelve `Unit`. Escribe todas las claves con `apply`, acota la anticipación a 1–180 minutos y guarda `use_legacy_alarm = settings.useLegacyAlarmSiren || settings.isAlarmEnabled`. La llaman pantallas/diálogo de configuración. No devuelve si la escritura llegó a disco ni rearma alarmas: ciertos llamadores realizan después ese paso. El OR mantiene legacy activo cuando se guarda sirena activa; un llamador debe enviar valores coherentes para desactivar ambos flags.

## 5. Catálogo territorial: direcciones, municipios y búsqueda

Fuente: [BarinasLocationsData.kt](<E:/LUZ BARINAS/app/src/main/java/com/example/data/model/BarinasLocationsData.kt:1>), leído completo, incluidas sus listas iniciales.

### 5.1 Modelos y declaraciones de datos

**`BarinasMunicipality` — L3.** `name: String` identifica municipio, `capital: String` su capital presentada y `parroquias: List<String>` las opciones del selector. No hay ID geográfico normalizado ni carga automática del listado.

**`BarinasLocation` — L9.** Cada dirección contiene:

| Campo | Significado |
|---|---|
| `id: String` | ID de la opción del catálogo, distinto de la PK del sector eléctrico |
| `name: String` | Nombre visible de urbanización, barrio, avenida, sector o municipio |
| `type: String` | Categoría textual libre |
| `parroquia: String` | Texto de parroquia; a veces combina nombres o abreviaturas |
| `block: String` | Etiqueta como `Bloque A`; necesita normalización antes de comparar con A/B/C/D |
| `circuitCode: String` | Circuito atribuido a la dirección |
| `sectorEntityId: String` | Vínculo explícito a la fila eléctrica Room/Supabase |
| `description: String` | Información de orientación y búsqueda |
| `keywords: List<String>` | Alias de búsqueda, introducidos manualmente |
| `municipio: String = "Barinas"` | Municipio, con respaldo Barinas si el constructor no lo especifica |

**`BarinasLocationsCatalog` — L22.** Singleton que reúne las listas y cuatro funciones. `LOCATIONS` — L24 — contiene **87 entradas con 86 IDs únicos**. Agrupa El Carmen, Alto Barinas, Varyna/Tavacare, casco central, Corazón de Jesús, zona industrial, Ramón Ignacio Méndez/Rómulo Betancourt y municipios/circuitos foráneos. No es una fuente dinámica ni define polígonos. Las 87 llamadas incluidas omiten `municipio`, por lo que incluso entradas descritas como otros municipios heredan el respaldo `Barinas` en ese campo.

`BARINAS_MUNICIPALITIES` — L1015 — contiene 12 municipios: Barinas, Bolívar, Antonio José de Sucre, Pedraza, Alberto Arvelo Torrealba, Obispos, Ezequiel Zamora, Rojas, Cruz Paredes, Sosa, Andrés Eloy Blanco y Arismendi. `BARINAS_PARROQUIAS` — L1149 — concatena sus listas; no elimina duplicados ni ordena globalmente. `customLocations` — L1151 — es una lista privada en memoria; se pierde al terminar el proceso.

### 5.2 Funciones del catálogo

**`getParroquiasForMunicipality(municipalityName)` — L1144.** Parámetro: nombre textual. Retorno: `List<String>`. Busca el municipio con igualdad ignorando mayúsculas/minúsculas y devuelve sus parroquias; si no encuentra, devuelve las del primer municipio, Barinas. La llama `BarinasAddressDialog` al elegir municipio. Sin efectos persistentes/red. No elimina acentos ni espacios exteriores; un nombre erróneo puede mostrar parroquias de Barinas sin advertencia.

**`addCustomLocation(loc)` — L1153.** Recibe `BarinasLocation`, devuelve `Unit`. Elimina de `customLocations` registros con igual ID o nombre igual sin distinguir mayúsculas; inserta la nueva dirección en posición cero. La llama el ViewModel tras registrar comunidad. Solo afecta memoria del proceso; no escribe `LOCATIONS`, Room, preferencias ni servidor. No elimina duplicados contra la lista estática y no separa comunidades homónimas de distintos municipios.

**`getAllLocations()` — L1158.** Sin parámetros; devuelve `customLocations + LOCATIONS`, una lista nueva con las altas temporales primero. La usa `searchLocations`; no filtra ni concilia IDs repetidos. Sin persistencia o red.

**`searchLocations(query)` — L1163.** Recibe texto de búsqueda; devuelve hasta 12 resultados ordenados por puntuación, o las primeras ocho opciones si la consulta queda vacía.

1. Construye la lista combinada y normaliza consulta con `trim().lowercase()`.
2. Separa tokens por uno o más espacios.
3. Por cada dirección convierte nombre, descripción, parroquia y circuito a minúsculas.
4. Suma 100 puntos si el nombre empieza por toda la consulta; en caso contrario suma 50 si contiene toda la consulta.
5. Por cada token, busca coincidencia de subcadena en esos cuatro campos o en algún keyword; cada token coincidente suma 10. Se detiene al primer token que no coincide.
6. Incluye el registro si coinciden todos los tokens **o** si ya acumuló alguna puntuación; ordena descendente, extrae direcciones y limita a 12.

La llama el selector para escribir o resolver una selección rápida. Sin red/disco, pero crea listas, cadenas y pares en cada búsqueda; el comentario “zero-allocation fuzzy search” no describe su implementación. Es búsqueda de subcadenas, sin distancia de edición ni normalización de tildes. Los keywords se comparan como están guardados, sin convertirlos dentro del bucle. La condición `score > 0` permite coincidencias parciales: un registro que coincide con el primer token puede aparecer aunque falle otro. No se busca explícitamente el campo `municipio`.

### 5.3 Conflictos concretos que debe conocer quien edite el catálogo

- `loc_barrio_el_carmen` aparece en L51 y L393: mismo ID/nombre, pero una entrada señala bloque C y `sec_c_corocito`, mientras la otra señala bloque A y `sec_a_centro`. Una lista Compose con claves por ID puede recibir claves duplicadas; la selección depende del elemento concreto.
- Sabaneta/CAAEZ — L991 — declara bloque D pero enlaza `sec_b_floresta`; Boconoito — L1002 — declara D pero enlaza `sec_c_corocito`, pese a existir un sector precargado `sec_d_boconoito`.
- Las listas de nombres por bloque de `PacScheduleData` no son la misma fuente que `BarinasLocationsCatalog` ni `SectorEntity.rotationBlock`. Por ejemplo, una dirección puede tener una atribución distinta en esas tres colecciones. Cambiar una no actualiza las otras.
- El normalizador del ViewModel basado en buscar la primera letra `[ABCD]` toma la B de `BLOQUE`: `Bloque C` termina en B en preferencias. El motor de predicción sí elimina la palabra completa. La modificación territorial debe revisar también el capítulo de UI y alarmas.

Para añadir una dirección utilizable offline, crear un `BarinasLocation` con ID único, municipio explícito cuando corresponda, keywords normalizados y `sectorEntityId` que exista. Para añadir una nueva unidad eléctrica, además hace falta una fila de sector y una decisión de sincronización; añadir únicamente un nombre al catálogo no crea esa fila.

## 6. `PacSchedule.kt`: estructura y reglas del cronograma

Fuente: [PacSchedule.kt](<E:/LUZ BARINAS/app/src/main/java/com/example/data/model/PacSchedule.kt:1>).

### 6.1 Modelos auxiliares, campo a campo

| Declaración | Campos y significado | Uso actual |
|---|---|---|
| `BroadcastNotice` — L8 | `title: String = ""`, título; `message: String = ""`, contenido; `level: String = "INFO"`, categoría textual; `timestamp: Long = System.currentTimeMillis()`, identidad temporal; `active: Boolean = true`, vigencia | Avisos de `app_config`, banner y notificación. No restringe `level` a un enum |
| `DayApproximation` — L16 | `dayOfMonth: Int`, número de día; `dayOfWeekName: String`, nombre; `shortDayName: String`, abreviatura; `cutSlots: List<PacSlot>`, franjas; `totalHours: Int`, horas declaradas | Modelo declarado sin construcción ni consumidor encontrado en este árbol |
| `MonthApproximation` — L24 | `monthIndex: Int`, mes 0–11; `monthName: String`; `year: Int`; `totalDays: Int`; `blockCode: String`; `totalCutsCount: Int`; `totalOutageHours: Int`; `avgCutsPerDay: Float`; `days: List<DayApproximation>` | También sin consumidor encontrado. Su presencia no implementa una proyección anual |
| `PacSlot` — L37 | `index: Int`, posición; `timeLabel: String`, texto visible; `startHour: Int` y `endHour: Int`, horas enteras | Franja base. No tiene minutos, zona horaria, fecha ni validación; la etiqueta puede contradecir sus horas |
| `PacDay` — L44 | `name: String`, nombre completo; `shortName: String`, abreviatura; `calendarDay: Int`, constante `Calendar` | Cabeceras semanales y conversión de día |
| `PacCell` — L50 | `dayIndex: Int`, columna; `slotIndex: Int`, fila; `block: String`; `isCurrentSlot: Boolean = false` | Tipo auxiliar sin consumidor encontrado; la matriz real guarda cadenas, no estos objetos |
| `DayTurnAudit` — L57 | `dayIdx: Int`; `dayName: String`; `cutsPerBlock: Map<String, Int>`, recuento A/B/C/D; `totalCutsCount: Int`; `detectedScheme: String`, etiqueta de esquema inferido | Retorno de `getDayTurnAudit`, que no tiene llamador actual encontrado |
| `RebalanceResult` — L65 | `slotIdx: Int`, celda; `dayIdx: Int`; `dayName: String`; `slotLabel: String`; `removedBlock: String`; `newlyAssignedBlock: String`; `explanation: String`; `countsSummary: Map<String, Int>` | Explica una sustitución de bloque realizada por `deleteTurnAndRebalance`; ViewModel lo expone a UI |
| `PacScheduleWindow` — L76 | `dayName: String`, etiqueta relativa o nombre; `timeLabel: String`; `block: String`; `startMillis: Long`; `endMillis: Long`; `isCurrentlyActive: Boolean`; `hoursUntil: Float` | Ventana absoluta para motor, configuración y scheduler |
| `PacWeekPlan` — L88 | `weekNumber: Int`; `dateRangeLabel: String`; `matrix: List<List<String>>` | Vista mensual y contrato Retrofit. `@Keep` y adaptador Moshi |

Estos modelos no suman horas ni cuentan cortes por sí mismos. Por ejemplo, construir un `MonthApproximation` con totales incompatibles no provoca validación.

### 6.2 Estado del singleton y convenciones de índices

**`PacScheduleData` — L94** concentra estado mutable global. Las operaciones siguientes, salvo las que reciben contexto en otros archivos, **no guardan en disco ni publican en red**. El ViewModel suele llamar después a `PacSchedulePrefs.saveSchedule` y a la función de publicación, que actualmente es un stub. Tampoco hay `StateFlow` propio en este objeto: modificarlo requiere que el consumidor actualice sus snapshots o la señal de versión.

| Propiedad | Comportamiento |
|---|---|
| `DEFAULT_SLOTS` — L96 | Lista privada de seis franjas: 03–07, 07–11, 11–15, 15–19, 19–23 y 23–03 |
| `activeSlots` — L105 | Lista mutable de copias de las franjas iniciales |
| `SLOTS` — getter L107 | Devuelve la misma lista activa vista como `List`; no crea snapshot ni garantiza que otro consumidor no la modifique |
| `DAYS` — L117 | Lunes a domingo, índices propios 0–6. No coincide con la numeración `Calendar.SUNDAY = 1`, `MONDAY = 2`, etc. |
| `DEFAULT_MATRIX` — L129 | Matriz privada de 6 filas/franjas × 7 columnas/días; las cadenas A/B/C se repiten con desplazamiento. No contiene D |
| `activeMatrix` — L138 | Copia mutable de la matriz; se puede reemplazar por otra cantidad de filas |
| `scheduleVersion` — L142 | `Long = 0`. Modificaciones locales usan hora del teléfono; carga remota/persistida puede asignar otro marcador temporal |
| `MATRIX` — getter L144 | Expone el array activo, incluyendo sus arrays internos mutables |
| `SECTORS_BLOQUE_A/B/C/D` — L324/369/415/468 | Listas mutables de nombres de sectores. No son relaciones Room y no actualizan `Sector.rotationBlock` |
| `MONTH_NAMES` — L536 | Enero a diciembre, índices 0–11; nombres para presentación |

La matriz inicial es la siguiente; la etiqueta de “oficial” en un comentario no aporta validación externa:

| Franja | LUN | MAR | MIÉ | JUE | VIE | SÁB | DOM |
|---|---|---|---|---|---|---|---|
| 03–07 | C | B | A | C | B | A | C |
| 07–11 | A | C | B | A | C | B | A |
| 11–15 | B | A | C | B | A | C | B |
| 15–19 | C | B | A | C | B | A | C |
| 19–23 | A | C | B | A | C | B | A |
| 23–03 | B | A | C | B | A | C | B |

Esta configuración reparte seis franjas entre tres bloques, con dos por bloque cada día. El bloque D tiene listado de nombres y soporte algorítmico, pero carece de ventanas reales en esta matriz inicial. Eso activa el respaldo artificial descrito en `findNextWindowForBlock` si se consulta D sin otro cronograma.

### 6.3 Lectura y edición de matriz/franjas

**`updateSlot(slotIdx, newTimeLabel, startHour, endHour)` — L110.** Parámetros: índice de franja, etiqueta nueva y horas enteras; retorno `Unit`. Si el índice pertenece a `activeSlots.indices`, sustituye ese elemento por un `PacSlot` con etiqueta recortada y asigna la hora actual a `scheduleVersion`; si no, no modifica nada. Lo llama `LuzBarinasViewModel.updateSlot`. No valida rangos 0–23, duración, solapamientos, orden ni correspondencia de etiqueta. Cambia memoria; el ViewModel realiza persistencia separada.

**`getMatrixSnapshot()` — L147.** Sin parámetros; devuelve `List<List<String>>`, copiando cada fila con `toList`. Lo llaman estado inicial y reconstrucción de estado del ViewModel, parámetros por defecto de vistas y administración. Evita que la interfaz reciba los arrays directamente; sin red/disco. No bloquea escrituras concurrentes durante la copia.

**`getSlotsSnapshot()` — L151.** Sin parámetros; devuelve `List<PacSlot>` con `copy()` de cada franja. Mismos consumidores de snapshots. Sin red/disco; no ordena ni valida. `PacSlot` contiene valores simples, por lo que esas copias bastan para sus campos actuales.

**`updateMatrix(newMatrix)` — L155.** Recibe array de filas, devuelve `Unit`. Recorre hasta `minOf(6, newMatrix.size)` filas y hasta `minOf(7, newMatrix[r].size)` columnas; sobrescribe con texto en mayúsculas/recortado y cambia versión. No reemplaza el array ni rellena celdas no aportadas. Sin llamador actual encontrado. Límite concreto: usa tamaño fijo seis en lugar del tamaño de `activeMatrix`; si antes se eliminó una franja, puede intentar escribir una fila inexistente. Si hay más de seis, no actualiza las adicionales. Una fila activa menor a siete también puede producir acceso inválido.

**`updateCell(slotIdx, dayIdx, block)` — L164.** Recibe fila, columna y cadena; devuelve `Unit`. Si fila existe y día está en 0–6, normaliza texto. Vacío, `LIBRE`, `SIN CORTE` o `SIN_CORTE` se convierten a `-`; el resto se guarda literalmente en mayúsculas. Actualiza versión solo dentro de esa rama. Lo llama `LuzBarinasViewModel.updateMatrixCell`. Acepta valores ajenos a A/B/C/D y presupone que la fila tiene siete columnas; no persiste/publica por sí misma.

**`deleteSlot(slotIdx)` — L172.** Recibe fila; devuelve `false` si queda una sola franja o el índice no existe, y `true` tras eliminar. Quita la franja, renumera `index` de las restantes y quita la fila equivalente de matriz si existe. Actualiza versión. La llama `LuzBarinasViewModel.deleteSlot`. No restaura cobertura de las 24 horas ni reasigna bloques del turno quitado; si franjas/matriz ya eran incompatibles, no reconstruye una correspondencia completa.

**`addSlot(label, startHour, endHour)` — L187.** Recibe etiqueta y horas; devuelve el `PacSlot` añadido. Usa índice igual al tamaño anterior; una etiqueta en blanco se sustituye por `Turno N`. Agrega franja y una fila de siete `-`, cambia versión. La llama `LuzBarinasViewModel.addSlot`. No ordena por hora, no comprueba solapamientos y no recorta una etiqueta no vacía. Hay que asignar después bloques a la nueva fila.

### 6.4 Auditoría y sustitución de turnos

**`getDayTurnAudit(dayIdx)` — L200.** Recibe columna; devuelve `DayTurnAudit`. Inicializa contadores A/B/C/D a cero, recorre todas las filas, lee esa columna con respaldo `-`, normaliza texto y suma únicamente bloques válidos. `totalCutsCount` suma celdas válidas. Si el máximo por bloque es al menos dos, etiqueta “2 Cortes Diarios (8 hrs)”; si no, “1 Solo Corte Diario (4 hrs)”. Usa nombre del día o lunes como respaldo. Sin llamador encontrado; sin persistencia/red. Es una clasificación heurística: no demuestra que cada bloque tenga exactamente uno/dos, y los textos 4/8 horas ignoran duraciones personalizadas.

**`deleteTurnAndRebalance(slotIdx, dayIdx)` — L225.** Recibe coordenadas de una celda; devuelve `RebalanceResult`. La llama `LuzBarinasViewModel.deleteTurnAndRebalance`.

1. Obtiene bloque actual, día y franja con respaldos.
2. Cuenta A/B/C/D en las otras filas de esa columna.
3. Asume esquema de dos cortes si alguna cuenta ya llega a dos **o** si al menos tres bloques tienen un turno; de otro modo asume uno.
4. Candidatos: bloques con menos de dos en primer esquema, o sin turnos en el segundo; si no hay, admite A/B/C/D.
5. Prefiere candidatos distintos de los vecinos anterior/siguiente; si eso deja lista vacía, vuelve a los candidatos iniciales.
6. Ordena por menor cuenta y penaliza elegir el mismo bloque actual; los empates conservan el orden de la colección de candidatos.
7. Si índices son válidos, sustituye la celda, cambia versión, suma al recuento el nuevo bloque y arma explicación/resultado.

Pese al nombre, **no borra una franja ni deja la celda libre: sustituye un bloque por otro**. No calcula horas de equidad ni garantiza todos los objetivos simultáneamente. Con índices inválidos puede devolver una explicación y cambiar versión sin escribir una celda; si no hay franjas, el respaldo `SLOTS[0]` puede fallar. El resultado describe lo hecho en memoria; guardado y anuncio posterior pertenecen al llamador.

### 6.5 Presets y listas de sectores

**`applySingleTurnPreset()` — L291.** Sin parámetros, `Unit`. Define seis filas: las cuatro primeras distribuyen A/B/C/D con desplazamiento diario; las dos últimas son `-`. Recorre las filas que tenga actualmente `activeMatrix` y copia el patrón, usando `-` para filas extra; cambia versión. La llama `LuzBarinasViewModel.applySingleTurnPreset`. No reconstruye `activeSlots` ni restaura seis filas si se eliminaron antes. Con seis franjas iniciales produce un turno de cuatro horas para cada bloque al día; con otras duraciones/tamaños no puede garantizar eso.

**`applyDoubleTurnPreset()` — L309.** Sin parámetros, `Unit`. Vacía franjas, repone las seis originales mediante copias, repone la matriz original 6×7 y actualiza versión. La llama `LuzBarinasViewModel.applyDoubleTurnPreset`, también desde `LuzBarinasViewModel.resetScheduleToDefault`; `PacScheduleData.resetToDefault` delega aquí. No restablece las cuatro listas de nombres. El preset contiene A/B/C y ningún D, aunque su nombre sugiere un esquema genérico.

**`resetToDefault()` — L320.** Sin parámetros, `Unit`. Delega únicamente a `applyDoubleTurnPreset`. Sin llamador externo encontrado. No borra SharedPreferences, nombres de sectores, catálogo o filas Room.

**`addSectorToBlock(sectorName, blockCode)` — L494.** Nombre y código textual → `Unit`. Recorta nombre; si queda vacío termina sin cambiar versión. Elimina una coincidencia exacta del nombre en cada lista; inserta al principio de la lista A/B/C/D indicada; código desconocido cae en A. Cambia versión. La llama `LuzBarinasViewModel.addSectorToBlock`. Comparación de nombres sensible a mayúsculas; `remove` elimina solo una ocurrencia, por lo que no sanea duplicados preexistentes. No crea sector eléctrico ni cambia Room.

**`removeSectorFromBlock(sectorName, blockCode)` — L511.** Nombre y código → `Unit`. Normaliza código con mayúsculas y trim; quita una coincidencia exacta del nombre de la lista elegida. Siempre actualiza versión, incluso si código/nombre no existe. La llama `LuzBarinasViewModel.removeSectorFromBlock`. No elimina de otras listas, catálogo, Room o servidor.

**`reassignSector(sectorName, targetBlock)` — L521.** Nombre y destino → `Unit`. Elimina una coincidencia exacta del nombre en cada lista; agrega al final de la lista destino, con respaldo A para un código desconocido; cambia versión. La llama `LuzBarinasViewModel.reassignSector`. A diferencia de `addSectorToBlock`, no recorta/descarta nombre vacío y agrega al final. No modifica `Sector.rotationBlock` ni relaciones territoriales reales.

### 6.6 Rotación mensual y consultas temporales

**`getMonthlyWeeks(monthName, totalDays)` — L541.** Parámetros: nombre del mes para etiqueta y cantidad de días; retorna exactamente cuatro `PacWeekPlan`. La llama `PacScheduleView` para la representación mensual.

1. Toma copia de la matriz actual y pone el nombre del mes en mayúsculas.
2. Plan 1: días 01–07, matriz sin desplazamiento.
3. Plan 2: 08–14, cada fila `r` toma `activeMatrix[(r + 1) % rows]`.
4. Plan 3: 15–21, desplazamiento de dos filas.
5. Plan 4: 22–`totalDays`, desplazamiento de tres filas.

`rows = activeMatrix.size.coerceAtLeast(1)` evita un divisor cero, pero no evita acceder al array vacío al construir las semanas siguientes. No usa año, mes numérico ni fecha real; no consulta API y no valida días del mes. La última “semana” puede abarcar diez días. Es una rotación mecánica de filas, no un cronograma confirmado para un mes futuro.

**`getDayIndex(calendarDayOfWeek)` — L586.** Constante `Calendar` → índice 0–6: lunes 0, martes 1, miércoles 2, jueves 3, viernes 4, sábado 5 y domingo 6. Cualquier otro entero devuelve cero. Consumidores: dashboard, vistas PAC, worker, `getActiveBlockNow` y `findNextWindowForBlock`. Sin efectos.

**`getWeekOffsetForDayOfMonth(dayOfMonth)` — L599.** Día numérico → desplazamiento: 1–7 da 0; 8–14 da 1; 15–21 da 2; todo lo demás da 3. La llaman las consultas de bloque/ventana. No es el número ISO de semana ni reinicia los lunes; para días inválidos, incluidos cero/negativos, también devuelve tres.

**`getBlockForDate(slotIdx, dayIdx, dayOfMonth = -1)` — L608.** Fila de franja, columna de día y fecha opcional → cadena del bloque o `-`. Usa desplazamiento cero cuando `dayOfMonth <= 0`; si es positivo usa el offset anterior. Calcula `(slotIdx + weekOffset) % activeMatrix.size.coerceAtLeast(1)` y accede con `getOrNull` a fila/columna. La llaman UI y worker. No necesita `Calendar` ni modifica estado. Con tamaños incoherentes retorna `-` para accesos inexistentes, y un índice negativo puede conservar resto negativo y caer igualmente en respaldo.

Ejemplo para matriz inicial, lunes y franja 07–11, índice 1: el día 5 consulta fila 1 y obtiene A; el día 12 consulta fila 2 y obtiene B; el día 19 consulta fila 3 y obtiene C; el día 26 consulta fila 4 y obtiene A. Se conserva la hora de la franja visible y se mueve la asignación de bloque entre filas.

**`getCurrentSlotIndex(hourOfDay, minuteOfHour = 0)` — L615.** Hora/minuto locales ya elegidos por el llamador → índice de franja. Convierte a minutos y busca la primera franja que contiene el instante. Para inicio menor que final utiliza intervalo semiabierto `[inicio, final)`; para inicio mayor o igual usa `hora >= inicio || hora < final`, que representa cruce de medianoche cuando son distintos. Si no encuentra, devuelve índices fijos del esquema original: 0 para 03–06, 1 para 07–10, 2 para 11–14, 3 para 15–18, 4 para 19–22 y 5 para el resto. Lo llaman dashboard/vista PAC y `getActiveBlockNow`.

Límites: no valida hora/minuto; una franja con inicio igual a final coincide todo el día bajo esa condición; si hay solapamientos gana la primera del listado. El respaldo puede devolver un índice inexistente después de eliminar franjas o crear huecos. Esta función no decide a qué fecha pertenece la franja nocturna.

**`getActiveBlockNow(currentTimeMillis = System.currentTimeMillis())` — L638.** Instante en ms → bloque textual. Crea `Calendar` en `America/Caracas`, obtiene día de semana, día del mes y franja actual, y delega en `getBlockForDate`. Sin llamador externo actual encontrado. Es determinista para su argumento y estado del singleton. Entre 00:00 y 02:59 usa la fecha del día que comenzó, aunque la franja 23–03 inició el anterior: puede consultar columna y desplazamiento equivocados, especialmente al cambiar de mes o tramo 7/8, 14/15, 21/22.

**`findNextWindowForBlock(targetBlock, currentTimeMillis = System.currentTimeMillis())` — L648.** Código o etiqueta de bloque e instante → `PacScheduleWindow` **siempre**, sin retorno nulo. La llaman motor, `PacAlarmScheduler` y diálogo de ajustes.

1. Normaliza bloque eliminando la palabra `BLOQUE`, mayúsculas y espacios exteriores.
2. Para cada fecha de hoy a hoy+14 días, ambas incluidas, calcula columna de día y desplazamiento mensual usando `America/Caracas`.
3. Recorre las franjas en su orden de lista, aplica la rotación de fila y compara el bloque.
4. Construye inicio en esa fecha a la hora de franja, con minutos/segundos/milisegundos cero. Si `endHour < startHour`, el final pertenece al día siguiente; si son iguales no suma un día.
5. Si el instante está entre inicio y final **incluyendo ambos extremos**, devuelve ventana activa, `hoursUntil = 0` y nombre del día.
6. Si el inicio está en el futuro, devuelve esa primera coincidencia, con “Hoy”, “Mañana” o nombre/día y espera calculada en horas.
7. Si no encuentra nada, devuelve un respaldo etiquetado “Hoy”, `15:00 a 19:00`, con inicio `ahora + 1 h`, final `ahora + 5 h`, bloque solicitado y espera una hora.

El último paso **inventa una ventana de respaldo, no demuestra que haya un corte programado**. Un bloque sin asignaciones —como D en la matriz inicial— obtiene así una alarma o predicción aparente. No distingue “sin corte” de “sin datos”.

Otros límites concretos: busca desde hoy, no desde ayer, por lo que a la 01:00 omite el corte de ayer 23:00 que todavía podría estar activo. La inclusión del extremo final contradice el intervalo semiabierto de `getCurrentSlotIndex`. Si el administrador desordena franjas, la primera futura de la lista puede no ser la más próxima por hora. Horas fuera de rango se entregan a un `Calendar` permisivo, sin validación previa. No guarda ni lee preferencias, por lo que depende de que el estado haya sido cargado antes.

## 7. Motor predictivo: algoritmo completo y significado de la confianza

Fuente: [OutagePredictionEngine.kt](<E:/LUZ BARINAS/app/src/main/java/com/example/engine/OutagePredictionEngine.kt:1>).

**`OutagePredictionEngine` — L12** es un singleton sin estado mutable propio. Constantes: `DEFAULT_OUTAGE_DURATION_HOURS = 4.0f` y `MODULO_DAY_HOURS = 24`. El motor no hace peticiones ni lee Room; el repositorio obtiene los insumos. Tampoco entrena un modelo, aprende pesos o persiste correcciones.

**`calculateNextOutageWindow(sector, history, weeklyReports = emptyList(), currentTimeMillis = System.currentTimeMillis())` — L17.** Función suspendida que devuelve `OutagePrediction`. `sector` es la unidad seleccionada; `history` contiene intervalos pasados; `weeklyReports` contiene eventos ciudadanos de una semana y `currentTimeMillis` permite fijar el reloj. Su único paso es delegar a `calculateLocalOutageWindow` con los mismos argumentos. La llaman `EnergyRepository.getPredictionForSector` y el test del motor. No tiene cambio de dispatcher ni acceso remoto propio; `suspend` no implica una API externa.

**`calculateLocalOutageWindow(sector, history, weeklyReports = emptyList(), currentTimeMillis = System.currentTimeMillis())` — L26.** Mismos parámetros y resultado, función síncrona. La llama el wrapper anterior. Pasos:

1. Crea calendario `America/Caracas` para el instante recibido.
2. Filtra histórico por igualdad exacta de `sectorId` y ordena inicio descendente. Promedia `durationHours` de **todos** los registros que le pasan ese filtro. Si la media está entre 2 y 8 horas, inclusive, la utiliza; si no, o si no hay registros, usa cuatro horas. Ordenar no limita la muestra; el límite 15 pertenece al DAO.
3. Limpia `sector.rotationBlock` eliminando `BLOQUE`. Si el resultado es A/B/C/D, pide ventana teórica a `findNextWindowForBlock`; si no, considera que no hay ventana.
4. Inicializa el inicio con el teórico o -1, confianza 75 y explicación `Teórico PAC (...)`.
5. Si hay reportes ciudadanos, conserva los que tienen `hasPower = false`, los agrupa por hora del día de Caracas y cuenta eventos. No diferencia aquí días de semana, tipo de reporte, avería/programado, personas ni dispositivos.
6. Busca la hora más frecuente. Solo considera ajuste si esa hora tiene al menos tres reportes. Si difiere más de una hora de la hora teórica —resta absoluta de enteros, sin distancia circular de 24 horas— o si no hay teoría, construye una candidata a esa hora hoy; si ya pasó, la mueve a mañana.
7. Si la candidata es estrictamente anterior a `ahora + 24 h`, reemplaza inicio, explicación por `Ajuste Ciudadano (Realidad > Teoría)` y confianza por `min(99, 70 + 2 × cantidadDeReportesEnHoraPico)`.
8. Si todavía no hay inicio, usa cálculo modular: offsets A=0, B=6, C=12, D=18 y otro=0; hora de hoy = `(díaDelAño × 4 + offset) % 24`. Si esa hora ya pasó, construye la de mañana con el siguiente día del año. Pone confianza 78 y explicación `PAC Rotación (...)`.
9. Si conservó teoría, pone confianza 94 cuando tiene al menos dos históricos del sector, o 88 en otro caso. La explicación indica corte en curso o nombre relativo y etiqueta de ventana.
10. Calcula final sumando la duración media/de respaldo al inicio. Calcula `hoursUntilWindow = max(0, (inicio - ahora) / 3 600 000)` y devuelve el modelo.

**Reglas y límites que afectan los cambios:**

- `weeklyReports` no se filtra por sector ni por antigüedad dentro del motor. El llamador activo sí solicita el sector/semana a Supabase; cualquier otro consumidor debe cumplir ese contrato o mezclaría barrios o periodos.
- Tres reportes repetidos del mismo emisor pueden cambiar la predicción; no existe deduplicación o validación de independencia en esta función.
- Las puntuaciones 88/94/78 y la fórmula ciudadana están escritas como constantes/reglas; no son porcentajes medidos contra un conjunto de evaluación. La UI debería conservar el carácter estimado.
- Los reportes offline enviados tarde llegan con la hora de sincronización por el defecto del uploader; el agrupamiento por hora del motor hereda esa distorsión.
- Un corte activo reconocido por teoría puede sustituirse por una candidata futura ciudadana; no hay una fase específica para conservar “sigue sin luz ahora”.
- La duración final procede de históricos o cuatro horas, **no de `theoreticalPacWindow.endMillis`**. Si el administrador cambia una franja a dos o seis horas, el inicio puede coincidir con el cronograma mientras el final mostrado no coincide.
- La resta horaria trata 23 y 0 como distancia 23, aunque estén a una hora alrededor de medianoche.
- Para A/B/C/D siempre recibe algún objeto de `findNextWindowForBlock`, incluso su ventana inventada. La rotación modular queda para bloque no reconocido; no es el algoritmo principal de todos los sectores, pese al nombre del test antiguo.
- No captura excepciones de sus insumos o del singleton. Una matriz inválida puede propagar error hacia la coroutine que la llamó.

Ejemplo didáctico: sin histórico, una ventana teórica 07–11 produce duración cuatro horas y confianza 88. Con dos históricos de dos horas cada uno, conserva inicio 07 pero calcula final 09 y confianza 94. Si tres reportes sin energía se concentran a las 18 y difieren de la hora teórica, pasa al ajuste ciudadano con confianza 76; esa cifra sale de `70 + 3×2`, no de precisión observada.

## 8. `EnergyRepository`: coordinador de Room y nube

Fuente: [EnergyRepository.kt](<E:/LUZ BARINAS/app/src/main/java/com/example/data/repository/EnergyRepository.kt:31>).

### 8.1 Constructor y propiedades observables

**`EnergyRepository(context, database = AppDatabase.getInstance(context))` — L31.** Conserva contexto y una base, y obtiene sus tres DAOs en L35–37. La inyección opcional de base facilita sustituirla, pero no existe una interfaz de nube inyectada: `cloudSync` — L39 — crea perezosamente `CloudSyncRepository(context, database)`. La instancia principal la posee el ViewModel.

| Propiedad / getter | Comportamiento y consumidor |
|---|---|
| `broadcastNoticeFlow` — L41 | Getter que devuelve el `StateFlow` de aviso de nube; ViewModel lo colecta |
| `updateInfoFlow` — L44 | Getter de versión remota; ViewModel lo colecta; expone `AppUpdateInfo` definido en paquete UI |
| `pacScheduleUpdatedFlow` — L47 | Getter del evento PAC; no crea persistencia ni repite el último evento |
| `allSectorsFlow` — L50 | `SectorDao.getAllSectors().map { entities.map(toDomain) }`; sectores ordenados por nombre y convertidos |
| `unsyncedCountFlow` — L54 | Cuenta reactiva directa del DAO de pendientes |
| `allReportsFlow` — L56 | Convierte todas las entidades de reportes a `CitizenReport`; ViewModel alimenta historial local |

Los tres getters de nube pueden provocar la creación lazy de `cloudSync`, pero no llaman a `startRealtimeSync` automáticamente. Los imports de `ApiClient` y `TelemetryReportRequest` permanecen sin uso: no ejecutan fallback HTTP.

### 8.2 `initializePreloadedDataIfEmpty()` — L60

**Contrato:** sin parámetros; función suspendida ejecutada en IO; no produce un resultado de negocio explícito. La llama el `init` del ViewModel. Si hay errores de base se propagan, pues no existe `try/catch` en esta función.

**Pasos y efectos:**

1. Si el total de sectores es menor que 60, construye e inserta/reemplaza **63 sectores**, con estados, voltajes, contadores, porcentajes, fechas relativas a “ahora”, bloques textuales y polígonos incluidos en Kotlin.
2. Distribución: 18 de A, 19 de B, 16 de C y 10 de D. Los B se siembran como `SCHEDULED_OUTAGE`, voltaje cero y porcentajes elevados; los otros bloques como `NORMAL`, con referencias de voltaje y contadores distintos de cero.
3. Si el histórico está vacío, crea seis registros: tres para Alto Barinas Norte, dos para Alto Barinas Sur y uno para Ciudad Tavacare, con fechas relativas a la ejecución y duraciones cuatro/cinco y media horas.
4. No siembra Supabase ni solicita consentimiento remoto. Son escrituras Room separadas, no una única transacción global.

**Identidades que importan:** los sectores se identifican como `sec_a_alto_barinas_1`, `sec_c_alto_barinas_2`, etc.; los seis históricos usan `sector_alto_barinas_norte`, `sector_alto_barinas_sur` y `sector_ciudad_tavacare`. No hay sectores con esos tres IDs entre los 63 sembrados. Como la predicción consulta por ID exacto, esos ejemplos no alimentan las predicciones de los sectores semilla correspondientes. No hay clave foránea que detecte la desconexión.

**Límites:** el nombre “IfEmpty” no equivale a su condición real `< 60`: una base válida con 59 sectores recibirá 63 valores de demostración y reemplazará filas coincidentes. La precarga y la sincronización de nube se arrancan en coroutines distintas; no existe orden garantizado para sus escrituras. Las fechas recientes de la semilla no indican actividad real recién recibida. Para retirar demostración conviene una migración/marcador explícito, no cambiar arbitrariamente el umbral.

### 8.3 `getPredictionForSector(sectorId)` — L210

**Entrada/retorno:** ID textual → `OutagePrediction?`, suspendida en IO. Devuelve `null` si Room no contiene el sector. La llama `LuzBarinasViewModel.refreshPrediction`.

**Pasos:** obtiene sector y convierte; consulta hasta 15 históricos por el mismo ID y convierte; espera `cloudSync.fetchWeeklyCitizenReports(sectorId)`; entrega ambos conjuntos y reloj actual al motor. Solo lee Room, pero sí realiza una petición de red para reportes. La nube devuelve lista vacía si falla, por lo que el cálculo puede continuar con teoría/histórico; el repositorio no conserva una caché Room de reportes ciudadanos remotos.

**Límites:** la predicción local se demora hasta terminar/fallar esa consulta de red; no emite primero un resultado local y luego otro enriquecido. No captura errores de Room/motor. El ViewModel lanza una coroutine por refresco, sin cancelación asociada aquí: una consulta antigua que termine tarde puede devolver una predicción de otro sector si el consumidor no comprueba identidad antes de actualizar UI.

### 8.4 `submitReport(sectorId, sectorName, hasPower, reportType, voltage, observation = null)` — L226

**Entrada:** ID/nombre del sector; disponibilidad; categoría libre; voltaje opcional; observación opcional. **Retorno:** `Result<Unit>` de Kotlin, suspendido en IO. `success` significa que terminó el flujo local y su intento de envío sin un error exterior capturado; no garantiza sincronización. Lo llama la acción de reporte del ViewModel.

**Pasos exactos:**

1. Inserta `PendingReportEntity` con fecha original actual, observación y `isSynced = false`. Ignora el ID `Long` devuelto por Room.
2. Busca sector local. Si existe, decide estado `NORMAL` con energía; sin energía usa `IRREGULAR_OUTAGE` solo para `FALLA_IRREGULAR` o `AVERIA`, y `SCHEDULED_OUTAGE` para el resto. Voltaje: el aportado o 118/0 según disponibilidad.
3. Incrementa contador en uno. Reduce porcentaje sin luz en ocho puntos con energía, acotado a cero; aumenta catorce sin energía, acotado a cien. Es una regla fija de feedback visual, no cálculo de una muestra ciudadana.
4. Actualiza sector y su fecha. Si no existe sector, conserva igualmente el reporte y continúa.
5. Crea trabajo de `ReportPowerWorker`, adjunta ID, disponibilidad, categoría y voltaje —cero cuando fue nulo—, exige red `CONNECTED` y lo encola como trabajo único `WORK_NAME` con `APPEND_OR_REPLACE`.
6. Intenta inmediatamente `cloudSync.uploadCitizenReport`. Para el nombre usa `sector?.name ?: sectorId`, aunque en Room guardó el `sectorName` del parámetro; la observación sí se pasa.
7. Si el uploader devuelve `true`, vuelve a leer todos los pendientes, toma el último y lo marca sincronizado. Si devuelve `false` o se captura error dentro de este intento, deja pendientes y continúa.
8. Devuelve `Result.success(Unit)`; cualquier excepción del `try` exterior devuelve `Result.failure(e)`.

**Persistencia/red:** escritura de reporte y sector en Room, registro durable de trabajo en WorkManager y una petición Supabase. No usa Retrofit. El envío inmediato se espera, pese al comentario “non-blocking”.

**Defectos que deben conservarse visibles en el manual hasta corregirlos:**

- Marca el último pendiente, no el insertado/enviado. Con dos envíos concurrentes puede confirmar B después de subir A y dejar B sin reintento.
- Worker e intento inmediato pueden enviar la misma fila; no hay reserva ni identificador idempotente del evento en el servidor.
- El worker repite la actualización optimista y sus reintentos pueden incrementar de nuevo el contador; sus reglas de porcentaje/estado difieren de estas.
- Un fallo tras insertar en Room puede devolver `failure` aunque el evento quedó guardado. Repetir el botón puede crear otro evento.
- La observación no se adjunta como `Data` al worker porque este relee el reporte Room; no debe borrarse la fila antes de que el worker la haya leído.
- No se crean/cierran `OutageRecordEntity` al reportar un corte o retorno. Modificar este método no actualiza automáticamente el histórico de duración.

### 8.5 `syncPendingReportsNow()` — L326

**Contrato:** sin parámetros → `Int` cantidad de filas confirmadas, suspendido en IO. Lo llama la sincronización manual del ViewModel.

Lee instantánea de todos los pendientes en orden ascendente; por cada uno envía ID/nombre, disponibilidad, categoría, voltaje y observación. Si recibe `true`, marca **ese `item.id`** y suma uno. Si el uploader devuelve `false`, continúa con los demás. Si una excepción sale del bloque de envío/marcado, rompe el bucle; el uploader normalmente captura sus propias excepciones y devuelve `false`, así que el comentario “si continúa el apagón, detener” no se cumple para esos fallos ordinarios. Devuelve el número confirmado; la lectura inicial del DAO está fuera del `try` interno y puede propagarse.

No actualiza optimismo ni programa un worker nuevo. Puede correr al mismo tiempo que worker/inmediato y duplicar inserciones remotas; no pasa la fecha original al uploader. Cero significa “ninguna confirmada”, tanto si no había pendientes como si todos fallaron.

### 8.6 `registerCommunityLocation(name, municipio = "Barinas", parroquia, block, circuit)` — L351

**Contrato:** textos territoriales y eléctricos → `Result<Sector>`, suspendido en IO. La llama el alta comunitaria del ViewModel.

Construye ID `sec_com_` más nombre en minúsculas sustituyendo cada carácter fuera de `[a-z0-9]` por `_`. Crea sector local `NORMAL`, 118 V, contador uno, porcentaje cero, fecha actual, `isCommunity = true`, bloque recortado y un polígono fijo de ejemplo. El circuito vacío pasa a `Circuito <parroquia>`. Inserta/reemplaza Room; llama al uploader comunitario; **ignora su Boolean**; devuelve éxito con el sector convertido. Solo una excepción que alcance el bloque exterior produce `Result.failure`.

El municipio y parroquia se usan en la petición remota, pero `SectorEntity` no los conserva localmente. Dos nombres iguales en distintas parroquias colisionan; tildes/puntuación también pueden producir IDs equivalentes. Si no hay red, el uploader devuelve `false`, el repositorio informa éxito y no existe outbox comunitaria para reintentar esa operación. El ViewModel añade después una dirección en memoria y muestra “sincronizada con éxito”, aunque ese resultado no se haya comprobado. El polígono no debe interpretarse como ubicación medida.

## 9. `CloudSyncRepository`: modelos remotos, configuración y Realtime

Fuente: [CloudSyncRepository.kt](<E:/LUZ BARINAS/app/src/main/java/com/example/data/repository/CloudSyncRepository.kt:1>). Este archivo contiene tanto los modelos serializables como el cliente de sincronización. No se reproducen aquí valores de llaves, tokens o credenciales. Sus nombres y responsabilidades bastan para mantener el código.

### 9.1 Modelos serializables y propiedad calculada

**`SupabaseAppConfig` — L47.** Dos campos obligatorios: `config_key: String`, nombre de configuración, y `config_value: JsonElement`, contenido JSON. El procesador actual exige que ese elemento pueda convertirse a `JsonObject`; un valor escalar o array puede provocar fallo de lectura del lote inicial.

**`SupabaseSector` — L53.** Representa la fila/registro de `sectors` recibida o enviada.

| Campo | Tipo / respaldo | Significado y uso |
|---|---|---|
| `id` | `String`, obligatorio | Clave del sector |
| `name` | `String? = null` | Nombre opcional; al actualizar conserva el local si falta |
| `municipio`, `parroquia` | `String? = null` | Metadatos territoriales; el espejo Room actual no los almacena |
| `circuitCode` | `String? = null` | Circuito opcional; conserva local o usa texto de respaldo |
| `status` | `String`, obligatorio | Nombre del estado; no hay validación de enum al recibirlo |
| `voltage` | `Double = 118.0` | Valor remoto; se reduce a `Float` en Room |
| `confirmedReportsCount` | `Int = 0` | Contador remoto |
| `withoutPowerPercentage` | `Int = 0` | Porcentaje remoto; al copiar no se acota a 0–100 |
| `rotationBlock` | `String? = null` | Bloque remoto opcional |
| `isCommunity` | `Boolean? = null` | Variante camelCase de marca comunitaria |
| `is_community` | `Boolean? = null` | Variante snake_case de la misma marca |
| `lastUpdatedMillis` | `Long = 0L` | Fecha remota; cero indica que el código debe recurrir al valor local/actual |

**`SupabaseSector.resolvedIsCommunity` — getter L68.** Sin argumentos, devuelve `Boolean`: prioriza `isCommunity` cuando no sea nulo —incluido `false`—; luego `is_community`; si ninguno está presente, acepta IDs con prefijo `sec_com_` o `sec_community_`. La utilizan la descarga completa y `updateLocalSector`. No escribe datos; resuelve compatibilidad de dos convenciones. Si ambos campos discrepan, camelCase gana silenciosamente.

**`SupabaseCitizenReport` — L77.** Campos: `sectorId: String`, `sectorName: String`, `hasPower: Boolean`, `reportType: String`, `voltage: Double`, `observation: String? = null`, `deviceOrigin: String` y `timestamp: Long`. Es el contrato activo para insertar/consultar `citizen_reports`. No contiene ID de evento, ID local Room ni fecha de recepción separada. `deviceOrigin` es una etiqueta fija de aplicación, no una identidad única y verificable de cada dispositivo.

**`SupabaseCommunityLocation` — L89.** Campos obligatorios: `id`, `name`, `municipio`, `parroquia`, `block`, `circuitCode`, `status`, `rotationBlock` como `String`; `voltage: Double`; `confirmedReportsCount: Int`; `withoutPowerPercentage: Int`; `submittedAt: Long`. Representa lo enviado a `community_locations`. No hay campo de aprobación en este modelo; el código rellena estado eléctrico, contador y bloque incluso para una solicitud nueva.

### 9.2 Constructor, estado y cliente

**`CloudSyncRepository(context, database = AppDatabase.getInstance(context))` — L104.** Mantiene contexto/base y un `CoroutineScope(Dispatchers.IO)` independiente del `viewModelScope`. `sectorsJob` guarda el trabajo del sector seleccionado; `appConfigJob` guarda el colector de configuración. No se guarda en una propiedad el colector de la tabla global de sectores. Las instancias las crean `EnergyRepository.cloudSync` y `ReportPowerWorker`; este último necesita solo el uploader.

| Declaración | Propósito y comportamiento |
|---|---|
| `_broadcastNoticeFlow` / `broadcastNoticeFlow` — L113–114 | Mutable/solo lectura del último `BroadcastNotice?`, inicialmente nulo |
| `_updateInfoFlow` / `updateInfoFlow` — L116–117 | Mutable/solo lectura de `AppUpdateInfo?`, inicialmente nulo |
| `_donationConfigFlow` / `donationConfigFlow` — L118–119 | Mutable/solo lectura de `DonationConfig?`, inicialmente nulo; lo observa directamente el ViewModel |
| `_pacScheduleUpdatedFlow` / `pacScheduleUpdatedFlow` — L121–122 | `MutableSharedFlow<Long>(extraBufferCapacity = 1)` expuesto como solo lectura; no configura replay. Anuncia marcador de actualización, no contenido completo |
| `syncPrefs` — L124 | `SharedPreferences("cloud_sync_prefs", MODE_PRIVATE)`, con `last_seen_broadcast_timestamp` y `last_applied_pac_updated_at` |
| `supabaseUrl`, `supabaseKey` — L126–127 | Valores de conexión incorporados en código. Mantener configuración y políticas del servidor coordinadas; este manual omite sus valores |
| `supabase` — lazy L129 | Crea `SupabaseClient` con URL/llave e instala `Postgrest` y `Realtime`. No instala Auth ni inicia una sesión de usuario en este archivo |
| `jsonFormat` — L139 | `Json { ignoreUnknownKeys = true }` para decodificar registros de eventos. No es una validación de rangos ni esquema de negocio |
| `TAG` — L566 | Etiqueta privada para registros de error |

Las propiedades de solo lectura protegen el setter del flujo, pero sus valores PAC siguen viniendo de un singleton mutable externo. No existe cifrado adicional, diario de cambios remoto o cola de configuración en estas declaraciones.

### 9.3 `startRealtimeSync()` — L141

**Contrato:** sin parámetros, devuelve `Unit` enseguida después de lanzar coroutine. La llama el `init` del ViewModel. Que la llamada retorne no significa que haya conectado.

**Secuencia en IO:**

1. Conecta Realtime.
2. Obtiene canal `public-app_config`, configura `postgresChangeFlow<PostgresAction>` para tabla `app_config` y lo lanza en el scope; guarda ese colector en `appConfigJob`.
3. Ante `Insert` o `Update` procesa el registro mediante `handleConfigChange`; ignora otros eventos, incluidos `Delete`.
4. Se suscribe al canal. Descarga después todas las filas de `app_config`, decodifica y llama a `processConfig` para cada una.
5. Descarga todas las filas de `sectors` con un `select` sin paginación explícita. Para cada una busca si existe en Room. Si existe, copia estado/cifras y campos opcionales presentes; si es nueva, crea entidad con nombre/circuito/bloque de respaldo, fecha válida o actual y un mismo polígono fijo de ejemplo.
6. Inserta/reemplaza por lote esas entidades. No borra las filas locales que ya no estén en el resultado remoto.
7. Crea canal `public-sectors-global`, escucha `Insert` y `Update` de toda `sectors`, decodifica con `jsonFormat` y llama a `updateLocalSector`. Ignora `Delete`. Lanza colector en `scope`, sin guardar su `Job`, y se suscribe.
8. Consulta `AppUpdater.checkForUpdates`; si devuelve información, la publica en `_updateInfoFlow`.
9. Cualquier excepción que alcance el `try` de esta coroutine se registra como fallo de inicio.

**Efectos:** conexión WebSocket, varias consultas HTTP, escrituras Room, eventual actualización de preferencias y notificaciones al procesar configuración, consulta GitHub. La descarga inicial escribe aun si el contenido coincide; el manejador incremental usa comparación.

**Límites:**

- Todo el arranque está en una misma secuencia: fallar al leer configuración impide descargar/suscribir la tabla global y comprobar GitHub en ese intento. No hay bucle de reintento de la instantánea ni observador de conectividad que repita `startRealtimeSync`.
- La suscripción global se crea después de descargar sectores: hay una ventana entre lectura y escucha. La reconexión del WebSocket, si la gestiona el SDK, no constituye por sí sola una reconciliación completa de todas las filas.
- No hay guardia de inicio idempotente en este repositorio. Repetir la llamada puede volver a hacer descargas y registrar colectores; el comportamiento exacto de reutilizar canales pertenece al SDK.
- El `try` exterior no envuelve cada evento futuro del colector global, lanzado por separado. Un registro que no decodifique correctamente puede terminar su colección; no hay `catch`/reintento dentro de ese flujo.
- Ignorar borrados deja sectores y configuración obsoletos localmente. Tampoco se consultan tombstones ni una versión por fila para resolver conflictos.
- El servidor puede limitar la cantidad de filas devueltas; aquí no hay recorrido paginado para garantizar una lista mayor al límite configurado.
- GitHub puede sobrescribir un aviso de actualización recibido antes desde `app_config`; no hay selección explícita de la versión más alta entre ambas fuentes.

### 9.4 `handleConfigChange(record)` — L235, privada

Recibe `JsonObject`, devuelve `Unit`. Extrae `config_key` como cadena y `config_value` como objeto; si falta cualquiera termina. Entrega ambos a `processConfig`; captura y registra excepciones. La llaman eventos insert/update del canal de configuración. Puede desencadenar preferencias, notificaciones y cambios de flujos a través del procesador. No trata `Delete` ni conserva el registro completo para auditoría.

### 9.5 `processConfig(key, obj)` — L245, privada

Nombre de configuración y objeto JSON → `Unit`. La llama la descarga inicial o `handleConfigChange`. No hace una petición adicional propia; despacha cuatro casos:

| Clave | Pasos / valores de respaldo | Efectos y límites |
|---|---|---|
| `pac_schedule` | Llama `applyRemotePacScheduleJson(obj)` | Cambia cronograma global, guarda preferencias y emite evento si se acepta la versión |
| `broadcast_notice` | `active` falso por defecto. Si está activo, lee título —“Aviso Informativo”—, mensaje vacío, nivel INFO y fecha —reloj actual—; construye aviso y actualiza StateFlow. Compara fecha con `last_seen_broadcast_timestamp` | Si fecha es mayor guarda marcador y llama `NotificationHelper.showBroadcastNoticeNotification`. Guarda “visto” antes de saber si Android muestra la notificación; no reintenta automáticamente tras conceder permisos. Si está inactivo pone flujo a nulo, sin limpiar aquí una notificación ya mostrada. Una fecha ausente cambia con cada lectura y puede parecer aviso nuevo |
| `donations_config` | Lee `paypal`, `binance`, `pmBank`, `pmPhone`, `pmId`, cada una vacía si falta; crea `DonationConfig` | Solo StateFlow en memoria; no caché local completa, validación de enlaces ni transacción monetaria |
| `version` | Lee `versionCode` o 1 y lo compara con `BuildConfig.VERSION_CODE`. Si es mayor crea `AppUpdateInfo` con nombre o “1.0”, notas o texto genérico, `apkDownloadUrl` o vacío, `isMandatory` o falso | Solo publica información. Si la configuración baja de versión o deja de indicar actualización, no limpia explícitamente un aviso anterior. La UI actual abre Play, aunque este modelo conserve URL de APK |

Cualquier otra clave se ignora. No existe `try/catch` propio; eventos pasan por `handleConfigChange`, que sí captura, mientras un error al procesar una fila del lote inicial termina ese arranque.

### 9.6 `listenToSector(sectorId)` — L297

ID de sector → `Unit`; cancela el `sectorsJob` previo y lanza uno nuevo. La llama el ViewModel al resolver/cambiar sector o ubicación.

Primero consulta `sectors` con filtro `id = sectorId`; si la lista no está vacía aplica el primer registro. Luego obtiene canal `public-sectors-<ID>`, filtra los cambios por ID, procesa únicamente `Update` mediante `updateLocalSector`, lanza ese colector como hijo de la coroutine y se suscribe. Captura y registra errores de su bloque inicial. Efectos: HTTP, suscripción WebSocket y actualización Room.

No observa `Insert` para un ID que no existía ni `Delete`; el canal global cubre inserciones solo si quedó funcionando. Hay intervalo lectura→suscripción. Cancelar el Job termina su coroutine/colector, pero este método no llama explícitamente a eliminar/desuscribir el canal anterior del cliente. La tabla completa ya se observa en otro flujo: ambos pueden intentar escribir el mismo sector y no hay transacción que serialice sus lecturas/copies.

### 9.7 `updateLocalSector(s)` — L329, privada y suspendida

Recibe `SupabaseSector`, devuelve `Unit`. La llaman los colectores global y seleccionado, además de la consulta inicial de `listenToSector`.

1. Busca entidad local y resuelve `isCommunity`.
2. Si existe, calcula cambio por estado, diferencia de voltaje **mayor a 0.1**, contador, porcentaje, bloque no nulo distinto, marca comunitaria, nombre no nulo distinto o circuito no nulo distinto.
3. Si hay cambio, copia datos remotos, conserva opcionales ausentes y conserva fecha anterior cuando la recibida no es positiva; escribe con `updateSector`.
4. Si no existe, inserta con respaldos: nombre=ID, circuito “Circuito Urbano”, bloque “Bloque C”, fecha actual si falta y polígono constante de ejemplo.

No consulta red dentro de la función. No compara versiones o fecha para rechazar eventos antiguos. Una modificación solo de `lastUpdatedMillis` se ignora, y una diferencia de voltaje de 0.1 o menos también; pueden quedarse fechas/referencias sin refrescar. Municipio/parroquia no se guardan. Valores de porcentaje, categoría o bloque inválidos no se rechazan. La lectura y escritura separadas permiten sobrescribir otra actualización concurrente con una copia anterior.

### 9.8 `uploadCitizenReport(sectorId, sectorName, hasPower, reportType, voltage, observation = null)` — L377

Textos de identidad/categoría, disponibilidad, voltaje opcional y observación → `Boolean`, suspendida en IO. Consumidores: intento inmediato del repositorio, sincronización manual y `ReportPowerWorker`.

Construye `SupabaseCitizenReport`: usa el voltaje dado o 118/0 según disponibilidad, lo convierte a `Double`, conserva observación y fija `deviceOrigin` a una etiqueta constante. **Fija `timestamp = System.currentTimeMillis()` en el momento de enviar**, no recibe el tiempo original Room. Inserta en `citizen_reports`; si termina devuelve `true`, ante excepción registra error y devuelve `false`.

Es una escritura HTTP; no marca Room, no actualiza `sectors`, no calcula agregado ni crea histórico. Los comentarios que sugieren triggers no implementan triggers en Android: si existen deben verificarse en el backend. No hay ID idempotente, comprobación de duplicados, autenticación de ciudadano ni control de tasa aquí. Un error se reduce a Boolean, por lo que el llamador no distingue falta de red, rechazo de permisos, schema inválido o timeout. El defecto temporal afecta especialmente el significado de los reportes offline y la predicción por hora.

### 9.9 `fetchWeeklyCitizenReports(sectorId = null)` — L411

ID opcional → `List<SupabaseCitizenReport>`, suspendida en IO. La llama `EnergyRepository.getPredictionForSector` pasando ID.

Calcula límite `ahora - 7×24×60×60×1000`, hace `select` en `citizen_reports` con `timestamp >= límite` y agrega igualdad de `sectorId` si no es nulo. Decodifica lista y la devuelve. Si cualquier excepción ocurre registra error y devuelve lista vacía.

Lee red y no persiste una copia local. “Semanal” es una ventana móvil de siete días desde hora actual, no lunes–domingo. No pone orden, límite superior ni paginación explícita: una fecha futura satisface el filtro, y el límite de filas del servidor puede truncar. Lista vacía es indistinguible de fallo remoto para el motor. Sin sector solicita todos los sectores; quien use ese resultado con un sector debe filtrarlo.

### 9.10 `publishPacScheduleToCloud()` — L430

Sin parámetros → `Boolean`, suspendida en IO. **Stub:** dentro del `try` solo devuelve `true`; no serializa, no hace `upsert`, no manda red y no escribe preferencias. Lo llaman las acciones PAC del ViewModel y `publishScheduleNotification`. El `catch` devuelve `false` si hubiera una excepción dentro, pero no hay operación real que confirme publicación.

Mantener visible este hecho: los mensajes UI de “publicado/sincronizado” no demuestran propagación desde Android. Para implementar esta capacidad habrá que definir payload validado, autorización administrativa, control de concurrencia y error real. También es válido retirar las acciones Android si la administración se decide exclusivamente web; eso es una decisión de producto, no una capacidad ya presente.

### 9.11 `uploadCommunityLocation(name, municipio = "Barinas", parroquia, block, circuit)` — L441

Textos de nombre/territorio/bloque/circuito → `Boolean`, suspendida en IO. La llama el alta del repositorio.

1. Genera ID por nombre con la misma sustitución `[a-z0-9]` que el repositorio.
2. Crea `SupabaseCommunityLocation`: campos recortados, circuito vacío “Circuito Urbano”, fecha actual, estado normal, 118 V, contador uno, porcentaje cero y bloque/rotationBlock con el texto recortado original.
3. Hace `upsert` en `community_locations`.
4. Limpia la palabra `Bloque` del código, con respaldo C si queda vacío.
5. Crea `SupabaseSector` con el mismo ID, nombre/parroquia/circuito/estado/cifras, bloque limpio y fecha de solicitud; hace `upsert` en `sectors`.
6. Devuelve `true`; si falla cualquiera de los pasos captura/loguea y devuelve `false`.

Son **dos escrituras independientes**, no una transacción remota. Puede existir solicitud sin sector si falla el segundo `upsert`. No implementa espera de aprobación: intenta escribir también `sectors` inmediatamente; la aceptación y visibilidad efectivas dependen de políticas/automatismos del backend, que no se comprueban desde este archivo. Las altas homónimas comparten ID; un reenvío puede reemplazar datos anteriores. No hay outbox ni reintento interno, y el repositorio actual ignora el resultado. El circuito de respaldo difiere del local `Circuito <parroquia>`.

### 9.12 `applyRemotePacScheduleJson(obj)` — L489, privada

Objeto JSON de cronograma → `Unit`. Solo la llama `processConfig` para `pac_schedule`.

1. Lee `version` como `Long` o cero; `updatedAt` como `Long` o el valor de versión. Lee marcador local `last_applied_pac_updated_at`.
2. Acepta si `updatedAt` supera el último aplicado, **o** si versión supera `PacScheduleData.scheduleVersion`, **o** si hay fecha remota positiva y la versión del singleton es cero.
3. Si `slots` existe y no está vacío, vacía `activeSlots` y agrega por posición `PacSlot`; defaults por campo: etiqueta `Turno N`, inicio cero, final cuatro.
4. Si `matrixRows` no está vacío, construye filas desde cada objeto `cells`; cuando falta cells utiliza siete `-`. Si no se usa ese formato y hay `matrix`, copia sus arrays de cadenas. Un array vacío no borra la matriz anterior.
5. Para cada `sectorsA/B/C/D` presente, vacía y repone la lista correspondiente; aquí sí un array vacío deja lista vacía.
6. Asigna `scheduleVersion = remoteUpdatedAt`, guarda marcador de última aplicación, llama `PacSchedulePrefs.saveSchedule` y hace `tryEmit(remoteUpdatedAt)`.
7. Captura/loguea cualquier excepción, sin devolver un estado ni revertir pasos anteriores.

Efectos: mutación global, preferencias locales y evento de cambio; no devuelve un ACK al servidor y no rearma alarmas por sí misma. La UI colecta el evento para snapshots/predicción.

Límites: no valida cantidad de columnas/filas, igualdad matriz/franjas, horas, bloques, solapamientos ni orden cronológico. Una excepción después de `activeSlots.clear()` deja estado parcial, y las listas mutables pueden leerse mientras se reconstruyen. Las dos nociones de versión/fecha se comparan con OR, pero al guardar se conserva solo `updatedAt` como versión efectiva; no hay una versión de esquema separada. Guardar primero el marcador y después el cronograma no es una transacción entre archivos de preferencias. `tryEmit` no garantiza que un consumidor tardío vea un evento pasado, ya que no hay replay.

### 9.13 `stopRealtimeSync()` — L555

Sin parámetros → `Unit`. La llama `LuzBarinasViewModel.onCleared`. Cancela `sectorsJob` y `appConfigJob`; lanza otra coroutine que intenta `supabase.realtime.disconnect()` y silencia cualquier excepción.

No cancela el scope completo, no conserva/cancela explícitamente el Job de sectores globales y no vacía los StateFlow. La desconexión se solicita de forma asíncrona: la llamada termina antes de saber su resultado. No cierra aquí Room ni elimina pendientes. Si se usa esta clase en nuevos componentes con ciclo de vida distinto, debe definirse quién es dueño del scope/cliente y cómo se cierran todos los trabajos.

## 10. Retrofit residual: cliente, endpoints y DTO

### 10.1 `ApiClient`

Fuente: [ApiClient.kt](<E:/LUZ BARINAS/app/src/main/java/com/example/data/remote/ApiClient.kt:10>).

`ApiClient` — L10 — es un singleton con construcción lazy. No define funciones nombradas; su comportamiento vive en inicializadores de propiedades:

| Propiedad | Pasos y efecto al utilizarse |
|---|---|
| `BASE_URL` — L12 | Base constante HTTPS de Vercel con barra final; debe concordar con las rutas relativas del contrato. No se utiliza como URL de Supabase |
| `moshi` — L14 | Construye `Moshi.Builder`, agrega `KotlinJsonAdapterFactory` y devuelve el serializador. Memoria; sin petición |
| `loggingInterceptor` — L20 | Construye interceptor HTTP; `BODY` si `BuildConfig.DEBUG`, `NONE` en otro caso. Si este cliente se reactiva, los cuerpos de reportes/observaciones pueden aparecer en logs de depuración |
| `okHttpClient` — L26 | Cliente con timeouts de conexión, lectura y escritura de diez segundos y el interceptor anterior |
| `api` — L35 | Construye Retrofit con base, cliente y converter Moshi, y crea implementación de `LuzBarinasApi`. Su acceso construye objetos; las llamadas de métodos son las que realizarían peticiones |

No hay referencia `ApiClient.api` en las fuentes Android revisadas. Los imports presentes en repositorio/worker son residuales. Para reactivar una ruta no basta con editar la URL: hay que elegir fuente de verdad, mapear DTO a Room y evitar que el mismo reporte se inserte además por Supabase.

### 10.2 `LuzBarinasApi`: cada operación declarada

Fuente: [LuzBarinasApi.kt](<E:/LUZ BARINAS/app/src/main/java/com/example/data/remote/LuzBarinasApi.kt:12>).

Son funciones suspendidas de interfaz, implementadas dinámicamente por Retrofit. Todas **carecen de llamadores activos encontrados**. La tabla describe el contrato Android, no acredita que cada ruta exista o acepte actualmente ese payload en el servidor.

| Función y línea | Entrada / retorno | Petición que declara y propósito |
|---|---|---|
| `getSectors()` — L14 | Ninguna → `Response<List<SectorDto>>` | `GET api/v1/sectors`, listado de unidades eléctricas |
| `getSectorDetails(sectorId)` — L17 | ID en segmento de ruta → `Response<SectorDto>` | `GET api/v1/sectors/{sectorId}`, detalle de una unidad |
| `submitPowerReport(request)` — L22 | `TelemetryReportRequest` como cuerpo → `Response<TelemetryReportResponse>` | `POST api/v1/telemetry/report`, envío de evento ciudadano |
| `getOutagePrediction(sectorId)` — L27 | ID como query → `Response<OutagePrediction>` | `GET api/getOutagePrediction?sectorId=...`, contrato de cálculo remoto residual |
| `getMonthlyWeeks(monthName, totalDays)` — L32 | Nombre y días como queries → `Response<MonthlyWeeksResponse>` | `GET api/getMonthlyWeeks?monthName=...&totalDays=...`, contrato mensual remoto residual |

Cada operación implicaría red y decodificación, sin persistir Room automáticamente. `Response<T>` permite comprobar código HTTP, cuerpo y error: una respuesta HTTP no exitosa no se transforma por esta interfaz en `Result.failure` ni en un modelo de éxito. Errores de conexión/serialización pueden propagarse como excepciones. Aquí no hay reintentos, validación de argumentos o fallback declarado.

### 10.3 `TelemetryDtos.kt`: contrato JSON campo a campo

Fuente: [TelemetryDtos.kt](<E:/LUZ BARINAS/app/src/main/java/com/example/data/remote/dto/TelemetryDtos.kt:1>). Las tres clases usan `@JsonClass(generateAdapter = true)` y anotaciones `@Json` explícitas. Sus nombres JSON **no son** los nombres camelCase usados por el contrato Supabase activo.

**`SectorDto` — L7.** Todos sus campos son obligatorios: `id: String → id`; `name: String → name`; `circuitCode: String → circuit_code`; `status: String → status`; `voltage: Float → voltage`; `confirmedReports: Int → confirmed_reports`; `withoutPowerPercentage: Int → without_power_percentage`; `lastUpdated: Long → last_updated`; `rotationBlock: String → rotation_block`; `polygonPoints: String → polygon_points`. No incluye `isCommunity`, municipio o parroquia ni función `toDomain`. Si se reactiva, hay que implementar conversión y aclarar unidad temporal de `last_updated` con el backend.

**`TelemetryReportRequest` — L21.** `sectorId: String → sector_id`; `hasPower: Boolean → has_power`; `timestamp: Long → timestamp`; `reportType: String → report_type`; `voltageReading: Float? = null → voltage_reading`; `observation: String? = null → observation`; `clientDeviceId: String = "barinas_citizen_app" → client_device_id`. Este contrato sí permite proporcionar fecha original, pero no se usa actualmente para resolver el defecto temporal del uploader Supabase. La etiqueta por defecto de dispositivo tampoco identifica un dispositivo único.

**`TelemetryReportResponse` — L32.** `success: Boolean → success`, resultado de negocio; `message: String → message`, explicación; `updatedSectorStatus: String? → updated_sector_status`, estado opcional; `totalSectorReports: Int? → total_sector_reports`, contador opcional. Los dos campos nulos no tienen valor por defecto en el constructor: aceptar `null` no significa que todos los servidores/serializadores permitan omitir la propiedad sin más. No actualiza por sí mismo sectores o reportes locales.

## 11. `AppUpdater`: detección GitHub y apertura de Google Play

Fuente: [AppUpdater.kt](<E:/LUZ BARINAS/app/src/main/java/com/example/utils/AppUpdater.kt:13>).

**`AppUpdater` — L13** es un objeto utilitario sin estado persistente. El modelo devuelto, `AppUpdateInfo`, se define en el ViewModel y contiene `versionCode: Int`, `versionName: String`, `releaseNotes: String`, `downloadUrl: String` e `isMandatory: Boolean`. Es una dependencia de utilidades/datos hacia el paquete UI que conviene tener presente al reorganizar capas.

### 11.1 `checkForUpdates()` — L15

**Entrada/retorno:** sin argumentos → `AppUpdateInfo?`, suspendida en IO. La llama `CloudSyncRepository.startRealtimeSync` al final de su secuencia.

1. Abre conexión HTTP a la API pública de la última release de GitHub del repositorio Android configurado; declara método GET y encabezado Accept de GitHub.
2. Si el código no es 200, devuelve `null`.
3. Lee cuerpo completo a cadena y lo interpreta con `JSONObject`.
4. Obtiene `tag_name` obligatorio y `body` con notas por defecto.
5. Elimina **todas** las letras `v` —ignorando mayúsculas— del nombre remoto y del `BuildConfig.VERSION_NAME`, recorta y separa por puntos. Cada componente que no convierta a entero pasa a cero.
6. Compara componentes de izquierda a derecha; los ausentes valen cero. Al primer componente remoto mayor marca nueva; al primero menor termina sin marcar.
7. Si es nueva, crea `AppUpdateInfo` con `versionCode = BuildConfig.VERSION_CODE + 1` —valor artificial, no recuperado del APK—, nombre/notas remotos, URL `market://details?id=<paquete configurado>` e indicador obligatorio si las notas contienen `[MANDATORY]`.
8. Si no es nueva, devuelve `null`; ante excepción imprime stacktrace y devuelve `null`.

**Efectos:** red GitHub y log de errores; no modifica preferencias, no descarga APK ni toca Room. **Límites:** no utiliza versión instalada de Play ni verifica disponibilidad regional/canal; no procesa SemVer completo —prerelease/sufijos pueden pasar componentes a cero—; no fija timeouts propios ni llama `disconnect` en un `finally`. Un error y “no hay actualización” se representan con el mismo nulo. El arranque de sincronización puede omitir esta llamada si fallan pasos Supabase anteriores.

### 11.2 `openPlayStoreForUpdate(context)` — L71

**Entrada/retorno:** `Context` → `Unit`, suspendida en `Dispatchers.Main`. La llama `UpdatePromptDialog` al aceptar actualización.

Intenta abrir `ACTION_VIEW` con esquema `market://details?id=...` para el application ID codificado y flag `NEW_TASK`. Si no hay actividad que atienda ese esquema, abre la ficha HTTPS de Google Play. Ante cualquier otro fallo imprime stacktrace y muestra Toast de imposibilidad de abrir Play.

No recibe `AppUpdateInfo`, por lo que no elige la URL `apkDownloadUrl` de Supabase ni la release GitHub. No descarga, comprueba hash/firma, instala ni verifica que el usuario actualice. En el diálogo, después de esta llamada se ejecuta `onDismiss`, incluso cuando la utilidad capturó un fallo y solo mostró Toast. El código actual tampoco incluye `downloadAndInstallLatestRelease` o `installApk` descritos en documentación antigua. Si cambia el application ID, revisar esta constante además de Gradle y publicación de la tienda.

## 12. Pruebas Android existentes: qué comprueba cada una

Estas fichas documentan las pruebas presentes. **No se ejecutaron para crear este capítulo**; la existencia de un archivo o imagen de referencia no acredita que la suite pase. Las pruebas no deben enviarse contra el backend real para verificar la cola: conviene sustituir cliente/tiempo y usar almacenamiento aislado.

### 12.1 `ExampleUnitTest.kt`

Fuente: [ExampleUnitTest.kt](<E:/LUZ BARINAS/app/src/test/java/com/example/ExampleUnitTest.kt:12>).

**`ExampleUnitTest` — L12** agrupa una prueba JUnit local. **`test modular arithmetic outage prediction engine` — L14** es el nombre exacto Kotlin entre backticks. No recibe parámetros, no tiene un resultado de negocio; JUnit la invoca y las aserciones fallidas interrumpen el test.

Usa `kotlinx.coroutines.test.runTest`. Construye un `Sector` A, calcula reloj real `now`, crea dos históricos de cuatro horas con el mismo ID y llama a `calculateNextOutageWindow` sin reportes semanales. Comprueba objeto no nulo, ID, duración 3.5–4.5, confianza al menos 70, inicio no anterior a `now - 1000` y final posterior al inicio.

No utiliza Room/red ni prueba la fórmula modular exacta: como el bloque A es reconocido, el motor toma teoría PAC. La expectativa de inicio futuro puede fallar cuando `now` cae en una ventana activa, pues el motor legítimamente devuelve su inicio pasado. Usa reloj y singleton global sin reset, de modo que no aísla fecha o cronograma de otras pruebas. Tampoco verifica medianoche, D sin ventanas, reporte tardío, manipulación por duplicados, cálculo mensual ni final de franja personalizada.

### 12.2 `ExampleRobolectricTest.kt`

Fuente: [ExampleRobolectricTest.kt](<E:/LUZ BARINAS/app/src/test/java/com/example/ExampleRobolectricTest.kt:15>).

**`ExampleRobolectricTest` — L17** usa `RobolectricTestRunner` y `@Config(sdk = [36])`: emula APIs Android en prueba local.

**`read string from context` — L20.** Sin parámetros/retorno de negocio. JUnit obtiene contexto de aplicación, lee `R.string.app_name` y exige texto exacto `PAC Barinas`. Solo lee recursos del entorno de prueba. Un cambio de nombre legítimo obliga a revisar esa expectativa; no comprueba pantallas, idiomas o conexión.

**`verify room sector dao insert and retrieve` — L27.** Sin parámetros/retorno de negocio; `runBlocking`. Obtiene contexto y `AppDatabase.getInstance`, crea un sector de prueba con ID fijo, estado normal, 120 V, bloque textual A y tres vértices. Inserta con `insertOrUpdateSector`, consulta por ID y exige fila no nula, nombre “Don Samuel” y bloque “Bloque A”. Efecto: escritura/lectura SQLite del sandbox de Robolectric; no hay Supabase. No construye DB en memoria ni cierra/limpia explícitamente el singleton; no prueba migración, índices, observación, pendientes o transacciones.

### 12.3 `GreetingScreenshotTest.kt`

Fuente: [GreetingScreenshotTest.kt](<E:/LUZ BARINAS/app/src/test/java/com/example/GreetingScreenshotTest.kt:17>).

**`GreetingScreenshotTest` — L20** usa Robolectric con gráficos nativos, cualificadores Pixel8 y SDK 36. **`composeTestRule` — L22** es regla JUnit creada con `createComposeRule`, que administra el contenido Compose de la prueba.

**`greeting_screenshot()` — L25.** Sin parámetros/retorno de negocio. JUnit configura contenido con `MyApplicationTheme(darkTheme = true)` y un `StatusBadge(NORMAL, isLarge = true)`, luego captura la raíz con `captureRoboImage(filePath = "src/test/screenshots/greeting.png")`. Efecto: captura/artefacto gráfico del entorno de test; el modo de registrar/comparar imágenes depende de cómo se invoquen tareas Roborazzi. El nombre “greeting” no refleja que hoy captura una insignia. No captura dashboard, flujos offline, accesibilidad, múltiples densidades o todos los estados.

La imagen existente está en [greeting.png](<E:/LUZ BARINAS/app/src/test/screenshots/greeting.png>). No se regeneró ni modificó al documentar.

### 12.4 `ExampleInstrumentedTest.kt`

Fuente: [ExampleInstrumentedTest.kt](<E:/LUZ BARINAS/app/src/androidTest/java/com/example/ExampleInstrumentedTest.kt:14>).

**`ExampleInstrumentedTest` — L15** usa `AndroidJUnit4` y requiere dispositivo/emulador con instrumentación. **`useAppContext()` — L17**, sin parámetros/retorno de negocio, obtiene `InstrumentationRegistry.getInstrumentation().targetContext` y compara su `packageName` con `BuildConfig.APPLICATION_ID`. Solo comprueba el contexto del paquete instalado para prueba; no valida firmado, API, red o datos.

### 12.5 Cómo convertir una corrección en una prueba útil

Para el motor usar instantes fijos de Caracas y reconstruir estado PAC antes/después; comprobar valores exactos de inicio/final, no solo “confianza ≥70”. Para Room, crear bases aisladas y probar conservación de un reporte pendiente durante migraciones. Para cola, simular ACK perdido y dos emisores concurrentes sobre el mismo UUID; el resultado exigible debe ser un evento remoto. Para alertas, simular proceso nuevo con preferencias personalizadas, sin crear ViewModel. Esos son cambios recomendados; las pruebas correspondientes no existen todavía en estos cinco métodos.

Las tareas locales se pueden solicitar desde la raíz con `gradlew.bat :app:testDebugUnitTest --offline --no-daemon` si las dependencias están disponibles; las instrumentadas requieren además emulador/dispositivo. `--offline` de Gradle controla resolución de dependencias, no bloquea por sí mismo todas las conexiones que el código de un test intentara hacer. El capítulo de ejecución general detalla requisitos y limitaciones del entorno.

## 13. Flujos prácticos para diagnosticar o mantener sin asistencia

### 13.1 Inicio con datos locales y llegada de nube

1. El ViewModel construye repositorio, carga PAC de preferencias y copia snapshots a su estado.
2. Arranca `startRealtimeSync` y, en otra coroutine, `initializePreloadedDataIfEmpty`.
3. Colecta lista de sectores Room. La semilla o la nube disparan nuevas emisiones y selección/predicción.
4. La nube procesa configuración y escribe sectores. El consumidor de `pacScheduleUpdatedFlow` reconstruye snapshots y predicción.
5. Selección de sector abre además `listenToSector`, aunque ya exista escucha global.

Si la pantalla muestra cifras recientes recién instalada y sin conexión, comprobar si proceden de los 63 valores sembrados. Si no llegan datos al recuperar señal, comprobar en qué paso abortó el arranque; no asumir que reconectar WebSocket ejecutó la descarga que quedó sin realizar. No borrar la DB para “arreglar conexión”: también borraría la cola de ciudadanos.

### 13.2 Reporte con observación durante un apagón

Ejemplo: ciudadano del sector S reporta sin luz a las 19:10 con observación “se escuchó un transformador”.

1. `submitReport` guarda hora 19:10, categoría y texto en `pending_reports`.
2. Modifica solo el estado local de S y registra worker conectado.
3. Si el envío inmediato falla, la fila queda pendiente y el historial local puede mostrarla.
4. A las 23:00, el worker lee esa misma observación y sube el evento. La implementación actual lo fecha a las 23:00; ese comportamiento debe corregirse para preservar 19:10.
5. Al aceptar el servidor, el worker marca el ID Room enviado. Si hay fallo/ACK perdido, lo reintentará sin identidad remota estable y puede duplicarlo.

Para seguir el texto revisar `CitizenReport.observation`, `PendingReportEntity.observation`, migración 2→3, parámetros de `submitReport`, worker, `SupabaseCitizenReport` y uploader. No se necesita incorporar observación al payload WorkManager si sigue releyendo Room; sí se necesita conservar la fila. Para proteger contenido personal, evitar logs de cuerpo si se reactiva el cliente Retrofit en debug.

### 13.3 Cambio de horario PAC desde administración

La ruta remota implementada es `app_config/pac_schedule → processConfig → applyRemotePacScheduleJson → singleton → preferencias + evento → snapshots UI`. La ruta de edición local es `ViewModel → mutación PacScheduleData → saveSchedule → publicar stub`. Para verificar propagación real desde Android, no basta con observar que cambió la pantalla o apareció un mensaje de éxito.

Un payload compatible puede usar slots con horas/etiquetas y `matrixRows: [{ cells: [...] }, ...]`, o el formato alternativo `matrix: [[...], ...]`. Debe tener tantas filas como slots y siete columnas por fila, aunque el receptor actual no lo imponga. Enviar un esquema diferente requiere actualizar parser, persistencia, algoritmos y consumidores juntos. Mantener un marcador creciente y una versión de esquema separada sería una mejora; hoy `version` y `updatedAt` mezclan semánticas.

### 13.4 Registro de una comunidad y aprobación

El flujo actual guarda sector local y envía dos `upsert`: solicitud comunitaria y sector. El nombre “solicitud” del panel no impone una espera en Android. Para establecer moderación real se necesita acordar con backend qué tabla puede escribir el ciudadano, qué estado representa pendiente y qué operación administrativa crea el sector público. También hace falta guardar de forma durable el alta cuando no hay red. Cambiar únicamente el mensaje de éxito mejora la claridad, pero no resuelve pérdida de operación o colisiones por nombre.

### 13.5 Diferencia entre “hay luz”, programación y estimación

`status`, ventana PAC, respuesta ciudadana y predicción son datos relacionados pero distintos. `hasPower` es una observación del usuario; la matriz es programación; `OutagePrediction` es resultado heurístico; `Sector.status` mezcla valor remoto y optimista. Un fin de horario no acredita físicamente retorno de energía. Las mejoras de producto deben mostrar fuente y antigüedad sin inventar una medición o certeza que la capa de datos no tiene.

## 14. Recetas de cambio y comprobaciones necesarias

| Cambio deseado | Archivos/contratos que revisar | Comprobación de comportamiento |
|---|---|---|
| Agregar campo a reporte | `CitizenReport`, `PendingReportEntity`, `PendingReportDao`, migración Room, `SupabaseCitizenReport`, uploader, worker y formulario/historial; DTO Retrofit solo si se conserva ese contrato | Guardar offline, reiniciar proceso, reenviar, verificar que valor y fecha original sobrevivan. No usar migración destructiva como reemplazo |
| Arreglar sincronización de reportes | ID estable por evento en entidad/contrato remoto, retorno de `insertReport`, uploader, envío manual y worker | Doble toque, dos emisores concurrentes, HTTP recibido sin ACK y reintento deben generar una sola fila remota y confirmar el ID local correcto |
| Añadir categoría eléctrica | `ServiceStatus` si es nuevo estado; textos `reportType`, conversores, optimismo repositorio/worker/receiver/widget, agregación backend y UI | Categoría no debe convertirse silenciosamente en NORMAL/programado; probar ruta de fondo |
| Cambiar duración/franjas | `PacSlot`, validación de edición/parser, preferencias, matriz, `getCurrentSlotIndex`, `findNextWindowForBlock`, motor y textos UI/alarma | Inicio/final exactos, franjas cruzando medianoche, huecos, orden y bloques sin turno. El motor no usa hoy final teórico |
| Añadir bloque E o esquema territorial distinto | Enumeraciones/cadenas de bloque, listas de nombres, presets, regex en UI/worker/scheduler, motor y backend | Ningún fallback debe convertir el nuevo bloque en A/B o inventarle ventana; no basta agregar columna de catálogo |
| Corregir una dirección | `LOCATIONS`, `sectorEntityId`, fila de sector, listas PAC y preferencias/perfiles ya guardados | ID único, territorio correcto, bloque coherente en todas las fuentes y migración de selección previa si cambia ID |
| Cambiar Supabase/configuración | Inicializador lazy del cliente, modelos serializables, schema/políticas remotos, formato PAC y marcadores | Fallo parcial y reconexión deben reconciliar datos; no duplicar credenciales en documentación |
| Cambiar versión/canal de distribución | Gradle para versión instalada; `AppUpdater`, config `version`, diálogo y ficha de Play | Una release no disponible en tienda no debe bloquear al usuario con un aviso imposible; comparar versión real y no `+1` artificial |
| Habilitar limpieza del histórico local | `deleteOldSyncedReports` y política de retención | Nunca eliminar pendientes ni eventos que aún puedan necesitar confirmación; conservar observaciones según política definida |
| Retirar Retrofit residual | `ApiClient`, `LuzBarinasApi`, DTO/Moshi asociados, imports, dependencias y contratos del backend | Confirmar búsqueda de consumidores, incluidas pruebas; no retirar serializers que otros modelos sí usan |

En cualquier cambio temporal usar `America/Caracas` de forma consistente y decidir explícitamente la fecha propietaria de una franja 23–03. Las preferencias y variables de UI también pueden mantener códigos/IDs antiguos: cambiar una constante de catálogo no reescribe por sí solo selecciones ya guardadas.

## 15. Inventario verificable de funciones y declaraciones ejecutables

El inventario siguiente contiene **84 funciones de datos/motor/utilidades y cinco funciones de pruebas: 89 en total**. Incluye privadas, abstractas, migración y nombres de test entre backticks. Los getters e inicializadores con comportamiento están descritos en los apartados de propiedades y se incluyen después como complemento. Las funciones generadas automáticamente por Kotlin/Room/Retrofit no se cuentan como funciones fuente propias.

| Archivo bajo la raíz `E:\LUZ BARINAS` | Nombres exactos y línea inicial |
|---|---|
| `app/src/main/java/com/example/data/PacSchedulePrefs.kt` | `getPrefs` L27; `saveSchedule` L31; `loadSchedule` L62 |
| `app/src/main/java/com/example/data/local/AppDatabase.kt` | `sectorDao` L24; `outageRecordDao` L25; `pendingReportDao` L26; `MIGRATION_2_3.migrate` L33; `getInstance` L39 |
| `app/src/main/java/com/example/data/local/dao/SectorDao.kt` | `getAllSectors` L14; `getAllSectorsList` L17; `getSectorById` L20; `observeSectorById` L23; `insertOrUpdateSectors` L26; `insertOrUpdateSector` L29; `updateSector` L32; `getSectorsCount` L35 |
| `app/src/main/java/com/example/data/local/dao/OutageRecordDao.kt` | `getRecentOutagesBySector` L13; `getAllOutages` L16; `insertOutages` L19; `insertOutage` L22; `getCount` L25 |
| `app/src/main/java/com/example/data/local/dao/PendingReportDao.kt` | `getUnsyncedReports` L14; `getAllReportsFlow` L17; `getUnsyncedReportsCountFlow` L20; `insertReport` L23; `updateReport` L26; `markAsSynced` L29; `deleteOldSyncedReports` L32 |
| `app/src/main/java/com/example/data/local/entity/SectorEntity.kt` | `SectorEntity.toDomain` L22 |
| `app/src/main/java/com/example/data/local/entity/OutageRecordEntity.kt` | `OutageRecordEntity.toDomain` L23 |
| `app/src/main/java/com/example/data/local/entity/PendingReportEntity.kt` | `PendingReportEntity.toDomain` L24 |
| `app/src/main/java/com/example/data/model/BarinasLocationsData.kt` | `getParroquiasForMunicipality` L1144; `addCustomLocation` L1153; `getAllLocations` L1158; `searchLocations` L1163 |
| `app/src/main/java/com/example/data/model/PacAlertSettings.kt` | `getSettings` L23; `saveSettings` L36 |
| `app/src/main/java/com/example/data/model/PacSchedule.kt` | `updateSlot` L110; `getMatrixSnapshot` L147; `getSlotsSnapshot` L151; `updateMatrix` L155; `updateCell` L164; `deleteSlot` L172; `addSlot` L187; `getDayTurnAudit` L200; `deleteTurnAndRebalance` L225; `applySingleTurnPreset` L291; `applyDoubleTurnPreset` L309; `resetToDefault` L320; `addSectorToBlock` L494; `removeSectorFromBlock` L511; `reassignSector` L521; `getMonthlyWeeks` L541; `getDayIndex` L586; `getWeekOffsetForDayOfMonth` L599; `getBlockForDate` L608; `getCurrentSlotIndex` L615; `getActiveBlockNow` L638; `findNextWindowForBlock` L648 |
| `app/src/main/java/com/example/data/remote/LuzBarinasApi.kt` | `getSectors` L14; `getSectorDetails` L17; `submitPowerReport` L22; `getOutagePrediction` L27; `getMonthlyWeeks` L32 |
| `app/src/main/java/com/example/data/repository/EnergyRepository.kt` | `initializePreloadedDataIfEmpty` L60; `getPredictionForSector` L210; `submitReport` L226; `syncPendingReportsNow` L326; `registerCommunityLocation` L351 |
| `app/src/main/java/com/example/data/repository/CloudSyncRepository.kt` | `startRealtimeSync` L141; `handleConfigChange` L235; `processConfig` L245; `listenToSector` L297; `updateLocalSector` L329; `uploadCitizenReport` L377; `fetchWeeklyCitizenReports` L411; `publishPacScheduleToCloud` L430; `uploadCommunityLocation` L441; `applyRemotePacScheduleJson` L489; `stopRealtimeSync` L555 |
| `app/src/main/java/com/example/engine/OutagePredictionEngine.kt` | `calculateNextOutageWindow` L17; `calculateLocalOutageWindow` L26 |
| `app/src/main/java/com/example/utils/AppUpdater.kt` | `checkForUpdates` L15; `openPlayStoreForUpdate` L71 |
| `app/src/test/java/com/example/ExampleUnitTest.kt` | `test modular arithmetic outage prediction engine` L14 |
| `app/src/test/java/com/example/ExampleRobolectricTest.kt` | `read string from context` L20; `verify room sector dao insert and retrieve` L27 |
| `app/src/test/java/com/example/GreetingScreenshotTest.kt` | `greeting_screenshot` L25 |
| `app/src/androidTest/java/com/example/ExampleInstrumentedTest.kt` | `useAppContext` L17 |

**Getters e inicializadores con comportamiento, adicionales a las 89 funciones:** `EnergyRepository.cloudSync`, `broadcastNoticeFlow`, `updateInfoFlow`, `pacScheduleUpdatedFlow`, `allSectorsFlow`, `unsyncedCountFlow`, `allReportsFlow`; `SupabaseSector.resolvedIsCommunity`; cliente lazy `CloudSyncRepository.supabase`, scope/flujos/preferencias y `jsonFormat`; `PacScheduleData.SLOTS`, `MATRIX` y snapshots iniciales; `ApiClient.moshi`, `loggingInterceptor`, `okHttpClient`, `api`; `AppDatabase.INSTANCE`/`MIGRATION_2_3`; `BarinasLocationsCatalog.BARINAS_PARROQUIAS`/`customLocations`; `GreetingScreenshotTest.composeTestRule`.

**Archivos de modelos sin funciones fuente propias, también cubiertos:** `PowerStatus.kt`, `DonationConfig.kt`, `MonthlyWeeksResponse.kt`, `TelemetryDtos.kt`. Sus constructores/campos, valores por defecto y anotaciones están explicados en sus apartados. Los modelos auxiliares de PAC, territoriales y Supabase están incluidos en los archivos que además contienen funciones. En total se leyeron los **25 archivos Kotlin** del alcance, sin editar su código; la imagen de referencia de captura se identificó como artefacto de test y no se regeneró.

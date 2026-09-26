# 📖 DOCUMENTACIÓN COMPLETA DEL SISTEMA PAC BARINAS

> **Versión del documento:** 2.0  
> **Última actualización:** 26 de septiembre de 2026  
> **Propósito:** Este documento contiene TODA la información necesaria para entender, mantener y evolucionar el sistema PAC Barinas (App Android nativa, Panel Web Administrativo, Backend Supabase y APIs Serverless). Si cambias de IA o de desarrollador, este archivo es tu biblia.

---

## ÍNDICE GENERAL

1. [Visión General del Sistema](#1-visión-general-del-sistema)
2. [Arquitectura Global](#2-arquitectura-global)
3. [Repositorios y Estructura de Archivos](#3-repositorios-y-estructura-de-archivos)
4. [Backend: Supabase (Schema v2 y Cache Reload)](#4-backend-supabase)
5. [Aplicación Android (Jetpack Compose & Settings Fullscreen)](#5-aplicación-android)
6. [Panel Administrativo Web (Directorio Maestro & Atlas Cartográfico Neón)](#6-panel-administrativo-web)
7. [APIs Serverless (Vercel)](#7-apis-serverless-vercel)
8. [Sistema de Notificaciones Push y Alarmas (Enrutamiento por Bloque)](#8-sistema-de-notificaciones-y-alarmas)
9. [Sistema OTA (Actualizaciones In-App)](#9-sistema-ota-actualizaciones-in-app)
10. [Monetización (AdMob)](#10-monetización-admob)
11. [Donaciones](#11-donaciones)
12. [Credenciales y Configuración Sensible](#12-credenciales-y-configuración-sensible)
13. [Proceso de Build y Release](#13-proceso-de-build-y-release)
14. [Esquema Completo de Base de Datos](#14-esquema-completo-de-base-de-datos)
15. [Modelos de Datos (Kotlin)](#15-modelos-de-datos-kotlin)
16. [Flujos de Datos Críticos](#16-flujos-de-datos-críticos)
17. [Archivo por Archivo: Aplicación Android](#17-archivo-por-archivo-aplicación-android)
18. [Archivo por Archivo: Panel Web Admin](#18-archivo-por-archivo-panel-web-admin)
19. [Problemas Conocidos y Deuda Técnica](#19-problemas-conocidos-y-deuda-técnica)
20. [Guía de Referencia Rápida](#20-guía-de-referencia-rápida)

---

## 1. VISIÓN GENERAL DEL SISTEMA

### ¿Qué es PAC Barinas?

**PAC Barinas** (Plan de Administración de Carga - Barinas) es un sistema completo de telemetría eléctrica ciudadana diseñado para el Estado Barinas, Venezuela. Su propósito es:

1. **Informar a los ciudadanos** sobre los cortes eléctricos programados (racionamiento/PAC) en su zona.
2. **Predecir** cuándo será el próximo corte en base al cronograma oficial de Corpoelec.
3. **Permitir reportes ciudadanos** donde cada usuario dice si tiene o no tiene luz, creando un mapa de calor en tiempo real.
4. **Alertar con alarmas sonoras** minutos antes de que inicie un corte programado.
5. **Administrar** el cronograma PAC y los sectores desde un panel web.

### Componentes del Sistema

| Componente | Tecnología | Descripción |
|---|---|---|
| **App Android** | Kotlin + Jetpack Compose | App nativa para ciudadanos de Barinas |
| **Panel Admin Web** | HTML5 + JS Vanilla + Tailwind CSS | Consola de gestión administrativa |
| **Backend** | Supabase (PostgreSQL + Realtime) | Base de datos, autenticación, websockets |
| **APIs Serverless** | Vercel Functions (Node.js) | Predicciones, proyecciones mensuales, push FCM |
| **Push Notifications** | Firebase Cloud Messaging (FCM) | Alertas masivas a dispositivos |
| **Monetización** | Google AdMob | Banner, Interstitial, App Open ads |
| **OTA Updates** | GitHub Releases API | Distribución de APKs sin Play Store |

### Contexto Operativo

- **Región:** Estado Barinas, Venezuela
- **Zona horaria:** `America/Caracas` (UTC-4)
- **Usuarios objetivo:** Ciudadanos del municipio Barinas y municipios aledaños
- **Sistema eléctrico:** Dividido en **4 Bloques de rotación** (A, B, C, D) con cortes programados de 4 horas cada uno
- **Voltaje nominal:** 118V (120V nominal con variación)

---

## 2. ARQUITECTURA GLOBAL

```
┌─────────────────────────────────────────────────────────────┐
│                    USUARIOS (Ciudadanos)                     │
│                 📱 App Android PAC Barinas                   │
└───────────────────────┬─────────────────────────────────────┘
                        │
          ┌─────────────┼─────────────┐
          │             │             │
          ▼             ▼             ▼
┌─────────────┐ ┌──────────────┐ ┌──────────────┐
│  Supabase   │ │ Vercel APIs  │ │  Firebase    │
│ (PostgreSQL │ │ (Serverless) │ │   (FCM)      │
│ + Realtime) │ │              │ │              │
└──────┬──────┘ └──────┬───────┘ └──────┬───────┘
       │               │               │
       └───────────────┼───────────────┘
                       │
          ┌────────────┼────────────┐
          │                         │
          ▼                         ▼
┌──────────────────┐    ┌──────────────────┐
│  Panel Admin Web │    │  GitHub Releases  │
│  (Vercel Hosting)│    │  (OTA Updates)    │
└──────────────────┘    └──────────────────┘
```

### Flujo de Datos Principal

1. **Cronograma PAC** → Administrador lo configura en Panel Web → Se guarda en Supabase `app_config` → App Android recibe por Realtime → Muestra al usuario
2. **Reportes Ciudadanos** → Usuario reporta "Tengo/No tengo luz" → Se guarda en Room (offline-first) → Se sincroniza a Supabase `citizen_reports` → Panel Web lo muestra en tiempo real
3. **Sectores Comunitarios** → Usuario denomina su sector → Se guarda en `community_locations` → Admin lo aprueba → Pasa a tabla `sectors` → App lo muestra

---

## 3. REPOSITORIOS, UBICACIONES FÍSICAS Y ESTRUCTURA DE ARCHIVOS

### ⚡ UBICACIÓN FÍSICA DEL PROYECTO (MUY IMPORTANTE)

> **DATO CLAVE:** Tanto la app Android como la página web administrativa están en la MISMA carpeta padre `e:\LUZ BARINAS\`, pero son **dos repositorios Git separados** que apuntan a dos repos de GitHub diferentes.

```
e:\LUZ BARINAS\                          ← CARPETA RAÍZ (repo Git: luz-barinas-android)
├── app\                                 ← APP ANDROID (código Kotlin/Compose)
│   └── src\main\java\com\example\       ← Código fuente de la app
├── admin-web\                           ← PÁGINA WEB ADMIN (repo Git SEPARADO: luz-barinas-admin-web)
│   ├── .git\                            ← Su PROPIO repositorio Git independiente
│   ├── app.js                           ← Lógica JavaScript de la web
│   ├── index.html                       ← Interfaz HTML de la web
│   └── api\                             ← APIs serverless de Vercel
└── .git\                                ← Repositorio Git de la app Android
```

**Relación entre los repos:**
- `e:\LUZ BARINAS\` es el repo Git del **Android App** → push a `github.com/Ralag/luz-barinas-android`
- `e:\LUZ BARINAS\admin-web\` es un repo Git **independiente** dentro de la misma carpeta → push a `github.com/Ralag/luz-barinas-admin-web`
- El `.gitignore` del repo Android tiene la línea `admin-web/` para excluir la subcarpeta web del tracking del repo Android
- Cada uno se maneja con `git add/commit/push` POR SEPARADO desde su carpeta

### Repositorios GitHub

| Repo | URL | Carpeta Local | Contenido |
|---|---|---|---|
| **Android App** | `https://github.com/Ralag/luz-barinas-android` | `e:\LUZ BARINAS\` | App Android completa |
| **Admin Web** | `https://github.com/Ralag/luz-barinas-admin-web` | `e:\LUZ BARINAS\admin-web\` | Panel web + APIs serverless |

> ⚠️ **AMBOS REPOSITORIOS SON PÚBLICOS.** No incluir contraseñas, claves privadas ni credenciales de servicio en el código fuente.

### Cómo Hacer Push a Cada Repo

```powershell
# Push del repo ANDROID (desde la carpeta raíz):
cd "e:\LUZ BARINAS"
git add . ; git commit -m "mensaje" ; git push

# Push del repo ADMIN WEB (desde la subcarpeta):
cd "e:\LUZ BARINAS\admin-web"
git add . ; git commit -m "mensaje" ; git push
```

### Estructura Completa del Proyecto

```
e:\LUZ BARINAS\                              ← RAÍZ DEL PROYECTO
│
│   ══════════════════════════════════════════
│   ARCHIVOS DE CONFIGURACIÓN (RAÍZ - REPO ANDROID)
│   ══════════════════════════════════════════
├── build.gradle.kts                         # Build raíz: declara plugins sin aplicar
├── settings.gradle.kts                      # rootProject.name = "Luz Barinas", include(":app")
├── gradle.properties                        # Config Gradle (memoria, AndroidX, etc.)
├── .env.example                             # Variables de entorno ejemplo (IDs test AdMob)
├── .gitignore                               # Excluye: admin-web/, *.jks, *.py, *.apk, .env, etc.
├── supabase_schema.sql                      # Schema SQL completo de Supabase (4 tablas)
├── seed_supabase.py                         # Script Python para sembrar datos iniciales
├── update_script.py                         # Script Python para parchear PacScheduleView.kt
├── my-upload-key.jks                        # Keystore para firmar APK release
├── metadata.json                            # Metadata del proyecto (nombre, desc)
├── DOCUMENTACION_SISTEMA.md                 # ← ESTE ARCHIVO
│
│   ══════════════════════════════════════════
│   APP ANDROID (módulo :app)
│   Ubicación: e:\LUZ BARINAS\app\
│   ══════════════════════════════════════════
├── app/
│   ├── build.gradle.kts                     # applicationId, SDK, AdMob, signing, dependencias
│   ├── proguard-rules.pro                   # Reglas de ofuscación para release
│   └── src/main/
│       │
│       ├── AndroidManifest.xml              # Permisos, Activity, Receivers, Services, Provider
│       │
│       ├── res/
│       │   ├── drawable/                    # Iconos y gráficos
│       │   ├── mipmap-*/                    # Iconos de lanzador (hdpi a xxxhdpi)
│       │   ├── values/
│       │   │   ├── strings.xml              # app_name = "PAC Barinas"
│       │   │   ├── colors.xml               # Colores XML legacy
│       │   │   └── themes.xml               # Tema base
│       │   └── xml/
│       │       ├── backup_rules.xml         # Reglas de backup Android
│       │       ├── data_extraction_rules.xml # Reglas de extracción datos
│       │       ├── file_paths.xml           # Paths FileProvider (OTA install)
│       │       └── luz_barinas_widget_info.xml  # Definición del Widget Home Screen
│       │
│       └── java/com/example/               # ← TODO EL CÓDIGO KOTLIN AQUÍ
│           │
│           ├── MainActivity.kt              # Punto de entrada (465 líneas)
│           │
│           ├── data/                        # ═══ CAPA DE DATOS ═══
│           │   ├── PacSchedulePrefs.kt      # Persistencia cronograma SharedPrefs (120 lín)
│           │   ├── local/
│           │   │   ├── AppDatabase.kt       # Room DB v3 singleton (54 lín)
│           │   │   ├── dao/
│           │   │   │   ├── SectorDao.kt     # DAO sectores (37 lín)
│           │   │   │   ├── OutageRecordDao.kt   # DAO historial (27 lín)
│           │   │   │   └── PendingReportDao.kt  # DAO reportes (34 lín)
│           │   │   └── entity/
│           │   │       ├── SectorEntity.kt      # @Entity "sectors" (53 lín)
│           │   │       ├── OutageRecordEntity.kt    # @Entity "outage_records" (41 lín)
│           │   │       └── PendingReportEntity.kt   # @Entity "pending_reports" (34 lín)
│           │   ├── model/
│           │   │   ├── PowerStatus.kt           # ServiceStatus enum, Sector, CitizenReport (61 lín)
│           │   │   ├── PacSchedule.kt           # PacScheduleData singleton + 12 data classes (603 lín)
│           │   │   ├── BarinasLocationsData.kt  # Catálogo hardcodeado 100+ ubicaciones (1204 lín)
│           │   │   ├── PacAlertSettings.kt      # Config alarma PAC (43 lín)
│           │   │   ├── DonationConfig.kt        # Config donaciones (10 lín)
│           │   │   └── MonthlyWeeksResponse.kt  # DTO API mensual (12 lín)
│           │   ├── remote/
│           │   │   ├── ApiClient.kt             # Retrofit + OkHttp → Vercel APIs (44 lín)
│           │   │   ├── LuzBarinasApi.kt         # Interface endpoints REST (37 lín)
│           │   │   └── dto/
│           │   │       └── TelemetryDtos.kt     # SectorDto, ReportRequest/Response (37 lín)
│           │   └── repository/
│           │       ├── EnergyRepository.kt      # Repositorio principal Room+Cloud (395 lín)
│           │       └── CloudSyncRepository.kt   # Motor sync Supabase Realtime (480 lín)
│           │
│           ├── engine/                      # ═══ MOTOR DE PREDICCIÓN ═══
│           │   └── OutagePredictionEngine.kt    # Delega predicción a API Vercel
│           │
│           ├── glance/                      # ═══ WIDGET DE PANTALLA DE INICIO ═══
│           │   └── LuzBarinasWidget.kt      # Widget Glance con botones 1-click (231 lín)
│           │
│           ├── notification/                # ═══ NOTIFICACIONES Y ALARMAS ═══
│           │   ├── NotificationHelper.kt        # Crear y mostrar 4 tipos de notificaciones
│           │   ├── NotificationActionReceiver.kt    # Receiver acciones desde notificación
│           │   ├── PacAlarmPlayer.kt            # Reproducir alarma sonora + vibración
│           │   ├── PacAlarmReceiver.kt          # BroadcastReceiver para AlarmManager
│           │   ├── PacAlarmScheduler.kt         # Programar alarmas exactas con anticipación
│           │   └── MyFirebaseMessagingService.kt    # FCM: onNewToken + onMessageReceived
│           │
│           ├── utils/                       # ═══ UTILIDADES ═══
│           │   └── AppUpdater.kt            # OTA: check GitHub Releases + download + install
│           │
│           ├── worker/                      # ═══ TAREAS EN BACKGROUND ═══
│           │   ├── ReportPowerWorker.kt         # WorkManager: sync reportes offline→cloud
│           │   └── PredictiveNotificationWorker.kt  # Worker periódico cada 30 min
│           │
│           └── ui/                          # ═══ CAPA DE INTERFAZ ═══
│               ├── viewmodel/
│               │   └── LuzBarinasViewModel.kt   # ViewModel central MVVM (492 lín)
│               ├── theme/
│               │   ├── Color.kt                 # Paleta 50+ colores brand/bloque/estado (100 lín)
│               │   ├── Shape.kt                 # Formas Material 3 (14 lín)
│               │   ├── Theme.kt                 # Tema claro/oscuro + dynamic color (88 lín)
│               │   └── Type.kt                  # 15 estilos tipográficos (120 lín)
│               ├── components/
│               │   ├── AdComponents.kt          # Banner, Interstitial, AppOpen ads (175 lín)
│               │   ├── BarinasAddressDialog.kt  # Selector ubicación + registro sector (763 lín)
│               │   ├── DonationsDialog.kt       # Diálogo PayPal/Binance/PagoMóvil (145 lín)
│               │   ├── GoogleSearchBar.kt       # Barra búsqueda con sugerencias (299 lín)
│               │   ├── InteractiveBarinasMap.kt # Mapa Canvas río/calles/nodos (490 lín)
│               │   ├── OnboardingFlowDialog.kt  # Wizard bienvenida 3 pasos (249 lín)
│               │   ├── PacScheduleView.kt       # Horarios PAC semanal/mensual/bloques (993 lín)
│               │   ├── PacSettingsDialog.kt     # Config alarmas y anticipación (506 lín)
│               │   ├── StatusBadge.kt           # Badge animado Normal/Corte/Avería (111 lín)
│               │   ├── SystemSettingsDialog.kt  # Ajustes del sistema (161 lín)
│               │   └── UpdatePromptDialog.kt    # Diálogo OTA actualización (151 lín)
│               └── screens/
│                   ├── DashboardScreen.kt       # Tab "Hoy": estado + reporte rápido (760 lín)
│                   ├── MapScreen.kt             # Tab "Mapa": mapa interactivo (338 lín)
│                   ├── TelemetryScreen.kt       # Pantalla telemetría ciudadana (345 lín)
│                   └── AdminScreen.kt           # Panel admin in-app (1188 lín)
│
│   ══════════════════════════════════════════
│   PÁGINA WEB ADMINISTRATIVA
│   Ubicación: e:\LUZ BARINAS\admin-web\
│   (Repo Git INDEPENDIENTE del Android)
│   ══════════════════════════════════════════
├── admin-web/                               ← REPO GIT INDEPENDIENTE
│   ├── .git/                                # Su PROPIO repositorio Git
│   ├── .gitignore                           # Excluye: .vercel/, node_modules/, .env
│   ├── .firebaserc                          # Project Firebase: luzbarinas-6cabc
│   ├── firebase.json                        # Config Firebase Hosting (legacy, ahora usa Vercel)
│   ├── vercel.json                          # Config Vercel: rewrites SPA → index.html
│   ├── package.json                         # Deps npm: @supabase/supabase-js, firebase-admin
│   ├── README.md                            # Documentación del panel web
│   │
│   ├── index.html                           # SPA HTML completa (1104 líneas)
│   ├── app.js                               # Toda la lógica JavaScript (1983 lín, 63 funciones)
│   ├── styles.css                           # Estilos Material 3 Dark + glassmorphism (153 lín)
│   │
│   └── api/                                 # APIs Serverless (Vercel Functions)
│       ├── getMonthlyWeeks.js               # Proyección mensual rotación PAC (92 lín)
│       ├── getOutagePrediction.js           # Predicción algorítmica de cortes (181 lín)
│       └── sendPushAlert.js                 # Push FCM masivo al tópico global (60 lín)
```

### AndroidManifest.xml — Permisos y Componentes Registrados

**Ruta:** `e:\LUZ BARINAS\app\src\main\AndroidManifest.xml` (97 líneas)

**Permisos declarados:**

| Permiso | Propósito |
|---|---|
| `INTERNET` | Conexión a Supabase, Vercel, Firebase, GitHub |
| `ACCESS_NETWORK_STATE` | Verificar conectividad antes de sync |
| `POST_NOTIFICATIONS` | Mostrar alertas y alarmas PAC (Android 13+) |
| `VIBRATE` | Vibración en alarmas de corte |
| `SCHEDULE_EXACT_ALARM` | Alarmas exactas con AlarmManager (Android 12+) |
| `RECEIVE_BOOT_COMPLETED` | Reprogramar alarmas tras reinicio del teléfono |
| `WAKE_LOCK` | Mantener CPU activa durante reproducción de alarma |
| `REQUEST_INSTALL_PACKAGES` | Instalar APK descargado por OTA |

**Componentes registrados en el Manifest:**

| Componente | Tipo | Propósito |
|---|---|---|
| `.MainActivity` | Activity (LAUNCHER) | Punto de entrada de la app |
| `.notification.NotificationActionReceiver` | BroadcastReceiver | Acciones "Tengo luz"/"Sin luz" desde notificaciones |
| `.notification.PacAlarmReceiver` | BroadcastReceiver | Alarma PAC + re-schedule tras BOOT_COMPLETED |
| `.glance.LuzBarinasWidgetReceiver` | BroadcastReceiver | Widget Glance de pantalla de inicio |
| `.notification.MyFirebaseMessagingService` | Service | Recepción de push FCM |
| `FileProvider` | ContentProvider | Compartir APK para instalación OTA |

**Meta-data:**
- `com.google.android.gms.ads.APPLICATION_ID` → `${ADMOB_APP_ID}` (inyectado desde `build.gradle.kts`)
- Widget info: `@xml/luz_barinas_widget_info`

---

## 4. BACKEND: SUPABASE

### Datos de Conexión

| Campo | Valor |
|---|---|
| **URL** | `https://ikttyjojubtredehtneb.supabase.co` |
| **Anon Public Key** | `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImlrdHR5am9qdWJ0cmVkZWh0bmViIiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTAwOTU4NTIsImV4cCI6MjEwNTY3MTg1Mn0.kbEerwT-EWdIlxlGWlEv7kSJ-k9AnteTe52IzxYDlXg` |
| **REST API** | `https://ikttyjojubtredehtneb.supabase.co/rest/v1` |

### Tablas (Esquema v2.0)

#### `sectors` — Directorio Maestro y Sectores Eléctricos Activos
| Columna | Tipo | Descripción |
|---|---|---|
| `id` | TEXT PK | ID único (ej: `sec_a_alto_barinas_1`, `sec_com_xxx`) |
| `name` | TEXT NOT NULL | Nombre del sector |
| `estado` | TEXT DEFAULT 'Barinas' | Estado geopolítico (Barinas) |
| `municipio` | TEXT DEFAULT 'Barinas' | Municipio de los 12 del Estado |
| `parroquia` | TEXT DEFAULT 'Barinas' | Parroquia específica |
| `sector` | TEXT | Nombre del sector / urbanización |
| `barrio` | TEXT DEFAULT '' | Barrio, vereda o detalle específico |
| `notes` | TEXT DEFAULT '' | Notas u observaciones administrativas internas |
| `lat` | FLOAT8 | Latitud georreferenciada (WGS84) |
| `lon` | FLOAT8 | Longitud georreferenciada (WGS84) |
| `circuitCode` | TEXT | Código del circuito eléctrico / alimentador |
| `status` | TEXT DEFAULT 'NORMAL' | `NORMAL`, `SCHEDULED_OUTAGE`, `IRREGULAR_OUTAGE` |
| `voltage` | FLOAT8 DEFAULT 118.0 | Voltaje observado (nominal 118V) |
| `confirmedReportsCount` | INT DEFAULT 0 | Cantidad de reportes confirmados |
| `withoutPowerPercentage` | INT DEFAULT 0 | Porcentaje de usuarios sin luz |
| `rotationBlock` | TEXT | Bloque de rotación (`A`, `B`, `C`, `D`) |
| `lastUpdatedMillis` | BIGINT | Timestamp última actualización |

> 💡 **Nota de Migración v2:** En la versión 2.0 se añadieron formalmente las columnas `estado`, `municipio`, `parroquia`, `sector`, `barrio`, `notes`, `lat` y `lon` mediante `supabase_migration_v2.sql`. Para que el motor HTTP de Supabase (PostgREST) reconozca nuevas columnas sin error de caché ("Could not find 'barrio' in schema cache"), se debe emitir `NOTIFY pgrst, 'reload schema';`. El Panel Web incluye un fallback en cliente (`upsertSectorSafe`) que evita bloqueos.

#### `community_locations` — Bandeja de Entrada Comunitaria (Pendientes de Aprobación)
| Columna | Tipo | Descripción |
|---|---|---|
| `id` | TEXT PK | ID generado (`sec_community_xxx`) |
| `name` | TEXT NOT NULL | Nombre propuesto por el usuario |
| `estado` | TEXT DEFAULT 'Barinas' | Estado geopolítico |
| `municipio` | TEXT NOT NULL DEFAULT 'Barinas' | Municipio seleccionado en el formulario |
| `parroquia` | TEXT NOT NULL DEFAULT 'Barinas' | Parroquia seleccionada |
| `sector` | TEXT | Sector o urbanización |
| `barrio` | TEXT DEFAULT '' | Barrio o detalle adicional |
| `notes` | TEXT DEFAULT '' | Notas del remitente |
| `lat` | FLOAT8 | Latitud capturada o aproximada |
| `lon` | FLOAT8 | Longitud capturada o aproximada |
| `block` | TEXT NOT NULL | Bloque propuesto (ej: "Bloque B") |
| `circuitCode` | TEXT NOT NULL | Circuito eléctrico |
| `status` | TEXT DEFAULT 'NORMAL' | Estado inicial |
| `voltage` | FLOAT8 DEFAULT 118.0 | Voltaje inicial |
| `confirmedReportsCount` | INT DEFAULT 0 | Reportes acumulados |
| `withoutPowerPercentage` | INT DEFAULT 0 | % sin luz |
| `rotationBlock` | TEXT | Bloque de rotación |
| `submittedAt` | BIGINT | Timestamp de envío desde la app móvil |

#### `app_config` — Configuración dinámica del sistema
| Columna | Tipo | Descripción |
|---|---|---|
| `config_key` | TEXT PK | Clave de configuración |
| `config_value` | JSONB NOT NULL | Valor en formato JSON |

**Claves conocidas en `app_config`:**

| `config_key` | Contenido de `config_value` |
|---|---|
| `pac_schedule` | `{ version, updatedAt, matrixRows, slots, sectorsA, sectorsB, sectorsC, sectorsD }` |
| `broadcast_notice` | `{ title, message, level, timestamp, active }` |
| `donations_config` | `{ paypal, binance, pmBank, pmPhone, pmId }` |
| `barinas_geography` | `{ municipalities: [...] }` |
| `version` | `{ versionCode, versionName, releaseNotes, apkDownloadUrl, isMandatory }` |

#### `citizen_reports` — Reportes de ciudadanos
| Columna | Tipo | Descripción |
|---|---|---|
| `id` | UUID PK (auto) | ID generado automáticamente |
| `sectorId` | TEXT NOT NULL | ID del sector reportado |
| `sectorName` | TEXT NOT NULL | Nombre del sector |
| `hasPower` | BOOLEAN NOT NULL | ¿Tiene luz? |
| `reportType` | TEXT NOT NULL | `NORMAL`, `SIN_LUZ`, `BAJON` |
| `voltage` | FLOAT8 | Voltaje observado |
| `deviceOrigin` | TEXT | Origen del dispositivo |
| `timestamp` | BIGINT | Timestamp del reporte |

### Row Level Security (RLS)

Todas las tablas tienen RLS habilitado con políticas que permiten acceso anónimo:
- `sectors`: SELECT, UPDATE, INSERT públicos
- `app_config`: SELECT, UPDATE, INSERT públicos
- `citizen_reports`: SELECT, INSERT públicos
- `community_locations`: SELECT, INSERT públicos

### Realtime (Websockets)

Tablas con Realtime habilitado (suscripciones en tiempo real):
- `sectors`
- `app_config`

Canales Realtime utilizados:
- `public:app_config_pac` — cambios en `pac_schedule`
- `public:app_config_broadcast` — cambios en `broadcast_notice`
- `public:sectors` — cambios en tabla sectores
- `public:community_locations` — nuevas solicitudes comunitarias
- `public:citizen_reports` — nuevos reportes ciudadanos
- `public:citizen_reports_logs` — feed de consola

---

## 5. APLICACIÓN ANDROID

### Datos del Build

| Campo | Valor |
|---|---|
| **Application ID** | `com.aistudio.luzbarinas.wvykrp` |
| **Namespace** | `com.example` |
| **Min SDK** | 24 (Android 7.0) |
| **Target SDK** | 36 (Android 16) |
| **Compile SDK** | `release(36) { minorApiLevel = 1 }` |
| **Version Code** | 4 |
| **Version Name** | "1.2" |
| **Kotlin** | 2.2.10 |
| **Compose BOM** | 2024.09.00 |
| **Room** | 2.7.0 |
| **Supabase SDK** | 3.0.2 |

### Arquitectura

La app sigue el patrón **MVVM (Model-View-ViewModel)** con enfoque **Offline-First**:

```
┌─────────────────────────────────────────────────┐
│                     UI LAYER                     │
│  MainActivity.kt → MainAppScreen (Composable)   │
│  ├── DashboardScreen (Tab 0: "Hoy")            │
│  ├── PacScheduleView (Tab 1: "Horarios")        │
│  └── MapScreen (Tab 2: "Mapa" - deshabilitado)  │
│  + Diálogos: Onboarding, Address, Settings...   │
└───────────────────────┬─────────────────────────┘
                        │ StateFlow<LuzBarinasUiState>
                        ▼
┌─────────────────────────────────────────────────┐
│                  VIEWMODEL LAYER                 │
│        LuzBarinasViewModel.kt                    │
│  ├── _uiState: MutableStateFlow                  │
│  ├── repository: EnergyRepository                │
│  └── userPrefs: SharedPreferences                │
└───────────────────────┬─────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────┐
│                DATA / REPOSITORY LAYER           │
│                                                  │
│  EnergyRepository.kt                             │
│  ├── Room Database (SQLite local)                │
│  │   ├── SectorDao (tabla "sectors")             │
│  │   ├── OutageRecordDao (tabla "outage_records")│
│  │   └── PendingReportDao (tabla "pending_reports")│
│  │                                               │
│  ├── CloudSyncRepository.kt                      │
│  │   ├── Supabase Realtime (Websockets)          │
│  │   ├── Supabase PostgREST (HTTP)               │
│  │   └── GitHub Releases API (OTA)               │
│  │                                               │
│  └── ApiClient.kt (Retrofit → Vercel APIs)       │
└─────────────────────────────────────────────────┘
```

### Navegación

La app tiene 2 tabs activos (el 3ro está comentado):

| Tab | Pantalla | Icono | Descripción |
|---|---|---|---|
| 0 | `DashboardScreen` | ⚡ Bolt | Estado actual, próximo corte, reporte rápido |
| 1 | `PacScheduleView` | 📅 Calendar | Horarios semanal/mensual, bloques |
| ~~2~~ | ~~`MapScreen`~~ | ~~🗺 Map~~ | ~~Mapa interactivo (deshabilitado)~~ |

**Back Handler:** Presionar Atrás siempre regresa al Tab 0.

### Room Database Local

- **Nombre del archivo:** `luz_barinas.db`
- **Versión actual:** 3
- **Migraciones:**
  - `MIGRATION_2_3`: `ALTER TABLE sectors ADD COLUMN isCommunity INTEGER NOT NULL DEFAULT 0`
- **Tablas:**
  - `sectors` — Sectores eléctricos (espejo de Supabase)
  - `outage_records` — Historial de cortes (para motor predictivo)
  - `pending_reports` — Reportes ciudadanos (cola offline)
- **Fallback:** `fallbackToDestructiveMigration()` si falla alguna migración

> ⚠️ **CRÍTICO:** Cualquier cambio de esquema futuro DEBE crear `Migration(3, 4)`. La versión debe subir a 4.

### SharedPreferences Utilizados

| Nombre | Claves | Propósito |
|---|---|---|
| `luz_barinas_user_prefs` | `saved_sector_id`, `saved_address`, `saved_sector_block`, `onboarding_done` | Ubicación del usuario |
| `pac_schedule_prefs` | `pac_matrix`, `pac_slots`, `sectors_bloque_a/b/c/d`, `schedule_version` | Cronograma PAC local |
| `pac_alert_settings` | `advance_minutes`, `notification_enabled`, `alarm_enabled`, `restore_alarm_enabled`, `vibration_enabled` | Config alarmas |
| `cloud_sync_prefs` | `last_seen_broadcast_timestamp`, `last_applied_pac_updated_at` | Control de sync |
| `admin_security_prefs` | `master_key` | Clave admin in-app |

### Dependencias Principales

| Librería | Versión | Uso |
|---|---|---|
| Jetpack Compose BOM | 2024.09.00 | UI declarativa |
| Room | 2.7.0 | Base de datos local SQLite |
| Supabase PostgREST | 3.0.2 | API REST a PostgreSQL |
| Supabase Realtime | 3.0.2 | Websockets en tiempo real |
| Ktor Client | 3.0.0 | HTTP client para Supabase SDK |
| Retrofit | 2.12.0 | HTTP client para APIs Vercel |
| OkHttp | 4.10.0 | Networking layer |
| Moshi | 1.15.2 | JSON serialization |
| WorkManager | 2.10.0 | Tareas en background garantizadas |
| Glance | 1.1.1 | Widget de pantalla de inicio |
| Firebase Messaging | BOM 34.17.0 | Push notifications (FCM) |
| Play Services Ads | 23.6.0 | AdMob SDK |
| Navigation Compose | 2.8.9 | Navegación entre pantallas |
| kotlinx-serialization | 1.7.3 | Serialización para Supabase SDK |

---

## 6. PANEL ADMINISTRATIVO WEB

### Datos de Despliegue

| Campo | Valor |
|---|---|
| **Hosting** | Vercel |
| **URL** | `https://luz-barinas-admin-web.vercel.app` |
| **Firebase Project ID** | `luzbarinas-6cabc` |
| **Tecnología** | HTML5 + JS Vanilla + Tailwind CSS CDN |

### Autenticación

El panel usa autenticación simple por contraseña almacenada en `localStorage`:

**Contraseñas aceptadas** (case-insensitive):
- `barinas2026`
- `2026`
- `barinas`
- `admin`
- `admin2026`
- `pacbarinas`
- `pac2026`

La sesión se persiste como `localStorage["luz_admin_session"] = "valid"`.

### Secciones/Pestañas del Panel

| # | Pestaña | ID | Descripción |
|---|---|---|---|
| 1 | Matriz PAC | `#tabPac` | Editor visual de la matriz de rotación semanal 6×7, proyección mensual, auditoría de cortes |
| 2 | Telemetría | `#tabTelemetry` | Lista de sectores con estado, voltaje, reportes; acciones masivas de reset |
| 3 | Gestión de Bloques | `#tabBlocks` | Kanban A/B/C/D para organizar sectores por bloque |
| 3.5 | Sectores en General | `#tabGeneralSectors` | **Directorio Maestro Enciclopédico & Atlas Cartográfico:** Fichas tipo Wikipedia y tabla interactiva, mapa oscuro brutalista (Leaflet + CartoDB Dark Matter) con pines neón por Bloque (A, B, C, D), modo de proyección térmica (Heatmap) y creación manual con geolocalización |
| 4 | Bandeja Comunitaria | `#tabCommunity` | **Inbox de Revisión Ciudadana:** Solicitudes enviadas desde la app, análisis de Typo-Clustering (Levenshtein + Jaccard) para fusión de variantes y validación geográfica con OpenStreetMap (Nominatim) |
| 5 | Avisos | `#tabBroadcast` | Emisión de alertas oficiales, test de push global |
| 6 | Consola Vercel | `#tabVercelConsole` | Terminal de logs en tiempo real de reportes ciudadanos |
| 7 | Donaciones | `#tabDonations` | Configuración de métodos de pago (PayPal, Binance, Pago Móvil) |

### Funciones Principales de `app.js`

**Autenticación:** `checkAuth()`, `handleLogin()`, `handleLogout()`

**Navegación:** `switchTab()` (con redimensionamiento automático de Leaflet y foco de pestañas)

**Sincronización:** `initFirebaseSync()`, `updateSyncStatus()`, `initConnectionMonitor()`, `retryFirebaseConnection()`, `initVercelLogsSync()`

**Atlas Cartográfico y Directorio Maestro (Módulo 16):**
- `initBarinasMasterMap()`: Inicializa el mapa Leaflet con CartoDB Dark Matter centrado en el Estado Barinas `[8.6226, -70.2075]`.
- `updateBarinasMapMarkers()`: Genera pines neón reactivos (`pin-glow-a`, `b`, `c`, `d`) o mapa de calor de cortes (`L.heatLayer`).
- `focusBarinasMapHome()` / `focusSectorOnMap(lat, lon, name)`: Navegación y animación suave sobre el mapa.
- `toggleMapHeatmapMode()`: Alterna entre marcadores vectoriales individuales y mapa de densidad térmica de averías/cortes.
- `renderGeneralSectorsMaster()`, `renderWikipediaCardsView()`, `renderDetailedTableView()`: Renderizado híbrido de fichas enciclopédicas y tabla de telemetría.
- `openCreateSectorModal()`, `closeCreateSectorModal()`, `handleSaveManualSector()`: Alta manual de sectores oficiales con dropdown dinámico de 12 municipios y 54 parroquias.
- `handleGeocodeForManualSector()`: Autocompletado de coordenadas WGS84 contra la API de OpenStreetMap.
- `upsertSectorSafe(payload)`: Wrapper resiliente contra desincronización de caché en Supabase (`Could not find column in schema cache`). Reintenta con campos base garantizados si el motor HTTP no ha recargado.
- `openDbMigrationHelper()`, `copyMigrationSql()`: Asistente visual para copiar el script SQL de recarga de caché (`NOTIFY pgrst, 'reload schema'`).

**Matriz PAC:** `renderMatrixTable()`, `openCellModal()`, `setCellBlock()`, `renderWeeklyAudit()`, `updateDetectedSchemeBadge()`, `openBranchGeneratorModal()`, `handleApplyBranchScale()`, `setPacViewMode()`, `renderMonthlyProjection()`, `applyMonthlyWeekAsBase()`, `openSchemeConfigModal()`, `handleApplySchemeConfig()`, `publishPacSchedule()`

**Franjas horarias:** `openEditSlotModal()`, `handleSaveSlot()`, `openAddSlotModal()`, `handleAddSlot()`, `deleteSlot()`, `onNewSlotDurationChanged()`, `onNewSlotTimeChanged()`

**Telemetría:** `renderTelemetryList()`, `setTelemetryFilter()`, `updateSectorStatus()`, `resetAllTelemetryToNormal()`, `setAllSectorsStatus()`

**Bloques:** `renderBlockKanban()`, `reassignSector()`, `removeSectorFromBlock()`, `handleAddSectorToBlock()`

**Comunitarios:** `renderCommunityTable()`, `approveCommunityLocation()`, `rejectCommunityLocation()`

**Avisos:** `renderActiveBroadcast()`, `handleSendBroadcast()`, `handleDismissBroadcast()`, `testGlobalPushAlert()`, `renderCitizenReportsFeed()`

**Catálogo:** `seedAllBarinasCatalog()`

**Donaciones:** `saveDonationConfig()`, `loadDonationUrl()`

**Consola:** `clearVercelConsole()`, `pingVercelApi()`

---

## 7. APIS SERVERLESS (VERCEL)

### `api/getMonthlyWeeks.js` (92 líneas)

**Propósito:** Calcula las 4 matrices de rotación PAC para las semanas del mes.

- **Input:** `monthName` (string), `totalDays` (number)
- **Output:** Array de 4 semanas, cada una con su matriz rotada
- **Algoritmo:** Desfase de filas `(row + weekOffset) % rows` donde `weekOffset` = 0, 1, 2, 3
- **Fuente de datos:** Supabase `app_config` → `pac_schedule`

### `api/getOutagePrediction.js` (181 líneas)

**Propósito:** Predice la próxima ventana de corte para un sector.

- **Input:** `sectorId` (string)
- **Output:** `{ sectorId, nextStartMillis, nextEndMillis, estimatedDurationHours, confidencePercentage, isCurrentlyActive, hoursUntilWindow }`
- **Algoritmo:** 
  1. Obtiene el bloque del sector desde `sectors`
  2. Lee reportes ciudadanos recientes de `citizen_reports`
  3. Consulta la matriz PAC de `app_config`
  4. Recorre horizonte de 14 días buscando coincidencias de bloque
  5. Confianza base 88%, sube a 94% si hay reportes previos
  6. **Fallback:** Rotación matemática basada en `dayOfYear` con confianza 78%

### `api/sendPushAlert.js` (60 líneas)

**Propósito:** Envía notificaciones push masivas vía Firebase Admin SDK.

- **Topic FCM:** `barinas_global`
- **Credenciales:** `process.env.FIREBASE_SERVICE_ACCOUNT` (JSON)
- **Payload:** `{ data: { title, message, is_alarm } }`
- **Uso:** Alertas de emergencia desde el panel admin

---

## 8. SISTEMA DE NOTIFICACIONES Y ALARMAS

### Componentes

| Archivo | Clase | Propósito |
|---|---|---|
| `NotificationHelper.kt` | Object | Crear y mostrar notificaciones |
| `NotificationActionReceiver.kt` | BroadcastReceiver | Manejar acciones de 1 toque en notificaciones |
| `PacAlarmPlayer.kt` | Object | Reproducir alarma sonora con vibración |
| `PacAlarmReceiver.kt` | BroadcastReceiver | Recibir alarmas de AlarmManager |
| `PacAlarmScheduler.kt` | Object | Programar alarmas exactas |
| `PredictiveNotificationWorker.kt` | CoroutineWorker | Worker periódico cada 30 min |
| `MyFirebaseMessagingService.kt` | FirebaseMessagingService | Recibir push FCM |

### Canal de Notificaciones

- **ID:** `alertas_cortes_barinas`
- **Nombre:** `Alertas PAC Barinas`
- **Importancia:** Alta (con vibración y badges)

### IDs de Notificación

| ID | Propósito |
|---|---|
| 1001 | Alerta interactiva de corte |
| 1002 | Confirmación de reporte |
| 1003 | Alerta predictiva |
| 1004 | Aviso broadcast |
| 2001 | Alarma PAC sonora |

### Flujo de Notificaciones PAC (Comportamiento v2.0)

> 🔔 **Actualización Arquitectónica:** En versiones previas, la alarma sonora sonaba en bucle continuo como sirena intrusiva generando fricción. A partir de la versión 2.0, el sistema opera con **Notificaciones Push Estándar por Defecto**:
> - Se emiten alertas nativas con sonido breve, distintivo y redundante (un tono para el corte de energía y otro tono claro para el retorno/restablecimiento del servicio).
> - Si el usuario prefiere el método antiguo de sirena continua audible hasta apagar manualmente, puede activarlo explícitamente desde la categoría *"Notificaciones y Alarmas"* en la pantalla de **Configuración**.

1. `PacAlarmScheduler.scheduleNextAlarm()` calcula el próximo corte correspondiente al bloque exacto del usuario (`A`, `B`, `C` o `D`).
2. Programa `AlarmManager.setExactAndAllowWhileIdle()` con X minutos de anticipación (configurable: 1-180 min, predeterminado 10 min).
3. Al cumplirse el tiempo, `PacAlarmReceiver` evalúa `useLegacyAlarmSiren`:
   - **Por Defecto (Push Estándar):** Despliega notificación flotante de alta prioridad con tono único y botones de acción rápida.
   - **Modo Sirena Antigua:** Invoca `PacAlarmPlayer.startAlarm()` reproduciendo alarma acústica en loop con vibración hasta silenciar con el botón flotante.
4. Opcionalmente, se programa una notificación de aviso de retorno de luz cuando expira el bloque de racionamiento.

### Corrección Crítica de Enrutamiento por Bloque

- **Problema previo:** Existía un error donde las notificaciones y alarmas enviaban siempre los horarios del Bloque A sin importar si el usuario residía en un sector de los Bloques B, C o D.
- **Solución implementada:** La app ahora suscribe dinámicamente a tópicos específicos por bloque (`block_A`, `block_B`, `block_C`, `block_D`) al cambiar de sector o iniciar la app. El backend y los workers leen estrictamente la columna `rotationBlock` del sector guardado en `luz_barinas_user_prefs`, garantizando que cada usuario reciba única y exclusivamente las alertas de su bloque.

### Configuración del Usuario (Pantalla Completa en Jetpack Compose)

- **Modo de Alerta:** Notificaciones Push Estándar (Recomendado/Default) vs. Alarma Sonora Continua (Antiguo).
- **Aviso de Retorno de la Luz:** Notificación al restablecerse el servicio eléctrico.
- **Anticipación:** 1 a 180 minutos (default: 10 min).
- **Vibración y Pruebas Acústicas:** Módulo de prueba en ajustes avanzados.

---

## 9. SISTEMA OTA (ACTUALIZACIONES IN-APP)

### Flujo de Actualización

1. Al iniciar la app, `CloudSyncRepository.startRealtimeSync()` llama a `AppUpdater.checkForUpdates()`
2. `AppUpdater` consulta: `https://api.github.com/repos/Ralag/luz-barinas-android/releases/latest`
3. Compara `tag_name` del release con `BuildConfig.VERSION_NAME`
4. Si hay versión nueva → emite a `_updateInfoFlow` → ViewModel actualiza `uiState.updateAvailable`
5. Se muestra `UpdatePromptDialog` con notas de release
6. Si el usuario acepta → `AppUpdater.downloadAndInstallLatestRelease()`:
   - Usa `DownloadManager` para descargar el APK
   - Registra `BroadcastReceiver` para `ACTION_DOWNLOAD_COMPLETE`
   - Invoca `installApk()` con `FileProvider` e `Intent.ACTION_VIEW`
7. Si el release tiene `[MANDATORY]` en la descripción → el diálogo no se puede cerrar

### Configuración Alternativa vía Supabase

También se puede publicar una actualización desde `app_config` con clave `"version"`:
```json
{
  "versionCode": 5,
  "versionName": "1.3",
  "releaseNotes": "Mejoras de estabilidad...",
  "apkDownloadUrl": "https://github.com/...",
  "isMandatory": false
}
```

---

## 10. MONETIZACIÓN (ADMOB)

### IDs de Producción

| Tipo | ID |
|---|---|
| **App ID** | `ca-app-pub-7639154379634043~2576948404` |
| **Banner** | `ca-app-pub-7639154379634043/8172800723` |
| **Interstitial** | `ca-app-pub-7639154379634043/1963218233` |
| **App Open** | `ca-app-pub-7639154379634043/8337054893` |

### Implementación

- **Banner:** Se muestra debajo del contenido principal, arriba del `NavigationBar`. Componente: `AdaptiveBannerAd()` en `AdComponents.kt`.
- **Interstitial:** Se precarga en `onCreate()`. Intervalo mínimo entre interstitials: **5 minutos** (`MIN_INTERVAL_MS = 5 * 60 * 1000L`).
- **App Open:** Se precarga en `onCreate()`. Se muestra al abrir la app por primera vez en la sesión después de 2 segundos de delay. Solo se muestra **una vez por sesión** (`hasShownOnce`).

### IDs de Test (para desarrollo)

| Tipo | ID de Test |
|---|---|
| App ID | `ca-app-pub-3940256099942544~3347511713` |
| Banner | `ca-app-pub-3940256099942544/9214589741` |
| Native | `ca-app-pub-3940256099942544/2247696110` |

> ⚠️ Los APKs de **debug** siempre muestran anuncios de prueba. Solo los APKs **release** firmados muestran anuncios reales.

---

## 11. DONACIONES

### Configuración

Los métodos de donación se configuran desde el Panel Web (pestaña "Donaciones") y se guardan en Supabase `app_config` con clave `donations_config`.

### Datos del Modelo

```kotlin
data class DonationConfig(
    val paypal: String = "",      // URL de PayPal.me
    val binance: String = "",     // ID de Binance Pay
    val pmBank: String = "",      // Banco del Pago Móvil
    val pmPhone: String = "",     // Teléfono del Pago Móvil
    val pmId: String = ""         // Cédula/RIF del Pago Móvil
)
```

### Flujo en la App

1. El botón "Dona Aquí" (rosa) en el TopAppBar abre `DonationsDialog`
2. El diálogo muestra:
   - Botón PayPal (abre URL en navegador)
   - Tarjeta Binance Pay (con botón copiar ID al portapapeles)
   - Tarjeta Pago Móvil (muestra banco, teléfono, cédula)
3. **Fallback PayPal:** `https://paypal.me/pacbarinas`

---

## 12. CREDENCIALES Y CONFIGURACIÓN SENSIBLE

> ⚠️ **ADVERTENCIA:** Los repositorios GitHub son PÚBLICOS. Las credenciales listadas aquí están expuestas en el código fuente y son de acceso público (anon keys).

### Supabase

| Dato | Valor |
|---|---|
| URL | `https://ikttyjojubtredehtneb.supabase.co` |
| Anon Key | `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...` (ver sección 4) |

### Release Keystore

| Dato | Valor |
|---|---|
| Archivo | `my-upload-key.jks` (raíz del proyecto) |
| Keystore Password | `password` (o env `STORE_PASSWORD`) |
| Key Alias | `upload` (o env `KEY_ALIAS`) |
| Key Password | `password` (o env `KEY_PASSWORD`) |

### Firebase

| Dato | Valor |
|---|---|
| Project ID | `luzbarinas-6cabc` |
| FCM Topic | `barinas_global` |
| Service Account | `process.env.FIREBASE_SERVICE_ACCOUNT` (Vercel) |

### Panel Admin Web

| Dato | Valor |
|---|---|
| Contraseñas | `barinas2026`, `2026`, `barinas`, `admin`, `admin2026`, `pacbarinas`, `pac2026` |
| Persistencia | `localStorage["luz_admin_session"]` |

---

## 13. PROCESO DE BUILD Y RELEASE

### Generar APK Release

1. Abrir Android Studio
2. Verificar que `my-upload-key.jks` existe en la raíz del proyecto
3. `Build → Generate Signed Bundle/APK → APK`
4. Seleccionar keystore, alias `upload`, password `password`
5. Seleccionar `release`
6. El APK se genera como `PAC-BARINAS-v1.2-release.apk`

### Publicar Update OTA

1. Compilar APK release
2. Crear un Release en GitHub (`https://github.com/Ralag/luz-barinas-android/releases/new`)
3. Tag: `v1.2` (debe ser mayor que la versión actual)
4. Adjuntar el archivo `.apk` como asset del release
5. En la descripción: agregar notas de release. Si es obligatorio, incluir `[MANDATORY]`
6. Publicar → Los usuarios existentes verán el diálogo de actualización

### Desplegar Panel Web

1. `cd admin-web`
2. Push a GitHub: `git add . && git commit -m "update" && git push`
3. Vercel detecta automáticamente el push y hace deploy
4. URL: `https://luz-barinas-admin-web.vercel.app`

---

## 14. ESQUEMA COMPLETO DE BASE DE DATOS

### Supabase (PostgreSQL remoto — Esquema v2.0)

```sql
CREATE TABLE public.sectors (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    estado TEXT DEFAULT 'Barinas',
    municipio TEXT DEFAULT 'Barinas',
    parroquia TEXT DEFAULT 'Barinas',
    sector TEXT,
    barrio TEXT DEFAULT '',
    notes TEXT DEFAULT '',
    lat FLOAT8,
    lon FLOAT8,
    "circuitCode" TEXT,
    status TEXT NOT NULL DEFAULT 'NORMAL',
    voltage FLOAT8 NOT NULL DEFAULT 118.0,
    "confirmedReportsCount" INT NOT NULL DEFAULT 0,
    "withoutPowerPercentage" INT NOT NULL DEFAULT 0,
    "rotationBlock" TEXT,
    "lastUpdatedMillis" BIGINT NOT NULL DEFAULT (extract(epoch from now()) * 1000)
);

CREATE TABLE public.community_locations (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    estado TEXT DEFAULT 'Barinas',
    municipio TEXT NOT NULL DEFAULT 'Barinas',
    parroquia TEXT NOT NULL DEFAULT 'Barinas',
    sector TEXT,
    barrio TEXT DEFAULT '',
    notes TEXT DEFAULT '',
    lat FLOAT8,
    lon FLOAT8,
    block TEXT NOT NULL,
    "circuitCode" TEXT NOT NULL,
    status TEXT DEFAULT 'NORMAL',
    voltage FLOAT8 DEFAULT 118.0,
    "confirmedReportsCount" INT DEFAULT 0,
    "withoutPowerPercentage" INT DEFAULT 0,
    "rotationBlock" TEXT,
    "submittedAt" BIGINT
);

CREATE TABLE public.app_config (
    config_key TEXT PRIMARY KEY,
    config_value JSONB NOT NULL
);

-- Recarga de caché para PostgREST
NOTIFY pgrst, 'reload schema';
NOTIFY pgrst, 'reload config';
```

CREATE TABLE public.citizen_reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    "sectorId" TEXT NOT NULL,
    "sectorName" TEXT NOT NULL,
    "hasPower" BOOLEAN NOT NULL,
    "reportType" TEXT NOT NULL,
    voltage FLOAT8,
    "deviceOrigin" TEXT,
    timestamp BIGINT NOT NULL DEFAULT (extract(epoch from now()) * 1000)
);

CREATE TABLE public.community_locations (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    municipio TEXT NOT NULL,
    parroquia TEXT NOT NULL,
    block TEXT NOT NULL,
    "circuitCode" TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'NORMAL',
    voltage FLOAT8 NOT NULL DEFAULT 118.0,
    "confirmedReportsCount" INT NOT NULL DEFAULT 0,
    "withoutPowerPercentage" INT NOT NULL DEFAULT 0,
    "rotationBlock" TEXT,
    "submittedAt" BIGINT NOT NULL DEFAULT (extract(epoch from now()) * 1000)
);
```

### Room (SQLite local en Android)

```sql
-- Tabla "sectors" (versión 3)
CREATE TABLE sectors (
    id TEXT PRIMARY KEY NOT NULL,
    name TEXT NOT NULL,
    circuitCode TEXT NOT NULL,
    status TEXT NOT NULL,
    voltage REAL NOT NULL,
    confirmedReportsCount INTEGER NOT NULL,
    withoutPowerPercentage INTEGER NOT NULL,
    lastUpdatedMillis INTEGER NOT NULL,
    rotationBlock TEXT NOT NULL,
    polygonPointsRaw TEXT NOT NULL,
    isCommunity INTEGER NOT NULL DEFAULT 0  -- Agregado en MIGRATION_2_3
);

-- Tabla "outage_records"
CREATE TABLE outage_records (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    sectorId TEXT NOT NULL,
    sectorName TEXT NOT NULL,
    statusType TEXT NOT NULL,
    startTimeMillis INTEGER NOT NULL,
    endTimeMillis INTEGER,
    durationHours REAL NOT NULL,
    notes TEXT NOT NULL
);
CREATE INDEX idx_outage_sector_time ON outage_records(sectorId, startTimeMillis);

-- Tabla "pending_reports"
CREATE TABLE pending_reports (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    sectorId TEXT NOT NULL,
    sectorName TEXT NOT NULL,
    hasPower INTEGER NOT NULL,
    reportedAtMillis INTEGER NOT NULL,
    reportType TEXT NOT NULL,
    voltageObserved REAL,
    isSynced INTEGER NOT NULL DEFAULT 0,
    retryCount INTEGER NOT NULL DEFAULT 0
);
CREATE INDEX idx_report_sync_time ON pending_reports(isSynced, reportedAtMillis);
```

---

## 15. MODELOS DE DATOS (KOTLIN)

### Enums

```kotlin
enum class ServiceStatus(val label: String, val hexColor: Long) {
    NORMAL("Servicio Normal", 0xFF10B981),
    SCHEDULED_OUTAGE("Corte Programado (PAC)", 0xFFEF4444),
    IRREGULAR_OUTAGE("Corte Irregular (Avería)", 0xFF9333EA)
}
```

### Modelos de Dominio

```kotlin
// Sector eléctrico
@Immutable data class Sector(
    val id: String,
    val name: String,
    val circuitCode: String,
    val status: ServiceStatus,
    val voltage: Float,
    val confirmedReportsCount: Int,
    val withoutPowerPercentage: Int,
    val lastUpdatedMillis: Long,
    val rotationBlock: String,
    val coordinates: List<Pair<Double, Double>>,
    val isCommunity: Boolean = false
)

// Reporte ciudadano
@Immutable data class CitizenReport(
    val id: Long = 0,
    val sectorId: String,
    val sectorName: String,
    val hasPower: Boolean,
    val reportedAtMillis: Long,
    val reportType: String,   // "NORMAL", "SIN_LUZ", "BAJON"
    val voltageObserved: Float?,
    val isSynced: Boolean = false
)

// Predicción de corte
@Immutable data class OutagePrediction(
    val sectorId: String,
    val sectorName: String,
    val rotationBlock: String,
    val nextEstimatedStartMillis: Long,
    val nextEstimatedEndMillis: Long,
    val estimatedDurationHours: Float,
    val confidencePercentage: Int,
    val algorithmDetail: String,
    val hoursUntilWindow: Float
)

// Aviso broadcast
@Immutable data class BroadcastNotice(
    val title: String = "",
    val message: String = "",
    val level: String = "INFO",   // "INFO", "WARNING", "EMERGENCY"
    val timestamp: Long = System.currentTimeMillis(),
    val active: Boolean = true
)

// Franja horaria PAC
@Immutable data class PacSlot(
    val index: Int,
    val timeLabel: String,       // "03:00 a 07:00"
    val startHour: Int,
    val endHour: Int
)

// Configuración de donaciones
data class DonationConfig(
    val paypal: String = "",
    val binance: String = "",
    val pmBank: String = "",
    val pmPhone: String = "",
    val pmId: String = ""
)

// Info de actualización OTA
data class AppUpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val releaseNotes: String,
    val downloadUrl: String,
    val isMandatory: Boolean
)

// Config alertas PAC
data class PacAlertSettings(
    val advanceMinutes: Int = 10,
    val isNotificationEnabled: Boolean = true,
    val isAlarmEnabled: Boolean = true,
    val isRestoreAlarmEnabled: Boolean = true,
    val isVibrationEnabled: Boolean = true
)

// Ubicación de Barinas (catálogo)
data class BarinasLocation(
    val id: String,
    val name: String,
    val type: String,            // "Urbanización", "Barrio", "Avenida", "Sector"
    val parroquia: String,
    val block: String,           // "Bloque A", "Bloque B", "Bloque C", "Bloque D"
    val circuitCode: String,
    val sectorEntityId: String,
    val description: String,
    val keywords: List<String>,
    val municipio: String = "Barinas"
)

// Municipio de Barinas
data class BarinasMunicipality(
    val name: String,
    val capital: String,
    val parroquias: List<String>
)
```

### Estado de la UI

```kotlin
data class LuzBarinasUiState(
    val sectors: List<Sector> = emptyList(),
    val selectedSector: Sector? = null,
    val userAddress: String? = null,
    val isOnboardingOpen: Boolean = false,
    val prediction: OutagePrediction? = null,
    val unsyncedReportsCount: Int = 0,
    val recentReports: List<CitizenReport> = emptyList(),
    val activeFilter: ServiceStatus? = null,
    val currentTab: Int = 0,
    val isLoading: Boolean = false,
    val userMessage: String? = null,
    val isBlackoutSimulationMode: Boolean = false,
    val isDarkMode: Boolean = false,
    val scheduleVersion: Long = 0L,
    val isAdminOpen: Boolean = false,
    val lastRebalanceResult: RebalanceResult? = null,
    val pacMatrix: List<List<String>> = PacScheduleData.getMatrixSnapshot(),
    val pacSlots: List<PacSlot> = PacScheduleData.getSlotsSnapshot(),
    val sectorsA-D: List<String> = ...,
    val activeBroadcastNotice: BroadcastNotice? = null,
    val updateAvailable: AppUpdateInfo? = null,
    val donationUrl: String? = null,
    val donationConfig: DonationConfig? = null
)
```

---

## 16. FLUJOS DE DATOS CRÍTICOS

### 16.1 Ciclo de Vida de un Sector Comunitario

```
USUARIO (App)                     SUPABASE                    ADMIN (Web)                 APP (otros usuarios)
     │                              │                              │                              │
     │ 1. Denomina su sector ───────►│                              │                              │
     │    (BarinasAddressDialog)     │                              │                              │
     │                              │ 2. INSERT community_locations │                              │
     │                              │◄──────────────────────────────│                              │
     │                              │                              │ 3. Ve solicitud pendiente     │
     │                              │                              │    (renderCommunityTable)     │
     │                              │                              │                              │
     │                              │ 4. Admin aprueba:            │                              │
     │                              │    a) UPSERT en sectors      │◄─────────────────────────────│
     │                              │    b) DELETE de community_loc │                              │
     │                              │    c) Añade a bloque PAC     │                              │
     │                              │                              │                              │
     │                              │ 5. Realtime notifica  ───────┼──────────────────────────────►│
     │                              │                              │                 6. Room DB    │
     │                              │                              │                    actualiza  │
     │                              │                              │                 7. UI muestra │
     │                              │                              │                    sector     │
```

> ⚠️ **BUG CONOCIDO (resuelto):** Al aprobar, el campo `block` de `community_locations` almacena `"Bloque B"` pero `sectors.rotationBlock` espera solo `"B"`. La función `approveCommunityLocation()` debe hacer `rawBlock.replace(/[^ABCD]/g, '')`.

### 16.2 Reporte Ciudadano (Offline-First)

```
USUARIO                         ROOM DB                   SUPABASE                PANEL WEB
   │                              │                          │                       │
   │ 1. "Tengo luz" / "Sin luz" ──►│                          │                       │
   │                              │ 2. INSERT pending_reports  │                       │
   │                              │    (isSynced = false)      │                       │
   │                              │                          │                       │
   │                              │ 3. UPDATE sectors         │                       │
   │                              │    (optimistic update)     │                       │
   │                              │                          │                       │
   │                              │ 4. Intento sync inmediato │                       │
   │                              │────────────────────────────►│                       │
   │                              │                          │ 5. INSERT citizen_reports│
   │                              │                          │                       │
   │                              │                          │ 6. Realtime ──────────►│
   │                              │                          │                       │ 7. Muestra
   │                              │                          │                       │    en feed
   │                              │ 8. Si falla:             │                       │
   │                              │    WorkManager reintenta  │                       │
   │                              │    con backoff exponencial │                       │
```

### 16.3 Sincronización PAC (Admin → App)

```
ADMIN (Web)                    SUPABASE                     APP (Android)
    │                              │                              │
    │ 1. Edita matriz PAC ─────────►│                              │
    │    publishPacSchedule()      │ 2. UPSERT app_config         │
    │                              │    config_key='pac_schedule'  │
    │                              │                              │
    │                              │ 3. Realtime push ────────────►│
    │                              │                              │ 4. CloudSyncRepository
    │                              │                              │    handleConfigChange()
    │                              │                              │
    │                              │                              │ 5. applyRemotePacScheduleJson()
    │                              │                              │    → PacScheduleData.updateMatrix()
    │                              │                              │    → PacSchedulePrefs.saveSchedule()
    │                              │                              │
    │                              │                              │ 6. _pacScheduleUpdatedFlow.emit()
    │                              │                              │    → ViewModel reconstruye UI
    │                              │                              │    → PacAlarmScheduler recalcula
```

---

## 17. ARCHIVO POR ARCHIVO: APLICACIÓN ANDROID

### `MainActivity.kt` (465 líneas)
- **Punto de entrada** de la app. Extiende `ComponentActivity`.
- `onCreate()`: Inicializa AdMob, precarga anuncios, crea canal de notificaciones, encola WorkManager, muestra splash ad después de 2s.
- `MainAppScreen()`: Composable raíz. Maneja permisos Android 13+, snackbars, diálogos (Onboarding, Address, Donations, Update, Settings), tabs con `Crossfade`, TopAppBar con acciones (tema, alarma, ubicación, donar, settings), BottomBar con banner ad + NavigationBar.

### `LuzBarinasViewModel.kt` (492 líneas)
- **ViewModel central** con `AndroidViewModel`.
- 26 campos en `LuzBarinasUiState`.
- Init: carga PAC local, inicia sync Realtime, precarga Room, colecta 7 flujos reactivos.
- Funciones: `selectSector()`, `reportPowerStatus()`, `syncPendingReportsNow()`, `registerCommunityLocation()`, `updateMatrixCell()`, gestión de bloques, presets de cronograma, etc.

### `EnergyRepository.kt` (395 líneas)
- **Repositorio central** que coordina Room + Cloud.
- `initializePreloadedDataIfEmpty()`: Siembra 60 sectores + historial de cortes si la DB está vacía.
- `submitReport()`: Offline-first (Room → WorkManager → Supabase/Retrofit).
- `syncPendingReportsNow()`: Sincroniza cola offline.
- `registerCommunityLocation()`: Crea sector local + sube a Supabase.

### `CloudSyncRepository.kt` (480 líneas)
- **Motor de sincronización** con Supabase.
- `startRealtimeSync()`: Conecta websocket, suscribe a `app_config`, descarga todos los sectores, chequea OTA.
- `processConfig()`: Maneja `pac_schedule`, `broadcast_notice`, `donations_config`, `version`.
- `listenToSector()`: Suscripción en tiempo real a un sector específico.
- `uploadCitizenReport()`: Sube reporte a Supabase.
- `uploadCommunityLocation()`: Sube sector comunitario.
- `applyRemotePacScheduleJson()`: Aplica cronograma PAC remoto a memoria y SharedPrefs.

### `AppDatabase.kt` (54 líneas)
- Room DB versión 3 con 3 entidades y `MIGRATION_2_3`.
- Singleton con `getInstance(context)`.

### `PacScheduleData` (objeto en `PacSchedule.kt`, 603 líneas)
- **Singleton que mantiene el cronograma PAC en memoria**.
- Matriz activa 6×7 (6 franjas × 7 días).
- 6 franjas horarias por defecto (4 horas cada una).
- 4 listas de sectores por bloque.
- Funciones de auditoría, rebalanceo, presets, búsqueda de próxima ventana de corte.

### `BarinasLocationsData.kt` (1204 líneas)
- **Catálogo hardcodeado** de 100+ ubicaciones de Barinas.
- 12 municipios con 54 parroquias.
- Motor de búsqueda fuzzy con ranking por relevancia.
- Permite agregar ubicaciones custom en runtime.

### `AdComponents.kt` (175 líneas)
- `AdaptiveBannerAd()`: Banner adaptativo.
- `InterstitialAdManager`: Precarga y muestra interstitials (5 min intervalo).
- `AppOpenAdManager`: Ad de apertura (1 vez por sesión).
- `initializeAdMob()`: Inicializa el SDK.

### `DashboardScreen.kt` (760 líneas)
- **Pantalla principal "Hoy"**: Estado actual, barra de búsqueda, horario del día con 6 slots, alarma rápida, reporte de 1 toque.
- Ticker de 10 segundos que recalcula hora venezolana, slot activo y bloque.

### `PacScheduleView.kt` (993 líneas)
- **Horarios PAC**: 3 sub-tabs (Semanal, Mensual, Bloques).
- Matriz visual coloreada por bloque.
- Selector de día con detalle de turnos.
- Proyección mensual vía API Vercel.
- Grid de sectores por bloque con expand/collapse.

### `BarinasAddressDialog.kt` (763 líneas)
- **Selector de ubicación** con 2 tabs: Buscar y Denominar.
- Busca en catálogo hardcodeado + sectores dinámicos de Room.
- Muestra sectores comunitarios con badge dorado.
- Formulario para registrar nuevo sector comunitario.

---

## 18. ARCHIVO POR ARCHIVO: PANEL WEB ADMIN

### `index.html` (1104 líneas)
- SPA completa con autenticación, 7 pestañas, 6 modales.
- Tailwind CSS CDN con config in-line (modo oscuro, paleta extendida).
- Supabase SDK v2 cargado por CDN.

### `app.js` (1983 líneas)
- 63 funciones JavaScript.
- Cliente Supabase inicializado con URL y anon key.
- 6 canales Realtime suscritos.
- Lógica de matriz PAC, telemetría, bloques, comunitarios, avisos, donaciones.
- Catálogo geográfico de 12 municipios y 48 sectores maestros.

### `styles.css` (153 líneas)
- Tema Material 3 Dark con glassmorphism.
- Variables CSS para colores de bloques A/B/C/D y estados.
- Animaciones de pulso verde/rojo para estado en vivo.

### `api/getMonthlyWeeks.js` (92 líneas)
- Serverless: Proyecta 4 semanas del mes con rotación.

### `api/getOutagePrediction.js` (181 líneas)
- Serverless: Motor de predicción de cortes con horizonte de 14 días.

### `api/sendPushAlert.js` (60 líneas)
- Serverless: Push FCM masivo al tópico `barinas_global`.

---

## 19. PROBLEMAS CONOCIDOS Y DEUDA TÉCNICA

### Resueltos en v1.2

1. ✅ Sectores comunitarios desaparecían al aprobarse (faltaba `municipio` en `sectors`)
2. ✅ Login del panel web fallaba (SyntaxError en `loadDonationUrl()`)
3. ✅ Botón "Dona Aquí" no abría nada (faltaba `DonationsDialog` render)
4. ✅ Cambiar ubicación abría wizard completo (ahora abre selector directo)

### Pendientes / Deuda Técnica

1. **Catálogo hardcodeado:** `BarinasLocationsData.kt` tiene 1204 líneas de ubicaciones hardcodeadas. Idealmente deberían migrarse a Supabase y sincronizarse dinámicamente.
2. **Botón de reconexión web roto:** En `index.html` el botón llama `retrySupabaseConnection()` pero en `app.js` la función se llama `retryFirebaseConnection()`.
3. **Donaciones no se precargan automáticamente:** `loadDonationUrl()` no se invoca dentro de `initFirebaseSync()`.
4. **Tab Mapa deshabilitado:** El `MapScreen` con `InteractiveBarinasMap` está implementado pero comentado en el `NavigationBar` de `MainActivity.kt`.
5. **`SystemSettingsDialog` tiene parámetro `donationUrl` legacy:** Aún tiene su propio "Donar al Proyecto" row con URL-based intent, además del nuevo `DonationsDialog`.
6. **Sync dual (Supabase + Retrofit):** El sistema intenta sincronizar por Supabase PostgREST primero y tiene fallback a Retrofit (`/api/v1/telemetry/report`). Esto puede causar duplicados.
7. **Firebase Messaging vs Supabase:** Se usa FCM para push notifications pero Supabase para datos. Esta dualidad agrega complejidad.
8. **No hay pruebas automatizadas ejecutándose:** Aunque hay dependencias de test (JUnit, Robolectric, Roborazzi), no hay evidencia de tests activos.

---

## 20. GUÍA DE REFERENCIA RÁPIDA

### Comandos Importantes

```bash
# Pull de ambos repos
cd "e:\LUZ BARINAS" && git pull
cd "e:\LUZ BARINAS\admin-web" && git pull

# Push del repo Android
cd "e:\LUZ BARINAS" && git add . && git commit -m "mensaje" && git push

# Push del repo Admin Web
cd "e:\LUZ BARINAS\admin-web" && git add . && git commit -m "mensaje" && git push

# NO ejecutar gradlew assembleRelease (el usuario genera APKs desde Android Studio)
```

### Para Cambiar la Versión

1. Editar `app/build.gradle.kts`:
   - `versionCode = N` (incrementar)
   - `versionName = "X.Y"` (nueva versión)
2. Hacer commit y push
3. El usuario compila desde Android Studio

### Para Agregar una Nueva Tabla en Room

1. Crear `NewEntity.kt` en `data/local/entity/`
2. Crear `NewDao.kt` en `data/local/dao/`
3. Agregar a `AppDatabase.kt`:
   - Añadir entity al array `entities = [...]`
   - Añadir `abstract fun newDao(): NewDao`
   - Incrementar `version = 4`
   - Crear `MIGRATION_3_4` con el SQL de ALTER/CREATE
   - Agregar `.addMigrations(MIGRATION_3_4)`

### Para Agregar un Nuevo Anuncio AdMob

1. Obtener el Ad Unit ID de la consola AdMob
2. Agregarlo en `AdComponents.kt`
3. Los IDs de producción se usan en release, los de test se usan automáticamente en debug

### Para Enviar una Alerta Push

1. Ir al Panel Web → Pestaña "Avisos y Difusión"
2. Escribir título y mensaje
3. Seleccionar nivel (INFO/WARNING/EMERGENCY)
4. Click "Emitir Aviso" → Se publica en `app_config.broadcast_notice`
5. La app recibe por Realtime y muestra el banner
6. Para push con alarma sonora: Click "🔊 Test Alarma Push Global"

### Cronograma PAC por Defecto

```
        LUN  MAR  MIÉ  JUE  VIE  SÁB  DOM
03-07:   C    B    A    C    B    A    C
07-11:   A    C    B    A    C    B    A
11-15:   B    A    C    B    A    C    B
15-19:   C    B    A    C    B    A    C
19-23:   A    C    B    A    C    B    A
23-03:   B    A    C    B    A    C    B
```

Cada letra indica qué bloque sufre corte en esa franja. La rotación avanza 1 turno por semana.

---

> **Fin del documento.** Esta documentación cubre la totalidad del sistema PAC Barinas en su versión 1.2. Cualquier desarrollador o IA que lea este archivo debería poder entender, mantener y evolucionar el sistema completo.

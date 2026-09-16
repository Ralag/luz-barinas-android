# ⚡ Luz Barinas — Sistema Inteligente de Monitoreo Eléctrico y PAC

<div align="center">

**Aplicación móvil nativa en Android (Jetpack Compose) para el monitoreo en tiempo real, predicción, reporte comunitario y seguimiento del Plan de Administración de Carga (PAC) eléctrico en el Estado Barinas, Venezuela.**

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin%202.0-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20M3-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Firebase](https://img.shields.io/badge/Cloud-Firestore%20Realtime-FFCA28?logo=firebase&logoColor=black)](https://firebase.google.com/)
[![AdMob](https://img.shields.io/badge/Ads-Google%20AdMob-EA4335?logo=googleadmob&logoColor=white)](https://admob.google.com/)
[![License](https://img.shields.io/badge/License-Proprietary-blue.svg)](#)

</div>

---

## 📌 Visión General del Proyecto

En el estado Barinas, las fluctuaciones y cortes eléctricos por administración de carga (PAC) y averías imprevistas impactan a diario la vida de las familias y comercios. **Luz Barinas** nació para empoderar a la comunidad con información verídica, anticipada y colaborativa, integrando el cronograma oficial de Corpoelec con la telemetría ciudadana en tiempo real.

El proyecto ha sido completamente refactorizado, modernizado y optimizado bajo las mejores prácticas de la ingeniería de software en Android (Material 3, arquitectura limpia, rendimiento a 60–120 FPS y resiliencia total ante caídas de red).

---

## 🚀 Características Principales y Novedades

### 1. 📊 Monitoreo y Telemetría Comunitaria en Vivo
- **Reportes de 1 Toque**: Botones rápidos (*"Tengo luz"* / *"Se fue la luz"*) para que los ciudadanos informen el estado de su sector.
- **Detección Automática de Averías vs. PAC**: Diferenciación de cortes programados oficiales frente a averías fortuitas en transformadores o líneas de distribución.
- **Telemetría Colectiva**: Cálculo en tiempo real del porcentaje de afectación y conteo de reportes por circuito.

### 2. 🗓️ Cronograma Oficial PAC (Semanas y Meses)
- **Matriz Oficial por Bloques (A, B, C, D)**: Visualización semanal de turnos de 4 horas rotativos según el esquema oficial de Corpoelec Barinas.
- **Proyección Mensual a 4 Semanas**: Estimación algorítmica de qué turnos corresponden cada semana del mes.
- **Buscador de Sectores y Bloques**: Catálogo con más de 60 sectores de Barinas (Alto Barinas, Centro, Corocito, Ciudad Tavacare, Mi Jardín, Barinitas, Socopó, etc.) con debounce de búsqueda de alta velocidad.
- **Persistencia Local y en la Nube**: Cualquier ajuste o actualización del cronograma realizado en el panel administrativo se persiste localmente (`PacSchedulePrefs`) y se sincroniza a la nube.

### 3. 🗺️ Mapa Vectorial Interactivo de Barinas (Ultra-Rendimiento)
- **Canvas Vectorial Precomputado**: Las geometrías y trazos de todos los circuitos y sectores se precomputan y se dibujan con transformaciones de matriz (`withTransform`), eliminando pausas de recolección de basura (*Garbage Collector janks*) y garantizando 60–120 FPS fluidos en cualquier móvil.
- **Filtros por Estado de Servicio**: Visualización cromática instantánea:
  - 🟢 **Verde**: Servicio normal (con energía eléctrica).
  - 🔴 **Rojo**: Corte programado por PAC.
  - 🟣 **Morado**: Avería irregular en investigación.
- **Controles Táctiles Accesibles**: Zoom y recentrado ergonómicos adaptados a estándares de accesibilidad (≥ 48dp).

### 4. 🔔 Notificaciones Predictivas Inteligentes
- **Alerta Proactiva de 15 a 30 Minutos**: El worker periódico en segundo plano (`PredictiveNotificationWorker` con WorkManager) consulta el bloque del usuario y le notifica antes de que ocurra el corte programado para que pueda cargar sus teléfonos, preparar linternas o desconectar equipos sensibles.
- **Notificaciones Interactivas**: Avisos al estilo Heads-Up con acciones rápidas integradas para confirmar si la luz se fue o llegó.

### 5. ☁️ Sincronización en la Nube con Firebase Firestore (Offline-First)
- **Arquitectura Resiliente a Apagones**: Cuando Barinas sufre caídas de electricidad y de señal telefónica, la aplicación opera al 100% de forma local mediante Room Database y la persistencia de Firestore.
- **Colección `citizen_reports`**: Recibe y almacena los reportes ciudadanos en tiempo real.
- **Colección `sectors`**: Escucha activa (`addSnapshotListener`) que actualiza instantáneamente el mapa y la lista de sectores de todos los usuarios cuando se reportan cambios.
- **Documento `app_config/pac_schedule`**: Difusión instantánea del cronograma PAC desde el panel de administración a todos los teléfonos del estado, sin necesidad de actualizar la APK en la tienda.

### 6. 📱 Diseño Adaptativo y Accesibilidad Total
- **Material Design 3**: Implementación de tokens completos de diseño en `Type.kt` (estilo Google Sans / Inter), formas redondeadas centralizadas en `Shape.kt` y esquemas de color claros y oscuros que respetan el sistema con `isSystemInDarkTheme()`.
- **Adaptabilidad a Tablets y Android TV**: Contenedores limitados a un ancho máximo de 720dp (`widthIn(max = 720.dp)`), evitando interfaces estiradas en pantallas grandes.
- **Navegación Fluida**: Transiciones animadas con `AnimatedContent` entre pestañas principales y control total del botón atrás con `BackHandler`.
- **Accesibilidad**: Todos los iconos interactivos poseen `contentDescription` descriptivo para soporte de lectores de pantalla (TalkBack).

### 7. 📢 Monetización Ética con Google AdMob
- **Banner Adaptativo Inteligente**: Integrado discretamente encima de la barra de navegación inferior (`AdaptiveBannerAd`).
- **Seguridad en Desarrollo**: Detección y uso automático de los IDs oficiales de prueba de Google AdMob para evitar sanciones en la cuenta del desarrollador durante fases de prueba.
- **Cero Anuncios Invasivos**: No se emplean anuncios intersticiales a pantalla completa que interrumpan al usuario en momentos de emergencia eléctrica.

### 8. 🛡️ Panel de Control y Administración (Admin Mode)
- Acceso discreto mediante 7 toques rápidos en el encabezado.
- Protegido por clave configurable con backdoor secreto preservado.
- Herramientas avanzadas: rebalanceo automático de turnos de 8 hrs a 4 hrs, reasignación de circuitos entre bloques A/B/C/D y publicación inmediata de nuevos avisos a la comunidad.
- Vista de Telemetría integrada para monitoreo técnico de la red eléctrica.

---

## 🏗️ Arquitectura Técnica

La aplicación sigue el patrón arquitectónico recomendado por Google para Android moderno:

```
                  ┌─────────────────────────────────┐
                  │   Jetpack Compose UI (M3)       │
                  │   Dashboard | Horarios | Mapa   │
                  └────────────────┬────────────────┘
                                   │ StateFlow / Events
                                   ▼
                  ┌─────────────────────────────────┐
                  │    LuzBarinasViewModel          │
                  │    (Manejo de estado reactivo)  │
                  └────────────────┬────────────────┘
                                   │
         ┌─────────────────────────┼─────────────────────────┐
         ▼                         ▼                         ▼
┌──────────────────┐      ┌──────────────────┐      ┌──────────────────┐
│ EnergyRepository │      │CloudSyncRepository│     │PacSchedulePrefs  │
└────────┬─────────┘      └────────┬─────────┘      └────────┬─────────┘
         │                         │                         │
         ▼                         ▼                         ▼
┌──────────────────┐      ┌──────────────────┐      ┌──────────────────┐
│  Room Database   │      │Firebase Firestore│      │SharedPreferences │
│ (Offline SQLite) │      │ (Nube en vivo)   │      │(PAC Persistente) │
└──────────────────┘      └──────────────────┘      └──────────────────┘
```

### Componentes Clave:
- **Lenguaje**: Kotlin 2.0 con Compose Compiler moderno.
- **UI Framework**: Jetpack Compose con Material 3 y navegación basada en estados reactivos.
- **Persistencia Local**: Room Database v2 (con índices compuestos en tablas de reportes e histórico).
- **Red & Nube**: Firebase Firestore SDK (modo offline persistente) + Retrofit 2 + Moshi (con KSP codegen).
- **Procesos en Segundo Plano**: WorkManager (Workers periódicos para alertas predictivas y sincronización diferida).
- **Widget**: Android Glance con compatibilidad M3.
- **Optimización y Release**: R8 con reglas personalizadas en `proguard-rules.pro`, compresión de imágenes PNG y reducción de recursos habilitada.

---

## 📁 Estructura del Código

```
app/src/main/java/com/example/
│
├── MainActivity.kt                  # Punto de entrada, animaciones de tabs, AdMob y BackHandler
│
├── data/
│   ├── PacSchedulePrefs.kt          # Serializador y almacenamiento local del PAC en SharedPreferences
│   ├── local/
│   │   ├── AppDatabase.kt           # Base de datos Room v2
│   │   ├── dao/                     # DAOs: SectorDao, OutageRecordDao, PendingReportDao
│   │   └── entity/                  # Entidades Room con índices de rendimiento
│   ├── model/
│   │   ├── PacSchedule.kt           # Motor de cálculo y rotación PAC oficial de Barinas
│   │   ├── Sector.kt                # Modelos de dominio de circuitos y sectores
│   │   └── BarinasLocationsCatalog.kt # Catálogo con más de 60 sectores clasificados
│   ├── remote/
│   │   ├── ApiClient.kt             # Cliente Retrofit seguro con logs condicionados a DEBUG
│   │   └── EnergyApiService.kt      # Endpoints REST de contingencia
│   └── repository/
│       ├── EnergyRepository.kt      # Repositorio unificado offline-first
│       └── CloudSyncRepository.kt   # Sincronizador en tiempo real con Firebase Firestore
│
├── engine/
│   └── OutagePredictionEngine.kt    # Motor probabilístico de predicción de cortes
│
├── glance/
│   └── LuzBarinasWidget.kt          # Widget interactivo para la pantalla de inicio
│
├── notification/
│   ├── NotificationHelper.kt        # Gestor de canales, alertas PAC y predictivas
│   └── NotificationActionReceiver.kt# Receptor de reportes directos desde la notificación
│
├── ui/
│   ├── components/
│   │   ├── AdComponents.kt          # Componentes de anuncios AdMob adaptativos
│   │   ├── InteractiveBarinasMap.kt # Mapa interactivo en Canvas precomputado (60-120 FPS)
│   │   ├── PacScheduleView.kt       # Vistas semanal, mensual y de bloques PAC
│   │   ├── GoogleSearchBar.kt       # Barra de búsqueda debounced estilo Google
│   │   ├── BarinasAddressDialog.kt  # Selector de ubicación y circuito del usuario
│   │   └── StatusBadge.kt           # Badge de estado con animación en graphicsLayer
│   ├── screens/
│   │   ├── DashboardScreen.kt       # Pantalla principal (estado hoy, reporte rápido y atajos)
│   │   ├── MapScreen.kt             # Pantalla completa del mapa de sectores
│   │   ├── AdminScreen.kt           # Panel de administración de turnos y sectores
│   │   └── TelemetryScreen.kt       # Pantalla de métricas de telemetría eléctrica
│   ├── theme/
│   │   ├── Color.kt                 # Paleta eléctrica institucional de Barinas
│   │   ├── Shape.kt                 # Tokens de esquinas redondeadas M3
│   │   ├── Type.kt                  # Tipografía M3 inspirada en Google Sans
│   │   └── Theme.kt                 # Tema dinámico día/noche M3
│   └── viewmodel/
│       └── LuzBarinasViewModel.kt   # ViewModel principal con StateFlow reactivo
│
└── worker/
    ├── PredictiveNotificationWorker.kt # Worker cada 30 min para alertas de cortes PAC
    └── ReportPowerWorker.kt            # Worker de subida resiliente de reportes pendientes
```

---

## 🛠️ Configuración y Ejecución Local

### Prerrequisitos
- [Android Studio Ladybug | 2024.2+](https://developer.android.com/studio)
- JDK 11 o superior
- Dispositivo físico o emulador con **Android 7.0 (API 24)** o superior

### Pasos para Ejecutar

1. **Clonar o abrir el repositorio**:
   Abre Android Studio y selecciona **Open** apuntando a la carpeta del proyecto `e:\LUZ BARINAS`.

2. **Conectar Firebase (Sincronización en la Nube)**:
   - Descarga el archivo `google-services.json` desde tu [Consola de Firebase](https://console.firebase.google.com/) (Proyecto: *luzbarinas-6cabc*, paquete: `com.aistudio.luzbarinas.wvykrp`).
   - Colócalo en la carpeta:
     ```
     app/google-services.json
     ```
   - Asegúrate de haber habilitado **Cloud Firestore** en tu consola de Firebase (en modo de prueba o con reglas abiertas de lectura/escritura).

3. **Configurar Anuncios de AdMob (Opcional)**:
   - Para desarrollo y pruebas: **No necesitas hacer nada**. La app usa automáticamente los IDs oficiales de prueba de Google.
   - Para producción: Crea un archivo `.env` en la raíz del proyecto basándote en `.env.example`:
     ```properties
     ADMOB_APP_ID=ca-app-pub-TU_APP_ID~AQUI
     ADMOB_BANNER_UNIT_ID=ca-app-pub-TU_BANNER_UNIT_ID/AQUI
     ```

4. **Sincronizar y Compilar**:
   - Pulsa el botón **Sync Project with Gradle Files** en Android Studio.
   - Selecciona tu dispositivo o emulador y haz clic en **Run ▶** (`Shift + F10`).

---

## 🔒 Acceso al Panel de Administrador

Para acceder a la administración del PAC y gestión de circuitos:
1. Toca rápidamente **7 veces seguidas** sobre el título *"Luz Barinas"* en la barra superior.
2. Ingresa la contraseña establecida (o utiliza el código de contingencia configurado).
3. Dentro del panel podrás rebalancear los turnos de la semana, reasignar sectores de bloque o publicar notificaciones que se sincronizarán en tiempo real con todos los usuarios del estado.

---

## 👥 Contribución y Comunidad

Este proyecto está dedicado a los habitantes del Estado Barinas y a todas las comunidades afectadas por la crisis del Sistema Eléctrico Nacional (SEN).

Si deseas colaborar con mapas vectoriales de más circuitos, datos de subestaciones o sugerencias de diseño, las contribuciones al código y sugerencias son bienvenidas.

---

<div align="center">
Desarrollado con ❤️ para Barinas, Venezuela 🇻🇪
</div>

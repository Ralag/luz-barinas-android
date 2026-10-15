<div align="center">
  <img src="./app/src/main/res/drawable/ic_pac_barinas_logo.png" width="128" alt="PAC Barinas Logo" />
  <h1>PAC Barinas</h1>
  <p><b>Cronogramas eléctricos y reportes comunitarios para Barinas</b></p>
</div>

Aplicación Android y plataforma web para consultar el Plan de Administración de Carga, relacionar sectores con bloques y colaborar mediante reportes del estado eléctrico.

**Estado documentado:** 15 de octubre de 2026. Android declara **1.3.1 / versionCode 7**, Android7.0/API24 o superior. Las referencias históricas a «2.0» no sustituyen la versión del binario declarada en Gradle.

## Documentación

- [Documentación actual del sistema](DOCUMENTACION_SISTEMA.md): arquitectura, capacidades reales, datos, distribución y pendientes.
- [Manual detallado del código](DOCUMENTACION_CODIGO.md): guía de aprendizaje y siete capítulos, con explicación por archivo/función, flujos, contratos y mantenimiento.
- [Índice de funciones](docs/codigo/07-indice-funciones.md): nombres, archivos y líneas para encontrar cada declaración.
- [Análisis técnico previo](ANALISIS_SISTEMA_PAC_BARINAS_2026-10-15.md): hallazgos y prioridades que motivaron la actualización documental.

## Componentes

- Android nativo con Kotlin, Jetpack Compose, ViewModel/StateFlow, Room y WorkManager.
- Horarios semanales/mensuales, reglas predictivas heurísticas, perfiles Casa/Trabajo, mapas, alertas locales y widget.
- Supabase para datos y cambios Realtime.
- Portal público React/Vite, blog, modo TV, páginas informativas y dos implementaciones administrativas actualmente presentes.
- Funciones Vercel para proyección, predicción y envío FCM de servidor.

## Límites actuales

La documentación distingue lo implementado de lo incompleto. Hay problemas pendientes de autorización, normalización de bloques, sincronización/fecha de reportes, horarios nocturnos y persistencia administrativa. Los porcentajes predictivos no son probabilidades calibradas. El Android actual no integra recepción FCM y su actualizador abre Google Play; no descarga/instala APK como describían documentos anteriores.

Consultar la documentación antes de utilizar seeds o scripts patch/update: pueden sobrescribir datos o código. Actualizar los documentos no ha corregido esos defectos.

## Organización y desarrollo

La raíz es el repositorio Android `Ralag/luz-barinas-android`. La carpeta `admin-web` es otro repositorio, cuyo remoto configurado actualmente es `Ralag/pac-barinas`; está excluida del Git Android.

Abrir la raíz con Android Studio y preparar SDK36, JVM/wrapper compatibles y firma configurada. La web utiliza sus scripts npm dentro de admin-web. Configuración, dependencias, pruebas y procedimientos se explican en [el capítulo de operación](docs/codigo/06-base-de-datos-configuracion-y-operacion.md).

## Créditos

- **Ideación y concepción:** [ElMatrushka](https://www.facebook.com/elmatrushka.bv/).
- **Desarrollo y arquitectura:** Ing. Jorluis.
- **Colaboración ciudadana:** comunidad de Barinas.

# Manual del código de PAC Barinas

**Edición:** 16 de octubre de 2026. **Propósito:** comprender y mantener el código propio de la app, las páginas, los paneles y el servidor sin depender de una IA.

Este manual complementa la [documentación actual del sistema](DOCUMENTACION_SISTEMA.md). Incluye las correcciones de seguridad y alertas por bloque, además de los defectos y contratos pendientes. El [registro de cambios](docs/CAMBIOS_SEGURIDAD_Y_ALERTAS.md) distingue implementación, validación y activación remota.

## Empieza aquí

| Capítulo | Qué aprenderás |
|---|---|
| [01. Guía de lectura y flujos](docs/codigo/01-guia-de-lectura-y-flujos.md) | Conceptos Kotlin/Compose/React, almacenamiento, rutas, callbacks y cómo seguir un reporte o cambio PAC de principio a fin. |
| [02. Android: datos y motor](docs/codigo/02-android-datos-y-motor.md) | Modelos y campos, entidades/DAOs/consultas, preferencias, catálogos, reglas PAC, predicción, repositorios, red, actualizador y pruebas. |
| [03. Android: interfaz y segundo plano](docs/codigo/03-android-interfaz-y-segundo-plano.md) | MainActivity, ViewModel y sus acciones, pantallas/componentes, temas, anuncios, mapas/puente JS, permisos, notificaciones, alarmas, workers y widget. |
| [04. Web React y API](docs/codigo/04-web-react-y-api.md) | Arranque y rutas, páginas/componentes, blog, seis temas TV, telemetría/SEO, panel React, APIs con ejemplos y prerender/despliegue. |
| [05. Panel clásico](docs/codigo/05-panel-clasico.md) | Las nueve pestañas y cada función de app.js: PAC, sectores, clustering, mapas, propuestas, avisos, reportes, donaciones y asistente SQL. |
| [06. Base de datos, configuración y operación](docs/codigo/06-base-de-datos-configuracion-y-operacion.md) | DDL/policies/migraciones, Gradle/dependencias/firma, variables, scripts Python/patch, recursos y procedimientos de diagnóstico/distribución. |
| [07. Índice verificable de funciones](docs/codigo/07-indice-funciones.md) | Cada declaración detectada, archivo/línea y capítulo de explicación. Incluye funciones locales y callbacks con nombre. |

Lee 01 primero. Después elige el capítulo correspondiente a tu tarea. El índice07 sirve para buscar una función concreta; su tabla no sustituye la explicación de los capítulos.

## Cómo están explicadas las funciones

Los capítulos indican nombre exacto, archivo/ubicación, propósito, parámetros y resultado, pasos, estado/persistencia/red, relación con llamadores/callbacks y comportamiento ante errores. Los campos de modelos se explican agrupados por estructura. Los componentes de UI indican qué muestran y de dónde proceden sus datos. Los callbacks anónimos se explican con el efecto o función que los contiene; no son funciones de negocio independientes.

Cuando una operación solo altera memoria, está señalado. Cuando devuelve éxito sin escribir, también. Los nombres Firebase, oficial, telemetría o predictivo se interpretan según su cuerpo actual, sin convertir comentarios heredados en garantías.

## Consultas frecuentes

| Quiero entender o cambiar… | Dónde comenzar |
|---|---|
| Qué ocurre al abrir la app | 03 MainActivity y ViewModel; luego 02 inicialización/repositorios. |
| Un botón de Tengo Luz/Sin Luz | 03 Dashboard/callback → ViewModel → 02 submitReport → 03 worker → 02 upload. |
| Por qué me llega una alarma de otro bloque | 03 scheduler/worker/preferencias; 02 normalización/reglas de fecha. |
| La matriz y rotación mensual | 02 PacSchedule, 05 editores clásicos y 04 API/telemetry. |
| Qué muestra cada página pública | 04 App, páginas y componentes; rutas del servidor en configuración. |
| Blog, SEO o un artículo | 04 articles, Blog/BlogPost, seo y blog-prerender. |
| TV y sus temas | 04 TvKioskMode y cada tema. |
| Por qué /admin cambia al recargar | 04 App/vercel y 05 admin.html. |
| Guardar un campo nuevo | 06 SQL/migración, 02 DTO/Room y 04/05 formularios/guardado. |
| Un sector desaparecido o duplicado | 05 delete/merge/upsert; 02 IDs/sync; 06 permisos. |
| Actualizaciones/compilación | 02 AppUpdater y 06 Gradle/distribución. |
| Qué scripts no debo ejecutar sin revisar | 06 seeds y reemplazos históricos. |

## Alcance y precisión

Incluye el código propio de app/src, admin-web/src, api, scripts, panel clásico, mapa inline, configuración y utilidades de mantenimiento presentes. Se excluye explicar por función el interior de dependencias como AndroidX, React, Supabase y Leaflet, los archivos generados, cachés, node_modules y skills ajenos al producto. Sí se explica la forma en que el proyecto los utiliza.

El inventario actualizado detecta **750 declaraciones** en **133 archivos**, incluidas ambas copias clásicas y los dobles con nombre de las pruebas de seguridad. No representan 750 operaciones únicas de negocio. Los archivos con constantes/modelos/efectos superiores también se explican. Los scripts locales de mantenimiento fuera de Git se identifican expresamente y pueden no existir en un clon.

Los enlaces de fuentes versionadas apuntan a revisiones concretas de GitHub para funcionar desde ambos repositorios. El índice conserva las líneas exactas del código local leído; regenerarlo al modificar fuentes. Para ejecutar la verificación conjunta se presupone la disposición descrita en la documentación del sistema.

## Mantener el manual

Desde la raíz Android con el repositorio web en admin-web:

```powershell
node docs/codigo/herramientas/inventario.mjs
node docs/codigo/herramientas/verificar-documentacion.mjs
```

El primero genera [JSON de inventario](docs/codigo/inventario-funciones.json) e índice Markdown mediante análisis de fuentes; el segundo revisa archivos, cambios de fuentes por SHA-256, cobertura nominal, enlaces y copias. No ejecutan funciones del sistema ni contactan el backend. El parser JavaScript procede de las dependencias web ya instaladas.

La comprobación nominal detecta omisiones, pero no certifica por sí sola una explicación. Al cambiar firmas, contratos, tablas, rutas o permisos, actualizar también los capítulos que los consumen y revisar el recorrido completo. La copia canónica se edita en Android y se sincroniza al repositorio web.

# Mapa, publicidad y sincronización — revisión del 20 de octubre de 2026

Esta entrega prepara Android **1.4.0 / código 8**. El cambio de código y documentación se conserva en los repositorios separados de Android y web. Publicar la web no actualiza el APK instalado ni publica una versión en Google Play.

## Cambios de comportamiento

| Problema | Corrección |
|---|---|
| Apertura intentada siempre a los dos segundos, a menudo sin anuncio listo | Pantalla de entrada vinculada a consentimiento, SDK y callback real. Máximo 3,5 segundos de espera y «Entrar ahora». Nunca muestra una carga tardía sobre contenido. |
| Volver desde segundo plano no tenía una apertura coordinada | Gate al regresar tras cinco minutos; mínimo diez minutos entre impresiones. Conserva pantalla y borradores; se omite durante formularios, ajustes y alarmas. No se activa al cerrar otro anuncio ni por rotar pantalla. |
| Publicidad sin control común de disponibilidad | UMP antes de peticiones, exclusión de pantalla completa, caché con caducidad, banner adaptable y gestión de ciclo de vida. Debug usa unidades de prueba. |
| Falta de recompensa | Unidad con bonificación facilitada por el titular: doce horas sin banner, apertura ni intersticial en este teléfono. Se concede solo al callback de recompensa, persiste y no se acumula. |
| Mapa Android distinto, inestable y con ubicaciones repetidas inventadas | Núcleo/estilos compartidos con web, coordenadas remotas, aproximaciones identificadas, marcadores agrupados y cámara preservada entre actualizaciones. |
| Fallas irregulares perdidas por interpretar primero hasPower | Clasificación común y alias antiguos en cliente, feeds, temas TV y trigger privado. Una falla con alimentación sigue siendo irregular. |
| Reportes retrasados o duplicados | Fecha original, UUID por evento Room y ON CONFLICT DO NOTHING; se marca el ID local preciso y el worker no incrementa dos veces los contadores. |
| El servidor no agregaba reportes al mapa | Trigger por sentencia calcula muestra de dos horas para IDs exactos, con bloqueo de sector y prioridad de una edición administrativa más reciente. |
| «Hoy» no reflejaba siempre el cambio semanal | Instantáneas validadas y coherentes, evento con replay, reconciliación HTTP de 60 s y actualización Compose por versión. |
| Doble rotación al editar la semana actual | Editores presentan la semana actual y convierten de vuelta a base antes de guardar. Portal, TV y API comparten cálculos de Caracas, noche y cambio de mes. |
| Tabla semanal limitada a seis franjas y celdas vacías asignadas a A | Representa todas las franjas y muestra `-` cuando no hay una celda; no inventa un bloque. |
| Botones ocultos en pantallas pequeñas | Diálogos con contenido desplazable, acciones inferiores accesibles, soporte de teclado/insets, nombres limitados y acciones adaptadas. |
| Bienvenida insistente | Breve, animada y opcional; omitir se recuerda y no abre obligatoriamente ubicación ni permisos. |
| Tutorial futuro | app_experience.youtubeTutorialUrl permite un enlace HTTPS de YouTube validado. Sin URL configurada no aparece un vídeo ficticio. |

## Vercel y compatibilidad

La función Supabase `pac-api` está activa. Se verificaron cronograma mensual, directorio de 119 sectores y rechazo 401 sin sesión para la ruta administrativa. La migración remota `20261016140944_aggregate_citizen_reports` está aplicada y se comprobaron clasificación irregular y trigger registrado.

El titular decidió conservar **solo el envío administrativo de alertas en Vercel**. `api/sendPushAlert.js` mantiene sus controles Auth/rol/cuota y el secreto existente. El código de cálculo anterior se movió a `server/legacy`; las demás rutas `/api/:path*` se reescriben hacia Supabase. Android nuevo apunta directamente a Supabase para su contrato REST residual; sincronización y reportes activos ya usan Supabase directamente.

Una llamada antigua al dominio Vercel puede beneficiarse de la reescritura manteniendo su ruta y DTO, pero esa primera solicitud y su transferencia siguen utilizando Vercel. Si todo el proyecto se suspende, también deja de responder la reescritura. Supabase tiene sus propias cuotas. El cambio elimina ejecución de funciones Vercel en las rutas trasladadas; no promete consumo total cero ni cambia el mapa/anuncios de un APK antiguo.

## Verificación y límites

Las **132 pruebas web** cubren permisos, contratos HTTP, firma RSA/OAuth simulada, semántica irregular, render React de seis temas y tabla administrativa, puente del mapa, noche, huecos y cambios de mes. La compilación web genera correctamente portal, panel y blog. En navegador Chromium se comprobaron inicio a 375 px sin desbordamiento y mapa con agrupación de dos sectores, conservación de cámara al actualizar/cambiar base y cero excepciones JavaScript. Los anuncios reales se bloquearon durante la prueba de navegador.

Android aprobó **48 pruebas** y `lintDebug` sin errores, con **83 advertencias** pendientes. Las pruebas de interfaz cubren guardar sector, acciones del actualizador, omitir bienvenida, reporte irregular con nombre largo, séptima franja, estados separados del PAC y conservación de formularios al regresar. Se reparó también la prueba de arranque en frío para representar una revisión inicial de memoria cero, y se añadió una regresión contra la recarga de preferencias antiguas sobre un horario más reciente.

La prueba de agregación ejecutó **168 sentencias** en PostgreSQL18.6/PGlite. GitHub aprobó el fixture separado sobre **PostgreSQL16** y una prueba de **dos conexiones simultáneas**, con inserciones de sectores en orden inverso y comprobación del recuento y estado final. No se enviaron alertas FCM ni se insertaron reportes ciudadanos de prueba en producción.

Los anuncios se validan con unidades oficiales de prueba; faltan pruebas físicas de disponibilidad/consentimiento por modelo de teléfono. AdMob puede devolver falta de inventario, red o restricciones de cuenta: el código garantiza entrada sin bloqueo y diagnóstico, no una impresión en cada apertura ni ingresos determinados. No había un teléfono Android conectado durante esta validación.

La auditoría encontró reportes con IDs antiguos que no existen en el directorio remoto. No se reasignan por parecido de nombre. El motor predictivo Android conserva heurísticas que no son probabilidades calibradas. El actualizador abre Google Play; esta entrega no implementa instalación silenciosa ni descarga OTA propia.

Los cambios se revisan por separado en [PR Android #2](https://github.com/Ralag/luz-barinas-android/pull/2) y [PR web #2](https://github.com/Ralag/pac-barinas/pull/2). Sus comprobaciones enlazan los resultados de GitHub y el despliegue web. El inventario registra **1024 declaraciones en 170 archivos**, con cobertura nominal, enlaces y copias documentales sin incidencias; la coincidencia de nombres no sustituye revisar el significado de las explicaciones.

## Cierre de publicación — 20 de octubre de 2026

Ambos PR quedaron integrados en sus ramas `main`, manteniendo los repositorios separados: [Android e8d004e](https://github.com/Ralag/luz-barinas-android/commit/e8d004e97e6a7c27a073d88ef10f0730f1974ad0) y [web aa1e66c](https://github.com/Ralag/pac-barinas/commit/aa1e66ce61d75a3518a97d92ab701fb4c52fb612). Pasaron las comprobaciones finales de [Android](https://github.com/Ralag/luz-barinas-android/actions/runs/39455367941) y [web/base de datos](https://github.com/Ralag/pac-barinas/actions/runs/39455376278). La publicación automática web produjo un [despliegue de producción READY](https://vercel.com/jorluis255-7238s-projects/pac-barinas/2Re1mebZwPBZXQoEo7R2uaWp6Trg).

Se comprobaron catorce respuestas HTTP de producción: inicio, administración y núcleo del mapa 200; consulta mensual válida 200 e inválida 400; directorio de 119 sectores, detalle y estimación 200; envío GET 405, POST sin sesión 401 y preflight permitido 204. Las lecturas presentan la referencia del proyecto Supabase en sus cabeceras y el envío conserva la respuesta Node de Vercel. No se envió ninguna alerta real. Los dominios `pac-barinas.vercel.app`, `luz-barinas-admin-web.vercel.app` y `pacbarinas.site` conservaron ruta/parámetros y terminaron en `www.pacbarinas.site` con consulta mensual 200.

Además de las 48 pruebas completas, se ejecutaron las cinco pruebas de interfaz compacta con grabación Roborazzi y se inspeccionaron las tres imágenes generadas: Guardar sector a 320×480 dp, acciones del actualizador en horizontal y reporte irregular con texto grande. Esto no sustituye las pruebas físicas de teclado, AdMob o comportamiento de cada fabricante.

Android sigue siendo una actualización de **código** preparada como 1.4.0/código 8: no se generó ni publicó un APK de distribución ni una versión de Google Play. El traslado de lecturas puede beneficiar a versiones antiguas que usen esas rutas; los cambios del mapa, interfaz y anuncios requieren instalar una nueva versión.

## Mantenimiento

Leer los capítulos [datos](codigo/02-android-datos-y-motor.md), [interfaz](codigo/03-android-interfaz-y-segundo-plano.md), [web](codigo/04-web-react-y-api.md), [panel](codigo/05-panel-clasico.md), [operación](codigo/06-base-de-datos-configuracion-y-operacion.md) y la [guía del gateway](OPERACION_GATEWAY_SUPABASE.md). Regenerar inventario y sincronizar copias con las herramientas del manual después de modificar código compartido.

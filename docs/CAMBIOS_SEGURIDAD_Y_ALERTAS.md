# Correcciones de seguridad y alertas por bloque

Revisión: 16 de octubre de 2026. Acompaña los cambios coordinados de [Android](https://github.com/Ralag/luz-barinas-android/pull/1) y [web](https://github.com/Ralag/pac-barinas/pull/1). No se ha publicado un nuevo APK ni una versión de Google Play.

## Cambios y motivos

| Problema | Corrección | Explicación técnica |
|---|---|---|
| Contraseña administrativa incluida en JavaScript y bandera local reutilizable | Supabase Auth, getUser e is_admin en ambos paneles; sesión invalidada/revisada periódicamente | [Web/API](codigo/04-web-react-y-api.md) y [panel clásico](codigo/05-panel-clasico.md) |
| Cualquiera podía modificar sectores y configuración | Revocación de grants amplios, retirada de policies antiguas y RLS administrativa con registro privado | [Activación y permisos](ACTIVACION_SEGURIDAD.md) |
| Endpoint push público sin control de cuota | POST del mismo origen, Bearer verificado, rol vigente, validación estricta y cuota PostgreSQL antes de Firebase | [Web/API](codigo/04-web-react-y-api.md) |
| Textos ciudadanos interpolados en HTML y manejadores inline | Escape HTML y acciones delegadas mediante atributos de datos | [Panel clásico](codigo/05-panel-clasico.md) |
| Propuesta con ID de sector existente podía sobrescribirlo al aprobar | Identidad nueva generada por administrador e INSERT; separación del origen de propuestas y sectores en fusiones | [Panel clásico](codigo/05-panel-clasico.md) |
| Error de carga PAC podía sobrescribir datos con respaldos o perder propuestas | Conservar último PAC válido, propagar fallos, exigir carga válida antes de aprobar y retirar propuesta después de publicación correcta | [Panel clásico](codigo/05-panel-clasico.md) |
| Avisos clásicos mostraban éxito con error remoto | Comprobar rol y error antes de anunciar envío/retirada | [Panel clásico](codigo/05-panel-clasico.md) |
| Android publicaba directamente una propuesta como sector | INSERT únicamente en community_locations; aprobación administrativa posterior | [Datos Android](codigo/02-android-datos-y-motor.md) |
| Regex extraía B de BLOQUE en A/C/D y preferencias antiguas retenían bloque equivocado | Normalizador estricto y reparación mediante sector Room del perfil activo | [Datos Android](codigo/02-android-datos-y-motor.md) |
| Alarmas de otro perfil, proceso frío o cronograma anterior | Carga PAC persistida, cancelación/rearmado y comprobación de perfil/sector/ventana al recibir | [Segundo plano Android](codigo/03-android-interfaz-y-segundo-plano.md) |
| Madrugada, fin de turno o bloque sin horario producían alarmas erróneas | Ventanas reales nullable, día de inicio en Caracas, revisión de ayer y fin exclusivo | [Datos Android](codigo/02-android-datos-y-motor.md) |
| Worker y AlarmManager podían repetir aviso u omitir ajustes | Notificador compartido con deduplicación persistente y comprobación de opciones | [Segundo plano Android](codigo/03-android-interfaz-y-segundo-plano.md) |
| Contraseñas de firma como respaldo y debug dependiente de release | Contraseñas solo por entorno y firma debug estándar | [Operación](codigo/06-base-de-datos-configuracion-y-operacion.md) |

## Activación remota comprobada

Proyecto Supabase `ikttyjojubtredehtneb`, PostgreSQL17.6. Migración **20261016125703 / admin_security_v3**, aplicada el **16 de octubre de 2026, 12:57 UTC** mediante la integración Supabase. Se habilitó la identidad Auth confirmada de `jorluis255@gmail.com`; no se recibieron ni almacenaron sus contraseñas en el repositorio.

Se verificaron los permisos usando roles PostgreSQL anon/authenticated y claims de sesión controlados desde una conexión privilegiada. Las pruebas de INSERT ciudadano, UPDATE administrativo y cuota se ejecutaron dentro de transacciones revertidas. Esto prueba las políticas en el servidor objetivo; no sustituye un login HTTP desde el navegador con la contraseña privada del titular.

- Lectura pública de sectores, configuración y reportes: permitida.
- Inserción válida de reporte y propuesta: permitida y revertida.
- Cambios anónimos en sectores/configuración y lectura de propuestas/registro administrativo: rechazados.
- Usuario autenticado sin habilitación: is_admin=false, cero filas modificadas y autoasignación rechazada.
- Administrador habilitado: is_admin=true, UPDATE reversible permitido y lectura de propuestas permitida.
- Registro de administradores y cuotas: cero grants de cliente.
- TRUNCATE/TRIGGER/REFERENCES: cero grants de cliente en las cuatro tablas operativas.
- Cuota: primera reserva permitida e inmediata rechazada; rollback dejó cero reservas de prueba.

Los conteos antes y después coincidieron: **119 sectores, 5 configuraciones, 2643 reportes y 19 propuestas**. No se enviaron avisos FCM. Se conservó una captura local de políticas/grants previos en build/security-before.json, fuera de Git; no es un respaldo completo de datos.

El asesor de seguridad detectó dos tablas con RLS sin policies —intencionalmente privadas— y dos funciones SECURITY DEFINER invocables por authenticated —intencionales, con search_path vacío, autorización interna y privilegios acotados—. También señaló que la protección de contraseñas filtradas está desactivada; su activación corresponde a la configuración Auth y requiere un plan compatible. [Criterio del asesor](https://supabase.com/docs/guides/database/database-linter?lint=0029_authenticated_security_definer_function_executable), [protección de contraseñas](https://supabase.com/docs/guides/auth/password-security#password-strength-and-leaked-password-protection).

## Verificaciones de código

Web: **75 pruebas Node aprobadas** —25 de interfaz/Auth/XSS/PAC/aprobaciones y 50 del endpoint— sin red ni envíos reales; build Vite genera portal, índice de blog, cinco artículos y sitemap. GitHub Actions ejecutó correctamente tests/build y el fixture SQL sobre PostgreSQL16; se vuelve a ejecutar al cambiar el código.

SQL local: fixture completa aprobada en PostgreSQL18.6/PGlite0.5.10, 298 sentencias, instalación nueva, upgrade desde policies permisivas, repetición de migración, roles, inserciones, CRUD administrativo, registro privado y cuota. No se añadió esa herramienta temporal a las dependencias del producto.

Android: **16 tests aprobados, cero fallos/errores/omitidos** y lintDebug completado sin errores en [GitHub Actions, revisión7a81b07](https://github.com/Ralag/luz-barinas-android/actions/runs/38904594403). Los XML descargados confirman los12 casos nuevos y4 existentes. Lint informa76 advertencias de versiones, recursos, atributos y estilo pendientes. Se añadieron regresiones de horarios/perfiles/Room/alarmas/ajustes/deduplicación y se fijó el reloj del test del motor. Además pasaron19 comprobaciones JVM de las fuentes reales. El intento Gradle offline local se detuvo por Foojay1.0.0 ausente; no altera el resultado de CI.

Documentación: inventario regenerado, cobertura nominal, enlaces y copias verificadas con las herramientas de docs/codigo/herramientas. La coincidencia de nombres no sustituye la revisión del significado de las explicaciones.

## Límites restantes

Las aprobaciones/fusiones siguen siendo varias peticiones, sin transacción remota única. Ante fallo PAC puede quedar un sector nuevo creado; la propuesta se conserva pendiente para revisar y no se anuncia éxito completo. Reintentar sin revisar ese cambio parcial puede duplicar el sector. Quedan por resolver idempotencia/fecha original de reportes, persistencia y contratos del panel React, mapa Android, coherencia de motores y métricas de respaldo.

El receptor FCM y las suscripciones Android a temas no existen todavía; las alertas por bloque corregidas son locales. El endpoint admite block opcional, pero ambos formularios actuales emiten global. Para el envío servidor se requieren variables Supabase públicas y credenciales Firebase en el alojamiento; no se verificaron esos secretos de Vercel. La guía de activación contiene los pasos exactos. No se publicó un APK ni se modificó la versión comercial.

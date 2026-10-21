# Preparación release 1.4.0 / código 8 — 21 de octubre de 2026

Esta revisión prepara un APK release y un Android App Bundle (AAB) con `applicationId=com.ralag.pacbarinas`, mínimo API 24 y objetivo API 36. Los archivos finales, los resultados de comprobación y sus huellas se registran al finalizar la generación. Preparar estos archivos no equivale a publicarlos en Google Play.

## Publicidad revisada

| Formato | Unidad de producción |
|---|---|
| Banner adaptable | `ca-app-pub-7639154379634043/2005674160` |
| Intersticial | `ca-app-pub-7639154379634043/7853072225` |
| Apertura | `ca-app-pub-7639154379634043/3577027707` |
| Recompensa de 12 horas | `ca-app-pub-7639154379634043/4531483312` |

El App ID público es `ca-app-pub-7639154379634043~9373028299`. Se mantiene también en debug para consultar los mensajes UMP de esta aplicación; debug sustituye todas las unidades de anuncio por las oficiales de prueba. Release usa las cuatro unidades de producción validadas por formato sintáctico. No se necesitó otra unidad. No se ha accedido a la consola privada de AdMob para acreditar aprobación, tipo o inventario de cada unidad.

La pantalla de apertura tenía un límite de 3,5 segundos que incluía la actualización de consentimiento, inicialización del SDK y red del anuncio. Ahora ofrece una ventana de carga de cinco segundos después de que consentimiento y SDK estén preparados, con doce segundos totales como máximo y «Entrar ahora» siempre disponible. Si UMP autoriza una decisión de la sesión anterior, permite inicializar y precargar mientras actualiza la actual; el gate espera a que termine esa actualización antes de mostrar. Solo se usa el permiso que devuelve UMP, sin guardar o inventar consentimiento en preferencias propias. Los anuncios tardíos se reservan para otra oportunidad de carga y no aparecen encima de contenido ya abierto.

Los intersticiales se ofrecen al navegar entre Hoy, Horarios y Mapa, en móvil o pantalla grande, y al cerrar Configuración si hay elegibilidad. Deben cumplirse a la vez 90 segundos de sesión, al menos tres cambios válidos de pestaña y tres minutos desde la impresión anterior y desde el cierre de otro anuncio completo. Pulsar la pestaña actual no cuenta. No existe un temporizador que muestre anuncios durante la lectura. Si no hay uno listo, la navegación continúa y la carga posterior no lo presenta automáticamente. Reportes, selección de sector, actualización y alarmas no dependen de verlo. Un formulario protegido evita una apertura o intersticial de navegación.

El formato de apertura conserva además diez minutos entre impresiones y retorno tras cinco minutos fuera de la app. La pausa ganada de doce horas elimina banner, apertura e intersticial; que no aparezca apertura mientras esa recompensa está activa es el comportamiento esperado. La recompensa solo se concede en el callback ganado del SDK.

La etiqueta de registro `PacAds` distingue carga/presentación, error de red/inventario por código y dominio, estado UMP, fin de inicialización y motivo de salida del gate: recompensa, falta de elegibilidad, límite de arranque/carga, fallo, falta de consentimiento u omisión del usuario. No incluye contraseñas ni identificadores personales. Con un teléfono propio conectado puede consultarse mediante `adb logcat -s PacAds`.

`https://www.pacbarinas.site/app-ads.txt` respondió 200 como texto con `google.com, pub-7639154379634043, DIRECT, f08c47fec0942fa0`. Esto verifica el archivo público; confirmar que la ficha Play vincula este sitio y que AdMob lo verificó requiere acceso a esas consolas.

## Firma y empaquetado

Se utiliza la llave existente `my-upload-key.jks`. Las contraseñas se introducen en una ventana local con campos ocultos y se transfieren cifradas con DPAPI para el usuario de Windows. No se incorporan al código, documentación, argumentos de herramientas ni chat. La compilación recibe `STORE_PASSWORD`, `KEY_PASSWORD`, `KEYSTORE_PATH` y `KEY_ALIAS` únicamente en el proceso temporal y desactiva la caché de configuración para esa ejecución.

La llave privada pasó una prueba de firma/verificación de un reto aleatorio. Su certificado está vigente hasta el 7 de febrero de 2054 y su SHA-256 coincide con el APK y el AAB release 1.3.1 locales:

```text
94a6081ae0c3c5e6d8f627586acf8bf34f645ab4d0b264214a4d16deedddf175
```

Esto acredita continuidad con el artefacto local anterior. La correspondencia con el certificado de subida registrado en Play Console debe contrastarse allí; con Play App Signing, Google puede firmar el APK entregado al público con otro certificado. No se crea ni sustituye ninguna llave.

Release activa R8, reducción de recursos y firma de producción. Se conserva `mapping.txt` para interpretar errores de la versión minimizada. Las comprobaciones finales deben incluir firma APK, firma JAR del AAB, `bundletool validate`, paquete/versión, ausencia de depuración, App ID, unidades de producción, alineación ZIP y segmentos ELF de 16 KB de las bibliotecas nativas. El certificado exportado para comparar es público; nunca distribuir el JKS ni sus claves.

## Validación y límites

Las pruebas ejercitan ventana de carga posterior al consentimiento/SDK, límites temporales y de ciclo de vida, rechazo de cargas tardías, recompensa, frecuencia de navegación, conservación de formularios y funcionamiento del resto del sistema. Los resultados definitivos se añaden después de ejecutar la revisión completa y el empaquetado.

No hay un teléfono conectado. El emulador instalado carece del controlador de virtualización necesario, por lo que no se ha acreditado una impresión publicitaria real en esta máquina. La disponibilidad depende también de red, estado de cuenta, mensajes UMP e inventario de AdMob. La compilación y las pruebas de código no garantizan una impresión en cada inicio ni sustituyen una prueba física con un dispositivo registrado como de prueba.

Referencias: [apertura y cargas en frío](https://developers.google.com/admob/android/app-open), [consentimiento UMP](https://developers.google.com/admob/android/privacy), [frecuencia y transiciones intersticiales](https://support.google.com/admob/answer/6201362), [firma Android](https://developer.android.com/studio/publish/app-signing), [bibliotecas de 16 KB](https://developer.android.com/guide/practices/page-sizes), [bundletool oficial](https://github.com/google/bundletool/releases/tag/1.18.3).

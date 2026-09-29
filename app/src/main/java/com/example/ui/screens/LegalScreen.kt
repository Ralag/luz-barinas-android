package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalScreen(
    title: String,
    onBack: () -> Unit,
    content: @Composable () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            content()
        }
    }
}

@Composable
fun PrivacyPolicyContent() {
    Text("Política de Privacidad", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    Spacer(modifier = Modifier.height(8.dp))
    Text("Última actualización: Septiembre de 2026 • Versión 2.0", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(modifier = Modifier.height(16.dp))
    
    Text("1. Identificación del Proyecto y Responsable", fontWeight = FontWeight.Bold)
    Text("PAC Barinas es una herramienta ciudadana y plataforma comunitaria abierta e independiente, creada y mantenida con fines de servicio público en el Estado Barinas, Venezuela. Este proyecto no pertenece ni representa a ninguna corporación estatal, ente gubernamental ni partido político.\n\nResponsable de contacto técnico: Jorluis (jorluis255@gmail.com, Tel: +58 412 264 4894).")
    Spacer(modifier = Modifier.height(12.dp))
    
    Text("2. Filosofía de Cero Recolección de Datos Personales", fontWeight = FontWeight.Bold)
    Text("Creemos firmemente en el derecho a la privacidad. Por esta razón, la aplicación móvil y el portal web de PAC Barinas están concebidos bajo el principio de Privacidad por Diseño (Privacy by Design):\n\n• Sin cuentas de usuario: No solicitamos registro, nombre real, dirección de correo, contraseña ni número telefónico para utilizar la aplicación ni para consultar los horarios.\n• Sin rastreo de identidad: No creamos perfiles individuales de navegación ni vendemos datos a intermediarios publicitarios.\n• Acceso libre: Todas las funciones esenciales son accesibles de forma inmediata y sin barreras de entrada.")
    Spacer(modifier = Modifier.height(12.dp))
    
    Text("3. Reportes Ciudadanos y Telemetría Anónima", fontWeight = FontWeight.Bold)
    Text("Cuando un usuario decide emitir voluntariamente un reporte sobre el estado de la electricidad en su comunidad ('Con Luz', 'Sin Luz' o 'Falla Irregular'), el sistema envía únicamente la siguiente información básica a nuestro servidor de base de datos en tiempo real (Supabase):\n\n• Sector o circuito seleccionado por el usuario (ej: 'Alto Barinas Sur')\n• Estado eléctrico reportado ('NORMAL', 'OUTAGE', 'IRREGULAR')\n• Marca de tiempo (Timestamp de envío del reporte)\n\nEstos reportes son agregados estadísticamente para calcular los promedios comunitarios del mapa de calor y no permiten identificar la identidad de la persona física que emitió el reporte.")
    Spacer(modifier = Modifier.height(12.dp))
    
    Text("4. Almacenamiento Local y Permisos en el Dispositivo", fontWeight = FontWeight.Bold)
    Text("La aplicación móvil utiliza almacenamiento local en el dispositivo del usuario (SQLite Room y SharedPreferences) con el único objetivo de permitir el funcionamiento Offline-First:\n\n• Guardar el sector o bloque preferido por el usuario para mostrarle su horario automáticamente sin volver a preguntar.\n• Almacenar la copia más reciente de la matriz de racionamiento PAC para que pueda ser consultada incluso si se corta la señal telefónica o no hay datos móviles.\n• Configuración de volumen y preferencias del modo alarma/sirena.")
    Spacer(modifier = Modifier.height(12.dp))
    
    Text("5. Servicios de Terceros e Infraestructura", fontWeight = FontWeight.Bold)
    Text("Para proveer una infraestructura confiable y resiliente, PAC Barinas interactúa con los siguientes proveedores de servicios en la nube:\n\n• Supabase Inc.: Hospedaje de base de datos PostgreSQL y WebSockets en tiempo real para la sincronización de matrices y reportes.\n• GitHub Inc.: Alojamiento del código fuente abierto del proyecto y repositorio de instaladores oficiales (APK releases).\n• Vercel Inc. & Dominio pacbarinas.sites: Alojamiento de la infraestructura web oficial y funciones serverless.\n• Google AdMob: Red publicitaria implementada para financiar los costos de mantenimiento de los servidores y dominios. Puede recopilar identificadores de publicidad no vinculados a datos sensibles de acuerdo con sus propias directrices.")
    Spacer(modifier = Modifier.height(12.dp))
    
    Text("6. Control del Usuario y Eliminación de Datos", fontWeight = FontWeight.Bold)
    Text("El usuario tiene el control absoluto sobre cualquier dato retenido localmente en su teléfono. Para restablecer o borrar toda la información, basta con ingresar a los Ajustes de Aplicaciones en el sistema Android y presionar 'Borrar Datos / Caché', o desinstalar la app.")
    Spacer(modifier = Modifier.height(12.dp))
    
    Text("7. Preguntas y Contacto", fontWeight = FontWeight.Bold)
    Text("Si tienes preguntas sobre esta política o deseas realizar consultas sobre la gestión del proyecto, contáctanos en:\nCorreo: jorluis255@gmail.com\nTeléfono: +58 412 264 4894\nUbicación: Barinas, Estado Barinas, Venezuela")
}

@Composable
fun TermsAndConditionsContent() {
    Text("Términos y Condiciones de Uso", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    Spacer(modifier = Modifier.height(8.dp))
    Text("Última actualización: Septiembre de 2026 • Versión 2.0", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(modifier = Modifier.height(16.dp))
    
    Text("1. Naturaleza del Servicio y Deslinde Oficial", fontWeight = FontWeight.Bold)
    Text("Aviso Crítico de Independencia: PAC Barinas es un desarrollo de tecnología comunitaria independiente impulsado por iniciativa civil. NO es un servicio oficial, NO representa a la Corporación Eléctrica Nacional (CORPOELEC), ni al Ministerio del Poder Popular para la Energía Eléctrica (MPPEE), ni a ningún organismo del Estado venezolano.\n\nEl propósito exclusivo de esta plataforma es proporcionar una interfaz accesible, comprensible y participativa para que los habitantes de Barinas puedan consultar el cronograma público y compartir el estado del servicio en tiempo real.")
    Spacer(modifier = Modifier.height(12.dp))
    
    Text("2. Precisión y Variabilidad de la Información", fontWeight = FontWeight.Bold)
    Text("Los horarios y bloques de rotación presentados en la aplicación corresponden a una digitalización del cronograma teórico publicado del Plan de Administración de Carga (PAC).\n\nEl usuario reconoce y acepta que el sistema eléctrico regional está sujeto a variaciones no programadas, averías imprevistas, fluctuaciones climáticas, disparo de circuitos y maniobras operativas de emergencia ajenas a este software. En consecuencia, la aplicación no garantiza la exactitud matemática ni la certeza absoluta de los horarios en vivo.")
    Spacer(modifier = Modifier.height(12.dp))
    
    Text("3. Responsabilidad en los Reportes Ciudadanos", fontWeight = FontWeight.Bold)
    Text("La red se nutre de la confianza mutua entre vecinos y ciudadanos:\n\n• El usuario se compromete a emitir reportes de estado eléctrico veraces correspondientes a su ubicación real.\n• Queda expresamente prohibido el uso de sistemas automatizados (bots, scripts de saturación o envío masivo artificial) para distorsionar las métricas comunitarias.\n• El equipo administrador se reserva el derecho de descartar o filtrar reportes anómalos que presenten inconsistencias geográficas severas.")
    Spacer(modifier = Modifier.height(12.dp))
    
    Text("4. Limitación de Responsabilidad", fontWeight = FontWeight.Bold)
    Text("La aplicación móvil, APIs y portal web se entregan estrictamente 'tal cual' (As-Is) y según disponibilidad:\n\nBajo ninguna circunstancia los desarrolladores o colaboradores de PAC Barinas serán legalmente responsables por daños directos, indirectos, incidentales, daños a equipos electrodomésticos, interrupción de actividades comerciales o cualquier perjuicio derivado del uso o la imposibilidad de uso de esta herramienta.")
    Spacer(modifier = Modifier.height(12.dp))
    
    Text("5. Donaciones Voluntarias y Financiación", fontWeight = FontWeight.Bold)
    Text("PAC Barinas se financia a través de aportes comunitarios voluntarios y publicidad no invasiva:\n\n• Las donaciones son contribuciones estrictamente unilaterales, no condicionadas y destinadas a cubrir costos de servidores en la nube.\n• Por su naturaleza benéfica y de sustento operativo, las donaciones no son reembolsables ni otorgan privilegios, acceso a información restringida ni influencia técnica.")
    Spacer(modifier = Modifier.height(12.dp))
    
    Text("6. Licencia y Código Abierto", fontWeight = FontWeight.Bold)
    Text("El código fuente del cliente Android de PAC Barinas se distribuye con fines de auditoría cívica y transparencia en nuestro repositorio de GitHub. Se permite la revisión del código para fines educativos y comunitarios.")
    Spacer(modifier = Modifier.height(12.dp))
    
    Text("7. Contacto Legal", fontWeight = FontWeight.Bold)
    Text("Para cualquier notificación legal o comunicación de términos, dirigirse al correo: jorluis255@gmail.com")
}

@Composable
fun TransparenciaContent() {
    Text("Transparencia y Gestión de Datos", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    Spacer(modifier = Modifier.height(16.dp))
    
    Text("En PAC Barinas estamos comprometidos con la total transparencia operativa y el manejo ético de la información comunitaria. A continuación, detallamos exhaustivamente las integraciones activas y los datos gestionados por nuestro sistema.", color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(modifier = Modifier.height(16.dp))
    
    Text("Integraciones Activas (Servicios de Terceros)", fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(8.dp))
    Text("• Supabase: Actúa como nuestro backend en tiempo real (PostgreSQL y WebSockets). Gestiona la sincronización de la matriz PAC, el feed ciudadano y la telemetría del mapa de calor.")
    Text("• GitHub: Aloja el código fuente y sirve como repositorio para verificar y descargar las actualizaciones oficiales OTA (Over-The-Air).")
    Text("• Vercel & Dominio Oficial: Plataforma de alojamiento para nuestra página web e infraestructura serverless, accesible en https://pacbarinas.sites y su espejo pac-barinas.vercel.app.")
    Text("• Google AdMob: Red publicitaria utilizada exclusivamente para financiar los costos operativos (servidores y dominios).")
    Spacer(modifier = Modifier.height(16.dp))

    Text("Lista Exacta de Datos Recopilados del Usuario", fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(8.dp))
    Text("El sistema PAC Barinas NO recopila nombres, correos, contraseñas, números telefónicos ni ubicaciones GPS exactas. Los únicos datos procesados son:")
    Spacer(modifier = Modifier.height(8.dp))
    Text("1. Preferencias Locales (Almacenado únicamente en el dispositivo):")
    Text("   - Sector o bloque eléctrico preferido.")
    Text("   - Configuración de alarmas y notificaciones (volumen, tipo de sonido).")
    Text("   - Copia caché de la matriz PAC (para funcionamiento sin conexión).")
    Spacer(modifier = Modifier.height(8.dp))
    Text("2. Telemetría y Reportes (Enviados de forma anónima al servidor):")
    Text("   - Estado eléctrico reportado ('NORMAL', 'OUTAGE', 'IRREGULAR').")
    Text("   - Sector/Circuito asociado al reporte.")
    Text("   - Timestamp (fecha y hora del reporte).")
    Spacer(modifier = Modifier.height(8.dp))
    Text("3. Datos Publicitarios:")
    Text("   - Identificadores de publicidad estándar recopilados por Google AdMob según sus propias políticas de privacidad aplicables, necesarios para el funcionamiento de los anuncios.")
}

@Composable
fun DeveloperInfoContent() {
    Text("Datos del Negocio / Desarrollador", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    Spacer(modifier = Modifier.height(16.dp))
    
    Text("Nombre del Proyecto:", fontWeight = FontWeight.Bold)
    Text("PAC Barinas")
    Spacer(modifier = Modifier.height(8.dp))
    
    Text("Tipo de Proyecto:", fontWeight = FontWeight.Bold)
    Text("Proyecto comunitario independiente de código abierto")
    Spacer(modifier = Modifier.height(8.dp))
    
    Text("Ubicación:", fontWeight = FontWeight.Bold)
    Text("Barinas, Estado Barinas, Venezuela")
    Spacer(modifier = Modifier.height(8.dp))
    
    Text("Portal Web Oficial:", fontWeight = FontWeight.Bold)
    Text("https://pacbarinas.sites (Espejo Vercel: https://pac-barinas.vercel.app)")
    Spacer(modifier = Modifier.height(8.dp))
    
    Text("Repositorio de Código:", fontWeight = FontWeight.Bold)
    Text("https://github.com/Ralag/luz-barinas-android")
    Spacer(modifier = Modifier.height(8.dp))
    
    Text("Contacto:", fontWeight = FontWeight.Bold)
    Text("Correo: jorluis255@gmail.com\nTeléfono: +58 412 264 4894")
    Spacer(modifier = Modifier.height(8.dp))
    
    Text("Nota Legal:", fontWeight = FontWeight.Bold)
    Text("PAC Barinas no es una entidad comercial registrada, gubernamental ni corporativa. Es un esfuerzo civil desarrollado por ingenieros y ciudadanos para la comunidad.")
}

@Composable
fun RefundPolicyContent() {
    Text("Política de Reembolsos", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    Spacer(modifier = Modifier.height(16.dp))
    
    Text("Costo de la Aplicación", fontWeight = FontWeight.Bold)
    Text("Actualmente la aplicación PAC Barinas y el acceso al portal web son 100% gratuitos para todos los usuarios. No se exigen pagos obligatorios ni suscripciones para utilizar las funciones básicas de telemetría y cronograma.")
    Spacer(modifier = Modifier.height(12.dp))
    
    Text("Naturaleza de las Donaciones", fontWeight = FontWeight.Bold)
    Text("Las donaciones realizadas al proyecto a través de cualquier plataforma o mecanismo proporcionado (ej: Pago Móvil, Binance, etc.) son contribuciones estrictamente voluntarias y unilaterales para apoyar el mantenimiento de los servidores en la nube y el desarrollo del proyecto.")
    Spacer(modifier = Modifier.height(12.dp))
    
    Text("Política de No Reembolso", fontWeight = FontWeight.Bold)
    Text("Por su naturaleza de aporte voluntario y de sustento operativo, todas las donaciones y aportes económicos realizados son definitivos y no reembolsables bajo ninguna circunstancia. Realizar una donación no otorga privilegios especiales, funciones premium, trato preferencial ni acceso a capacidades ocultas de la plataforma.")
    Spacer(modifier = Modifier.height(12.dp))
    
    Text("Contacto", fontWeight = FontWeight.Bold)
    Text("Para cualquier duda o aclaratoria relacionada con el financiamiento colaborativo del proyecto, por favor contáctenos a:\nCorreo: jorluis255@gmail.com\nTeléfono: +58 412 264 4894")
}

@Composable
fun SystemDocumentationContent() {
    Text("Documentación del Sistema PAC Barinas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    Spacer(modifier = Modifier.height(8.dp))
    Text("Versión 2.0 • Septiembre 2026", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(modifier = Modifier.height(16.dp))

    Text("1. Visión y Propósito", fontWeight = FontWeight.Bold)
    Text("PAC Barinas es una plataforma integral de telemetría ciudadana para el monitoreo en tiempo real del Plan de Administración de Carga (PAC) y el estado del servicio eléctrico en los 12 municipios del Estado Barinas, Venezuela.")
    Spacer(modifier = Modifier.height(12.dp))

    Text("2. Arquitectura Global", fontWeight = FontWeight.Bold)
    Text("• App Android: Interfaz nativa moderna construida 100% con Jetpack Compose y arquitectura MVVM (StateFlow, Coroutines).\n• Backend Cloud: Supabase (PostgreSQL 15 + WebSockets Realtime) para sincronización bidireccional instantánea sin polling.\n• APIs Serverless: Funciones serverless en Vercel para modelos de predicción y cálculo de rotaciones mensuales.\n• Portal Web Oficial: Plataforma web pública y administrativa alojada en Vercel bajo el dominio https://pacbarinas.sites.")
    Spacer(modifier = Modifier.height(12.dp))

    Text("3. Notificaciones y Alarmas", fontWeight = FontWeight.Bold)
    Text("• Notificaciones Push Estándar (Default): Avisos predictivos con sonidos cortos y exclusivos para corte y retorno de luz.\n• Modo Sirena Continua: Opción configurable para usuarios que requieran una alarma audible continua hasta apagarla manualmente.\n• Enrutamiento por Bloque: Suscripción granular a tópicos según el bloque rotativo asignado (A, B, C o D).")
    Spacer(modifier = Modifier.height(12.dp))

    Text("4. Persistencia y Modo Offline (Offline-First)", fontWeight = FontWeight.Bold)
    Text("• Base de datos local SQLite con Room (Versión 3) para garantizar acceso al cronograma aún sin conexión de datos.\n• SharedPreferences para parámetros de configuración del usuario y ubicación guardada.")
    Spacer(modifier = Modifier.height(12.dp))

    Text("5. Repositorio y Documentación Completa", fontWeight = FontWeight.Bold)
    Text("El documento maestro 'DOCUMENTACION_SISTEMA.md' con más de 1.400 líneas de detalles técnicos, esquemas DDL y flujos de datos se encuentra disponible en la raíz del repositorio oficial en GitHub:\nhttps://github.com/Ralag/luz-barinas-android")
}

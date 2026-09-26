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
    Text("Última actualización: Septiembre 2026", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(modifier = Modifier.height(16.dp))
    
    Text("Nombre de la App", fontWeight = FontWeight.Bold)
    Text("PAC Barinas")
    Spacer(modifier = Modifier.height(8.dp))
    
    Text("Desarrollador", fontWeight = FontWeight.Bold)
    Text("Proyecto comunitario independiente (no es una entidad gubernamental).")
    Spacer(modifier = Modifier.height(8.dp))
    
    Text("Datos Recopilados", fontWeight = FontWeight.Bold)
    Text("• Sector/ubicación seleccionada: Almacenada localmente en tu dispositivo.\n• Reportes ciudadanos de estado eléctrico: Enviados a nuestros servidores (Supabase) de forma totalmente anónima. No se recopila nombre, email ni datos personales identificables.\n• Preferencias de alarma: Almacenadas localmente en tu dispositivo.")
    Spacer(modifier = Modifier.height(8.dp))
    
    Text("Servicios de Terceros", fontWeight = FontWeight.Bold)
    Text("• Google AdMob: Se utiliza para mostrar anuncios y puede recopilar identificadores publicitarios según sus propias políticas de privacidad.\n• Supabase: Plataforma en la nube utilizada para la sincronización en tiempo real, recepción de reportes ciudadanos anónimos y distribución de comunicados oficiales.\n• GitHub: Utilizado para la verificación de actualizaciones OTA (Over-The-Air) de la aplicación.")
    Spacer(modifier = Modifier.height(8.dp))
    
    Text("Cuentas de Usuario", fontWeight = FontWeight.Bold)
    Text("La aplicación no requiere ningún tipo de registro ni inicio de sesión.")
    Spacer(modifier = Modifier.height(8.dp))
    
    Text("Retención de Datos", fontWeight = FontWeight.Bold)
    Text("Los reportes ciudadanos sobre el estado del servicio eléctrico son completamente anónimos y se almacenan en el servidor de forma indefinida para realizar análisis estadísticos del servicio eléctrico en la región.")
    Spacer(modifier = Modifier.height(8.dp))
    
    Text("Derechos del Usuario", fontWeight = FontWeight.Bold)
    Text("El usuario tiene el control total sobre los datos almacenados localmente y puede eliminarlos en cualquier momento desinstalando la aplicación o borrando los datos de la misma desde la configuración del sistema operativo.")
    Spacer(modifier = Modifier.height(8.dp))
    
    Text("Contacto", fontWeight = FontWeight.Bold)
    Text("Para cualquier duda o consulta relacionada con esta política, puede contactarnos en: jorluis255@gmail.com (Teléfono: 04122644894)")
}

@Composable
fun TermsAndConditionsContent() {
    Text("Términos y Condiciones", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    Spacer(modifier = Modifier.height(16.dp))
    
    Text("1. Naturaleza del Servicio", fontWeight = FontWeight.Bold)
    Text("PAC Barinas es una herramienta informativa de origen comunitario y NO representa ni es un servicio oficial de CORPOELEC ni de ninguna entidad gubernamental o del Estado.")
    Spacer(modifier = Modifier.height(8.dp))
    
    Text("2. Precisión de la Información", fontWeight = FontWeight.Bold)
    Text("Los horarios de corte mostrados en la aplicación son estimaciones basadas en el cronograma PAC (Plan de Administración de Carga) publicado oficialmente. Estos horarios pueden no reflejar la realidad exacta en tiempo real, ya que el servicio eléctrico está sujeto a cambios no programados.\n\nLa aplicación NO garantiza la precisión del estado eléctrico mostrado en vivo; la información en tiempo real se basa en los reportes ciudadanos voluntarios y en el cronograma publicado.")
    Spacer(modifier = Modifier.height(8.dp))
    
    Text("3. Reportes Ciudadanos", fontWeight = FontWeight.Bold)
    Text("Los reportes de estado eléctrico emitidos por los usuarios son contribuciones voluntarias y completamente anónimas. El usuario se compromete a emitir reportes veraces para mantener la utilidad comunitaria de la herramienta.")
    Spacer(modifier = Modifier.height(8.dp))
    
    Text("4. Limitación de Responsabilidad", fontWeight = FontWeight.Bold)
    Text("El servicio se ofrece tal cual ('as-is'), sin ningún tipo de garantías sobre su disponibilidad continua, precisión o confiabilidad.\n\nEl desarrollador no se hace responsable bajo ninguna circunstancia por decisiones tomadas o daños sufridos basándose en la información provista por la aplicación.")
    Spacer(modifier = Modifier.height(8.dp))
    
    Text("5. Publicidad y Financiación", fontWeight = FontWeight.Bold)
    Text("La aplicación contiene anuncios publicitarios (a través de AdMob) con el fin de financiar los costos de operación y mantenimiento de los servidores.\n\nLas donaciones realizadas al proyecto son completamente voluntarias y no otorgan ningún tipo de beneficio adicional, privilegios o funciones especiales dentro de la aplicación.")
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
    
    Text("Repositorio de Código:", fontWeight = FontWeight.Bold)
    Text("https://github.com/Ralag/luz-barinas-android")
    Spacer(modifier = Modifier.height(8.dp))
    
    Text("Contacto:", fontWeight = FontWeight.Bold)
    Text("Correo: jorluis255@gmail.com\nTeléfono: 04122644894")
    Spacer(modifier = Modifier.height(8.dp))
    
    Text("Nota Legal:", fontWeight = FontWeight.Bold)
    Text("No es una entidad comercial registrada.")
}

@Composable
fun RefundPolicyContent() {
    Text("Política de Reembolsos (Borrador)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    Spacer(modifier = Modifier.height(16.dp))
    
    Text("Costo de la Aplicación", fontWeight = FontWeight.Bold)
    Text("Actualmente la aplicación PAC Barinas es 100% gratuita para todos los usuarios y no ofrece compras dentro de la app (In-App Purchases).")
    Spacer(modifier = Modifier.height(8.dp))
    
    Text("Donaciones", fontWeight = FontWeight.Bold)
    Text("Las donaciones realizadas al proyecto son contribuciones estrictamente voluntarias para apoyar el mantenimiento de los servidores. Por su naturaleza, las donaciones son no reembolsables.")
    Spacer(modifier = Modifier.height(8.dp))
    
    Text("Funciones Futuras", fontWeight = FontWeight.Bold)
    Text("Si en el futuro se llegan a implementar funciones premium o de pago, se aplicará de manera estricta la política de reembolsos estándar de Google Play Store, la cual generalmente ofrece un plazo de 48 horas para solicitar un reembolso por una compra directa.")
    Spacer(modifier = Modifier.height(8.dp))
    
    Text("Contacto", fontWeight = FontWeight.Bold)
    Text("Para cualquier consulta relacionada con donaciones, por favor contáctenos a: jorluis255@gmail.com (Teléfono: 04122644894)")
}

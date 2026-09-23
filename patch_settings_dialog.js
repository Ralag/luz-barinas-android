const fs = require('fs');
let kt = fs.readFileSync('app/src/main/java/com/example/ui/components/SystemSettingsDialog.kt', 'utf8');

kt = kt.replace('onCheckUpdate: () -> Unit', 'onCheckUpdate: () -> Unit,\n    donationUrl: String? = null');

kt = kt.replace('import androidx.compose.ui.window.Dialog', 'import androidx.compose.ui.window.Dialog\nimport android.content.Intent\nimport android.net.Uri\nimport androidx.compose.ui.platform.LocalContext\nimport androidx.compose.material.icons.outlined.FavoriteBorder');

const btn = `                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                // Donation Button
                if (!donationUrl.isNullOrEmpty()) {
                    val context = LocalContext.current
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onDismiss()
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(donationUrl))
                                    context.startActivity(intent)
                                } catch (e: Exception) {}
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.FavoriteBorder,
                            contentDescription = null,
                            tint = androidx.compose.ui.graphics.Color(0xFFE91E63),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Donar al Proyecto", fontWeight = FontWeight.SemiBold)
                            Text("Apóyanos para mantener los servidores", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))`;
kt = kt.replace('HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))\n\n                // System Info', btn + '\n\n                // System Info');

fs.writeFileSync('app/src/main/java/com/example/ui/components/SystemSettingsDialog.kt', kt);

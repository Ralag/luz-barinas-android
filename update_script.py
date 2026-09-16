import re

path = 'e:/LUZ BARINAS/app/src/main/java/com/example/ui/components/PacScheduleView.kt'
with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

start_marker = '        // =========================================================================\n        // TAB 0: VISTA SEMANAL (Matriz de la semana + Detalle de Hoy)\n        // =========================================================================\n        if (mainTab == 0) {'
end_marker = '            item {\n                PacBlockSectorsGrid(onSectorClicked = onSectorClicked)\n            }\n        }\n    }\n}'

if 'import androidx.compose.animation.core.tween' not in content:
    content = content.replace('import androidx.compose.animation.AnimatedVisibility\n', 'import androidx.compose.animation.AnimatedVisibility\nimport androidx.compose.animation.AnimatedContent\nimport androidx.compose.animation.core.tween\nimport androidx.compose.animation.togetherWith\n')

replacement = """        // =========================================================================
        // TAB CONTENT
        // =========================================================================
        item {
            AnimatedContent(
                targetState = mainTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                },
                label = "schedule_tab_transition"
            ) { tab ->
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    when (tab) {
                        0 -> {
                            PacOfficialMatrixCard(
                                title = "SEMANA EN CURSO • ROTACIÓN PAC",
                                matrix = PacScheduleData.MATRIX,
                                highlightToday = true,
                                todayIdx = todayIdx,
                                currentSlotIdx = currentSlotIdx,
                                userBlock = userBlock,
                                selectedSectorName = selectedSector?.name
                            )

                            Column {
                                Text(
                                    text = "Detalle diario para ${selectedSector?.name ?: "Barinas"}:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    itemsIndexed(PacScheduleData.DAYS, key = { _, day -> day.name }) { index, day ->
                                        val isSelected = selectedDayIdx == index
                                        val isToday = index == todayIdx

                                        Surface(
                                            shape = RoundedCornerShape(14.dp),
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                            border = androidx.compose.foundation.BorderStroke(
                                                1.dp,
                                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                            ),
                                            modifier = Modifier.clickable { selectedDayIdx = index }
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(
                                                    text = day.shortName,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                                )
                                                if (isToday) {
                                                    Text(
                                                        text = "Hoy",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color = if (isSelected) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            val selectedDay = PacScheduleData.DAYS[selectedDayIdx]
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Turnos para el ${selectedDay.name}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = getBlockColor(userBlock).copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "Tu Bloque: $userBlock",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = getBlockColor(userBlock),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        PacScheduleData.SLOTS.forEachIndexed { slotIdx, slot ->
                                            val block = PacScheduleData.MATRIX[slotIdx][selectedDayIdx]
                                            val isCutForUser = block.equals(userBlock, ignoreCase = true)
                                            val isNow = selectedDayIdx == todayIdx && slotIdx == currentSlotIdx
                                            val blockColor = getBlockColor(block)

                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = when {
                                                    isNow -> blockColor.copy(alpha = 0.12f)
                                                    isCutForUser -> MaterialTheme.colorScheme.surfaceVariant
                                                    else -> MaterialTheme.colorScheme.surface
                                                },
                                                border = androidx.compose.foundation.BorderStroke(
                                                    1.dp,
                                                    when {
                                                        isNow -> blockColor
                                                        isCutForUser -> blockColor.copy(alpha = 0.5f)
                                                        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                                                    }
                                                ),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(32.dp)
                                                                .background(blockColor, RoundedCornerShape(8.dp)),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(
                                                                text = block,
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 14.sp,
                                                                color = Color.White
                                                            )
                                                        }
                                                        Spacer(modifier = Modifier.width(12.dp))
                                                        Column {
                                                            Text(
                                                                text = slot.timeLabel,
                                                                fontWeight = FontWeight.SemiBold,
                                                                fontSize = 13.sp,
                                                                color = MaterialTheme.colorScheme.onSurface
                                                            )
                                                            Text(
                                                                text = if (isCutForUser) "🔴 Corte PAC asignado a tu bloque" else "🟢 Servicio eléctrico con luz garantizada",
                                                                fontSize = 11.sp,
                                                                color = if (isCutForUser) StatusScheduledRed else StatusNormalGreen
                                                            )
                                                        }
                                                    }

                                                    if (isNow) {
                                                        Surface(
                                                            shape = RoundedCornerShape(100.dp),
                                                            color = StatusScheduledRed.copy(alpha = 0.15f)
                                                        ) {
                                                            Text(
                                                                text = "EN CURSO",
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = StatusScheduledRed,
                                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        1 -> {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Outlined.Info,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Cronograma mensual oficial organizado por semanas y bloques para $selectedMonthName $currentYear. Las rotaciones avanzan de forma cíclica durante el mes.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = "Selecciona el Mes:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    itemsIndexed(PacScheduleData.MONTH_NAMES, key = { _, monthName -> monthName }) { index, monthName ->
                                        val isSelected = selectedMonthIdx == index
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { selectedMonthIdx = index },
                                            label = {
                                                Text(
                                                    text = monthName,
                                                    fontSize = 12.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                )
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                selectedLabelColor = Color.White
                                            ),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                    }
                                }
                            }

                            monthlyWeeks.forEach { week ->
                                val isCurrentWeek = (selectedMonthIdx == currentMonthIdx && week.weekNumber == currentWeekNumber)
                                PacOfficialMatrixCard(
                                    title = week.dateRangeLabel,
                                    matrix = week.matrix,
                                    highlightToday = isCurrentWeek,
                                    todayIdx = if (isCurrentWeek) todayIdx else -1,
                                    currentSlotIdx = if (isCurrentWeek) currentSlotIdx else -1,
                                    userBlock = userBlock,
                                    selectedSectorName = selectedSector?.name
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "— ASIGNACIÓN DE CIRCUITOS POR BLOQUE —",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            PacBlockSectorsGrid(onSectorClicked = onSectorClicked)
                        }
                        2 -> {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Outlined.LocationOn,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Circuitos y Sectores del Estado Barinas",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Consulta a qué bloque de corte pertenece tu urbanización, barrio o circuito eléctrico. Toca cualquier sector para seleccionarlo.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            PacBlockSectorsGrid(onSectorClicked = onSectorClicked)
                        }
                    }
                }
            }
        }
    }
}"""

idx1 = content.find(start_marker)
idx2 = content.find(end_marker)

if idx1 != -1 and idx2 != -1:
    new_content = content[:idx1] + replacement + content[idx2 + len(end_marker):]
    with open(path, 'w', encoding='utf-8') as f:
        f.write(new_content)
    print('Success updating PacScheduleView.kt')
else:
    print('Could not find markers.')

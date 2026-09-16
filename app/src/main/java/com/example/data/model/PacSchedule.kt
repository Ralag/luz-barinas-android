package com.example.data.model

import java.util.Calendar
import java.util.TimeZone
import androidx.compose.runtime.Immutable

data class DayApproximation(
    val dayOfMonth: Int,
    val dayOfWeekName: String,
    val shortDayName: String,
    val cutSlots: List<PacSlot>,
    val totalHours: Int
)

data class MonthApproximation(
    val monthIndex: Int, // 0..11
    val monthName: String,
    val year: Int,
    val totalDays: Int,
    val blockCode: String,
    val totalCutsCount: Int,
    val totalOutageHours: Int,
    val avgCutsPerDay: Float,
    val days: List<DayApproximation>
)

@Immutable
data class PacSlot(
    val index: Int,
    val timeLabel: String,
    val startHour: Int,
    val endHour: Int
)

data class PacDay(
    val name: String,
    val shortName: String,
    val calendarDay: Int // Calendar.MONDAY, etc.
)

data class PacCell(
    val dayIndex: Int,
    val slotIndex: Int,
    val block: String, // "A", "B", "C", "D"
    val isCurrentSlot: Boolean = false
)

data class DayTurnAudit(
    val dayIdx: Int,
    val dayName: String,
    val cutsPerBlock: Map<String, Int>,
    val totalCutsCount: Int,
    val detectedScheme: String // "2 Cortes Diarios (8 hrs)" or "1 Solo Corte Diario (4 hrs)"
)

data class RebalanceResult(
    val slotIdx: Int,
    val dayIdx: Int,
    val dayName: String,
    val slotLabel: String,
    val removedBlock: String,
    val newlyAssignedBlock: String,
    val explanation: String,
    val countsSummary: Map<String, Int>
)

data class PacScheduleWindow(
    val dayName: String,
    val timeLabel: String,
    val block: String,
    val startMillis: Long,
    val endMillis: Long,
    val isCurrentlyActive: Boolean,
    val hoursUntil: Float
)

data class PacWeekPlan(
    val weekNumber: Int,
    val dateRangeLabel: String,
    val matrix: Array<Array<String>>
)

object PacScheduleData {

    private val DEFAULT_SLOTS = listOf(
        PacSlot(0, "03:00 a 07:00", 3, 7),
        PacSlot(1, "07:00 a 11:00", 7, 11),
        PacSlot(2, "11:00 a 15:00", 11, 15),
        PacSlot(3, "15:00 a 19:00", 15, 19),
        PacSlot(4, "19:00 a 23:00", 19, 23),
        PacSlot(5, "23:00 a 03:00", 23, 3)
    )

    val activeSlots: MutableList<PacSlot> = DEFAULT_SLOTS.map { it.copy() }.toMutableList()

    val SLOTS: List<PacSlot>
        get() = activeSlots

    fun updateSlot(slotIdx: Int, newTimeLabel: String, startHour: Int, endHour: Int) {
        if (slotIdx in activeSlots.indices) {
            activeSlots[slotIdx] = PacSlot(slotIdx, newTimeLabel.trim(), startHour, endHour)
            scheduleVersion = System.currentTimeMillis()
        }
    }

    val DAYS = listOf(
        PacDay("Lunes", "LUN", Calendar.MONDAY),
        PacDay("Martes", "MAR", Calendar.TUESDAY),
        PacDay("Miércoles", "MIÉ", Calendar.WEDNESDAY),
        PacDay("Jueves", "JUE", Calendar.THURSDAY),
        PacDay("Viernes", "VIE", Calendar.FRIDAY),
        PacDay("Sábado", "SÁB", Calendar.SATURDAY),
        PacDay("Domingo", "DOM", Calendar.SUNDAY)
    )

    // PAC Matrix from Corpoelec official schedule (2 cuts per day scheme)
    // Rows = Slots (0..5), Cols = Days (0=Lun, 1=Mar, 2=Mié, 3=Jue, 4=Vie, 5=Sáb, 6=Dom)
    private val DEFAULT_MATRIX = arrayOf(
        arrayOf("C", "B", "A", "C", "B", "A", "C"), // 03:00 a 07:00
        arrayOf("A", "C", "B", "A", "C", "B", "A"), // 07:00 a 11:00
        arrayOf("B", "A", "C", "B", "A", "C", "B"), // 11:00 a 15:00
        arrayOf("C", "B", "A", "C", "B", "A", "C"), // 15:00 a 19:00
        arrayOf("A", "C", "B", "A", "C", "B", "A"), // 19:00 a 23:00
        arrayOf("B", "A", "C", "B", "A", "C", "B")  // 23:00 a 03:00
    )

    var activeMatrix: Array<Array<String>> = Array(6) { r ->
        Array(7) { c -> DEFAULT_MATRIX[r][c] }
    }

    var scheduleVersion: Long = System.currentTimeMillis()

    val MATRIX: Array<Array<String>>
        get() = activeMatrix

    fun getMatrixSnapshot(): List<List<String>> {
        return activeMatrix.map { row -> row.toList() }
    }

    fun getSlotsSnapshot(): List<PacSlot> {
        return activeSlots.map { it.copy() }
    }

    fun updateMatrix(newMatrix: Array<Array<String>>) {
        for (r in 0 until minOf(6, newMatrix.size)) {
            for (c in 0 until minOf(7, newMatrix[r].size)) {
                activeMatrix[r][c] = newMatrix[r][c].uppercase().trim()
            }
        }
        scheduleVersion = System.currentTimeMillis()
    }

    fun updateCell(slotIdx: Int, dayIdx: Int, block: String) {
        if (slotIdx in activeMatrix.indices && dayIdx in 0..6) {
            val clean = block.uppercase().trim()
            activeMatrix[slotIdx][dayIdx] = if (clean.isEmpty() || clean == "LIBRE" || clean == "SIN CORTE" || clean == "SIN_CORTE") "-" else clean
            scheduleVersion = System.currentTimeMillis()
        }
    }

    fun deleteSlot(slotIdx: Int): Boolean {
        if (activeSlots.size <= 1 || slotIdx !in activeSlots.indices) return false
        activeSlots.removeAt(slotIdx)
        for (i in activeSlots.indices) {
            activeSlots[i] = activeSlots[i].copy(index = i)
        }
        if (slotIdx < activeMatrix.size) {
            val newMatrix = activeMatrix.toMutableList()
            newMatrix.removeAt(slotIdx)
            activeMatrix = newMatrix.toTypedArray()
        }
        scheduleVersion = System.currentTimeMillis()
        return true
    }

    fun addSlot(label: String, startHour: Int, endHour: Int): PacSlot {
        val newIdx = activeSlots.size
        val newSlot = PacSlot(index = newIdx, timeLabel = label.ifBlank { "Turno ${newIdx + 1}" }, startHour = startHour, endHour = endHour)
        activeSlots.add(newSlot)
        val newRow = Array(7) { "-" }
        activeMatrix = (activeMatrix.toList() + listOf(newRow)).toTypedArray()
        scheduleVersion = System.currentTimeMillis()
        return newSlot
    }

    /**
     * Revisa cuántas veces se va la luz por bloque en un día específico (1 sola vez o 2 veces).
     */
    fun getDayTurnAudit(dayIdx: Int): DayTurnAudit {
        val counts = mutableMapOf("A" to 0, "B" to 0, "C" to 0, "D" to 0)
        var total = 0
        for (s in activeMatrix.indices) {
            val b = activeMatrix[s].getOrElse(dayIdx) { "-" }.uppercase().trim()
            if (counts.containsKey(b)) {
                counts[b] = (counts[b] ?: 0) + 1
                total++
            }
        }
        val maxCount = counts.values.maxOrNull() ?: 0
        val detected = if (maxCount >= 2) "2 Cortes Diarios (8 hrs)" else "1 Solo Corte Diario (4 hrs)"
        return DayTurnAudit(
            dayIdx = dayIdx,
            dayName = DAYS.getOrElse(dayIdx) { DAYS[0] }.name,
            cutsPerBlock = counts,
            totalCutsCount = total,
            detectedScheme = detected
        )
    }

    /**
     * Borra un turno y reasigna inteligentemente el mismo revisando si los bloques
     * se van 2 veces o 1 sola vez en el día, garantizando balance y equidad en el PAC.
     */
    fun deleteTurnAndRebalance(slotIdx: Int, dayIdx: Int): RebalanceResult {
        val currentBlock = activeMatrix.getOrNull(slotIdx)?.getOrNull(dayIdx)?.uppercase()?.trim() ?: "A"
        val dayObj = DAYS.getOrElse(dayIdx) { DAYS[0] }
        val slotObj = activeSlots.getOrElse(slotIdx) { SLOTS[0] }

        // Contar cortes en los otros turnos de ese día
        val remainingCounts = mutableMapOf("A" to 0, "B" to 0, "C" to 0, "D" to 0)
        for (s in activeMatrix.indices) {
            if (s != slotIdx) {
                val b = activeMatrix[s].getOrElse(dayIdx) { "-" }.uppercase().trim()
                if (remainingCounts.containsKey(b)) {
                    remainingCounts[b] = (remainingCounts[b] ?: 0) + 1
                }
            }
        }

        val maxRemaining = remainingCounts.values.maxOrNull() ?: 0
        val isTwoCutScheme = maxRemaining >= 2 || remainingCounts.values.count { it >= 1 } >= 3

        val targetCandidates = if (isTwoCutScheme) {
            val needed = remainingCounts.filter { it.value < 2 }.keys.toList()
            if (needed.isNotEmpty()) needed else listOf("A", "B", "C", "D")
        } else {
            val needed = remainingCounts.filter { it.value == 0 }.keys.toList()
            if (needed.isNotEmpty()) needed else listOf("A", "B", "C", "D")
        }

        val prevBlock = if (slotIdx > 0) activeMatrix[slotIdx - 1].getOrElse(dayIdx) { null } else null
        val nextBlock = if (slotIdx < activeMatrix.size - 1) activeMatrix[slotIdx + 1].getOrElse(dayIdx) { null } else null

        val nonAdjacent = targetCandidates.filter { it != prevBlock && it != nextBlock }
        val pool = if (nonAdjacent.isNotEmpty()) nonAdjacent else targetCandidates

        val sortedCandidates = pool.sortedWith(
            compareBy(
                { remainingCounts[it] ?: 0 },
                { if (it == currentBlock) 1 else 0 }
            )
        )

        val newBlock = sortedCandidates.firstOrNull() ?: if (currentBlock == "A") "B" else "A"

        if (slotIdx in activeMatrix.indices && dayIdx in 0..6) {
            activeMatrix[slotIdx][dayIdx] = newBlock
        }
        scheduleVersion = System.currentTimeMillis()

        remainingCounts[newBlock] = (remainingCounts[newBlock] ?: 0) + 1
        val schemeName = if (isTwoCutScheme) "esquema de 2 cortes" else "esquema de 1 corte"
        val explanation = "Turno ${slotObj.timeLabel} borrado para Bloque $currentBlock. Reasignado al Bloque $newBlock según $schemeName (queda con ${remainingCounts[newBlock]} corte(s) hoy)."

        return RebalanceResult(
            slotIdx = slotIdx,
            dayIdx = dayIdx,
            dayName = dayObj.name,
            slotLabel = slotObj.timeLabel,
            removedBlock = currentBlock,
            newlyAssignedBlock = newBlock,
            explanation = explanation,
            countsSummary = remainingCounts
        )
    }

    /**
     * Aplica el esquema de 1 solo corte al día (4 horas por bloque) a toda la semana.
     */
    fun applySingleTurnPreset() {
        // En esquema de 1 solo corte (4 hrs), cada bloque A, B, C, D tiene exactamente 1 turno diario rotativo (4 turnos con corte, 2 libres)
        val singleMatrix = arrayOf(
            arrayOf("A", "D", "C", "B", "A", "D", "C"), // Turno 1
            arrayOf("B", "A", "D", "C", "B", "A", "D"), // Turno 2
            arrayOf("C", "B", "A", "D", "C", "B", "A"), // Turno 3
            arrayOf("D", "C", "B", "A", "D", "C", "B"), // Turno 4
            arrayOf("-", "-", "-", "-", "-", "-", "-"), // Turno 5 (Libre)
            arrayOf("-", "-", "-", "-", "-", "-", "-")  // Turno 6 (Libre)
        )
        for (r in activeMatrix.indices) {
            for (c in 0..6) {
                activeMatrix[r][c] = if (r < singleMatrix.size) singleMatrix[r][c] else "-"
            }
        }
        scheduleVersion = System.currentTimeMillis()
    }

    fun applyDoubleTurnPreset() {
        activeSlots.clear()
        for (slot in DEFAULT_SLOTS) {
            activeSlots.add(slot.copy())
        }
        activeMatrix = Array(6) { r ->
            Array(7) { c -> DEFAULT_MATRIX[r][c] }
        }
        scheduleVersion = System.currentTimeMillis()
    }

    fun resetToDefault() {
        applyDoubleTurnPreset()
    }

    val SECTORS_BLOQUE_A = mutableListOf(
        "Alto Barinas I 34,5 kV",
        "Obispos 34,5 kV",
        "Guasimito",
        "Centro",
        "Sur",
        "Industrial",
        "Norte",
        "Raúl Leoni",
        "Las Palmas",
        "Primero Diciembre",
        "Pagueycito",
        "Progreso",
        "Don Simón",
        "El Real",
        "El Tambor",
        "Cdad Bolivia II",
        "Mijagua 34,5 kV",
        "Mirí"
    )

    val SECTORS_BLOQUE_B = mutableListOf(
        "Barinitas 34,5 kV",
        "Parangula",
        "Floresta",
        "Centro Norte",
        "Los Pinos",
        "Carolina",
        "Borburata",
        "Negro Primero",
        "Hormiga",
        "Cdad Varyna",
        "Cdad Tavacare",
        "San Silvestre",
        "Sta Ines Lucia",
        "Libertad",
        "Sta. Rosa",
        "Curbati",
        "Ticoporo",
        "Capitanejo 34,5 kV",
        "Santa Elena kV"
    )

    val SECTORS_BLOQUE_C = mutableListOf(
        "Expresa 34,5 kV",
        "Alto Barinas II 34,5 kV",
        "Socopo I",
        "Bum Bum",
        "Socopo II",
        "Cardenera",
        "Corocito",
        "Bolivar",
        "Esperanza",
        "Torunos",
        "Cdad Nutria 34,5 kV",
        "Dolores",
        "El Paguey",
        "Cdad Bolivia I",
        "Fundacea",
        "Estadio"
    )

    val SECTORS_BLOQUE_D = mutableListOf(
        "Alto Barinas II 34,5 kV",
        "Obispos 34,5 kV",
        "Floresta",
        "Los Pinos",
        "Industrial",
        "Cardenera",
        "Corocito",
        "El Paguey",
        "Cdad Bolivia I",
        "Mijagua 34,5 kV",
        "Caaez 34,5 kV",
        "Boconoito",
        "Otopum-Pajen",
        "Canagua 34,5 kV",
        "Libertad"
    )

    fun addSectorToBlock(sectorName: String, blockCode: String) {
        val cleanName = sectorName.trim()
        if (cleanName.isEmpty()) return
        SECTORS_BLOQUE_A.remove(cleanName)
        SECTORS_BLOQUE_B.remove(cleanName)
        SECTORS_BLOQUE_C.remove(cleanName)
        SECTORS_BLOQUE_D.remove(cleanName)
        when (blockCode.uppercase().trim()) {
            "A" -> SECTORS_BLOQUE_A.add(0, cleanName)
            "B" -> SECTORS_BLOQUE_B.add(0, cleanName)
            "C" -> SECTORS_BLOQUE_C.add(0, cleanName)
            "D" -> SECTORS_BLOQUE_D.add(0, cleanName)
            else -> SECTORS_BLOQUE_A.add(0, cleanName)
        }
        scheduleVersion = System.currentTimeMillis()
    }

    fun removeSectorFromBlock(sectorName: String, blockCode: String) {
        when (blockCode.uppercase().trim()) {
            "A" -> SECTORS_BLOQUE_A.remove(sectorName)
            "B" -> SECTORS_BLOQUE_B.remove(sectorName)
            "C" -> SECTORS_BLOQUE_C.remove(sectorName)
            "D" -> SECTORS_BLOQUE_D.remove(sectorName)
        }
        scheduleVersion = System.currentTimeMillis()
    }

    fun reassignSector(sectorName: String, targetBlock: String) {
        SECTORS_BLOQUE_A.remove(sectorName)
        SECTORS_BLOQUE_B.remove(sectorName)
        SECTORS_BLOQUE_C.remove(sectorName)
        SECTORS_BLOQUE_D.remove(sectorName)
        when (targetBlock.uppercase().trim()) {
            "A" -> SECTORS_BLOQUE_A.add(sectorName)
            "B" -> SECTORS_BLOQUE_B.add(sectorName)
            "C" -> SECTORS_BLOQUE_C.add(sectorName)
            "D" -> SECTORS_BLOQUE_D.add(sectorName)
            else -> SECTORS_BLOQUE_A.add(sectorName)
        }
        scheduleVersion = System.currentTimeMillis()
    }

    val MONTH_NAMES = listOf(
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    )

    fun getMonthApproximation(
        monthIndex: Int,
        year: Int,
        targetBlock: String
    ): MonthApproximation {
        val cleanBlock = targetBlock.uppercase().replace("BLOQUE", "").trim().ifEmpty { "A" }
        val cal = Calendar.getInstance(TimeZone.getTimeZone("America/Caracas")).apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, monthIndex)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val totalDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val monthName = MONTH_NAMES.getOrElse(monthIndex) { "Mes" }

        val daysList = mutableListOf<DayApproximation>()
        var totalCuts = 0
        var totalHours = 0

        for (day in 1..totalDays) {
            val dayCal = Calendar.getInstance(TimeZone.getTimeZone("America/Caracas")).apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, monthIndex)
                set(Calendar.DAY_OF_MONTH, day)
            }
            val calDayOfWeek = dayCal.get(Calendar.DAY_OF_WEEK)
            val dayIdx = getDayIndex(calDayOfWeek)
            val dayObj = DAYS[dayIdx]

            // Rotation offset depending on the week in month:
            val weekOffset = when (day) {
                in 1..7 -> 0
                in 8..14 -> 1
                in 15..21 -> 2
                else -> 3
            }

            val matchingSlots = mutableListOf<PacSlot>()
            for (slotIdx in 0..5) {
                val rotatedSlot = (slotIdx + weekOffset) % 6
                val slotBlock = activeMatrix[rotatedSlot][dayIdx]
                if (slotBlock.equals(cleanBlock, ignoreCase = true)) {
                    matchingSlots.add(SLOTS[slotIdx])
                }
            }

            val dayHours = matchingSlots.size * 4 // each standard slot is 4 hours
            totalCuts += matchingSlots.size
            totalHours += dayHours

            daysList.add(
                DayApproximation(
                    dayOfMonth = day,
                    dayOfWeekName = dayObj.name,
                    shortDayName = dayObj.shortName,
                    cutSlots = matchingSlots,
                    totalHours = dayHours
                )
            )
        }

        val avg = if (totalDays > 0) totalCuts.toFloat() / totalDays else 0f

        return MonthApproximation(
            monthIndex = monthIndex,
            monthName = monthName,
            year = year,
            totalDays = totalDays,
            blockCode = cleanBlock,
            totalCutsCount = totalCuts,
            totalOutageHours = totalHours,
            avgCutsPerDay = avg,
            days = daysList
        )
    }

    /**
     * Generates 4 weeks of the month for the #SOYBARINAS monthly PAC graphic format.
     */
    fun getMonthlyWeeks(monthName: String, totalDays: Int): List<PacWeekPlan> {
        val week1 = PacWeekPlan(
            weekNumber = 1,
            dateRangeLabel = "DEL 01 AL 07 DE ${monthName.uppercase()}",
            matrix = activeMatrix
        )
        // Shift rotation for subsequent weeks (classic Corpoelec cycle)
        val week2Matrix = Array(6) { r ->
            Array(7) { c ->
                activeMatrix[(r + 1) % 6][c]
            }
        }
        val week3Matrix = Array(6) { r ->
            Array(7) { c ->
                activeMatrix[(r + 2) % 6][c]
            }
        }
        val week4Matrix = Array(6) { r ->
            Array(7) { c ->
                activeMatrix[(r + 3) % 6][c]
            }
        }

        return listOf(
            week1,
            PacWeekPlan(2, "DEL 08 AL 14 DE ${monthName.uppercase()}", week2Matrix),
            PacWeekPlan(3, "DEL 15 AL 21 DE ${monthName.uppercase()}", week3Matrix),
            PacWeekPlan(4, "DEL 22 AL $totalDays DE ${monthName.uppercase()}", week4Matrix)
        )
    }


    fun getDayIndex(calendarDayOfWeek: Int): Int {
        return when (calendarDayOfWeek) {
            Calendar.MONDAY -> 0
            Calendar.TUESDAY -> 1
            Calendar.WEDNESDAY -> 2
            Calendar.THURSDAY -> 3
            Calendar.FRIDAY -> 4
            Calendar.SATURDAY -> 5
            Calendar.SUNDAY -> 6
            else -> 0
        }
    }

    fun getWeekOffsetForDayOfMonth(dayOfMonth: Int): Int {
        return when (dayOfMonth) {
            in 1..7 -> 0
            in 8..14 -> 1
            in 15..21 -> 2
            else -> 3
        }
    }

    fun getBlockForDate(slotIdx: Int, dayIdx: Int, dayOfMonth: Int): String {
        val offset = getWeekOffsetForDayOfMonth(dayOfMonth)
        return activeMatrix[(slotIdx + offset) % 6][dayIdx]
    }

    fun getCurrentSlotIndex(hourOfDay: Int): Int {
        return when {
            hourOfDay in 3..6 -> 0
            hourOfDay in 7..10 -> 1
            hourOfDay in 11..14 -> 2
            hourOfDay in 15..18 -> 3
            hourOfDay in 19..22 -> 4
            else -> 5 // 23:00 to 02:59
        }
    }

    fun getActiveBlockNow(currentTimeMillis: Long = System.currentTimeMillis()): String {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("America/Caracas")).apply {
            timeInMillis = currentTimeMillis
        }
        val dayIdx = getDayIndex(cal.get(Calendar.DAY_OF_WEEK))
        val slotIdx = getCurrentSlotIndex(cal.get(Calendar.HOUR_OF_DAY))
        val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
        return getBlockForDate(slotIdx, dayIdx, dayOfMonth)
    }

    fun findNextWindowForBlock(
        targetBlock: String,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): PacScheduleWindow {
        val cleanBlock = targetBlock.uppercase().replace("BLOQUE", "").trim()
        val cal = Calendar.getInstance(TimeZone.getTimeZone("America/Caracas")).apply {
            timeInMillis = currentTimeMillis
        }

        // Look ahead up to 14 days x 6 slots, accounting for actual month, day of month, and rotating weeks!
        for (dayOffset in 0..14) {
            val testCal = Calendar.getInstance(TimeZone.getTimeZone("America/Caracas")).apply {
                timeInMillis = currentTimeMillis
                add(Calendar.DAY_OF_YEAR, dayOffset)
            }
            val dayIdx = getDayIndex(testCal.get(Calendar.DAY_OF_WEEK))
            val dayOfMonth = testCal.get(Calendar.DAY_OF_MONTH)
            val weekOffset = getWeekOffsetForDayOfMonth(dayOfMonth)

            for (slotIdx in 0..5) {
                val slot = SLOTS[slotIdx]
                val rotatedSlot = (slotIdx + weekOffset) % 6
                val blockInSlot = activeMatrix[rotatedSlot][dayIdx]

                if (blockInSlot.equals(cleanBlock, ignoreCase = true)) {
                    val slotCalStart = Calendar.getInstance(TimeZone.getTimeZone("America/Caracas")).apply {
                        timeInMillis = testCal.timeInMillis
                        set(Calendar.HOUR_OF_DAY, slot.startHour)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }

                    val slotCalEnd = Calendar.getInstance(TimeZone.getTimeZone("America/Caracas")).apply {
                        timeInMillis = testCal.timeInMillis
                        if (slot.endHour < slot.startHour) {
                            // Overnight slot 23:00 to 03:00
                            add(Calendar.DAY_OF_YEAR, 1)
                        }
                        set(Calendar.HOUR_OF_DAY, slot.endHour)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }

                    // Check if active right now
                    if (currentTimeMillis in slotCalStart.timeInMillis..slotCalEnd.timeInMillis) {
                        return PacScheduleWindow(
                            dayName = DAYS[dayIdx].name,
                            timeLabel = slot.timeLabel,
                            block = cleanBlock,
                            startMillis = slotCalStart.timeInMillis,
                            endMillis = slotCalEnd.timeInMillis,
                            isCurrentlyActive = true,
                            hoursUntil = 0f
                        )
                    }

                    // If in future
                    if (slotCalStart.timeInMillis > currentTimeMillis) {
                        val hoursUntil = (slotCalStart.timeInMillis - currentTimeMillis) / (3600000f)
                        val dayLabel = when (dayOffset) {
                            0 -> "Hoy"
                            1 -> "Mañana"
                            else -> "${DAYS[dayIdx].name} $dayOfMonth"
                        }
                        return PacScheduleWindow(
                            dayName = dayLabel,
                            timeLabel = slot.timeLabel,
                            block = cleanBlock,
                            startMillis = slotCalStart.timeInMillis,
                            endMillis = slotCalEnd.timeInMillis,
                            isCurrentlyActive = false,
                            hoursUntil = hoursUntil
                        )
                    }
                }
            }
        }

        // Fallback default
        return PacScheduleWindow(
            dayName = "Hoy",
            timeLabel = "15:00 a 19:00",
            block = cleanBlock,
            startMillis = currentTimeMillis + 3600000L,
            endMillis = currentTimeMillis + 5 * 3600000L,
            isCurrentlyActive = false,
            hoursUntil = 1.0f
        )
    }
}

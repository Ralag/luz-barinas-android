package com.example.data.repository

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.data.local.AppDatabase
import com.example.data.local.entity.OutageRecordEntity
import com.example.data.local.entity.PendingReportEntity
import com.example.data.local.entity.SectorEntity
import com.example.data.model.CitizenReport
import com.example.data.model.OutagePrediction
import com.example.data.model.OutageRecord
import com.example.data.model.Sector
import com.example.data.model.ServiceStatus
import com.example.data.remote.ApiClient
import com.example.data.remote.dto.TelemetryReportRequest
import com.example.engine.OutagePredictionEngine
import com.example.worker.ReportPowerWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class EnergyRepository(
    private val context: Context,
    private val database: AppDatabase = AppDatabase.getInstance(context)
) {
    private val sectorDao = database.sectorDao()
    private val outageDao = database.outageRecordDao()
    private val pendingDao = database.pendingReportDao()

    val cloudSync: CloudSyncRepository by lazy { CloudSyncRepository(context, database) }

    val allSectorsFlow: Flow<List<Sector>> = sectorDao.getAllSectors().map { list ->
        list.map { it.toDomain() }
    }

    val unsyncedCountFlow: Flow<Int> = pendingDao.getUnsyncedReportsCountFlow()

    val allReportsFlow: Flow<List<CitizenReport>> = pendingDao.getAllReportsFlow().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun initializePreloadedDataIfEmpty() = withContext(Dispatchers.IO) {
        if (sectorDao.getSectorsCount() < 60) {
            val now = System.currentTimeMillis()
            val initialSectors = listOf(
                // BLOQUE A
                SectorEntity("sec_a_alto_barinas_1", "Alto Barinas I 34,5 kV", "Circuito Don Samuel / Alto Barinas", "NORMAL", 119.0f, 48, 6, now - 15 * 60 * 1000, "Bloque A", "8.635,-70.245;8.648,-70.230;8.638,-70.210;8.622,-70.228"),
                SectorEntity("sec_a_obispos", "Obispos 34,5 kV", "Subestación Obispos", "NORMAL", 116.5f, 22, 10, now - 25 * 60 * 1000, "Bloque A", "8.605,-70.180;8.620,-70.165;8.600,-70.150;8.590,-70.170"),
                SectorEntity("sec_a_guasimito", "Guasimito", "Troncal 5 Guasimito", "NORMAL", 117.2f, 31, 8, now - 30 * 60 * 1000, "Bloque A", "8.570,-70.190;8.585,-70.175;8.565,-70.160;8.555,-70.180"),
                SectorEntity("sec_a_centro", "Centro", "Casco Central / Plaza Bolívar", "NORMAL", 118.5f, 114, 4, now - 10 * 60 * 1000, "Bloque A", "8.628,-70.210;8.635,-70.198;8.618,-70.192;8.612,-70.205"),
                SectorEntity("sec_a_sur", "Sur", "Cuatricentenaria Sur", "NORMAL", 115.0f, 45, 12, now - 40 * 60 * 1000, "Bloque A", "8.600,-70.225;8.610,-70.210;8.590,-70.200;8.585,-70.220"),
                SectorEntity("sec_a_industrial", "Industrial", "Zona Industrial Barinas", "NORMAL", 121.0f, 29, 3, now - 50 * 60 * 1000, "Bloque A", "8.580,-70.250;8.592,-70.238;8.575,-70.225;8.568,-70.240"),
                SectorEntity("sec_a_norte", "Norte", "Av. Alberto Arvelo Torrealba", "NORMAL", 118.0f, 38, 7, now - 18 * 60 * 1000, "Bloque A", "8.645,-70.240;8.655,-70.225;8.640,-70.215;8.630,-70.235"),
                SectorEntity("sec_a_raul_leoni", "Raúl Leoni", "Urb Raúl Leoni / Los Pozones", "NORMAL", 117.8f, 36, 9, now - 35 * 60 * 1000, "Bloque A", "8.612,-70.225;8.618,-70.210;8.602,-70.205;8.598,-70.220"),
                SectorEntity("sec_a_las_palmas", "Las Palmas", "Sector Las Palmas", "NORMAL", 116.0f, 27, 11, now - 45 * 60 * 1000, "Bloque A", "8.630,-70.230;8.640,-70.220;8.625,-70.210;8.618,-70.222"),
                SectorEntity("sec_a_primero_diciembre", "Primero Diciembre", "Barrio 1ero de Diciembre", "NORMAL", 115.5f, 41, 14, now - 22 * 60 * 1000, "Bloque A", "8.608,-70.235;8.615,-70.220;8.600,-70.212;8.595,-70.230"),
                SectorEntity("sec_a_pagueycito", "Pagueycito", "Sector Pagueycito", "NORMAL", 114.0f, 19, 8, now - 60 * 60 * 1000, "Bloque A", "8.550,-70.230;8.560,-70.215;8.545,-70.205;8.538,-70.225"),
                SectorEntity("sec_a_progreso", "Progreso", "Urb El Progreso", "NORMAL", 117.0f, 25, 6, now - 28 * 60 * 1000, "Bloque A", "8.625,-70.240;8.632,-70.230;8.620,-70.220;8.615,-70.235"),
                SectorEntity("sec_a_don_simon", "Don Simón", "Don Simón Rodríguez", "NORMAL", 118.2f, 33, 5, now - 15 * 60 * 1000, "Bloque A", "8.640,-70.250;8.650,-70.238;8.635,-70.228;8.628,-70.245"),
                SectorEntity("sec_a_el_real", "El Real", "Parroquia El Real", "NORMAL", 113.8f, 15, 12, now - 75 * 60 * 1000, "Bloque A", "8.520,-70.160;8.535,-70.145;8.515,-70.135;8.505,-70.155"),
                SectorEntity("sec_a_el_tambor", "El Tambor", "Sector El Tambor", "NORMAL", 115.0f, 18, 10, now - 55 * 60 * 1000, "Bloque A", "8.530,-70.190;8.542,-70.178;8.528,-70.168;8.518,-70.185"),
                SectorEntity("sec_a_cdad_bolivia_2", "Cdad Bolivia II", "Pedraza / Ciudad Bolivia II", "NORMAL", 114.5f, 24, 15, now - 80 * 60 * 1000, "Bloque A", "8.380,-70.580;8.395,-70.565;8.375,-70.550;8.365,-70.570"),
                SectorEntity("sec_a_mijagua", "Mijagua 34,5 kV", "Subestación Mijagua 34,5 kV", "NORMAL", 119.5f, 30, 7, now - 20 * 60 * 1000, "Bloque A", "8.555,-70.265;8.568,-70.250;8.548,-70.240;8.540,-70.258"),
                SectorEntity("sec_a_miri", "Mirí", "Sector Mirí", "NORMAL", 113.0f, 12, 14, now - 90 * 60 * 1000, "Bloque A", "8.320,-70.620;8.335,-70.605;8.315,-70.590;8.305,-70.610"),

                // BLOQUE B
                SectorEntity("sec_b_barinitas", "Barinitas 34,5 kV", "Subestación Barinitas 34,5 kV", "SCHEDULED_OUTAGE", 0f, 85, 96, now - 45 * 60 * 1000, "Bloque B", "8.750,-70.410;8.765,-70.395;8.745,-70.380;8.735,-70.400"),
                SectorEntity("sec_b_parangula", "Parangula", "Parangula / Quebrada Seca", "SCHEDULED_OUTAGE", 0f, 22, 92, now - 40 * 60 * 1000, "Bloque B", "8.720,-70.380;8.732,-70.368;8.718,-70.355;8.710,-70.372"),
                SectorEntity("sec_b_floresta", "Floresta", "Urb La Floresta", "SCHEDULED_OUTAGE", 0f, 39, 94, now - 35 * 60 * 1000, "Bloque B", "8.625,-70.255;8.635,-70.242;8.620,-70.235;8.612,-70.250"),
                SectorEntity("sec_b_centro_norte", "Centro Norte", "Av. Medina Jiménez / Centro", "SCHEDULED_OUTAGE", 0f, 62, 95, now - 50 * 60 * 1000, "Bloque B", "8.632,-70.215;8.640,-70.202;8.625,-70.198;8.620,-70.210"),
                SectorEntity("sec_b_los_pinos", "Los Pinos", "Sector Los Pinos", "SCHEDULED_OUTAGE", 0f, 31, 91, now - 30 * 60 * 1000, "Bloque B", "8.642,-70.260;8.652,-70.248;8.638,-70.238;8.630,-70.255"),
                SectorEntity("sec_b_carolina", "Carolina", "Urb La Carolina / Estadio", "SCHEDULED_OUTAGE", 0f, 44, 93, now - 42 * 60 * 1000, "Bloque B", "8.622,-70.220;8.630,-70.208;8.618,-70.202;8.612,-70.215"),
                SectorEntity("sec_b_borburata", "Borburata", "Sector Borburata / Obispos", "SCHEDULED_OUTAGE", 0f, 16, 89, now - 55 * 60 * 1000, "Bloque B", "8.590,-70.150;8.602,-70.138;8.588,-70.125;8.580,-70.145"),
                SectorEntity("sec_b_negro_primero", "Negro Primero", "Barrio Negro Primero", "SCHEDULED_OUTAGE", 0f, 28, 90, now - 25 * 60 * 1000, "Bloque B", "8.602,-70.230;8.610,-70.218;8.598,-70.210;8.592,-70.225"),
                SectorEntity("sec_b_hormiga", "Hormiga", "Sector La Hormiga", "SCHEDULED_OUTAGE", 0f, 19, 88, now - 65 * 60 * 1000, "Bloque B", "8.585,-70.215;8.595,-70.202;8.582,-70.192;8.575,-70.210"),
                SectorEntity("sec_b_cdad_varyna", "Cdad Varyna", "Ciudad Varyna Todos los Sectores", "SCHEDULED_OUTAGE", 0f, 95, 98, now - 20 * 60 * 1000, "Bloque B", "8.595,-70.270;8.610,-70.252;8.592,-70.240;8.582,-70.262"),
                SectorEntity("sec_b_cdad_tavacare", "Cdad Tavacare", "Ciudad Tavacare / Terrazas", "SCHEDULED_OUTAGE", 0f, 108, 97, now - 15 * 60 * 1000, "Bloque B", "8.665,-70.270;8.675,-70.255;8.658,-70.245;8.650,-70.260"),
                SectorEntity("sec_b_san_silvestre", "San Silvestre", "Parroquia San Silvestre", "SCHEDULED_OUTAGE", 0f, 26, 92, now - 60 * 60 * 1000, "Bloque B", "8.420,-70.050;8.435,-70.035;8.415,-70.020;8.405,-70.040"),
                SectorEntity("sec_b_sta_ines_lucia", "Sta Ines Lucia", "Santa Inés - Santa Lucía", "SCHEDULED_OUTAGE", 0f, 21, 90, now - 70 * 60 * 1000, "Bloque B", "8.350,-69.950;8.365,-69.935;8.345,-69.920;8.335,-69.940"),
                SectorEntity("sec_b_libertad", "Libertad", "Municipio Rojas / Libertad", "SCHEDULED_OUTAGE", 0f, 35, 94, now - 50 * 60 * 1000, "Bloque B", "8.320,-69.650;8.335,-69.635;8.315,-69.620;8.305,-69.640"),
                SectorEntity("sec_b_sta_rosa", "Sta. Rosa", "Santa Rosa de Barinas", "SCHEDULED_OUTAGE", 0f, 18, 87, now - 75 * 60 * 1000, "Bloque B", "8.280,-69.550;8.295,-69.535;8.275,-69.520;8.265,-69.540"),
                SectorEntity("sec_b_curbati", "Curbati", "Sector Curbati", "SCHEDULED_OUTAGE", 0f, 14, 89, now - 85 * 60 * 1000, "Bloque B", "8.480,-70.450;8.495,-70.435;8.475,-70.420;8.465,-70.440"),
                SectorEntity("sec_b_ticoporo", "Ticoporo", "Reserva Ticoporo / Socopó", "SCHEDULED_OUTAGE", 0f, 32, 93, now - 65 * 60 * 1000, "Bloque B", "8.150,-70.750;8.165,-70.735;8.145,-70.720;8.135,-70.740"),
                SectorEntity("sec_b_capitanejo", "Capitanejo 34,5 kV", "Capitanejo 34,5 kV / Zamora", "SCHEDULED_OUTAGE", 0f, 27, 91, now - 80 * 60 * 1000, "Bloque B", "7.950,-71.150;7.965,-71.135;7.945,-71.120;7.935,-71.140"),
                SectorEntity("sec_b_santa_elena", "Santa Elena kV", "Subestación Santa Elena", "SCHEDULED_OUTAGE", 0f, 20, 90, now - 90 * 60 * 1000, "Bloque B", "8.540,-70.280;8.552,-70.268;8.538,-70.258;8.530,-70.275"),

                // BLOQUE C
                SectorEntity("sec_c_expresa", "Expresa 34,5 kV", "Línea Expresa 34,5 kV", "NORMAL", 118.0f, 52, 5, now - 12 * 60 * 1000, "Bloque C", "8.650,-70.220;8.662,-70.208;8.648,-70.198;8.640,-70.215"),
                SectorEntity("sec_c_alto_barinas_2", "Alto Barinas II 34,5 kV", "Alto Barinas Sur / Av. Francia", "NORMAL", 119.2f, 78, 4, now - 15 * 60 * 1000, "Bloque C", "8.618,-70.242;8.622,-70.228;8.605,-70.215;8.601,-70.235"),
                SectorEntity("sec_c_socopo_1", "Socopo I", "Socopó Casco Central", "NORMAL", 115.0f, 65, 8, now - 35 * 60 * 1000, "Bloque C", "8.230,-70.730;8.245,-70.715;8.225,-70.700;8.215,-70.720"),
                SectorEntity("sec_c_bum_bum", "Bum Bum", "Sector Bum Bum / Troncal 5", "NORMAL", 114.0f, 43, 9, now - 40 * 60 * 1000, "Bloque C", "8.290,-70.680;8.305,-70.665;8.285,-70.650;8.275,-70.670"),
                SectorEntity("sec_c_socopo_2", "Socopo II", "Socopó Zona Sur", "NORMAL", 115.5f, 49, 7, now - 30 * 60 * 1000, "Bloque C", "8.210,-70.740;8.225,-70.725;8.205,-70.710;8.195,-70.730"),
                SectorEntity("sec_c_cardenera", "Cardenera", "Sector La Cardenera", "NORMAL", 116.0f, 21, 6, now - 55 * 60 * 1000, "Bloque C", "8.610,-70.260;8.620,-70.248;8.608,-70.238;8.600,-70.255"),
                SectorEntity("sec_c_corocito", "Corocito", "Corocito / Av. Intercomunal", "NORMAL", 117.5f, 58, 6, now - 22 * 60 * 1000, "Bloque C", "8.595,-70.235;8.601,-70.220;8.585,-70.210;8.580,-70.228"),
                SectorEntity("sec_c_bolivar", "Bolivar", "Plaza Bolívar / Casco", "NORMAL", 118.0f, 37, 5, now - 18 * 60 * 1000, "Bloque C", "8.625,-70.212;8.632,-70.200;8.620,-70.194;8.615,-70.206"),
                SectorEntity("sec_c_esperanza", "Esperanza", "Sector La Esperanza", "NORMAL", 116.8f, 29, 8, now - 45 * 60 * 1000, "Bloque C", "8.615,-70.250;8.625,-70.238;8.612,-70.228;8.605,-70.245"),
                SectorEntity("sec_c_torunos", "Torunos", "Parroquia Torunos", "NORMAL", 113.5f, 23, 11, now - 65 * 60 * 1000, "Bloque C", "8.480,-70.120;8.495,-70.105;8.475,-70.090;8.465,-70.110"),
                SectorEntity("sec_c_cdad_nutrias", "Cdad Nutria 34,5 kV", "Subestación Ciudad de Nutrias", "NORMAL", 112.5f, 31, 14, now - 70 * 60 * 1000, "Bloque C", "8.180,-69.180;8.195,-69.165;8.175,-69.150;8.165,-69.170"),
                SectorEntity("sec_c_dolores", "Dolores", "Parroquia Dolores", "NORMAL", 114.0f, 19, 10, now - 85 * 60 * 1000, "Bloque C", "8.250,-69.450;8.265,-69.435;8.245,-69.420;8.235,-69.440"),
                SectorEntity("sec_c_el_paguey", "El Paguey", "Río Paguey / Sector Rural", "NORMAL", 113.0f, 15, 12, now - 95 * 60 * 1000, "Bloque C", "8.520,-70.280;8.535,-70.265;8.515,-70.250;8.505,-70.270"),
                SectorEntity("sec_c_cdad_bolivia_1", "Cdad Bolivia I", "Ciudad Bolivia Pedraza Centro", "NORMAL", 115.0f, 38, 8, now - 50 * 60 * 1000, "Bloque C", "8.390,-70.570;8.405,-70.555;8.385,-70.540;8.375,-70.560"),
                SectorEntity("sec_c_fundacea", "Fundacea", "Sector Fundacea / UNELLEZ", "NORMAL", 117.0f, 41, 6, now - 28 * 60 * 1000, "Bloque C", "8.605,-70.245;8.615,-70.232;8.602,-70.222;8.595,-70.240"),
                SectorEntity("sec_c_estadio", "Estadio", "Alrededores del Estadio Cuatricentenario", "NORMAL", 118.5f, 33, 5, now - 20 * 60 * 1000, "Bloque C", "8.618,-70.218;8.625,-70.206;8.612,-70.200;8.608,-70.214"),

                // BLOQUE D
                SectorEntity("sec_d_barinas_centro", "Barinas Centro D", "Subestación Centro 34,5 kV", "NORMAL", 118.0f, 42, 5, now - 10 * 60 * 1000, "Bloque D", "8.630,-70.210;8.638,-70.195;8.622,-70.190;8.615,-70.205"),
                SectorEntity("sec_d_punta_gorda", "Punta Gorda", "Circuito Punta Gorda", "NORMAL", 116.0f, 25, 8, now - 20 * 60 * 1000, "Bloque D", "8.605,-70.240;8.618,-70.225;8.602,-70.215;8.595,-70.230"),
                SectorEntity("sec_d_caramuca", "La Caramuca", "Troncal 5 / Subestación Caramuca", "NORMAL", 115.5f, 31, 7, now - 35 * 60 * 1000, "Bloque D", "8.520,-70.250;8.535,-70.235;8.515,-70.220;8.505,-70.240"),
                SectorEntity("sec_d_rodriguez_dominguez", "Rodríguez Domínguez", "Circuito Hospital / R. Domínguez", "NORMAL", 119.0f, 55, 4, now - 12 * 60 * 1000, "Bloque D", "8.625,-70.225;8.635,-70.212;8.620,-70.205;8.612,-70.220"),
                SectorEntity("sec_d_palacio_fajardo", "Palacio Fajardo", "Sector Palacio Fajardo", "NORMAL", 116.5f, 28, 9, now - 40 * 60 * 1000, "Bloque D", "8.610,-70.235;8.620,-70.220;8.605,-70.210;8.598,-70.228"),
                SectorEntity("sec_d_sabana_grande", "Sabana Grande", "Sector Sabana Grande / Troncal 5", "NORMAL", 114.8f, 20, 11, now - 50 * 60 * 1000, "Bloque D", "8.560,-70.220;8.572,-70.205;8.555,-70.195;8.548,-70.215"),
                SectorEntity("sec_d_san_silvestre_rural", "San Silvestre Rural", "Circuito Rural San Silvestre", "NORMAL", 113.5f, 15, 12, now - 60 * 60 * 1000, "Bloque D", "8.430,-70.060;8.445,-70.045;8.425,-70.030;8.415,-70.050"),
                SectorEntity("sec_d_sta_lucia_rural", "Santa Lucía Rural", "Sector Santa Lucía Troncal", "NORMAL", 114.0f, 18, 10, now - 70 * 60 * 1000, "Bloque D", "8.360,-69.960;8.375,-69.945;8.355,-69.930;8.345,-69.950"),
                SectorEntity("sec_d_ciudad_tavacare_d", "Cdad Tavacare D", "Ciudad Tavacare Terrazas B-D", "NORMAL", 117.0f, 64, 6, now - 18 * 60 * 1000, "Bloque D", "8.670,-70.265;8.680,-70.250;8.665,-70.240;8.655,-70.255"),
                SectorEntity("sec_d_boconoito", "Boconoito", "Circuito Boconoito Intercomunal", "NORMAL", 113.0f, 22, 13, now - 80 * 60 * 1000, "Bloque D", "8.710,-69.820;8.725,-69.805;8.705,-69.790;8.695,-69.810")
            )
            sectorDao.insertOrUpdateSectors(initialSectors)
        }

        // Seed sectors and active PAC schedule to Firebase Firestore if empty
        cloudSync.seedSectorsIfEmpty()

        // Seed realistic historical records for modular arithmetic calculations
        if (outageDao.getCount() == 0) {
            val now = System.currentTimeMillis()
            val hourMs = 3600 * 1000L
            val dayMs = 24 * hourMs

            val historicalRecords = listOf(
                // Alto Barinas Norte: 4h outages every day shifting cyclically
                OutageRecordEntity(
                    sectorId = "sector_alto_barinas_norte",
                    sectorName = "Alto Barinas Norte",
                    statusType = "SCHEDULED_OUTAGE",
                    startTimeMillis = now - (dayMs * 1) - (hourMs * 6),
                    endTimeMillis = now - (dayMs * 1) - (hourMs * 2),
                    durationHours = 4.0f,
                    notes = "PAC Bloque A Rotación"
                ),
                OutageRecordEntity(
                    sectorId = "sector_alto_barinas_norte",
                    sectorName = "Alto Barinas Norte",
                    statusType = "SCHEDULED_OUTAGE",
                    startTimeMillis = now - (dayMs * 2) - (hourMs * 10),
                    endTimeMillis = now - (dayMs * 2) - (hourMs * 6),
                    durationHours = 4.0f,
                    notes = "PAC Bloque A Rotación"
                ),
                OutageRecordEntity(
                    sectorId = "sector_alto_barinas_norte",
                    sectorName = "Alto Barinas Norte",
                    statusType = "SCHEDULED_OUTAGE",
                    startTimeMillis = now - (dayMs * 3) - (hourMs * 14),
                    endTimeMillis = now - (dayMs * 3) - (hourMs * 10),
                    durationHours = 4.0f,
                    notes = "PAC Bloque A Rotación"
                ),
                // Alto Barinas Sur: active outage now
                OutageRecordEntity(
                    sectorId = "sector_alto_barinas_sur",
                    sectorName = "Alto Barinas Sur",
                    statusType = "SCHEDULED_OUTAGE",
                    startTimeMillis = now - (hourMs * 2),
                    endTimeMillis = null,
                    durationHours = 4.0f,
                    notes = "PAC Bloque B en curso"
                ),
                OutageRecordEntity(
                    sectorId = "sector_alto_barinas_sur",
                    sectorName = "Alto Barinas Sur",
                    statusType = "SCHEDULED_OUTAGE",
                    startTimeMillis = now - (dayMs * 1) - (hourMs * 6),
                    endTimeMillis = now - (dayMs * 1) - (hourMs * 2),
                    durationHours = 4.0f,
                    notes = "PAC Bloque B"
                ),
                // Ciudad Tavacare: Irregular transformer fault
                OutageRecordEntity(
                    sectorId = "sector_ciudad_tavacare",
                    sectorName = "Ciudad Tavacare",
                    statusType = "IRREGULAR_OUTAGE",
                    startTimeMillis = now - (hourMs * 3),
                    endTimeMillis = null,
                    durationHours = 5.5f,
                    notes = "Falla de Transformador en Subestación"
                )
            )
            outageDao.insertOutages(historicalRecords)
        }
    }

    suspend fun getPredictionForSector(sectorId: String): OutagePrediction? = withContext(Dispatchers.IO) {
        val sectorEntity = sectorDao.getSectorById(sectorId) ?: return@withContext null
        val sector = sectorEntity.toDomain()
        val historyEntities = outageDao.getRecentOutagesBySector(sectorId)
        val history = historyEntities.map { it.toDomain() }

        OutagePredictionEngine.calculateNextOutageWindow(
            sector = sector,
            history = history,
            currentTimeMillis = System.currentTimeMillis()
        )
    }

    suspend fun submitReport(
        sectorId: String,
        sectorName: String,
        hasPower: Boolean,
        reportType: String,
        voltage: Float?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // 1. Save locally to Room (Zero data loss, 100% Offline-First)
            val entity = PendingReportEntity(
                sectorId = sectorId,
                sectorName = sectorName,
                hasPower = hasPower,
                reportedAtMillis = System.currentTimeMillis(),
                reportType = reportType,
                voltageObserved = voltage,
                isSynced = false
            )
            pendingDao.insertReport(entity)

            // 2. Optimistic local update for instant UI feedback
            val sector = sectorDao.getSectorById(sectorId)
            if (sector != null) {
                val updatedStatus = if (hasPower) {
                    if (reportType == "BAJON") "NORMAL" else "NORMAL"
                } else {
                    if (reportType == "AVERIA") "IRREGULAR_OUTAGE" else "SCHEDULED_OUTAGE"
                }
                val newVoltage = voltage ?: if (hasPower) 118f else 0f
                val newCount = sector.confirmedReportsCount + 1
                val newPct = if (hasPower) {
                    (sector.withoutPowerPercentage - 8).coerceAtLeast(0)
                } else {
                    (sector.withoutPowerPercentage + 14).coerceAtMost(100)
                }
                sectorDao.updateSector(
                    sector.copy(
                        status = updatedStatus,
                        voltage = newVoltage,
                        confirmedReportsCount = newCount,
                        withoutPowerPercentage = newPct,
                        lastUpdatedMillis = System.currentTimeMillis()
                    )
                )
            }

            // 3. Enqueue background work via WorkManager
            val inputData = Data.Builder()
                .putString(ReportPowerWorker.KEY_SECTOR_ID, sectorId)
                .putBoolean(ReportPowerWorker.KEY_HAS_POWER, hasPower)
                .putString(ReportPowerWorker.KEY_REPORT_TYPE, reportType)
                .putFloat(ReportPowerWorker.KEY_VOLTAGE, voltage ?: 0f)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<ReportPowerWorker>()
                .setInputData(inputData)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                ReportPowerWorker.WORK_NAME,
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                workRequest
            )

            // 4. Try immediate sync via Firestore & REST (non-blocking, offline resilient)
            try {
                // Real-time Firestore sync
                val firestoreSynced = cloudSync.uploadCitizenReport(
                    sectorId = sectorId,
                    sectorName = sector?.name ?: sectorId,
                    hasPower = hasPower,
                    reportType = reportType,
                    voltage = voltage
                )

                if (firestoreSynced) {
                    val latest = pendingDao.getUnsyncedReports().lastOrNull()
                    if (latest != null) {
                        pendingDao.markAsSynced(latest.id)
                    }
                } else {
                    // Fallback to REST API
                    val request = TelemetryReportRequest(
                        sectorId = sectorId,
                        hasPower = hasPower,
                        timestamp = System.currentTimeMillis(),
                        reportType = reportType,
                        voltageReading = voltage
                    )
                    val response = ApiClient.api.submitPowerReport(request)
                    if (response.isSuccessful && response.body()?.success == true) {
                        val latest = pendingDao.getUnsyncedReports().lastOrNull()
                        if (latest != null) {
                            pendingDao.markAsSynced(latest.id)
                        }
                    }
                }
            } catch (e: Exception) {
                // Keep offline in Room; WorkManager and Firestore handle it
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncPendingReportsNow(): Int = withContext(Dispatchers.IO) {
        val unsynced = pendingDao.getUnsyncedReports()
        var count = 0
        for (item in unsynced) {
            try {
                val response = ApiClient.api.submitPowerReport(
                    TelemetryReportRequest(
                        sectorId = item.sectorId,
                        hasPower = item.hasPower,
                        timestamp = item.reportedAtMillis,
                        reportType = item.reportType,
                        voltageReading = item.voltageObserved
                    )
                )
                if (response.isSuccessful && response.body()?.success == true) {
                    pendingDao.markAsSynced(item.id)
                    count++
                }
            } catch (e: Exception) {
                // If Barinas blackout continues, stop loop
                break
            }
        }
        count
    }

    suspend fun registerCommunityLocation(
        name: String,
        parroquia: String,
        block: String,
        circuit: String
    ): Result<Sector> = withContext(Dispatchers.IO) {
        try {
            val cleanId = "sec_com_" + name.lowercase().replace("[^a-z0-9]".toRegex(), "_")
            val newSector = SectorEntity(
                id = cleanId,
                name = name.trim(),
                circuitCode = circuit.trim().ifEmpty { "Circuito $parroquia" },
                status = "NORMAL",
                voltage = 118f,
                confirmedReportsCount = 1,
                withoutPowerPercentage = 0,
                lastUpdatedMillis = System.currentTimeMillis(),
                rotationBlock = block.trim(),
                polygonPointsRaw = "8.625,-70.220;8.635,-70.208;8.620,-70.202;8.615,-70.215"
            )
            sectorDao.insertOrUpdateSector(newSector)

            // Upload to Firestore in background
            cloudSync.uploadCommunityLocation(name, parroquia, block, circuit)

            Result.success(newSector.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

package com.example.data

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.model.PacBlock

data class PacAlertTarget(
    val profile: String,
    val sectorId: String,
    val sectorName: String,
    val block: String
)

/** Room owns the block assignment; older cached preferences may contain the B of BLOQUE. */
object PacAlertTargetResolver {
    suspend fun resolve(
        context: Context,
        database: AppDatabase = AppDatabase.getInstance(context)
    ): PacAlertTarget? {
        val prefs = context.getSharedPreferences("luz_barinas_user_prefs", Context.MODE_PRIVATE)
        val profile = prefs.getString("active_profile", "home") ?: return null
        if (profile !in setOf("home", "work")) return null
        val profileId = prefs.getString("profile_${profile}_sector_id", null)
        val savedId = prefs.getString("saved_sector_id", null)
        val selectedId = prefs.getString("selected_sector_id", null)
        // A newly selected, unconfigured work profile must not inherit the home's alarms.
        val sectorId = profileId ?: if (profile == "home") {
            savedId ?: selectedId
        } else null
        sectorId ?: return null
        val sector = database.sectorDao().getSectorById(sectorId) ?: return null
        val block = PacBlock.normalize(sector.rotationBlock) ?: return null
        val name = prefs.getString("profile_${profile}_address", null)
            ?: prefs.getString("saved_address", null)?.takeIf {
                prefs.getString("saved_sector_id", null) == sectorId
            }
            ?: sector.name

        // Do not repair a profile that changed while the database query was suspended.
        if (prefs.getString("active_profile", "home") != profile ||
            prefs.getString("profile_${profile}_sector_id", null) != profileId ||
            (profileId == null && (prefs.getString("saved_sector_id", null) != savedId ||
                prefs.getString("selected_sector_id", null) != selectedId))) return null
        prefs.edit()
            .putString("saved_sector_id", sectorId)
            .putString("selected_sector_id", sectorId)
            .putString("saved_block", block)
            .putString("saved_sector_block", block)
            .putString("selected_sector_block", block)
            .putString("profile_${profile}_sector_id", sectorId)
            .putString("profile_${profile}_block", block)
            .apply()
        return PacAlertTarget(profile, sectorId, name, block)
    }
}

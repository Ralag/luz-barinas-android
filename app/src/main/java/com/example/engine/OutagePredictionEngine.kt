package com.example.engine

import android.util.Log
import com.example.data.model.OutagePrediction
import com.example.data.model.OutageRecord
import com.example.data.model.Sector
import com.example.data.remote.ApiClient

object OutagePredictionEngine {

    /**
     * Calls the 'getOutagePrediction' Serverless Function to get the estimated next outage
     * from the backend, significantly reducing local CPU usage and memory footprint.
     */
    suspend fun calculateNextOutageWindow(
        sector: Sector,
        history: List<OutageRecord>,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): OutagePrediction? {
        return try {
            val response = ApiClient.api.getOutagePrediction(sector.id)
            if (response.isSuccessful) {
                response.body()
            } else {
                Log.e("OutagePrediction", "API Error: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e("OutagePrediction", "Failed to fetch prediction from backend: ${e.message}")
            // Consider returning a default/fallback prediction here if needed
            null
        }
    }
}

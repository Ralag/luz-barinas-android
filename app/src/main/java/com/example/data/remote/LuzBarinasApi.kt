package com.example.data.remote

import com.example.data.remote.dto.SectorDto
import com.example.data.remote.dto.TelemetryReportRequest
import com.example.data.remote.dto.TelemetryReportResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface LuzBarinasApi {
    @GET("api/v1/sectors")
    suspend fun getSectors(): Response<List<SectorDto>>

    @GET("api/v1/sectors/{sectorId}")
    suspend fun getSectorDetails(
        @Path("sectorId") sectorId: String
    ): Response<SectorDto>

    @POST("api/v1/telemetry/report")
    suspend fun submitPowerReport(
        @Body request: TelemetryReportRequest
    ): Response<TelemetryReportResponse>
}

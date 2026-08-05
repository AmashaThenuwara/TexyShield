/*
 * File: BlockchainApi.kt
 * Includes UI components and functionality for the Smart Factory Android app.
 */
package com.example.smartfactory.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

data class ReportData(
    val worker_id: String,
    val issue: String,
    val location: String,
    val timestamp: String
)

data class AttendanceData(
    val worker_uid: String,
    val worker_name: String,
    val timestamp: String,
    val shift: String = "Morning"
)

data class MineBlockResponse(
    val message: String?,
    val index: Long?,
    val timestamp: Double?,
    val report_data: String?,
    val proof: Long?,
    val previous_hash: String?
)

data class Block(
    val index: Long?,
    val timestamp: Double?,
    val report_data: String?,
    val proof: Long?,
    val previous_hash: String?
)

data class GetChainResponse(
    val chain: List<Block>?,
    val length: Int?,
    val is_valid: Boolean?
)

data class GetAttendanceChainResponse(
    val chain: List<Block>?,
    val length: Int?,
    val is_valid: Boolean?
)

interface BlockchainApi {
    @POST("mine_block")
    suspend fun mineBlock(@Body data: ReportData): MineBlockResponse

    @GET("get_chain")
    suspend fun getChain(): GetChainResponse
    
    @POST("mine_attendance_block")
    suspend fun mineAttendanceBlock(@Body data: AttendanceData): MineBlockResponse

    @GET("get_attendance_chain")
    suspend fun getAttendanceChain(): GetAttendanceChainResponse
}

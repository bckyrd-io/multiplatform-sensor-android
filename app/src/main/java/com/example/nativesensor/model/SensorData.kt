package com.example.nativesensor.model

import java.util.Date

/**
 * Represents sensor data collected from wear devices
 * Stores health and activity metrics
 */
data class SensorData(
    val id: Int = 0,
    val userId: Int,
    val heartRate: Int? = null,
    val steps: Int? = null,
    val calories: Int? = null,
    val activityType: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val timestamp: Date = Date()
) {
    companion object {
        fun fromMap(map: Map<String, Any>): SensorData {
            return SensorData(
                id = (map["id"] as? Number)?.toInt() ?: 0,
                userId = (map["user_id"] as? Number)?.toInt() ?: 0,
                heartRate = (map["heart_rate"] as? Number)?.toInt(),
                steps = (map["steps"] as? Number)?.toInt(),
                calories = (map["calories"] as? Number)?.toInt(),
                activityType = map["activity_type"] as? String,
                latitude = (map["latitude"] as? Number)?.toDouble(),
                longitude = (map["longitude"] as? Number)?.toDouble(),
                timestamp = try {
                    Date((map["timestamp"] as? String)?.let { 
                        java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(it)?.time 
                    } ?: System.currentTimeMillis())
                } catch (e: Exception) {
                    Date()
                }
            )
        }
    }
    
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "user_id" to userId,
            "heart_rate" to (heartRate ?: 0),
            "steps" to (steps ?: 0),
            "calories" to (calories ?: 0),
            "activity_type" to (activityType ?: ""),
            "latitude" to (latitude ?: 0.0),
            "longitude" to (longitude ?: 0.0),
            "timestamp" to timestamp
        )
    }
}

/**
 * Represents user statistics from sensor data
 */
data class UserStats(
    val totalRecords: Int = 0,
    val avgHeartRate: Double = 0.0,
    val totalSteps: Int = 0,
    val totalCalories: Int = 0,
    val lastActivity: Date? = null,
    val todayRecords: Int = 0,
    val todayAvgHeartRate: Double = 0.0,
    val todaySteps: Int = 0,
    val todayCalories: Int = 0
) {
    companion object {
        fun fromMap(totalStats: Map<String, Any>, todayStats: Map<String, Any>): UserStats {
            return UserStats(
                totalRecords = (totalStats["total_records"] as? Number)?.toInt() ?: 0,
                avgHeartRate = (totalStats["avg_heart_rate"] as? Number)?.toDouble() ?: 0.0,
                totalSteps = (totalStats["total_steps"] as? Number)?.toInt() ?: 0,
                totalCalories = (totalStats["total_calories"] as? Number)?.toInt() ?: 0,
                lastActivity = try {
                    (totalStats["last_activity"] as? String)?.let { 
                        java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(it) 
                    }
                } catch (e: Exception) {
                    null
                },
                todayRecords = (todayStats["today_records"] as? Number)?.toInt() ?: 0,
                todayAvgHeartRate = (todayStats["today_avg_heart_rate"] as? Number)?.toDouble() ?: 0.0,
                todaySteps = (todayStats["today_steps"] as? Number)?.toInt() ?: 0,
                todayCalories = (todayStats["today_calories"] as? Number)?.toInt() ?: 0
            )
        }
    }
} 
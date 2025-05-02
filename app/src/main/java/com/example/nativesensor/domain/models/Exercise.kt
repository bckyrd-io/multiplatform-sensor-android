package com.example.nativesensor.domain.models

/**
 * Base class for all exercises
 * Provides common functionality and sensor integration
 */
open class Exercise(
    private var id: String = "",
    private var name: String = "",
    private var description: String = "",
    private var duration: Int = 0,      // Duration in minutes
    private var intensity: Int = 0,     // Intensity level (1-5)
    private var calories: Double = 0.0,
    private var distance: Double = 0.0,  // Distance in kilometers
    private var steps: Int = 0,
    private var sensorData: Map<String, Any> = emptyMap()
) {
    
    // Properties with getters and setters
    fun getId(): String = id
    fun setId(value: String) { id = value }
    
    fun getName(): String = name
    fun setName(value: String) { name = value }
    
    fun getDescription(): String = description
    fun setDescription(value: String) { description = value }
    
    fun getDuration(): Int = duration
    fun setDuration(value: Int) { duration = value }
    
    fun getIntensity(): Int = intensity
    fun setIntensity(value: Int) { intensity = value }
    
    fun getCalories(): Double = calories
    fun setCalories(value: Double) { calories = value }
    
    fun getDistance(): Double = distance
    fun setDistance(value: Double) { distance = value }
    
    fun getSteps(): Int = steps
    fun setSteps(value: Int) { steps = value }
    
    fun getSensorData(): Map<String, Any> = sensorData
    fun setSensorData(value: Map<String, Any>) { sensorData = value }
    
    /**
     * Calculate calories burned based on intensity and duration
     * @return Double - calories burned
     */
    open fun calculateCalories(): Double {
        // Base formula: MET * weight * time / 200
        // MET values: 8 for running, 6 for cycling, 3 for yoga
        return when (name) {
            "Running" -> 8.0 * duration
            "Cycling" -> 6.0 * duration
            "Yoga" -> 3.0 * duration
            else -> 5.0 * duration
        }
    }
    
    /**
     * Get exercise intensity level
     * @return Int - intensity level (1-5)
     */
    open fun getIntensityLevel(): Int {
        return intensity
    }
    
    /**
     * Get exercise summary for display
     * @return String - formatted exercise summary
     */
    open fun getSummary(): String {
        return """
            Exercise Summary:
            Name: $name
            Duration: $duration minutes
            Intensity: $intensity
            Calories: ${"%.2f".format(calories)}
            Distance: ${"%.2f".format(distance)} km
            Steps: $steps
        """.trimIndent()
    }
    
    /**
     * Validate exercise data
     * @return Boolean - true if exercise data is valid
     */
    open fun validateExercise(): Boolean {
        return name.isNotEmpty() && 
               duration > 0 && 
               intensity in 1..5 && 
               calories >= 0 && 
               distance >= 0 && 
               steps >= 0
    }
    
    // Nested class for exercise statistics
    class ExerciseStats {
        private var totalCalories: Double = 0.0
        private var totalDistance: Double = 0.0
        private var totalDuration: Int = 0
        private var totalSteps: Int = 0
        private var sensorData: Map<String, Any> = emptyMap()
        
        fun addCalories(calories: Double) {
            totalCalories += calories
        }
        
        fun addDistance(distance: Double) {
            totalDistance += distance
        }
        
        fun addDuration(minutes: Int) {
            totalDuration += minutes
        }
        
        fun addSteps(steps: Int) {
            totalSteps += steps
        }
        
        fun addSensorData(key: String, value: Any) {
            sensorData = sensorData + (key to value)
        }
        
        fun getStats(): Map<String, Any> {
            return mapOf(
                "totalCalories" to totalCalories,
                "totalDistance" to totalDistance,
                "totalDuration" to totalDuration,
                "totalSteps" to totalSteps,
                "sensorData" to sensorData
            )
        }
    }
    
    // Native sensor integration methods
    open fun integrateStepCounter() {
        // TODO: Implement step counter integration
        // Track steps and activity type
    }
    
    open fun integrateGPS() {
        // TODO: Implement GPS integration
        // Track distance and pace
    }
    
    open fun integrateAccelerometer() {
        // TODO: Implement accelerometer integration
        // Track intensity and activity type
    }
    
    open fun integrateHeartRate() {
        // TODO: Implement heart rate integration
        // Track heart rate zones and calories
    }
}

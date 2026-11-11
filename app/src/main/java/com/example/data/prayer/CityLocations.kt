package com.example.data.prayer

data class CityLocation(
    val name: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val defaultMethod: CalculationMethod = CalculationMethod.UMM_AL_QURA
)

object CityLocations {
    val predefinedCities = listOf(
        // Holy Sites & Middle East
        CityLocation("Makkah", "Saudi Arabia", 21.4225, 39.8262, CalculationMethod.UMM_AL_QURA),
        CityLocation("Medina", "Saudi Arabia", 24.5247, 39.5692, CalculationMethod.UMM_AL_QURA),
        CityLocation("Riyadh", "Saudi Arabia", 24.7136, 46.6753, CalculationMethod.UMM_AL_QURA),
        CityLocation("Jeddah", "Saudi Arabia", 21.4858, 39.1925, CalculationMethod.UMM_AL_QURA),
        CityLocation("Dubai", "United Arab Emirates", 25.2048, 55.2708, CalculationMethod.DUBAI),
        CityLocation("Abu Dhabi", "United Arab Emirates", 24.4539, 54.3773, CalculationMethod.DUBAI),
        CityLocation("Doha", "Qatar", 25.2854, 51.5310, CalculationMethod.UMM_AL_QURA),
        CityLocation("Kuwait City", "Kuwait", 29.3759, 47.9774, CalculationMethod.UMM_AL_QURA),
        CityLocation("Manama", "Bahrain", 26.2285, 50.5860, CalculationMethod.UMM_AL_QURA),
        CityLocation("Muscat", "Oman", 23.5880, 58.3829, CalculationMethod.UMM_AL_QURA),
        CityLocation("Cairo", "Egypt", 30.0444, 31.2357, CalculationMethod.EGYPTIAN),
        CityLocation("Alexandria", "Egypt", 31.2001, 29.9187, CalculationMethod.EGYPTIAN),
        CityLocation("Amman", "Jordan", 31.9454, 35.9284, CalculationMethod.UMM_AL_QURA),
        CityLocation("Jerusalem (Al-Quds)", "Palestine", 31.7683, 35.2137, CalculationMethod.UMM_AL_QURA),
        CityLocation("Beirut", "Lebanon", 33.8938, 35.5018, CalculationMethod.MUSLIM_WORLD_LEAGUE),
        CityLocation("Baghdad", "Iraq", 33.3152, 44.3661, CalculationMethod.MUSLIM_WORLD_LEAGUE),
        CityLocation("Istanbul", "Turkey", 41.0082, 28.9784, CalculationMethod.MUSLIM_WORLD_LEAGUE),
        CityLocation("Ankara", "Turkey", 39.9334, 32.8597, CalculationMethod.MUSLIM_WORLD_LEAGUE),
        CityLocation("Casablanca", "Morocco", 33.5731, -7.5898, CalculationMethod.MUSLIM_WORLD_LEAGUE),
        CityLocation("Rabat", "Morocco", 34.0209, -6.8416, CalculationMethod.MUSLIM_WORLD_LEAGUE),
        CityLocation("Tunis", "Tunisia", 36.8065, 10.1815, CalculationMethod.MUSLIM_WORLD_LEAGUE),
        CityLocation("Algiers", "Algeria", 36.7538, 3.0588, CalculationMethod.MUSLIM_WORLD_LEAGUE),

        // North America
        CityLocation("New York", "United States", 40.7128, -74.0060, CalculationMethod.ISNA),
        CityLocation("Los Angeles", "United States", 34.0522, -118.2437, CalculationMethod.ISNA),
        CityLocation("Chicago", "United States", 41.8781, -87.6298, CalculationMethod.ISNA),
        CityLocation("Houston", "United States", 29.7604, -95.3698, CalculationMethod.ISNA),
        CityLocation("Dallas", "United States", 32.7767, -96.7970, CalculationMethod.ISNA),
        CityLocation("Washington, D.C.", "United States", 38.9072, -77.0369, CalculationMethod.ISNA),
        CityLocation("San Francisco", "United States", 37.7749, -122.4194, CalculationMethod.ISNA),
        CityLocation("Seattle", "United States", 47.6062, -122.3321, CalculationMethod.ISNA),
        CityLocation("Miami", "United States", 25.7617, -80.1918, CalculationMethod.ISNA),
        CityLocation("Toronto", "Canada", 43.6532, -79.3832, CalculationMethod.ISNA),
        CityLocation("Montreal", "Canada", 45.5017, -73.5673, CalculationMethod.ISNA),
        CityLocation("Vancouver", "Canada", 49.2827, -123.1207, CalculationMethod.ISNA),
        CityLocation("Calgary", "Canada", 51.0447, -114.0719, CalculationMethod.ISNA),

        // Europe
        CityLocation("London", "United Kingdom", 51.5074, -0.1278, CalculationMethod.MUSLIM_WORLD_LEAGUE),
        CityLocation("Birmingham", "United Kingdom", 52.4862, -1.8904, CalculationMethod.MUSLIM_WORLD_LEAGUE),
        CityLocation("Manchester", "United Kingdom", 53.4808, -2.2426, CalculationMethod.MUSLIM_WORLD_LEAGUE),
        CityLocation("Paris", "France", 48.8566, 2.3522, CalculationMethod.MUSLIM_WORLD_LEAGUE),
        CityLocation("Berlin", "Germany", 52.5200, 13.4050, CalculationMethod.MUSLIM_WORLD_LEAGUE),
        CityLocation("Frankfurt", "Germany", 50.1109, 8.6821, CalculationMethod.MUSLIM_WORLD_LEAGUE),
        CityLocation("Amsterdam", "Netherlands", 52.3676, 4.9041, CalculationMethod.MUSLIM_WORLD_LEAGUE),
        CityLocation("Brussels", "Belgium", 50.8503, 4.3517, CalculationMethod.MUSLIM_WORLD_LEAGUE),
        CityLocation("Rome", "Italy", 41.9028, 12.4964, CalculationMethod.MUSLIM_WORLD_LEAGUE),
        CityLocation("Madrid", "Spain", 40.4168, -3.7038, CalculationMethod.MUSLIM_WORLD_LEAGUE),

        // Asia & Australasia
        CityLocation("Jakarta", "Indonesia", -6.2088, 106.8456, CalculationMethod.MUSLIM_WORLD_LEAGUE),
        CityLocation("Surabaya", "Indonesia", -7.2575, 112.7521, CalculationMethod.MUSLIM_WORLD_LEAGUE),
        CityLocation("Kuala Lumpur", "Malaysia", 3.1390, 101.6869, CalculationMethod.MUSLIM_WORLD_LEAGUE),
        CityLocation("Singapore", "Singapore", 1.3521, 103.8198, CalculationMethod.MUSLIM_WORLD_LEAGUE),
        CityLocation("Karachi", "Pakistan", 24.8607, 67.0011, CalculationMethod.KARACHI),
        CityLocation("Lahore", "Pakistan", 31.5204, 74.3587, CalculationMethod.KARACHI),
        CityLocation("Islamabad", "Pakistan", 33.6844, 73.0479, CalculationMethod.KARACHI),
        CityLocation("Dhaka", "Bangladesh", 23.8103, 90.4125, CalculationMethod.KARACHI),
        CityLocation("New Delhi", "India", 28.6139, 77.2090, CalculationMethod.KARACHI),
        CityLocation("Mumbai", "India", 19.0760, 72.8777, CalculationMethod.KARACHI),
        CityLocation("Sydney", "Australia", -33.8688, 151.2093, CalculationMethod.MUSLIM_WORLD_LEAGUE),
        CityLocation("Melbourne", "Australia", -37.8136, 144.9631, CalculationMethod.MUSLIM_WORLD_LEAGUE),
        CityLocation("Auckland", "New Zealand", -36.8485, 174.7633, CalculationMethod.MUSLIM_WORLD_LEAGUE)
    )

    fun findCity(name: String): CityLocation? {
        return predefinedCities.find { it.name.equals(name, ignoreCase = true) }
    }
}

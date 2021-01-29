object Config {
    data class ProjectProperty(val apiUrl: String)


    object SdkVersions {
        const val versionCode = 6
        const val compileSdk = 28
        const val targetSdk = 28
        const val minSdk = 23
        const val versionName = "1.5.0"
    }

    object Project {
        const val applicationId = "dev.kiwik.tracking"
        val config =
                mapOf(
                        "DEV" to ProjectProperty(
                                "\"http://192.168.0.104:8000\"" //"\"\""
                        ),
                        "PROD" to ProjectProperty("\"http://66.228.47.109:80\"" //"PROD" to ProjectProperty("\"http://162.243.100.163:7000\""
                        ),
                        "CAL" to ProjectProperty("\"http://66.228.47.109:80\""
                        )
                )
    }
}
package com.appcitas.app.util

object Constants {
    // API Configuration
    const val BASE_URL = "https://api.appcitas.com/v1/"
    const val SOCKET_URL = "wss://api.appcitas.com"

    // Local Storage Keys
    const val ACCESS_TOKEN_KEY = "access_token"
    const val REFRESH_TOKEN_KEY = "refresh_token"
    const val USER_ID_KEY = "user_id"
    const val ENCRYPTION_KEY_KEY = "encryption_key"

    // AdMob
    const val ADMOB_APP_ID = "ca-app-pub-xxxxxxxxxxxxxxxx~yyyyyyyyyy"
    const val AD_BANNER_HOME = "ca-app-pub-xxxxxxxxxxxxxxxx/zzzzzzzzzz"
    const val AD_BANNER_CHAT = "ca-app-pub-xxxxxxxxxxxxxxxx/zzzzzzzzzz"
    const val AD_BANNER_PROFILE = "ca-app-pub-xxxxxxxxxxxxxxxx/zzzzzzzzzz"

    // Verification
    const val MIN_SIMILARITY_SCORE = 0.6
    const val MIN_LIVENESS_SCORE = 0.7
    const val MIN_ANTI_SPOOFING_SCORE = 0.7

    // Chat
    const val MESSAGE_LIMIT = 50
    const val COMMUNITY_MESSAGE_LIMIT = 100

    // Location
    const val DEFAULT_RADIUS_KM = 50.0
    const val LOCATION_UPDATE_INTERVAL = 300000L // 5 minutes

    // Moderation
    const val MAX_WARNINGS = 3
    const val WARNING_DURATION_HOURS = 24

    // Pagination
    const val DEFAULT_PAGE_SIZE = 20

    // Date Formats
    const val DATE_FORMAT = "yyyy-MM-dd"
    const val DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
}

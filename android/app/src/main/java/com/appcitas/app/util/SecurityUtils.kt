package com.appcitas.app.util

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecurityUtils @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val encryptedPrefs by lazy {
        EncryptedSharedPreferences.create(
            context,
            "secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun saveAccessToken(token: String) {
        encryptedPrefs.edit().putString(Constants.ACCESS_TOKEN_KEY, token).apply()
    }

    fun getAccessToken(): String? {
        return encryptedPrefs.getString(Constants.ACCESS_TOKEN_KEY, null)
    }

    fun saveRefreshToken(token: String) {
        encryptedPrefs.edit().putString(Constants.REFRESH_TOKEN_KEY, token).apply()
    }

    fun getRefreshToken(): String? {
        return encryptedPrefs.getString(Constants.REFRESH_TOKEN_KEY, null)
    }

    fun saveUserId(userId: String) {
        encryptedPrefs.edit().putString(Constants.USER_ID_KEY, userId).apply()
    }

    fun getUserId(): String? {
        return encryptedPrefs.getString(Constants.USER_ID_KEY, null)
    }

    fun saveEncryptionKey(key: String) {
        encryptedPrefs.edit().putString(Constants.ENCRYPTION_KEY_KEY, key).apply()
    }

    fun getEncryptionKey(): String? {
        return encryptedPrefs.getString(Constants.ENCRYPTION_KEY_KEY, null)
    }

    fun clearAll() {
        encryptedPrefs.edit().clear().apply()
    }

    fun isLoggedIn(): Boolean {
        return getAccessToken() != null && getUserId() != null
    }
}

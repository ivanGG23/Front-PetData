package com.example.petdata.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.auth0.android.jwt.JWT
import com.example.petdata.data.model.UserData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

class TokenManager(val context: Context) {

    companion object {
        val TOKEN_KEY = stringPreferencesKey("jwt_token")
        val USER_ID_KEY = stringPreferencesKey("user_id")
        val ROL_ID_KEY = stringPreferencesKey("rol_id")
        val NOMBRE_KEY = stringPreferencesKey("nombre")
    }

    val token: Flow<String?> = context.dataStore.data.map { it[TOKEN_KEY] }
    val rolId: Flow<String?> = context.dataStore.data.map { it[ROL_ID_KEY] }
    val AUTH_PROVIDER_KEY = stringPreferencesKey("auth_provider")

    fun getUserId(): Flow<String?> = context.dataStore.data.map { it[USER_ID_KEY] }
    suspend fun saveSession(token: String, user: UserData) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
            prefs[USER_ID_KEY] = user.user_id.toString()
            prefs[ROL_ID_KEY] = user.rol_id.toString()
            prefs[NOMBRE_KEY] = user.nombre
            prefs[AUTH_PROVIDER_KEY] = user.auth_provider ?: "local"
        }
    }

    fun getAuthProvider(): Flow<String?> = context.dataStore.data.map { it[AUTH_PROVIDER_KEY] }

    suspend fun getValidToken(): String? {
        val token = context.dataStore.data.map { it[TOKEN_KEY] }.first() ?: return null
        return try {
            val jwt = JWT(token)
            if (jwt.isExpired(0)) {
                clearSession()
                null
            } else {
                token
            }
        } catch (e: Exception) {
            clearSession()
            null
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }
}
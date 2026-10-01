package com.pairstudy.app.data.local
import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.pairstudy.app.data.model.Auth
import com.pairstudy.app.data.api.ServerConfig
import kotlinx.coroutines.flow.first
private val Context.sessionDataStore by preferencesDataStore("pairstudy_session")
class SessionStore(private val context: Context) {
    @Volatile var token: String? = null; private set
    @Volatile var userId: Long = 0; private set
    private val tokenKey = stringPreferencesKey("token")
    private val userKey = longPreferencesKey("userId")
    private val nicknameKey = stringPreferencesKey("nickname")
    private val groupKey = longPreferencesKey("groupId")
    private val serverKey = stringPreferencesKey("serverUrl")
    suspend fun load() { val p = context.sessionDataStore.data.first(); p[serverKey]?.let { ServerConfig.set(it) }; token = p[tokenKey]; userId = p[userKey] ?: 0 }
    suspend fun server(url: String) { ServerConfig.set(url); context.sessionDataStore.edit { it[serverKey] = ServerConfig.url } }
    suspend fun save(auth: Auth) {
        context.sessionDataStore.edit { p -> p[tokenKey] = auth.token; p[userKey] = auth.user.id; p[nicknameKey] = auth.user.nickname; auth.user.groupId?.let { p[groupKey] = it } ?: p.remove(groupKey) }
        token = auth.token; userId = auth.user.id
    }
    suspend fun group(id: Long?) { context.sessionDataStore.edit { if (id == null) it.remove(groupKey) else it[groupKey] = id } }
    suspend fun clear() { token = null; userId = 0; context.sessionDataStore.edit { p -> val server = p[serverKey]; p.clear(); if (server != null) p[serverKey] = server } }
}

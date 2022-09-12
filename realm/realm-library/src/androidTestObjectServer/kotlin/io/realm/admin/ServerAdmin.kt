package io.realm.admin

import android.os.SystemClock
import io.realm.log.LogLevel
import io.realm.log.RealmLog
import io.realm.mongodb.App
import io.realm.mongodb.User
import io.realm.mongodb.sync.SyncSession
import okhttp3.*
import okio.Buffer
import org.json.JSONArray
import org.json.JSONObject
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit

/**
 * Wrapper around MongoDB Realm Server Admin functions needed for tests.
 */
class ServerAdmin(private val app: App) {

    private lateinit var accessToken: String
    private lateinit var groupId: String
    private lateinit var appId: String

    private val json = MediaType.parse("application/json; charset=utf-8")
    private val baseUrl = "http://127.0.0.1:9090/api/admin/v3.0"
    private val client: OkHttpClient = OkHttpClient.Builder()
            .callTimeout(30, TimeUnit.SECONDS)
            .followRedirects(true)
            .addInterceptor { chain ->
                val request: Request = chain.request()
                if (RealmLog.getLevel() <= LogLevel.DEBUG) {
                    val sb = StringBuilder(request.method())
                    sb.append(' ')
                    sb.append(request.url())
                    sb.append('\n')
                    sb.append(request.headers())
                    if (request.body() != null) {
                        // Stripped down version of https://github.com/square/okhttp/blob/master/okhttp-logging-interceptor/src/main/java/okhttp3/logging/HttpLoggingInterceptor.java
                        // We only expect request context to be JSON.
                        val buffer = Buffer()
                        request.body()?.writeTo(buffer)
                        sb.append(buffer.readString(Charset.forName("UTF-8")))
                    }
                    RealmLog.debug("Admin HTTP Request = \n%s", sb)
                }
                chain.proceed(request)
            }
            .connectionPool(ConnectionPool(5, 5, TimeUnit.SECONDS))
            .build()

    init {
        logIn()
    }

    private fun executeRequest(builder: Request.Builder, authenticate: Boolean = true): String {
        if (authenticate) {
            builder.header("Authorization", "Bearer $accessToken")
        }
        val call = client.newCall(builder.build())
        val response = call.execute()
        val body: String = response.body()?.string() ?: ""
        val code = response.code()
        if (code < 200 || code > 299) {
            throw IllegalArgumentException("HTTP error $code : $body")
        }

        return body
    }

    // Logs the admin user so we can call other endpoints
    private fun logIn() {
        // Login
        val body = mapOf(Pair("username", "unique_user@domain.com"), Pair("password", "password"))
        var builder: Request.Builder = Request.Builder()
                .url("$baseUrl/auth/providers/local-userpass/login")
                .post(RequestBody.create(json, JSONObject(body).toString()))
        var result = JSONObject(executeRequest(builder, authenticate = false))
        accessToken = result.getString("access_token")

        // Get GroupId
        builder = Request.Builder().url("$baseUrl/auth/profile").get()
        result = JSONObject(executeRequest(builder))
        groupId = (result.getJSONArray("roles")[0] as JSONObject).getString("group_id")

        // Get Internal App Id of the requested app
        val appId = this.app.configuration.appId
        builder = Request.Builder().url("$baseUrl/groups/$groupId/apps").get()
        val response = JSONArray(executeRequest(builder))
        for (i in 0 until response.length()) {
            val appObject = response[i] as JSONObject
            if (appObject.getString("client_app_id") == appId) {
                this.appId = appObject.getString("_id")
                return
            }
        }
        throw IllegalArgumentException("Could not find app: $")
    }

    fun enableFlexibleSync() {
        var request = Request.Builder()
            .url("$baseUrl/groups/$groupId/apps/$appId/services")
            .get()
        var result = executeRequest(request, true)
        val list = JSONArray(executeRequest(request))
        for (i in 0 until list.length()) {
            val obj: JSONObject = list.getJSONObject(i)
            if (obj.getString("type") == "mongodb") {
                val serviceId = obj.getString("_id")
                val configUpdateJson = """
                        {
                            "flexible_sync": {
                                "state": "enabled", 
                                "database_name": "test_data",
                                "permissions": {
                                    "rules": {},
                                    "defaultRoles": [
                                        {
                                            "name": "all",
                                            "applyWhen": {},
                                            "read": true,
                                            "write": true
                                        }
                                    ]
                                }, 
                                "queryable_fields_names": ["owner", "name", "color", "section"]
                            }
                        }
                """.trimIndent()
                obj.put("config", JSONObject(configUpdateJson))
                request = Request.Builder()
                    .url("$baseUrl/groups/$groupId/apps/$appId/services/${serviceId}/config")
                    .patch(RequestBody.create(json, configUpdateJson))
                executeRequest(request, true)
                break;
            }
        }
    }

    fun disableUser(user: User) {
        var request = Request.Builder()
                .url("$baseUrl/groups/$groupId/apps/$appId/users/${user.id}/disable")
                .put(RequestBody.create(json, ""))
        executeRequest(request, true)
    }

    /**
     * Deletes all currently registered and pending users on MongoDB Realm.
     */
    fun deleteAllUsers() {
        deleteAllRegisteredUsers()
        deleteAllPendingUsers()
    }

    private fun deleteAllPendingUsers() {
        var request = Request.Builder()
                .url("$baseUrl/groups/$groupId/apps/$appId/user_registrations/pending_users")
                .get()
        val pendingUsers = JSONArray(executeRequest(request))
        for (i in 0 until pendingUsers.length()) {
            val user = pendingUsers[i] as JSONObject
            val loginTypes = user.getJSONArray("login_ids")
            for (j in 0 until loginTypes.length()) {
                val login = loginTypes[j] as JSONObject
                if (login.getString("id_type") == "email") {
                    deletePendingUser(login.getString("id"))
                }
            }
        }
    }

    private fun deleteAllRegisteredUsers() {
        var request = Request.Builder()
                .url("$baseUrl/groups/$groupId/apps/$appId/users")
                .get()
        val list = JSONArray(executeRequest(request))
        for (i in 0 until list.length()) {
            val o = list[i] as JSONObject
            request = Request.Builder()
                    .url("$baseUrl/groups/$groupId/apps/$appId/users/${o.getString("_id")}")
                    .delete()
            executeRequest(request)
        }
    }

    private fun deletePendingUser(email: String) {
        val request = Request.Builder()
                .url("$baseUrl/groups/$groupId/apps/$appId/user_registrations/by_email/$email")
                .delete()
        executeRequest(request)
    }

    /**
     * Determines whether or not the preconfigured reset password function is used instead
     * of sending an email.
     */
    fun setResetFunction(enabled: Boolean) {
        val providerId: String = getLocalUserPassProviderId()

        // Read current config
        var request = Request.Builder()
                .url("$baseUrl/groups/$groupId/apps/$appId/auth_providers/$providerId")
                .get()
        val authProviderConfig = JSONObject(executeRequest(request, true))
        authProviderConfig.getJSONObject("config").apply {
            put("runResetFunction", enabled)
        }
        // Change autoConfirm and update the provider
        request = Request.Builder()
                .url("$baseUrl/groups/$groupId/apps/$appId/auth_providers/$providerId")
                .patch(RequestBody.create(json, authProviderConfig.toString()))
        executeRequest(request)
    }

    private fun getLocalUserPassProviderId(): String {
        val request: Request.Builder = Request.Builder()
            .url("$baseUrl/groups/$groupId/apps/$appId/auth_providers")
            .get()
        val authProvidersListResult = JSONArray(executeRequest(request, true))
        var providerId: String? = null
        for (i in 0 until authProvidersListResult.length()) {
            val o = authProvidersListResult[i] as JSONObject
            if (o.getString("name") == "local-userpass") {
                providerId = o.getString("_id")
                break
            }
        }
        return providerId!!
    }

    /**
     * Creates an admin API key that can be used for testing purposes.
     */
    fun createServerApiKey(): String {
        val body = mapOf(Pair("name", "SERVER_KEY"))
        val builder = Request.Builder()
                .url("$baseUrl/groups/$groupId/apps/$appId/api_keys")
                .post(RequestBody.create(json, JSONObject(body).toString()))
        val result = JSONObject(executeRequest(builder))
        return result.getString("key")
    }

    private fun getMongodbServiceId(): String {
        var request = Request.Builder()
            .url("$baseUrl/groups/$groupId/apps/$appId/services")
            .get()
        val list = JSONArray(executeRequest(request))
        for (i in 0 until list.length()) {
            val obj: JSONObject = list.getJSONObject(i)
            if (obj.getString("type") == "mongodb") {
                return obj.getString("_id")
            }
        }

        error("Mongodb service not found for ${this.app.configuration.appId}")
    }

    private fun getConfig(): JSONObject {
        val serviceId = getMongodbServiceId()

        val request: Request.Builder = Request.Builder()
            .url("$baseUrl/groups/$groupId/apps/$appId/services/$serviceId/config")
            .get()

        return JSONObject(executeRequest(request, true))
    }

    private fun isRecoveryModeEnabled(): Boolean = !getConfig()
        .getJSONObject("sync")
        .optBoolean("is_recovery_mode_disabled", false)

    private fun setIsRecoveryModeEnabled(isRecoveryModeEnabled: Boolean) {
        val serviceId = getMongodbServiceId()

        val config = getConfig().apply {
            getJSONObject("sync").put("is_recovery_mode_disabled", !isRecoveryModeEnabled)
        }

        val request = Request.Builder()
            .url("$baseUrl/groups/$groupId/apps/$appId/services/${serviceId}/config")
            .patch(RequestBody.create(json, config.toString()))

        executeRequest(request, true)
    }

    private fun callTriggerResetFunction(
        userId: String
    ) {

        val functionCall = JSONObject("""
            {
                "name": "triggerClientReset",
                "arguments": ["$userId"]
            }
        """.trimIndent())

        val request = Request.Builder()
            .url("$baseUrl/groups/$groupId/apps/$appId/debug/execute_function?run_as_system=true")
            .post(RequestBody.create(json, functionCall.toString()))

        executeRequest(request, true)
    }

    // Will trigger a client reset with the recovery mode disabled if needed
    // Disabling the recovery mode would force a `RecoverOrDiscardUnsyncedChangesStrategy` to
    // discard the local changes even if they are recoverable.
    fun triggerClientReset(syncSession: SyncSession,
                           withRecoveryModeEnabled: Boolean = true,
                           block: () -> Unit)
    {
        // Later, we will restore the original status
        val wasRecoveryModeEnabled = isRecoveryModeEnabled()

        syncSession.downloadAllServerChanges()
        syncSession.stop()

        block()

        setIsRecoveryModeEnabled(withRecoveryModeEnabled)

        callTriggerResetFunction(syncSession.user.id)

        syncSession.start()
        syncSession.downloadAllServerChanges()

        setIsRecoveryModeEnabled(wasRecoveryModeEnabled)
    }
}

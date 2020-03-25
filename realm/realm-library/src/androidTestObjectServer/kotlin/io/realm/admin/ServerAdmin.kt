package io.realm.admin

import io.realm.log.LogLevel
import io.realm.log.RealmLog
import okhttp3.*
import okio.Buffer
import org.json.JSONArray
import org.json.JSONObject
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit

/**
 * Wrapper around MongoDB Realm Server Admin functions needed for tests.
 */
class ServerAdmin {

    private lateinit var accessToken: String
    private lateinit var groupId: String
    private lateinit var appId: String

    private val json = MediaType.parse("application/json; charset=utf-8")
    private val baseUrl = "http://127.0.0.1:9090/api/admin/v3.0"
    private val client: OkHttpClient = OkHttpClient.Builder()
            .callTimeout(10, TimeUnit.SECONDS)
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

        // Get Internal App Id
        builder = Request.Builder().url("$baseUrl/groups/$groupId/apps").get()
        result = JSONArray(executeRequest(builder))[0] as JSONObject
        appId = result.getString("_id")
    }

    /**
     * Toggle whether or not automatic confirmation of new users are enabled.
     */
    fun setAutomaticConfirmation(enabled: Boolean) {
        val providerId: String = getLocalUserPassProviderId()
        var request = Request.Builder()
                .url("$baseUrl/groups/$groupId/apps/$appId/auth_providers/$providerId")
                .get()
        val authProviderConfig = JSONObject(executeRequest(request, true))
        authProviderConfig.getJSONObject("config").apply {
            put("autoConfirm", enabled)
            put("emailConfirmationUrl", "http://realm.io/confirm-user")
        }
        // Change autoConfirm and update the provider
        request = Request.Builder()
                .url("$baseUrl/groups/$groupId/apps/$appId/auth_providers/$providerId")
                .patch(RequestBody.create(json, authProviderConfig.toString()))
        executeRequest(request)
    }

    fun deletePendingUser(email: String) {
        val request = Request.Builder()
                .url("$baseUrl/groups/$groupId/apps/$appId/user_registrations/by_email/$email")
                .delete()
        executeRequest(request)
    }

    /**
     * Deletes all currently registered users on MongoDB Realm.
     */
    fun deleteAllUsers() {
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
}

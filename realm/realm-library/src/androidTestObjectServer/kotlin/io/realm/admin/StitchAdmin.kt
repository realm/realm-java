package io.realm.admin

import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.lang.IllegalArgumentException
import java.util.concurrent.TimeUnit

/**
 * Wrapper around MongoDB Realm Admin functions needed for tests.
 */
class StitchAdmin {

    private lateinit var accessToken: String
    private lateinit var groupId: String
    private lateinit var appId: String

    private val json = MediaType.parse("application/json; charset=utf-8")
    private val baseUrl = "http://127.0.0.1:9090/api/admin/v3.0"
    private val client: OkHttpClient = OkHttpClient.Builder()
            .callTimeout(10, TimeUnit.SECONDS)
            .followRedirects(true)
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
        val responseBody: ResponseBody = response.body()!!

        if (response.code() < 200 || response.code() > 299) {
            throw IllegalArgumentException("HTTP error ${response.code()} : ${responseBody.string()}")
        }

        return responseBody.string()
    }

    fun logIn() {
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

    fun setAutomaticConfirmation(enabled: Boolean) {
        var request: Request.Builder = Request.Builder()
                .url("$baseUrl/groups/$groupId/apps/$appId/auth_providers")
                .get()
        val authProvidersListResult = JSONArray(executeRequest(request, true))
        var providerId: String? = null
        for (i in 0 until authProvidersListResult.length()) {
            val o = authProvidersListResult[i] as JSONObject
            if (o.getString("name") == "user-localpass") {
                providerId = o.getString("_id")
                break
            }
        }

        if (providerId != null) {
            // Read current config
            request = Request.Builder()
                    .url("$baseUrl/groups/$groupId/apps/$appId/auth_providers/$providerId")
                    .get()
            val authProviderConfig = JSONObject(executeRequest(request, true))
            authProviderConfig.getJSONObject("config").put("autoConfirm", enabled);

            // Change autoConfirm and update the provider
            var builder: Request.Builder = Request.Builder()
                    .url("$baseUrl/groups/$groupId/apps/$appId/auth_providers/$providerId")
                    .patch(RequestBody.create(json, authProvidersListResult.toString()))
            executeRequest(builder)
        }
    }

    fun deletePendingUser(email: String) {
        val request = Request.Builder()
                .url("$baseUrl/groups/$groupId/apps/$appId/user_registrations/by_email/$email")
                .delete()
        executeRequest(request)
    }
}
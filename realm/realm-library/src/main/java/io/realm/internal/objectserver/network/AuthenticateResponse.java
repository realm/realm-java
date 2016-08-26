package io.realm.internal.objectserver.network;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import io.realm.internal.Util;
import io.realm.internal.log.RealmLog;
import io.realm.objectserver.Error;
import io.realm.internal.objectserver.Token;
import okhttp3.Response;

public class AuthenticateResponse {

    private final Error error;
    private final String errorMessage;

    private final String identifier;
    private final String path;
    private final String appId;
    private final Token accessToken;
    private final Token refreshToken;

    /**
     * Helper method for creating the proper Authenticate response. This method will set the appropriate error
     * depending on any HTTP response codes or IO errors.
     */
    public static AuthenticateResponse createFrom(Response response) {
        String serverResponse;
        try {
            serverResponse = response.body().string();
        } catch (IOException e) {
            return new AuthenticateResponse(Error.IO_ERROR, "Failed to read response." + Util.getStackTrace(e));
        }
        RealmLog.d("Authenticate response: " + serverResponse);
        if (response.code() != 200) {
            try {
                JSONObject obj = new JSONObject(serverResponse);
                String type = obj.getString("type");
                String hint = obj.getString("hint");
                Error error = Error.fromAuthError(type);
                return new AuthenticateResponse(error, hint);
            } catch (JSONException e) {
                return new AuthenticateResponse(Error.UNEXPECTED_JSON_FORMAT, "Server failed with " + response.code() +
                        ", but could not parse error." + Util.getStackTrace(e));
            }
        } else {
            return new AuthenticateResponse(serverResponse);
        }
    }

    /**
     * Create a unsuccessful authentication response. This should only happen in case of network / IO problems.
     */
    public AuthenticateResponse(Error reason, String errorMessage) {
        this.error = reason;
        this.errorMessage = errorMessage;
        this.identifier = null;
        this.path = null;
        this.appId = null;
        this.accessToken = null;
        this.refreshToken = null;
    }

    /**
     * Parse a valid (200) server response. It might still result in a unsuccessful authentication attemp.
     */
    public AuthenticateResponse(String serverResponse) {
        Error error;
        String errorMessage;
        String identifier;
        String path;
        String appId;
        Token accessToken;
        Token refreshToken;
        try {
            JSONObject obj = new JSONObject(serverResponse);
            identifier = obj.getString("identity");
            path = obj.optString("path");
            appId = obj.optString("app_id"); // FIXME No longer sent?
            accessToken = obj.has("token") ? Token.from(obj) : null;
            refreshToken = obj.has("refresh") ? Token.from(obj.getJSONObject("refresh")) : null;
            error = null;
            errorMessage = null;
        } catch (JSONException ex) {
            identifier = null;
            path =  null;
            appId = null;
            accessToken = null;
            refreshToken = null;
            error = Error.BAD_SYNTAX;
            errorMessage = "Unexpected JSON: " + Util.getStackTrace(ex);
        }
        this.identifier = identifier;
        this.path = path;
        this.appId = appId;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.error = error;
        this.errorMessage = errorMessage;
    }

    public boolean isValid() {
        return (error == null);
    }
    public String getErrorMessage() {
        return errorMessage;
    }

    public Error getError() {
        return error;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getPath() {
        return path;
    }

    public String getAppId() {
        return appId;
    }

    public Token getAccessToken() {
        return accessToken;
    }

    public Token getRefreshToken() {
        return refreshToken;
    }
}

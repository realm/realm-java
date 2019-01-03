/*
 * Copyright 2016 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.realm;


import java.io.IOException;
import java.util.Locale;

import io.realm.log.RealmLog;

/**
 * This class enumerate all potential errors related to using the Object Server or synchronizing data.
 */
public enum ErrorCode {

    // See Client::Error in https://github.com/realm/realm-sync/blob/master/src/realm/sync/client.hpp
    // See https://github.com/realm/realm-object-server/blob/master/object-server/doc/problems.md
    // See https://github.com/realm/realm-sync/blob/develop/src/realm/sync/protocol.hpp

    // Catch-all
    // The underlying type and error code should be part of the error message
    UNKNOWN(Type.UNKNOWN, -1),

    // Realm Java errors (0-49)
    IO_EXCEPTION(Type.JAVA, 0, Category.RECOVERABLE),    // Some IO error while either contacting the server or reading the response
    JSON_EXCEPTION(Type.JAVA, 1),                        // JSON input could not be parsed correctly
    CLIENT_RESET(Type.OBJECT_STORE, 7),               // Client Reset required. Don't change this value without modifying io_realm_internal_OsSharedRealm.cpp

    // Realm Object Server errors (100 - 199)
    // Connection level and protocol errors.
    CONNECTION_CLOSED(Type.SYNC_CLIENT, 100),          // Connection closed (no error)
    OTHER_ERROR(Type.SYNC_CLIENT, 101),                // Other connection level error
    UNKNOWN_MESSAGE(Type.SYNC_CLIENT, 102),            // Unknown type of input message
    BAD_SYNTAX(Type.SYNC_CLIENT, 103),                 // Bad syntax in input message head
    LIMITS_EXCEEDED(Type.SYNC_CLIENT, 104),            // Limits exceeded in input message
    WRONG_PROTOCOL_VERSION(Type.SYNC_CLIENT, 105),     // Wrong protocol version (Type.SYNC_CLIENT, CLIENT)
    BAD_SESSION_IDENT(Type.SYNC_CLIENT, 106),          // Bad session identifier in input message
    REUSE_OF_SESSION_IDENT(Type.SYNC_CLIENT, 107),     // Overlapping reuse of session identifier (Type.SYNC_CLIENT, BIND)
    BOUND_IN_OTHER_SESSION(Type.SYNC_CLIENT, 108),     // Client file bound in other session (Type.SYNC_CLIENT, IDENT)
    BAD_MESSAGE_ORDER(Type.SYNC_CLIENT, 109),          // Bad input message order
    BAD_ORIGIN_FILE_IDENT(Type.SYNC_CLIENT, 110),      // Bad origin file identifier in changeset header (Type.SYNC_CLIENT, DOWNLOAD)
    BAD_SERVER_VERSION_DOWNLOAD(Type.SYNC_CLIENT, 111),// Bad server version in changeset header (Type.SYNC_CLIENT, DOWNLOAD)
    BAD_CHANGESET_DOWNLOAD(Type.SYNC_CLIENT, 112),     // Bad changeset (Type.SYNC_CLIENT, DOWNLOAD)
    BAD_REQUEST_IDENT(Type.SYNC_CLIENT, 113),          // Bad request identifier (Type.SYNC_CLIENT, MARK)
    BAD_ERROR_CODE(Type.SYNC_CLIENT, 114),             // Bad error code (Type.SYNC_CLIENT, ERROR)
    BAD_COMPRESSION(Type.SYNC_CLIENT, 115),            // Bad compression (Type.SYNC_CLIENT, DOWNLOAD)
    BAD_CLIENT_VERSION_DOWNLOAD(Type.SYNC_CLIENT, 116),// Bad last integrated client version in changeset header (Type.SYNC_CLIENT, DOWNLOAD)
    SSL_SERVER_CERT_REJECTED(Type.SYNC_CLIENT, 117),   // SSL server certificate rejected
    PONG_TIMEOUT(Type.SYNC_CLIENT, 118),               // Timeout on reception of PONG response messsage

    // Session level errors ("", 200 - 299)
    SESSION_CLOSED("", 200, Category.RECOVERABLE),      // Session closed ("", no error)
    OTHER_SESSION_ERROR("", 201, Category.RECOVERABLE), // Other session level error
    TOKEN_EXPIRED("", 202, Category.RECOVERABLE),       // Access token expired

    // Session fatal: Auth wrong. Cannot be fixed without a new User/SyncConfiguration.
    BAD_AUTHENTICATION("", 203),                        // Bad user authentication ("", BIND, REFRESH)
    ILLEGAL_REALM_PATH("", 204),                        // Illegal Realm path ("", BIND)
    NO_SUCH_PATH("", 205),                              // No such Realm ("", BIND)
    PERMISSION_DENIED("", 206),                         // Permission denied ("", BIND, REFRESH)

    // Fatal: Wrong server/client versions. Trying to sync incompatible files or the file was corrupted.
    BAD_SERVER_FILE_IDENT("", 207),                     // Bad server file identifier ("", IDENT)
    BAD_CLIENT_FILE_IDENT("", 208),                     // Bad client file identifier ("", IDENT)
    BAD_SERVER_VERSION("", 209),                        // Bad server version ("", IDENT, UPLOAD)
    BAD_CLIENT_VERSION("", 210),                        // Bad client version ("", IDENT, UPLOAD)
    DIVERGING_HISTORIES("", 211),                       // Diverging histories ("", IDENT)
    BAD_CHANGESET("", 212),                             // Bad changeset ("", UPLOAD)
    DISABLED_SESSION("", 213),                          // Disabled session
    PARTIAL_SYNC_DISABLED("", 214),                     // Partial sync disabled ("", BIND)

    // 300 - 599 Reserved for Standard HTTP error codes
    MULTIPLE_CHOICES("", 300),
    MOVED_PERMANENTLY("", 301),
    FOUND("", 302),
    SEE_OTHER("", 303),
    NOT_MODIFIED("", 304),
    USE_PROXY("", 305),
    TEMPORARY_REDIRECT("", 307),
    PERMANENT_REDIRECT("", 308),
    BAD_REQUEST("", 400),
    UNAUTHORIZED("", 401),
    PAYMENT_REQUIRED("", 402),
    FORBIDDEN("", 403),
    NOT_FOUND("", 404),
    METHOD_NOT_ALLOWED("", 405),
    NOT_ACCEPTABLE("", 406),
    PROXY_AUTHENTICATION_REQUIRED("", 407),
    REQUEST_TIMEOUT("", 408),
    CONFLICT("", 409),
    GONE("", 410),
    LENGTH_REQUIRED("", 411),
    PRECONDITION_FAILED("", 412),
    PAYLOAD_TOO_LARGE("", 413),
    URI_TOO_LONG("", 414),
    UNSUPPORTED_MEDIA_TYPE("", 415),
    RANGE_NOT_SATISFIABLE("", 416),
    EXPECTATION_FAILED("", 417),
    MISDIRECTED_REQUEST("", 421),
    UNPROCESSABLE_ENTITY("", 422),
    LOCKED("", 423),
    FAILED_DEPENDENCY("", 424),
    UPGRADE_REQUIRED("", 426),
    PRECONDITION_REQUIRED("", 428),
    TOO_MANY_REQUESTS("", 429),
    REQUEST_HEADER_FIELDS_TOO_LARGE("", 431),
    UNAVAILABLE_FOR_LEGAL_REASONS("", 451),
    INTERNAL_SERVER_ERROR("", 500),
    NOT_IMPLEMENTED("", 501),
    BAD_GATEWAY("", 502),
    SERVICE_UNAVAILABLE("", 503),
    GATEWAY_TIMEOUT("", 504),
    HTTP_VERSION_NOT_SUPPORTED("", 505),
    VARIANT_ALSO_NEGOTIATES("", 506),
    INSUFFICIENT_STORAGE("", 507),
    LOOP_DETECTED("", 508),
    NOT_EXTENDED("", 510),
    NETWORK_AUTHENTICATION_REQUIRED("", 511),

    // Realm Authentication Server response errors ("", 600 - 699)
    INVALID_PARAMETERS(Type.REALM_OBJECT_SERVER, 601),
    MISSING_PARAMETERS(Type.REALM_OBJECT_SERVER, 602),
    INVALID_CREDENTIALS(Type.REALM_OBJECT_SERVER, 611),
    UNKNOWN_ACCOUNT(Type.REALM_OBJECT_SERVER, 612),
    EXISTING_ACCOUNT(Type.REALM_OBJECT_SERVER, 613),
    ACCESS_DENIED(Type.REALM_OBJECT_SERVER, 614),
    EXPIRED_REFRESH_TOKEN(Type.REALM_OBJECT_SERVER, 615),
    INVALID_HOST(Type.REALM_OBJECT_SERVER, 616),

    // Other Realm Object Server response errors
    EXPIRED_PERMISSION_OFFER(Type.REALM_OBJECT_SERVER, 701),
    AMBIGUOUS_PERMISSION_OFFER_TOKEN(Type.REALM_OBJECT_SERVER, 702),
    FILE_MAY_NOT_BE_SHARED(Type.REALM_OBJECT_SERVER, 703),
    SERVER_MISCONFIGURATION(Type.REALM_OBJECT_SERVER, 801);

    // Generic system errors we want to enumerate specifically



    private final String type;
    private final int code;
    private final Category category;

    ErrorCode(String type, int errorCode) {
        this(type, errorCode, Category.FATAL);
    }

    ErrorCode(String type, int errorCode, Category category) {
        this.type = type;
        this.code = errorCode;
        this.category = category;
    }

    @Override
    public String

    toString() {
        return super.toString() + "(" + type + ":" + code + ")";
    }

    /**
     * Returns the numerical value for this error code. Note that an error is only uniquely
     * identified by the {@code (type:value)} pair.
     *
     * @return the error code as an unique {@code int} value.
     */
    public int intValue() {
        return code;
    }

    /**
     * Returns the getCategory of the error.
     * <p>
     * Errors come in 2 categories: FATAL, RECOVERABLE
     * <p>
     * FATAL: The session cannot be recovered and needs to be re-created. A likely cause is that the User does not
     * have access to this Realm. Check that the {@link SyncConfiguration} is correct.
     * <p>
     * RECOVERABLE: Temporary error. The session will automatically try to recover as soon as possible.
     * <p>
     *
     * @return the severity of the error.
     */
    public Category getCategory() {
        return category;
    }

    /**
     * Returns the type of error.  Note that an error is only uniquely identified by the
     * {@code (type:value)} pair.
     *
     * @return the type of error.
     */
    public String getType() {
        return type;
    }

    /**
     * Converts a native error to the appropriate Java equivalent
     *
     * @param type type of error. This is normally the C++ category.
     * @param errorCode specific code within the type
     *
     * @return the Java error representing the native error. This method will never throw, so in case
     * a Java error does not exists. {@link #UNKNOWN} will be returned.
     */
    public static ErrorCode fromNativeError(String type, int errorCode) {
        ErrorCode[] errorCodes = values();
        for (int i = 0; i < errorCodes.length; i++) {
            ErrorCode error = errorCodes[i];
            if (error.intValue() == errorCode && error.type.equals(type)) {
                return error;
            }
        }
        RealmLog.warn(String.format(Locale.US, "Unknown error code: '%s:%d'", type, errorCode));
        return UNKNOWN;
    }

    @Deprecated
    public static ErrorCode fromInt(int errorCode) {
        ErrorCode[] errorCodes = values();
        for (int i = 0; i < errorCodes.length; i++) {
            ErrorCode error = errorCodes[i];
            if (error.intValue() == errorCode) {
                return error;
            }
        }
        RealmLog.warn("Unknown error code: " + errorCode);
        return UNKNOWN;
    }

    /**
     * Helper method for mapping between {@link Exception} and {@link ErrorCode}.
     * @param exception to be mapped as an {@link ErrorCode}.
     * @return mapped {@link ErrorCode}.
     */
    public static ErrorCode fromException(Exception exception) {
        // IOException are recoverable (with exponential backoff)
        if (exception instanceof IOException) {
            return ErrorCode.IO_EXCEPTION;
        } else {
            return ErrorCode.UNKNOWN;
        }
    }

    public static class Type {
        public static final String JAVA = "java";
        public static final String OBJECT_STORE = "object-store";
        public static final String REALM_OBJECT_SERVER = "realm-object-server";
        public static final String UNKNOWN = "unknown";
        public static final String SYNC_CLIENT = "realm::sync::Client::Error";
    }


    public enum Category {
        FATAL,          // Abort session as soon as possible
        RECOVERABLE,    // Still possible to recover the session by either rebinding or providing the required information.
    }

}

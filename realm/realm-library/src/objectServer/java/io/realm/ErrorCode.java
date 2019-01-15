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

    // Realm Java errors
    IO_EXCEPTION(Type.JAVA, 0, Category.RECOVERABLE), // Some IO error while either contacting the server or reading the response
    JSON_EXCEPTION(Type.AUTH, 1),                     // JSON input could not be parsed correctly
    CLIENT_RESET(Type.PROTOCOL, 7),                   // Client Reset required. Don't change this value without modifying io_realm_internal_OsSharedRealm.cpp

    // Connection level and protocol errors from the native Sync Client
    CONNECTION_CLOSED(Type.PROTOCOL, 100, Category.RECOVERABLE),    // Connection closed (no error)
    OTHER_ERROR(Type.PROTOCOL, 101),                                // Other connection level error
    UNKNOWN_MESSAGE(Type.PROTOCOL, 102),                            // Unknown type of input message
    BAD_SYNTAX(Type.PROTOCOL, 103),                                 // Bad syntax in input message head
    LIMITS_EXCEEDED(Type.PROTOCOL, 104),                            // Limits exceeded in input message
    WRONG_PROTOCOL_VERSION(Type.PROTOCOL, 105),                     // Wrong protocol version (CLIENT)
    BAD_SESSION_IDENT(Type.PROTOCOL, 106),                          // Bad session identifier in input message
    REUSE_OF_SESSION_IDENT(Type.PROTOCOL, 107),                     // Overlapping reuse of session identifier (BIND)
    BOUND_IN_OTHER_SESSION(Type.PROTOCOL, 108),                     // Client file bound in other session (IDENT)
    BAD_MESSAGE_ORDER(Type.PROTOCOL, 109),                          // Bad input message order
    BAD_DECOMPRESSION(Type.PROTOCOL, 110),                          // Error in decompression (UPLOAD)
    BAD_CHANGESET_HEADER_SYNTAX(Type.PROTOCOL, 111),                // Bad server version in changeset header (DOWNLOAD)
    BAD_CHANGESET_SIZE(Type.PROTOCOL, 112),                         // Bad size specified in changeset header (UPLOAD)
    BAD_CHANGESETS(Type.PROTOCOL, 113),                             // Bad changesets (UPLOAD)

    // Session level errors from the native Sync Client
    SESSION_CLOSED(Type.PROTOCOL, 200, Category.RECOVERABLE),      // Session closed (no error)
    OTHER_SESSION_ERROR(Type.PROTOCOL, 201, Category.RECOVERABLE), // Other session level error
    TOKEN_EXPIRED(Type.PROTOCOL, 202, Category.RECOVERABLE),       // Access token expired

    // Session fatal: Auth wrong. Cannot be fixed without a new User/SyncConfiguration.
    BAD_AUTHENTICATION(Type.PROTOCOL, 203),                        // Bad user authentication (BIND, REFRESH)
    ILLEGAL_REALM_PATH(Type.PROTOCOL, 204),                        // Illegal Realm path (BIND)
    NO_SUCH_PATH(Type.PROTOCOL, 205),                              // No such Realm (BIND)
    PERMISSION_DENIED(Type.PROTOCOL, 206),                         // Permission denied (BIND, REFRESH)

    // Fatal: Wrong server/client versions. Trying to sync incompatible files or the file was corrupted.
    BAD_SERVER_FILE_IDENT(Type.PROTOCOL, 207),                     // Bad server file identifier (IDENT)
    BAD_CLIENT_FILE_IDENT(Type.PROTOCOL, 208),                     // Bad client file identifier (IDENT)
    BAD_SERVER_VERSION(Type.PROTOCOL, 209),                        // Bad server version (IDENT, UPLOAD)
    BAD_CLIENT_VERSION(Type.PROTOCOL, 210),                        // Bad client version (IDENT, UPLOAD)
    DIVERGING_HISTORIES(Type.PROTOCOL, 211),                       // Diverging histories (IDENT)
    BAD_CHANGESET(Type.PROTOCOL, 212),                             // Bad changeset (UPLOAD)
    DISABLED_SESSION(Type.PROTOCOL, 213),                          // Disabled session
    PARTIAL_SYNC_DISABLED(Type.PROTOCOL, 214),                     // Partial sync disabled (BIND)
    UNSUPPORTED_SESSION_FEATURE(Type.PROTOCOL, 215),               // Unsupported session-level feature
    BAD_ORIGIN_FILE_IDENT(Type.PROTOCOL, 216),                     // Bad origin file identifier (UPLOAD)

    // Sync Network Client errors.
    // TODO: All enums in here should be prefixed with `CLIENT_`, but in order to avoid
    // breaking changes, this is not the case for all of them. This should be fixed in the
    // next major release.
    // See https://github.com/realm/realm-java/issues/6387
    CLIENT_CONNECTION_CLOSED(Type.SESSION, 100),            // Connection closed (no error)
    CLIENT_UNKNOWN_MESSAGE(Type.SESSION, 101),              // Unknown type of input message
    CLIENT_LIMITS_EXCEEDED(Type.SESSION, 103),              // Limits exceeded in input message
    CLIENT_BAD_SESSION_IDENT(Type.SESSION, 104),            // Bad session identifier in input message
    CLIENT_BAD_MESSAGE_ORDER(Type.SESSION, 105),            // Bad input message order
    CLIENT_BAD_CLIENT_FILE_IDENT(Type.SESSION, 106),        // Bad client file identifier (IDENT)
    CLIENT_BAD_PROGRESS(Type.SESSION, 107),                 // Bad progress information (DOWNLOAD)
    CLIENT_BAD_CHANGESET_HEADER_SYNTAX(Type.SESSION, 108),  // Bad syntax in changeset header (DOWNLOAD)
    CLIENT_BAD_CHANGESET_SIZE(Type.SESSION, 109),           // Bad changeset size in changeset header (DOWNLOAD)
    CLIENT_BAD_ORIGIN_FILE_IDENT(Type.SESSION, 110),        // Bad origin file identifier in changeset header (DOWNLOAD)
    CLIENT_BAD_SERVER_VERSION(Type.SESSION, 111),           // Bad server version in changeset header (DOWNLOAD)
    CLIENT_BAD_CHANGESET(Type.SESSION, 112),                // Bad changeset (DOWNLOAD)
    BAD_REQUEST_IDENT(Type.SESSION, 113),                   // Bad request identifier (MARK)
    BAD_ERROR_CODE(Type.SESSION, 114),                      // Bad error code (ERROR)
    BAD_COMPRESSION(Type.SESSION, 115),                     // Bad compression (DOWNLOAD)
    BAD_CLIENT_VERSION_DOWNLOAD(Type.SESSION, 116),         // Bad last integrated client version in changeset header (DOWNLOAD)
    SSL_SERVER_CERT_REJECTED(Type.SESSION, 117),            // SSL server certificate rejected
    PONG_TIMEOUT(Type.SESSION, 118),                        // Timeout on reception of PONG respone message
    CLIENT_BAD_CLIENT_FILE_IDENT_SALT(Type.SESSION, 119),   // Bad client file identifier salt (IDENT)
    CLIENT_FILE_IDENT(Type.SESSION, 120),                   // Bad file identifier (ALLOC)
    CLIENT_CONNECT_TIMEOUT(Type.SESSION, 121),              // Sync connection was not fully established in time
    CLIENT_BAD_TIMESTAMP(Type.SESSION, 122),                // Bad timestamp (PONG)

    // 300 - 599 Reserved for Standard HTTP error codes
    MULTIPLE_CHOICES(Type.HTTP, 300),
    MOVED_PERMANENTLY(Type.HTTP, 301),
    FOUND(Type.HTTP, 302),
    SEE_OTHER(Type.HTTP, 303),
    NOT_MODIFIED(Type.HTTP, 304),
    USE_PROXY(Type.HTTP, 305),
    TEMPORARY_REDIRECT(Type.HTTP, 307),
    PERMANENT_REDIRECT(Type.HTTP, 308),
    BAD_REQUEST(Type.HTTP, 400),
    UNAUTHORIZED(Type.HTTP, 401),
    PAYMENT_REQUIRED(Type.HTTP, 402),
    FORBIDDEN(Type.HTTP, 403),
    NOT_FOUND(Type.HTTP, 404),
    METHOD_NOT_ALLOWED(Type.HTTP, 405),
    NOT_ACCEPTABLE(Type.HTTP, 406),
    PROXY_AUTHENTICATION_REQUIRED(Type.HTTP, 407),
    REQUEST_TIMEOUT(Type.HTTP, 408),
    CONFLICT(Type.HTTP, 409),
    GONE(Type.HTTP, 410),
    LENGTH_REQUIRED(Type.HTTP, 411),
    PRECONDITION_FAILED(Type.HTTP, 412),
    PAYLOAD_TOO_LARGE(Type.HTTP, 413),
    URI_TOO_LONG(Type.HTTP, 414),
    UNSUPPORTED_MEDIA_TYPE(Type.HTTP, 415),
    RANGE_NOT_SATISFIABLE(Type.HTTP, 416),
    EXPECTATION_FAILED(Type.HTTP, 417),
    MISDIRECTED_REQUEST(Type.HTTP, 421),
    UNPROCESSABLE_ENTITY(Type.HTTP, 422),
    LOCKED(Type.HTTP, 423),
    FAILED_DEPENDENCY(Type.HTTP, 424),
    UPGRADE_REQUIRED(Type.HTTP, 426),
    PRECONDITION_REQUIRED(Type.HTTP, 428),
    TOO_MANY_REQUESTS(Type.HTTP, 429),
    REQUEST_HEADER_FIELDS_TOO_LARGE(Type.HTTP, 431),
    UNAVAILABLE_FOR_LEGAL_REASONS(Type.HTTP, 451),
    INTERNAL_SERVER_ERROR(Type.HTTP, 500),
    NOT_IMPLEMENTED(Type.HTTP, 501),
    BAD_GATEWAY(Type.HTTP, 502),
    SERVICE_UNAVAILABLE(Type.HTTP, 503),
    GATEWAY_TIMEOUT(Type.HTTP, 504),
    HTTP_VERSION_NOT_SUPPORTED(Type.HTTP, 505),
    VARIANT_ALSO_NEGOTIATES(Type.HTTP, 506),
    INSUFFICIENT_STORAGE(Type.HTTP, 507),
    LOOP_DETECTED(Type.HTTP, 508),
    NOT_EXTENDED(Type.HTTP, 510),
    NETWORK_AUTHENTICATION_REQUIRED(Type.HTTP, 511),

    // Realm Authentication Server response errors (600 - 699)
    INVALID_PARAMETERS(Type.AUTH, 601),
    MISSING_PARAMETERS(Type.AUTH, 602),
    INVALID_CREDENTIALS(Type.AUTH, 611),
    UNKNOWN_ACCOUNT(Type.AUTH, 612),
    EXISTING_ACCOUNT(Type.AUTH, 613),
    ACCESS_DENIED(Type.AUTH, 614),
    EXPIRED_REFRESH_TOKEN(Type.AUTH, 615),
    INVALID_HOST(Type.AUTH, 616),
    REALM_NOT_FOUND(Type.AUTH, 617),
    UNKNOWN_USER(Type.AUTH, 618),
    WRONG_REALM_TYPE(Type.AUTH, 619), // The Realm found on the server is of different type than the one requested.

    // Other Realm Object Server response errors
    EXPIRED_PERMISSION_OFFER(Type.AUTH, 701),
    AMBIGUOUS_PERMISSION_OFFER_TOKEN(Type.AUTH, 702),
    FILE_MAY_NOT_BE_SHARED(Type.AUTH, 703),
    SERVER_MISCONFIGURATION(Type.AUTH, 801),

    // Generic system errors we want to enumerate specifically
    CONNECTION_RESET_BY_PEER(Type.CONNECTION, 104, Category.RECOVERABLE), // ECONNRESET: Connection reset by peer
    CONNECTION_SOCKET_SHUTDOWN(Type.CONNECTION, 110, Category.RECOVERABLE), // ESHUTDOWN: Can't send after socket shutdown
    CONNECTION_REFUSED(Type.CONNECTION, 111, Category.RECOVERABLE), // ECONNREFUSED: Connection refused
    CONNECTION_ADDRESS_IN_USE(Type.CONNECTION, 112, Category.RECOVERABLE), // EADDRINUSE: Address already i use
    CONNECTION_CONNECTION_ABORTED(Type.CONNECTION, 113, Category.RECOVERABLE), // ECONNABORTED: Connection aborted

    MISC_END_OF_INPUT(Type.MISC, 1), // End of input
    MISC_PREMATURE_END_OF_INPUT(Type.MISC, 2), // Premature end of input. That is, end of input at an unexpected, or illegal place in an input stream.
    MISC_DELIMITER_NOT_FOUND(Type.MISC, 3); // Delimiter not found

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
        public static final String AUTH = "auth"; // Errors from the Realm Object Server
        public static final String CONNECTION = "realm.basic_system"; // Connection/System errors from the native Sync Client
        public static final String DEPRECATED = "deprecated"; // Deprecated errors
        public static final String HTTP = "http"; // Errors from the HTTP layer
        public static final String JAVA = "java"; // Errors from the Java layer
        public static final String MISC = "realm.util.misc_ext"; // Misc errors from the native Sync Client
        public static final String PROTOCOL = "realm::sync::ProtocolError"; // Protocol level errors from the native Sync Client
        public static final String SESSION = "realm::sync::Client::Error"; // Session level errors from the native Sync Client
        public static final String UNKNOWN = "unknown"; // Catch-all category
    }


    public enum Category {
        FATAL,          // Abort session as soon as possible
        RECOVERABLE,    // Still possible to recover the session by either rebinding or providing the required information.
    }

}

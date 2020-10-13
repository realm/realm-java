/*
 * Copyright 2020 Realm Inc.
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

package io.realm.mongodb;


import java.util.Locale;

import io.realm.annotations.Beta;
import io.realm.internal.Keep;
import io.realm.internal.objectstore.OsJavaNetworkTransport;
import io.realm.log.RealmLog;
import io.realm.mongodb.sync.SyncConfiguration;

/**
 * This class enumerate all potential errors related to using the Object Server or synchronizing data.
 */
@Beta
@Keep
public enum ErrorCode {

    // See Client::Error in https://github.com/realm/realm-sync/blob/master/src/realm/sync/client.hpp#L1230
    // See https://github.com/realm/realm-sync/blob/develop/src/realm/sync/protocol.hpp
    // See https://github.com/realm/realm-object-store/blob/v10/src/sync/generic_network_transport.hpp#L47

    // Catch-all
    // The underlying type and error code should be part of the error message
    UNKNOWN(Type.UNKNOWN, -1),

    // Errors originating from Java
    // Network Transport related errors originating from Java
    NETWORK_IO_EXCEPTION(Type.JAVA, OsJavaNetworkTransport.ERROR_IO),
    NETWORK_INTERRUPTED(Type.JAVA, OsJavaNetworkTransport.ERROR_INTERRUPTED),
    NETWORK_UNKNOWN(Type.JAVA, OsJavaNetworkTransport.ERROR_UNKNOWN),

    // BSON encoding/decoding errors originating from java
    BSON_CODEC_NOT_FOUND(Type.JAVA, 1100),
    BSON_ENCODING(Type.JAVA, 1101),
    BSON_DECODING(Type.JAVA, 1102),

    // Stream serializing errors originated from Java
    EVENT_DESERIALIZING(Type.JAVA, 1200),

    // Custom Object Store errors
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
    BAD_CLIENT_FILE(Type.PROTOCOL, 217),                           // Synchronization no longer possible for client-side file
    SERVER_FILE_DELETED(Type.PROTOCOL, 218),                       // Server file was deleted while session was bound to it
    CLIENT_FILE_BLACKLISTED(Type.PROTOCOL, 219),                   // Client file has been blacklisted (IDENT)
    USER_BLACKLISTED(Type.PROTOCOL, 220),                          // User has been blacklisted (BIND)
    TRANSACT_BEFORE_UPLOAD(Type.PROTOCOL, 221),                    // Serialized transaction before upload completion
    CLIENT_FILE_EXPIRED(Type.PROTOCOL, 222),                       // Client file has expired
    USER_MISMATCH(Type.PROTOCOL, 223),                             // User mismatch for client file identifier (IDENT)
    TOO_MANY_SESSIONS(Type.PROTOCOL, 224),                         // Too many sessions in connection (BIND)
    INVALID_SCHEMA_CHANGE(Type.PROTOCOL, 225),                     // Invalid schema change (UPLOAD)

    // Sync Network Client errors.
    // See https://github.com/realm/realm-sync/blob/master/src/realm/sync/client.hpp#L1230
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
    CLIENT_BAD_REQUEST_IDENT(Type.SESSION, 113),            // Bad request identifier (MARK)
    CLIENT_BAD_ERROR_CODE(Type.SESSION, 114),               // Bad error code (ERROR)
    CLIENT_BAD_COMPRESSION(Type.SESSION, 115),              // Bad compression (DOWNLOAD)
    CLIENT_BAD_CLIENT_VERSION_DOWNLOAD(Type.SESSION, 116),  // Bad last integrated client version in changeset header (DOWNLOAD)
    CLIENT_SSL_SERVER_CERT_REJECTED(Type.SESSION, 117),     // SSL server certificate rejected
    CLIENT_PONG_TIMEOUT(Type.SESSION, 118),                 // Timeout on reception of PONG respone message
    CLIENT_BAD_CLIENT_FILE_IDENT_SALT(Type.SESSION, 119),   // Bad client file identifier salt (IDENT)
    CLIENT_FILE_IDENT(Type.SESSION, 120),                   // Bad file identifier (ALLOC)
    CLIENT_CONNECT_TIMEOUT(Type.SESSION, 121),              // Sync connection was not fully established in time
    CLIENT_BAD_TIMESTAMP(Type.SESSION, 122),                // Bad timestamp (PONG)
    CLIENT_BAD_PROTOCOL_FROM_SERVER(Type.SESSION, 123),     // Bad or missing protocol version information from server
    CLIENT_TOO_OLD_FOR_SERVER(Type.SESSION, 124),           // Protocol version negotiation failed: Client is too old for server
    CLIENT_TOO_NEW_FOR_SERVER(Type.SESSION, 125),           // Protocol version negotiation failed: Client is too new for server
    CLIENT_PROTOCOL_MISMATCH(Type.SESSION, 126),            // Protocol version negotiation failed: No version supported by both client and server
    CLIENT_BAD_STATE_MESSAGE(Type.SESSION, 127),            // Bad values in state message (STATE)
    CLIENT_MISSING_PROTOCOL_FEATURE(Type.SESSION, 128),     // Requested feature missing in negotiated protocol version
    CLIENT_BAD_SERIAL_TRANSACT_STATUS(Type.SESSION, 129),   // Bad status of serialized transaction (TRANSACT)
    CLIENT_BAD_OBJECT_ID_SUBSTITUTIONS(Type.SESSION, 130),  // Bad encoded object identifier substitutions (TRANSACT)
    CLIENT_HTTP_TUNNEL_FAILED(Type.SESSION, 131),           // Failed to establish HTTP tunnel with configured proxy


    // 300 - 599 Reserved for Standard HTTP error codes
    MULTIPLE_CHOICES(Type.HTTP, 300),
    MOVED_PERMANENTLY(Type.HTTP, 301),
    FOUND(Type.HTTP, 302),
    SEE_OTHER(Type.HTTP, 303),
    NOT_MODIFIED(Type.HTTP, 304),
    USE_PROXY(Type.HTTP, 305),
    TEMPORARY_REDIRECT(Type.HTTP, 307),
    PERMANENT_REDIRECT(Type.HTTP, 308),
    HTTP_BAD_REQUEST(Type.HTTP, 400),
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

    // MongoDB Realm Service Response codes
    INVALID_SESSION(Type.SERVICE, 2),
    USER_APP_DOMAIN_MISMATCH(Type.SERVICE, 3),
    DOMAIN_NOT_ALLOWED(Type.SERVICE, 4),
    READ_SIZE_LIMIT_EXCEEDED(Type.SERVICE, 5),
    INVALID_PARAMETER(Type.SERVICE, 6),
    MISSING_PARAMETER(Type.SERVICE, 7),
    TWILIO_ERROR(Type.SERVICE, 8),
    GCM_ERROR(Type.SERVICE, 9),
    HTTP_ERROR(Type.SERVICE, 10),
    AWS_ERROR(Type.SERVICE, 11),
    MONGODB_ERROR(Type.SERVICE, 12),
    ARGUMENTS_NOT_ALLOWED(Type.SERVICE, 13),
    FUNCTION_EXECUTION_ERROR(Type.SERVICE, 14),
    NO_MATCHING_RULE_FOUND(Type.SERVICE, 15),
    SERVICE_INTERNAL_SERVER_ERROR(Type.SERVICE, 16),
    AUTH_PROVIDER_NOT_FOUND(Type.SERVICE, 17),
    AUTH_PROVIDER_ALREADY_EXISTS(Type.SERVICE, 18),
    SERVICE_NOT_FOUND(Type.SERVICE, 19),
    SERVICE_TYPE_NOT_FOUND(Type.SERVICE, 20),
    SERVICE_ALREADY_EXISTS(Type.SERVICE, 21),
    SERVICE_COMMAND_NOT_FOUND(Type.SERVICE, 22),
    VALUE_NOT_FOUND(Type.SERVICE, 23),
    VALUE_ALREADY_EXISTS(Type.SERVICE, 24),
    VALUE_DUPLICATE_NAME(Type.SERVICE, 25),
    FUNCTION_NOT_FOUND(Type.SERVICE, 26),
    FUNCTION_ALREADY_EXISTS(Type.SERVICE, 27),
    FUNCTION_DUPLICATE_NAME(Type.SERVICE, 28),
    FUNCTION_SYNTAX_ERROR(Type.SERVICE, 29),
    FUNCTION_INVALID(Type.SERVICE, 30),
    INCOMING_WEBHOOK_NOT_FOUND(Type.SERVICE, 31),
    INCOMING_WEBHOOK_ALREADY_EXISTS(Type.SERVICE, 32),
    INCOMING_WEBHOOK_DUPLICATE_NAME(Type.SERVICE, 33),
    RULE_NOT_FOUND(Type.SERVICE, 34),
    API_KEY_NOT_FOUND(Type.SERVICE, 35),
    RULE_ALREADY_EXISTS(Type.SERVICE, 36),
    RULE_DUPLICATE_NAME(Type.SERVICE, 37),
    AUTH_PROVIDER_DUPLICATE_NAME(Type.SERVICE, 38),
    RESTRICTED_HOST(Type.SERVICE, 39),
    API_KEY_ALREADY_EXISTS(Type.SERVICE, 40),
    INCOMING_WEBHOOK_AUTH_FAILED(Type.SERVICE, 41),
    EXECUTION_TIME_LIMIT_EXCEEDED(Type.SERVICE, 42),
    NOT_CALLABLE(Type.SERVICE, 43),
    USER_ALREADY_CONFIRMED(Type.SERVICE, 44),
    USER_NOT_FOUND(Type.SERVICE, 45),
    USER_DISABLED(Type.SERVICE, 46),
    AUTH_ERROR(Type.SERVICE, 47),
    BAD_REQUEST(Type.SERVICE, 48),
    ACCOUNT_NAME_IN_USE(Type.SERVICE, 49),

    SERVICE_UNKNOWN(Type.SERVICE, -1),
    SERVICE_NONE(Type.SERVICE, 0),

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

    public static class Type {
        // FIXME Figure out where errors like 'realm::util::websocket::Error:7' are coming from
        public static final String AUTH = "auth"; // Errors from the Realm Object Server
        public static final String CONNECTION = "realm.basic_system"; // Connection/System errors from the native Sync Client
        public static final String DEPRECATED = "deprecated"; // Deprecated errors
        public static final String HTTP = "realm::app::HttpError"; // Errors from the HTTP layer
        public static final String JAVA = "realm::app::CustomError"; // Errors from the Java layer
        public static final String MISC = "realm.util.misc_ext"; // Misc errors from the native Sync Client
        public static final String SERVICE = "realm::app::ServiceError"; // MongoDB Realm Response errors
        public static final String JSON = "realm::app::JSONError"; // Errors when parsing JSON
        public static final String PROTOCOL = "realm::sync::ProtocolError"; // Protocol level errors from the native Sync Client
        public static final String SESSION = "realm::sync::Client::Error"; // Session level errors from the native Sync Client
        public static final String UNKNOWN = "unknown"; // Catch-all category
    }

    public enum Category {
        FATAL,          // Abort session as soon as possible
        RECOVERABLE,    // Still possible to recover the session by either rebinding or providing the required information.
    }

}

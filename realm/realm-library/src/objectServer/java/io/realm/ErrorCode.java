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

import io.realm.annotations.Beta;

/**
 * @Beta
 * This class enumerate all potential errors related to using the Object Server or synchronizing data.
 */
@Beta
public enum ErrorCode {

    // See https://github.com/realm/realm-sync/blob/master/doc/protocol.md

    // Realm Java errors (0-49)
    UNKNOWN(-1),                                // Catch-all
    IO_EXCEPTION(0, Category.RECOVERABLE),      // Some IO error while either contacting the server or reading the response
    JSON_EXCEPTION(1),                          // JSON input could not be parsed correctly

    // Realm Object Server errors (100 - 199)
    // Connection level and protocol errors.
    CONNECTION_CLOSED(100),          // Connection closed (no error)
    OTHER_ERROR(101),                // Other connection level error
    UNKNOWN_MESSAGE(102),            // Unknown type of input message
    BAD_SYNTAX(103),                 // Bad syntax in input message head
    LIMITS_EXCEEDED(104),            // Limits exceeded in input message
    WRONG_PROTOCOL_VERSION(105),     // Wrong protocol version (CLIENT)
    BAD_SESSION_IDENT(106),          // Bad session identifier in input message
    REUSE_OF_SESSION_IDENT(107),     // Overlapping reuse of session identifier (BIND)
    BOUND_IN_OTHER_SESSION(108),     // Client file bound in other session (IDENT)
    BAD_MESSAGE_ORDER(109),          // Bad input message order

    // Session level errors (200 - 299)
    SESSION_CLOSED(200, Category.RECOVERABLE),      // Session closed (no error)
    OTHER_SESSION_ERROR(201, Category.RECOVERABLE), // Other session level error
    TOKEN_EXPIRED(202, Category.RECOVERABLE),       // Access token expired

    // Session fatal: Auth wrong. Cannot be fixed without a new User/SyncConfiguration.
    BAD_AUTHENTICATION(203),                        // Bad user authentication (BIND, REFRESH)
    ILLEGAL_REALM_PATH(204),                        // Illegal Realm path (BIND)
    NO_SUCH_PATH(205),                              // No such Realm (BIND)
    PERMISSION_DENIED(206),                         // Permission denied (BIND, REFRESH)

    // Fatal: Wrong server/client versions. Trying to sync incompatible files or the file was corrupted.
    BAD_SERVER_FILE_IDENT(207),                     // Bad server file identifier (IDENT)
    BAD_CLIENT_FILE_IDENT(208),                     // Bad client file identifier (IDENT)
    BAD_SERVER_VERSION(209),                        // Bad server version (IDENT, UPLOAD)
    BAD_CLIENT_VERSION(210),                        // Bad client version (IDENT, UPLOAD)
    DIVERGING_HISTORIES(211),                       // Diverging histories (IDENT)
    BAD_CHANGESET(212),                             // Bad changeset (UPLOAD)

    // 300 - 599 Reserved for Standard HTTP error codes

    // Realm Authentication Server response errors (600 - 699)
    INVALID_PARAMETERS(601),
    MISSING_PARAMETERS(602),
    INVALID_CREDENTIALS(611),
    UNKNOWN_ACCOUNT(612),
    EXISTING_ACCOUNT(613),
    ACCESS_DENIED(614),
    EXPIRED_REFRESH_TOKEN(615);

    private final int code;
    private final Category category;

    ErrorCode(int errorCode) {
        this(errorCode, Category.FATAL);
    }

    ErrorCode(int errorCode, Category category) {
        this.code = errorCode;
        this.category = category;
    }

    @Override
    public String toString() {
        return super.toString() + "(" + code + ")";
    }

    /**
     * Returns the numerical value for this error code.
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
     * have access to this Realm. Check that the {@link SyncConfiguration} is correct. Any fatal error will cause
     * the session to be become {@link SessionState#STOPPED}.
     * <p>
     * RECOVERABLE: Temporary error. The session becomes {@link SessionState#UNBOUND}, but will automatically try to
     * recover as soon as possible.
     * <p>
     *
     * @return the severity of the error.
     */
    public Category getCategory() {
        return category;
    }

    public static ErrorCode fromInt(int errorCode) {
        ErrorCode[] errorCodes = values();
        for (int i = 0; i < errorCodes.length; i++) {
            ErrorCode error = errorCodes[i];
            if (error.intValue() == errorCode) {
                return error;
            }
        }
        throw new IllegalArgumentException("Unknown error code: " + errorCode);
    }

public enum Category {
        FATAL,          // Abort session as soon as possible
        RECOVERABLE    // Still possible to recover the session by either rebinding or providing the required information.
    }
}

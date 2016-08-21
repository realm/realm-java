package io.realm.internal.objectserver;

public enum Error {

        // See https://github.com/realm/realm-sync/issues/585
        // See https://github.com/realm/realm-sync/blob/master/doc/protocol.md

        // Connection level and protocol errors
        CONNECTION_CLOSED(100), // Connection closed (no error)
        OTHER_ERROR(101),       // Other connection level error
        UNKNOWN_MESSAGE(102),   // Unknown type of input message
        BAD_SYNTAX(103)         // Bad syntax in input message head
    ;
//        limits_exceeded              = 104, // Limits exceeded in input message
//        wrong_protocol_version       = 105, // Wrong protocol version (CLIENT)
//        bad_session_ident            = 106, // Bad session identifier in input message
//        reuse_of_session_ident       = 107, // Overlapping reuse of session identifier (BIND)
//        bound_in_other_session       = 108, // Client file bound in other session (IDENT)
//        bad_message_order            = 109, // Bad input message order
//
//        // Session level errors
//        session_closed               = 200, // Session closed (no error)
//        other_session_error          = 201, // Other session level error
//        token_expired                = 202, // Access token expired
//        bad_authentication           = 203, // Bad user authentication (BIND, REFRESH)
//        illegal_realm_path           = 204, // Illegal Realm path (BIND)
//        no_such_realm                = 205, // No such Realm (BIND)
//        permission_denied            = 206, // Permission denied (BIND, REFRESH)
//        bad_server_file_ident        = 207, // Bad server file identifier (IDENT)
//        bad_client_file_ident        = 208, // Bad client file identifier (IDENT)
//        bad_server_version           = 209, // Bad server version (IDENT, UPLOAD)
//        bad_client_version           = 210, // Bad client version (IDENT, UPLOAD)
//        diverging_histories          = 211, // Diverging histories (IDENT)
//        bad_changeset                = 212, // Bad changeset (UPLOAD)
//

    private final int code;

    Error(int errorCode) {
        this.code = errorCode;
    }

    @Override
    public String toString() {
        return code + " : " + super.toString();
    }

    public int errorCode() {
        return code;
    }
}

/*
 * Copyright 2023 Realm Inc.
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

package io.realm.internal;

import java.lang.annotation.Native;

import io.realm.mongodb.ErrorCode;

public class ErrorCategory {
    @Native // annotate a member to force JNI header generation
    public static final byte RLM_APP_ERROR_CATEGORY_JSON = 0;
    public static final byte RLM_APP_ERROR_CATEGORY_SERVICE = 1;
    public static final byte RLM_APP_ERROR_CATEGORY_HTTP = 2;
    public static final byte RLM_APP_ERROR_CATEGORY_CUSTOM = 3;
    public static final byte RLM_APP_ERROR_CATEGORY_CLIENT = 4;

    public static final byte RLM_SYNC_ERROR_CATEGORY_CLIENT = 5;
    public static final byte RLM_SYNC_ERROR_CATEGORY_CONNECTION = 6; // protocol
    public static final byte RLM_SYNC_ERROR_CATEGORY_SESSION = 7;

    /**
     * System error - POSIX errno, Win32 HRESULT, etc.
     */
    public static final byte RLM_SYNC_ERROR_CATEGORY_SYSTEM = 8;

    /**
     * Unknown source of error.
     */
    public static final byte RLM_SYNC_ERROR_CATEGORY_UNKNOWN = 9;

    public static String toCategory(byte value) {
        String category;
        switch (value) {
            case RLM_APP_ERROR_CATEGORY_JSON:
                category = ErrorCode.Type.JSON;
                break;
            case RLM_APP_ERROR_CATEGORY_SERVICE:
                category = ErrorCode.Type.SERVICE;
                break;
            case RLM_APP_ERROR_CATEGORY_HTTP:
                category = ErrorCode.Type.HTTP;
                break;
            case RLM_APP_ERROR_CATEGORY_CUSTOM:
                category = ErrorCode.Type.JAVA;
                break;
            case RLM_APP_ERROR_CATEGORY_CLIENT:
                category = ErrorCode.Type.APP;
                break;
            case RLM_SYNC_ERROR_CATEGORY_CLIENT:
                category = ErrorCode.Type.CLIENT;
                break;
            case RLM_SYNC_ERROR_CATEGORY_CONNECTION:
                category = ErrorCode.Type.PROTOCOL;
                break;
            case RLM_SYNC_ERROR_CATEGORY_SESSION:
                category = ErrorCode.Type.SESSION;
                break;
            case RLM_SYNC_ERROR_CATEGORY_SYSTEM:
                category = ErrorCode.Type.SYSTEM;
                break;
            default: // RLM_SYNC_ERROR_CATEGORY_UNKNOWN
                category = ErrorCode.Type.UNKNOWN;
        }

        return category;
    }
}

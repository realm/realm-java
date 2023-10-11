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
    public static final byte RLM_APP_ERROR_CATEGORY_LOGIC = 0;
    public static final byte RLM_APP_ERROR_CATEGORY_RUNTIME = 1;
    public static final byte RLM_APP_ERROR_CATEGORY_INVALID_ARGUMENT = 2;
    public static final byte RLM_APP_ERROR_CATEGORY_FILE_ACCESS = 3;
    public static final byte RLM_APP_ERROR_CATEGORY_SYSTEM = 4; // System error - POSIX errno, Win32 HRESULT, etc.
    public static final byte RLM_SYNC_ERROR_CATEGORY_APP = 5;
    public static final byte RLM_SYNC_ERROR_CATEGORY_CLIENT = 6;
    public static final byte RLM_SYNC_ERROR_CATEGORY_JSON = 7;
    public static final byte RLM_SYNC_ERROR_CATEGORY_SERVICE = 8;
    public static final byte RLM_SYNC_ERROR_CATEGORY_HTTP = 9;
    public static final byte RLM_SYNC_ERROR_CATEGORY_CUSTOM = 10;
    public static final byte RLM_SYNC_ERROR_CATEGORY_WEBSOCKET = 11;
    public static final byte RLM_SYNC_ERROR_CATEGORY_SYNC = 12;
    public static final byte RLM_SYNC_ERROR_CATEGORY_UNKNOWN = 13; // Unknown source of error. This is not a category exposed by Core.

    public static String toCategory(byte value) {
        String category;
        switch (value) {
            case RLM_APP_ERROR_CATEGORY_LOGIC:
                category = ErrorCode.Type.LOGIC;
                break;
            case RLM_APP_ERROR_CATEGORY_RUNTIME:
                category = ErrorCode.Type.RUNTIME;
                break;
            case RLM_APP_ERROR_CATEGORY_INVALID_ARGUMENT:
                category = ErrorCode.Type.INVALID_ARGUMENT;
                break;
            case RLM_APP_ERROR_CATEGORY_SYSTEM:
                category = ErrorCode.Type.SYSTEM;
                break;
            case RLM_APP_ERROR_CATEGORY_FILE_ACCESS:
                category = ErrorCode.Type.FILE_ACCESS;
                break;
            case RLM_SYNC_ERROR_CATEGORY_APP:
                category = ErrorCode.Type.APP;
                break;
            case RLM_SYNC_ERROR_CATEGORY_CLIENT:
                category = ErrorCode.Type.CLIENT;
                break;
            case RLM_SYNC_ERROR_CATEGORY_JSON:
                category = ErrorCode.Type.JSON;
                break;
            case RLM_SYNC_ERROR_CATEGORY_SERVICE:
                category = ErrorCode.Type.SERVICE;
                break;
            case RLM_SYNC_ERROR_CATEGORY_HTTP:
                category = ErrorCode.Type.HTTP;
                break;
            case RLM_SYNC_ERROR_CATEGORY_CUSTOM:
                category = ErrorCode.Type.CUSTOM;
                break;
            case RLM_SYNC_ERROR_CATEGORY_WEBSOCKET:
                category = ErrorCode.Type.WEBSOCKET;
                break;
            case RLM_SYNC_ERROR_CATEGORY_SYNC:
                category = ErrorCode.Type.SYNC;
                break;
            default:
                category = ErrorCode.Type.UNKNOWN;
        }
        return category;
    }
}

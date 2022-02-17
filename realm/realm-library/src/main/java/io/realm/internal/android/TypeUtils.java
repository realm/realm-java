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

package io.realm.internal.android;

public class TypeUtils {
    public static byte[] convertNonPrimitiveBinaryToPrimitive(Byte[] bytes) {
        byte[] transfer = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] == null) {
                throw new IllegalArgumentException("Byte arrays cannot contain null values.");
            }
            transfer[i] = bytes[i];
        }
        return transfer;
    }

    public static Byte[] convertPrimitiveBinaryToNonPrimitive(byte[] bytes) {
        Byte[] transfer = new Byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            transfer[i] = bytes[i];
        }
        return transfer;
    }
}

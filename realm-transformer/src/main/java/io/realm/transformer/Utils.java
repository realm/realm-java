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

package io.realm.transformer;

import com.android.build.gradle.BaseExtension;
import io.realm.gradle.RealmPluginExtension;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import javax.xml.bind.DatatypeConverter;
import org.gradle.api.Project;

public class Utils {

    /**
     * Encode the given string with Base64
     * @param data the string to encode
     * @return the encoded string
     * @throws UnsupportedEncodingException
     */
    public static String base64Encode(String data) throws UnsupportedEncodingException {
        return DatatypeConverter.printBase64Binary(data.getBytes("UTF-8"));
    }

    /**
     * Compute the SHA-256 hash of the given byte array
     * @param data the byte array to hash
     * @return the hashed byte array
     * @throws NoSuchAlgorithmException
     */
    public static byte[] sha256Hash(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        return messageDigest.digest(data);
    }

    /**
     * Convert a byte array to its hex-string
     * @param data the byte array to convert
     * @return the hex-string of the byte array
     */
    public static String hexStringify(byte[] data) {
        StringBuilder stringBuilder = new StringBuilder();
        for (byte singleByte : data) {
            stringBuilder.append(Integer.toString((singleByte & 0xff) + 0x100, 16).substring(1));
        }

        return stringBuilder.toString();
    }

    public static boolean isSyncEnabled(Project project) {
        RealmPluginExtension realmExtension = (RealmPluginExtension) project.getExtensions().findByName("realm");
        return realmExtension != null && realmExtension.isSyncEnabled();
    }

}

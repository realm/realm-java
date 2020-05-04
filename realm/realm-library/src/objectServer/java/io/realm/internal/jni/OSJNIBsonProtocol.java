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

package io.realm.internal.jni;

import org.bson.BsonDocument;
import org.bson.BsonValue;

/**
 * Protocol for passing {@link BsonValue}s to JNI.
 *
 * For now this just encapsulated the BSON value in a document with key {@value VALUE}. This
 * overcomes the shortcoming of {@code org.bson.JsonWrite} not being able to serialize single values.
 */
public class OSJNIBsonProtocol {

    private static final String VALUE = "value";

    public static String encode(BsonValue bsonValue) {
        BsonDocument document = new BsonDocument();
        document.append(VALUE, bsonValue);
        return document.toJson();
    }

    public static BsonValue decode(String string) {
        BsonDocument document = BsonDocument.parse(string);
        return document.get("value");
    }

}

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

import org.bson.BsonValue;
import org.bson.codecs.Decoder;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.Encoder;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.json.JsonMode;
import org.bson.json.JsonReader;
import org.bson.json.JsonWriter;
import org.bson.json.JsonWriterSettings;

import java.io.StringReader;
import java.io.StringWriter;

/**
 * Protocol for passing {@link BsonValue}s to JNI.
 *
 * For now this just encapsulated the BSON value in a document with key {@value VALUE}. This
 * overcomes the shortcoming of {@code org.bson.JsonWrite} not being able to serialize single values.
 */
public class JniBsonProtocol {

    private static final String VALUE = "value";

    private static JsonWriterSettings writerSettings = JsonWriterSettings.builder()
                .outputMode(JsonMode.EXTENDED)
                .build();

    public static <T> String encode(T value, CodecRegistry registry) {
        return encode(value, (Encoder<T>)registry.get(value.getClass()));
    }

    public static <T> String encode(T value, Encoder<T> encoder) {
        StringWriter stringWriter = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(stringWriter, writerSettings);
        jsonWriter.writeStartDocument();
        jsonWriter.writeName(VALUE);
        encoder.encode(jsonWriter, value, EncoderContext.builder().build());
        jsonWriter.writeEndDocument();
        return stringWriter.toString();
    }

    public static <T> T decode(String string, Class<T> clz, CodecRegistry registry) {
        return decode(string, registry.get(clz));
    }

    public static <T> T decode(String string, Decoder<T> decoder) {
        StringReader stringReader = new StringReader(string);
        JsonReader jsonReader = new JsonReader(stringReader);
        jsonReader.readStartDocument();
        jsonReader.readName(VALUE);
        T value = decoder.decode(jsonReader, DecoderContext.builder().build());
        jsonReader.readEndDocument();
        return value;
    }

    // Only to enable testing JNI roundtrip
    static <S, T> T test(S value, Class<T> clz, CodecRegistry registry) {
        String bsonInput = encode(value, registry);
        String bsonOutput = nativeTest(bsonInput);
        String y = bsonInput;
        return decode(bsonOutput, clz, registry);
    }

    // Only to enable testing JNI roundtrip
    private static native String nativeTest(String input);

}

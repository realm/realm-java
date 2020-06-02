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
import org.bson.codecs.Codec;
import org.bson.codecs.Decoder;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.Encoder;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecConfigurationException;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.json.JsonMode;
import org.bson.json.JsonReader;
import org.bson.json.JsonWriter;
import org.bson.json.JsonWriterSettings;

import java.io.StringReader;
import java.io.StringWriter;

import io.realm.ErrorCode;
import io.realm.ObjectServerError;

/**
 * Protocol for passing {@link BsonValue}s to JNI.
 * <p>
 * For now this just encapsulated the BSON value in a document with key {@value VALUE}. This
 * overcomes the shortcoming of {@code org.bson.JsonWrite} not being able to serialize single values.
 */
public class JniBsonProtocol {

    private static final String VALUE = "value";

    private static JsonWriterSettings writerSettings = JsonWriterSettings.builder()
            .outputMode(JsonMode.EXTENDED)
            .build();

    public static <T> String encode(T value, CodecRegistry registry) {
        try {
            Codec<?> codec = registry.get(value.getClass());
            return encode(value, (Encoder<T>) codec);
        } catch (Exception e) {
            throw new ObjectServerError(ErrorCode.BSON_CODEC_NOT_FOUND, "Could not resolve decoder for " + value.getClass().getSimpleName(), e);
        }
    }

    public static <T> String encode(T value, Encoder<T> encoder) {
        try {
            StringWriter stringWriter = new StringWriter();
            JsonWriter jsonWriter = new JsonWriter(stringWriter, writerSettings);
            jsonWriter.writeStartDocument();
            jsonWriter.writeName(VALUE);
            encoder.encode(jsonWriter, value, EncoderContext.builder().build());
            jsonWriter.writeEndDocument();
            return stringWriter.toString();
        } catch (CodecConfigurationException e) {
            throw new ObjectServerError(ErrorCode.BSON_CODEC_NOT_FOUND, "Could not resolve encoder for value of class " + value.getClass().getSimpleName(), e);
        } catch (Exception e) {
            throw new ObjectServerError(ErrorCode.BSON_ENCODING, "Error encoding value", e);
        }
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

    public static <T> Decoder<T> decoder(CodecRegistry codecRegistry, Class<T> clz) {
        try {
            return codecRegistry.get(clz);
        } catch (Exception e) {
            throw new ObjectServerError(ErrorCode.BSON_CODEC_NOT_FOUND, "Could not resolve decoder for " + clz.getName(), e);
        }
    }
}

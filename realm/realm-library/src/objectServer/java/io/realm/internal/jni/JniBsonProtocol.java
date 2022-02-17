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

import io.realm.mongodb.ErrorCode;
import io.realm.mongodb.AppException;

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
        // catch possible missing codecs before the actual encoding
        return encode(value, (Encoder<T>) getCodec(value.getClass(), registry));
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
            // same exception as in the guard above, but needed here as well nonetheless as the
            // result might be wrapped inside an iterable or a map and the codec for the end type
            // might be missing
            throw new AppException(ErrorCode.BSON_CODEC_NOT_FOUND, "Could not resolve encoder for end type", e);
        } catch (Exception e) {
            throw new AppException(ErrorCode.BSON_ENCODING, "Error encoding value", e);
        }
    }

    public static <T> T decode(String string, Class<T> clz, CodecRegistry registry) {
        // catch possible missing codecs before the actual decoding
        return decode(string, getCodec(clz, registry));
    }

    public static <T> T decode(String string, Decoder<T> decoder) {
        try {
            StringReader stringReader = new StringReader(string);
            JsonReader jsonReader = new JsonReader(stringReader);
            jsonReader.readStartDocument();
            jsonReader.readName(VALUE);
            T value = decoder.decode(jsonReader, DecoderContext.builder().build());
            jsonReader.readEndDocument();
            return value;
        } catch (CodecConfigurationException e) {
            // same exception as in the guard above, but needed here as well nonetheless as the
            // result might be wrapped inside an iterable or a map and the codec for the end type
            // might be missing
            throw new AppException(ErrorCode.BSON_CODEC_NOT_FOUND, "Could not resolve decoder for end type" + string, e);
        } catch (Exception e) {
            throw new AppException(ErrorCode.BSON_DECODING, "Error decoding value " + string, e);
        }
    }

    public static <T> Codec<T> getCodec(Class<T> clz, CodecRegistry registry) {
        try {
            return registry.get(clz);
        } catch (CodecConfigurationException e) {
            throw new AppException(ErrorCode.BSON_CODEC_NOT_FOUND, "Could not resolve codec for " + clz.getSimpleName(), e);
        }
    }
}

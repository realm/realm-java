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

package io.realm.internal.util;

import org.bson.BsonArray;
import org.bson.BsonBoolean;
import org.bson.BsonInt32;
import org.bson.BsonInt64;
import org.bson.BsonString;
import org.bson.BsonType;
import org.bson.BsonValue;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;

/**
 * A <i>BSON converter</i> to handle conversion between native Java types and BSON values.
 */
public class BsonConverter {

    /**
     * Converts value object to BSON value based on type.
     *
     * Converts primitive boxed types to the equivalent BSON equivalent value object.
     *
     * {@link BsonValue} objects are left as is.
     *
     * @param value The object to convert.
     * @return BSON value representation of the origin value object.
     *
     * @throws IllegalArgumentException If the object could not be mapped to a BSON type.
     */
    // FIXME Review supported types...any obvious types missing?
    public static BsonValue to(Object value) {
        // Just leave BsonValues as is
        if (value instanceof BsonValue) {
            return (BsonValue) value;
        } else if (value instanceof Boolean) {
            return new BsonBoolean((Boolean) value);
        } else if (value instanceof Integer) {
            return new BsonInt32((Integer) value);
        } else if (value instanceof Long) {
            return new BsonInt64((Long) value);
        } else if (value instanceof String){
            return new BsonString((String) value);
        }
        throw new IllegalArgumentException("Conversion to BSON value not supported for " + value.getClass().getName());
    }

    /**
     * Converts a list of objects to BSON values.
     *
     * @param value List of value objects to convert.
     * @return A list of BSON values of the converted input arguments.
     *
     * @throws IllegalArgumentException If any of the value objects could not be converted to a
     * BSON type.
     *
     * @see #to(Object)
     */
    public static BsonArray to(Object... value) {
        ArrayList result = new ArrayList();
        for (Object o1 : value) {
            result.add(to(o1));
        }
        return new BsonArray(result);
    }

    /**
     *
     *
     * @param clz
     * @param value
     * @param <T>
     * @return
     *
     */
    // FIXME Would we rather return null? Would maybe make it cleaner to use with
    //  functions.callFunctionTyped("sum", BsonString::class.java, "Realm")?.value
    //  but would also silently hide if expectations of type is wrong
    private static <T extends BsonValue> T fromToBson(Class<T> clz, BsonValue value) {
        if (clz.isInstance(value)) {
            return (T) value;
        } else {
            throw new IllegalArgumentException("Cannot convert " + value + " to " + clz.getName());
        }
    }

    /**
     * Unwrap BSON values for types that has to plain Java type .
     *
     * @param value The BSON value to convert.
     * @param <T> The requested result type of the conversion.
     * @return The converted value object corresponding to the given {@code value}.
     *
     * @throws IllegalArgumentException if not able to convert the value to the requested type.
     * @throws ClassCastException if the BsonValue cannot be converted to the requested type
     *  parameters.
     */
    // FIXME Do we want this at all. Or at least review supported types...should we allow
    //  unwrapping of all BsonValues as in:
    //    BsonConverter.from(ObjectId::class.java, bsonObjectId)
    public static <T> T from(Class<T> clz, BsonValue value) {
        Object result = null;

        if (BsonValue.class.isAssignableFrom(clz)) {
            if (clz.isInstance(value)) {
                return (T) value;
            } else {
                throw new ClassCastException("Cannot convert " + value + " to " + clz.getName());
            }
        }
        BsonType bsonType = value.getBsonType();
        switch (bsonType) {
//            case END_OF_DOCUMENT:
//                break;
            case DOUBLE:
                result = value.asDouble().getValue();
                break;
            case STRING:
                result = value.asString().getValue();
                break;
//            case DOCUMENT:
//                break;
//            case ARRAY:
//                break;
//            case BINARY:
//                break;
//            case UNDEFINED:
//                break;
//            case OBJECT_ID:
//                 FIXME Do we need this...is so, I guess it should be consistently unwrapping all
//                  other BsonValue's too
//                result = value.asObjectId().getValue();
//                break;
            case BOOLEAN:
                result = value.asBoolean().getValue();
                break;
//            case DATE_TIME:
//                break;
//            case NULL:
//                break;
//            case REGULAR_EXPRESSION:
//                break;
//            case DB_POINTER:
//                break;
//            case JAVASCRIPT:
//                break;
//            case SYMBOL:
//                break;
//            case JAVASCRIPT_WITH_SCOPE:
//                break;
            case INT32:
                result = value.asInt32().getValue();
                break;
//            case TIMESTAMP:
//                break;
            case INT64:
                result = value.asInt64().getValue();
                break;
//            case DECIMAL128:
//                result = value.asDecimal128().getValue();
//                break;
//            case MIN_KEY:
//                break;
//            case MAX_KEY:
//                break;
            default:
                // FIXME
                throw new IllegalArgumentException("Not able to convert " + value + " to " + clz.getName());
        }
        if (clz.isInstance(result)) {
            return (T) result;
        } else  {
            throw new IllegalArgumentException("Not able to convert " + value + " to " + clz.getName());
        }
    }

}

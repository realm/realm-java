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

import org.bson.BsonInt32;
import org.bson.BsonInt64;
import org.bson.BsonString;
import org.bson.BsonType;
import org.bson.BsonValue;

import java.util.ArrayList;
import java.util.List;

/**
 * A <i>BSON converter</i> to handle conversion between native Java types and BSON values.
 */
public class BsonConverter {

    /**
     * Converts value object to BSON value based on type.
     *
     * @param value The object to convert.
     * @return BSON value representation of the origin value object.
     *
     * @throws UnsupportedOperationException If the object could not be mapped to a BSON type.
     */
    // FIXME Review supported types
    public static BsonValue to(Object value) {
        // Just leave BsonValues as is
        if (value instanceof BsonValue) {
            return (BsonValue) value;
        } else if (value instanceof Integer) {
            return new BsonInt32((Integer) value);
        } else if (value instanceof Long) {
            return new BsonInt64((Long) value);
        } else if (value instanceof String){
            return new BsonString((String) value);
        }
        throw new UnsupportedOperationException("Conversion to BSON value not supported for " + value.getClass().getSimpleName() );
    }

    /**
     * Converts a list of objects to BSON values.
     *
     * @param value List of value objects to convert.
     * @return The
     *
     * @throws UnsupportedOperationException If any of the value objects could not be converted to a
     * BSON type.
     *
     * @see #to(Object)
     */
    public static List<BsonValue> to(Object... value) {
        ArrayList result = new ArrayList();
        for (Object o1 : value) {
            result.add(to(o1));
        }
        return result;
    }

    /**
     * Converts a BSON value to a plan Java type.
     *
     * @param value The BSON value to convert.
     * @param <T> The request result type of the conversion.
     * @return The converted value object corresponding to the given {@code value}.
     *
     * @throws UnsupportedOperationException if the value is a BsonValue type not handled by Realm.
     * @throws ClassCastException if the BsonValue cannot be converted to the requested type
     *  parameters.
     */
    // FIXME Review supported types
    public static <T> T from(Class<T> clz, BsonValue value) {
        Object result = null;

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
            case OBJECT_ID:
                break;
            case BOOLEAN:
                break;
            case DATE_TIME:
                break;
            case NULL:
                break;
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
            case TIMESTAMP:
                break;
            case INT64:
                result = value.asInt64().getValue();
                break;
            case DECIMAL128:
                result = value.asDecimal128().getValue();
                break;
//            case MIN_KEY:
//                break;
//            case MAX_KEY:
//                break;
            default:
                // FIXME
                throw new UnsupportedOperationException("Unsupported BSON type");
        }
        if (clz.isInstance(result)) {
            return (T) result;
        } else  {
            throw new UnsupportedOperationException("Not able to convert " + value + " to " + clz.getSimpleName());
        }
    }

    // FIXME Review supported types
    // FIXME Optimize to static map o.a.?
    public static <T> Class<? extends BsonValue> bsontype(Class<T> clz) {
        if (clz == Integer.class) {
            return BsonInt32.class;
        } else if (clz == String.class) {
            return BsonString.class;
        }
        return null;
    }

}

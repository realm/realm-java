/*
 * Copyright 2015 Realm Inc.
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

public class Util {
    /**
     * Basic validation the parameters used by multi-column sorting methods. It will throw an
     * exception in case of an error.
     *
     * @param fieldNames list of field names
     * @param sortAscending list of sorting orders
     */
    public static void validateMultiSortParameters(String[] fieldNames, boolean[] sortAscending) {
        if (fieldNames == null) {
            throw new IllegalArgumentException("fieldNames must be provided.");
        } else if (sortAscending == null) {
            throw new IllegalArgumentException("sortAscending must be provided.");
        } else if (fieldNames.length == 0) {
            throw new IllegalArgumentException("You must provide at least one field name.");
        } else if (sortAscending.length == 0) {
            throw new IllegalArgumentException("You must provide at least one sort order.");
        } else if (fieldNames.length != sortAscending.length) {
            throw new IllegalArgumentException(String.format("Number of field names (%d) and sort orders (%d) do not match.", fieldNames.length, sortAscending.length));
        }
    }
}

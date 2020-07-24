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

package io.realm.mongodb.mongo.remote;

public enum OperationType {
    INSERT, DELETE, REPLACE, UPDATE, UNKNOWN;

    /**
     * Returns the appropriate local operation type enum value vased on the remote operation type
     * string from a change stream event.
     *
     * @param type the string description of the operation type.
     * @return the operation type.
     */
    public static OperationType fromRemote(final String type) {
        switch (type) {
            case "insert":
                return INSERT;
            case "delete":
                return DELETE;
            case "replace":
                return REPLACE;
            case "update":
                return UPDATE;
            default:
                return UNKNOWN;
        }
    }

    /**
     * Converts this operation to the remote string representation of the operation as
     * represented in a {@link ChangeEvent} from a remote cluster.
     *
     * @return the remote representation of the update operation.
     */
    public String toRemote() {
        switch (this) {
            case INSERT:
                return "insert";
            case DELETE:
                return "delete";
            case REPLACE:
                return "replace";
            case UPDATE:
                return "update";
            default:
                return "unknown";
        }
    }
}
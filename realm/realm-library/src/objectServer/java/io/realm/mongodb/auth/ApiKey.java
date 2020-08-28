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
package io.realm.mongodb.auth;

import org.bson.types.ObjectId;

import javax.annotation.Nullable;

import io.realm.annotations.Beta;
import io.realm.mongodb.User;

/**
 * Class representing an API key for a {@link User}. An API can be used to represent the
 * user when logging instead of using email and password.
 * <p>
 * These keys are created or fetched through {@link ApiKeyAuth#create(String)} or the various
 * {@code fetch}-methods.
 * <p>
 * Note that a keys {@link #value} is only available when the key is created, after that it is not
 * visible. So anyone creating an API key is responsible for storing it safely after that.
 */
@Beta
public class ApiKey {
    private final ObjectId id;
    private final String value;
    private final String name;
    private final boolean enabled;

    ApiKey(ObjectId id, @Nullable String value, String name, boolean enabled) {
        this.id = id;
        this.value = value;
        this.name = name;
        this.enabled = enabled;
    }

    /**
     * Returns the unique identifier for this key.
     *
     * @return the id, uniquely identifying the key.
     */
    public ObjectId getId() {
        return id;
    }

    /**
     * Returns this keys value. This value is only returned when the key is created. After that
     * the value is no longer visible.
     *
     * @return the value of this key. Is only returned when the key is created.
     */
    @Nullable
    public String getValue() {
        return value;
    }

    /**
     * Returns the name of this key.
     *
     * @return the name of the key.
     */
    public String getName() {
        return name;
    }


    /**
     * Returns whether or not this key is currently enabled.
     *
     * @return if the key is enabled or not.
     */
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ApiKey that = (ApiKey) o;

        if (enabled != that.enabled) return false;
        if (!id.equals(that.id)) return false;
        if (!value.equals(that.value)) return false;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + value.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + (enabled ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ApiKey{" +
                "id=" + id +
                ", value='" + value + '\'' +
                ", name='" + name + '\'' +
                ", enabled=" + enabled +
                '}';
    }
}

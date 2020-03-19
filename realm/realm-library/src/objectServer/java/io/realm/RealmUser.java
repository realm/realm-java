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
package io.realm;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import io.realm.internal.objectstore.OsSyncUser;
import io.realm.internal.util.Pair;

/**
 * FIXME
 */
public class RealmUser {

    private final OsSyncUser osUser;

    /**
     * FIXME
     */
    enum UserType {
        NORMAL("normal"),
        SERVER("server"),
        UNKNOWN("unknown");

        private final String key;

        UserType(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }

    RealmUser(long nativePtr) {
        this.osUser = new OsSyncUser(nativePtr);
    }

    /**
     * FIXME
     * @return
     */
    public String getId() {
        return osUser.getIdentity();
    }

    /**
     * FIXME
     * @return
     */
    public String getName() {
        return osUser.nativeGetName();
    }

    /**
     * FIXME
     * @return
     */
    @Nullable
    public String getEmail() {
        return osUser.getEmail();
    }

    /**
     * FIXME
     * @return
     */
    @Nullable
    public String getPictureUrl() {
        return osUser.getPictureUrl();
    }

    /**
     * FIXME
     * @return
     */
    @Nullable
    public String getFirstName() {
        return osUser.getFirstName();
    }

    /**
     * FIXME
     * @return
     */
    @Nullable
    public String getLastName() {
        return osUser.getLastName();
    }

    /**
     * FIXME
     * @return
     */
    @Nullable
    public String getGender() {
        return osUser.getGender();
    }

    /**
     * FIXME
     * @return
     */
    @Nullable
    public String getBirthday() {
        return osUser.getBirthday();
    }

    /**
     * FIXME
     * @return
     */
    @Nullable
    public Long getMinAge() {
        String minAge = osUser.getMinAge();
        return (minAge == null) ? null : Long.parseLong(minAge);
    }

    /**
     * FIXME
     * @return
     */
    @Nullable
    public Long getMaxAge() {
        String maxAge = osUser.getMaxAge();
        return (maxAge == null) ? null : Long.parseLong(maxAge);
    }

    /**
     * FIXME
     * @return
     */
    public List<RealmUserIdentity> getIdentities() {
        Pair<String, String>[] osIdentities = osUser.getIdentities();
        List<RealmUserIdentity> identities = new ArrayList<>(osIdentities.length);
        for (int i = 0; i < osIdentities.length; i++) {
            Pair<String, String> data = osIdentities[i];
            identities.add(new RealmUserIdentity(data.first, data.second));
        }
        return identities;
    }

    /**
     * FIXME
     * @return
     */

    public String getAccessToken() {
        return osUser.getAccessToken();
    }

    /**
     * FIXME
     * @return
     */
    public String getRefreshToken() {
        return osUser.getRefreshToken();
    }

}

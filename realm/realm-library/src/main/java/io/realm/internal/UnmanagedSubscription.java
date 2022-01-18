/*
 * Copyright 2022 Realm Inc.
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
package io.realm.internal;

import java.util.Date;

import javax.annotation.Nullable;

import io.realm.RealmQuery;
import io.realm.mongodb.sync.Subscription;

/**
 * Class that handles unmanaged subscriptions. Required as we need to track a realm query ptr.
 */
public class UnmanagedSubscription implements Subscription {

    private final Date createdAt;
    private final Date updatedAt;
    @Nullable
    private final String name;
    private final String objectType;
    private final String queryDesc;
    private final long queryPtr;

    public UnmanagedSubscription(@Nullable String name, RealmQuery query) {
        this.createdAt = null;
        this.updatedAt = null;
        this.name = name;
        this.objectType = query.getTypeQueried();
        this.queryDesc = query.getDescription();
        this.queryPtr = query.getQueryPointer();
    }

    @Override
    public Date getCreatedAt() {
        return null;
    }

    @Override
    public Date getUpdatedAt() {
        return null;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getObjectType() {
        return objectType;
    }

    @Override
    public String getQuery() {
        return queryDesc;
    }

    public long getQueryPointer() {
        return queryPtr;
    }
}

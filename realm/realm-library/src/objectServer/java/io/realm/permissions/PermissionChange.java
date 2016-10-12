/*
 * Copyright 2016 Realm Inc.
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
package io.realm.permissions;

import java.util.Date;
import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * TODO
 */
public class PermissionChange extends RealmObject {

    // Base fields
    @PrimaryKey
    @Required
    private String id = UUID.randomUUID().toString();
    @Required
    private Date createdAt;
    @Required
    private Date updatedAt;
    private Integer statusCode; // nil=not processed, 0=success, >0=error
    private String statusMessage;

    @Required
    private String realmUrl;
    @Required
    private String userId;
    private boolean mayRead;
    private boolean mayWrite;
    private boolean mayManage;

    public PermissionChange() {
        // Default constructor required by Realm
    }

    /**
     * TODO
     */
    public PermissionChange(String realmUrl, String userId, boolean mayRead, boolean mayWrite, boolean mayManage) {
        this.realmUrl = realmUrl;
        this.userId = userId;
        this.mayRead = mayRead;
        this.mayWrite = mayWrite;
        this.mayManage = mayManage;
    }
}

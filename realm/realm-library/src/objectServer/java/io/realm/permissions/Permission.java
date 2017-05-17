/*
 * Copyright 2017 Realm Inc.
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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.UUID;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import io.realm.RealmModel;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;


/**
 * FIXME Javadoc
 * Public description of what a a Permission is and how it works
 */
public class Permission extends RealmObject {

    /**
     * FIXME: Public description of what a permission is and how it works
     */
    public enum AccessLevel {
        /**
         * FIXME: add description
         */
        NONE(false, false, false),

        /**
         * FIXME: add description
         */
        READ(true, false, false),

        /**
         * FIXME: add description
         */
        WRITE(true, true, false),

        /**
         * FIXME: add description
         */
        ADMIN(true, true, true);

        private final boolean mayRead;
        private final boolean mayWrite;
        private final boolean mayManage;

        AccessLevel(boolean mayRead, boolean mayWrite, boolean mayManage) {
            this.mayRead = mayRead;
            this.mayWrite = mayWrite;
            this.mayManage = mayManage;
        }

        public boolean mayRead() {
            return mayRead;
        }

        public boolean mayWrite() {
            return mayWrite;
        }

        public boolean mayManage() {
            return mayManage;
        }
    }

    @Required
    private String userId;
    @Required
    private String path;
    private boolean mayRead;
    private boolean mayWrite;
    private boolean mayManage;
    @Required
    private Date updatedAt;

    public Permission() {
    }

    public String getUserId() {
        return userId;
    }

    public String getPath() {
        return path;
    }

    public boolean isMayRead() {
        return mayRead;
    }

    public boolean isMayWrite() {
        return mayWrite;
    }

    public boolean isMayManage() {
        return mayManage;
    }

    @SuppressFBWarnings({"EI_EXPOSE_REP"})
    public Date getUpdatedAt() {
        return updatedAt;
    }
}

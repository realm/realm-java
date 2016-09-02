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

package io.realm.examples.objectserver;

import android.app.Application;

import io.realm.objectserver.util.SharedPrefsUserStore;
import io.realm.objectserver.util.UserStore;

public class MyApplication extends Application {

    public static final String OBJECT_SERVER_IP = "192.168.1.21";
    public static final String APP_USER_KEY = "defaultAppUser";
    public static UserStore USER_STORE;

    @Override
    public void onCreate() {
        super.onCreate();
        USER_STORE = new SharedPrefsUserStore(this);
    }
}

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

package io.realm.objectserver.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import io.realm.Realm;
import io.realm.SyncConfiguration;
import io.realm.SyncUser;
import io.realm.objectserver.model.TestObject;
import io.realm.objectserver.utils.Constants;
import io.realm.objectserver.utils.UserFactory;

/**
 * Open a sync Realm on a different process, then send one commit.
 */
public class SendsALot extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(getApplicationContext());
        SyncUser user = UserFactory.createDefaultUser(Constants.AUTH_URL);
        String realmUrl = Constants.SYNC_SERVER_URL_2;
        final SyncConfiguration syncConfig = new SyncConfiguration.Builder(user, realmUrl)
                .name(SendsALot.class.getSimpleName())
                .build();
        Realm.deleteRealm(syncConfig);
        Realm realm = Realm.getInstance(syncConfig);

        realm.beginTransaction();

        for (int i = 0; i < 100; i++) {
            TestObject testObject = realm.createObject(TestObject.class);
            testObject.setIntProp(i);
            testObject.setStringProp("property " + i);
        }
        realm.commitTransaction();

        realm.close();//FIXME the close may not give a chance to the sync client to process/upload the changeset
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

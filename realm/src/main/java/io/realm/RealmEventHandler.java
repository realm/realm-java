/*
 * Copyright 2014 Realm Inc.
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

import java.lang.ref.WeakReference;

public class RealmEventHandler implements Runnable {

    private WeakReference<Realm> realmRef;

    public RealmEventHandler(Realm realm) {
        realmRef = new WeakReference<Realm>(realm);
    }

    @Override
    public void run() {
        Realm realm = realmRef.get();
        if(realm != null && realm.runEventHandler) {
            if (realm.hasChanged()) {
                realm.sendNotifications();
            }
        }
    }
}

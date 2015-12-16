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

package io.realm.examples.threads;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.widget.Toast;

import io.realm.Realm;
import io.realm.examples.threads.model.Person;

public class ReceivingService extends IntentService {

    public ReceivingService() {
        super("ReceivingService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent.getExtras() != null) {
            String personId = intent.getStringExtra("person_id");
            Realm realm = Realm.getDefaultInstance();
            Person person = realm.where(Person.class).equalTo("id", personId).findFirst();
            final String output = person.toString();
            new Handler(getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "Loaded Person from intent service: " + output, Toast.LENGTH_LONG).show();
                }
            });
            realm.close();
        }
    }
}

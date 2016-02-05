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

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.TextView;

import io.realm.Realm;
import io.realm.examples.threads.model.Person;

public class ReceivingActivity extends ActionBarActivity {

    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiving);

        realm = Realm.getDefaultInstance();

        if (getIntent() != null) {
            String personId = getIntent().getStringExtra("person_id");
            if (personId != null) {
                Person person = realm.where(Person.class).equalTo("id", personId).findFirst();
                ((TextView) findViewById(R.id.received_content)).setText(String.format("Received person_id and loaded: %s", person.toString()));
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}

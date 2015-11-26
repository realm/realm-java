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
package io.realm.examples.realmmultiprocessaexample;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.examples.realmmultiprocessaexample.models.ProcessInfo;

public class MainActivity extends AppCompatActivity {

    private TextView textView;
    private Realm realm;
    private RealmChangeListener listener = new RealmChangeListener() {
        @Override
        public void onChange() {
            StringBuilder stringBuilder = new StringBuilder();

            RealmResults<ProcessInfo> results = realm.allObjects(ProcessInfo.class);
            for (ProcessInfo processInfo : results) {
                stringBuilder.append(processInfo.getName());
                stringBuilder.append("\npid: ");
                stringBuilder.append(processInfo.getPid());
                stringBuilder.append("\nlast response time: ");
                DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
                stringBuilder.append(df.format(processInfo.getLastResponseDate()));
                stringBuilder.append("\n------\n");
            }
            textView.setText(stringBuilder.toString());

            Intent intent = new Intent(MainActivity.this, AnotherProcessService.class);
            startService(intent);

            intent = new Intent();
            intent.setClassName("io.realm.examples.realmmultiprocessserviceonlyexample",
                    "io.realm.examples.realmmultiprocessserviceonlyexample.AnotherAPKService");
            startService(intent);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.textView);

        if (realm == null) {
            realm = Realm.getDefaultInstance();
            realm.addChangeListener(listener);
        }

        realm.beginTransaction();
        realm.copyToRealmOrUpdate(Utils.createStandaloneProcessInfo(this));
        realm.commitTransaction();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (realm != null) {
            realm.removeChangeListener(listener);
            realm.close();
            realm = null;
            Realm.disableInterprocessNotification();
        }
    }
}

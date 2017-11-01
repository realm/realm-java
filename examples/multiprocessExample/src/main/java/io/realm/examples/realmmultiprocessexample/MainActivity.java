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
package io.realm.examples.realmmultiprocessexample;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.examples.realmmultiprocessexample.models.ProcessInfo;

public class MainActivity extends AppCompatActivity {

    private TextView textView;
    private Realm realm;
    private RealmResults<ProcessInfo> processInfoResults;
    private DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.ENGLISH);

    private RealmChangeListener<RealmResults<ProcessInfo>> listener =
            new RealmChangeListener<RealmResults<ProcessInfo>>() {
        @Override
        public void onChange(RealmResults<ProcessInfo> results) {
            StringBuilder stringBuilder = new StringBuilder();

            for (ProcessInfo processInfo : results) {
                stringBuilder.append(processInfo.getName());
                stringBuilder.append("\npid: ");
                stringBuilder.append(processInfo.getPid());
                stringBuilder.append("\nlast response time: ");
                stringBuilder.append(dateFormat.format(processInfo.getLastResponseDate()));
                stringBuilder.append("\n------\n");
            }
            textView.setText(stringBuilder.toString());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.textView);

        if (realm == null) {
            realm = Realm.getDefaultInstance();
            processInfoResults = realm.where(ProcessInfo.class).findAllAsync();
            processInfoResults.addChangeListener(listener);
        }

        realm.beginTransaction();
        realm.copyToRealmOrUpdate(Utils.createStandaloneProcessInfo(this));
        realm.commitTransaction();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (realm != null) {
            realm.close();
            realm = null;
            processInfoResults = null;
        }
    }

    public void onStartButton(View button) {
        Intent intent = new Intent(MainActivity.this, AnotherProcessService.class);
        startService(intent);
        button.setEnabled(false);
    }
}

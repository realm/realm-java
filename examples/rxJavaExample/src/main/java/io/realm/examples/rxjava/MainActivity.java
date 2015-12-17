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

package io.realm.examples.rxjava;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.Map;
import java.util.TreeMap;

import io.realm.examples.rxjava.animation.AnimationActivity;
import io.realm.examples.rxjava.gotchas.GotchasActivity;
import io.realm.examples.rxjava.retrofit.RetrofitExample;
import io.realm.examples.rxjava.throttle.ThrottleSearchActivity;

public class MainActivity extends Activity {

    private ViewGroup container;
    private TreeMap<String, Class<? extends Activity>> buttons = new TreeMap<String, Class<? extends Activity>>() {{
        put("Animation", AnimationActivity.class);
        put("Throttle search", ThrottleSearchActivity.class);
        put("Network", RetrofitExample.class);
        put("Working with Realm", GotchasActivity.class);
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        container = (ViewGroup) findViewById(R.id.list);
        setupButtons();
    }

    private void setupButtons() {
        for (final Map.Entry<String, Class<? extends Activity>> entry : buttons.entrySet()) {
            Button button = new Button(this);
            button.setText(entry.getKey());
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(entry.getValue());
                }
            });
            container.addView(button);
        }
    }

    private void startActivity(Class<? extends Activity> activityClass) {
        startActivity(new Intent(this, activityClass));
    }
}

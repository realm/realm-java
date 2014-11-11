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

package io.realm.path;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.path.android.jobqueue.JobManager;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;


public class MainActivity extends ActionBarActivity {

    @InjectView(R.id.list) LinearLayout list;

    private EventBus bus;
    private JobManager jobManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        bus = EventBus.getDefault();
        bus.register(this);
        jobManager = new JobManager(this);
    }

    @OnClick(R.id.button)
    public void addPerson() {
        jobManager.addJob(new AddPersonJob(this));
    }

    // Calback from EventBus on same thread as job who posted the event. This is not the UI thread.
    public void onEvent(Person person) {
        final String description = person.toString();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, description, Toast.LENGTH_SHORT).show();
                TextView view = new TextView(MainActivity.this);
                view.setText(description);
                list.addView(view);
            }
        });
    }
}

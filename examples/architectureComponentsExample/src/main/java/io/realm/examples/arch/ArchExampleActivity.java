/*
 * Copyright 2018 Realm Inc.
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

package io.realm.examples.arch;

import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;

public class ArchExampleActivity extends AppCompatActivity {
    private FloatingActionButton backgroundJobStartStop;

    private BackgroundTask backgroundTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_arch_example);
        setupViews();

        backgroundTask = (BackgroundTask) getLastCustomNonConfigurationInstance();
        if (backgroundTask == null) { // this could also live inside a ViewModel, a singleton job queue, etc.
            backgroundTask = new BackgroundTask();
            backgroundTask.start(); // this task will update items in Realm on a background thread.
        }
        updateJobButton();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, PersonListFragment.create())
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return backgroundTask; // retain background task through config changes without ViewModel.
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing()) {
            if(backgroundTask.isStarted()) {
                backgroundTask.stop(); // make sure job is stopped when exiting the app
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() <= 1) {
            finish();
        } else {
            super.onBackPressed();
        }
    }

    @MainThread
    private void setupViews() {
        backgroundJobStartStop = findViewById(R.id.backgroundJobStartStop);
        backgroundJobStartStop.setOnClickListener(v -> {
            if (!backgroundTask.isStarted()) {
                backgroundTask.start();
            } else {
                backgroundTask.stop();
            }
            updateJobButton();
        });
    }

    private void updateJobButton() {
        if (backgroundTask.isStarted()) {
            backgroundJobStartStop.setImageResource(R.drawable.ic_stop_black_24dp);
        } else {
            backgroundJobStartStop.setImageResource(R.drawable.ic_play_arrow_black_24dp);
        }
    }
}

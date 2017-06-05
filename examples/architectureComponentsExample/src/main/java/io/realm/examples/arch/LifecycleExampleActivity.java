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

package io.realm.examples.arch;

import android.arch.lifecycle.LifecycleActivity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import java.util.Locale;

import io.realm.examples.arch.model.Person;


public class LifecycleExampleActivity extends LifecycleActivity {

    public static final String TAG = LifecycleExampleActivity.class.getName();
    private TextView ageView;

    private ExampleViewModel exampleViewModel;
    private Updater updater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_realm_basic_example);
        setupViews();

        exampleViewModel = ViewModelProviders.of(this).get(ExampleViewModel.class);
        exampleViewModel.getPerson().observe(this, new Observer<Person>() {
            @Override
            public void onChanged(@Nullable Person person) {
                updateViews(person);
            }
        });

        updater = ViewModelProviders.of(this).get(Updater.class);
    }

    @MainThread
    private void setupViews() {
        ageView = (TextView) findViewById(R.id.age);

        findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updater.start();
            }
        });
        findViewById(R.id.stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updater.stop();
            }
        });
    }

    @MainThread
    private void updateViews(@Nullable Person person) {
        ageView.setText(formatPerson(person));
    }

    @NonNull
    private static String formatPerson(@Nullable Person person) {
        if (person == null) {
            return "";
        }
        return String.format(Locale.ENGLISH, "%1$s: %2$d", person.name, person.age);
    }

    @Override
    protected void onStart() {
        super.onStart();

        updateViews(exampleViewModel.getPerson().getValue());
    }
}

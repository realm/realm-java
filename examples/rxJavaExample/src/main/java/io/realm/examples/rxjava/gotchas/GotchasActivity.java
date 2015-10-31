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
 *
 *
 */

package io.realm.examples.rxjava.gotchas;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.examples.rxjava.R;
import io.realm.examples.rxjava.common.model.Person;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;

public class GotchasActivity extends Activity {


    private Realm realm;
    private Subscription subscription;
    private ViewGroup container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animations);
        container = (ViewGroup) findViewById(R.id.list);
        container.removeAllViews();
        realm = Realm.getDefaultInstance();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Objects are auto-updatable (not immutable)
        // TODO Test:
        // ReplaySubject
        // buffer
        // cache()
        // distinctUntilChanged
    }

    @Override
    protected void onPause() {
        super.onPause();
        subscription.unsubscribe();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }



}

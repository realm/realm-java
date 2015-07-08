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

import android.os.Looper;
import android.util.Log;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import de.greenrobot.event.EventBus;
import io.realm.Realm;

public class AddPersonJob extends Job {
    private static final AtomicInteger jobCounter = new AtomicInteger(0);

    private final int id;
    private final Random random = new Random();

    MainActivity context;

    public AddPersonJob(MainActivity context) {
        super(new Params(1));
        id = jobCounter.incrementAndGet();
        this.context = context;
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {
        if (id != jobCounter.get()) {
            //looks like other fetch jobs has been added after me. no reason to keep fetching
            //many times, cancel me, let the other one fetch tweets.
            return;
        }
        Log.d("Job Manager", "Job Started, Main Thread:" + (Looper.myLooper() == Looper.getMainLooper()));

        Realm realm = Realm.getInstance(context);
        realm.beginTransaction();
        Person p = realm.createObject(Person.class);
        p.setPerson(getRandomName());
        p.setAge(getRandomAge());
        realm.commitTransaction();

        // Beware of this. EventHandler might be on another thread, but as long as you use
        // onEvent(Person person) you should be fine
        EventBus.getDefault().post(p);
    }

    private int getRandomAge() {
        return random.nextInt(100);
    }

    private String getRandomName() {
        String[] names = new String[] {"John", "Frank", "Niels"};
        return names[random.nextInt(names.length)];
    }

    @Override
    protected void onCancel() {
        // Ignore
    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return false;
    }
}

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

package io.realm.examples.threads;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.Random;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.examples.threads.model.Dot;
import io.realm.examples.threads.widget.DotsView;

/**
 * This fragment demonstrates how Realm can interact with a background thread.
 */
public class ThreadFragment extends Fragment {

    private Realm realm;
    private Random random = new Random();
    private Thread backgroundThread;
    private DotsView dotsView;

    // Realm change listener that refreshes the UI when there is changes to Realm.
    private RealmChangeListener realmListener = new RealmChangeListener() {
        @Override
        public void onChange() {
            dotsView.invalidate();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Create Realm instance for the UI thread
        realm = Realm.getInstance(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_thread, container, false);
        dotsView = (DotsView) rootView.findViewById(R.id.dots);

        // Create a RealmQuery on the UI thread and send the results to the custom view. The
        // RealmResults will automatically be updated whenever the Realm data is changed.
        // We still need to invalidate the UI to show the changes however. See the RealmChangeListener.
        //
        // Note that the query gets updated by rerunning it on the thread it was
        // created. This can negatively effect frame rates if it is a complicated query or a very
        // large data set.
        dotsView.setRealmResults(realm.allObjects(Dot.class));

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_backgroundthread, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()) {
            case R.id.action_add_dot:
                // Add blue dot from the UI thread
                realm.beginTransaction();
                Dot dot = realm.createObject(Dot.class);
                dot.setX(random.nextInt(100));
                dot.setY(random.nextInt(100));
                dot.setColor(getResources().getColor(R.color.realm_blue));
                realm.commitTransaction();
                return true;

            case R.id.action_clear:
                realm.beginTransaction();
                realm.clear(Dot.class);
                realm.commitTransaction();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Enable UI refresh while the fragment is active.
        realm.addChangeListener(realmListener);

        // Create background thread that add a new dot every 0.5 second.
        backgroundThread = new Thread() {

            @Override
            public void run() {
                // Realm instances cannot be shared between threads, so we need to create a new
                // instance on the background thread.
                int redColor = getResources().getColor(R.color.realm_red);
                Realm backgroundThreadRealm = Realm.getInstance(getActivity());
                while (!backgroundThread.isInterrupted()) {
                    backgroundThreadRealm.beginTransaction();

                    // Add red dot from the background thread
                    Dot dot = backgroundThreadRealm.createObject(Dot.class);
                    dot.setX(random.nextInt(100));
                    dot.setY(random.nextInt(100));
                    dot.setColor(redColor);
                    backgroundThreadRealm.commitTransaction();

                    // Wait 0.5 sec. before adding the next dot.
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        break;
                    }
                }

                // Also close Realm instances used in background threads.
                backgroundThreadRealm.close();
            }
        };
        backgroundThread.start();
    }

    @Override
    public void onPause() {
        super.onPause();

        // Disable UI refresh while the fragment is no longer active.
        realm.removeChangeListener(realmListener);
        backgroundThread.interrupt();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Remember to close the Realm instance when done with it.
        realm.close();
    }
}

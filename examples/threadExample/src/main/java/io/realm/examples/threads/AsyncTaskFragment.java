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

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import java.lang.ref.WeakReference;

import io.realm.Realm;
import io.realm.examples.threads.model.Score;

/**
 * This fragment demonstrates how Realm can work with AsyncTasks.
 */
public class AsyncTaskFragment extends Fragment {

    private static final String TAG = AsyncTaskFragment.class.getName();
    private static final int TEST_OBJECTS = 100;

    private LinearLayout logsView;
    private ProgressBar progressView;
    private ImportAsyncTask asyncTask;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_asynctask, container, false);
        logsView = (LinearLayout) rootView.findViewById(R.id.logs);
        progressView = (ProgressBar) rootView.findViewById(R.id.progressBar);
        progressView.setVisibility(View.GONE);
        rootView.findViewById(R.id.start_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (asyncTask != null) {
                    asyncTask.cancel(true);
                }

                asyncTask = new ImportAsyncTask(AsyncTaskFragment.this);
                asyncTask.execute();
            }
        });

        return rootView;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (asyncTask != null) {
            asyncTask.cancel(false);
        }
    }

    private void showStatus(String txt) {
        Log.i(TAG, txt);
        TextView tv = new TextView(getActivity());
        tv.setText(txt);
        tv.setTextColor(getResources().getColor(android.R.color.white));
        logsView.addView(tv);
    }

    // ASyncTask that imports Realm data while providing progress and returns the value of an
    // aggregate function in the end.
    //
    // Note:
    // doInBackground() runs in its own background thread while all other methods are executed on the
    // UI thread. This means that it is not possible to reuse RealmObjects or RealmResults created
    // in doInBackground() in the other methods. Nor is it possible to use RealmObjects as Progress
    // or Result objects.
    private static class ImportAsyncTask extends AsyncTask<Void, Integer, Integer> {

        private final WeakReference<AsyncTaskFragment> fragmentRef;

        ImportAsyncTask(AsyncTaskFragment outerFragment) {
            fragmentRef = new WeakReference<AsyncTaskFragment>(outerFragment);
        }

        @Override
        protected Integer doInBackground(Void... params) {
            Realm realm = Realm.getDefaultInstance();

            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.delete(Score.class);
                    for (int i = 0; i < TEST_OBJECTS; i++) {
                        if (isCancelled()) break;
                        Score score = realm.createObject(Score.class);
                        score.setName("Name" + i);
                        score.setScore(i);
                    }
                }
            });

            Number sum = realm.where(Score.class).sum("score");
            realm.close();
            return sum.intValue();
        }

        @Override
        protected void onPreExecute() {
            final AsyncTaskFragment fragment = fragmentRef.get();
            if (fragment == null || fragment.isDetached()) {
                cancel(false);
                return;
            }
            fragment.logsView.removeAllViews();
            fragment.progressView.setVisibility(View.VISIBLE);
            fragment.showStatus("Starting import");
        }

        @Override
        protected void onPostExecute(Integer sum) {
            final AsyncTaskFragment fragment = fragmentRef.get();
            if (fragment == null || fragment.isDetached()) {
                return;
            }
            fragment.progressView.setVisibility(View.GONE);
            fragment.showStatus(TEST_OBJECTS + " objects imported.");
            fragment.showStatus("The total score is : " + sum);
        }
    }
}

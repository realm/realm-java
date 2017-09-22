/*
 * Copyright 2016 Realm Inc.
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

package io.realm.examples.objectserver;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnull;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Progress;
import io.realm.ProgressListener;
import io.realm.ProgressMode;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.SyncConfiguration;
import io.realm.SyncManager;
import io.realm.SyncSession;
import io.realm.SyncUser;
import io.realm.examples.objectserver.model.CRDTCounter;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

public class CounterActivity extends AppCompatActivity {
    private static final String REALM_URL = "realm://" + BuildConfig.OBJECT_SERVER_IP + ":9080/~/default";

    private final ProgressListener downloadListener = new ProgressListener() {
        @Override
        public void onChange(@Nonnull Progress progress) {
            downloadingChanges.set(!progress.isTransferComplete());
            runOnUiThread(updateProgressBar);
        }
    };
    private final ProgressListener uploadListener = new ProgressListener() {
        @Override
        public void onChange(@Nonnull Progress progress) {
            uploadingChanges.set(!progress.isTransferComplete());
            runOnUiThread(updateProgressBar);
        }
    };
    private final Runnable updateProgressBar = new Runnable() {
        @Override
        public void run() {
            updateProgressBar(downloadingChanges.get(), uploadingChanges.get());
        }
    };

    private final AtomicBoolean downloadingChanges = new AtomicBoolean(false);
    private final AtomicBoolean uploadingChanges = new AtomicBoolean(false);

    private Realm realm;
    private SyncSession session;
    private SyncUser user;

    @BindView(R.id.text_counter) TextView counterView;
    @BindView(R.id.progressbar) MaterialProgressBar progressBar;
    private CRDTCounter counter; // Keep strong reference to counter to keep change listeners alive.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_counter);
        ButterKnife.bind(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        user = getLoggedInUser();
        if (user == null) { return; }

        // Create a RealmConfiguration for our user
        SyncConfiguration config = new SyncConfiguration.Builder(user, REALM_URL)
                .initialData(new Realm.Transaction() {
                    @Override
                    public void execute(@Nonnull Realm realm) {
                        realm.createObject(CRDTCounter.class, user.getIdentity());
                    }
                })
                .build();

        // This will automatically sync all changes in the background for as long as the Realm is open
        realm = Realm.getInstance(config);

        counterView.setText("-");
        counter = realm.where(CRDTCounter.class).equalTo("name", user.getIdentity()).findFirstAsync();
        counter.addChangeListener(new RealmChangeListener<CRDTCounter>() {
            @Override
            public void onChange(@Nonnull CRDTCounter counter) {
                counterView.setText((!counter.isValid()) ? "-" : String.format(Locale.US, "%d", counter.getCount()));
            }
        });

        // Setup progress listeners for indeterminate progress bars
        session = SyncManager.getSession(config);
        session.addDownloadProgressListener(ProgressMode.INDEFINITELY, downloadListener);
        session.addUploadProgressListener(ProgressMode.INDEFINITELY, uploadListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (session != null) {
            session.removeProgressListener(downloadListener);
            session.removeProgressListener(uploadListener);
            session = null;
        }
        closeRealm();
        user = null;
        counter = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_counter, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_logout:
                closeRealm();
                user.logout();
                user = getLoggedInUser();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @OnClick(R.id.upper)
    public void incrementCounter() {
        adjustCounter(1);
    }

    @OnClick(R.id.lower)
    public void decrementCounter() {
        adjustCounter(-1);
    }

    private void updateProgressBar(boolean downloading, boolean uploading) {
        @ColorRes int color = android.R.color.black;
        int visibility = View.VISIBLE;
        if (downloading && uploading) {
            color = R.color.progress_both;
        } else if (downloading) {
            color = R.color.progress_download;
        } else if (uploading) {
            color = R.color.progress_upload;
        } else {
            visibility = View.GONE;
        }
        progressBar.getIndeterminateDrawable().setColorFilter(getResources().getColor(color), PorterDuff.Mode.SRC_IN);
        progressBar.setVisibility(visibility);
    }

    private void adjustCounter(final int adjustment) {
        // A synchronized Realm can get written to at any point in time, so doing synchronous writes on the UI
        // thread is HIGHLY discouraged as it might block longer than intended. Use only async transactions.
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(@Nonnull Realm realm) {
                CRDTCounter counter = realm.where(CRDTCounter.class).findFirst();
                if (counter != null) {
                    counter.incrementCounter(adjustment);
                }
            }
        });
    }

    private SyncUser getLoggedInUser() {
        SyncUser user = null;

        try { user = SyncUser.currentUser(); }
        catch (IllegalStateException ignore) { }

        if (user == null) {
            startActivity(new Intent(this, LoginActivity.class));
        }

        return user;
    }

    private void closeRealm() {
        if (realm != null && !realm.isClosed()) {
            realm.close();
        }
    }
}

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

package io.realm.examples.cursor;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Random;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.examples.cursor.models.Score;


/**
 * This example project shows how Realm can expose its data as a Cursor which makes it possible to integrate Realm into
 * an existing app without migrating all existing SQLite database code at once.
 */
public class CursorExampleActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOADER_ID = 1;

    private Realm realm;
    private CursorAdapter adapter;
    private RealmChangeListener listener = new RealmChangeListener() {
        @Override
        public void onChange() {
            getLoaderManager().restartLoader(LOADER_ID, null, CursorExampleActivity.this);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        realm = Realm.getInstance(this);
        adapter = new MyCursorAdapter(this, null, 0);
        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        realm.addChangeListener(listener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        realm.removeChangeListener(listener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Cursor c = adapter.swapCursor(null);
        if (c != null) {
            c.close();
        }
        realm.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_add) {
            realm.beginTransaction();
            Score score = realm.createObject(Score.class);
            score.setId(PrimaryKeyFactory.nextScoreId());
            score.setName(getRandomUserName());
            score.setScore(getRandomScore());
            realm.commitTransaction();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private int getRandomScore() {
        return new Random().nextInt(100);
    }

    private String getRandomUserName() {
        return UUID.randomUUID().toString().replace("-","").substring(0, 8);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CustomRealmLoader(this, realm);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.changeCursor(null);
    }

    // Custom CursorAdapter should continue to work as normal
    private static class MyCursorAdapter extends CursorAdapter {

        private LayoutInflater inflater;

        public MyCursorAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
            inflater = LayoutInflater.from(context);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = inflater.inflate(R.layout.row_simpleitem, parent, false);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.id = (TextView) view.findViewById(R.id.text_left);
            viewHolder.name = (TextView) view.findViewById(R.id.text_center);
            viewHolder.score = (TextView) view.findViewById(R.id.text_right);
            view.setTag(viewHolder);
            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ViewHolder holder = (ViewHolder) view.getTag();
            holder.id.setText("" + cursor.getLong(cursor.getColumnIndexOrThrow("_id")));
            holder.name.setText(cursor.getString(cursor.getColumnIndexOrThrow(Score.FIELD_NAME)));
            holder.score.setText("" + cursor.getInt(cursor.getColumnIndexOrThrow(Score.FIELD_SCORE)));
        }

        private static class ViewHolder {
            TextView id;
            TextView score;
            TextView name;
        }
    }
}

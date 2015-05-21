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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.examples.cursor.models.Score;


/**
 * This example project shows how Realm can expose its data as a Cursor which makes it possible to integrate Realm into
 * an existing app without migrating all existing SQLite database code at once.
 */
public class CursorExampleActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOADER_ID = 1;

    private Realm realm;
    private CursorAdapter adapter;
    private Loader<Cursor> loader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        Realm.deleteRealmFile(this);

        realm = Realm.getInstance(this);
        realm.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                if (loader != null) {
                    loader.onContentChanged();
                }
            }
        });
        adapter = new MyCursorAdapter(this, null, 0);
        RealmResults<Score> timeStamps = realm.where(Score.class).findAll();
        final MyAdapter adapter = new MyAdapter(this, R.id.listView, timeStamps, true);
        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close(); // Remember to close Realm when done.
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        RealmQuery<Score> query = new RealmQuery<Score>(realm, Score.class);
        return new RealmLoader(this, query);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        this.loader = loader;
        adapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        this.loader = null;
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
            viewHolder.name = (TextView) view.findViewById(R.id.text_left);
            viewHolder.score = (TextView) view.findViewById(R.id.text_right);
            view.setTag(viewHolder);
            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ViewHolder holder = (ViewHolder) view.getTag();
            holder.score.setText(cursor.getString(cursor.getColumnIndexOrThrow(Score.FIELD_NAME)));
            holder.score.setText("" + cursor.getInt(cursor.getColumnIndexOrThrow(Score.FIELD_SCORE)));
        }

        private static class ViewHolder {
            TextView score;
            TextView name;
        }
    }
}

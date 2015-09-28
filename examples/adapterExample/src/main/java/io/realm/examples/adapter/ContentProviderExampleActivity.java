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

package io.realm.examples.adapter;

import android.app.Activity;
import android.content.ContentProviderClient;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import io.realm.Realm;


/**
 * This activity demonstrates how to access Realm data from another app using a ContentProvider.
 * The data provided here comes from the cursorExample app: /examples/cursorExample.
 */
public class ContentProviderExampleActivity extends Activity {

    private Realm realm;
    private ContentProviderClient provider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        // Find ContentProvider from cursorExample app
        provider = getContentResolver().acquireContentProviderClient("io.realm.examples.cursor.provider");

        // Query content
        Cursor cursor;
        try {
            cursor = provider.query(Uri.parse("content://io.realm.examples.cursor.provider/scores"), null, null, null, null);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }

        MyCursorAdapter adapter = new MyCursorAdapter(this, cursor, 0);
        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        provider.release();
        realm.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add) {
        }
        return true;
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
            holder.name.setText(cursor.getString(cursor.getColumnIndexOrThrow("name")));
            holder.score.setText("" + cursor.getInt(cursor.getColumnIndexOrThrow("score")));
        }

        private static class ViewHolder {
            TextView id;
            TextView score;
            TextView name;
        }
    }

}

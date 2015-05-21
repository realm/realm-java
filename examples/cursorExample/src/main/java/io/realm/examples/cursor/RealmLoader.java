package io.realm.examples.cursor;

import android.content.Context;
import android.content.Loader;
import android.database.Cursor;

import io.realm.RealmQuery;
import io.realm.android.RealmCursor;

/**
 * Custom RealmLoader replacing the standard CursorLoader
 *
 * @see http://developer.android.com/reference/android/content/CursorLoader.html
 */
public class RealmLoader extends Loader<Cursor> {

    private final RealmQuery query;

    public RealmLoader(Context context, RealmQuery query) {
        super(context);
        this.query = query;
    }

    @Override
    protected void onStartLoading() {
        // Realm currently doesn't support loading data on a background thread.
        RealmCursor cursor = query.findAll().getCursor();
        deliverResult(cursor);
    }

    @Override
    protected void onForceLoad() {
    }
}

package io.realm.examples.cursor;

import android.content.Context;
import android.content.Loader;
import android.database.Cursor;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.android.RealmCursor;
import io.realm.examples.cursor.models.Score;

/**
 * Custom RealmLoader replacing the standard CursorLoader
 *
 * @see http://developer.android.com/reference/android/content/CursorLoader.html
 */
public class CustomRealmLoader extends Loader<Cursor> {

    private Realm realm;

    public CustomRealmLoader(Context context, Realm realm) {
        super(context);
        this.realm = realm;
    }

    @Override
    protected void onStartLoading() {
        // Execute the query convert it to a Cursor
        RealmCursor cursor = realm.where(Score.class)
                .findAllSorted(Score.FIELD_ID, RealmResults.SORT_ORDER_ASCENDING)
                .getCursor();

        // Map "id" to "_id" which is required by CursorAdapters
        cursor.setIdColumn(Score.FIELD_ID);
        deliverResult(cursor);
    }


}

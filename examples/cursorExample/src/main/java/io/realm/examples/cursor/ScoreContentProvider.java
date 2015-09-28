package io.realm.examples.cursor;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.util.HashMap;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.android.RealmCursor;
import io.realm.examples.cursor.models.Score;

/**
 * Example of a simple content provider backed by Realm
 * ContentProviders are started when the app is started and live as long as the process.
 */
public class ScoreContentProvider extends ContentProvider {

    private static final String TAG = ScoreContentProvider.class.getName();
    private static final String AUTHORITY = "io.realm.examples.cursor.provider";
    private static final String URL = "content://" + AUTHORITY + "/scores";
    private static final Uri CONTENT_URI = Uri.parse(URL);

    private static final int SCORES = 1;
    private static final int SCORES_ID = 2;
    private static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, "scores", SCORES);
        uriMatcher.addURI(AUTHORITY, "scores/#", SCORES_ID);
    }

    private Realm realm;

    @Override
    public boolean onCreate() {
        realm = Realm.getInstance(getContext());
        return true;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        // ContentValues is a generic key-value data structure that needs to be manually mapped to the model class.
        realm.beginTransaction();
        Score score = realm.createObject(Score.class);
        score.setId(PrimaryKeyFactory.nextScoreId());
        score.setName(values.getAsString(Score.FIELD_NAME));
        score.setScore(values.getAsInteger(Score.FIELD_SCORE));
        realm.commitTransaction();

        Uri _uri = ContentUris.withAppendedId(CONTENT_URI, score.getId());
        getContext().getContentResolver().notifyChange(_uri, null);
        return _uri;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Log.e(TAG, Thread.currentThread().getName());
        // Map between the ContentProvider query interface and the Realm query interface.
        // Realm is not a SQL database, so normal SQL queries does not work when run on Realm.
        // This means that it is up to the ContentProvider to interpret the input parameters in a sensible manner.

        // In this example:
        // 1) Projection is ignored as RealmCursor does not support this.
        // 2) selection must have the format "WHERE fieldName = ?, fieldName = ?, ..."
        RealmResults<Score> results;
        RealmQuery query = realm.where(Score.class);
        if (selection == null || selection.isEmpty()) {
            // Treat this as "SELECT * FROM Score"
        } else {
            addEqualToParameters(query, selection, selectionArgs);
        }

        // Sorting parameters
        // Expected input is "fieldName DESC, fieldName ASC"
        if (sortOrder != null && !sortOrder.isEmpty()) {
            String[] sortParts = sortOrder.split("\\.");
            String[] fieldNames = getFieldNames(sortParts);
            boolean[] sortDirections = getSortDirections(sortParts);
            results = query.findAllSorted(fieldNames, sortDirections);
        } else {
            results = query.findAll();
        }

        RealmCursor c = results.getCursor();
        c.setIdColumn("id");
        return c;
    }

    // Convert standard query selection and selectionArgs to Realm method calls
    private void addEqualToParameters(RealmQuery query, String selection, String[] selectionArgs) {

    }

    // Convert "fieldName DESC, fieldName ASC" to list of field names
    private String[] getFieldNames(String[] sortParts) {
        String[] fieldNames = new String[sortParts.length];
        for (int i = 0; i < sortParts.length; i++) {
            fieldNames[i] = sortParts[i].split(" ")[0];
        }
        return fieldNames;
    }

    // Convert "fieldName DESC, fieldName ASC" to list of Realm sort directions
    private boolean[] getSortDirections(String[] sortParts) {
        boolean[] sortDirections = new boolean[sortParts.length];
        for (int i = 0; i < sortParts.length; i++) {
            String dir = sortParts[i].split(" ")[1];
            sortDirections[i] = dir.equals("DESC") ? RealmResults.SORT_ORDER_DESCENDING : RealmResults.SORT_ORDER_ASCENDING;
        }
        return sortDirections;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Not implemented;");
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Not implemented;");
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case SCORES:
                return "vnd.android.cursor.dir/io.realm.example.provider.scores";
            case SCORES_ID:
                return "vnd.android.cursor.item/io.realm.example.provider.scores";
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }
}
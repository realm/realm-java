package io.realm.examples.performance.sqlite;


import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import io.realm.examples.performance.model.Comment;

public class SQLiteTestDataSource {

    // Database fields
    private SQLiteDatabase database;
    private SQLiteTestHelper dbHelper;
    private String[] allColumns = { SQLiteTestHelper.COLUMN_ID,
            SQLiteTestHelper.COLUMN_COMMENT };

    public SQLiteTestDataSource(Context context) {
        dbHelper = new SQLiteTestHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public long createComment(String comment) {
        ContentValues values = new ContentValues();
        values.put(SQLiteTestHelper.COLUMN_COMMENT, comment);
        long insertId = database.insert(SQLiteTestHelper.TABLE_COMMENTS, null,
                values);
        return insertId;
    }

//    public Comment createComment(String comment) {
//        ContentValues values = new ContentValues();
//        values.put(SQLiteTestHelper.COLUMN_COMMENT, comment);
//        long insertId = database.insert(SQLiteTestHelper.TABLE_COMMENTS, null,
//                values);
//        Cursor cursor = database.query(SQLiteTestHelper.TABLE_COMMENTS,
//                allColumns, SQLiteTestHelper.COLUMN_ID + " = " + insertId, null,
//                null, null, null);
//        cursor.moveToFirst();
//        Comment newComment = cursorToComment(cursor);
//        cursor.close();
//        return newComment;
//    }

    public long performQuery() {
        String query = "SELECT * FROM t1 WHERE comment =";
        Cursor cursor = database.rawQuery(query, new String[] {"SMILE"});
        return cursor.getCount();
    }

    public long countQuery() {
        String count = "SELECT count(*) FROM table";
        Cursor mcursor = database.rawQuery(count, null);
        mcursor.moveToFirst();
        return mcursor.getInt(0);
    }

    public void deleteComment(Comment comment) {
        long id = comment.getId();
        System.out.println("Comment deleted with id: " + id);
        database.delete(SQLiteTestHelper.TABLE_COMMENTS, SQLiteTestHelper.COLUMN_ID
                + " = " + id, null);
    }

    public List<Comment> getAllComments() {
        List<Comment> comments = new ArrayList<Comment>();

        Cursor cursor = database.query(SQLiteTestHelper.TABLE_COMMENTS,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Comment comment = cursorToComment(cursor);
            comments.add(comment);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return comments;
    }

    private Comment cursorToComment(Cursor cursor) {
        Comment comment = new Comment();
        comment.setId(cursor.getLong(0));
        comment.setComment(cursor.getString(1));
        return comment;
    }
}
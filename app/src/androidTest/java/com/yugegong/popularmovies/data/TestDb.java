package com.yugegong.popularmovies.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import java.util.HashSet;

/**
 * Created by ygong on 9/3/15.
 */
public class TestDb extends AndroidTestCase {

    void deleteDb(){
        Log.v("TestDb", "deleteDb()");
        mContext.deleteDatabase(MovieDbHelper.DATABASE_NAME);
    }


    @Override
    protected void setUp() throws Exception {
        Log.v("TestDb", "setUp");
        deleteDb();
    }

    public void testCreateDb() throws Throwable {

        Log.v("TestDb", "testCreateDb");

        final HashSet<String> tableNameSet = new HashSet<>();
        tableNameSet.add(MovieContract.MovieEntry.TABLE_NAME);
        tableNameSet.add(MovieContract.VideoEntry.TABLE_NAME);

        SQLiteDatabase db = new MovieDbHelper(mContext).getWritableDatabase();
        assertTrue("Error: DB is not open", db.isOpen());

        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        assertTrue("Error: The db has not been created correctly", cursor.moveToFirst());

        do{
           tableNameSet.remove(cursor.getString(0));
        } while (cursor.moveToNext());

        assertTrue("Error: The db was created without movie table or/and video table",
                tableNameSet.isEmpty());

        // now, do our tables contain the correct columns?
        cursor = db.rawQuery("PRAGMA table_info(" + MovieContract.MovieEntry.TABLE_NAME + ")", null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                cursor.moveToFirst());

        final HashSet<String> movieColumnNameSet = new HashSet<>();
        movieColumnNameSet.add(MovieContract.MovieEntry._ID);
        movieColumnNameSet.add(MovieContract.MovieEntry.COLUMN_MOVIE_ID);
        movieColumnNameSet.add(MovieContract.MovieEntry.COLUMN_MOVIE_TITLE);
        movieColumnNameSet.add(MovieContract.MovieEntry.COLUMN_MOVIE_OVERVIEW);
        movieColumnNameSet.add(MovieContract.MovieEntry.COLUMN_MOVIE_RELEASE_DATE);
        movieColumnNameSet.add(MovieContract.MovieEntry.COLUMN_MOVIE_RATE);
        movieColumnNameSet.add(MovieContract.MovieEntry.COLUMN_MOVIE_POSTER_URI);

        int columnNameIndex = cursor.getColumnIndex("name");
        do {
            movieColumnNameSet.remove(cursor.getString(columnNameIndex));
        } while (cursor.moveToNext());
        assertTrue("Error: The table was created without all the fields",
                movieColumnNameSet.isEmpty());

        db.close();
    }

    public void testMovieTable() {
        insertMovie();
    }

    public void testVideoTable() {
        long movieRowId = insertMovie();
        assertTrue("Error: movie not inserted correctly.", movieRowId != -1);

        SQLiteDatabase db = new MovieDbHelper(mContext).getWritableDatabase();

        ContentValues testValues  = TestUtility.createVideoValue();
        long videoRowId = db.insert(MovieContract.VideoEntry.TABLE_NAME, null, testValues);
        assertTrue("Error: video not inserted correctly.", videoRowId != -1);

        Cursor cursor = db.query(
                MovieContract.VideoEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null);

        assertTrue("Error: no records returned from video query", cursor.moveToFirst());
        TestUtility.validateCurrentRecord("Error: Movie query validation failed.",
                cursor, testValues);

        cursor.close();
        db.close();
    }

    public long insertMovie() {
        SQLiteDatabase db = new MovieDbHelper(mContext).getWritableDatabase();
        assertTrue("Error: DB is not open", db.isOpen());

        ContentValues testValues = TestUtility.createMadMaxMovieValue();

        long movieRowId = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, testValues);

        assertTrue("Error: movie not inserted correctly.", movieRowId != -1);

        Cursor cursor = db.query(
                MovieContract.MovieEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null
        );

        assertTrue("Error: No records returned from movie table", cursor.moveToFirst());

        TestUtility.validateCurrentRecord("Error: Movie query validation failed.",
                cursor, testValues);

        cursor.close();
        db.close();
        return movieRowId;
    }
}

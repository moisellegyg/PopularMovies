package com.yugegong.popularmovies.data;

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.test.AndroidTestCase;
import android.util.Log;

/**
 * Created by ygong on 9/4/15.
 */
public class TestMovieProvider extends AndroidTestCase {

    private static final String LOG_TAG = TestMovieProvider.class.getSimpleName();

    public void testProviderRegistery() {
        PackageManager pm = mContext.getPackageManager();
        ComponentName componentName = new ComponentName(mContext.getPackageName(),
                MovieProvider.class.getName());
        try {
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);

            assertEquals("Error: MovieProvider registered with authority: " + providerInfo.authority
                    + "instead of authority: " + MovieContract.CONTENT_AUTHORITY,
                    providerInfo.authority, MovieContract.CONTENT_AUTHORITY);
        } catch (PackageManager.NameNotFoundException e) {
            assertTrue("Error: MovieProvider not registered at " + mContext.getPackageName(), false);
        }
    }

    public void testGetType() {
        // content://com.yugegong.popularmovies/movie/
        String type = mContext.getContentResolver().getType(MovieContract.MovieEntry.CONTENT_URI);
        assertEquals("Error: the MovieEntry CONTENT_URI should return MovieEntry.CONTENT_TYPE",
                MovieContract.MovieEntry.CONTENT_TYPE, type);

        // content://com.yugegong.popularmovies/video/
        type = mContext.getContentResolver().getType(MovieContract.VideoEntry.CONTENT_URI);
        assertEquals("Error: the VideoEntry CONTENT_URI should return VideoEntry.CONTENT_TYPE",
                MovieContract.VideoEntry.CONTENT_TYPE, type);

        long testMovieId = 12345;
        // content://com.yugegong.popularmovies/video/12345
        type = mContext.getContentResolver().getType(
                MovieContract.VideoEntry.buildVideoMovieUri(testMovieId));
        assertEquals("Error: the VideoEntry CONTENT_URI with movieId should return VideoEntry.CONTENT_TYPE",
                MovieContract.VideoEntry.CONTENT_TYPE, type);
    }

    public void testBasicMovieQuery() {
        SQLiteDatabase db = new MovieDbHelper(mContext).getWritableDatabase();
        ContentValues testValues = TestUtility.createMadMaxMovieValue();
        long movieRowId = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, testValues);
        assertTrue("Error: failure to insert Mad Max movie values", movieRowId != 1);
        db.close();

        Cursor movieCursor = mContext.getContentResolver().query(
                MovieContract.MovieEntry.CONTENT_URI,
                null,
                null,
                null,
                null,
                null
        );
        assertTrue("ERROR: Empty cursor returned. ", movieCursor.moveToFirst());

        TestUtility.validateCurrentRecord("Error", movieCursor, testValues);

        if (Build.VERSION.SDK_INT >= 19) {
            assertEquals("Error: Movie Query did not properly set NotificationUri",
                    movieCursor.getNotificationUri(), MovieContract.MovieEntry.CONTENT_URI);
        }
    }

    public void testBasicMovieVideoQuery() {
        SQLiteDatabase db = new MovieDbHelper(mContext).getWritableDatabase();

        // insert movie into db first
        ContentValues testMovieValues = TestUtility.createMadMaxMovieValue();
        long movieRowId = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, testMovieValues);
        assertTrue("Error: failure to insert Mad Max movie values", movieRowId != 1);

        ContentValues testVideoValues = TestUtility.createVideoValue();
        long videoRowId = db.insert(MovieContract.VideoEntry.TABLE_NAME, null, testVideoValues);
        assertTrue("Error: failure to insert VideoEntry into db", videoRowId != 1);

        db.close();

        Cursor videoCursor = mContext.getContentResolver().query(
                MovieContract.VideoEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        assertTrue("Error: Empty cursor returned. ", videoCursor.moveToFirst());
        TestUtility.validateCurrentRecord("Error", videoCursor, testVideoValues);
    }

    public void testUpdateMovie() {
        ContentValues testValues = TestUtility.createMadMaxMovieValue();

        Uri movieUri = mContext.getContentResolver()
                .insert(MovieContract.MovieEntry.CONTENT_URI, testValues);
        long movieRowId = ContentUris.parseId(movieUri);

        assertTrue(movieRowId != -1);
        Log.d(LOG_TAG, "New movieRowId = " + movieRowId);

        ContentValues updateValues = new ContentValues(testValues);
        updateValues.put(MovieContract.MovieEntry._ID, movieRowId);
        updateValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_TITLE, "Minions");

        Cursor movieCursor = mContext.getContentResolver()
                .query(MovieContract.MovieEntry.CONTENT_URI, null, null, null, null);

        int count = mContext.getContentResolver()
                .update(MovieContract.MovieEntry.CONTENT_URI, updateValues,
                        MovieContract.MovieEntry._ID + "= ?",
                        new String[]{Long.toString(movieRowId)});
        assertEquals(count, 1);
        movieCursor.close();


        Cursor cursor = mContext.getContentResolver().query(
                MovieContract.MovieEntry.CONTENT_URI,
                null,
                MovieContract.MovieEntry._ID + " = ?",
                new String[] {Long.toString(movieRowId)},
                null
        );
        assertTrue("ERROR: Empty cursor returned. ", cursor.moveToFirst());
        TestUtility.validateCurrentRecord("Error", cursor, updateValues);
        cursor.close();
    }

    public void testInsertReadProvider() {
        ContentValues testValues = TestUtility.createMadMaxMovieValue();
        Uri movieUri = mContext.getContentResolver()
                .insert(MovieContract.MovieEntry.CONTENT_URI, testValues);
        long movieId = ContentUris.parseId(movieUri);
        assertTrue(movieId != -1);

        ContentValues videoValues = TestUtility.createVideoValue();
        Uri videoUri = mContext.getContentResolver().insert(
                MovieContract.VideoEntry.CONTENT_URI, videoValues);
        long videoId = ContentUris.parseId(videoUri);
        assertTrue(videoId != -1);


    }

}

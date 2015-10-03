package com.yugegong.popularmovies.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.test.AndroidTestCase;

import java.util.Map;
import java.util.Set;

/**
 * Created by ygong on 9/3/15.
 */
public class TestUtility extends AndroidTestCase {

    static ContentValues createMadMaxMovieValue() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, 76341);
        contentValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_TITLE, "Mad Max: Fury Road");
        contentValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_OVERVIEW, "Overview");
        contentValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_RELEASE_DATE, "2015-05-15");
        contentValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_RATE, 7.7);
        contentValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_POSTER_URI, "/kqjL17yufvn9OVLyXYpvtyrFfak.jpg");

        return contentValues;
    }

    static ContentValues createVideoValue() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MovieContract.VideoEntry.COLUMN_MOVIE_ID, 76341);
        contentValues.put(MovieContract.VideoEntry.COLUMN_VIDEO_KEY, "FRDdRto_3SA");
        contentValues.put(MovieContract.VideoEntry.COLUMN_VIDEO_NAME, "Trailers From Hell");

        return contentValues;
    }

    static void validateCurrentRecord(String error, Cursor cursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = cursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String realValue = cursor.getString(idx);
            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + realValue +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue, realValue);
        }
    }
}

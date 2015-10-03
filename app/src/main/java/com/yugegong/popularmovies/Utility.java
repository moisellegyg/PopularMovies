package com.yugegong.popularmovies;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;

import com.yugegong.popularmovies.data.MovieContract.MovieEntry;
import com.yugegong.popularmovies.data.MovieContract.ReviewEntry;
import com.yugegong.popularmovies.data.MovieContract.VideoEntry;
import com.yugegong.popularmovies.model.Movie;
import com.yugegong.popularmovies.model.Review;
import com.yugegong.popularmovies.model.Video;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by ygong on 9/16/15.
 */
public class Utility {

    private static final String LOG_TAG = Utility.class.getSimpleName();

    public static String getPreferredSortBy(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return  prefs.getString(context.getString(R.string.pref_sort_by_key),
                context.getString(R.string.pref_sort_by_default));
    }

    public static int getScreenWidth(Activity activity){
        int screenWidth = 0;
        // get the screen width in pixels
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        screenWidth = dm.widthPixels;
        // convert the screen width from pi to dp. If screenwidth >= 600dp, means we are going to
        // use two pane mode for tablet. Divide the screenwidth by 2 to calculate the column width
        // of the grid view, which is also the imageview width.
        if (screenWidth*160/dm.densityDpi >= 600) screenWidth /= 2;
        //        Log.v(LOG_TAG, "dp2: " + screenWidth * 160 / dm.densityDpi + " " + screenWidth + " " + dm.densityDpi);
        return screenWidth;
    }

    public static void saveFavoriteMovie(Movie movie, Context context) {
        long movieId = addMovie(movie, context);
        Vector<ContentValues> cVVector = new Vector<>(movie.getVideoList().size());

        for (Video video : movie.getVideoList()) {
            ContentValues videoValues = new ContentValues();
            videoValues.put(VideoEntry.COLUMN_MOVIE_ID, movieId);
            videoValues.put(VideoEntry.COLUMN_VIDEO_KEY, video.getKey());
            videoValues.put(VideoEntry.COLUMN_VIDEO_NAME, video.getName());

            cVVector.add(videoValues);
        }
        int insertedVideo = 0;
        if (cVVector.size() > 0) {
            ContentValues[] cVArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cVArray);
            insertedVideo = context.getContentResolver()
                    .bulkInsert(VideoEntry.CONTENT_URI, cVArray);
        }
        Log.d(LOG_TAG, insertedVideo + " inserted.");

        cVVector.clear();

        cVVector = new Vector<>(movie.getReviewList().size());
        for (Review review : movie.getReviewList()) {
            ContentValues reviewValues = new ContentValues();
            reviewValues.put(ReviewEntry.COLUMN_MOVIE_ID, movieId);
            reviewValues.put(ReviewEntry.COLUMN_REVIEW_ID, review.getId());
            reviewValues.put(ReviewEntry.COLUMN_REVIEW_AUTHOR, review.getAuthor());
            reviewValues.put(ReviewEntry.COLUMN_REVIEW_CONTENT, review.getContent());
            cVVector.add(reviewValues);
        }
        int insertedReview = 0;
        if (cVVector.size() > 0) {
            ContentValues[] cVArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cVArray);
            insertedReview = context.getContentResolver()
                    .bulkInsert(ReviewEntry.CONTENT_URI, cVArray);
        }
        Log.d(LOG_TAG, insertedReview + " inserted.");
    }

    private static long addMovie(Movie movie, Context context){
        long movieId;

        // Check if the movie exists in the db
        Cursor cursor = context.getContentResolver().query(
                MovieEntry.CONTENT_URI,
                new String[]{MovieEntry._ID},
                MovieEntry.COLUMN_MOVIE_ID + " = ? ",
                new String[]{movie.getId()},
                null);

        // If exists, return the current ID
        // Otherwise, insert it
        try {
            if(cursor.moveToFirst()) {
                int movieIdIndex = cursor.getColumnIndex(MovieEntry._ID);
                movieId = cursor.getLong(movieIdIndex);
            } else {
                ContentValues movieValues = new ContentValues();
                movieValues.put(MovieEntry.COLUMN_MOVIE_ID, movie.getId());
                movieValues.put(MovieEntry.COLUMN_MOVIE_TITLE, movie.getTitle());
                movieValues.put(MovieEntry.COLUMN_MOVIE_POSTER_URI, movie.getPosterPath());
                movieValues.put(MovieEntry.COLUMN_MOVIE_RATE, movie.getRate());
                movieValues.put(MovieEntry.COLUMN_MOVIE_RELEASE_DATE, movie.getReleaseDate());
                movieValues.put(MovieEntry.COLUMN_MOVIE_RUNTIME, movie.getRuntime());
                movieValues.put(MovieEntry.COLUMN_MOVIE_OVERVIEW, movie.getOverview());
                movieValues.put(MovieEntry.COLUMN_MOVIE_POSTER_IMG, movie.getPosterImg());

                Uri uri = context.getContentResolver().insert(MovieEntry.CONTENT_URI, movieValues);
                movieId = ContentUris.parseId(uri);
            }
        } finally {
            cursor.close();
        }

        return movieId;
    }

    public static Movie retrieveMovie(Context context, Long id, Movie movie) {
        if (movie == null || id == -1) return null;

        ArrayList<Video> videoList = new ArrayList<>();
        Cursor cursor = context.getContentResolver().query(VideoEntry.CONTENT_URI,
                DetailFragment.VIDEO_COLUMNS,
                VideoEntry.COLUMN_MOVIE_ID + " = ? ",
                new String[]{Long.toString(id)},
                null);
        try {
            if (cursor.moveToFirst()){
                do {
                    String name = cursor.getString(cursor.getColumnIndex(VideoEntry.COLUMN_VIDEO_NAME));
                    String key = cursor.getString(cursor.getColumnIndex(VideoEntry.COLUMN_VIDEO_KEY));
                    videoList.add(new Video(name, key));
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }

        ArrayList<Review> reviewList = new ArrayList<>();
        cursor = context.getContentResolver().query(ReviewEntry.CONTENT_URI,
                DetailFragment.REVIEW_COLUMNS,
                ReviewEntry.COLUMN_MOVIE_ID + " = ? ",
                new String[]{Long.toString(id)},
                null);
        try {
            if (cursor.moveToFirst()){
                do {
                    String reviewId = cursor.getString(cursor.getColumnIndex(ReviewEntry.COLUMN_REVIEW_ID));
                    String author = cursor.getString(cursor.getColumnIndex(ReviewEntry.COLUMN_REVIEW_AUTHOR));
                    String content = cursor.getString(cursor.getColumnIndex(ReviewEntry.COLUMN_REVIEW_CONTENT));
                    reviewList.add(new Review(reviewId, author, content));
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }

        if (videoList.size() > 0) movie.setVideoList(videoList);
        if (reviewList.size() > 0) movie.setReviewList(reviewList);

        return movie;
    }

    public static byte[] getByteArrayFromBitmap(Bitmap bitmap) {
        if (bitmap == null) return new byte[0];

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }
    public static Bitmap getBitmapFromByteArray(byte[] byteArray) {
        if (byteArray == null) {
            return null;
        }
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }
}

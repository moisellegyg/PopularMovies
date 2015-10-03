package com.yugegong.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

/**
 * Created by ygong on 9/3/15.
 */
public class MovieProvider extends ContentProvider {

    // The URI Matcher used by this content provider
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MovieDbHelper mMovieDbHelper;

    // a list of movies
    // content://com.yugegong.popularmovies/movie
    static final int MOVIE = 100;

    // content://com.yugegong.popularmovies/video
    static final int VIDEO = 200;
    // a list of videos for a specific movie
    // content://com.yugegong.popularmovies/video/[MOVIE_ID]
    static final int VIDEO_WITH_MOVIE = 201;

    static final int REVIEW = 300;
    static final int REVIEW_WITH_MOVIE = 301;


    private static final SQLiteQueryBuilder sMovieQueryBuilder;
    static {
        sMovieQueryBuilder = new SQLiteQueryBuilder();
        sMovieQueryBuilder.setTables(
                MovieContract.MovieEntry.TABLE_NAME
        );
    }

    private static final SQLiteQueryBuilder sVideoQueryBuilder;
    static {
        sVideoQueryBuilder = new SQLiteQueryBuilder();
        sVideoQueryBuilder.setTables(
                MovieContract.VideoEntry.TABLE_NAME
        );
    }

    private static final SQLiteQueryBuilder sReviewQueryBuilder;
    static {
        sReviewQueryBuilder = new SQLiteQueryBuilder();
        sReviewQueryBuilder.setTables(
                MovieContract.ReviewEntry.TABLE_NAME
        );
    }

    // video.movie_id = ?
    private static final String sVideoMovieIdSelection =
            MovieContract.VideoEntry.COLUMN_MOVIE_ID + " = ? ";
    // review.movie_id = ?
    private static final String sReviewMovieIdSelection =
            MovieContract.ReviewEntry.COLUMN_MOVIE_ID + " = ? ";

    static UriMatcher buildUriMatcher() {
        final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MovieContract.CONTENT_AUTHORITY;
        uriMatcher.addURI(authority, MovieContract.PATH_MOVIE, MOVIE);

        uriMatcher.addURI(authority, MovieContract.PATH_VIDEO, VIDEO);
        uriMatcher.addURI(authority, MovieContract.PATH_VIDEO + "/#", VIDEO_WITH_MOVIE);

        uriMatcher.addURI(authority, MovieContract.PATH_REVIEW, REVIEW);
        uriMatcher.addURI(authority, MovieContract.PATH_REVIEW + "/#", REVIEW_WITH_MOVIE);
        return uriMatcher;
    }

    private Cursor getVideoByMovieId(Uri uri, String[] projection, String sortOrder) {
        String movieId = MovieContract.VideoEntry.getMovieIdFromUri(uri);
        String selection = sVideoMovieIdSelection;
        String[] selectionArgs = new String[]{movieId};
        return sVideoQueryBuilder.query(mMovieDbHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);
    }

    private Cursor getReviewByMovieId(Uri uri, String[] projection, String sortOrder) {
        String movieId = MovieContract.ReviewEntry.getMovieIdFromUri(uri);
        String selection = sReviewMovieIdSelection;
        String[] selectionArgs = new String[]{movieId};
        return sReviewQueryBuilder.query(mMovieDbHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);
    }

    @Override
    public boolean onCreate() {
        mMovieDbHelper = new MovieDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MOVIE:
                return MovieContract.MovieEntry.CONTENT_TYPE;
            case VIDEO:
                return MovieContract.VideoEntry.CONTENT_TYPE;
            case VIDEO_WITH_MOVIE:
                return MovieContract.VideoEntry.CONTENT_TYPE;
            case REVIEW:
                return MovieContract.ReviewEntry.CONTENT_TYPE;
            case REVIEW_WITH_MOVIE:
                return MovieContract.ReviewEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        Cursor cursor;
        switch (sUriMatcher.match(uri)) {
            // "movie"
            case MOVIE: {
                cursor = mMovieDbHelper.getReadableDatabase().query(
                        MovieContract.MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            // "video"
            case VIDEO: {
                cursor = mMovieDbHelper.getReadableDatabase().query(
                        MovieContract.VideoEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "video/#"
            case VIDEO_WITH_MOVIE: {
                cursor = getVideoByMovieId(uri, projection, sortOrder);
                break;
            }
            // "review"
            case REVIEW: {
                cursor = mMovieDbHelper.getReadableDatabase().query(
                        MovieContract.ReviewEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "review/#"
            case REVIEW_WITH_MOVIE: {
                cursor = getReviewByMovieId(uri, projection, sortOrder);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mMovieDbHelper.getWritableDatabase();
        Uri returnUri;

        switch (sUriMatcher.match(uri)) {
            case MOVIE: {
                long _id = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = MovieContract.MovieEntry.buildMovieUri(_id);
                } else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case VIDEO: {
                long _id = db.insert(MovieContract.VideoEntry.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = MovieContract.VideoEntry.buildVideoUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            case REVIEW: {
                long _id = db.insert(MovieContract.ReviewEntry.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = MovieContract.ReviewEntry.buildReviewUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mMovieDbHelper.getWritableDatabase();
        int returnCount = 0;

        Log.v("Provider", uri.toString());

        switch (sUriMatcher.match(uri)) {
            case VIDEO: {
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MovieContract.VideoEntry.TABLE_NAME, null, value);
                        if(_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                break;
            }
            case REVIEW: {
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MovieContract.ReviewEntry.TABLE_NAME, null, value);
                        if(_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                break;
            }
            default:
                returnCount = super.bulkInsert(uri, values);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnCount;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mMovieDbHelper.getWritableDatabase();
        int rowsDeleted;
        if(selection == null) selection = "1";
        switch (sUriMatcher.match(uri)) {
            case MOVIE: {
                rowsDeleted = db.delete(MovieContract.MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case VIDEO: {
                rowsDeleted = db.delete(MovieContract.VideoEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case REVIEW: {
                rowsDeleted = db.delete(MovieContract.ReviewEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        final SQLiteDatabase db = mMovieDbHelper.getWritableDatabase();
        int rowsUpdated;

        switch (sUriMatcher.match(uri)) {
            case MOVIE:
                rowsUpdated = db.update(MovieContract.MovieEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case VIDEO:
                rowsUpdated = db.update(MovieContract.VideoEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case REVIEW:
                rowsUpdated = db.update(MovieContract.ReviewEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }


    @Override
    public void shutdown() {
        super.shutdown();
    }
}

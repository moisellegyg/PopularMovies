package com.yugegong.popularmovies;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.yugegong.popularmovies.data.MovieContract;
import com.yugegong.popularmovies.model.Movie;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by ygong on 9/24/15.
 */
public class FavoriteFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    @Bind(R.id.gridview_movie) GridView bGridView;

    private static final String LOG_TAG = FavoriteFragment.class.getSimpleName();
    private static final int MOVIE_LOADER = 0;
    private static final String SELECTED_KEY = "selected_position";

    public static final String FAVORITEFRAGMENT_TAG = "FFTAG";
    public static final String SELECTED_MOVIE = "selected_movie";
    public static final String SELECTED_ID = "selected_id";
    public static int sScreenWidth = 0;

    private MovieAdapter mMovieAdapter;
    private int mPosition = GridView.INVALID_POSITION;

    public static final String[] MOVIE_COLUMNS = {
            MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_MOVIE_ID,
            MovieContract.MovieEntry.COLUMN_MOVIE_TITLE,
            MovieContract.MovieEntry.COLUMN_MOVIE_OVERVIEW,
            MovieContract.MovieEntry.COLUMN_MOVIE_RELEASE_DATE,
            MovieContract.MovieEntry.COLUMN_MOVIE_RUNTIME,
            MovieContract.MovieEntry.COLUMN_MOVIE_RATE,
            MovieContract.MovieEntry.COLUMN_MOVIE_POSTER_URI,
            MovieContract.MovieEntry.COLUMN_MOVIE_POSTER_IMG
    };

    static final int COL_ID = 0;
    static final int COL_MOVIE_ID = 1;
    static final int COL_MOVIE_TITLE = 2;
    static final int COL_MOVIE_OVERVIEW = 3;
    static final int COL_MOVIE_RELEASE_DATE = 4;
    static final int COL_MOVIE_RUNTIME = 5;
    static final int COL_MOVIE_RATE = 6;
    static final int COL_MOVIE_POSTER_URL = 7;
    static final int COL_MOVIE_POSTER_IMG = 8;

    public interface Callback{
        void onItemSelected(long movieId, Movie movie);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onCreateView");

        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        //px = dp * (dpi / 160)
        sScreenWidth = dm.widthPixels;
        if (sScreenWidth*160/dm.densityDpi >= 600) sScreenWidth /= 2;
//        Log.v(LOG_TAG, "dp2: " + sScreenWidth * 160 / dm.densityDpi + " " + sScreenWidth + " " + dm.densityDpi);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, rootView);

        mMovieAdapter = new MovieAdapter(getActivity(), null, 0);
        bGridView.setAdapter(mMovieAdapter);
        bGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                if (cursor != null) {
                    ((Callback) getActivity()).onItemSelected(cursor.getLong(COL_ID),
                            getMovieFromCursor(cursor));
                }
                mPosition = position;
            }
        });

        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            mPosition = savedInstanceState.getInt(SELECTED_KEY, 0);
        }
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(MOVIE_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        String sort_by = Utility.getPreferredSortBy(getActivity());
        if (!sort_by.equals(getString(R.string.sort_by_favorite))) {
            Log.v(LOG_TAG, "change to RegularFragment");
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new RegularFragment(), RegularFragment.REGULARFRAGMENT_TAG)
                    .commit();
            return;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mPosition != GridView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.v(LOG_TAG, "onCreateLoader");
        Uri movieUri = MovieContract.MovieEntry.CONTENT_URI;
        return new CursorLoader(getActivity(), movieUri, MOVIE_COLUMNS, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.v(LOG_TAG, "onLoadFinished, mPosition: " + mPosition );
        mMovieAdapter.swapCursor(data);
        if (mPosition != GridView.INVALID_POSITION) {
            bGridView.smoothScrollToPosition(mPosition);
        }
        Log.v(LOG_TAG, "onLoadFinished, adapter: " + mMovieAdapter.getCount());
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMovieAdapter.swapCursor(null);
    }

    private Movie getMovieFromCursor(Cursor c) {
        String id = c.getString(COL_MOVIE_ID);
        String title = c.getString(COL_MOVIE_TITLE);
        String overview = c.getString(COL_MOVIE_OVERVIEW);
        String poster = c.getString(COL_MOVIE_POSTER_URL);
        String release = c.getString(COL_MOVIE_RELEASE_DATE);
        String rate = c.getString(COL_MOVIE_RATE);
        String runtime = c.getString(COL_MOVIE_RUNTIME);
        byte[] posterImg = c.getBlob(COL_MOVIE_POSTER_IMG);

        Movie movie = new Movie(id, title);
        movie.setOverview(overview);
        movie.setPosterPath(poster);
        movie.setReleaseDate(release);
        movie.setRate(rate);
        movie.setRuntime(runtime);
        movie.setPosterImg(posterImg);

        return movie;
    }
}

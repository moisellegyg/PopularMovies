package com.yugegong.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;


/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {
    private final static String LOG_TAG = DetailActivityFragment.class.getSimpleName();
    private final static String KEY_DETAILS = "details";

    private Movie mMovie;
    private int position;

    private MovieService service = MovieService.SERVICE;
    private MovieService.MovieDetailWatcher watcher;

    public DetailActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if ( (savedInstanceState == null) || !savedInstanceState.containsKey(KEY_DETAILS) ) {
            Intent intent = getActivity().getIntent();
            if ( (intent != null) && intent.hasExtra(MainActivityFragment.KEY_POSITION)) {
                position = intent.getIntExtra(MainActivityFragment.KEY_POSITION, 0);
//                Log.v(LOG_TAG,"Position: " + position);

                if ((service.getMovieList() != null)
                        && (service.getMovieList().size() > position) ) {
                    mMovie = service.getMovieList().get(position);
                }
            }
        } else {
            mMovie = savedInstanceState.getParcelable(KEY_DETAILS);
        }

        this.setMovieDetailWatcher(new MovieService.MovieDetailWatcher() {
            @Override
            public void detailRefresh(Movie movie) {
                ((TextView) getActivity().findViewById(R.id.runtime))
                        .setText(movie.getRuntime() + "min");
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (mMovie == null) {
            Log.d(LOG_TAG, "Movie is null. No details loaded");
            return null;
        }

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        if (mMovie.getRuntime() == null || mMovie.getRuntime().isEmpty()) {
            service.updateMovieDetails(mMovie, watcher);
        } else {
            ((TextView) rootView.findViewById(R.id.runtime)).setText(mMovie.getRuntime() + "min");
        }

        // Set data to view
        ((TextView) rootView.findViewById(R.id.movie_title)).setText(mMovie.getTitle());
        Picasso.with(getActivity())
                .load(mMovie.getPosterPath())
                .into((ImageView) rootView.findViewById(R.id.poster));
        ((TextView) rootView.findViewById(R.id.year)).setText(mMovie.getReleaseDate());
        ((TextView) rootView.findViewById(R.id.rate)).setText(mMovie.getRate() + "/10");
        ((TextView) rootView.findViewById(R.id.overview)).setText(mMovie.getOverview());

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mMovie != null) {
            outState.putParcelable(KEY_DETAILS, mMovie);
        }
    }

    private void setMovieDetailWatcher(MovieService.MovieDetailWatcher watcher) {
        this.watcher = watcher;
    }

}

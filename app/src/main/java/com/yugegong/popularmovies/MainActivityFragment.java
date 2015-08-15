package com.yugegong.popularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    public static MovieService sService = MovieService.SERVICE;
    private MovieService.MovieListWatcher mWatcher;
    public static final String KEY_POSITION = "position";
    private static ImageAdapter sImageAdapter;

    /**
     * The number of the Movies get rendered in the Adapter.
     */
    private static int renderedItemCount = 0;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

//        Log.v("onCreateView", "renderedItemCount = " + renderedItemCount);
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        GridView gridView = (GridView)rootView.findViewById(R.id.gridview_movie);

        sImageAdapter = new ImageAdapter(getActivity(), R.layout.grid_item_movie,
                R.id.grid_item_movie_imageview, sService.getMovieList());

        this.setMovieListWatcher(new MovieService.MovieListWatcher() {
            @Override
            public void listRefresh() {
                //sImageAdapter.addAll(movies);
                sImageAdapter.notifyDataSetChanged();
            }
        });

        gridView.setAdapter(sImageAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Movie movie = (Movie) parent.getItemAtPosition(position);
                Intent detailIntent = new Intent(getActivity(), DetailActivity.class)
//                        .putExtra("movie_details", movie)
                        .putExtra(KEY_POSITION, position);
                startActivity(detailIntent);
            }
        });

        gridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // Nothing to do when the use is scrolling
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
//                Log.v("OnScroll", firstVisibleItem + " " + visibleItemCount + " " + renderedItemCount + " " + totalItemCount);
                if (visibleItemCount == 0) return;
                if (renderedItemCount == totalItemCount) return;

                // If scroll down to the end of the movie list
                if ((firstVisibleItem + visibleItemCount) == totalItemCount) {
                    renderedItemCount = totalItemCount;
                    sService.updateMovie(mWatcher);
                }
            }
        });

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
//        Log.v("On Start", "renderedItemCount = " + renderedItemCount);
//        Log.v("On Start", "mMovieList Size: " + sService.getMovieList().size()
//                + " sImageAdapter Size: " + sImageAdapter.getCount());
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sort_by = settings.getString(getString(R.string.pref_sort_by_key),
                getString(R.string.pref_sort_by_default));

//        Log.v("Sort_by", sort_by + " currOrderBy: " + sService.getCurrSortBy());
        if (sService.getCurrSortBy().equals(sort_by)) {
            return;
        }

        // Order by setting is changed
        // Only clear the data in the ImageAdapter when there is no data getting rendered
        // This could prevent the Adapter gets cleared when navigating back from child activity
        renderedItemCount = 0;
        sImageAdapter.clear();
        sService.updateMovie(sort_by, mWatcher);
    }


    public void setMovieListWatcher(MovieService.MovieListWatcher watcher) {
        this.mWatcher = watcher;
    }

}
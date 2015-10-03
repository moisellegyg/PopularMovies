package com.yugegong.popularmovies;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;

import com.yugegong.popularmovies.service.MovieService;

import butterknife.Bind;
import butterknife.ButterKnife;


/**
 * A placeholder fragment containing a simple view.
 */
public class RegularFragment extends Fragment {
    @Bind(R.id.gridview_movie) GridView bGridView;

    public static final String REGULARFRAGMENT_TAG = "RFTAG";
    public static final String SELECTED_KEY = "selected_position";

    public static int sScreenWidth = 0;
    public static MovieService sService = MovieService.SERVICE;

    private static final String LOG_TAG = RegularFragment.class.getSimpleName();
    private static ImageAdapter sImageAdapter;

    private MovieService.MovieListWatcher mWatcher;

    /**
     * The number of the Movies get rendered in the Adapter.
     */
    private static int renderedItemCount = 0;

    public interface Callback {
        void onItemSelected(int position);
    }

    public RegularFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // get the screen width
        sScreenWidth = Utility.getScreenWidth(getActivity());

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, rootView);

        sImageAdapter = new ImageAdapter(getActivity(), R.layout.grid_item_movie,
                R.id.grid_item_movie_imageview, sService.getMovieList());

        this.setMovieListWatcher(new MovieService.MovieListWatcher() {
            @Override
            public void listRefresh() {
                sImageAdapter.notifyDataSetChanged();
            }
        });

        bGridView.setAdapter(sImageAdapter);

        bGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Movie movie = (Movie) parent.getItemAtPosition(position);
                ((Callback) getActivity())
                        .onItemSelected(position);
//                Intent detailIntent = new Intent(getActivity(), DetailActivity.class)
//                        .putExtra(SELECTED_KEY, position);
//                startActivity(detailIntent);
            }
        });

        bGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // Nothing to do when the use is scrolling
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
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
        Log.v(LOG_TAG, "onStart");
        super.onStart();
        String sort_by = Utility.getPreferredSortBy(getActivity());
        if (sService.getCurrSortBy().equals(sort_by)) {
            return;
        }
        if (sort_by.equals(getString(R.string.sort_by_favorite))) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new FavoriteFragment(), FavoriteFragment.FAVORITEFRAGMENT_TAG)
                    .commit();
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
package com.yugegong.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.yugegong.popularmovies.model.Movie;


public class MainActivity extends AppCompatActivity implements RegularFragment.Callback, FavoriteFragment.Callback{

    private boolean mTwoPane;
    private static final String DETAILFRAGMENT_TAG = "DFTAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v("MainActivity", "onCreate");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            String sort_by = Utility.getPreferredSortBy(this);
            Log.v("MainActivity", sort_by);

            if (sort_by.equals(getString(R.string.sort_by_favorite))) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, new FavoriteFragment())
                        .commit();
            } else {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, new RegularFragment())
                        .commit();
            }
        }

        if(findViewById(R.id.movie_detail_container) != null) {
            mTwoPane = true;
            if(savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.movie_detail_container, new DetailFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
            getSupportActionBar().setElevation(0f);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(int position) {
        if (mTwoPane) {
            Bundle args = new Bundle();
            args.putInt(DetailFragment.DETAIL_POSITION, position);
            DetailFragment df = new DetailFragment();
            df.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, df, DETAILFRAGMENT_TAG)
                    .commit();
        } else {
            Intent detailIntent = new Intent(this, DetailActivity.class)
                    .putExtra(RegularFragment.SELECTED_KEY, position);
            startActivity(detailIntent);
        }
    }

    @Override
    public void onItemSelected(long movieId, Movie movie) {
        if (mTwoPane) {
            Bundle args = new Bundle();
            args.putLong(DetailFragment.DETAIL_ID, movieId);
            args.putParcelable(DetailFragment.DETAIL_MOVIE, movie);

            DetailFragment df = new DetailFragment();
            df.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, df, DETAILFRAGMENT_TAG)
                    .commit();
        } else {
            Intent detailIntent = new Intent(this, DetailActivity.class)
                    .putExtra(FavoriteFragment.SELECTED_ID, movieId)
                    .putExtra(FavoriteFragment.SELECTED_MOVIE, movie);
            startActivity(detailIntent);
        }
    }
}

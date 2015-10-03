package com.yugegong.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;


public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            Bundle args = new Bundle();
            Intent intent = getIntent();
            if (intent.hasExtra(RegularFragment.SELECTED_KEY)) {
                args.putInt(DetailFragment.DETAIL_POSITION,
                        intent.getIntExtra(RegularFragment.SELECTED_KEY, -1));
            } else if (intent.hasExtra(FavoriteFragment.SELECTED_MOVIE)
                    && intent.hasExtra(FavoriteFragment.SELECTED_ID)) {
                args.putLong(DetailFragment.DETAIL_ID,
                        intent.getLongExtra(FavoriteFragment.SELECTED_ID, -1L));
                args.putParcelable(DetailFragment.DETAIL_MOVIE,
                        intent.getParcelableExtra(FavoriteFragment.SELECTED_MOVIE));
            }

            DetailFragment df = new DetailFragment();
            df.setArguments(args);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.movie_detail_container, df)
                    .commit();
        }
    }


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_detail, menu);
//        return true;
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
}

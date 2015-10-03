package com.yugegong.popularmovies;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.yugegong.popularmovies.data.MovieContract;
import com.yugegong.popularmovies.model.Movie;
import com.yugegong.popularmovies.model.Review;
import com.yugegong.popularmovies.model.Video;
import com.yugegong.popularmovies.service.MovieService;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;


/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment {
    @Bind(R.id.movie_title) TextView bTitle;
    @Bind(R.id.poster) ImageView bPoster;
    @Bind(R.id.year) TextView bYear;
    @Bind(R.id.rate) TextView bRate;
    @Bind(R.id.overview) TextView bOverview;
    @Bind(R.id.runtime) TextView bRuntime;
    @Bind(R.id.btn_favorite) Button bFavoriteBtn;
    @Bind(R.id.video_list) LinearLayout bVideoList;
    @Bind(R.id.review_list) LinearLayout bReviewList;

    private final static String LOG_TAG = DetailFragment.class.getSimpleName();
    private final static String KEY_DETAILS = "details";
    private final static String YOUTUBE_BASE_URL = "https://www.youtube.com/watch?v=";

    private Movie mMovie;
    private int mPosition;
    private ShareActionProvider mShareActionProvider;
    private MovieService mService = MovieService.SERVICE;
    private MovieService.MovieDetailWatcher mWatcher;

    public static final String DETAIL_POSITION = "POSITION";
    public static final String DETAIL_ID = "ID";
    public static final String DETAIL_MOVIE = "MOVIE";

    public static final String[] VIDEO_COLUMNS = {
            MovieContract.VideoEntry.COLUMN_VIDEO_KEY,
            MovieContract.VideoEntry.COLUMN_VIDEO_NAME,
    };

    public static final String[] REVIEW_COLUMNS = {
            MovieContract.ReviewEntry.COLUMN_REVIEW_ID,
            MovieContract.ReviewEntry.COLUMN_REVIEW_AUTHOR,
            MovieContract.ReviewEntry.COLUMN_REVIEW_CONTENT
    };

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(LOG_TAG, "onCreate");

        if ( savedInstanceState != null && savedInstanceState.containsKey(KEY_DETAILS)) {
            Log.v(LOG_TAG, "have savedInstanceState");
            mMovie = savedInstanceState.getParcelable(KEY_DETAILS);
            return;
        }

        Bundle args = getArguments();
        if (args != null) {
            if (args.getInt(DETAIL_POSITION, -1) != -1) {
                mPosition = args.getInt(DETAIL_POSITION);
                if ((mService.getMovieList() != null)
                        && (mService.getMovieList().size() > mPosition) ) {
                    mMovie = mService.getMovieList().get(mPosition);
                }
            } else if (args.getLong(DETAIL_ID, -1L) != -1L
                    && args.getParcelable(DETAIL_MOVIE) != null) {
                long movieId = args.getLong(DETAIL_ID);
                Movie movie = args.getParcelable(DETAIL_MOVIE);
                mMovie = Utility.retrieveMovie(getActivity(), movieId, movie);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onCreateView");

        if (mMovie == null) {
            Log.d(LOG_TAG, "Movie is null. No details loaded");
            TextView textView = new TextView(getActivity());
            textView.setText(getString(R.string.no_movie_detail));
            container.addView(textView);
            return null;
        }

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        ButterKnife.bind(this, rootView);

        this.setMovieDetailWatcher(new MovieService.MovieDetailWatcher() {
            @Override
            public void detailRefresh(Movie movie) {
                // Prevent view get loaded while fragment hasn't been attached to the activity yet.
                if (!isAdded()) return;
                bRuntime.setText(String.format(getString(R.string.runtime), movie.getRuntime()));
                setVideoList(movie.getVideoList());
                setReviewList(movie.getReviewList());
                if (mShareActionProvider != null) {
                    mShareActionProvider.setShareIntent(createShareForecastIntent());
                }
            }
        });

        if (mMovie.getRuntime() == null || mMovie.getRuntime().isEmpty()) {
            mService.updateMovieDetails(mMovie, mWatcher);
        } else {
            bRuntime.setText(String.format(getString(R.string.runtime), mMovie.getRuntime()));
            setVideoList(mMovie.getVideoList());
            setReviewList(mMovie.getReviewList());
        }

        // Set data to view
        bTitle.setText(mMovie.getTitle());
        bYear.setText(mMovie.getReleaseDate());
        bRate.setText(String.format("%s/10",mMovie.getRate()));
        bOverview.setText(mMovie.getOverview());

        Bitmap bitmap = Utility.getBitmapFromByteArray(mMovie.getPosterImg());
        if (bitmap == null) {
            Picasso.with(getActivity())
                    .load(mMovie.getPosterPath())
                    .placeholder(R.drawable.poster_placeholder)
                    .error(R.drawable.poster_placeholder_error)
                    .into(bPoster);
            if (!bPoster.getDrawable().getClass().getSimpleName().equals("GradientDrawable")) {
                bitmap = ((BitmapDrawable)bPoster.getDrawable()).getBitmap();
            }
            mMovie.setPosterImg(Utility.getByteArrayFromBitmap(bitmap));
        } else {
            bPoster.setImageBitmap(bitmap);
        }

        bFavoriteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utility.saveFavoriteMovie(mMovie, getActivity());
            }
        });

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mMovie != null) {
            outState.putParcelable(KEY_DETAILS, mMovie);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.v(LOG_TAG, "onCreateOptionMenu");
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_detail, menu);

        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        if (mMovie != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    private Intent createShareForecastIntent() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        String share_info;
        if (mMovie.getVideoList() != null && mMovie.getVideoList().size() != 0) {
            String youtubeLink = YOUTUBE_BASE_URL + mMovie.getVideoList().get(0).getKey();
            share_info = String.format(getString(R.string.shared_movie_with_video),
                    mMovie.getTitle(), youtubeLink);
        } else {
            share_info = String.format(getString(R.string.shared_movie_no_video),
                    mMovie.getTitle());
        }
        intent.putExtra(Intent.EXTRA_TEXT, share_info);
        intent.setType("text/plain");
        return intent;
    }

    private void setMovieDetailWatcher(MovieService.MovieDetailWatcher watcher) {
        this.mWatcher = watcher;
    }

    private void setVideoList(ArrayList<Video> videoList) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        if (videoList == null || videoList.size() == 0) {
            View view = inflater.inflate(R.layout.no_data, bVideoList, false);
            TextView noData = (TextView) view.findViewById(R.id.no_data);
            noData.setText(R.string.no_video);
            bVideoList.addView(noData);
            return;
        }

        for (Video video : videoList) {
            View view = inflater.inflate(R.layout.row_video, bVideoList, false);
            view.setTag(YOUTUBE_BASE_URL + video.getKey());
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Toast.makeText(getActivity(), (String) button.getTag(), Toast.LENGTH_SHORT).show();
                    startYoutube((String) v.getTag());
                }
            });
            TextView videoTitle = (TextView)view.findViewById(R.id.video_title);
            videoTitle.setText(video.getName());
            bVideoList.addView(view);
        }
    }

    private void setReviewList(ArrayList<Review> reviewList) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        if (reviewList == null || reviewList.size() == 0) {
            View view = inflater.inflate(R.layout.no_data, bReviewList, false);
            TextView noData = (TextView) view.findViewById(R.id.no_data);
            noData.setText(R.string.no_reivew);
            bReviewList.addView(noData);
            return;
        }

        for (Review review : reviewList) {
            View view  = inflater.inflate(R.layout.row_review, bReviewList, false);
            TextView reviewContent = (TextView)view.findViewById(R.id.textview_review_content);
            reviewContent.setText(review.getContent());
            TextView reviewAuthor = (TextView)view.findViewById(R.id.textview_review_author);
            reviewAuthor.setText("--" + review.getAuthor());
            bReviewList.addView(view);
        }
    }

    private void startYoutube(String uriStr) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(uriStr));
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Log.d(LOG_TAG, "Couldn't call " + uriStr + ", no Youtube video to open.");
        }
    }


}

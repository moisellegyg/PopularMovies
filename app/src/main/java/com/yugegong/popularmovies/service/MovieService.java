package com.yugegong.popularmovies.service;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.yugegong.popularmovies.model.Movie;
import com.yugegong.popularmovies.model.Review;
import com.yugegong.popularmovies.model.Video;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * MovieService will be responsible for the work of processing data,
 * including fetching data from server, retrieve data from cache(need to be done)
 */
public class MovieService {

    public static MovieService SERVICE = new MovieService();

    /** Indicates which mPage of data to fetch from server. */
    private int mPage;
    /**
     * The minimum vote count when order by average rate,
     * in order to make the information makes more sense.
     */
    private final String MIN_VOTE_DEFAULT = "100";
    private final String API_KEY_VALUE = "YOUR_API_KEY";

    private String mCurrSortBy;
    private ArrayList<Movie> mMovieList;
    private ArrayList<MovieListWatcher> mMovieListWatchers;
    private ArrayList<MovieDetailWatcher> mMovieDetailWatchers;

    /**
     * Class implementing MovieListWatcher will define a callback function {@link #listRefresh}
     * to refresh the data in the class
     * */
    public interface MovieListWatcher {
        /** MovieListWatcher to run this method in onPostExecute() when all data get fetched. */
        void listRefresh();
    }

    public interface MovieDetailWatcher {
        void detailRefresh(Movie movie);
    }

    private MovieService() {
        mPage = 0;
        mCurrSortBy = "";
        mMovieList = new ArrayList<>();
        mMovieListWatchers = new ArrayList<>();
        mMovieDetailWatchers = new ArrayList<>();
//        Log.v("MovieService", "Service get init");
    }

    public ArrayList<Movie> getMovieList() {
        return mMovieList;
    }

    public String getCurrSortBy() {
        return mCurrSortBy;
    }

    public void setCurrSortBy(String sort_by) {
        mCurrSortBy = sort_by;
    }

    private void execute(MovieListWatcher watcher) {
        mMovieListWatchers.add(watcher);
        FetchMovieTask task = new FetchMovieTask();
        mPage++;
        task.execute(Integer.toString(mPage), mCurrSortBy);
    }

    /**
     * update the movie data for MovieListWatcher with the default sorting method
     * @param watcher an object implementing the MovieListWatcher
     */
    public void updateMovie(MovieListWatcher watcher) {
        execute(watcher);
    }

    /**
     * Update the default sorting method with the specified sorting method, reset {@link #mPage}=0
     * and then update the movie data for {@link MovieListWatcher} with the new sorting method.
     * @param sort_by the way to sort the data
     * @param watcher an object implementing the MovieListWatcher
     */
    public void updateMovie(String sort_by, MovieListWatcher watcher) {
        setCurrSortBy(sort_by);
        mPage = 0;
        execute(watcher);
    }

    private class FetchMovieTask extends AsyncTask<String, Void, ArrayList<Movie>> {

        private final String LOG_TAG = FetchMovieTask.class.getSimpleName();

        private final String MOVIE_BASE_URL = "http://api.themoviedb.org/3/discover/movie?";
        private final String API_KEY = "api_key";
        private final String SORT_BY = "sort_by";
        private final String PAGE = "page";
        private final String VOTE_COUNT = "vote_count.gte";

        @Override
        protected ArrayList<Movie> doInBackground(String... params) {
            //params[0] is the mPage count
            return getMovieData(params);
        }

        @Override
        protected void onPostExecute(ArrayList<Movie> movieList) {
            if (movieList == null) {
                Log.d(LOG_TAG, "movieList is null");
                return;
            }
            MovieService.this.mMovieList.addAll(movieList);
            for (MovieListWatcher watcher : mMovieListWatchers) {
                watcher.listRefresh();
            }
            mMovieListWatchers.clear();
//                Log.v("onPostExecute After", "mMovieList Size: " + SERVICE.getMovieList().size()
//                        + " mMovieList Size: " + mMovieList.size());
        }

        private ArrayList<Movie> getMovieDataFromJson(String movieJsonStr)
                throws JSONException {

            final String MOVIE_RESULTS = "results";
            final String MOVIE_ID = "id";
            final String MOVIE_TITLE = "original_title";
            final String MOVIE_OVERVIEW = "overview";
            final String MOVIE_RELEASE_DATE = "release_date";
            final String MOVIE_POSTER = "poster_path";
            final String MOVIE_RATE = "vote_average";
            final String MOVIE_IMAGE_BASE_URL = "http://image.tmdb.org/t/p/";
            final String MOVIE_IMAGE_SIZE = "w185";


            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray moviewArray = movieJson.getJSONArray(MOVIE_RESULTS);

            ArrayList<Movie> movieList = new ArrayList<>();
            for (int i=0; i<moviewArray.length(); i++) {
                JSONObject movieInfo = moviewArray.getJSONObject(i);
                Movie movie = new Movie(movieInfo.getString(MOVIE_ID),
                        movieInfo.getString(MOVIE_TITLE));
                movie.setOverview(movieInfo.getString(MOVIE_OVERVIEW));
                movie.setReleaseDate(movieInfo.getString(MOVIE_RELEASE_DATE));
                movie.setRate(movieInfo.getString(MOVIE_RATE));
                if (!movieInfo.getString(MOVIE_POSTER).equals("null"))
                    movie.setPosterPath(MOVIE_IMAGE_BASE_URL
                            + MOVIE_IMAGE_SIZE
                            + movieInfo.getString(MOVIE_POSTER));
                //movie.setImage(downloadBitmap(MOVIE_IMAGE_BASE_URL + MOVIE_IMAGE_SIZE
                // + movieInfo.getString(MOVIE_POSTER)));
                movieList.add(movie);
            }
            return movieList;
        }

        private ArrayList<Movie> getMovieData(String... params) {
            String page = params[0];
            String sort_by = params[1];

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String movieJsonStr = null;

            try {
                Uri buildUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                        .appendQueryParameter(API_KEY, API_KEY_VALUE)
                        .appendQueryParameter(SORT_BY, sort_by)
                        .appendQueryParameter(PAGE, page)
                        .build();
                if (sort_by.equals("vote_average.desc"))
                    buildUri = buildUri.buildUpon()
                            .appendQueryParameter(VOTE_COUNT, MIN_VOTE_DEFAULT)
                            .build();


                URL url = new URL(buildUri.toString());
//                Log.v(LOG_TAG, "Build URI " + buildUri.toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                InputStream inputStream = urlConnection.getInputStream();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                StringBuffer buffer = new StringBuffer();
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + '\n');
                }

                if (buffer.length() == 0) {
                    return null;
                }
                movieJsonStr = buffer.toString();
//                Log.v(LOG_TAG, "Movie JSON string: " + movieJsonStr);

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getMovieDataFromJson(movieJsonStr);
            }catch (JSONException e) {
                Log.e(LOG_TAG, "Error", e);
                e.printStackTrace();
            }

            return null;
        }
    }

    public void updateMovieDetails(Movie movie, MovieDetailWatcher watcher) {
        mMovieDetailWatchers.add(watcher);
        FetchMovieDetailsTask fetchMovieDetailsTask = new FetchMovieDetailsTask();
        fetchMovieDetailsTask.execute(movie);
    }

    private class MoreDetails {
        String runtime;
        ArrayList<Video> videoList;
        ArrayList<Review> reviewList;
    }

    private class FetchMovieDetailsTask extends AsyncTask<Movie, Void, MoreDetails> {

        private final String LOG_TAG = FetchMovieDetailsTask.class.getSimpleName();
        private final String DETAIL_BASE_URL = "http://api.themoviedb.org/3/movie/";
        private final String API_KEY = "api_key";

        private Movie mMovie;

        @Override
        protected MoreDetails doInBackground(Movie... params) {
            mMovie = params[0];
            return getDetailsData(mMovie);
        }

        @Override
        protected void onPostExecute(MoreDetails details) {
            if (details != null) {
                mMovie.setRuntime(details.runtime);
                mMovie.setVideoList(details.videoList);
                mMovie.setReviewList(details.reviewList);
            }

            for (MovieDetailWatcher watcher : mMovieDetailWatchers) {
                watcher.detailRefresh(mMovie);
            }
            mMovieDetailWatchers.clear();
        }

        private String getMovieBaseURL(Movie movie) {
            return DETAIL_BASE_URL + movie.getId();
        }

        private String getVideoBaseURL(String base) {
            return base + "/videos?";
        }

        private String getReviewBaseURL(String base) {
            return base + "/reviews?";
        }

        private MoreDetails getDetailsData(Movie movie) {
            String BASE_URL = getMovieBaseURL(movie);
            String MOVIE_URL = BASE_URL + '?';
            String VIDEO_URL = getVideoBaseURL(BASE_URL);
            String REVIEW_URL = getReviewBaseURL(BASE_URL);

            String runtimeJsonStr = getData(MOVIE_URL);
            String videoJsonStr = getData(VIDEO_URL);
            String reviewJsonStr = getData(REVIEW_URL);

//            movie.setPosterImg(downloadImage(movie.getPosterPath()));

            String runtime;
            ArrayList<Video> videoList;
            ArrayList<Review> reviewList;

            try {
                runtime = getRuntimeFromJsonStr(runtimeJsonStr);
                videoList = getVideoFromJsonStr(videoJsonStr);
                reviewList = getReviewFromJsonStr(reviewJsonStr);

                MoreDetails details = new MoreDetails();
                details.runtime = runtime;
                details.videoList = videoList;
                details.reviewList = reviewList;

                return details;
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Error", e);
                e.printStackTrace();
            }

            return null;
        }

        public String getRuntimeFromJsonStr(String runtimeJsonStr) throws JSONException {
            final String MOVIE_RUNTIME = "runtime";
            JSONObject runtimeJson = new JSONObject(runtimeJsonStr);
            String runtime = runtimeJson.getString(MOVIE_RUNTIME);
            return runtime;
        }

        private ArrayList<Video> getVideoFromJsonStr(String videoJsonStr) throws JSONException {
            final String VIDEO_RESULTS = "results";
            final String VIDEO_NAME = "name";
            final String VIDEO_KEY = "key";

            JSONObject videoJson = new JSONObject(videoJsonStr);
            JSONArray videoArray = videoJson.getJSONArray(VIDEO_RESULTS);

            ArrayList<Video> videoList = new ArrayList<>();

            for (int i = 0; i < videoArray.length(); i++) {
                JSONObject videoInfo = videoArray.getJSONObject(i);
                Video video = new Video(videoInfo.getString(VIDEO_NAME),
                        videoInfo.getString(VIDEO_KEY));
                videoList.add(video);
            }
            return videoList;
        }

        private ArrayList<Review> getReviewFromJsonStr(String reviewJsonStr) throws JSONException {
            final String REVIEW_RESULTS = "results";
            final String REVIEW_ID = "id";
            final String REVIEW_AUTHOR = "author";
            final String REVIEW_CONTENT = "content";

            JSONObject reviewJson = new JSONObject(reviewJsonStr);
            JSONArray reviewArray = reviewJson.getJSONArray(REVIEW_RESULTS);

            ArrayList<Review> reviewList = new ArrayList<>();

            for (int i = 0; i < reviewArray.length(); i++) {
                JSONObject reviewInfo = reviewArray.getJSONObject(i);
                Review review = new Review(reviewInfo.getString(REVIEW_ID),
                        reviewInfo.getString(REVIEW_AUTHOR),
                        reviewInfo.getString(REVIEW_CONTENT));
                reviewList.add(review);
            }
            return reviewList;
        }

        private String getData(String urlStr) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String detailJsonStr = null;

            try {

                Uri buildUri = Uri.parse(urlStr).buildUpon()
                        .appendQueryParameter(API_KEY, API_KEY_VALUE)
                        .build();
                URL url = new URL(buildUri.toString());
//                Log.v(LOG_TAG, "Build URI " + buildUri.toString());

                urlConnection = (HttpURLConnection)url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                StringBuffer buffer = new StringBuffer();
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + '\n');
                }

                if (buffer.length() == 0) {
                    return null;
                }
                detailJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error", e);
                return null;
            }
            finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            return detailJsonStr;
        }
    }
}

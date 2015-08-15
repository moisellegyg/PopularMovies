package com.yugegong.popularmovies;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

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

    private void changeSorting(String sort_by) {
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
        changeSorting(sort_by);
        mPage = 0;
        execute(watcher);
    }

    private class FetchMovieTask extends AsyncTask<String, Void, ArrayList<Movie>> {

        private final String LOG_TAG = FetchMovieTask.class.getSimpleName();

        private final String MOVIE_BASE_URL = "http://api.themoviedb.org/3/discover/movie?";
        private final String API_KEY = "api_key";
        private final String API_KEY_VALUE = "YOUR_API_KEY";
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


        /**
         * Use this method if not using Picasso Library.
         * @param uriString
         * @return
         */
/*        private Bitmap downloadBitmap(String uriString) {
            HttpURLConnection urlConnection = null;
            Bitmap bitmap = null;
            try {
                URL url = new URL(uriString);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                bitmap = BitmapFactory.decodeStream(bufferedInputStream);
                bufferedInputStream.close();
                inputStream.close();

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error dowloading image", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }

            return bitmap;


        }*/
    }

    public void updateMovieDetails(Movie movie, MovieDetailWatcher watcher) {
        mMovieDetailWatchers.add(watcher);
        FetchMovieDetailsTask fetchMovieDetailsTask = new FetchMovieDetailsTask();
        fetchMovieDetailsTask.execute(movie);
    }

    private class FetchMovieDetailsTask extends AsyncTask<Movie, Void, String> {

        private final String LOG_TAG = FetchMovieDetailsTask.class.getSimpleName();
        private final String DETAIL_BASE_URL = "http://api.themoviedb.org/3/movie/";
        private final String API_KEY = "api_key";
        private final String API_KEY_VALUE = "YOUR_API_KEY";

        private Movie mMovie;


        @Override
        protected String doInBackground(Movie... params) {
            mMovie = params[0];
            return getDetailsData(mMovie);
        }

        @Override
        protected void onPostExecute(String runtime) {
            mMovie.setRuntime(runtime);
            for (MovieDetailWatcher watcher : mMovieDetailWatchers) {
                watcher.detailRefresh(mMovie);
            }
            mMovieDetailWatchers.clear();
        }

        private String getMovieBaseURL(Movie movie) {
            return DETAIL_BASE_URL + movie.getId() + '?';
        }

        private String getDetailsData(Movie movie) {
            String MOVIE_URL = getMovieBaseURL(movie);

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String detailJsonStr = null;

            try {

                Uri buildUri = Uri.parse(MOVIE_URL).buildUpon()
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

            try {
                return getDetailFromJsonStr(detailJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Error", e);
                e.printStackTrace();
            }

            return  null;
        }

        public String getDetailFromJsonStr(String detailJsonStr) throws JSONException {
            final String MOVIE_RUNTIME = "runtime";

            JSONObject detailJson = new JSONObject(detailJsonStr);
            String runtime = detailJson.getString(MOVIE_RUNTIME);

            return runtime;
        }
    }
}

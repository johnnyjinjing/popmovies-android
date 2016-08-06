package com.johnnyjinjing.popmovies;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.johnnyjinjing.popmovies.data.MovieContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

// Get poster and info of movies from TheMovieDB
public class GetMoviesTask extends AsyncTask<Void, Void, Void> {

    // Tag for debugging
    private final String LOG_TAG = GetMoviesTask.class.getSimpleName();

    private final Context mContext;

    public GetMoviesTask(Context context) {
        mContext = context;
    }

    // Declare outside the try/catch so that they can be closed in the finally block.
    HttpURLConnection urlConnection = null;
    BufferedReader reader = null;

    final String MOVIEDB_BASE_URL = "https://api.themoviedb.org/3/movie/";
    final String API_KEY_PARAM = "api_key";

    // Names of the JSON objects that need to be extracted.
    final String TMDB_RESULTS = "results";
    final String TMDB_POSTER_PATH = "poster_path";
    final String TMDB_ORIGINAL_TITLE = "original_title";
    final String TMDB_PLOT = "overview";
    final String TMDB_RATING = "vote_average";
    final String TMDB_DATE = "release_date";
    final String TMDB_POPULARITY = "popularity";
    final String TMDB_ID = "id";
    final String TMDB_KEY = "key";
    final String TMDB_NAME = "name";
    final String TMDB_CONTENT = "content";
    final String TMDB_AUTHOR = "author";
    final String TMDB_TRAILER_PATH = "videos";
    final String TMDB_REVIEW_PATH = "reviews";


    @Override
    protected Void doInBackground(Void... params) {
        // Get preferences
//            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
//            String sort = prefs.getString(getString(R.string.pref_sort_key),
//                    getString(R.string.pref_sort_popularity));
        String sort = "popularity";

        // Construct URL for movie query
        // https://api.themoviedb.org/3/movie/popular?api_key=API_KEY

        String sortMethod;
        if (sort.equals("popularity")) {
            sortMethod = "popular?";
        } else {
            sortMethod = "top_rated?";
        }

        Uri uri = Uri.parse(MOVIEDB_BASE_URL + sortMethod).buildUpon()
                .appendQueryParameter(API_KEY_PARAM, BuildConfig.THE_MOVIE_DB_API_KEY)
                .build();

        URL url = null;
        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        String popMoviesJsonStr = getOnlineData(url);
        // Parse JSON result
        getMoviesDataFromJson(popMoviesJsonStr);
        return null;

    }
/*
        @Override
        protected void onPostExecute(Movie[] movies) {
            movieAdapter.clear();
            for (Movie movie:movies) {
                movieAdapter.add(movie);
            }
            return;
        }*/

    private void getMoviesDataFromJson(String popMoviesJsonStr) {

        try {
            JSONObject popMoviesJson = new JSONObject(popMoviesJsonStr);
            // JSONArray of movies
            JSONArray popMoviesArray = popMoviesJson.getJSONArray(TMDB_RESULTS);

            // An array of values for bulkInsert
            ContentValues[] movieCvArray = new ContentValues[popMoviesArray.length()];

//            Movie[] movies = new Movie[popMoviesArray.length()];

            // For each movie in the array, get essential info and create a ContentValue
            for (int i = 0; i < popMoviesArray.length(); i++) {
                JSONObject movieJsonObj = popMoviesArray.getJSONObject(i);

                /* Create Movie object (used in previous version)
                    movies[i] = new Movie(movieJsonObj.getString(TMDB_POSTER_PATH),
                            movieJsonObj.getString(TMDB_ORIGINAL_TITLE),
                            movieJsonObj.getString(TMDB_PLOT),
                            movieJsonObj.getDouble(TMDB_RATING),
                            movieJsonObj.getString(TMDB_DATE));
                */
                ContentValues movieValue = new ContentValues();
                movieValue.put(MovieContract.MovieEntry.COLUMN_NAME_POSTER_PATH, movieJsonObj.getString(TMDB_POSTER_PATH));
                movieValue.put(MovieContract.MovieEntry.COLUMN_NAME_ORIGINAL_TITLE, movieJsonObj.getString(TMDB_ORIGINAL_TITLE));
                movieValue.put(MovieContract.MovieEntry.COLUMN_NAME_PLOT, movieJsonObj.getString(TMDB_PLOT));
                movieValue.put(MovieContract.MovieEntry.COLUMN_NAME_RATING, movieJsonObj.getDouble(TMDB_RATING));
                movieValue.put(MovieContract.MovieEntry.COLUMN_NAME_POPULARITY, movieJsonObj.getDouble(TMDB_POPULARITY));
                movieValue.put(MovieContract.MovieEntry.COLUMN_NAME_RELEASE_DATE, movieJsonObj.getString(TMDB_DATE));
                movieValue.put(MovieContract.MovieEntry.COLUMN_NAME_ID, movieJsonObj.getInt(TMDB_ID));
                movieCvArray[i] = movieValue;
            }

            // Insert into database
            if (movieCvArray.length > 0) {
                mContext.getContentResolver().bulkInsert(MovieContract.MovieEntry.CONTENT_URI, movieCvArray);
                getMoreData(movieCvArray);
            }
        } catch (JSONException e) {
            return;
        }
        return;
    }

    private void getMoreData(ContentValues[] cvs) throws JSONException {
        List<ContentValues> trailerCvVector = new ArrayList<ContentValues>();
        List<ContentValues> reviewCvVector = new ArrayList<ContentValues>();

        for (ContentValues cv : cvs) {
            long movie_id = cv.getAsLong(MovieContract.MovieEntry.COLUMN_NAME_ID);
//            Log.d(LOG_TAG, "Movie ID is: " + movie_id);

            // Trailer URL: https://api.themoviedb.org/3/movie/movie_id/videos?api_key=***
            Uri uri = Uri.parse(MOVIEDB_BASE_URL).buildUpon()
                    .appendPath(Long.toString(movie_id))
                    .appendPath(TMDB_TRAILER_PATH)
                    .appendQueryParameter(API_KEY_PARAM, BuildConfig.THE_MOVIE_DB_API_KEY)
                    .build();
            URL url = null;
            try {
                url = new URL(uri.toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            String dataString = getOnlineData(url);
            JSONObject dataJson = new JSONObject(dataString);
            JSONArray dataArray = dataJson.getJSONArray(TMDB_RESULTS);

            for (int i = 0; i < dataArray.length(); i++) {
                JSONObject movieJsonObj = dataArray.getJSONObject(i);
                ContentValues movieValue = new ContentValues();
                movieValue.put(MovieContract.TrailerEntry.COLUMN_NAME_TRAILER_KEY, movieJsonObj.getString(TMDB_KEY));
                movieValue.put(MovieContract.TrailerEntry.COLUMN_NAME_TRAILER_ID, movieJsonObj.getString(TMDB_ID));
                movieValue.put(MovieContract.TrailerEntry.COLUMN_NAME_TRAILER_NAME, movieJsonObj.getString(TMDB_NAME));
                movieValue.put(MovieContract.TrailerEntry.COLUMN_KEY_MOVIE, movie_id);
//                Log.d(LOG_TAG, movieJsonObj.getString(TMDB_KEY) + " " + movieJsonObj.getString(TMDB_ID) +
//                        " " + movieJsonObj.getString(TMDB_NAME));
//                trailerCvArray[i] = movieValue;
                trailerCvVector.add(movieValue);
            }

            // Review URL: https://api.themoviedb.org/3/movie/movie_id/reviews?api_key=***
            uri = Uri.parse(MOVIEDB_BASE_URL).buildUpon()
                    .appendPath(Long.toString(movie_id))
                    .appendPath(TMDB_REVIEW_PATH)
                    .appendQueryParameter(API_KEY_PARAM, BuildConfig.THE_MOVIE_DB_API_KEY)
                    .build();
            url = null;
            try {
                url = new URL(uri.toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            dataString = getOnlineData(url);
            dataJson = new JSONObject(dataString);
            dataArray = dataJson.getJSONArray(TMDB_RESULTS);

            for (int i = 0; i < dataArray.length(); i++) {
                JSONObject movieJsonObj = dataArray.getJSONObject(i);
                ContentValues movieValue = new ContentValues();
                movieValue.put(MovieContract.ReviewEntry.COLUMN_NAME_REVIEW_CONTENT, movieJsonObj.getString(TMDB_CONTENT));
                movieValue.put(MovieContract.ReviewEntry.COLUMN_NAME_REVIEW_AUTHOR, movieJsonObj.getString(TMDB_AUTHOR));
                movieValue.put(MovieContract.ReviewEntry.COLUMN_NAME_REVIEW_ID, movieJsonObj.getString(TMDB_ID));
                movieValue.put(MovieContract.ReviewEntry.COLUMN_KEY_MOVIE, movie_id);
//                reviewCvArray[i] = movieValue;
                reviewCvVector.add(movieValue);
            }
        }

//        for (ContentValues value : trailerCvArray) {
//            if (value.getAsString(MovieContract.TrailerEntry.COLUMN_NAME_TRAILER_KEY) != null) {
//                Log.d(LOG_TAG, value.getAsString(MovieContract.TrailerEntry.COLUMN_NAME_TRAILER_KEY));
//            }
//        }

//        if (trailerCvArray.length > 0) {
//            mContext.getContentResolver().bulkInsert(MovieContract.TrailerEntry.CONTENT_URI, trailerCvArray);
//        }
//
//        if (reviewCvArray.length > 0) {
//            mContext.getContentResolver().bulkInsert(MovieContract.ReviewEntry.CONTENT_URI, reviewCvArray);
//        }
        if (trailerCvVector.size() > 0) {
            ContentValues[] trailerCvArray = new ContentValues[trailerCvVector.size()];
            trailerCvVector.toArray(trailerCvArray);
            mContext.getContentResolver().bulkInsert(MovieContract.TrailerEntry.CONTENT_URI, trailerCvArray);
        }
        if (reviewCvVector.size() > 0) {
            ContentValues[] reviewCvArray = new ContentValues[reviewCvVector.size()];
            reviewCvVector.toArray(reviewCvArray);
            mContext.getContentResolver().bulkInsert(MovieContract.ReviewEntry.CONTENT_URI, reviewCvArray);
        }
    }


    private String getOnlineData(URL url) {
        String receivedJsonStr;
        try {
            // Create the request and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing received
                return null;
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Add newline, easy for debugging
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Empty stream
                return null;
            }

            receivedJsonStr = buffer.toString();
            return receivedJsonStr;

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
        return null;
    }
}


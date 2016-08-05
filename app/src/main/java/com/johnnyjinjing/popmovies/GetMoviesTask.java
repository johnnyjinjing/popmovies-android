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
import java.net.URL;

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

    @Override
    protected Void doInBackground(Void... params) {
        try {
            // Get preferences
//            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
//            String sort = prefs.getString(getString(R.string.pref_sort_key),
//                    getString(R.string.pref_sort_popularity));
            String sort = "popularity";

            // Construct URL for movie query
            // https://api.themoviedb.org/3/movie/popular?api_key=API_KEY
            final String MOVIEDB_BASE_URL =
                    "https://api.themoviedb.org/3/movie/";
            String sortMethod;
            if (sort.equals("popularity")) {
                sortMethod = "popular?";
            } else {
                sortMethod = "top_rated?";
            }
            final String API_KEY_PARAM = "api_key";

            Uri uri = Uri.parse(MOVIEDB_BASE_URL + sortMethod).buildUpon()
                    .appendQueryParameter(API_KEY_PARAM, BuildConfig.THE_MOVIE_DB_API_KEY)
                    .build();

            URL url = new URL(uri.toString());

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

            String popMoviesJsonStr = buffer.toString();
            // Parse JSON result
            getMoviesDataFromJson(popMoviesJsonStr);
            return null;

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

        // Names of the JSON objects that need to be extracted.
        final String TMDB_RESULTS = "results";
        final String TMDB_POSTER_PATH = "poster_path";
        final String TMDB_ORIGINAL_TITLE = "original_title";
        final String TMDB_PLOT = "overview";
        final String TMDB_RATING = "vote_average";
        final String TMDB_DATE = "release_date";
        final String TMDB_POPULARITY = "popularity";
        final String TMDB_ID = "id";

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
//                    movies[i] = new Movie(movieJsonObj.getString(TMDB_POSTER_PATH),
//                            movieJsonObj.getString(TMDB_ORIGINAL_TITLE),
//                            movieJsonObj.getString(TMDB_PLOT),
//                            movieJsonObj.getDouble(TMDB_RATING),
//                            movieJsonObj.getString(TMDB_DATE));
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
            }
        } catch (JSONException e) {
            return;
        }
        return;
    }
}

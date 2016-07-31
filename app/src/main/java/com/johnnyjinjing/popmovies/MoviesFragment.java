package com.johnnyjinjing.popmovies;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;

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
import java.util.Arrays;
import java.util.List;


public class MoviesFragment extends Fragment {

//    private TextView textView;
    private final String LOG_TAG = MoviesFragment.class.getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_movies, container, false);

        GetMoviesTask getMoviesTask = new GetMoviesTask();
        getMoviesTask.execute();

//        textView = (TextView) rootView.findViewById(R.id.textView);

        String[] array = {"test1", "test2", "test3", "test4", "test5"};
        List<String> myStringArray = new ArrayList<String>(Arrays.asList(array));
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                R.layout.grid_item_poster, R.id.grid_item_poster_textview, myStringArray);

        Log.d(LOG_TAG, myStringArray.toString());

        GridView gridView = (GridView) rootView.findViewById(R.id.gridview_poster);
        gridView.setAdapter(adapter);

//        ImageView imageView = (ImageView) rootView.findViewById(R.id.imageView);

//        Picasso.with(getContext())
//                .load("https://cms-assets.tutsplus.com/uploads/users/21/posts/19431/featured_image/CodeFeature.jpg")
//                .into(imageView);
        return rootView;
    }

    // Get poster and info of movies from TheMovieDB
    public class GetMoviesTask extends AsyncTask<Void, Void, Movie[]> {

        // Tag for debugging
        private final String LOG_TAG = GetMoviesTask.class.getSimpleName();

        // Declare outside the try/catch so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        @Override
        protected Movie[] doInBackground(Void... params) {
            try {

                // Construct URL for movie query
                // https://api.themoviedb.org/3/movie/popular?api_key=API_KEY
                final String MOVIEDB_BASE_URL =
                        "https://api.themoviedb.org/3/movie/popular?";
                final String API_KEY_PARAM = "api_key";

                Uri uri = Uri.parse(MOVIEDB_BASE_URL).buildUpon()
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
                return getMoviesDataFromJson(popMoviesJsonStr);

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

        @Override
        protected void onPostExecute(Movie[] movies) {
            return;

        }

        private Movie[] getMoviesDataFromJson(String popMoviesJsonStr) {

            // Names of the JSON objects that need to be extracted.
            final String TMDB_RESULTS = "results";
            final String TMDB_POSTER_PATH = "poster_path";
            final String TMDB_ORIGINAL_TITLE = "original_title";
            final String TMDB_PLOT= "overview";
            final String TMDB_RATING = "vote_average";
            final String TMDB_DATE = "release_date";

            Movie[] movies;

            try {
                JSONObject popMoviesJson = new JSONObject(popMoviesJsonStr);
                // JSONArray of movies
                JSONArray popMoviesArray = popMoviesJson.getJSONArray(TMDB_RESULTS);
                movies = new Movie[popMoviesArray.length()];

                // For each movie in the array, get essential info and create a Movie Object
                for (int i = 0; i < popMoviesArray.length(); i++) {
                    JSONObject movieJsonObj = popMoviesArray.getJSONObject(i);
                    movies[i] = new Movie (movieJsonObj.getString(TMDB_POSTER_PATH),
                            movieJsonObj.getString(TMDB_ORIGINAL_TITLE),
                            movieJsonObj.getString(TMDB_PLOT),
                            movieJsonObj.getDouble(TMDB_RATING),
                            movieJsonObj.getString(TMDB_DATE));
                }
                return movies;
            } catch (JSONException e) {
                return null;
            }
        }
    }
}
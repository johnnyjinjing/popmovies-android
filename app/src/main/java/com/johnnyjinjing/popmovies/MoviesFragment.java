package com.johnnyjinjing.popmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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


public class MoviesFragment extends Fragment {

    private final String LOG_TAG = MoviesFragment.class.getSimpleName();


    private MovieAdapter movieAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
//        setHasOptionsMenu(true);
    }

    /*
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.movies_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updateMovies();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_movies, container, false);

        movieAdapter = new MovieAdapter(getActivity(), new ArrayList<Movie>());

        GridView gridView = (GridView) rootView.findViewById(R.id.gridview_poster);
        gridView.setAdapter(movieAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Movie movie = movieAdapter.getItem(position);
                Intent intent = new Intent(getActivity(), MovieActivity.class)
                        .putExtra("movie_detail", movie);
                startActivity(intent);
            }
        });

        return rootView;
    }

    public void onStart() {
        super.onStart();
        updateMovies();
    }

    private void updateMovies() {
        GetMoviesTask getMoviesTask = new GetMoviesTask();
        getMoviesTask.execute();
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
                // Parse JSON result
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
            movieAdapter.clear();
            for (Movie movie:movies) {
                movieAdapter.add(movie);
            }
            return;
        }

        private Movie[] getMoviesDataFromJson(String popMoviesJsonStr) {

            // Names of the JSON objects that need to be extracted.
            final String TMDB_RESULTS = "results";
            final String TMDB_POSTER_PATH = "poster_path";
            final String TMDB_ORIGINAL_TITLE = "original_title";
            final String TMDB_PLOT = "overview";
            final String TMDB_RATING = "vote_average";
            final String TMDB_DATE = "release_date";

            try {
                JSONObject popMoviesJson = new JSONObject(popMoviesJsonStr);
                // JSONArray of movies
                JSONArray popMoviesArray = popMoviesJson.getJSONArray(TMDB_RESULTS);
                Movie[] movies = new Movie[popMoviesArray.length()];

                // For each movie in the array, get essential info and create a Movie Object
                for (int i = 0; i < popMoviesArray.length(); i++) {
                    JSONObject movieJsonObj = popMoviesArray.getJSONObject(i);
                    movies[i] = new Movie(movieJsonObj.getString(TMDB_POSTER_PATH),
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
package com.johnnyjinjing.popmovies;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

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


public class MoviesFragment extends Fragment {

    private final String LOG_TAG = MoviesFragment.class.getSimpleName();

    private MovieCursorAdapter movieCursorAdapter;

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
        String sortOrder = MovieContract.MovieEntry.COLUMN_NAME_RATING + " DESC";
        Cursor cur = getActivity().getContentResolver().query(MovieContract.MovieEntry.CONTENT_URI,
                null, null, null, sortOrder);
        GridView gridView = (GridView) rootView.findViewById(R.id.gridview_poster);
        gridView.setAdapter(movieCursorAdapter);

        /*
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
        */
        return rootView;
    }

    public void onStart() {
        super.onStart();
        updateMovies();
    }

    public void onResume() {
        super.onResume();
        updateMovies();
    }

    private void updateMovies() {
        GetMoviesTask getMoviesTask = new GetMoviesTask(getContext());
        getMoviesTask.execute();
    }

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
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                String sort = prefs.getString(getString(R.string.pref_sort_key),
                        getString(R.string.pref_sort_popularity));

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
//                return getMoviesDataFromJson(popMoviesJsonStr);

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
//                    movies[i] = new Movie(movieJsonObj.getString(TMDB_POSTER_PATH),
//                            movieJsonObj.getString(TMDB_ORIGINAL_TITLE),
//                            movieJsonObj.getString(TMDB_PLOT),
//                            movieJsonObj.getDouble(TMDB_RATING),
//                            movieJsonObj.getString(TMDB_DATE));
                    ContentValues movieValues = new ContentValues();
                    movieValues.put(MovieContract.MovieEntry.COLUMN_NAME_POSTER_PATH, movieJsonObj.getString(TMDB_POSTER_PATH));
                    movieValues.put(MovieContract.MovieEntry.COLUMN_NAME_ORIGINAL_TITLE, movieJsonObj.getString(TMDB_ORIGINAL_TITLE));
                    movieValues.put(MovieContract.MovieEntry.COLUMN_NAME_PLOT, movieJsonObj.getString(TMDB_PLOT));
                    movieValues.put(MovieContract.MovieEntry.COLUMN_NAME_RATING, movieJsonObj.getString(TMDB_RATING));
                    movieValues.put(MovieContract.MovieEntry.COLUMN_NAME_RELEASE_DATE, movieJsonObj.getString(TMDB_DATE));

                    mContext.getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI, movieValues);

                }

                return movies;
            } catch (JSONException e) {
                return null;
            }
        }
    }
}
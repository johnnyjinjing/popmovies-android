package com.johnnyjinjing.popmovies;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.johnnyjinjing.popmovies.data.MovieContract;

/**
 * Need to import these packages:
 * android.support.v4.app.Fragment;
 * android.support.v4.app.LoaderManager;
 * android.support.v4.content.CursorLoader;
 * android.support.v4.content.Loader;
 */
public class MoviesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private final String LOG_TAG = MoviesFragment.class.getSimpleName();

    private MoviesCursorAdapter moviesCursorAdapter;

    private static final int MOVIE_LOADER = 0;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        public void onItemSelected(long id);
    }

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
        // The cursor adapter takes data from cursor and populate the View.
        moviesCursorAdapter = new MoviesCursorAdapter(getActivity(), null, 0);

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_movies, container, false);

        GridView gridView = (GridView) rootView.findViewById(R.id.gridview_poster);
        gridView.setAdapter(moviesCursorAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // Cursor adapter returns a cursor at position for getItem(), or null
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    int col = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_NAME_ID);
                    /* Use Intent to start a new activity (used in old version)
                    Intent intent = new Intent(getActivity(), MovieActivity.class)
                            .putExtra("movie_id", cursor.getInt(col));
                    startActivity(intent);
                    */
                    // Using a callback to launch new activity
                    ((Callback) getActivity()).onItemSelected(cursor.getInt(col));
                }
            }
        });

        /* Use Movie object (in previous version)
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
        // Re-create the cursor loader in case Settings has been changed
        getLoaderManager().restartLoader(MOVIE_LOADER, null, this);
        super.onResume();
        updateMovies();
    }

    private void updateMovies() {
        // Get preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sort = prefs.getString(getString(R.string.pref_sort_key),
                getString(R.string.pref_sort_popularity));
        if (sort.equals(getResources().getString(R.string.pref_sort_popularity)) ||
                sort.equals(getResources().getString(R.string.pref_sort_rating))) {
            GetMoviesTask getMoviesTask = new GetMoviesTask(getContext());
            getMoviesTask.execute(sort);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(MOVIE_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sort = prefs.getString(getString(R.string.pref_sort_key),
                getString(R.string.pref_sort_popularity));
        String sortOrder = null;
        String favoriteSelection = null;
        String[] favoriteValue = null;

        // Initialize the cursor loader
        if (sort.equals(getResources().getString(R.string.pref_sort_popularity))) {
            sortOrder = MovieContract.MovieEntry.COLUMN_NAME_POPULARITY + " DESC";
        } else if (sort.equals(getResources().getString(R.string.pref_sort_rating))) {
            sortOrder = MovieContract.MovieEntry.COLUMN_NAME_RATING + " DESC";
        } else if (sort.equals(getResources().getString(R.string.pref_sort_favorite))) {
            sortOrder = MovieContract.MovieEntry.COLUMN_NAME_RATING + " DESC";
            favoriteSelection = MovieContract.MovieEntry.TABLE_NAME + "." +
                    MovieContract.MovieEntry.COLUMN_NAME_FAVORITE + " = ? ";
            favoriteValue = new String[]{"1"};
        }
        Uri uri = MovieContract.MovieEntry.CONTENT_URI;

        return new CursorLoader(getActivity(), uri, null, favoriteSelection, favoriteValue, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        moviesCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        moviesCursorAdapter.swapCursor(null);
    }
}
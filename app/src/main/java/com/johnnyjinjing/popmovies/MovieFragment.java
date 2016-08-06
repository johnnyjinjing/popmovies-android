package com.johnnyjinjing.popmovies;


import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.johnnyjinjing.popmovies.data.MovieContract;
import com.squareup.picasso.Picasso;

public class MovieFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private final String LOG_TAG = MovieFragment.class.getSimpleName();

    static final String MOVIE_ID = "movie_id";

    private long movie_id;

    private static final int MOVIE_LOADER = 0;
    private static final int TRAILER_LOADER = 1;
    private static final int REVIEW_LOADER = 2;

    private static final String[] MOVIE_COLUMNS = {
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_NAME_ORIGINAL_TITLE,
            MovieContract.MovieEntry.COLUMN_NAME_POSTER_PATH,
            MovieContract.MovieEntry.COLUMN_NAME_PLOT,
            MovieContract.MovieEntry.COLUMN_NAME_RATING,
            MovieContract.MovieEntry.COLUMN_NAME_RELEASE_DATE,
            MovieContract.MovieEntry.COLUMN_NAME_FAVORITE,
    };
    private static final int COL_MOVIE_ID = 0;
    private static final int COL_MOVIE_ORIGINAL_TITLE = 1;
    private static final int COL_MOVIE_POSTER_PATH = 2;
    private static final int COL_MOVIE_PLOT = 3;
    private static final int COL_MOVIE_RATING = 4;
    private static final int COL_MOVIE_RELEASE_DATE = 5;
    private static final int COL_MOVIE_FAVORITE = 6;

    private static final String[] TRAILER_COLUMNS = {
            MovieContract.TrailerEntry.COLUMN_NAME_TRAILER_KEY,
            MovieContract.TrailerEntry.COLUMN_NAME_TRAILER_NAME,
    };
    private static final int COL_TRAILER_KEY = 0;
    private static final int COL_TRAILER_NAME = 1;

    private static final String[] REVIEW_COLUMNS = {
            MovieContract.ReviewEntry.COLUMN_NAME_REVIEW_CONTENT,
            MovieContract.ReviewEntry.COLUMN_NAME_REVIEW_AUTHOR,
    };
    private static final int COL_REIVEW_CONTENT = 0;
    private static final int COL_REIVEW_AUTHOR = 1;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
//        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();

        if (arguments != null) {
            movie_id = arguments.getLong(MOVIE_ID);
        }

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_movie, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(MOVIE_LOADER, null, this);
        getLoaderManager().initLoader(TRAILER_LOADER, null, this);
        getLoaderManager().initLoader(REVIEW_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
//        Intent intent = getActivity().getIntent();
//        if (intent == null || !intent.hasExtra(MovieFragment.MOVIE_ID)) {
//            return null;
//        }

        // Initialize the cursor loader
        String sortOrder = MovieContract.MovieEntry.COLUMN_NAME_POPULARITY + " DESC";
//        int movie_id = intent.getIntExtra(MovieFragment.MOVIE_ID, -1);
        Uri uri;
        if (movie_id <= 0) return null;

        switch (id) {
            case MOVIE_LOADER:
                uri = MovieContract.MovieEntry.buildMovieUri(movie_id);
                return new CursorLoader(getActivity(), uri, MOVIE_COLUMNS, null, null, sortOrder);
            case TRAILER_LOADER:
                uri = MovieContract.MovieEntry.buildMovieWithTrailerUri(movie_id);
//                Log.i(LOG_TAG, uri.toString());
                return new CursorLoader(getActivity(), uri, TRAILER_COLUMNS, null, null, null);
//                uri = MovieContract.TrailerEntry.buildTrailerUri(id);
//                return new CursorLoader(getActivity(), uri, TRAILER_COLUMNS, null, null, null);
            case REVIEW_LOADER:
                uri = MovieContract.MovieEntry.buildMovieWithReviewUri(movie_id);
//                Log.i(LOG_TAG, uri.toString());
                return new CursorLoader(getActivity(), uri, REVIEW_COLUMNS, null, null, null);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (!data.moveToFirst()) {
            return;
        }

        View rootView = getView();

        switch (loader.getId()) {
            case MOVIE_LOADER:
                final String POSTER_BASE_URL = "http://image.tmdb.org/t/p/";
                final String POSTER_WIDTH = "w185";

                ((TextView) rootView.findViewById(R.id.text_original_title)).setText(data.getString(COL_MOVIE_ORIGINAL_TITLE));
                ((TextView) rootView.findViewById(R.id.text_plot)).setText(data.getString(COL_MOVIE_PLOT));
                ((TextView) rootView.findViewById(R.id.text_rating)).setText(Double.toString(data.getDouble(COL_MOVIE_RATING)));
                ((TextView) rootView.findViewById(R.id.text_release_date)).setText(data.getString(COL_MOVIE_RELEASE_DATE));

                ImageView thumbnailView = (ImageView) rootView.findViewById(R.id.image_poster_thumbnail);
                String posterUrlStr = POSTER_BASE_URL + POSTER_WIDTH + data.getString(COL_MOVIE_POSTER_PATH);
                Picasso.with(getContext()).load(posterUrlStr).into(thumbnailView);
                break;

            case TRAILER_LOADER:
                ((TextView) rootView.findViewById(R.id.trailer_url)).setText(data.getString(COL_TRAILER_KEY));
                break;
            case REVIEW_LOADER:
                ((TextView) rootView.findViewById(R.id.review_url)).setText(data.getString(COL_REIVEW_CONTENT));
            default:
                return;
        }
        return;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}

package com.johnnyjinjing.popmovies;


import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

    private TextView originalTitleTextView;
    private TextView plotTextView;
    private TextView ratingTextView;
    private TextView dateTextView;
    private ImageView thumbnailView;
    private View separaterTrailerView;
    private View separaterReviewView;
    private TextView trailerLabelView;
    private LinearLayout trailerLinearLayout;
    private TextView reviewLabelView;
    private LinearLayout reviewLinearLayout;
    private CheckBox favoriteCheckbox;

    static final String INTENT_EXTRA_TRAILER_KEY = "trailer_key";

    private static final String YOUTUBE_BASE_URL = "https://www.youtube.com/watch?";
    private static final String YOUTUBE_VIDEO_PARAM = "v";

    private static final String MOVIE_SELECT_STRING = MovieContract.MovieEntry.TABLE_NAME + "." +
            MovieContract.MovieEntry.COLUMN_NAME_ID + " = ? ";

    private String trailerUrl;

//    Cursor currentMovieCursor = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        super.onCreateOptionsMenu(menu, inflater);
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_movie, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        ShareActionProvider mShareActionProvider =
                (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        // Attach an intent to this ShareActionProvider.  You can update this at any time,
        // like when the user selects a new piece of data they might like to share.
        if (mShareActionProvider != null ) {
            mShareActionProvider.setShareIntent(createShareIntent());
        }
//        else {
//            Log.d(LOG_TAG, "Share Action Provider is null?");
//        }
    }

    private Intent createShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                trailerUrl);
        return shareIntent;
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

        originalTitleTextView = (TextView) rootView.findViewById(R.id.text_original_title);
        plotTextView = (TextView) rootView.findViewById(R.id.text_plot);
        ratingTextView = (TextView) rootView.findViewById(R.id.text_rating);
        dateTextView = (TextView) rootView.findViewById(R.id.text_release_date);
        thumbnailView = (ImageView) rootView.findViewById(R.id.image_poster_thumbnail);
        separaterTrailerView = rootView.findViewById(R.id.separater_trailer);
        trailerLabelView = ((TextView) rootView.findViewById(R.id.label_trailer));
        trailerLinearLayout = (LinearLayout) rootView.findViewById(R.id.linear_layout_trailer);
        separaterReviewView = rootView.findViewById(R.id.separater_review);
        reviewLabelView = ((TextView) rootView.findViewById(R.id.label_review));
        reviewLinearLayout = (LinearLayout) rootView.findViewById(R.id.linear_layout_review);
        favoriteCheckbox = (CheckBox) rootView.findViewById(R.id.checkbox_favorite);
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
        Uri uri;
        if (movie_id <= 0) return null;

        switch (id) {
            case MOVIE_LOADER:
                uri = MovieContract.MovieEntry.buildMovieUri(movie_id);
                return new CursorLoader(getActivity(), uri, MOVIE_COLUMNS, null, null, null);
            case TRAILER_LOADER:
                uri = MovieContract.MovieEntry.buildMovieWithTrailerUri(movie_id);
                return new CursorLoader(getActivity(), uri, TRAILER_COLUMNS, null, null,
                        MovieContract.TrailerEntry.COLUMN_NAME_TRAILER_NAME + " ASC");
            case REVIEW_LOADER:
                uri = MovieContract.MovieEntry.buildMovieWithReviewUri(movie_id);
                return new CursorLoader(getActivity(), uri, REVIEW_COLUMNS, null, null,
                        MovieContract.ReviewEntry.COLUMN_NAME_REVIEW_AUTHOR + " ASC");
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (!data.moveToFirst()) {
            return;
        }

        switch (loader.getId()) {
            case MOVIE_LOADER:
                final String POSTER_BASE_URL = "http://image.tmdb.org/t/p/";
                final String POSTER_WIDTH = "w185";

                originalTitleTextView.setText(data.getString(COL_MOVIE_ORIGINAL_TITLE));
                originalTitleTextView.setBackgroundColor(getResources().getColor(R.color.movie_title_background));

                favoriteCheckbox.setVisibility(View.VISIBLE);
                favoriteCheckbox.setChecked(data.getInt(COL_MOVIE_FAVORITE) == 1);
                favoriteCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        ContentValues cv = new ContentValues();
                        if (buttonView.isChecked()) {
                            cv.put(MovieContract.MovieEntry.COLUMN_NAME_FAVORITE, "1");
                        } else {
                            cv.put(MovieContract.MovieEntry.COLUMN_NAME_FAVORITE, "0");
                        }
                        getContext().getContentResolver().update(MovieContract.MovieEntry.CONTENT_URI,
                                cv, MOVIE_SELECT_STRING, new String[]{Long.toString(movie_id)});
                    }
                });

                plotTextView.setText(data.getString(COL_MOVIE_PLOT));
                ratingTextView.setText(Utility.getRating(data.getDouble(COL_MOVIE_RATING)));
                dateTextView.setText(Utility.getYearFromDate(data.getString(COL_MOVIE_RELEASE_DATE)));

                String posterUrlStr = POSTER_BASE_URL + POSTER_WIDTH + data.getString(COL_MOVIE_POSTER_PATH);
                Picasso.with(getContext()).load(posterUrlStr).into(thumbnailView);
                break;

            case TRAILER_LOADER:
                separaterTrailerView.setBackgroundColor(getResources().getColor(R.color.movie_seperater_background));
                trailerLabelView.setText(getResources().getString(R.string.movie_trailer_label));
                trailerLinearLayout.removeAllViews();
                trailerUrl = YOUTUBE_BASE_URL + YOUTUBE_VIDEO_PARAM + "=" + data.getString(COL_TRAILER_KEY);

                while (true) {
                    View trailerItemView = LayoutInflater.from(getActivity()).inflate(R.layout.list_item_trailer, null);
                    ((TextView) trailerItemView.findViewById(R.id.textview_trailer))
                            .setText(data.getString(COL_TRAILER_NAME));

                    final String trailerKey = data.getString(COL_TRAILER_KEY);
                    trailerItemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Uri uri = Uri.parse(YOUTUBE_BASE_URL).buildUpon()
                                    .appendQueryParameter(YOUTUBE_VIDEO_PARAM, trailerKey)
                                    .build();
                            startActivity(new Intent(Intent.ACTION_VIEW, uri));
                        }
                    });
                    trailerLinearLayout.addView(trailerItemView);
                    if (!data.moveToNext()) {
                        return;
                    }
                }
            case REVIEW_LOADER:
                separaterReviewView.setBackgroundColor(getResources().getColor(R.color.movie_seperater_background));
                reviewLabelView.setText(getResources().getString(R.string.movie_review_label));
                reviewLinearLayout.removeAllViews();
                while (true) {
                    View reviewItemView = LayoutInflater.from(getActivity()).inflate(R.layout.list_item_review, null);
                    ((TextView) reviewItemView.findViewById(R.id.textview_review_author))
                            .setText(data.getString(COL_REIVEW_AUTHOR));
                    ((TextView) reviewItemView.findViewById(R.id.textview_review))
                            .setText(data.getString(COL_REIVEW_CONTENT));
                    reviewLinearLayout.addView(reviewItemView);
                    if (!data.moveToNext()) {
                        return;
                    }
                }
            default:
                return;
        }
        return;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}

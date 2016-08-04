package com.johnnyjinjing.popmovies.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class MovieProvider extends ContentProvider {

    // Creates a UriMatcher object
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static final String authority = MovieContract.CONTENT_AUTHORITY;

    static final int MOVIES = 1;
    static final int MOVIE = 2;
    static final int TRAILER = 3;
    static final int REVIEW = 4;

    // Handle to the database helper object
    private MovieDbHelper mMovieDbHelper;

    // Holds the database object
    private SQLiteDatabase db;

    // All of the content URI patterns that the provider recognize
    static {
        sUriMatcher.addURI(authority, MovieContract.PATH_MOVIE, MOVIES);
        sUriMatcher.addURI(authority, MovieContract.PATH_MOVIE + "/#", MOVIE);
        sUriMatcher.addURI(authority, MovieContract.PATH_MOVIE + "/#/trailer", TRAILER);
        sUriMatcher.addURI(authority, MovieContract.PATH_MOVIE + "/#/review", REVIEW);
    }



    @Override
    public boolean onCreate() {
        mMovieDbHelper = new MovieDbHelper(getContext());
        return true;
    }

    @Override
    // Implements ContentProvider.query()
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        switch (sUriMatcher.match(uri)) {
            case MOVIE:
//                db = mMovieDbHelper.getReadableDatabase();
                return mMovieDbHelper.getReadableDatabase().query(
                        MovieContract.MovieEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
                // If the URI is not recognized, you should do some error handling here.
        }
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case MOVIE:
                return MovieContract.MovieEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mMovieDbHelper.getWritableDatabase();
        switch (sUriMatcher.match(uri)) {
            case MOVIE:
                long _id = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    return MovieContract.MovieEntry.buildMovieUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    public UriMatcher getUriMatcher() {
        return sUriMatcher;
    }
}

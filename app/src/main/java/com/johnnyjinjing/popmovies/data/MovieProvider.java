package com.johnnyjinjing.popmovies.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import java.util.List;

public class MovieProvider extends ContentProvider {

    // Creates a UriMatcher object
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static final String authority = MovieContract.CONTENT_AUTHORITY;

    static final int MOVIES = 1;
    static final int TRAILERS = 2;
    static final int REVIEWS = 3;
    static final int MOVIE = 101;
    static final int TRAILER = 102;
    static final int REVIEW = 103;


    // Handle to the database helper object
    private MovieDbHelper mMovieDbHelper;

    // Holds the database object
//    private SQLiteDatabase db;

    private static final String movieSelection = MovieContract.MovieEntry.TABLE_NAME + "." +
            MovieContract.MovieEntry.COLUMN_NAME_ID + " = ? ";


    // All of the content URI patterns that the provider recognize
    static {
        sUriMatcher.addURI(authority, MovieContract.PATH_MOVIE, MOVIES);
        sUriMatcher.addURI(authority, MovieContract.PATH_TRAILER, TRAILERS);
        sUriMatcher.addURI(authority, MovieContract.PATH_REVIEW, REVIEWS);
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
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case MOVIES:
                return MovieContract.MovieEntry.CONTENT_TYPE;
            case MOVIE:
                return MovieContract.MovieEntry.CONTENT_ITEM_TYPE;
            case TRAILERS:
                return MovieContract.TrailerEntry.CONTENT_TYPE;
            case TRAILER:
                return MovieContract.TrailerEntry.CONTENT_TYPE;
            case REVIEWS:
                return MovieContract.ReviewEntry.CONTENT_TYPE;
            case REVIEW:
                return MovieContract.ReviewEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        SQLiteQueryBuilder qb;
        String id;
        List<String> uriFragment;

        switch (sUriMatcher.match(uri)) {
            case MOVIES:
                retCursor = mMovieDbHelper.getReadableDatabase().query(
                        MovieContract.MovieEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;

            case MOVIE:
                id = Long.toString(ContentUris.parseId(uri));
                retCursor = mMovieDbHelper.getReadableDatabase().query(
                        MovieContract.MovieEntry.TABLE_NAME, projection, movieSelection,
                        new String[]{id}, null, null, sortOrder);
                break;

            case TRAILER:
                uriFragment = uri.getPathSegments();
                id = uriFragment.get(uriFragment.size() - 2);
                qb = new SQLiteQueryBuilder();
                qb.setTables(MovieContract.MovieEntry.TABLE_NAME +
                        " INNER JOIN " + MovieContract.TrailerEntry.TABLE_NAME + " ON " +
                        MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry.COLUMN_NAME_ID + " = "
                        + MovieContract.TrailerEntry.TABLE_NAME + "." + MovieContract.TrailerEntry.COLUMN_KEY_MOVIE);
                retCursor = qb.query(mMovieDbHelper.getReadableDatabase(), projection, movieSelection,
                        new String[]{id}, null, null, sortOrder);
                break;

            case REVIEW:
                uriFragment = uri.getPathSegments();
                id = uriFragment.get(uriFragment.size() - 2);
                qb = new SQLiteQueryBuilder();
                qb.setTables(MovieContract.MovieEntry.TABLE_NAME +
                        " INNER JOIN " + MovieContract.ReviewEntry.TABLE_NAME + " ON " +
                        MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry.COLUMN_NAME_ID + "="
                        + MovieContract.ReviewEntry.TABLE_NAME + "." + MovieContract.ReviewEntry.COLUMN_KEY_MOVIE);
                retCursor = qb.query(mMovieDbHelper.getReadableDatabase(), projection, movieSelection,
                        new String[]{id}, null, null, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mMovieDbHelper.getWritableDatabase();
        Uri retUri;
        long rowId;

        switch (sUriMatcher.match(uri)) {
            case MOVIES:
                rowId = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, values);
                if (rowId > 0)
                    retUri = MovieContract.MovieEntry.buildMovieUri(rowId);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            case TRAILERS:
                rowId = db.insert(MovieContract.TrailerEntry.TABLE_NAME, null, values);
                if (rowId > 0)
//                    retUri = MovieContract.MovieEntry.buildTrailerUri(rowId);
                    retUri = MovieContract.TrailerEntry.buildTrailerUri(rowId);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            case REVIEWS:
                rowId = db.insert(MovieContract.ReviewEntry.TABLE_NAME, null, values);
                if (rowId > 0)
//                    retUri = MovieContract.MovieEntry.buildMovieWithReviewUri(rowId);
                    retUri = MovieContract.ReviewEntry.buildReviewUri(rowId);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Notify registered observers that a row was updated
        getContext().getContentResolver().notifyChange(uri, null);
        return retUri;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mMovieDbHelper.getWritableDatabase();
        int rowsDeleted;

        // To remove all rows and get a count pass "1" as the whereClause
        if (selection == null) selection = "1";

        switch (sUriMatcher.match(uri)) {
            case MOVIES:
                rowsDeleted = db.delete(
                        MovieContract.MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case TRAILER:
                rowsDeleted = db.delete(
                        MovieContract.TrailerEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case REVIEW:
                rowsDeleted = db.delete(
                        MovieContract.ReviewEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return 0;
    }

    public UriMatcher getUriMatcher() {
        return sUriMatcher;
    }
}

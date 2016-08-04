package com.johnnyjinjing.popmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * MovieDbHelper
 */
public class MovieDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Movie.db";

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {

        // Movie table contains info of movies
        final String SQL_CREATE_MOVIE_TABLE = "CREATE TABLE " + MovieContract.MovieEntry.TABLE_NAME + " (" +
                MovieContract.MovieEntry._ID + " INTEGER PRIMARY KEY, " +
                MovieContract.MovieEntry.COLUMN_NAME_ID + " INTEGER UNIQUE NOT NULL, " +
                MovieContract.MovieEntry.COLUMN_NAME_ORIGINAL_TITLE + " TEXT NOT NULL, " +
                MovieContract.MovieEntry.COLUMN_NAME_POSTER_PATH + " TEXT, " +
                MovieContract.MovieEntry.COLUMN_NAME_PLOT + " TEXT, " +
                MovieContract.MovieEntry.COLUMN_NAME_POPULARITY + " REAL, " +
                MovieContract.MovieEntry.COLUMN_NAME_RATING + " REAL, " +
                MovieContract.MovieEntry.COLUMN_NAME_RELEASE_DATE + " TEXT, " +

                // Use 1 for favorite, default is 0
                MovieContract.MovieEntry.COLUMN_NAME_FAVORITE + " INTEGER DEFAULT 0 NOT NULL" +
                " );";

        // Trailer table contains movie trailer addresses
        // Since one movie may have multiple trailers, we use a separate table to avoid repeating
        // movie entries
        final String SQL_CREATE_TRAILER_TABLE = "CREATE TABLE " + MovieContract.TrailerEntry.TABLE_NAME + " (" +
                MovieContract.TrailerEntry._ID + " INTEGER PRIMARY KEY, " +
                MovieContract.TrailerEntry.COLUMN_KEY_MOVIE + " INTEGER NOT NULL," +
                MovieContract.TrailerEntry.COLUMN_NAME_TRAILER_PATH + " TEXT UNIQUE NOT NULL, " +

                // Set up the movie column as a foreign key to movie table.
                " FOREIGN KEY (" + MovieContract.TrailerEntry.COLUMN_KEY_MOVIE  + ") REFERENCES " +
                MovieContract.MovieEntry.TABLE_NAME + " (" + MovieContract.MovieEntry._ID + ") " +
                " );";

        // Review table contains movie Review addresses
        final String SQL_CREATE_Review_TABLE = "CREATE TABLE " + MovieContract.ReviewEntry.TABLE_NAME + " (" +
                MovieContract.ReviewEntry._ID + " INTEGER PRIMARY KEY, " +
                MovieContract.ReviewEntry.COLUMN_KEY_MOVIE + " INTEGER NOT NULL, " +
                MovieContract.ReviewEntry.COLUMN_NAME_REVIEW_PATH + " TEXT UNIQUE NOT NULL, " +

                // Set up the movie column as a foreign key to movie table.
                " FOREIGN KEY (" + MovieContract.ReviewEntry.COLUMN_KEY_MOVIE  + ") REFERENCES " +
                MovieContract.MovieEntry.TABLE_NAME + " (" + MovieContract.MovieEntry._ID + ") " +
                " );";

        db.execSQL(SQL_CREATE_MOVIE_TABLE);
        db.execSQL(SQL_CREATE_TRAILER_TABLE);
        db.execSQL(SQL_CREATE_Review_TABLE);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // TODO: alter instead of drop tables to keep user's favorite movies
        db.execSQL("DROP TABLE IF EXISTS " + MovieContract.MovieEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MovieContract.TrailerEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MovieContract.ReviewEntry.TABLE_NAME);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}

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
        final String SQL_CREATE_MOVIE_TABLE = "CREATE TABLE " + MovieContract.MovieEntry.TABLE_NAME + " (" +
                MovieContract.MovieEntry._ID + " INTEGER PRIMARY KEY," +
                MovieContract.MovieEntry.COLUMN_NAME_ORIGINAL_TITLE + " TEXT NOT NULL, " +
                MovieContract.MovieEntry.COLUMN_NAME_POSTER + " TEXT, " +
                MovieContract.MovieEntry.COLUMN_NAME_PLOT + " TEXT, " +
                MovieContract.MovieEntry.COLUMN_NAME_RATING + " REAL, " +
                MovieContract.MovieEntry.COLUMN_NAME_RELEASE_DATE + " TEXT, " +

                // Use 1 for favorite, default is 0
                MovieContract.MovieEntry.COLUMN_NAME_FAVORITE + " INTEGER DEFAULT 0 NOT NULL" +
                " );";

        final String SQL_CREATE_TRAILER_TABLE = "CREATE TABLE " + MovieContract.TrailerEntry.TABLE_NAME + " (" +
                MovieContract.TrailerEntry._ID + " INTEGER PRIMARY KEY," +
                MovieContract.TrailerEntry.COLUMN_MOVIE_KEY + " INTEGER NOT NULL," +
                MovieContract.TrailerEntry.COLUMN_NAME_TRAILER + " TEXT NOT NULL, " +

                // Set up the movie column as a foreign key to movie table.
                " FOREIGN KEY (" + MovieContract.TrailerEntry.COLUMN_MOVIE_KEY  + ") REFERENCES " +
                MovieContract.MovieEntry.TABLE_NAME + " (" + MovieContract.MovieEntry._ID + "), " +
                " );";

        final String SQL_CREATE_COMMENT_TABLE = "CREATE TABLE " + MovieContract.CommentEntry.TABLE_NAME + " (" +
                MovieContract.CommentEntry._ID + " INTEGER PRIMARY KEY," +
                MovieContract.CommentEntry.COLUMN_MOVIE_KEY + " INTEGER NOT NULL," +
                MovieContract.CommentEntry.COLUMN_NAME_COMMENT + " TEXT NOT NULL, " +

                // Set up the movie column as a foreign key to movie table.
                " FOREIGN KEY (" + MovieContract.CommentEntry.COLUMN_MOVIE_KEY  + ") REFERENCES " +
                MovieContract.MovieEntry.TABLE_NAME + " (" + MovieContract.MovieEntry._ID + "), " +
                " );";

        db.execSQL(SQL_CREATE_MOVIE_TABLE);
        db.execSQL(SQL_CREATE_TRAILER_TABLE);
        db.execSQL(SQL_CREATE_COMMENT_TABLE);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL("DROP TABLE IF EXISTS " + MovieContract.MovieEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MovieContract.TrailerEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MovieContract.CommentEntry.TABLE_NAME);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}

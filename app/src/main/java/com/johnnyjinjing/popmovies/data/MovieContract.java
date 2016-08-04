package com.johnnyjinjing.popmovies.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * MovieContract:
 * Container for constants that define names for URIs, tables, and columns.
 */
public class MovieContract {

    // Content provider URI: content://com.johnnyjinjing.popmovies.provider/movie/
    public static final String CONTENT_AUTHORITY = "com.johnnyjinjing.popmovies.provider";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_MOVIE = "movie";
    public static final String PATH_TRAILER = "trailer";
    public static final String PATH_REVIEW = "review";

    // To prevent someone from accidentally instantiating the contract class, give it an empty constructor.
    public MovieContract() {}

    /* Inner class that defines the movie table */
    public static abstract class MovieEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE).build();

        // Used in Provider's getType method
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;

        public static final String TABLE_NAME = "movie";

        public static final String COLUMN_NAME_ORIGINAL_TITLE = "orig_title";
        public static final String COLUMN_NAME_POSTER_PATH = "poster_path";
        public static final String COLUMN_NAME_PLOT = "plot";
        public static final String COLUMN_NAME_POPULARITY = "popularity";
        public static final String COLUMN_NAME_RATING = "rating";
        public static final String COLUMN_NAME_RELEASE_DATE = "release_date";
        public static final String COLUMN_NAME_FAVORITE = "favorite";
        public static final String COLUMN_NAME_ID= "id";

        public static Uri buildMovieUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildTrailerUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id).buildUpon()
                    .appendPath(PATH_TRAILER).build();
        }

        public static Uri buildReviewUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id).buildUpon()
                    .appendPath(PATH_REVIEW).build();
        }
    }

    /* Inner class that defines the trailer table */
    public static abstract class TrailerEntry implements BaseColumns {
        public static final String TABLE_NAME = "trailer";

        // Column with the foreign key
        public static final String COLUMN_KEY_MOVIE = "movie_id";

        public static final String COLUMN_NAME_TRAILER_PATH = "trailer_path";
    }

    /* Inner class that defines the comment table */
    public static abstract class ReviewEntry implements BaseColumns {
        public static final String TABLE_NAME = "review";

        // Column with the foreign key
        public static final String COLUMN_KEY_MOVIE = "movie_id";

        public static final String COLUMN_NAME_REVIEW_PATH = "comment_path";
    }

}

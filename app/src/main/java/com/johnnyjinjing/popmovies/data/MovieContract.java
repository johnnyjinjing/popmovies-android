package com.johnnyjinjing.popmovies.data;

import android.provider.BaseColumns;

/** MovieContract
 * Container for constants that define names for URIs, tables, and columns.
 */
public class MovieContract {

    // To prevent someone from accidentally instantiating the contract class, give it an empty constructor.
    public MovieContract() {}

    /* Inner class that defines the movie table */
    public static abstract class MovieEntry implements BaseColumns {
        public static final String TABLE_NAME = "movie";

        public static final String COLUMN_NAME_ORIGINAL_TITLE = "orig_title";
        public static final String COLUMN_NAME_POSTER = "poster";
        public static final String COLUMN_NAME_PLOT = "plot";
        public static final String COLUMN_NAME_RATING = "rating";
        public static final String COLUMN_NAME_RELEASE_DATE = "release_date";
    }

    /* Inner class that defines the trailer table */
    public static abstract class TrailerEntry implements BaseColumns {
        public static final String TABLE_NAME = "trailer";

        // Column with the foreign key
        public static final String COLUMN_MOVIE_KEY = "movie_id";

        public static final String COLUMN_NAME_TRAILER = "trailer";
    }

    /* Inner class that defines the comment table */
    public static abstract class CommentEntry implements BaseColumns {
        public static final String TABLE_NAME = "comment";

        // Column with the foreign key
        public static final String COLUMN_MOVIE_KEY = "movie_id";

        public static final String COLUMN_NAME_COMMENT = "comment";
    }

}

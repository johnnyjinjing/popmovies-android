package com.johnnyjinjing.popmovies.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.test.AndroidTestCase;

import java.util.Map;
import java.util.Set;

/**
 * TestUtility:
 * Help functions for test
 */

/* Create test data for Movie table */
public class TestUtility extends AndroidTestCase{

    /* valid query data */
    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString() +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }

    static ContentValues createMovieTestValues() {
        ContentValues cv = new ContentValues();
        cv.put(MovieContract.MovieEntry.COLUMN_NAME_ID, 550);
        cv.put(MovieContract.MovieEntry.COLUMN_NAME_ORIGINAL_TITLE, "Fight Club");
        cv.put(MovieContract.MovieEntry.COLUMN_NAME_POSTER_PATH, "/811DjJTon9gD6hZ8nCjSitaIXFQ.jpg");
        cv.put(MovieContract.MovieEntry.COLUMN_NAME_PLOT, "A ticking-time-bomb insomniac and ...");
        cv.put(MovieContract.MovieEntry.COLUMN_NAME_RATING, 8.1);
        cv.put(MovieContract.MovieEntry.COLUMN_NAME_POPULARITY, 4.92427);
        cv.put(MovieContract.MovieEntry.COLUMN_NAME_RELEASE_DATE, "1999-10-14");
        return cv;
    }

    static ContentValues createTrailerTestValues(Long rowId) {
        ContentValues cv = new ContentValues();
        cv.put(MovieContract.TrailerEntry.COLUMN_KEY_MOVIE, rowId);
        cv.put(MovieContract.TrailerEntry.COLUMN_NAME_TRAILER_PATH, "2LqzF5WauAw");
        return cv;
    }

    static ContentValues createReviewTestValues(Long rowId) {
        ContentValues cv = new ContentValues();
        cv.put(MovieContract.ReviewEntry.COLUMN_KEY_MOVIE, rowId);
        cv.put(MovieContract.ReviewEntry.COLUMN_NAME_REVIEW_PATH, "some words here");
        return cv;
    }
}

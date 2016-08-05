package com.johnnyjinjing.popmovies.data;

import android.content.ContentValues;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.test.AndroidTestCase;

import com.johnnyjinjing.popmovies.utils.PollingCheck;

import java.util.Map;
import java.util.Set;

/**
 * TestUtility:
 * Help functions for test
 */

/* Create test data for Movie table */
public class TestUtility extends AndroidTestCase{

    /* Validate cursor */
    static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
        valueCursor.close();
    }

    /* Validate query data */
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

    static ContentValues createTrailerTestValues(long rowId) {
        ContentValues cv = new ContentValues();
        cv.put(MovieContract.TrailerEntry.COLUMN_KEY_MOVIE, rowId);
        cv.put(MovieContract.TrailerEntry.COLUMN_NAME_TRAILER_PATH, "2LqzF5WauAw");
        return cv;
    }

    static ContentValues createReviewTestValues(long rowId) {
        ContentValues cv = new ContentValues();
        cv.put(MovieContract.ReviewEntry.COLUMN_KEY_MOVIE, rowId);
        cv.put(MovieContract.ReviewEntry.COLUMN_NAME_REVIEW_PATH, "some words here");
        return cv;
    }

    /*
        Test the ContentObserver callbacks using the PollingCheck class grabbed from the Android
        CTS tests (only tests that the onChange function is called; it does not test that the
        correct Uri is returned)
     */
    static class TestContentObserver extends ContentObserver {
        final HandlerThread mHT;
        boolean mContentChanged;

        static TestContentObserver getTestContentObserver() {
            HandlerThread ht = new HandlerThread("ContentObserverThread");
            ht.start();
            return new TestContentObserver(ht);
        }

        private TestContentObserver(HandlerThread ht) {
            super(new Handler(ht.getLooper()));
            mHT = ht;
        }

        // On earlier versions of Android, this onChange method is called
        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            mContentChanged = true;
        }

        public void waitForNotificationOrFail() {
            // The PollingCheck class is taken from the Android CTS (Compatibility Test Suite).
            // The reason that PollingCheck works is that, by default, the JUnit
            // testing framework is not running on the main Android application thread.
            new PollingCheck(5000) {
                @Override
                protected boolean check() {
                    return mContentChanged;
                }
            }.run();
            mHT.quit();
        }
    }

    static TestContentObserver getTestContentObserver() {
        return TestContentObserver.getTestContentObserver();
    }
}

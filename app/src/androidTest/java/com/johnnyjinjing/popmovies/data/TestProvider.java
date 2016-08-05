package com.johnnyjinjing.popmovies.data;

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;
import android.test.AndroidTestCase;

public class TestProvider extends AndroidTestCase {
    public static final String LOG_TAG = TestProvider.class.getSimpleName();

    public void setUp() throws Exception {
        super.setUp();
        // Delete the database before each test
        deleteDatabase();
    }

    private void deleteDatabase() {
        mContext.deleteDatabase(MovieDbHelper.DATABASE_NAME);
    }

    /* Test if content provider is registered correctly */
    public void testProviderRegistry() {
        PackageManager pm = mContext.getPackageManager();

        // Define the component name based on the package name from the context and the provider class
        ComponentName componentName = new ComponentName(mContext.getPackageName(),
                MovieProvider.class.getName());
        try {
            // Fetch the provider info using the component name from the PackageManager
            // This throws an exception if the provider isn't registered
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);

            // Make sure that the registered authority matches the authority from the Contract.
            assertEquals("Error: Provider registered with authority: " + providerInfo.authority +
                            " instead of authority: " + MovieContract.CONTENT_AUTHORITY,
                    providerInfo.authority, MovieContract.CONTENT_AUTHORITY);
        } catch (PackageManager.NameNotFoundException e) {
            // Provider isn't registered correctly
            assertTrue("Error: Provider not registered at " + mContext.getPackageName(), false);
        }
    }

    /* Test if content provider returns the correct type for each type of URI */
    public void testGetType() {
        // content://com.johnnyjinjing.popmovies/movie
        String type = mContext.getContentResolver().getType(MovieContract.MovieEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.johnnyjinjing.popmovies/movie
        assertEquals("Error: wrong type", MovieContract.MovieEntry.CONTENT_TYPE, type);

        long testId = 1L;
        // content://com.johnnyjinjing.popmovies/movie/1
        type = mContext.getContentResolver().getType(
                MovieContract.MovieEntry.buildMovieUri(testId));
        // vnd.android.cursor.item/com.johnnyjinjing.popmovies/movie
        assertEquals("Error: wrong type", MovieContract.MovieEntry.CONTENT_ITEM_TYPE, type);

        // content://com.johnnyjinjing.popmovies/movie/1/trailer
        type = mContext.getContentResolver().getType(
                MovieContract.MovieEntry.buildMovieWithTrailerUri(testId));
        // vnd.android.cursor.dir/com.johnnyjinjing.popmovies/trailer
        assertEquals("Error: wrong type", MovieContract.TrailerEntry.CONTENT_TYPE, type);

        // content://com.johnnyjinjing.popmovies/movie/1/review
        type = mContext.getContentResolver().getType(MovieContract.MovieEntry.buildMovieWithReviewUri(testId));
        // vnd.android.cursor.dir/com.johnnyjinjing.popmovies/review
        assertEquals("Error: wrong type", MovieContract.ReviewEntry.CONTENT_TYPE, type);
    }

    /* Test query */
/*    public void testQuery() {
        // insert our test data into database
        MovieDbHelper dbHelper = new MovieDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues testValues = TestUtility.createMovieTestValues();
        long rowId = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, testValues);
        assertTrue("Unable to insert entry into database", rowId != -1);

        ContentValues trailerValues = TestUtility.createTrailerTestValues(rowId);
        ContentValues reviewValues = TestUtility.createReviewTestValues(rowId);
        long trailerRowId = db.insert(MovieContract.TrailerEntry.TABLE_NAME, null, trailerValues);
        long reviewRowId = db.insert(MovieContract.ReviewEntry.TABLE_NAME, null, reviewValues);

        assertTrue("Unable to insert entry into database", trailerRowId != -1);
        assertTrue("Unable to insert entry into database", reviewRowId != -1);

        db.close();

        // Test the basic content provider query
        Cursor c = mContext.getContentResolver().query(MovieContract.MovieEntry.CONTENT_URI,
                null, null, null, null);

        // Test if returns the correct cursor out of database
        TestUtility.validateCursor("Error", c, testValues);

        c = mContext.getContentResolver().query(MovieContract.MovieEntry.buildTrailerUri(rowId),
                null, null, null, null);
        TestUtility.validateCursor("Error", c, trailerValues);

        c = mContext.getContentResolver().query(MovieContract.MovieEntry.buildReviewUri(rowId),
                null, null, null, null);
        TestUtility.validateCursor("Error", c, reviewValues);
    }*/

    public void testInsert() {
        ContentValues testValues = TestUtility.createMovieTestValues();

        // Register a content observer for insertion
        TestUtility.TestContentObserver tco = TestUtility.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(MovieContract.MovieEntry.CONTENT_URI, true, tco);
        Uri uri = mContext.getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI, testValues);

        // Test if content observer get called
        tco.waitForNotificationOrFail();

        mContext.getContentResolver().unregisterContentObserver(tco);

        long rowId = ContentUris.parseId(uri);

        // Verify we got a row back.
        assertTrue(rowId != -1);

        Cursor cursor = mContext.getContentResolver().query(MovieContract.MovieEntry.CONTENT_URI,
                null, null, null, null);

        TestUtility.validateCursor("Error validating entry.", cursor, testValues);

        // Test adding trailer
        ContentValues trailerValues = TestUtility.createTrailerTestValues(rowId);

        tco = TestUtility.getTestContentObserver();

        mContext.getContentResolver().registerContentObserver(MovieContract.TrailerEntry.buildTrailerUri(rowId), true, tco);

        Uri trailerInsertUri = mContext.getContentResolver().insert(MovieContract.TrailerEntry.CONTENT_URI, trailerValues);

        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        // Add the movie values in with the trailer data
        trailerValues.putAll(testValues);

        Cursor trailerCursor = mContext.getContentResolver().query(MovieContract.MovieEntry.buildMovieWithTrailerUri(rowId),
                null, null, null, null);
        TestUtility.validateCursor("Error validating data", trailerCursor, trailerValues);

        // Test adding review
        ContentValues reviewValues = TestUtility.createReviewTestValues(rowId);

        tco = TestUtility.getTestContentObserver();

        mContext.getContentResolver().registerContentObserver(MovieContract.ReviewEntry.buildReviewUri(rowId), true, tco);

        Uri reviewInsertUri = mContext.getContentResolver().insert(MovieContract.ReviewEntry.CONTENT_URI, reviewValues);

        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        // Add the movie values in with the review data
        reviewValues.putAll(testValues);

        // Get the joined Weather and Location data
        Cursor reviewCursor = mContext.getContentResolver().query(MovieContract.MovieEntry.buildMovieWithReviewUri(rowId),
                null, null, null, null);
        TestUtility.validateCursor("Error validating data", reviewCursor, reviewValues);
    }
/*
    public void testDelete(){
        testZnsertProvider();

        // Register a content observer for movie delete
        TestUtility.TestContentObserver movieObs = TestUtility.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(MovieContract.MovieEntry.CONTENT_URI, true, movieObs);

        // Register a content observer for trailer delete
        TestUtility.TestContentObserver trailerObs = TestUtility.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(WeatherEntry.CONTENT_URI, true, weatherObserver);

        deleteAllRecordsFromProvider();

        // Students: If either of these fail, you most-likely are not calling the
        // getContext().getContentResolver().notifyChange(uri, null); in the ContentProvider
        // delete.  (only if the insertReadProvider is succeeding)
        locationObserver.waitForNotificationOrFail();
        weatherObserver.waitForNotificationOrFail();

        mContext.getContentResolver().unregisterContentObserver(locationObserver);
        mContext.getContentResolver().unregisterContentObserver(weatherObserver);
    }
*/
}

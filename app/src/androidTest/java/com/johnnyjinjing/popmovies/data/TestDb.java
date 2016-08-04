package com.johnnyjinjing.popmovies.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.HashSet;

/**
 * Testdb:
 * Use to test database implementation
 */
public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    public void setUp() {
        // Delete the database before each test
        deleteDatabase();
    }

    /* Test creation of database */
    public void testCreateDb() throws Throwable {
        // HashSet of table names we want to test
        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(MovieContract.MovieEntry.TABLE_NAME);
        tableNameHashSet.add(MovieContract.TrailerEntry.TABLE_NAME);
        tableNameHashSet.add(MovieContract.ReviewEntry.TABLE_NAME);

        // Create a database with table
        SQLiteDatabase db = new MovieDbHelper(this.mContext).getWritableDatabase();

        // Check if tables are correctly created
        assertEquals(true, db.isOpen());
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        assertTrue("Error: database has not been created correctly",
                c.moveToFirst());
        do {
            tableNameHashSet.remove(c.getString(0));
        } while (c.moveToNext());
        assertTrue("Error: Your database was created without designed tables",
                tableNameHashSet.isEmpty());

        // Check if tables contain correct columns
        c = db.rawQuery("PRAGMA table_info(" + MovieContract.MovieEntry.TABLE_NAME + ")", null);
        assertTrue("Error: unable to query the database for table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        HashSet<String> columnHashSet = new HashSet<String>();
        columnHashSet.add(MovieContract.MovieEntry._ID);
        columnHashSet.add(MovieContract.MovieEntry.COLUMN_NAME_ORIGINAL_TITLE);
        columnHashSet.add(MovieContract.MovieEntry.COLUMN_NAME_POSTER_PATH);
        columnHashSet.add(MovieContract.MovieEntry.COLUMN_NAME_PLOT);
        columnHashSet.add(MovieContract.MovieEntry.COLUMN_NAME_RATING);
        columnHashSet.add(MovieContract.MovieEntry.COLUMN_NAME_POPULARITY);
        columnHashSet.add(MovieContract.MovieEntry.COLUMN_NAME_RELEASE_DATE);
        columnHashSet.add(MovieContract.MovieEntry.COLUMN_NAME_FAVORITE);

        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            columnHashSet.remove(columnName);
        } while (c.moveToNext());

        assertTrue("Error: The database doesn't contain all of the required columns",
                columnHashSet.isEmpty());

        // Test Trailer table
        c = db.rawQuery("PRAGMA table_info(" + MovieContract.TrailerEntry.TABLE_NAME + ")", null);
        assertTrue("Error: unable to query the database for table information.",
                c.moveToFirst());
        columnHashSet = new HashSet<String>();
        columnHashSet.add(MovieContract.TrailerEntry._ID);
        columnHashSet.add(MovieContract.TrailerEntry.COLUMN_KEY_MOVIE);
        columnHashSet.add(MovieContract.TrailerEntry.COLUMN_NAME_TRAILER_PATH);
        columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            columnHashSet.remove(columnName);
        } while (c.moveToNext());
        assertTrue("Error: The database doesn't contain all of the required columns",
                columnHashSet.isEmpty());

        // Test Comment table
        c = db.rawQuery("PRAGMA table_info(" + MovieContract.ReviewEntry.TABLE_NAME + ")", null);
        assertTrue("Error: unable to query the database for table information.",
                c.moveToFirst());
        columnHashSet = new HashSet<String>();
        columnHashSet.add(MovieContract.TrailerEntry._ID);
        columnHashSet.add(MovieContract.ReviewEntry.COLUMN_KEY_MOVIE);
        columnHashSet.add(MovieContract.ReviewEntry.COLUMN_NAME_REVIEW_PATH);
        columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            columnHashSet.remove(columnName);
        } while (c.moveToNext());
        assertTrue("Error: The database doesn't contain all of the required columns",
                columnHashSet.isEmpty());
        db.close();
    }

    /* Test Movie table insertion and query, tests has been done in the insertion test */
    public void testMovieTable() {
        insertMovieTable();
    }

    /* Test insertion and query of the Movie table*/
    public long insertMovieTable() {
        MovieDbHelper dbHelper = new MovieDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Insert test data
        ContentValues testValues = TestUtility.createMovieTestValues();
        long rowId;
        rowId = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, testValues);
        assertTrue(rowId != -1);

        // Query the database
        Cursor cursor = db.query(
                MovieContract.MovieEntry.TABLE_NAME,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );
        assertTrue("Error: No Records returned from query", cursor.moveToFirst());

        // Validate data
        TestUtility.validateCurrentRecord("Error: Query Validation Failed",
                cursor, testValues);

        // Only one row should be returned
        assertFalse("Error: More than one record returned from query",
                cursor.moveToNext());

        cursor.close();
        db.close();
        return rowId;
    }

    private void deleteDatabase() {
        mContext.deleteDatabase(MovieDbHelper.DATABASE_NAME);

    }

    /* Test Trailer table */
    public void testTrailerTable() {
        // Insert an entry to Movie table first, and get a foreign key
        long rowId = insertMovieTable();
        assertFalse("Error: Inserted incorrectly", rowId == -1L);

        MovieDbHelper dbHelper = new MovieDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues testValues = TestUtility.createTrailerTestValues(rowId);

        long tRowId = db.insert(MovieContract.TrailerEntry.TABLE_NAME, null, testValues);
        assertTrue(tRowId != -1);

        Cursor c = db.query(MovieContract.TrailerEntry.TABLE_NAME, null, null, null, null, null, null);
        assertTrue("Error: No Records returned", c.moveToFirst());

        TestUtility.validateCurrentRecord("Error: Query Validation Failed",
                c, testValues);

        assertFalse("Error: More than one record returned from query",
                c.moveToNext());

        c.close();
        db.close();
    }

    /* Test Comment table */
    public void testReviewTable() {
        // Insert an entry to Movie table first, and get a foreign key
        long rowId = insertMovieTable();
        assertFalse("Error: Inserted incorrectly", rowId == -1L);

        MovieDbHelper dbHelper = new MovieDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues testValues = TestUtility.createReviewTestValues(rowId);

        long tRowId = db.insert(MovieContract.ReviewEntry.TABLE_NAME, null, testValues);
        assertTrue(tRowId != -1);

        Cursor c = db.query(MovieContract.ReviewEntry.TABLE_NAME, null, null, null, null, null, null);
        assertTrue("Error: No Records returned", c.moveToFirst());

        TestUtility.validateCurrentRecord("Error: Query Validation Failed",
                c, testValues);

        assertFalse("Error: More than one record returned from query",
                c.moveToNext());

        c.close();
        db.close();
    }

}



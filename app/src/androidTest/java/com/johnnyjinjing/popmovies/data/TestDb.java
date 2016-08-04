package com.johnnyjinjing.popmovies.data;

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

    // Test creation of database
    public void testCreateDb() throws Throwable {
        // HashSet of table names we want to test
        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(MovieContract.MovieEntry.TABLE_NAME);
        tableNameHashSet.add(MovieContract.TrailerEntry.TABLE_NAME);
        tableNameHashSet.add(MovieContract.CommentEntry.TABLE_NAME);

        deleteDatabase();

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
        columnHashSet.add(MovieContract.MovieEntry.COLUMN_NAME_POSTER);
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
        columnHashSet.add(MovieContract.TrailerEntry.COLUMN_NAME_TRAILER);
        columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            columnHashSet.remove(columnName);
        } while (c.moveToNext());
        assertTrue("Error: The database doesn't contain all of the required columns",
                columnHashSet.isEmpty());

        // Test Comment table
        c = db.rawQuery("PRAGMA table_info(" + MovieContract.CommentEntry.TABLE_NAME + ")", null);
        assertTrue("Error: unable to query the database for table information.",
                c.moveToFirst());
        columnHashSet = new HashSet<String>();
        columnHashSet.add(MovieContract.TrailerEntry._ID);
        columnHashSet.add(MovieContract.CommentEntry.COLUMN_KEY_MOVIE);
        columnHashSet.add(MovieContract.CommentEntry.COLUMN_NAME_COMMENT);
        columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            columnHashSet.remove(columnName);
        } while (c.moveToNext());
        assertTrue("Error: The database doesn't contain all of the required columns",
                columnHashSet.isEmpty());
        db.close();
    }

    private void deleteDatabase() {
        mContext.deleteDatabase(MovieDbHelper.DATABASE_NAME);
    }

}

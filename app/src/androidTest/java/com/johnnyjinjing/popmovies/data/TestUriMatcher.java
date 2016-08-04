package com.johnnyjinjing.popmovies.data;

import android.content.UriMatcher;
import android.net.Uri;
import android.test.AndroidTestCase;

public class TestUriMatcher extends AndroidTestCase {

    private static final long TEST_MOVIE_ID = 1L;

    // content://com.johnnyjinjing.popmovies/movie
    private static final Uri TEST_MOVIE_DIR = MovieContract.MovieEntry.CONTENT_URI;
    // content://com.johnnyjinjing.popmovies/movie/1
    private static final Uri TEST_MOVIE_ITEM = MovieContract.MovieEntry.buildMovieUri(TEST_MOVIE_ID);
    // content://com.johnnyjinjing.popmovies/movie/1/trailer
    private static final Uri TEST_TRAILER_DIR = MovieContract.MovieEntry.buildTrailerUri(TEST_MOVIE_ID);
    // content://com.johnnyjinjing.popmovies/movie/1/review
    private static final Uri TEST_REVIEW_DIR = MovieContract.MovieEntry.buildReviewUri(TEST_MOVIE_ID);

    //  Test UriMatcher returns the correct integer value
    public void testUriMatcher() {
        UriMatcher testMatcher = new MovieProvider().getUriMatcher();

        assertEquals("Error: TEST_MOVIE_DIR matched incorrectly.",
                testMatcher.match(TEST_MOVIE_DIR), MovieProvider.MOVIES);
        assertEquals("Error: TEST_MOVIE_ITEM matched incorrectly.",
                testMatcher.match(TEST_MOVIE_ITEM), MovieProvider.MOVIE);
        assertEquals("Error: TEST_TRAILER_DIR matched incorrectly.",
                testMatcher.match(TEST_TRAILER_DIR), MovieProvider.TRAILER);
        assertEquals("Error: TEST_REVIEW_DIR matched incorrectly.",
                testMatcher.match(TEST_REVIEW_DIR), MovieProvider.REVIEW);
    }

}

package com.johnnyjinjing.popmovies;

/**
 * Some helper functions
 */
public class Utility {
    private static final int FULL_RATING = 10;

    public static String getYearFromDate(String date) {
        return date.substring(0,4);
    }

    public static String getRating (double rating) {
        return rating + " / " + FULL_RATING;
    }
}

package com.johnnyjinjing.popmovies;

public class Movie {
    private String posterUrl;
    private String originTitle;
    private String plotSynopsis;
    private double UserRating;
    private String releaseDate  = null;

    public Movie (String url, String title, String plot, double rate, String date){
        this.posterUrl = url;
        this.originTitle = title;
        this.plotSynopsis = plot;
        this.UserRating = rate;
        this.releaseDate = date;
    }
}

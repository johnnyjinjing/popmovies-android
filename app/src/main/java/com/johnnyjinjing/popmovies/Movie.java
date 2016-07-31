package com.johnnyjinjing.popmovies;

public class Movie {
    public String posterUrl;
    public String originTitle;
    public String plotSynopsis;
    public double UserRating;
    public String releaseDate  = null;

    public Movie (String url, String title, String plot, double rate, String date){
        this.posterUrl = url;
        this.originTitle = title;
        this.plotSynopsis = plot;
        this.UserRating = rate;
        this.releaseDate = date;
    }
}

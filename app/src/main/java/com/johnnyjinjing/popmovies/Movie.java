package com.johnnyjinjing.popmovies;

import java.util.Date;

public class Movie {
    private String posterUrl;
    private String originTitle;
    private String plotSynopsis;
    private float UserRating;
    private Date releaseDate  = null;

    public Movie (String url, String title, String plot, float rate){
        this.posterUrl = url;
        this.originTitle = title;
        this.plotSynopsis = plot;
        this.UserRating = rate;
    }
}

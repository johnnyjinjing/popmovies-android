package com.johnnyjinjing.popmovies;

import android.os.Parcel;
import android.os.Parcelable;

/* Movie Class
    Implements Parcelable to allow passing from one activity to another
 */
public class Movie implements Parcelable{
    String posterUrl;
    String originTitle;
    String plotSynopsis;
    double UserRating;
    String releaseDate  = null;

    public Movie(String url, String title, String plot, double rate, String date){
        this.posterUrl = url;
        this.originTitle = title;
        this.plotSynopsis = plot;
        this.UserRating = rate;
        this.releaseDate = date;
    }

    public Movie(Parcel in) {
        this.posterUrl = in.readString();
        this.originTitle = in.readString();
        this.plotSynopsis = in.readString();
        this.UserRating = in.readDouble();
        this.releaseDate = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(this.posterUrl);
        parcel.writeString(this.originTitle);
        parcel.writeString(this.plotSynopsis);
        parcel.writeDouble(this.UserRating);
        parcel.writeString(this.releaseDate);
    }

    public final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>(){
        @Override
        public Movie createFromParcel(Parcel parcel) {
            return new Movie(parcel);
        }

        @Override
        public Movie[] newArray(int i) {
            return new Movie[i];
        }
    };
}

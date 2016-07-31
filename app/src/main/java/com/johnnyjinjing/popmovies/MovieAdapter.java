package com.johnnyjinjing.popmovies;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class MovieAdapter extends ArrayAdapter<Movie> {
    public MovieAdapter(Context context, List<Movie> moviesList) {
        super(context, 0, moviesList);
    }

    /**
     * Provides a view for an AdapterView (ListView, GridView, etc.)
     *
     * @param position    The AdapterView position that is requesting a view
     * @param convertView The recycled view to populate.
     * @param parent      The parent ViewGroup that is used for inflation.
     * @return The View for the position in the AdapterView.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final String POSTER_BASE_URL = "http://image.tmdb.org/t/p/";
        final String POSTER_WIDTH = "w185";

        // Gets the Movie object from the ArrayAdapter at the appropriate position
        Movie movie = getItem(position);

        // Adapters recycle views to AdapterViews.
        // If this is a new View object we're getting, then inflate the layout.
        // If not, this view already has the layout inflated from a previous call to getView,
        // and we modify the View widgets as usual.
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.grid_item_poster, parent, false);
        }

        ImageView posterView = (ImageView) convertView.findViewById(R.id.grid_item_poster_imageview);

        String posterUrlStr = POSTER_BASE_URL + POSTER_WIDTH + movie.posterUrl;

        Picasso.with(getContext()).load(posterUrlStr).into(posterView);
//        posterView.setImageResource(androidFlavor.image);

//        TextView versionNameView = (TextView) convertView.findViewById(R.id.list_item_version_name);
//        versionNameView.setText(androidFlavor.versionName);
//
//        TextView versionNumberView = (TextView) convertView.findViewById(R.id.list_item_versionnumber_textview);
//        versionNumberView.setText(androidFlavor.versionNumber);
        return convertView;
    }
}
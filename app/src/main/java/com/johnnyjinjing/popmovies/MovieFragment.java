package com.johnnyjinjing.popmovies;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class MovieFragment extends Fragment {

    private final String LOG_TAG = MovieFragment.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
//        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_movie, container, false);

        // Get intent and put movie details into view
        Intent intent = getActivity().getIntent();

        if (intent != null && intent.hasExtra("movie_detail")) {

            final String POSTER_BASE_URL = "http://image.tmdb.org/t/p/";
            final String POSTER_WIDTH = "w185";

            Movie movie = (Movie) intent.getParcelableExtra("movie_detail");

            ((TextView) rootView.findViewById(R.id.text_original_title)).setText(movie.originTitle);
            ((TextView) rootView.findViewById(R.id.text_plot)).setText(movie.plotSynopsis);
            ((TextView) rootView.findViewById(R.id.text_rating)).setText(Double.toString(movie.UserRating));
            ((TextView) rootView.findViewById(R.id.text_release_date)).setText(movie.releaseDate);

            ImageView thumbnailView = (ImageView) rootView.findViewById(R.id.image_poster_thumbnail);
            String posterUrlStr = POSTER_BASE_URL + POSTER_WIDTH + movie.posterUrl;
            Picasso.with(getContext()).load(posterUrlStr).into(thumbnailView);

        }


        return rootView;
    }

}

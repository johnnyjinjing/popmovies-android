package com.johnnyjinjing.popmovies;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;

import com.johnnyjinjing.popmovies.data.MovieContract;
import com.squareup.picasso.Picasso;

public class MovieCursorAdapter extends CursorAdapter{

    public MovieCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.grid_item_poster, parent, false);
        return view;
    }

    @Override
    /* Fill-in the views with the contents of the cursor. */
    public void bindView(View view, Context context, Cursor cursor) {

        final String POSTER_BASE_URL = "http://image.tmdb.org/t/p/";
        final String POSTER_WIDTH = "w185";

        // Fill-in the views with the contents of the cursor
        ImageView posterView = (ImageView) view.findViewById(R.id.grid_item_poster_imageview);
        int idx_poster = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_NAME_POSTER_PATH);
        String posterUrlStr = POSTER_BASE_URL + POSTER_WIDTH + cursor.getString(idx_poster);
        Picasso.with(context).load(posterUrlStr).into(posterView);
    }
}

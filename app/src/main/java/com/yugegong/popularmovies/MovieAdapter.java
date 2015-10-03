package com.yugegong.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by ygong on 9/16/15.
 */
public class MovieAdapter extends CursorAdapter {
    private static final String LOG_TAG = MovieAdapter.class.getSimpleName();

    public  MovieAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
    }

    public static class ViewHolder {
        @Bind(R.id.grid_item_movie_imageview) ImageView bPoster;
        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.grid_item_movie, parent, false);
        int numColumns = ((GridView)parent).getNumColumns();
        int columnWidth = ((GridView)parent).getColumnWidth();

//        Log.v("newView", cursor.getString(FavoriteFragment.COL_MOVIE_TITLE) + " " +
//                FavoriteFragment.sScreenWidth + " " + numColumns + " " + columnWidth);

        ViewHolder holder = new ViewHolder(view);
        holder.bPoster.getLayoutParams().width = FavoriteFragment.sScreenWidth / numColumns;
        holder.bPoster.getLayoutParams().height = (int) ((float)278/185 * holder.bPoster.getLayoutParams().width);
        view.setTag(holder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
//        Log.v("bindView", cursor.getString(FavoriteFragment.COL_MOVIE_TITLE));
        ViewHolder holder = (ViewHolder) view.getTag();
        byte[] bytes = cursor.getBlob(FavoriteFragment.COL_MOVIE_POSTER_IMG);
        Bitmap posterImg = Utility.getBitmapFromByteArray(bytes);
        if (posterImg != null) {
            holder.bPoster.setImageBitmap(posterImg);
        } else {
            holder.bPoster.setImageResource(R.drawable.poster_placeholder_error);
        }

    }
}

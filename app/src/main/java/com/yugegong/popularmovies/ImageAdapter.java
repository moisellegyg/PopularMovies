package com.yugegong.popularmovies;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.yugegong.popularmovies.model.Movie;

import java.util.List;

/**
 * Created by ygong on 7/31/15.
 */
public class ImageAdapter extends ArrayAdapter<Movie> {

    private static final String LOG_TAG = ImageAdapter.class.getSimpleName();

    private int mResource;
    private int mFieldId = 0;
    LayoutInflater mInflater;


    public ImageAdapter(Context context, int resource, int imageViewResourceId, List<Movie> movies) {
        super(context, resource, imageViewResourceId, movies);
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mResource = resource;
        mFieldId = imageViewResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        ImageView imageView;

        if (convertView == null) {
            view = mInflater.inflate(mResource, parent, false);
        } else {
            view = convertView;
        }

        try {
            if (mFieldId == 0) {
                imageView = (ImageView) view;
            } else {
                imageView = (ImageView) view.findViewById(mFieldId);
            }

        } catch (ClassCastException e) {
            Log.e(LOG_TAG, "You must supply a resource ID for a ImageView");
            throw new IllegalStateException(LOG_TAG + " requires the resource ID to be a ImageView", e);
        }

//        Log.v(LOG_TAG, "width: " + RegularFragment.sScreenWidth);
        int numColumns = ((GridView)parent).getNumColumns();
        imageView.getLayoutParams().width = RegularFragment.sScreenWidth / numColumns;
        imageView.getLayoutParams().height = (int) ((float)278/185 * imageView.getLayoutParams().width);

        String url = getItem(position).getPosterPath();
        Picasso.with(getContext())
                .load(url)
                .placeholder(R.drawable.poster_placeholder)
                .error(R.drawable.poster_placeholder_error)
                .into(imageView);

        //Bitmap image = getItem(position);
        //imageView.setImageBitmap(image);

        return view;
    }
}

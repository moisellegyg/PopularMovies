package com.yugegong.popularmovies;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

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


        imageView.getLayoutParams().width = parent.getWidth()/2;
        imageView.getLayoutParams().height = parent.getHeight()/2;
        String url = getItem(position).getPosterPath();
        //if (url != null && !url.isEmpty())
        Picasso.with(getContext()).load(url).into(imageView);

        //Bitmap image = getItem(position);
        //imageView.setImageBitmap(image);

        return view;
    }
}

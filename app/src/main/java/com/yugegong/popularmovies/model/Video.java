package com.yugegong.popularmovies.model;

/**
 * Created by ygong on 9/21/15.
 */
public class Video {

    private String mName;
    private String mKey;

    public Video(String name, String key){
        mName = name;
        mKey = key;
    }

    public String getName() {
        return mName;
    }

    public String getKey() {
        return mKey;
    }
}

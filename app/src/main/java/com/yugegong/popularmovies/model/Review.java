package com.yugegong.popularmovies.model;

/**
 * Created by ygong on 9/23/15.
 */
public class Review {
    private String mId;
    private String mAuthor;
    private String mContent;

    public Review(String id, String author, String content) {
        mId = id;
        mAuthor = author;
        mContent = content;
    }

    public String getId() {
        return mId;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public String getContent() {
        return mContent;
    }
}

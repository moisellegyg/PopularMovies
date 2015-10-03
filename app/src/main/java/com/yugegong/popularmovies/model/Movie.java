package com.yugegong.popularmovies.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by ygong on 7/31/15.
 * Implement Parcelable to transport the instance via intent
 */

// No need the implementation for Parcelable since i will not pass Movie object via intent.
// Pass the position of the Movie instance inside the Movie list via intent is enough.
public class Movie implements Parcelable {
// TODO: remove the Parcelable implementation after using local database to store Movie data.

    private String mId;
    private String mTitle;
    private String mOverview;
    private String mReleaseDate;
    private String mRate;
    private String mPosterPath;
    private String mRuntime;
    private byte[] mPosterImg;

    private ArrayList<Video> mVideoList;
    private ArrayList<Review> mReviewList;

    //private String video  //get video: http://api.themoviedb.org/3/movie/211672/videos?api_key=...
    // https://www.youtube.com/watch?v=$KEY

    public Movie(String id, String title) {
        mId = id;
        mTitle = title;
    }

    public void setOverview(String overview) {
        mOverview = overview;
    }

    public void setReleaseDate(String releaseDate) {
        mReleaseDate = releaseDate;
    }

    public void setPosterPath(String posterPath) {
        mPosterPath = posterPath;
    }

    public void setRate(String rate) {
        mRate = rate;
    }

    public void setRuntime(String runtime) { mRuntime = runtime; }

    public void setPosterImg(byte[] img) {
        mPosterImg = img;
    }

    public void setVideoList(ArrayList<Video> videoList) {
        mVideoList = videoList;
    }

    public void setReviewList(ArrayList<Review> reviewList) {
        mReviewList = reviewList;
    }

    public String getId() { return mId; }

    public String getTitle() { return mTitle; }

    public String getOverview() { return mOverview; }

    public String getReleaseDate() { return mReleaseDate; }

    public String getRate() { return mRate; }

    public String getPosterPath() { return mPosterPath; }

    public String getRuntime() { return mRuntime; }

    public byte[] getPosterImg() { return mPosterImg; }

    public ArrayList<Video> getVideoList() { return mVideoList; }

    public ArrayList<Review> getReviewList() {
        return mReviewList;
    }

// Comment this part because no implementation from Parcelable

    //Code below are for implementing Parcelable
    private Movie(Parcel in) {
        mId = in.readString();
        mTitle = in.readString();
        mOverview = in.readString();
        mReleaseDate = in.readString();
        mRate = in.readString();
        mPosterPath = in.readString();
        mRuntime = in.readString();
        mPosterImg = new byte[in.readInt()];
        in.readByteArray(mPosterImg);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mId);
        dest.writeString(mTitle);
        dest.writeString(mOverview);
        dest.writeString(mReleaseDate);
        dest.writeString(mRate);
        dest.writeString(mPosterPath);
        dest.writeString(mRuntime);
        dest.writeInt(mPosterImg.length);
        dest.writeByteArray(mPosterImg);
    }

    public static final Parcelable.Creator<Movie> CREATOR
            = new Parcelable.Creator<Movie>() {

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }

        @Override
        public Movie createFromParcel(Parcel source) {
            Movie movie = new Movie(source);
            return movie;
        }
    };
}

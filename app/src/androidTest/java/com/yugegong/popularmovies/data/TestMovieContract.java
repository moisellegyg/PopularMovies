package com.yugegong.popularmovies.data;

import android.net.Uri;
import android.test.AndroidTestCase;

/**
 * Created by ygong on 9/3/15.
 */
public class TestMovieContract extends AndroidTestCase{

    private static final long TEST_MOVIE_ID = 76341;

    public void testbuildVideoMovieUri() {
        Uri videoUri = MovieContract.VideoEntry.buildVideoMovieUri(TEST_MOVIE_ID);
        assertNotNull("Error: Null Uri returned.", videoUri);
        assertEquals("Error: Movie for video not properly appended to the end of the Uri",
                TEST_MOVIE_ID, videoUri.getLastPathSegment());
        assertEquals("Error: Video movie Uri doesn't match the expected result",
                videoUri.toString(),
                "content://com.yugegong.popularmovies/video/76341");
    }
}

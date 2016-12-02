package com.chandra.myflickr.events;

import com.chandra.myflickr.models.PhotoComment;

import java.util.ArrayList;

public class CommentsDownloadedEvent {

    private ArrayList<PhotoComment> mCommentsArray;

    public ArrayList<PhotoComment> getCommentsArray() {
        return mCommentsArray;
    }

    public CommentsDownloadedEvent(ArrayList<PhotoComment> commentsArray) {

        mCommentsArray = commentsArray;
    }
}

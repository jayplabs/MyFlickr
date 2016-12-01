package com.chandra.myflickr.models;

import com.googlecode.flickrjandroid.photos.comments.Comment;

import java.io.Serializable;

public class PhotoComment implements Serializable {

    private String mAuthorName;
    private String mCommentData;

    public String getAuthorName() {
        return mAuthorName;
    }

    public String getCommentData() {
        return mCommentData;
    }

    public PhotoComment(Comment comment) {
        mAuthorName = comment.getAuthorName();
        mCommentData = comment.getText();
    }

    public PhotoComment(String authorName, String commentData) {
        mAuthorName = authorName;
        mCommentData = commentData;
    }


}

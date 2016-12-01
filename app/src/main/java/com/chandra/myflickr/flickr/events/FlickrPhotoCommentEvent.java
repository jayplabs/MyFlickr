package com.chandra.myflickr.flickr.events;

public class FlickrPhotoCommentEvent {

    private String commentId = null;
    private int position;
    private String authorName;
    private String commentData;
    public FlickrPhotoCommentEvent(String id) {
        this(-1, null, null, null);
    }
    public FlickrPhotoCommentEvent(int position, String id, String authorName, String commentData) {
        this.position = position;
        commentId = id;
        this.authorName = authorName;
        this.commentData = commentData;
    }

    public int getPosition() {
        return position;
    }

    public String getCommentId() {
        return commentId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getCommentData() {
        return commentData;
    }
}

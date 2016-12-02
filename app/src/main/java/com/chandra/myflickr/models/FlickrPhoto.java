package com.chandra.myflickr.models;

import com.googlecode.flickrjandroid.photos.Photo;

import java.io.Serializable;

public class FlickrPhoto implements Serializable {
    //https://farm{farm-id}.staticflickr.com/{server-id}/{id}_{secret}.jpg

    private static final String IMAGE_URL = "https://farm";
    private static final String STATIC_FLICKR = ".staticflickr.com/";

    private String uid;
    private String name;
    private String url;
    private int commentSum;

    public FlickrPhoto(Photo photo) {
        this.uid = photo.getId();
        this.name = photo.getTitle();

        this.commentSum = photo.getComments();

        StringBuilder sbUrl = new StringBuilder(IMAGE_URL);
        sbUrl.append(photo.getFarm());
        sbUrl.append(STATIC_FLICKR);
        sbUrl.append(photo.getServer());
        sbUrl.append("/");
        sbUrl.append(uid);
        sbUrl.append("_");
        sbUrl.append(photo.getSecret());
        sbUrl.append(".");
        sbUrl.append(photo.getOriginalFormat());

        this.url = sbUrl.toString();
    }

    public String getPhotoId() { return uid; }

    public void setCommentSum(int commentCountSum) {
        commentSum = commentCountSum;
    }

    public int getCommentSum() {
        return commentSum;
    }

    public String getUrl() {
        return url;
    }

    public String getName() {
        return name;
    }
}
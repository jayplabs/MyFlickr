package com.chandra.myflickr.events;


import com.chandra.myflickr.models.FlickrPhoto;

import java.util.ArrayList;

public class FlickrPhotoEvent {
    private ArrayList<FlickrPhoto> mDataArray;

    public FlickrPhotoEvent(ArrayList<FlickrPhoto> data) {
        this.mDataArray = data;
    }

    public ArrayList<FlickrPhoto> getDataArray() {
        return mDataArray;
    }
}

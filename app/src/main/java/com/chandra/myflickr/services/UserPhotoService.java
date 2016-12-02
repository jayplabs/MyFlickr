package com.chandra.myflickr.services;

import android.app.IntentService;
import android.content.Intent;

import com.chandra.myflickr.managers.FlickrManager;
import com.chandra.myflickr.models.FlickrPhoto;
import com.chandra.myflickr.events.CommentsDownloadedEvent;
import com.chandra.myflickr.events.FlickrPhotoCommentEvent;
import com.chandra.myflickr.events.FlickrPhotoEvent;
import com.chandra.myflickr.models.PhotoComment;
import com.googlecode.flickrjandroid.photos.Photo;
import com.googlecode.flickrjandroid.photos.PhotoList;
import com.googlecode.flickrjandroid.photos.comments.Comment;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;


public class UserPhotoService extends IntentService {

    public static final String ACTION_GET_PHOTOS = "com.chandra.myflickr.services.action.GET_PHOTOS";
    public static final String ACTION_ADD_COMMENT = "com.chandra.myflickr.services.action.ADD_COMMENT";
    public static final String ACTION_GET_COMMENTS = "com.chandra.myflickr.services.action.GET_COMMENTS";

    public static final String PHOTOS_BY_TAG = "com.chandra.myflickr.services.extra.PHOTOS_BY_TAG";
    public static final String PHOTO_POSITION = "com.chandra.myflickr.services.extra.PHOTO_POSITION";
    public static final String PHOTO_ID = "com.chandra.myflickr.services.extra.PHOTO_ID";
    public static final String PHOTO_COMMENT_AUTHOR = "com.chandra.myflickr.services.extra.PHOTO_COMMENT_AUTHOR";
    public static final String PHOTO_COMMENT = "com.chandra.myflickr.services.extra.PHOTO_COMMENT";

    private static final String NAME = UserPhotoService.class.getSimpleName();


    FlickrManager mFlickrManager = null;

    public UserPhotoService() {
        super(NAME);
    }



    @Override
    protected void onHandleIntent(Intent intent) {
        mFlickrManager = FlickrManager.getInstance();
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_GET_PHOTOS.equals(action)) {
                PhotoList photoList;
                String[] tags = null;

                if (intent.hasExtra(PHOTOS_BY_TAG)) {
                    tags = intent.getStringArrayExtra(PHOTOS_BY_TAG);
                }

                if (tags == null || tags.length == 0) {
                    photoList = mFlickrManager.getRecentPhotos();
                } else {
                    photoList = mFlickrManager.getPhotosByTag(tags);
                }
                if (photoList == null) {
                    EventBus.getDefault().post(new FlickrPhotoEvent(null));
                    return;
                }

                ArrayList<FlickrPhoto> mDataArray = convertDataToMyDataModel(photoList);
                EventBus.getDefault().post(new FlickrPhotoEvent(mDataArray));
            } else if (ACTION_ADD_COMMENT.equals(action)) {
                int position = Integer.valueOf(intent.getStringExtra(PHOTO_POSITION));
                String photoId = intent.getStringExtra(PHOTO_ID);
                String commentAuthor = intent.getStringExtra(PHOTO_COMMENT_AUTHOR);
                String comment = intent.getStringExtra(PHOTO_COMMENT);
                String commentId = null;
                if (photoId != null && comment != null) {
                    commentId = mFlickrManager.addComment(photoId, comment);
                }

                if (commentId == null) {
                    EventBus.getDefault().post(new FlickrPhotoCommentEvent(null));
                    return;
                }
                EventBus.getDefault().post(new FlickrPhotoCommentEvent(position, commentId, commentAuthor, comment));
            } else if (ACTION_GET_COMMENTS.equals(action)) {
                String photoId = intent.getStringExtra(PHOTO_ID);
                List<Comment> commentList = null;
                if (photoId != null)
                    commentList = mFlickrManager.getComments(photoId, null, null);

                if (commentList == null) {
                    EventBus.getDefault().post(new CommentsDownloadedEvent(null));
                    return;
                }

                ArrayList<PhotoComment> commentsArray = convertCommentToPhotoCommentModel(commentList);
                EventBus.getDefault().post(new CommentsDownloadedEvent(commentsArray));
            }
        }

    }

    protected ArrayList<PhotoComment> convertCommentToPhotoCommentModel(List<Comment> commentList) {
        if (commentList == null) {
            return null;
        }

        ArrayList<PhotoComment> newCommentsArray = new ArrayList<>();
        for (Comment comment : commentList) {
            newCommentsArray.add(new PhotoComment(comment));
        }

        return newCommentsArray;
    }

    protected ArrayList<FlickrPhoto> convertDataToMyDataModel(PhotoList photos) {
        if (photos == null) {
            return null;
        }

        ArrayList<FlickrPhoto> newDataArray = new ArrayList<>();
        for (Photo photo : photos) {
            FlickrPhoto mPhoto = new FlickrPhoto(photo);
            int commentSum = mFlickrManager.getCommentsCount(photo.getId());
            mPhoto.setCommentSum(commentSum);
            newDataArray.add(mPhoto);
        }

        return newDataArray;
    }
}

package com.chandra.myflickr.flickr;

import com.chandra.myflickr.Constants;
import com.chandra.myflickr.managers.CacheManager;
import com.chandra.myflickr.utils.StringUtils;
import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.FlickrException;
import com.googlecode.flickrjandroid.REST;
import com.googlecode.flickrjandroid.RequestContext;
import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.oauth.OAuthToken;
import com.googlecode.flickrjandroid.people.PeopleInterface;
import com.googlecode.flickrjandroid.photos.Extras;
import com.googlecode.flickrjandroid.photos.Photo;
import com.googlecode.flickrjandroid.photos.PhotoList;
import com.googlecode.flickrjandroid.photos.PhotosInterface;
import com.googlecode.flickrjandroid.photos.SearchParameters;
import com.googlecode.flickrjandroid.photos.comments.Comment;
import com.googlecode.flickrjandroid.photos.comments.CommentsInterface;

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import static com.googlecode.flickrjandroid.Flickr.SAFETYLEVEL_SAFE;

public class FlickrManager {

    private static final Logger logger = LoggerFactory.getLogger(FlickrManager.class.getSimpleName());
    private static FlickrManager instance = null;
    private static final String CLIENT_ID=<KEY>; //$NON-NLS-1$
    private static final String CLIENT_SECRET =<SECRET>; //$NON-NLS-1$

    private FlickrManager() {}

    public static FlickrManager getInstance() {
        if (instance == null) {
            instance = new FlickrManager();
        }
        return instance;
    }


    public Flickr getFlickr() {
        try {
            Flickr f = new Flickr(CLIENT_ID, CLIENT_SECRET, new REST());
            return f;
        } catch (ParserConfigurationException e) {
            return null;
        }
    }

    public Flickr getFlickrAuthed(String token, String secret) {
        Flickr f = getFlickr();
        RequestContext requestContext = RequestContext.getRequestContext();
        OAuth auth = new OAuth();
        auth.setToken(new OAuthToken(token, secret));
        requestContext.setOAuth(auth);
        return f;
    }

    //----------------------------------------------------------------------------------------------
    // Private function
    //----------------------------------------------------------------------------------------------

    private PhotoList getUserPhotos() {
        final PeopleInterface peopleInterface = getPeopleInterface();
        if (peopleInterface == null) {
            return null;
        }

        final String userId = CacheManager.getStringCacheData(Constants.KEY_USER_ID);
        if (StringUtils.isNull(userId)) {
            return null;
        }

        Set<String> extras = new HashSet();
        extras.add(Extras.URL_O);                //get the link of origin image.
        extras.add(Extras.ORIGINAL_FORMAT);      //get image with origin format.
        extras.add(Extras.TAGS);                 // get image tags
        extras.add(Extras.DESCRIPTION);          // get image description
        PhotoList photoList = null;
        try {

            photoList = peopleInterface.getPhotos(userId, extras, 0, 0);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (FlickrException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return photoList;
    }

    public PhotoList getPhotos(String[] tags) {
        if (tags == null || tags.length == 0)
            return getUserPhotos();
        else
            return getPhotosByTag(tags);
    }

    private PhotoList getPhotosByTag(String[] tags) {

        final PhotosInterface photosInterface = getPhotosInterface();
        if (photosInterface == null)
            return null;

        Set<String> extras = new HashSet<>();
        extras.add(Extras.URL_O);                //get the link of origin image.
        extras.add(Extras.ORIGINAL_FORMAT);      //get image with origin format.
        extras.add(Extras.TAGS);                 // get image tags
        extras.add(Extras.DESCRIPTION);          // get image description
        extras.add(Extras.OWNER_NAME);           // get owner name of image
        PhotoList photoList = null;
        SearchParameters params = new SearchParameters();
        params.setExtras(extras);
        params.setTags(tags);
        params.setSafeSearch(SAFETYLEVEL_SAFE);
        try {
            photoList = photosInterface.search(params, 0, 0);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (FlickrException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return photoList;
    }

    private OAuthToken getOAuth() {
        String token = CacheManager.getStringCacheData(Constants.KEY_OAUTH_TOKEN);
        if (StringUtils.isNull(token))
            return null;

        String secret = CacheManager.getStringCacheData(Constants.KEY_TOKEN_SECRET);
        if (StringUtils.isNull(secret))
            return null;

        return new OAuthToken(token, secret);
    }

    public PeopleInterface getPeopleInterface() {

        OAuthToken oauth = getOAuth();
        if (oauth == null) {
            return null;
        }

        Flickr f = getFlickrAuthed(oauth.getOauthToken(), oauth.getOauthTokenSecret());
        if (f != null) {
            return f.getPeopleInterface();
        } else {
            return null;
        }


    }

    public PhotosInterface getPhotosInterface() {
        OAuthToken oauth = getOAuth();
        if (oauth == null) {
            return null;
        }

        Flickr f = getFlickrAuthed(oauth.getOauthToken(), oauth.getOauthTokenSecret());
        if (f != null) {
            return f.getPhotosInterface();
        } else {
            return null;
        }
    }


    public List<Comment> getComments(String photoId, Date minCommentDate, Date maxCommentDate) {
        final CommentsInterface commentsInterface = getCommentsInterface();
        if (commentsInterface == null || photoId == null) {
            return null;
        }

        List<Comment> commentsList = null;
        try {
            commentsList = commentsInterface.getList(photoId, minCommentDate, maxCommentDate);
        } catch (FlickrException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return commentsList;
    }

    public int getCommentsCount(String photoId) {
        final CommentsInterface commentsInterface = getCommentsInterface();
        if (commentsInterface == null || photoId == null) {
            return 0;
        }
        int commentsCount = 0;
        try {
            commentsCount = commentsInterface.getList(photoId, null, null).size();
        } catch (FlickrException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return commentsCount;

    }

    public String addComment(String photoId, String comment) {
        final CommentsInterface commentsInterface = getCommentsInterface();
        if (commentsInterface == null || photoId == null || comment == null) {
            return null;
        }
        String commentId = null;

        try {
            commentId = commentsInterface.addComment(photoId, comment);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (FlickrException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return commentId;
    }

    public CommentsInterface getCommentsInterface() {
        OAuthToken oauth = getOAuth();
        if (oauth == null) {
            return null;
        }

        Flickr f = getFlickrAuthed(oauth.getOauthToken(), oauth.getOauthTokenSecret());
        if (f != null) {
            return f.getCommentsInterface();
        } else {
            return null;
        }
    }
}

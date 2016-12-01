package com.chandra.myflickr.flickr.cache;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.chandra.myflickr.utils.PhotoUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;


public class ImageDownloadTask extends AsyncTask<String, Integer, Bitmap> {

    private static final Logger logger = LoggerFactory.getLogger(ImageDownloadTask.class.getSimpleName());
    private WeakReference<ImageView> imgRef = null;
    private String mUrl;

    public ImageDownloadTask(ImageView imageView) {
        imgRef = new WeakReference<>(imageView);
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        mUrl = params[0];
        Bitmap image = PhotoCache.getFromCache(mUrl);
        if (image != null) {
            return image;
        }
        return PhotoUtils.downloadImage(mUrl);
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        if (this.isCancelled()) {
            result = null;
            return;
        }

        PhotoCache.saveToCache(this.mUrl, result);
        if (imgRef != null) {
            ImageView imageView = imgRef.get();
            ImageDownloadTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);
            // Change bitmap only if this process is still associated with it
            // Or if we don't use any bitmap to task association
            // (NO_DOWNLOADED_DRAWABLE mode)
            if (this == bitmapDownloaderTask && bitmapDownloaderTask != null ) {
                imageView.setImageBitmap(result);
            }
        }
    }


    private ImageDownloadTask getBitmapDownloaderTask(ImageView imageView) {
        if (imageView != null) {
            Drawable drawable = imageView.getDrawable();
            if (drawable instanceof PhotoUtils.DownloadedDrawable) {
                PhotoUtils.DownloadedDrawable downloadedDrawable = (PhotoUtils.DownloadedDrawable) drawable;
                return downloadedDrawable.getBitmapDownloaderTask();
            }
        }
        return null;
    }

}

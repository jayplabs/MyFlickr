package com.chandra.myflickr.flickr.cache;

import android.graphics.Bitmap;

import java.lang.ref.SoftReference;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class PhotoCache {

    // LRU Cache
    private static final Map<String, SoftReference<Bitmap>> cache = new LinkedHashMap<String, SoftReference<Bitmap>>() {
        private static final long serialVersionUID = 1L;

        /* (non-Javadoc)
         * @see java.util.LinkedHashMap#removeEldestEntry(java.util.Map.Entry)
         */
        @Override
        protected boolean removeEldestEntry(
                Entry<String, SoftReference<Bitmap>> eldest) {
            return size() > 50;
        }

    };

    private static final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private static final Lock read  = readWriteLock.readLock();
    private static final Lock write = readWriteLock.writeLock();

    /**
     *
     */
    private PhotoCache() {
        super();
    }

    public static Bitmap getFromCache(String id){
        read.lock();
        try {
            if(!cache.containsKey(id))
                return null;
            SoftReference<Bitmap> ref=cache.get(id);
            return ref.get();
        } finally {
            read.unlock();
        }
    }

    public static void saveToCache(String id, Bitmap bitmap){
        write.lock();
        try {
            cache.put(id, new SoftReference<>(bitmap));
        } finally {
            write.unlock();
        }
    }
}

package com.solide.imagelibs;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.webkit.URLUtil;

/**
 * A simple subclass of {@link ImageResizer} that fetches and resizes images
 * fetched from a URL.
 */
public class ImageFetcher extends ImageResizer {
    private static final String TAG = "ImageFetcher";
    private static final int HTTP_CACHE_SIZE = 10 * 1024 * 1024; // 10MB
    public static final String HTTP_CACHE_DIR = "http";

    /**
     * Initialize providing a target image width and height for the processing
     * images.
     * 
     * @param context
     * @param imageWidth
     * @param imageHeight
     */
    public ImageFetcher(Context context, int imageWidth, int imageHeight) {
        super(context, imageWidth, imageHeight);
        init(context);
    }

    /**
     * Initialize providing a single target image size (used for both width and
     * height);
     * 
     * @param context
     * @param imageSize
     */
    public ImageFetcher(Context context, int imageSize) {
        super(context, imageSize);
        init(context);
    }

    private void init(Context context) {
        checkConnection(context);
    }

    /**
     * Simple network connection check.
     * 
     * @param context
     */
    private void checkConnection(Context context) {
        final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnectedOrConnecting()) {
            if (Utils.DEBUG) {
                //Toast.makeText(context, "No network connection found.", Toast.LENGTH_LONG).show();
                Log.e(TAG, "checkConnection - no connection found");
            }
        }
    }

    /**
     * The main process method, which will be called by the ImageWorker in the
     * AsyncTask background thread.
     * 
     * @param data
     *            The data to load the bitmap, in this case, a regular http URL
     * @return The downloaded and resized bitmap
     */
    private Bitmap processBitmap(String data) {
        Bitmap bitmap = null;
        if (Utils.DEBUG) {
            Log.d(TAG, "processBitmap - " + data);
        }
        File image_file = null;

        if (URLUtil.isNetworkUrl(data)) {
            if (Utils.getUsableSpace(new File("sdcard")) > 10 * 1024 * 1024) {
                // Download a bitmap, write it to a file
                for (int i = 0; image_file == null && i < 3; i++) {
                    image_file = downloadBitmap(mContext, data);
                }
            } else {
                return null;
            }
        } else {
            image_file = new File(data);
        }

        if (image_file != null) {

            try {
                // Return a sampled down version
                bitmap = decodeSampledBitmapFromFile(image_file.toString(), mImageWidth, mImageHeight);
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
            }

        }

        return bitmap;
    }

    @Override
    protected Bitmap processBitmap(Object data) {
        return processBitmap(String.valueOf(data));
    }

    /**
     * Download a bitmap from a URL, write it to a disk and return the File
     * pointer. This implementation uses a simple disk cache.
     * 
     * @param context
     *            The context to use
     * @param urlString
     *            The URL to fetch
     * @return A File pointing to the fetched bitmap
     */
    public File downloadBitmap(Context context, String urlString) {
        final File cacheDir = DiskLruCache.getDiskCacheDir(context, HTTP_CACHE_DIR);

        final DiskLruCache cache = DiskLruCache.openCache(context, cacheDir, HTTP_CACHE_SIZE);

        File cacheFile = null;

        String cacheName = urlString;

        if (mOnSubString != null) {
            if (Utils.DEBUG) {
                Log.i(TAG, "fileTag-->" + mOnSubString.onSub(urlString));
            }
            cacheName = mOnSubString.onSub(urlString);
            cacheFile = new File(cache.createFilePath(cacheName));
        } else {
            cacheFile = new File(cache.createFilePath(urlString));
        }

        if (cache.get(cacheName) != null) {
            if (Utils.DEBUG) {
                Log.d(TAG, "downloadBitmap - found in http cache - " + urlString);
            }
            return cacheFile;
        }

        if (Utils.DEBUG) {
            Log.d(TAG, "downloadBitmap - downloading - " + urlString);
        }

        Utils.disableConnectionReuseIfNecessary();
        try {
            if (cacheFile.exists()) {
                cacheFile.delete();
            }
            cacheFile.createNewFile();
            DownloadUtils.downloadImage(cacheFile, urlString);
        } catch (IOException e) {
            e.printStackTrace();
            if (Utils.DEBUG) {
                Log.i(TAG, "Download fail!!!");
            }
            return null;
        }
        return cacheFile;

    }

}

package com.solide.imagelibs;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.net.Uri;
import android.util.Log;

public class DownloadUtils {

    private static final String TAG = DownloadUtils.class.getName();

    /**
     * Default time for httpConnectiong connect timeout.
     */
    public static final int DEFAULT_HTTP_CONNECT_TIMEOUT = 5 * 1000; // milliseconds

    /**
     * Default time for httpConnectiong read timeout.
     */
    public static final int DEFAULT_HTTP_READ_TIMEOUT = 60 * 1000; // milliseconds

    /**
     * Buffer size for every option for the stream.
     */
    protected static final int BUFFER_SIZE = 8 * 1024; // 8 Kb

    /**
     * Particularly chars which were allowed in Uri.
     */
    protected static final String ALLOWED_URI_CHARS = "@#&=*+-_.,:!?()/~'%";

    /**
     * The max count to redirect.
     */
    protected static final int MAX_REDIRECT_COUNT = 5;

    /**
     * Retrieves {@link InputStream} of image by URI (image is located in the
     * network).
     * 
     * @param imageUri
     *            Image URI
     * @param extra
     *            Auxiliary object which was passed to
     *            {@link DisplayImageOptions.Builder#extraForDownloader(Object)
     *            DisplayImageOptions.extraForDownloader(Object)}; can be null
     * @return {@link InputStream} of image
     * @throws IOException
     *             if some I/O error occurs during network request or if no
     *             InputStream could be created for URL.
     */
    public static InputStream getStreamFromNetwork(String fileUri) throws IOException {
        HttpURLConnection conn = createConnection(fileUri);
        int redirectCount = 0;
        if (Utils.DEBUG) {
            Log.i(TAG, "ResponseCode-->" + conn.getResponseCode());
        }
        while (conn.getResponseCode() / 100 == 3 && redirectCount < MAX_REDIRECT_COUNT) {
            if (Utils.DEBUG) {
                Log.i(TAG, "ResponseCode-->" + conn.getResponseCode());
            }
            conn = createConnection(conn.getHeaderField("Location"));
            redirectCount++;
        }

        return new BufferedInputStream(conn.getInputStream(), BUFFER_SIZE);
    }

    /**
     * Create {@linkplain HttpURLConnection HTTP connection} for incoming URL
     * 
     * @param url
     *            URL to connect to
     * @return {@linkplain HttpURLConnection Connection} for incoming URL.
     *         Connection isn't established so it still configurable.
     * @throws IOException
     *             if some I/O error occurs during network request or if no
     *             InputStream could be created for URL.
     */
    public static HttpURLConnection createConnection(String url) throws IOException {
        String encodedUrl = Uri.encode(url, ALLOWED_URI_CHARS);
        HttpURLConnection conn = (HttpURLConnection) new URL(encodedUrl).openConnection();
        conn.setConnectTimeout(DEFAULT_HTTP_CONNECT_TIMEOUT);
        conn.setReadTimeout(DEFAULT_HTTP_READ_TIMEOUT);
        return conn;
    }

    /**
     * Download the image file according to the fileUri.
     * 
     * @param targetFile
     *            the cache file which you create to storage the download file.
     * @param fileUri
     *            the file's uri.
     */
    public static void downloadImage(File targetFile, String fileUri) throws IOException {
        InputStream is = getStreamFromNetwork(fileUri);
        try {
            OutputStream os = new BufferedOutputStream(new FileOutputStream(targetFile), BUFFER_SIZE);
            try {
                copyStream(is, os);
            } finally {
                closeSilently(os);
            }
        } finally {
            closeSilently(is);
        }
    }

    /**
     * Copy the stream to another stream.
     */
    public static void copyStream(InputStream is, OutputStream os) throws IOException {
        byte[] bytes = new byte[BUFFER_SIZE];
        while (true) {
            int count = is.read(bytes, 0, BUFFER_SIZE);
            if (count == -1) {
                break;
            }
            os.write(bytes, 0, count);
        }
    }

    /**
     * Close the stream which you have created.
     */
    public static void closeSilently(Closeable closeable) {
        try {
            closeable.close();
        } catch (Exception e) {
            if (Utils.DEBUG) {
                Log.i(TAG, "Close error!!!");
            }
            e.printStackTrace();
        }
    }

}

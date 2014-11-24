package com.solide.imagelibs;

/**
 * Interface definition for a callback to be invoked when a URL will change to
 * create a id for saving object.
 */
public interface OnSubString {
    /**
     * Called when a URL need to be operated.
     * 
     * @param url
     *            The URL that will be operated.
     */
    String onSub(String url);
}

package com.sendi.deliveredrobot.utils;

import android.text.TextUtils;
import android.webkit.MimeTypeMap;

public class MimeTypeMapUtils {
    public MimeTypeMapUtils() {
    }

    public static String getFileExtensionFromUrl(String url) {
        url = url.toLowerCase();
        if (!TextUtils.isEmpty(url)) {
            int fragment = url.lastIndexOf(35);
            if (fragment > 0) {
                url = url.substring(0, fragment);
            }

            int query = url.lastIndexOf(63);
            if (query > 0) {
                url = url.substring(0, query);
            }

            int filenamePos = url.lastIndexOf(47);
            String filename = 0 <= filenamePos ? url.substring(filenamePos + 1) : url;
            if (!filename.isEmpty()) {
                int dotPos = filename.lastIndexOf(46);
                if (0 <= dotPos) {
                    return filename.substring(dotPos + 1);
                }
            }
        }

        return "";
    }

    public static String getMimeTypeFromUrl(String url) {
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(getFileExtensionFromUrl(url));
    }

    public static String getMimeTypeFromExtension(String extension) {
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }
}

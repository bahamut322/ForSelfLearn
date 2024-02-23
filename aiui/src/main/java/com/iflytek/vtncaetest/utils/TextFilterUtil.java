package com.iflytek.vtncaetest.utils;

import static android.content.ContentValues.TAG;

import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextFilterUtil {
    static String REPLACE_NULL = "";
    static String REGEX_TTS_ASSIST_TAG = "\\[[a-z]\\d\\]";

    public static String removeTTSAssistTag(String text) {
        Log.d(TAG, "removeTTSAssistTag: " + text);
        Pattern regex = Pattern.compile(REGEX_TTS_ASSIST_TAG);
        Matcher input = regex.matcher(text);
        return input.replaceAll(REPLACE_NULL);
    }
}

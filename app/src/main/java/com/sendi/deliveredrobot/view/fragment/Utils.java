package com.sendi.deliveredrobot.view.fragment;

import android.content.Context;
import android.graphics.Rect;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;

public class Utils {
    public static final int inputWidth = 320;
    public static final int inputHeight = 256;
    public static final int NUM_THREADS = 4;

    public static final int mOutputRow = 5040; //9984, 4420, 1118, 640x480-18900, 320x256-5040
    public static final int mOutputColumn = 8; // left, top, right, bottom, score and 80 class probability
    public static final float mThreshold = 0.7f; // score above which a detection is generated
    public static final float mIoUThreshold = 0.30f; // used for IoU
    public static int mNmsLimit = 10;//识别最大人脸个数

    public static final float[] NO_MEAN_RGB = {0.0f, 0.0f, 0.0f};
    public static final float[] NO_STD_RGB = {1.0f, 1.0f, 1.0f};

    private static final String TAG = Utils.class.getName();


    public static void copyFileFromAsset(Context context, String assets_path, String new_path) {
        File father_path = new File(new File(new_path).getParent());
        if (!father_path.exists()) {
            father_path.mkdirs();
        }
        try {
            File new_file = new File(new_path);
            InputStream is_temp = context.getAssets().open(assets_path);
            if (new_file.exists() && new_file.isFile()) {
                if (contrastFileMD5(new_file, is_temp)) {
                    Log.d(TAG, new_path + " is exists!");
                    return;
                } else {
                    Log.d(TAG, "delete old model file!");
                    new_file.delete();
                }
            }
            InputStream is = context.getAssets().open(assets_path);
            FileOutputStream fos = new FileOutputStream(new_file);
            byte[] buffer = new byte[1024];
            int byteCount;
            while ((byteCount = is.read(buffer)) != -1) {
                fos.write(buffer, 0, byteCount);
            }
            fos.flush();
            is.close();
            fos.close();
            Log.d(TAG, "the model file is copied");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean contrastFileMD5(File new_file, InputStream assets_file) {
        MessageDigest new_file_digest, assets_file_digest;
        int len;
        try {
            byte[] buffer = new byte[1024];
            new_file_digest = MessageDigest.getInstance("MD5");
            FileInputStream in = new FileInputStream(new_file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                new_file_digest.update(buffer, 0, len);
            }

            assets_file_digest = MessageDigest.getInstance("MD5");
            while ((len = assets_file.read(buffer, 0, 1024)) != -1) {
                assets_file_digest.update(buffer, 0, len);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        String new_file_md5 = new BigInteger(1, new_file_digest.digest()).toString(16);
        String assets_file_md5 = new BigInteger(1, assets_file_digest.digest()).toString(16);
        Log.d("new_file_md5", new_file_md5);
        Log.d("assets_file_md5", assets_file_md5);
        return new_file_md5.equals(assets_file_md5);
    }
}


class Info {
    private int age;
    private String gender;
    private Rect rect;
    private int maskState;
    private float[] feature;
    private float[] confList;

    public Info(int age, String gender, Rect rect, int maskState, float[] feature, float[] confList) {
        this.age = age;
        this.gender = gender;
        this.rect = rect;
        this.maskState = maskState;
        this.feature = feature;
        this.confList = confList;
    }

    public Info(Rect rect, int maskState) {
        this.rect = rect;
        this.maskState = maskState;
    }

    public int getAge() {
        return age;
    }

    public String getGender() {
        return gender;
    }

    public Rect getRect() {
        return rect;
    }

    public int getMaskState() {
        return maskState;
    }

    public float[] getFeature() {
        return feature;
    }

    public float[] getConfList() {
        return confList;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setRect(Rect rect) {
        this.rect = rect;
    }

    public void setMaskState(int maskState) {
        this.maskState = maskState;
    }

    public void setFeature(float[] feature) {
        this.feature = feature;
    }

    public void setConfList(float[] confList) {
        this.confList = confList;
    }
}


class AgeGender{
    private int age;
    private String gender;

    public AgeGender(int age, String gender) {
        this.age = age;
        this.gender = gender;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public int getAge() {
        return age;
    }

    public String getGender() {
        return gender;
    }
}


class Result {
    private int maskState;
    private Float score;
    private Rect rect;

    public Result(int maskState, Float score, Rect rect) {
        this.maskState = maskState;
        this.score = score;
        this.rect = rect;
    }

    public int getMaskState() {
        return maskState;
    }

    public void setMaskState(int maskState) {
        this.maskState = maskState;
    }

    public Float getScore() {
        return score;
    }

    public void setScore(Float score) {
        this.score = score;
    }

    public Rect getRect() {
        return rect;
    }

    public void setRect(Rect rect) {
        this.rect = rect;
    }
}
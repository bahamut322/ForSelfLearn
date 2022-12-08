package com.sendi.deliveredrobot.entity;

import android.graphics.Bitmap;

/**
 * @author swn
 * 存储异常人脸信息的实体类
 */
public class Abnormal {
    private Bitmap bitmap;
    private int mask;
    private float temperature;

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public void mask(int mask) {
        this.mask = mask;
    }

    public void temperature(float temperature) {
        this.temperature = temperature;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public int getMask() {
        return mask;
    }

    public void setMask(int mask) {
        this.mask = mask;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }
}

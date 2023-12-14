package com.sendi.deliveredrobot.interfaces;

import android.graphics.Bitmap;

import com.sendi.deliveredrobot.model.FaceModel;

import java.util.List;

/**
 * @Author Swn
 * @Data 2023/12/8
 * @describe
 */
public class FaceDataListener {
    public interface OnChangeListener{
        void onChange();
    }
    private static FaceDataListener.OnChangeListener onChangeListener;    // 声明interface接口
    public static void setOnChangeListener(FaceDataListener.OnChangeListener onChange) {    // 创建setListener方法
        onChangeListener = onChange;
    }

    private static List<FaceModel> faceModels;//人脸位置相识度等信息

    private static Bitmap faceBit;//bitmap

    public static Bitmap getFaceBit() {
        return faceBit;
    }

    public static void setFaceBit(Bitmap faceBit) {
        FaceDataListener.faceBit = faceBit;
        if (onChangeListener!=null) {
            onChangeListener.onChange();
        }
    }

    public static void removeOnChangeListener() {
        onChangeListener = null;
    }

    public static List<FaceModel> getFaceModels() {
        return faceModels;
    }


    public static void setFaceModels(List<FaceModel> progress) {
        FaceDataListener.faceModels = progress;
        if (onChangeListener!=null) {
            onChangeListener.onChange();
        }
    }

}

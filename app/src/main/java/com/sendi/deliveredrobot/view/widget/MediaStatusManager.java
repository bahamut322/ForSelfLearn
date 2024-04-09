package com.sendi.deliveredrobot.view.widget;

public class MediaStatusManager {
    public interface OnChangeListener {    // 创建interface类
        void onChange(boolean stop);    // 值改变
    }

    private static OnChangeListener onChangeListener;    // 声明interface接口

    public static void setOnChangeListener(OnChangeListener onChange) {    // 创建setListener方法
        onChangeListener = onChange;
    }

    public static void stopMediaPlay(boolean stop) {//1是停视频声音 0是播放视频声音
        if (onChangeListener != null) {
            onChangeListener.onChange(stop);
        }
    }
}


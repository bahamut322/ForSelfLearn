package com.sendi.deliveredrobot.interfaces;


/**
 * @Author Swn
 * @Data 2023/12/1
 * @describe 下载任务数据接口类
 */
public class DownLoadListener {
    public interface OnChangeListener {    // 创建interface类
        void onChange();    // 值改变
    }

    private static DownLoadListener.OnChangeListener onChangeListener;    // 声明interface接口

    public static void setOnChangeListener(DownLoadListener.OnChangeListener onChange) {    // 创建setListener方法
        onChangeListener = onChange;
    }

    private static int progress;

    public static int getProgress() {
        return progress;
    }


    public static void setProgress(int progress) {
        DownLoadListener.progress = progress;
        if (onChangeListener!=null) {
            onChangeListener.onChange();
        }
    }

}

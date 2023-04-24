package com.sendi.deliveredrobot.view.widget;

/**
 * @Author Swn
 * @describe 观察状态
 * @Data 2023-04-24 11:44
 */
public class Stat {
    public interface OnChangeListener {    // 创建interface类
        void onChange();    // 值改变
    }

    private static OnChangeListener onChangeListener;    // 声明interface接口

    public static void setOnChangeListener(OnChangeListener onChange) {    // 创建setListener方法
        onChangeListener = onChange;
    }

    private static int flage;

    public static int getFlage() {
        return flage;
    }

    public static void setFlage(int flage) {
        flage = flage;
        if (onChangeListener!=null) {
            onChangeListener.onChange();
        }
    }
}

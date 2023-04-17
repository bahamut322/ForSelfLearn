package com.sendi.deliveredrobot.view.widget;

public class Order {
    public interface OnChangeListener {    // 创建interface类
        void onChange();    // 值改变
    }

    private static OnChangeListener onChangeListener;    // 声明interface接口

    public static void setOnChangeListener(OnChangeListener onChange) {    // 创建setListener方法
        onChangeListener = onChange;
    }

    private static String flage;

    public static String getFlage() {
        return flage;
    }

    public static void setFlage(String flage) {
        Order.flage = flage;
        if (onChangeListener!=null) {
            onChangeListener.onChange();
        }
    }
}


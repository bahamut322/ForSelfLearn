package com.sendi.deliveredrobot.view.widget;


public class NextTask {
    public interface OnChangeListener {    // 创建interface类
        void onChange();    // 值改变
    }

    private static OnChangeListener onChangeListener;    // 声明interface接口

    public static void setOnChangeListener(OnChangeListener onChange) {    // 创建setListener方法
        onChangeListener = onChange;
    }

    private static Boolean NextTasK;

    public static Boolean getNextTasK() {
        return NextTasK;
    }

    public static void setNextTasK(Boolean NextTasK) {
        NextTask.NextTasK = NextTasK;
        onChangeListener.onChange();
    }
}



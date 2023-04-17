package com.sendi.deliveredrobot.view.widget;

/**
 * @Author Swn
 * @describe
 * @Data 2023-04-13 12:35
 */

public class TaskNext {
    public interface OnChangeListener {    // 创建interface类
        void onChange();    // 值改变
    }

    private static OnChangeListener onChangeListener;    // 声明interface接口

    public static void setOnChangeListener(OnChangeListener onChange) {    // 创建setListener方法
        onChangeListener = onChange;
    }

    private static String toDo;

    public static String getToDo() {
        return toDo;
    }

    public static void setToDo(String toDo) {
        TaskNext.toDo = toDo;
        if (onChangeListener!=null) {
            onChangeListener.onChange();
        }
    }
}



package com.sendi.deliveredrobot.view.widget;

/**
 * @Author Swn
 * @Data 2023/9/28
 * @describe
 */
public class TaskArray {
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
        TaskArray.toDo = toDo;
        if (onChangeListener!=null) {
            onChangeListener.onChange();
        }
    }
}

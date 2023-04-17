package com.sendi.deliveredrobot.viewmodel;

import androidx.lifecycle.ViewModel;

public class BaseViewModel extends ViewModel {
    //更具字符串长度添加换行符
    public String getLength(String string) {
        //记录一共有多少位字符
        double valueLength = 0;
        //中文编码
        String chinese = "[\u4e00-\u9fa5]";
        //定义一个StringBuffer存储数据
        StringBuilder stringBuffer= new StringBuilder();
        //遍历判断哪些是中文和非中文
        for (int i = 0; i < string.length(); i++) {
            // 获取一个字符
            String temp = string.substring(i, i + 1);
            // 判断是否为中文字符
            if (temp.matches(chinese)) {
                // 中文字符长度为+1
                valueLength += 1;
            } else {
                // 其他字符长度为+0.5
                valueLength += 0.5;
            }
            //每个数据放入StringBuffer中
            stringBuffer.append(temp);
            //如果长度为5，开始换行
            if (valueLength >= 5){
                stringBuffer.append("\n");
                //清空valueLength
                valueLength = 0;
            }
        }
        //返回数据样式
        return new String(stringBuffer);
    }
    /**
     * 判断是否是照片
     */
    public static boolean checkIsImageFile(String fName) {
        boolean isImageFile;
        //获取拓展名
        String fileEnd = fName.substring(fName.lastIndexOf(".") + 1).toLowerCase();
        isImageFile = fileEnd.equals("jpg") || fileEnd.equals("png") || fileEnd.equals("gif")
                || fileEnd.equals("jpeg") || fileEnd.equals("bmp") || fileEnd.equals("JPG");
        return isImageFile;
    }

}

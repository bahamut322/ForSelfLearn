package com.sendi.deliveredrobot.view.widget;

public class Advance {

    public String path;//路径  我使用的是本地绝对路径
    public String type;//类型 1、视频 2、图片
    public int viewType;//图片：2:填充 1:自适应 视频：0全 1平铺

    public Advance(String path, String type,int viewType) {
        this.path = path;
        this.type = type;
        this.viewType = viewType;
    }
}

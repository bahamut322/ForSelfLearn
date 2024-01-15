package com.sendi.deliveredrobot.entity;

import org.litepal.crud.LitePalSupport;

import java.util.List;
//路线列表
public class Table_Route extends LitePalSupport {

    private int id;

    private String routeName;//路线名字

    private String rootMapName;//总图名字

    private String backgroundPic;//路线背景图

    private String introduction;//简介

    private Long timeStamp;//配置时间戳

    private List<Table_Point_Config> mapPointName;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public String getRootMapName() {
        return rootMapName;
    }

    public void setRootMapName(String rootMapName) {
        this.rootMapName = rootMapName;
    }

    public String getBackgroundPic() {
        return backgroundPic;
    }

    public void setBackgroundPic(String backgroundPic) {
        this.backgroundPic = backgroundPic;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public List<Table_Point_Config> getMapPointName() {
        return mapPointName;
    }

    public void setMapPointName(List<Table_Point_Config> mapPointName) {
        this.mapPointName = mapPointName;
    }
}

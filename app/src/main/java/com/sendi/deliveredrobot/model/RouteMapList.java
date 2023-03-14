package com.sendi.deliveredrobot.model;

public class RouteMapList {
    private int id;
    private String rootMapName;
    private String routeName;
    private String backGroundPic;
    private String introduction;
    private Long timeStamp;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRootMapName() {
        return rootMapName;
    }

    public void setRootMapName(String rootMapName) {
        this.rootMapName = rootMapName;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public String getBackGroundPic() {
        return backGroundPic;
    }

    public void setBackGroundPic(String backGroundPic) {
        this.backGroundPic = backGroundPic;
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
}

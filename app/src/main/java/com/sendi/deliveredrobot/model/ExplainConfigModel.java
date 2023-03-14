package com.sendi.deliveredrobot.model;

public class ExplainConfigModel {
    private int id;
    private String pointListText;
    private String interruptionText;
    private String endText;
    private int stayTime;
    private String routeListText;
    private String startText;
    private String slogan;
    private Long timeStamp;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPointListText() {
        return pointListText;
    }

    public void setPointListText(String pointListText) {
        this.pointListText = pointListText;
    }

    public String getInterruptionText() {
        return interruptionText;
    }

    public void setInterruptionText(String interruptionText) {
        this.interruptionText = interruptionText;
    }

    public String getEndText() {
        return endText;
    }

    public void setEndText(String endText) {
        this.endText = endText;
    }

    public int getStayTime() {
        return stayTime;
    }

    public void setStayTime(int stayTime) {
        this.stayTime = stayTime;
    }

    public String getRouteListText() {
        return routeListText;
    }

    public void setRouteListText(String routeListText) {
        this.routeListText = routeListText;
    }

    public String getStartText() {
        return startText;
    }

    public void setStartText(String startText) {
        this.startText = startText;
    }

    public String getSlogan() {
        return slogan;
    }

    public void setSlogan(String slogan) {
        this.slogan = slogan;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }
}

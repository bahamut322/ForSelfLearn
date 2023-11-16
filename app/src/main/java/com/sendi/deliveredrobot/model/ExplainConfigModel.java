package com.sendi.deliveredrobot.model;

/**
 * 智能讲解模式Model
 */
public class ExplainConfigModel {
    private int id;
    private String pointListText = "点击查看路线";
    private String interruptionText = "我的任务完成了，祝您生活愉快，拜拜咯";
    private String endText = "我的任务完成了，祝您生活愉快，拜拜咯";
    private int stayTime = 30;
    private String routeListText = "欢迎使用讲解模式";
    private String startText = "我们要了开始讲解";
    private String slogan = "智能讲解";
    private Long timeStamp = 0L;

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

package com.sendi.deliveredrobot.model;

public class SendRoutesModel {
    private String RouteName;
    private Long routeTimeStamp;

    public String getRouteName() {
        return RouteName;
    }

    public void setRouteName(String routeName) {
        RouteName = routeName;
    }

    public Long getRouteTimeStamp() {
        return routeTimeStamp;
    }

    public void setRouteTimeStamp(Long routeTimeStamp) {
        this.routeTimeStamp = routeTimeStamp;
    }
}

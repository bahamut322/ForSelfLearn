package com.sendi.deliveredrobot.model.log;


import java.util.List;

/**
 * @author : yujx
 * @since : 2023/02/16
 */
public class RobotLog {

    private String robotId;

    private List<RobotBagLog> bag;

    private List<RobotNavLog> nav;

    private List<RobotAppLog> app;

    public String getRobotId() {
        return robotId;
    }

    public void setRobotId(String robotId) {
        this.robotId = robotId;
    }

    public List<RobotBagLog> getBag() {
        return bag;
    }

    public void setBag(List<RobotBagLog> bag) {
        this.bag = bag;
    }

    public List<RobotNavLog> getNav() {
        return nav;
    }

    public void setNav(List<RobotNavLog> nav) {
        this.nav = nav;
    }

    public List<RobotAppLog> getApp() {
        return app;
    }

    public void setApp(List<RobotAppLog> app) {
        this.app = app;
    }


}

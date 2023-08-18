package com.sendi.deliveredrobot.model.log;


/**
 * @author : yujx
 * @since : 2023/02/16
 */
public class RobotBagLog {

    private String path = "robotBagLog";

    private String filename;

    private String size;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }
}

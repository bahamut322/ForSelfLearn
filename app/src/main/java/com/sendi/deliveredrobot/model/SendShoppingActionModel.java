package com.sendi.deliveredrobot.model;

/**
 * @Author Swn
 * @Data 2023/11/8
 * @describe
 */
public class SendShoppingActionModel {
    private String name;
    private Long timeStamp;
    private String mapName;

    public String getMapName() {
        return mapName;
    }

    public void setMapName(String mapName) {
        this.mapName = mapName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }
}

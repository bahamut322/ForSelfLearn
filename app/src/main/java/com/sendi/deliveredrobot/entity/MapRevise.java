package com.sendi.deliveredrobot.entity;

import org.litepal.crud.LitePalSupport;

public class MapRevise extends LitePalSupport {
    private int id;
    private String mapName;
    private long time;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMapName() {
        return mapName;
    }

    public void setMapName(String mapName) {
        this.mapName = mapName;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}

package com.sendi.deliveredrobot.model;

import com.sendi.deliveredrobot.room.entity.QueryAllPointEntity;

import java.util.List;

public class Maps {
    private String mapName;
    private List<QueryAllPointEntity> pointList ;

    public String getMapName() {
        return mapName;
    }

    public void setMapName(String mapName) {
        this.mapName = mapName;
    }

    public List<QueryAllPointEntity> getPointList() {
        return pointList;
    }

    public void setPointList(List<QueryAllPointEntity> pointList) {
        this.pointList = pointList;
    }
}

package com.sendi.deliveredrobot.model;

import com.sendi.deliveredrobot.room.entity.QueryAllPointEntity;
import com.sendi.deliveredrobot.room.entity.QueryPointEntity;

public class ExplanationTraceModel {
    /** 地点名字 */
    private QueryPointEntity pointName;
    /** 描述 */
    private String acceptStation;
    /**图片*/
    private String pointImage;

    public ExplanationTraceModel() {
    }

    public ExplanationTraceModel(QueryPointEntity pointName, String acceptStation,String pointImage) {
        this.pointName = pointName;
        this.acceptStation = acceptStation;
        this.pointImage = pointImage;
    }

    public QueryPointEntity getPointName() {
        return pointName;
    }

    public void setPointName(QueryPointEntity pointName) {
        this.pointName = pointName;
    }

    public String getAcceptStation() {
        return acceptStation;
    }

    public void setAcceptStation(String acceptStation) {
        this.acceptStation = acceptStation;
    }

    public String getPointImage() {
        return pointImage;
    }

    public void setPointImage(String pointImage) {
        this.pointImage = pointImage;
    }
}

package com.sendi.deliveredrobot.ros.debug.dto;

public class TargetPointConfirmDto {
    /*
    前端交互Dto
    根据需要使用其中的属性 -> rename: id + subMapId + name ..
                       -> createPoint: subMapId + type + direction + name ..
     */
    private Integer id;
    private Integer subMapId;
    private Integer type;
    private String direction;
    private String name;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getSubMapId() {
        return subMapId;
    }

    public void setSubMapId(Integer subMapId) {
        this.subMapId = subMapId;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

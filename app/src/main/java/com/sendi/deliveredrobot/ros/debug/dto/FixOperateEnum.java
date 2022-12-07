package com.sendi.deliveredrobot.ros.debug.dto;

public enum FixOperateEnum {
    UP_MOVE(8, "上"),
    DOWN_MOVE(5, "下"),
    LEFT_MOVE(4, "左"),
    RIGHT_MOVE(6, "右"),
    TURN_LEFT(7, "左旋"),
    TURN_RIGHT(9, "右旋");


    private final int type;
    private final String name;

    FixOperateEnum(int type, String name) {
        this.type = type;
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public String getName() {
        return name;
    }
}

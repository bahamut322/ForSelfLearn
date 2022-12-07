package com.sendi.deliveredrobot.service;

/**
 * 仓门类型
 *
 * @author Sunzecong
 * @since 2021-11-09
 */
public enum DoorEnum {
    NO_USE_DOOR("未使用仓门", 0),
    FIRST_DOOR("1号仓", 1),
    SECOND_DOOR("2号仓", 2);

    private String name;

    private int code;

    DoorEnum(String name, int code) {
        this.name = name;
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public int getCode() {
        return code;
    }
}

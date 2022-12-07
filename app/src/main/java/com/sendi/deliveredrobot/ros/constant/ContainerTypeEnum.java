package com.sendi.deliveredrobot.ros.constant;

public enum ContainerTypeEnum {
    STATIC_MAP(1, "总图"),
    UPDATE_MAP(2, "子图");


    private final int type;
    private final String name;

    ContainerTypeEnum(int type, String name) {
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

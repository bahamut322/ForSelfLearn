package com.sendi.deliveredrobot.ros.debug.dto;

public enum MapTypeEnum {
    /**
     * 总图
     */
    ROOT_MAP(1),
    /**
     * 激光地图
     */
    LASER_MAP(2),
    /**
     * 路径地图
     */
    ROUTE_MAP(3),
    /**
     * 目标点地图
     */
    POINT_MAP(4),
    /**
     * 虚拟墙
     */
    VIRTUAL_WALL(5),
    /**
     * 单行道
     */
    SINGLE_LANE(6),
    /**
     * 限速区域
     */
    SPEED_LIMIT_AREA(7);

    private final int type;

    public int getType() {
        return type;
    }

    MapTypeEnum(int type) {
        this.type = type;
    }
}

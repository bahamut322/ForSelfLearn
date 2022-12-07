package com.sendi.deliveredrobot.ros.constant;

/**
 * Ros 交互结果code和描述
 *
 * @author eden
 */
public enum RosResultEnum {
    // ==================通用==================
    /**
     * 消息发送成功
     */
    SEND_MESSAGE_SUCCESS(1, "消息发送成功 "),
    /**
     * 消息发送失败
     */
    SEND_MESSAGE_FAILURE(-1, "消息发送失败 "),
    /**
     * 未知异常
     */
    UNKNOWN_EXCEPTION(100, "未知异常 "),
    /**
     * 参数类型不匹配
     */
    ARGUMENT_TYPE_MISMATCH(101, "参数类型不匹配 "),
    /**
     * 超时
     */
    TIME_OUT(102, "超时 "),
    /**
     * sleep Exception
     */
    SLEEP_EXCEPTION(103, "sleep Exception "),
    /**
     * 初始化Bean异常
     */
    INIT_MAP_EXCEPTION(104, "初始化Bean异常 "),

    // ==================ROS==================
    /**
     * ROS连接失败
     */
    ROS_CONNECT_ERROR(301, "ROS连接失败 "),

    /**
     * ROS超时
     */
    ROS_TIME_OUT(302, "ROS超时 "),

    /**
     * response获取失败
     */
    GET_RESPONSE_ERROR(302, "response获取失败，请检查参数！"),

    /**
     * response获取失败
     */
    GET_RESPONSE_PARSE_ERROR(302, "response解析失败！"),

    /**
     * INVALID_URL
     */
    INVALID_URL(303, "Client Url 错误 "),

    /**
     * JSON_PARSE_ERROR
     */
    JSON_PARSE_ERROR(304, "ROS JSON 解析错误 "),

    /**
     * LASER_FAIL_RESULT
     */
    LASER_SUCCESS_RESULT(1, "ROS 激光图返回成功结果 "),

    /**
     * LASER_FAIL_RESULT
     */
    LASER_FAIL_RESULT(305, "ROS 激光图返回错误结果 "),

    ;

    private final int code;

    private final String msg;

    RosResultEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    /**
     * 通过状态码获取枚举对象
     *
     * @param code 状态码
     * @return 枚举对象
     */
    public static RosResultEnum getByCode(int code) {
        for (RosResultEnum rosResultEnum : RosResultEnum.values()) {
            if (code == rosResultEnum.getCode()) {
                return rosResultEnum;
            }
        }
        return null;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}


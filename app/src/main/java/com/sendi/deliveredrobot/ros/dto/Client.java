package com.sendi.deliveredrobot.ros.dto;

import static com.sendi.deliveredrobot.ros.constant.JRosbridgeConstant.OP_CODE_CALL_SERVICE;

import com.alibaba.fastjson.JSONObject;

import java.util.Map;

/**
 * Ros Service-Client发送消息请求体
 */
public class Client {
    /**
     * 操作
     */
    private final String op;
    /**
     * service名称
     */
    private String service;
    /**
     * 参数
     */
    private Map<String, Object> args;

    /**
     * op默认call_service，请求服务
     *
     * @param service
     * @param args
     */
    public Client(String service, Map<String, Object> args) {
        this.op = OP_CODE_CALL_SERVICE;
        this.service = service;
        this.args = args;
    }

    public String getOp() {
        return op;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public Map<String, Object> getArgs() {
        return args;
    }

    public void setArgs(Map<String, Object> args) {
        this.args = args;
    }

    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
    }
}

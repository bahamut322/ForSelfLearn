package com.sendi.deliveredrobot.ros.debug.dto;

import java.io.Serializable;
import java.util.HashMap;

/**
 * 调试消息体
 */
public class MapResult implements Serializable {
    /**
     * 结果
     */
    private boolean flag;
    /**
     * 详细信息
     */
    private String msg;
    /**
     * 返回消息体
     */
    private HashMap<String, Object> data;

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public HashMap<String, Object> getData() {
        return data;
    }

    public void setData(HashMap<String, Object> data) {
        this.data = data;
    }
}

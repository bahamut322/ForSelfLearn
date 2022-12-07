package com.sendi.deliveredrobot.ros.dto;

import com.alibaba.fastjson.JSON;

import org.ros.internal.message.Message;

/**
 * 返回消息
 *
 * @author Sunzecong
 * @version 1.0
 * @CreateDate: 2021/5/26
 */
public class RosResult<T> {
    /**
     * 发送结果
     */
    private boolean flag;
    /**
     * url
     */
    private String url;
    /**
     * 详细信息
     */
    private String msg;
    /**
     * 返回消息体
     */
    private T response;

    /**
     * sequence
     */
//    private int seq;
    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public T getResponse() {
        return response;
    }

    public void setResponse(T response) {
        this.response = response;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}

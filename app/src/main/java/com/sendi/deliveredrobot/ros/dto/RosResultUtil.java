package com.sendi.deliveredrobot.ros.dto;

/**
 * 返回消息构建类
 *
 * @author Sunzecong
 * @version 1.0
 */
public class RosResultUtil {

    public static RosResult<Boolean> success(String url) {
        RosResult<Boolean> result = new RosResult<>();
        result.setUrl(url);
        result.setFlag(true);
        result.setResponse(true);
        result.setMsg("success");
        return result;
    }

    public static <M> RosResult<M> success(String url, M response) {
        RosResult<M> result = new RosResult<M>();
        result.setUrl(url);
        result.setFlag(true);
        result.setResponse(response);
        result.setMsg("success");
        return result;
    }

    public static <M> RosResult<M> success(String url, M response, String msg) {
        RosResult<M> result = new RosResult<>();
        result.setUrl(url);
        result.setFlag(true);
        result.setResponse(response);
        result.setMsg(msg);
        return result;
    }

    public static RosResult<Boolean> success(String url, String msg) {
        RosResult<Boolean> result = new RosResult<>();
        result.setUrl(url);
        result.setFlag(true);
        result.setResponse(true);
        result.setMsg(msg);
        return result;
    }

    public static RosResult<Boolean> failure(String url, String msg) {
        RosResult<Boolean> result = new RosResult<>();
        result.setUrl(url);
        result.setFlag(false);
        result.setResponse(false);
        result.setMsg(msg);
        return result;
    }

    public static <T> RosResult<T> failure(String url, String msg, Class<T> requireType) {
        RosResult<T> result = new RosResult<>();
        result.setUrl(url);
        result.setFlag(false);
        result.setResponse(null);
        result.setMsg(msg);
        return result;
    }

    public static RosResult<Boolean> failure(String msg) {
        RosResult<Boolean> result = new RosResult<>();
        result.setUrl("unknown url !");
        result.setFlag(false);
        result.setResponse(false);
        result.setMsg(msg);
        return result;
    }
}
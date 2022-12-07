package com.sendi.deliveredrobot.ros.debug.dto;

import java.util.HashMap;

/**
 * 返回消息构建类
 *
 * @author Sunzecong
 * @version 1.0
 * @CreateDate: 2021/9/2
 */
public class MapResultUtil {

    public static MapResult success(HashMap<String, Object> data) {
        MapResult result = new MapResult();
        result.setFlag(true);
        result.setData(data);
        result.setMsg("success");
        return result;
    }


    public static MapResult success(HashMap<String, Object> data, String msg) {
        MapResult result = new MapResult();
        result.setFlag(true);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }

    public static MapResult success() {
        MapResult result = new MapResult();
        result.setData(new HashMap<>());
        result.setFlag(true);
        result.setMsg("success");
        return result;
    }

    public static MapResult failure(String msg) {
        MapResult result = new MapResult();
        result.setFlag(false);
        result.setData(new HashMap<>());
        result.setMsg(msg);
        return result;
    }

    public static MapResult failure(HashMap<String, Object> data, String msg) {
        MapResult result = new MapResult();
        result.setFlag(false);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }
}
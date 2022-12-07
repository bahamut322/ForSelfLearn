package com.sendi.deliveredrobot.ros;

import com.alibaba.fastjson.JSONObject;
import com.sendi.deliveredrobot.ros.constant.Constant;
import com.sendi.deliveredrobot.ros.constant.ContainerTypeEnum;
import com.sendi.deliveredrobot.ros.constant.RosResultEnum;
import com.sendi.deliveredrobot.utils.LogUtil;

import java.util.ArrayList;
import java.util.regex.Pattern;

import geometry_msgs.Point32;

/**
 * ros 激光图json处理工具类
 *
 * @author eden
 */
public class RosPointArrUtil {
    public static ArrayList<float[]> updateMap = new ArrayList<>();
    public static ArrayList<float[]> staticMap = new ArrayList<>();
    public static ArrayList<Point32> idInfo = new ArrayList<>();
    public static int result = 0;

    /**
     * 解析ros字符串中的result字段 int
     *
     * @param message ros返回json
     * @return
     */
    public static int setResult(String message) {
        // 清空点图数据
        result = 0;
        // 截取调用结果
        String startIndexStr = "\"result\": ";
        String endIndexStr = ",";
        try {
            int startIndex = message.indexOf(startIndexStr) + startIndexStr.length();
            int endIndex = message.indexOf(endIndexStr, startIndex);
            if (startIndex < startIndexStr.length() || endIndex < 0 || startIndex >= endIndex) {
                return (result = RosResultEnum.JSON_PARSE_ERROR.getCode());
            }
            String res = "";
            for (int i = startIndex; i < endIndex; i++) {
                res += message.charAt(i);
            }
            if (!res.matches("[-]?[0-9]+"))
                return (result = RosResultEnum.JSON_PARSE_ERROR.getCode());
            result = Integer.parseInt(res);
        } catch (Exception e) {
            LogUtil.INSTANCE.e(RosResultEnum.JSON_PARSE_ERROR.getMsg() + e);
            result = RosResultEnum.LASER_FAIL_RESULT.getCode();
        }
        return result;
    }

    /**
     * 解析实时子图数据
     *
     * @param message   ros返回json
     * @param total     分割容器数量
     * @param container 地图数据容器
     */
    public static boolean parsePointCloudMapPoint(String message, int total, ContainerTypeEnum container) {
        String startIndexStr = "points\": [";
        String endIndexStr = "}]}";
        long start = System.currentTimeMillis();
        try {
            int startIndex = -1;
            int endIndex = -1;
            if (ContainerTypeEnum.STATIC_MAP.getType() == container.getType()) {
                startIndex = message.indexOf(startIndexStr) + startIndexStr.length();
                endIndex = message.indexOf(endIndexStr, startIndex);
            } else if (ContainerTypeEnum.UPDATE_MAP.getType() == container.getType()) {
                startIndex = message.lastIndexOf(startIndexStr) + startIndexStr.length();
                endIndex = message.lastIndexOf(endIndexStr);
            }
            if (endIndex < 0 || startIndex < startIndexStr.length() || startIndex >= endIndex) {
                throw new RuntimeException("未找到子图数据");
            }
            ArrayList<String> updateMapDivideList = equallyDivide(message.substring(startIndex, endIndex), total);
            ArrayList<float[]> mapList = getMapBy2Arr(updateMapDivideList);
            setContainer(mapList, container);
            long end = System.currentTimeMillis();
            LogUtil.INSTANCE.i("【子图数据设置完成】耗时：" + (end - start) / 1000);
        } catch (Exception e) {
            LogUtil.INSTANCE.e(RosResultEnum.JSON_PARSE_ERROR.getMsg() + e);
            return false;
        }
        return true;
    }

    public static boolean parsePointCloudMapPoint(String message, ContainerTypeEnum container) {
        return parsePointCloudMapPoint(message, 1, container);
    }

    /**
     * 解析idInfo，设置子图序号
     *
     * @param message ros返回json
     */
    public static boolean parseIdInfo(String message) {
        long start = System.currentTimeMillis();
        idInfo = null;
        String startIndexStr = "points\": [";
        String endIndexStr = "}]}";
        String splitRegex = "[}], [{]";
        try {
            int startIndex = message.lastIndexOf(startIndexStr) + startIndexStr.length();
            int endIndex = message.indexOf(endIndexStr, startIndex);
            if (endIndex < 0 || startIndex < 0) {
                throw new RuntimeException("【未找到idInfo数据】");
            }
            String idInfosStr = message.substring(startIndex, endIndex);
            Pattern pattern = Pattern.compile(splitRegex);
            String[] paperArr = pattern.split(idInfosStr);
            if (paperArr.length == 0) return false;
            idInfo = new ArrayList<>(paperArr.length);
            for (String pointStr : paperArr) {
                Point32 point32 = dealPoint(pointStr);
                if (point32 == null) continue;
                idInfo.add(point32);
            }
            long end = System.currentTimeMillis();
            LogUtil.INSTANCE.i("【idInfo数据设置完成】耗时(ms)：" + (end - start));
        } catch (Exception e) {
            LogUtil.INSTANCE.e(RosResultEnum.JSON_PARSE_ERROR.getMsg() + e);
            if (idInfo != null) idInfo.clear();
            return false;
        }
        return true;
    }

    /**
     * 解析全激光图数据
     *
     * @param message   ros返回json
     * @param total     分段数-32万点解析2秒，后续再考虑分段处理
     * @param container 地图数据容器
     */
    public static boolean parseIntArrMapPoint(String message, int total, ContainerTypeEnum container) {
        long start = System.currentTimeMillis();
        String startIndexStr = "\"data\": [";
        String endIndexStr = "], \"size\": ";
        try {
            // 数据索引头
            int startIndex = -1;
            if (ContainerTypeEnum.STATIC_MAP.getType() == container.getType()) {
                startIndex = message.indexOf(startIndexStr) + startIndexStr.length();
            } else if (ContainerTypeEnum.UPDATE_MAP.getType() == container.getType()) {
                startIndex = message.lastIndexOf(startIndexStr) + startIndexStr.length();
            }
            // 数据索引尾
            int endIndex = message.indexOf(endIndexStr, startIndex);
            if (endIndex < 0 || startIndex < startIndexStr.length() || startIndex >= endIndex) {
                throw new RuntimeException("【无地图数据】类型：" + container.getName() + ", startIndex: " + startIndex + ", endIndex: " + endIndex);
            }
            String mapStr = message.substring(startIndex, endIndex);
            String[] pointStrArr = mapStr.split(", ");
            if (pointStrArr.length == 0) {
                throw new RuntimeException("【无地图数据】类型：" + container.getName());
            }
            float[] staticMapArr = new float[pointStrArr.length];
            for (int i = 0; i < pointStrArr.length; i++) {
                staticMapArr[i] = Float.parseFloat(pointStrArr[i]) / 100;
            }
            ArrayList<float[]> result = new ArrayList<>(1);
            result.add(staticMapArr);
            setContainer(result, container);
            long end = System.currentTimeMillis();
//            LogUtil.INSTANCE.i("【地图数据设置完成】类型：" + container.getName() + ", 耗时(ms)：" + (end - start));
        } catch (Exception e) {
            LogUtil.INSTANCE.e(RosResultEnum.JSON_PARSE_ERROR.getMsg() + e);
            return false;
        }
        return true;
    }

    public static boolean parseIntArrMapPoint(String message, ContainerTypeEnum container) {
        return parseIntArrMapPoint(message, 1, container);
    }

    /**
     * 等分json
     *
     * @param pointsStr 点串 {"y": x.xxx, "x": x.xxx, "z": 0.0}, ...
     * @param divide    分段数
     * @return
     */
    private static ArrayList<String> equallyDivide(String pointsStr, int divide) {
        ArrayList<String> list = new ArrayList(divide);
        // 步长
        int h = pointsStr.length() / divide;
        if (divide == 1 || h < Constant.FIVE_HUNDRED) {
            list.add(pointsStr);
            return list;
        }
        // 等分点
        int left = 0;
        for (int i = 1; i < divide; i++) {
            int fstIndex = pointsStr.indexOf("{", h * i);
            list.add(pointsStr.substring(left + 1, fstIndex));
            left = fstIndex;
        }
        list.add(pointsStr.substring(left));
        return list;
    }

    /**
     * 遍历转化子字符串为float[]
     *
     * @param list
     * @return
     */
    private static ArrayList<float[]> getMapBy2Arr(ArrayList<String> list) {
        ArrayList<float[]> result = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            float[] paperResult = dealPaper(list.get(i));
            list.set(i, null);
            if (paperResult == null || paperResult.length == 0) continue;
            result.add(paperResult);
        }
        return result;
    }

    private static void setContainer(ArrayList<float[]> result, ContainerTypeEnum container) {
        if (ContainerTypeEnum.STATIC_MAP.getType() == container.getType()) {
            RosPointArrUtil.staticMap = result;
        } else if (ContainerTypeEnum.UPDATE_MAP.getType() == container.getType()) {
            RosPointArrUtil.updateMap = result;
        }
    }

    /**
     * 转化子字符串为float[]
     *
     * @param paper 子字符串 {"y": x.xxx, "x": x.xxx, "z": 0.0}, ...
     * @return
     */
    private static float[] dealPaper(String paper) {
        Pattern pattern = Pattern.compile("[}], [{]");
        String[] paperArr = pattern.split(paper);
        if (paperArr.length == 0) return null;
        float[] mapArr = new float[paperArr.length << 1];
        for (int i = 0; i < paperArr.length; i++) {
            String pointStr = paperArr[i];
            Point32 point32 = dealPoint(pointStr);
            if (point32 == null) continue;
            mapArr[i << 1] = point32.getX();
            mapArr[(i << 1) + 1] = point32.getY();
        }
        return mapArr;
    }

    /**
     * 转化单个点的字符串
     *
     * @param pointStr 子字符串 "y": x.xxx, "x": x.xxx, "z": x.xxx | {"y": x.xxx, "x": x.xxx, "z": x.xxx | "y": x.xxx, "x": x.xxx, "z": x.xxx}
     * @return
     */
    private static Point32 dealPoint(String pointStr) {
        if (!Pattern.matches("\"y\": .*\"z\": [0-9]+.?[0-9]*", pointStr)) {
            int lastIndex = pointStr.lastIndexOf("}");
            if (lastIndex < 0) {
                pointStr = pointStr.substring(pointStr.indexOf("\"y\""));
            } else {
                pointStr = pointStr.substring(pointStr.indexOf("\"y\""), lastIndex);
            }
        }
        String json = "{" + pointStr + "}";
        try {
            return JSONObject.parseObject(json, Point32.class);
        } catch (Exception e) {
            LogUtil.INSTANCE.e("【invalid point json】: " + json);
            return null;
        }
    }
}

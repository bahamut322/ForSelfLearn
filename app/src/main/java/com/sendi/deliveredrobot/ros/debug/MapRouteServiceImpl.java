package com.sendi.deliveredrobot.ros.debug;

import static com.sendi.deliveredrobot.ros.constant.ClientConstant.ROUTE_MAP_END;
import static com.sendi.deliveredrobot.ros.constant.ClientConstant.ROUTE_MAP_INIT;
import static com.sendi.deliveredrobot.ros.constant.ClientConstant.SHOW_PATH_MAP;
import static com.sendi.deliveredrobot.ros.constant.Constant.CMD;

import com.sendi.deliveredrobot.MyApplication;
import com.sendi.deliveredrobot.room.dao.DebugDao;
import com.sendi.deliveredrobot.room.database.DataBaseDeliveredRobotMap;
import com.sendi.deliveredrobot.room.entity.RouteMap;
import com.sendi.deliveredrobot.ros.ClientManager;
import com.sendi.deliveredrobot.ros.SubManager;
import com.sendi.deliveredrobot.ros.constant.ClientConstant;
import com.sendi.deliveredrobot.ros.debug.dto.MapResult;
import com.sendi.deliveredrobot.ros.debug.dto.MapResultUtil;
import com.sendi.deliveredrobot.ros.dto.Client;
import com.sendi.deliveredrobot.ros.dto.RosResult;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import label_msgs.DeleteMapResponse;
import label_msgs.ShowPathMapResponse;
import route_map_msgs.EndResponse;
import route_map_msgs.InitResponse;

/**
 * 调试中路径相关类
 * create by yujx
 *
 * @date 2021/09/01
 */

public class MapRouteServiceImpl {

    // get singleton
    public static MapRouteServiceImpl getInstance() {
        return MapRouteServiceImpl.MapRouteInnerClass.INSTANCE;
    }

    // private inner class
    private static class MapRouteInnerClass {
        private final static MapRouteServiceImpl INSTANCE = new MapRouteServiceImpl();
    }


    //开始创建路径
    //@param map_sub表中的path 建路径的雷达图
    public MapResult startRoute(String map_sub_path) throws InterruptedException {
        HashMap<String, Object> clientPara = new HashMap<>();
        clientPara.put("list_name", map_sub_path);
        Client client = new Client(ROUTE_MAP_INIT, clientPara);
        RosResult rosResult = ClientManager.sendClientMsg(client);
        if (!rosResult.isFlag()) {
            return MapResultUtil.failure("get response fail");
        }
        InitResponse initResponse = (InitResponse) rosResult.getResponse();
        MapResult result = new MapResult();
        switch (initResponse.getResult()) {
            case 1:
                result.setFlag(true);
                result.setMsg("成功");
                SubManager.sub(ClientConstant.ROBOT_POSE);
                SubManager.sub(ClientConstant.GLOBAL_LASER);
                break;
            case -3:
                result.setFlag(false);
                result.setMsg("传感器失败");
                break;
            case -2:
                result.setFlag(false);
                result.setMsg("状态失败");
                break;
            case -1:
                result.setFlag(false);
                result.setMsg("未知错误");
                break;
            case -5:
                result.setFlag(false);
                result.setMsg("加载地图失败");
                break;
            case -8:
                result.setFlag(false);
                result.setMsg("文件名缺失失败");
                break;
            case -10:
                result.setFlag(false);
                result.setMsg("运行线程重复失败");
                break;
            default:
                result.setFlag(false);
                result.setMsg("未定义失败码");
                break;
        }
        return result;
    }

    //获取路径图列表
    public List<RouteMap> getList() {
        List<RouteMap> arrayList;
        DebugDao dao = DataBaseDeliveredRobotMap.Companion.getDatabase(Objects.requireNonNull(MyApplication.Companion.getInstance())).getDebug();
        arrayList = dao.queryMapRoute();
        return arrayList;
    }

    //保存路径
    //路径名称
    public MapResult saveRoute(String name, Integer sub_map_id) throws InterruptedException {
        MapResult result = new MapResult();
        DebugDao dao = DataBaseDeliveredRobotMap.Companion.getDatabase(Objects.requireNonNull(MyApplication.Companion.getInstance())).getDebug();
        List<RouteMap> arrayList = dao.searchRouteName(name);
        if (!arrayList.isEmpty()) {
            result.setFlag(false);
            result.setMsg("文件名已经存在");
            return result;
        }

        HashMap<String, Object> clientPara = new HashMap<>();
        clientPara.put(CMD, 0);
        Client client = new Client(ROUTE_MAP_END, clientPara);
        RosResult rosResult = ClientManager.sendClientMsg(client);
        EndResponse endResponse = (EndResponse) rosResult.getResponse();
        switch (endResponse.getResult()) {
            case 1:
                result.setFlag(true);
                result.setMsg("成功");
                SubManager.unsub(ClientConstant.ROBOT_POSE);
                SubManager.unsub(ClientConstant.GLOBAL_LASER);
                RouteMap routeMap = new RouteMap(0, name, endResponse.getMapName(), sub_map_id);
                dao.insertMapRoute(routeMap);
                break;
            case -2:
                result.setFlag(false);
                result.setMsg("状态失败");
                break;
            case -1:
                result.setFlag(false);
                result.setMsg("未知错误");
                break;
            case -5:
                result.setFlag(false);
                result.setMsg("加载地图失败");
                break;
            case -6:
                result.setFlag(false);
                result.setMsg("原始数据失败");
                break;
            case -7:
                result.setFlag(false);
                result.setMsg("保存地图失败");
                break;
            default:
                result.setFlag(false);
                result.setMsg("未定义错误码");
                break;
        }
        return result;
    }

    //退出路径
    public MapResult exitRoute() throws InterruptedException {
        MapResult result = new MapResult();
        HashMap<String, Object> clientPara = new HashMap<>();
        clientPara.put(CMD, 1);
        Client client = new Client(ROUTE_MAP_END, clientPara);
        RosResult rosResult = ClientManager.sendClientMsg(client);
        EndResponse endResponse = (EndResponse) rosResult.getResponse();
        switch (endResponse.getResult()) {
            case 1:
                result.setFlag(true);
                result.setMsg("成功");
                SubManager.unsub(ClientConstant.ROBOT_POSE);
                break;
            case -2:
                result.setFlag(false);
                result.setMsg("状态失败");
                break;
            case -1:
                result.setFlag(false);
                result.setMsg("未知错误");
                break;
            case -5:
                result.setFlag(false);
                result.setMsg("加载地图失败");
                break;
            case -6:
                result.setFlag(false);
                result.setMsg("原始数据失败");
                break;
            case -7:
                result.setFlag(false);
                result.setMsg("保存地图失败");
                break;
            default:
                result.setFlag(false);
                result.setMsg("未定义错误码");
                break;
        }
        SubManager.unsub(ClientConstant.GLOBAL_LASER);
        return result;
    }

    //删除路径图
    public MapResult deleteRoute(int id) {
        MapResult mapResult = new MapResult();
        DebugDao dao = DataBaseDeliveredRobotMap.Companion.getDatabase(Objects.requireNonNull(MyApplication.Companion.getInstance())).getDebug();
        List<Integer> list = dao.cantDeleteRoute(id);
        if (!list.isEmpty()) {
            mapResult.setFlag(false);
            mapResult.setMsg("删除前请在总图删除绑定关系");
            return mapResult;
        }
        dao.deleteRoute(id);
        mapResult.setFlag(true);
        mapResult.setMsg("删除成功");
        return mapResult;
    }

    //重置路径图 废弃方法，不允许重置
    @Deprecated
    public MapResult resetRoute(String map_sub_path) {
        MapResult mapResult = new MapResult();
        HashMap<String, Object> clientPara = new HashMap<>();
        clientPara.put("map_name", map_sub_path);
        Client client = new Client(ClientConstant.MAP_DELETE, clientPara);
        RosResult rosResult = ClientManager.sendClientMsg(client);
        DeleteMapResponse deleteMapResponse = (DeleteMapResponse) rosResult.getResponse();
        switch (deleteMapResponse.getResult()) {
            case 1:
                mapResult.setFlag(true);
                mapResult.setMsg("重置成功");
                break;
            case -1:
                mapResult.setFlag(false);
                mapResult.setMsg("未知错误");
                break;
            case -2:
                mapResult.setFlag(false);
                mapResult.setMsg("状态错误");
                break;
            case -8:
                mapResult.setFlag(false);
                mapResult.setMsg("文件名错误");
                break;
            default:
                mapResult.setFlag(false);
                mapResult.setMsg("未定义错误码");
                break;
        }
        return mapResult;
    }

    //查询底盘路径地图点集
    //@param 路径地图名称（底盘中的名称，不是应用中起的名字）
    public MapResult queryPathMap(String map_name) throws InterruptedException {
        MapResult result = new MapResult();
        HashMap<String, Object> clientPara = new HashMap<>();
        clientPara.put("map_name", map_name);
        Client client = new Client(SHOW_PATH_MAP, clientPara);
        RosResult rosResult = ClientManager.sendClientMsg(client);
        ShowPathMapResponse showPathMapResponse = (ShowPathMapResponse) rosResult.getResponse();
        switch (showPathMapResponse.getResult()) {
            case 1:
                result.setFlag(true);
                result.setMsg("成功");
                HashMap<String, Object> map = new HashMap<>();
                map.put("data", showPathMapResponse.getMap());
                result.setData(map);
                break;
            case -1:
                result.setFlag(false);
                result.setMsg("未知错误");
                break;
            case -2:
                result.setFlag(false);
                result.setMsg("状态错误");
                break;
            case -5:
                result.setFlag(false);
                result.setMsg("加载地图失败");
                break;
            case -8:
                result.setFlag(false);
                result.setMsg("文件名错误");
                break;
            default:
                result.setFlag(false);
                result.setMsg("未定义错误码");
                break;
        }
        return result;
    }
}

package com.sendi.deliveredrobot.ros.debug;

import static com.sendi.deliveredrobot.ros.constant.ClientConstant.MAP_CHECK;
import static com.sendi.deliveredrobot.ros.constant.ClientConstant.MAP_REBASE;
import static com.sendi.deliveredrobot.ros.constant.ClientConstant.MAP_UPDATE;
import static com.sendi.deliveredrobot.ros.constant.ClientConstant.OUT_IN_MAP;
import static com.sendi.deliveredrobot.ros.constant.Constant.CMD;

import com.sendi.deliveredrobot.MyApplication;
import com.sendi.deliveredrobot.room.dao.DebugDao;
import com.sendi.deliveredrobot.room.database.DataBaseDeliveredRobotMap;
import com.sendi.deliveredrobot.room.entity.RelationshipLift;
import com.sendi.deliveredrobot.room.entity.SubMap;
import com.sendi.deliveredrobot.ros.ClientManager;
import com.sendi.deliveredrobot.ros.RosPointArrUtil;
import com.sendi.deliveredrobot.ros.SubManager;
import com.sendi.deliveredrobot.ros.constant.ClientConstant;
import com.sendi.deliveredrobot.ros.constant.Constant;
import com.sendi.deliveredrobot.ros.constant.RosResultEnum;
import com.sendi.deliveredrobot.ros.debug.dto.FixOperateEnum;
import com.sendi.deliveredrobot.ros.debug.dto.MapResult;
import com.sendi.deliveredrobot.ros.debug.dto.MapResultUtil;
import com.sendi.deliveredrobot.ros.dto.Client;
import com.sendi.deliveredrobot.ros.dto.RosResult;
import com.sendi.deliveredrobot.utils.LogUtil;

import java.util.HashMap;
import java.util.Objects;

import map_msgs.Laser_map_managerResponse;
import map_msgs.New_empty_mapResponse;
import map_msgs.RebaseResponse;

public class MapLaserServiceImpl implements IMapLaserService {
    private final DebugDao dao = DataBaseDeliveredRobotMap.Companion.getDatabase(Objects.requireNonNull(MyApplication.Companion.getInstance())).getDebug();
    /**
     * 当前新建激光地图路径
     */
    private String curOriginalName = "";
    /**
     * 当前新建激光地图名称
     */
    private String curName = "";
    /**
     * 当前新建激光地图楼层编码
     */
//    private int floorCode = -1;
    /**
     * 当前新建激光地图楼层名
     */
    private String floorName = "";

    // get singleton
    public static IMapLaserService getInstance() {
        return MapLaserInnerClass.INSTANCE;
    }

    // private inner class
    private static class MapLaserInnerClass {
        private final static IMapLaserService INSTANCE = new MapLaserServiceImpl();
    }

    // private constructor
    public MapLaserServiceImpl() {
    }

    @Override
    public MapResult create() {
        Client client = new Client(ClientConstant.NEW_EMPTY_MAP, new HashMap<>());
        RosResult rosResult = ClientManager.sendClientMsg(client);
        if (!rosResult.isFlag())
            return MapResultUtil.failure(RosResultEnum.GET_RESPONSE_ERROR.getMsg());
        New_empty_mapResponse response = (New_empty_mapResponse) rosResult.getResponse();
        int result = response.getResult();
        if (result != 1) {
            String msg;
            switch (result) {
                case -1:
                    msg = "未知错误";
                    break;
                case -2:
                    msg = "创建文件夹失败";
                    break;
                case -11:
                    msg = "状态失败";
                    break;
                default:
                    msg = "未定义错误";
                    break;
            }
            return MapResultUtil.failure(msg);
        }
        // 保存当前扫描图文件名
        this.curOriginalName = response.getMapName();
        return MapResultUtil.success();
    }

    @Override
    public MapResult startScan(String name, /*int floorCode,*/ String floorName) {
        if (name == null || "".equals(name) || /*floorCode < 0 ||*/ floorName == null || "".equals(floorName))
            return MapResultUtil.failure(RosResultEnum.ARGUMENT_TYPE_MISMATCH.getMsg());
        MapResult mapResult = sendLaserMapManagerMsg(1);
        if (mapResult.isFlag()) {
            // 订阅实时子图数据
            this.curName = name;
//            this.floorCode = floorCode;
            this.floorName = floorName;
            switchSubMapInfo(true, ClientConstant.SUB_MAP_INFO);
            switchSubMapInfo(true, ClientConstant.PAUSE_CHECK);
        }
        return mapResult;
    }

    @Override
    @Deprecated
    public MapResult startScan(String name) {
        if (name == null || "".equals(name))
            return MapResultUtil.failure(RosResultEnum.ARGUMENT_TYPE_MISMATCH.getMsg());
        MapResult mapResult = sendLaserMapManagerMsg(1);
        if (mapResult.isFlag()) {
            // 订阅实时子图数据
            this.curName = name;
            switchSubMapInfo(true, ClientConstant.SUB_MAP_INFO);
            switchSubMapInfo(true, ClientConstant.PAUSE_CHECK);
        }
        return mapResult;
    }

    @Override
    public MapResult stop() {
        // 取消订阅实时子图数据
        switchSubMapInfo(false, ClientConstant.SUB_MAP_INFO);
        switchSubMapInfo(false, ClientConstant.PAUSE_CHECK);
        // 编辑情况下执行停止扫描操作不向数据库中插入数据
        createLaserMapAndLiftRelationShip();
        return sendLaserMapManagerMsg(4);
    }

    @Override
    public MapResult saveSubMap() {
        switchSubMapInfo(true, ClientConstant.SUB_MAP_INFO);
        return sendSubMapCheckMsg(1);
    }

    @Override
    public MapResult resetSubMap() {
        // 1.重置40帧图，底盘直接退出建图，不需要stop/quit
        MapResult mapResult = sendSubMapCheckMsg(-1);
        if (!mapResult.isFlag()) return mapResult;
        // 2.停止扫描
        MapResult stop = stop();
//        if (!stop.isFlag()) return stop;
        // 3.退出
        MapResult quit = quit(true);
        return mapResult;
    }

    @Override
    public MapResult clearUpdateMessage() {
        return sendUpdateMapMsg(3);
    }

    @Override
    public MapResult showFixMap() {
        return sendUpdateMapMsg(1);
    }

    @Override
    public MapResult showOriginalMap() {
        return sendUpdateMapMsg(2);
    }

    public MapResult chooseFilesToFix(Integer fstMapId, Integer scdMapId) {
        if (fstMapId == null || fstMapId < 0 || scdMapId == null || scdMapId < 0 || fstMapId.equals(scdMapId))
            return MapResultUtil.failure(RosResultEnum.ARGUMENT_TYPE_MISMATCH.getMsg());
        HashMap<String, Object> para = new HashMap<>();
        // 谁小修谁
        para.put("from_id", Math.min(fstMapId, scdMapId));
        para.put("to_id", Math.max(fstMapId, scdMapId));
        Client client = new Client(ClientConstant.LOOP_PAIR, para);
        RosResult rosResult = ClientManager.sendClientMsg(client);
        if (!rosResult.isFlag())
            return MapResultUtil.failure(RosResultEnum.GET_RESPONSE_ERROR.getMsg());
        int result = RosPointArrUtil.result;
        if (result != RosResultEnum.LASER_SUCCESS_RESULT.getCode()) {
            String msg;
            switch (result) {
                case -1:
                    msg = "未知错误";
                    break;
                case -2:
                    msg = "状态失败";
                    break;
                default:
                    msg = "未定义错误";
                    break;
            }
            return MapResultUtil.failure(msg);
        }
        return MapResultUtil.success();
    }

    @Override
    public MapResult fix(FixOperateEnum type, int distance) {
        HashMap<String, Object> para = new HashMap<>();
        // direction -> 旋转方向或者移动方向
        para.put("diretion", type.getType());
        para.put(Constant.VALUE, distance);
        Client client = new Client(ClientConstant.MOVE_SUB_MAP, para);
        RosResult rosResult = ClientManager.sendClientMsg(client);
        if (!rosResult.isFlag())
            return MapResultUtil.failure(RosResultEnum.GET_RESPONSE_ERROR.getMsg());
        int result = RosPointArrUtil.result;
        if (result != RosResultEnum.LASER_SUCCESS_RESULT.getCode()) {
            String msg;
            switch (result) {
                case -1:
                    msg = "未知错误";
                    break;
                case -2:
                    msg = "状态失败";
                    break;
                default:
                    msg = "未定义错误";
                    break;
            }
            return MapResultUtil.failure(msg);
        }
        return MapResultUtil.success();
    }

    @Override
    public MapResult saveFix(int cmd) {
        HashMap<String, Object> para = new HashMap<>();
        para.put(CMD, cmd);
        Client client = new Client(ClientConstant.CHECK_LOOP, para);
        RosResult rosResult = ClientManager.sendClientMsg(client);
        if (!rosResult.isFlag())
            return MapResultUtil.failure(RosResultEnum.GET_RESPONSE_ERROR.getMsg());
        Laser_map_managerResponse response = (Laser_map_managerResponse) rosResult.getResponse();
        int result = response.getResult();
        if (result != 1) {
            String msg;
            switch (result) {
                case -1:
                    msg = "未知错误";
                    break;
                case -2:
                    msg = "状态失败";
                    break;
                case -12:
                    msg = "非法指令";
                    break;
                default:
                    msg = "未定义错误";
                    break;
            }
            return MapResultUtil.failure(msg);
        }
        return MapResultUtil.success();
    }

    @Override
    public MapResult quit() {
        return quit(false);
    }

    /**
     * @param hasStop 是否先停止扫描
     * @return
     */
    private MapResult quit(boolean hasStop) {
        this.curName = "";
        this.curOriginalName = "";
        if (!hasStop) {
            switchSubMapInfo(false, ClientConstant.SUB_MAP_INFO);
            switchSubMapInfo(false, ClientConstant.PAUSE_CHECK);
        }
        return sendLaserMapManagerMsg(3);
    }

    @Override
    public MapResult showLaserMap(String originalName) {
        if (originalName == null || "".equals(originalName))
            return MapResultUtil.failure(RosResultEnum.ARGUMENT_TYPE_MISMATCH.getMsg());
        this.curOriginalName = originalName;
        HashMap<String, Object> para = new HashMap<>();
        para.put("map_name", originalName);
//        para.put("map_name", "/home/rpdzkj/catkin_ws/src/sendi_mapping/map/2021-09-17#17-26-08");
        Client client = new Client(ClientConstant.GET_GLOBAL_MAP, para);
        RosResult rosResult = ClientManager.sendClientMsg(client);
        if (!rosResult.isFlag())
            return MapResultUtil.failure(RosResultEnum.GET_RESPONSE_ERROR.getMsg());
        int result = RosPointArrUtil.result;
        if (result != RosResultEnum.LASER_SUCCESS_RESULT.getCode()) {
            String msg;
            switch (result) {
                case -20:
                    msg = "地图数量错误";
                    break;
                case -6:
                    msg = "地图数据异常";
                    break;
                case -72:
                    msg = "获取地图失败";
                    break;
                default:
                    msg = "未定义错误";
                    break;
            }
            return MapResultUtil.failure(msg);
        }
        return MapResultUtil.success();
    }

    @Override
    public MapResult updateOriginalFile(int cmd) {
        // 新办公区，最大的图    处理时间2分半
//        this.curOriginalName = "/home/rpdzkj/catkin_ws/src/sendi_mapping/map/2021-09-17#17-26-08";
        // 旧办公区，约最大图的8分之一
//        this.curOriginalName = "/home/rpdzkj/catkin_ws/src/sendi_mapping/map/2021-08-05#10-37-47";
        return sendRebaseMapMsg(1, this.curOriginalName);
    }


    @Override
    public MapResult updateCopyFile(int cmd, String name, /*int floorCode,*/ String floorName) {
        if (name == null || "".equals(name) || /*floorCode < 0 ||*/ floorName == null || "".equals(floorName))
            return MapResultUtil.failure(RosResultEnum.ARGUMENT_TYPE_MISMATCH.getMsg());
        MapResult mapResult = sendRebaseMapMsg(2, this.curOriginalName);
        if (mapResult.isFlag()) {
            this.curName = name;
//            this.floorCode = floorCode;
            this.floorName = floorName;
            // curOriginalName has changed
            createLaserMapAndLiftRelationShip();
        }
        return mapResult;
    }

    @Override
    public MapResult updateCopyFile(int cmd, String name) {
        if (name == null || "".equals(name))
            return MapResultUtil.failure(RosResultEnum.ARGUMENT_TYPE_MISMATCH.getMsg());
        MapResult mapResult = sendRebaseMapMsg(2, this.curOriginalName);
        if (mapResult.isFlag()) {
            // curOriginalName has changed
            new Thread(() -> dao.insertLaserMap(new SubMap(0, name, this.curOriginalName, 0, 0, 0))).start();
        }
        return mapResult;
    }

    @Override
    public MapResult redirect(int cmd) {
        return redirect(cmd, this.curOriginalName);
    }

    @Override
    public MapResult redirect(int cmd, String path) {
        this.curOriginalName = path;
        return sendRedirectMsg(cmd, path);
    }

    @Override
    public MapResult continueScan() {
        MapResult mapResult = sendLaserMapManagerMsg(1);
        if (mapResult.isFlag()) {
            // 订阅实时子图数据
            switchSubMapInfo(true, ClientConstant.SUB_MAP_INFO);
            // 订阅实时40帧子图数据
            switchSubMapInfo(true, ClientConstant.PAUSE_CHECK);
        }
        return mapResult;
    }

    @Override
    public MapResult continueFix() {
        return sendLaserMapManagerMsg(5);
    }

    @Override
    public MapResult importLaserMap(String originalName) {
        if (originalName == null || "".equals(originalName))
            return MapResultUtil.failure(RosResultEnum.ARGUMENT_TYPE_MISMATCH.getMsg());
        return sendMapOutInMsg(2, originalName);
    }

    @Override
    public MapResult exportLaserMap(String originalName) {
        if (originalName == null || "".equals(originalName))
            return MapResultUtil.failure(RosResultEnum.ARGUMENT_TYPE_MISMATCH.getMsg());
        return sendMapOutInMsg(1, originalName);
    }

    /**
     * 发送激光建图指令
     *
     * @param cmd 1：开始建立子图
     *            2：开始处理路径和目标点
     *            3：退出
     *            4：完成子图的建立
     * @return
     */
    public MapResult sendLaserMapManagerMsg(int cmd) {
        HashMap<String, Object> para = new HashMap<>();
        para.put(CMD, cmd);
        Client client = new Client(ClientConstant.START_BUILD_MAP, para);
        RosResult rosResult = ClientManager.sendClientMsg(client);
        if (!rosResult.isFlag())
            return MapResultUtil.failure(RosResultEnum.GET_RESPONSE_ERROR.getMsg());
        Laser_map_managerResponse response = (Laser_map_managerResponse) rosResult.getResponse();
        int result = response.getResult();
        if (result != RosResultEnum.LASER_SUCCESS_RESULT.getCode()) {
            String msg;
            switch (result) {
                case -1:
                    msg = "未知错误";
                    break;
                case -10:
                    msg = "已经存在重复线程";
                    break;
                case -12:
                    msg = "非法指令";
                    break;
                case -2:
                    msg = "状态失败";
                    break;
                default:
                    msg = "未定义错误";
                    break;
            }
            return MapResultUtil.failure(msg);
        }
        return MapResultUtil.success();
    }

    /**
     * 确认子图
     *
     * @param cmd 1: 确认正常
     *            -1：子图异常
     * @return
     */
    private MapResult sendSubMapCheckMsg(int cmd) {
        HashMap<String, Object> para = new HashMap<>();
        para.put(CMD, cmd);
        Client subMapCheck = new Client(MAP_CHECK, para);
        RosResult rosResult = ClientManager.sendClientMsg(subMapCheck);
        if (!rosResult.isFlag())
            return MapResultUtil.failure(RosResultEnum.GET_RESPONSE_ERROR.getMsg());
        Laser_map_managerResponse response = (Laser_map_managerResponse) rosResult.getResponse();
        int result = response.getResult();
        if (result != RosResultEnum.LASER_SUCCESS_RESULT.getCode()) {
            String msg;
            switch (result) {
                case -1:
                    msg = "未知错误";
                    break;
                case -20:
                    msg = "子图数量错误";
                    break;
                case -12:
                    msg = "非法指令";
                    break;
                case -2:
                    msg = "状态失败";
                    break;
                default:
                    msg = "未定义错误";
                    break;
            }
            return MapResultUtil.failure(msg);
        }
        return MapResultUtil.success();
    }

    /**
     * 更新地图
     *
     * @param cmd 1：读取回环信息，进行地图优化，然后返回优化后的地图数据，最后总的全局地图会同步更新。
     *            2：不读取回环，只显示原始的地图，最后的全局地图不变。
     *            3：清除所有的回环信息，进行地图优化（实际上就是把全局地图变为没有回环的样子）
     * @return
     */
    private MapResult sendUpdateMapMsg(int cmd) {
        HashMap<String, Object> para = new HashMap<>();
        para.put(CMD, cmd);
        Client subMapCheck = new Client(MAP_UPDATE, para);
        RosResult rosResult = ClientManager.sendClientMsg(subMapCheck);
        if (!rosResult.isFlag())
            return MapResultUtil.failure(RosResultEnum.GET_RESPONSE_ERROR.getMsg());
        int result = RosPointArrUtil.result;
        if (result != RosResultEnum.LASER_SUCCESS_RESULT.getCode()) {
            String msg;
            switch (result) {
                case -1:
                    msg = "未知错误";
                    break;
                case -12:
                    msg = "非法指令";
                    break;
                case -2:
                    msg = "状态失败";
                    break;
                default:
                    msg = "未定义错误";
                    break;
            }
            return MapResultUtil.failure(msg);
        }
        return MapResultUtil.success();
    }

    /**
     * 基于已有地图基础上建图
     *
     * @param cmd          1: 直接修改source_name的地图
     *                     2：深复制一份source_name的地图形成target_name，后续的改动在target_name地图上，不会影响source_name的地图。
     * @param originalName 基础地图名称
     * @return
     */
    private MapResult sendRebaseMapMsg(int cmd, String originalName) {
        HashMap<String, Object> para = new HashMap<>();
        para.put(CMD, cmd);
        para.put("source_name", originalName);
        Client subMapCheck = new Client(MAP_REBASE, para);
        RosResult rosResult = ClientManager.sendClientMsg(subMapCheck);
        if (!rosResult.isFlag())
            return MapResultUtil.failure(RosResultEnum.GET_RESPONSE_ERROR.getMsg());
        RebaseResponse response = (RebaseResponse) rosResult.getResponse();
        int result = response.getResult();
        if (result != RosResultEnum.LASER_SUCCESS_RESULT.getCode()) {
            String msg;
            switch (result) {
                case -1:
                    msg = "未知错误";
                    break;
                case -12:
                    msg = "非法指令";
                    break;
                case -2:
                    msg = "状态失败";
                    break;
                default:
                    msg = "未定义错误";
                    break;
            }
            return MapResultUtil.failure(msg);
        }
        // 副文本编辑
        if (cmd == 2) {
            this.curOriginalName = response.getTargetName();
        }
        return MapResultUtil.success();
    }

    /**
     * 订阅、取消订阅topic
     *
     * @param button
     * @param topic
     * @return
     */
    private boolean switchSubMapInfo(boolean button, String topic) {
        return button ? SubManager.sub(topic) : SubManager.unsub(topic);
    }

    /**
     * 发送导入、导出文件指令
     *
     * @param cmd          1: 导出
     *                     2：导入
     * @param originalName
     * @return
     */
    private MapResult sendMapOutInMsg(int cmd, String originalName) {
        HashMap<String, Object> para = new HashMap<>();
        para.put(CMD, cmd);
        para.put("source_name", originalName);
        Client subMapCheck = new Client(OUT_IN_MAP, para);
        RosResult rosResult = ClientManager.sendClientMsg(subMapCheck);
        if (!rosResult.isFlag())
            return MapResultUtil.failure(RosResultEnum.GET_RESPONSE_ERROR.getMsg());
        RebaseResponse response = (RebaseResponse) rosResult.getResponse();
        int result = response.getResult();
        if (result != RosResultEnum.LASER_SUCCESS_RESULT.getCode()) {
            String msg;
            switch (result) {
                case -1:
                    msg = "未知错误";
                    break;
                default:
                    msg = "未定义错误";
                    break;
            }
            return MapResultUtil.failure(msg);
        }
        if (cmd == 1) {
            HashMap<String, Object> resultMap = new HashMap<>();
            resultMap.put("targetName", response.getTargetName());
            return MapResultUtil.success(resultMap);
        }
        return MapResultUtil.success();
    }

    /**
     * 重定向消息发送
     *
     * @param cmd          1：在子图建立时进行重定位
     *                     2：在进行路径以及目标点时进行重定位
     * @param originalName 地图文件名
     * @return
     */
    public MapResult sendRedirectMsg(int cmd, String originalName) {
        HashMap<String, Object> para = new HashMap<>();
        para.put(CMD, cmd);
        para.put("map_name", originalName);
        Client client = new Client(ClientConstant.INIT_LOCATION, para);
        RosResult rosResult = ClientManager.sendClientMsg(client);
        if (!rosResult.isFlag())
            return MapResultUtil.failure(RosResultEnum.GET_RESPONSE_ERROR.getMsg());
        int result = RosPointArrUtil.result;
        if (result != RosResultEnum.LASER_SUCCESS_RESULT.getCode()) {
            String msg;
            switch (result) {
                case -1:
                    msg = "未知错误";
                    break;
                case -2:
                    msg = "状态失败";
                    break;
                case -12:
                    msg = "非法指令";
                    break;
                default:
                    msg = "未定义错误";
                    break;
            }
            return MapResultUtil.failure(msg);
        }
        return MapResultUtil.success();
    }

    private MapResult createLaserMapAndLiftRelationShip() {
        if ("".equals(this.curName)) {
            return MapResultUtil.failure(RosResultEnum.ARGUMENT_TYPE_MISMATCH.getMsg());
        }
        new Thread(() -> {
            SubMap laserMap = new SubMap(0, this.curName, this.curOriginalName, 0, 0, 0);
            long laserMapId = dao.insertLaserMap(laserMap);
            RelationshipLift lift = new RelationshipLift(0, (int) laserMapId, /*this.floorCode,*/ this.floorName);
            dao.insertRelationshipLift(lift);
            LogUtil.INSTANCE.i("【MapLaserService】插入一条激光图数据 " + laserMap + "\t" + lift);
            this.curName = "";
//            this.floorCode = -1;
            this.floorName = "";
        }).start();
        return MapResultUtil.success();
    }
}

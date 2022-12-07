package com.sendi.deliveredrobot.ros.debug;

import static com.sendi.deliveredrobot.ros.constant.ClientConstant.TARGET_POSE;

import com.sendi.deliveredrobot.MyApplication;
import com.sendi.deliveredrobot.helpers.ROSHelper;
import com.sendi.deliveredrobot.room.PointType;
import com.sendi.deliveredrobot.room.dao.DebugDao;
import com.sendi.deliveredrobot.room.database.DataBaseDeliveredRobotMap;
import com.sendi.deliveredrobot.room.entity.Point;
import com.sendi.deliveredrobot.room.entity.PublicArea;
import com.sendi.deliveredrobot.room.entity.SubMap;
import com.sendi.deliveredrobot.ros.ClientManager;
import com.sendi.deliveredrobot.ros.debug.dto.MapResult;
import com.sendi.deliveredrobot.ros.debug.dto.MapResultUtil;
import com.sendi.deliveredrobot.ros.dto.Client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import geometry_msgs.Pose2D;
import navigation_base_msgs.CreateOneWayResponse;
import navigation_base_msgs.TargetPoseResponse;

public class MapTargetPointServiceImpl implements IMapTargetPointService {
    private static final DebugDao dao;
    private static List<Point> temporaryStorage;
    private static List<PublicArea> typeList;
    private static final List<Point> alteredPointList;
    private static int MAX_POINT_ID;

    static {
        alteredPointList = new ArrayList<>(16);
        dao = DataBaseDeliveredRobotMap.Companion.getDatabase(Objects.requireNonNull(MyApplication.Companion.getInstance())).getDebug();
        temporaryStorage = new ArrayList<>(16);
        typeList = dao.queryTypeList();
    }

    // get singleton
    public static MapTargetPointServiceImpl getInstance() {
        return MapTargetPointServiceImpl.MapTargetPointInnerClass.INSTANCE;
    }

    // private inner class
    private static class MapTargetPointInnerClass {
        private final static MapTargetPointServiceImpl INSTANCE = new MapTargetPointServiceImpl();
    }

    /*
    获取目标点图文件
     */
    @Override
    public MapResult getMaps() {
        List<SubMap> res = dao.queryTargetPointMap();
        HashMap<String, Object> data = new HashMap<>();
        data.put("maps", res);
        return MapResultUtil.success(data);
    }

    /*
    删除目标点图文件下的所有目标点
     */
    @Override
    public MapResult deletePointMap(Integer subMapId) {
        List<Point> points = dao.queryPointsBySubMapId(subMapId);
        if (hasBoundRelation(points)) {
            return MapResultUtil.failure("该文件下的目标点有绑定关系，删除失败");
        }
        for (Point point : points) {
            dao.deletePoint(point);
        }
        return MapResultUtil.success(null, "删除成功");
    }

    /*
    获取当前机器人下的所有分类
     */
    @Override
    public MapResult getTypes() {
        typeList = dao.queryTypeList();
        HashMap<String, Object> result = new HashMap<>();
        result.put("typeList", typeList);
        return MapResultUtil.success(result);
    }

    /*
    新增分类，数据直接入库
     */
    @Override
    public MapResult addNewType(String typeName) {
        int res = preCheckType(0, typeName, 2);
        if (res != 1) {
            return handleNameError(res);
        }
        PublicArea area = new PublicArea(getMaxTypeId() + 1, typeName, 1);
        typeList.add(area);
        dao.insertType(area);
        HashMap<String, Object> data = new HashMap<>();
        data.put("typeList", typeList);
        return MapResultUtil.success(data, "成功新增分类");
    }

    /*
    删除分类 连同下属目标点一起删除，操作无法回退
     */
    @Override
    public MapResult deleteType(int typeId) {
        List<Point> contains = new ArrayList<>(16);
        for (Point point : temporaryStorage) {
            if (point.getType() != null) {
                if (point.getType() == typeId) {
                    contains.add(point);
                }
            }
        }
        if (hasBoundRelation(contains)) {
            return MapResultUtil.failure("该分类下的目标点有绑定关系，删除失败");
        }
        for (Point point : contains) {
            dao.deletePoint(point);
        }
        PublicArea temp = null;
        for (PublicArea area : typeList) {
            if (area.getId() == typeId) {
                temp = area;
            }
        }
        typeList.remove(temp);
        dao.deleteType(temp);
        return MapResultUtil.success(null, "成功删除分类以及下属的所有目标点");
    }

    // 根据子图Id获取所有目标点
    @Override
    public MapResult getOriginalPoints(Integer subMapId, int typeId) {
        alteredPointList.clear();
        temporaryStorage = dao.queryPointsBySubMapId(subMapId);
        MAX_POINT_ID = dao.queryMaxId() + 1;
        HashMap<String, Object> data = new HashMap<>();
        data.put("pointList", temporaryStorage);
        return MapResultUtil.success(data);
    }

    // 根据子图Id、分类获取所有目标点
    @Override
    public MapResult getTotalPoints(Integer subMapId, int typeId) {
        if (typeId == -1) {
            HashMap<String, Object> data = new HashMap<>();
            data.put("pointList", temporaryStorage);
            return MapResultUtil.success(data);
        }
        List<Point> current = new ArrayList<>(16);
        for (Point point : temporaryStorage) {
            if (point.getType() != null) {
                if (typeId == point.getType()) {
                    current.add(point);
                }
            }
        }
        HashMap<String, Object> data = new HashMap<>();
        data.put("pointList", current);
        return MapResultUtil.success(data);
    }

    /*
    标记目标点
     */
    @Override
    public MapResult signPoint(Point point, double range) {
        int checkRes = preCheckType(0, point.getName(), 1);
        if (checkRes != 1) {
            return handleNameError(checkRes);
        }
        HashMap<String, Object> data = new HashMap<>();
        switch (point.getType()) {
            case 2:
                if (!Objects.requireNonNull(point.getName()).contains("外")) {
                    return MapResultUtil.failure("标记电梯外部停靠点名字需包含\"外\"");
                }
                CreateOneWayResponse createOneWayResponse = ROSHelper.INSTANCE.getLiftPoint(range);
                if (createOneWayResponse != null) {
                    switch (createOneWayResponse.getState()) {
                        case 1: {
                            Pose2D pose1 = createOneWayResponse.getPose1();
                            Point inside = new Point(
                                    MAX_POINT_ID++,
                                    Objects.requireNonNull(point.getName()).replace("外", "内"),
                                    point.getDirection(),
                                    (float) pose1.getX(),
                                    (float) pose1.getY(),
                                    pose1.getTheta(),
                                    point.getSubMapId(),
                                    PointType.LIFT_INSIDE,
                                    point.getElevator()
                            );
                            Pose2D pose2 = createOneWayResponse.getPose2();
                            Point outside = new Point(
                                    MAX_POINT_ID++,
                                    point.getName(),
                                    point.getDirection(),
                                    (float) pose2.getX(),
                                    (float) pose2.getY(),
                                    pose1.getTheta(),
                                    point.getSubMapId(),
                                    PointType.LIFT_OUTSIDE,
                                    point.getElevator()
                            );
                            temporaryStorage.add(inside);
                            temporaryStorage.add(outside);
                            alteredPointList.add(inside);
                            alteredPointList.add(outside);
                            data.put("inside", inside);
                            data.put("outside", outside);
                            break;
                        }
                        case -1: {
                            return MapResultUtil.failure("标记目标点失败");
                        }
                        case -10: {
                            return MapResultUtil.failure("传感器异常");
                        }
                    }
                }
                break;
            default:
                Client client = new Client(TARGET_POSE, new HashMap<>());
//        RosResult rosResult = ClientManager.sendClientMsg(client);
                TargetPoseResponse response = (TargetPoseResponse) ClientManager.sendClientMsg(client).getResponse();
                int result = response.getResult();
                if (result != 1) {
                    return MapResultUtil.failure("标记目标点失败");
                }
                int id = MAX_POINT_ID++;
                Float x = (float) response.getPose().getX();
                Float y = (float) response.getPose().getY();
                Point res = new Point(id, point.getName(), point.getDirection(), x, y, response.getPose().getTheta(), point.getSubMapId(), point.getType(),null);
                temporaryStorage.add(res);
                alteredPointList.add(res);
                data.put("point", res);
        }

        return MapResultUtil.success(data, "成功标记目标点");
    }

    /*
    修改目标点信息
     */
    @Override
    public MapResult resetPoint(Point point) {
        int res = preCheckType(point.getId(), point.getName(), 1);
        if (res != 1) {
            return handleNameError(res);
        } else { // res == 1 execute
            int index = searchIndex(point, temporaryStorage);
            Point oldPoint = temporaryStorage.get(index);
            Point updatedPoint = updatePointInfo(point, oldPoint);
            temporaryStorage.set(index, updatedPoint);
            if (alteredPointList.contains(oldPoint)) {
                alteredPointList.set(searchIndex(oldPoint, alteredPointList), updatedPoint);
            }
            dao.updatePoint(updatedPoint);
            HashMap<String, Object> data = new HashMap<>();
            data.put("point", updatedPoint);
            return MapResultUtil.success(data, "成功修改目标点信息");
        }
    }

    /*
    删除目标点，直接连数据库信息一起删除
     */
    @Override
    public MapResult deletePoint(Point point) {
        if (!dao.judgeDeletePoint(point.getId()).isEmpty()) {
            return MapResultUtil.failure("删除失败，请先删除绑定关系");
        }
        temporaryStorage.remove(point);
        alteredPointList.remove(point);
        dao.deletePoint(point);
        return MapResultUtil.success(null, "删除成功");
    }

    /*
    退出目标点页面并保存所有修改
     */
    @Override
    public MapResult save() {
        saveDataListToDB();
        alteredPointList.clear();
        temporaryStorage.clear();
        return MapResultUtil.success(null, "保存成功");
    }

    /*
    只退出，不保存
     */
    @Override
    @Deprecated
    public void quit() {
        temporaryStorage.clear();
        typeList.clear();
    }

    /*
    根据分类获取目标点，还有一个默认的约束条件为子图Id即楼层，目前合并成上面那一个方法
     */
    @Override
    @Deprecated
    public MapResult getPointListByType(String type) {
        int typeId = 0;
        for (PublicArea area : typeList) {
            if (type.equals(area.getName())) {
                typeId = area.getId();
            }
        }
        List<Point> result = new ArrayList<>();
        for (Point point : temporaryStorage) { // 从当前楼层的point list中找
            if (point.getType() != null) {
                if (point.getType() == (typeId)) {
                    result.add(point);
                }
            }
        }
        HashMap<String, Object> data = new HashMap<>();
        data.put("pointList", result);
        return MapResultUtil.success(data);
    }

    // ---------------------------------- private function ---------------------------------- //

    /**
     * 新增分类时进行判断 返回 1 - 正常 2 ～ 5 - 异常
     */
    private int preCheckType(int id, String name, int type) {
        if ("".equals(name) || name.trim().length() == 0) {
            return 2;
        }
        if (name.length() > 8) {
            return 3;
        }
//        for (char ch : name.toCharArray()) {
//            if (ch < 0x4E00 || ch > 0x9FA5) {
//                return 4;
//            }
//        }
        return checkRepeatedName(id, name, type); // 5 or 1
    }

    /**
     * 检测重命名
     */
    private int checkRepeatedName(int id, String name, int type) {
        if (type == 1) {
            for (Point point : temporaryStorage) {
                if (id == point.getId()) {
                    continue;
                }
                if (name.equals(point.getName())) {
                    return 5;
                }
            }
        } else {
            for (PublicArea area : typeList) {
                if (name.equals(area.getName())) {
                    return 5;
                }
            }
        }
        return 1;
    }

    /**
     * 处理命名错误结果
     */
    public MapResult handleNameError(int res) {
        switch (res) {
            case 2:
                return MapResultUtil.failure("命名不能全部为空格");
            case 3:
                return MapResultUtil.failure("命名最大长度为8个中文字符");
            case 4:
                return MapResultUtil.failure("命名只允许用中文命名");
            default: // res = 5
                return MapResultUtil.failure("命名已存在，请重新输入");
        }
    }

    /**
     * 获取列表中最大typeId
     */
    public int getMaxTypeId() {
        int max = -1;
        for (PublicArea area : typeList) {
            if (max < area.getId()) {
                max = area.getId();
            }
        }
        return max;
    }

    /**
     * 更新Point
     */
    private static Point updatePointInfo(Point changed, Point old) {
        int id = old.getId();
        Float x = old.getX();
        Float y = old.getY();
        Double w = old.getW();
        String name = changed.getName();
        String direction = changed.getDirection();
        Integer type = changed.getType();
        Integer subMapId = changed.getSubMapId();
        String elevator = changed.getElevator();
        return new Point(id, name, direction, x, y, w, subMapId, type, elevator);
    }

    /**
     * 数据入库
     */
    private static void saveDataListToDB() {
        for (Point point : alteredPointList) {
            dao.insertTargetPoint(point); // 旧的insert失败，只会insert进新的
        }
    }

    /**
     * 查找Point在对应列表的位置
     */
    private static int searchIndex(Point toSearch, List<Point> target) {
        int index = 0;
        for (Point point : target) {
            if (point.getId() == toSearch.getId()) {
                break;
            }
            index++;
        }
        return index;
    }

    /**
     * 判定点的绑定关系
     */
    private static boolean hasBoundRelation(List<Point> points) {
        boolean flag = false;
        for (Point point : points) {
            if (!dao.judgeDeletePoint(point.getId()).isEmpty()) {
                flag = true;
            }
        }
        return flag;
    }
    ///    /**
//     * 根据分类名获取Id
//     */
///    private int getTypeIdByName(String name) {
//        int id = 0;
//        for (PublicArea area : typeList) {
//            if (area.getName().equals(name)) {
//                id = area.getId();
//            }
//        }
//        return id;
//    }
}


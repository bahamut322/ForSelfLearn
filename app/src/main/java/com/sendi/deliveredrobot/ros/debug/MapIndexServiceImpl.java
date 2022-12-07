package com.sendi.deliveredrobot.ros.debug;

import static com.sendi.deliveredrobot.ros.debug.dto.MapTypeEnum.LASER_MAP;
import static com.sendi.deliveredrobot.ros.debug.dto.MapTypeEnum.POINT_MAP;
import static com.sendi.deliveredrobot.ros.debug.dto.MapTypeEnum.ROOT_MAP;
import static com.sendi.deliveredrobot.ros.debug.dto.MapTypeEnum.ROUTE_MAP;

import com.sendi.deliveredrobot.MyApplication;
import com.sendi.deliveredrobot.room.dao.DebugDao;
import com.sendi.deliveredrobot.room.database.DataBaseDeliveredRobotMap;
import com.sendi.deliveredrobot.ros.debug.dto.DebugConstant;
import com.sendi.deliveredrobot.ros.debug.dto.MapResult;
import com.sendi.deliveredrobot.ros.debug.dto.MapResultUtil;
import com.sendi.deliveredrobot.ros.debug.dto.MapTypeEnum;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class MapIndexServiceImpl implements IMapIndexService {
    private final DebugDao dao = DataBaseDeliveredRobotMap.Companion.getDatabase(Objects.requireNonNull(MyApplication.Companion.getInstance())).getDebug();

    @Override
    public MapResult getMapList(MapTypeEnum type) {
        List<String> nameList;
        if (ROOT_MAP.getType() == type.getType()) {
            nameList = dao.queryMapRootList();
        } else if (LASER_MAP.getType() == type.getType()) {
            nameList = dao.queryMapLaserList();
        } else if (ROUTE_MAP.getType() == type.getType()) {
            nameList = dao.queryMapRouteList();
        } else if (POINT_MAP.getType() == type.getType()) {
            nameList = dao.queryMapPointList();
        } /*else if (SINGLE_LANE.getType() == type) {
            // todo
        } else if (SPEED_LIMIT_AREA.getType() == type) {
            // todo
        } else if (VIRTUAL_WALL.getType() == type) {
            // todo
        }*/ else {
            return MapResultUtil.failure(DebugConstant.INVALID_PARAM);
        }
        HashMap<String, Object> data = new HashMap<>();
        data.put("list", nameList);
        return MapResultUtil.success(data);
    }

    public MapResult deleteMapById(MapTypeEnum type, Integer id) {
        if (id == null || id < 0)
            return MapResultUtil.failure(DebugConstant.INVALID_PARAM);
        if (ROOT_MAP.getType() == type.getType()) {
            // todo
        } else if (LASER_MAP.getType() == type.getType()) {
            return deleteLaserMap(id);
        } else if (ROUTE_MAP.getType() == type.getType()) {
            // todo
        } else if (POINT_MAP.getType() == type.getType()) {
            // todo
        } /*else if (SINGLE_LANE.getType() == type) {
            // todo
        } else if (SPEED_LIMIT_AREA.getType() == type) {
            // todo
        } else if (VIRTUAL_WALL.getType() == type) {
            // todo
        }*/ else {
            return MapResultUtil.failure(DebugConstant.INVALID_PARAM);
        }
        return MapResultUtil.success();
    }

    /**
     * 通过文件名删除激光地图
     *
     * @param id
     * @return 如果已经绑定了总图，那么删除失败，返回所有总图名称
     * rootNameList : List<String>
     */
    private MapResult deleteLaserMap(Integer id) {
        List<String> rootNameList = dao.selectLaserMapCountInRelationShipTable(id);
        if (rootNameList != null && !rootNameList.isEmpty()) {
            HashMap<String, Object> data = new HashMap<>();
            data.put("rootNameList", rootNameList);
            return MapResultUtil.failure(data, DebugConstant.IS_BINDING);
        }
        dao.deleteLaserMapById(id);
        return MapResultUtil.success();
    }
}

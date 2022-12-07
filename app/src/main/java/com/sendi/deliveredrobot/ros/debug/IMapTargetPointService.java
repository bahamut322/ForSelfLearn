package com.sendi.deliveredrobot.ros.debug;

import com.sendi.deliveredrobot.room.entity.Point;
import com.sendi.deliveredrobot.ros.debug.dto.MapResult;

public interface IMapTargetPointService {

    /**
     * 获取目标点图文件
     */
    MapResult getMaps();

    /**
     * 删除目标点图
     */
    MapResult deletePointMap(Integer subMapId);

    /**
     * 获取当前酒店的所有分类
     */
    MapResult getTypes(); // done

    /**
     * 新增分类
     *
     * @param typeName 新增分类名
     * @return MapResult.success()
     */
    MapResult addNewType(String typeName); // done

    /**
     * 删除分类
     *
     * @param typeId 删除的分类Id
     * @return 如果有绑定关系，不允许删除。
     */
    MapResult deleteType(int typeId);

    /**
     * 获取某个subMap和分类下的数据库中的所有目标点（查数据库）
     * 当传入typeId = -1时， 查询subMap下的所有目标点
     *
     * @param subMapId 子图ID
     * @return Map<String, Object>，key为固定的字符串'pointList'，value为point列表
     */
    MapResult getOriginalPoints(Integer subMapId, int typeId);

    /**
     * 获取后端列表中的所有目标点（查自己维护的列表）
     */
    MapResult getTotalPoints(Integer subMapId, int typeId);

    /**
     * 根据分类查询当前楼层所有目标点，不需要传subMapId是因为之前已经保存了当前楼层的所有目标点，所以不需要重复传参，目前弃用
     *
     * @param type 分类名
     * @return Map<String, Object>，key为固定的字符串'pointList'，value为point列表
     */
    @Deprecated
    MapResult getPointListByType(String type);

    /**
     * 标记一个新的目标点
     *
     * @param point Point实体类
     * @return MapResult { flag = true/false, msg = "成功"/"失败"}
     */
    MapResult signPoint(Point point, double range);

    /**
     * 重新编辑并保存之后调用，修改一个目标点的相关信息
     *
     * @return MapResult.success()
     */
    MapResult resetPoint(Point point);

    /**
     * 删除某一个目标点，如果存在绑定关系也一并删除，暂时的删除
     *
     * @param point point对象
     * @return MapResult.success()
     */
    MapResult deletePoint(Point point);

    /**
     * 退出目标点的页面并保存所有新增的点
     */
    MapResult save();

    /**
     * 对应 上一步之后的功能，但不进行保存
     */
    @Deprecated
    void quit();
}

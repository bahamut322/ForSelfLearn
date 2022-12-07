package com.sendi.deliveredrobot.ros.debug;

import com.sendi.deliveredrobot.ros.debug.dto.FixOperateEnum;
import com.sendi.deliveredrobot.ros.debug.dto.MapResult;

public interface IMapLaserService {

    /**
     * 新建激光地图
     *
     * @return
     */
    MapResult create();

    /**
     * 开始扫描
     *
     * @param name      激光地图名称
     * @param floorCode 楼层指令
     * @param floorName 楼层名称
     * @return
     */
    MapResult startScan(String name, /*int floorCode,*/ String floorName);

    MapResult startScan(String name);

    /**
     * 停止扫描
     *
     * @return
     */
    MapResult stop();

    /**
     * 保存40帧子图
     *
     * @return
     */
    MapResult saveSubMap();

    /**
     * 重置40帧子图，回到列表页
     *
     * @return
     */
    MapResult resetSubMap();

    /**
     * 清除所有的回环信息，进行地图优化
     *
     * @return
     */
    MapResult clearUpdateMessage();

    /**
     * 显示修正图
     *
     * @return RosPointArrUtil.staticMap : ArrayList<float[]>
     * RosPointArrUtil.idInfo : ArrayList<float[]>
     */
    MapResult showFixMap();

    /**
     * 显示原始图
     *
     * @return RosPointArrUtil.staticMap : ArrayList<float[]>
     * RosPointArrUtil.idInfo : ArrayList<float[]>
     */
    MapResult showOriginalMap();

    /**
     * 修正
     *
     * @param fstMapId 第一张子图序号
     * @param scdMapId 第二张子图序号
     * @return RosPointArrUtil.staticMap : ArrayList<float[]>
     * RosPointArrUtil.updateMap : ArrayList<float[]>
     */
    MapResult chooseFilesToFix(Integer fstMapId, Integer scdMapId);

    /**
     * 修正操作
     *
     * @param type     修正类型 FixOperateEnum
     * @param distance 厘米或者度数
     * @return RosPointArrUtil.updateMap : ArrayList<float[]>
     */
    MapResult fix(FixOperateEnum type, int distance);

    /**
     * 保存修正
     *
     * @param cmd 1: 确认正常
     *            -1：放弃
     * @return
     */
    MapResult saveFix(int cmd);

    /**
     * 退出
     *
     * @return
     */
    MapResult quit();

    /**
     * 显示激光地图
     *
     * @param name 文件名
     * @return RosPointArrUtil.staticMap : ArrayList<float[]>
     */
    MapResult showLaserMap(String name);

    /**
     * 原始图编辑
     *
     * @return
     */
    MapResult updateOriginalFile(int cmd);

    /**
     * 副本编辑
     *
     * @param cmd       1:源文件编辑 2：副文本编辑
     * @param name      激光地图名称
     * @param floorCode 楼层指令
     * @param floorName 楼层名称
     * @return
     */
    MapResult updateCopyFile(int cmd, String name, /*int floorCode,*/ String floorName);

    @Deprecated
    MapResult updateCopyFile(int cmd, String name);

    /**
     * 重定向
     * 这里只会解析实时地图，因为在预览的时候已经获取了staticMap
     *
     * @return RosPointArrUtil.staticMap : ArrayList<float[]>
     * RosPointArrUtil.updateMap : ArrayList<float[]>
     */
    MapResult redirect(int cmd);

    MapResult redirect(int cmd, String path);

    /**
     * 继续扫描
     *
     * @return
     */
    MapResult continueScan();


    /**
     * 继续修正
     *
     * @return
     */
    MapResult continueFix();

    /**
     * 导入
     *
     * @return
     */
    MapResult importLaserMap(String name);

    /**
     * 导出
     *
     * @return targetName:String
     */
    MapResult exportLaserMap(String name);


    /**
     * 发送激光建图指令
     *
     * @return
     */
    MapResult sendLaserMapManagerMsg(int cmd);

}

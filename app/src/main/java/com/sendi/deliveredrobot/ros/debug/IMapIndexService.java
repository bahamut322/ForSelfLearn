package com.sendi.deliveredrobot.ros.debug;

import com.sendi.deliveredrobot.ros.debug.dto.MapResult;
import com.sendi.deliveredrobot.ros.debug.dto.MapTypeEnum;

/**
 * 调试页面通用接口
 */
public interface IMapIndexService {
    MapResult getMapList(MapTypeEnum type);

    MapResult deleteMapById(MapTypeEnum type, Integer id);

}

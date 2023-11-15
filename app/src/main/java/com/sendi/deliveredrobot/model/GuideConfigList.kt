package com.sendi.deliveredrobot.model

/**
 * @Author Swn
 * @Data 2023/11/13
 * @describe 获取引领配置实体类
 */
data  class GuideConfigList (
    var mapTimeStamp : Long? = 0,
    var mapName : String? = "",
    var pointName : String? = "",
    var guidePicUrl : String? ="",
    var pointTimeStamp : Long? = 0
)
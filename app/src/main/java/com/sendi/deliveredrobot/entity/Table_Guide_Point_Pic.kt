package com.sendi.deliveredrobot.entity

import org.litepal.crud.LitePalSupport

/**
 * @Author Swn
 * @Data 2023/11/13
 * @describe 引领点的图片
 */
data class Table_Guide_Point_Pic(
    var mapTimeStamp : Long? = 0,
    var mapName : String? = "",
    var pointName : String? = "",
    var guidePicUrl : String? ="",
    var pointTimeStamp : Long? = 0
): LitePalSupport()

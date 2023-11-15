package com.sendi.deliveredrobot.entity

import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.R
import org.litepal.crud.LitePalSupport

/**
 * @Author Swn
 * @Data 2023/11/13
 * @describe 引领点的图片
 */
data class GuidePointPicDB(
    var mapTimeStamp : Long? = 0,
    var mapName : String? = "",
    var pointName : String? = "",
    var guidePicUrl : String? ="",
    var pointTimeStamp : Long? = 0
): LitePalSupport()

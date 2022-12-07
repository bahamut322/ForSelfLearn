package com.sendi.deliveredrobot.model

import com.sendi.deliveredrobot.room.entity.Point
import com.sendi.deliveredrobot.room.entity.RouteMap
import com.sendi.deliveredrobot.room.entity.SubMap
import java.util.*

/**
 * @describe 总图绑定关系
 */
class FoldSelMapModel(
    var id: Int = 0,
    var name: String = "",
    /** 父级的下标，如无父级则为-1*/
    var parentIndex: Int = -1,
    var mSelect: Boolean = false,
    /** 是否折叠 ture:折叠  false:展开 */
    var mColl: Boolean = true,
    /** 是否为多选 true:多选  false:单选 */
    var mMultipleSelect: Boolean = false,
    var mSubMenu: ArrayList<FoldSelMapModel> = ArrayList<FoldSelMapModel>(),
    var index: Int = -1,
    var selectRouteId : Int = -1,
    var selectRouteName : String = "",
    /** 是否为全选状态 */
    var isAllSelect : Boolean = false,
)

package com.sendi.deliveredrobot.room

/**
 *   @author: heky
 *   @date: 2021/8/19 18:38
 *   @describe: 点的类型
 */
object PointType {
    const val ROOM = 100            //客房
    const val LIFT_INSIDE = 1       //电梯内部停靠点
    const val LIFT_OUTSIDE = 2      //电梯外部停靠点
    const val CHARGE_POINT = 3      //充电桩点
    const val CHARGE_NEAR_POINT = 4 //充电桩自动对接开始点
    const val CHARGE_FAR_POINT = 5  //充电桩远端停靠点
}
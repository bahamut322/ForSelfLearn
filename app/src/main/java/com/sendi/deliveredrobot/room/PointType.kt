package com.sendi.deliveredrobot.room

/**
 *   @author: heky
 *   @date: 2021/8/19 18:38
 *   @describe: 点的类型
 */
object PointType {
    const val ROOM = 2100            //客房
    const val LIFT_INSIDE = 2001       //电梯内部停靠点
    const val LIFT_OUTSIDE = 2002      //电梯外部停靠点
    const val CHARGE_POINT = 2003      //充电桩点
    const val READY_POINT = 2004 //待命点
    const val USHER_POINT = 2005  //迎宾点
    const val LIFT_AXIS = 2006  //电梯锚点
}
package com.sendi.deliveredrobot.helpers

/**
 * @author heky
 * @date 2022-08-11
 * @description 里程计帮助类
 */
object RobotMileageHelper {
    private var accumulatedRobotMile: Double = 0.0

    fun robotMilePlus(double: Double){
        accumulatedRobotMile += double
    }

    fun resetRobotMileage(){
        accumulatedRobotMile = 0.0
    }

    fun robotMileage(): Int{
        return accumulatedRobotMile.toInt()
    }
}
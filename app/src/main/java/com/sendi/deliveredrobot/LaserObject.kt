package com.sendi.deliveredrobot

import androidx.lifecycle.MutableLiveData
import geometry_msgs.Point32
import geometry_msgs.Pose2D

/**
 *   @author: heky
 *   @date: 2021/9/17 15:09
 *   @describe: 激光建图静态类
 */
object LaserObject {
    var frameId: Int? = -1   //帧数
    var status: Int? = -1    //1：绿色 2：红色
    var robotPose: Pose2D? = null //机器人位置
//    var pauseCheckPoints: List<Point32>? = null //40帧点云
//    val livePoints = MutableLiveData<List<Point32>>() //实时点云
    var pauseCheckPoints: IntArray? = null //40帧点云
    val livePoints = MutableLiveData<IntArray>() //实时点云
    val routePoints = MutableLiveData<Pose2D>() //实时路径点
    val tempObstacle = MutableLiveData<List<Point32>>() //实时限速区、虚拟墙路径点云
    val liveRobotPose = MutableLiveData<Pose2D>() //实时机器人位置
    /**
     * @describe 清除缓存
     */
    fun clear(){
        frameId = -1
        status = -1
        robotPose = null
        pauseCheckPoints = null
        livePoints.value = null
        tempObstacle.value = null
        liveRobotPose.value = null
    }
}
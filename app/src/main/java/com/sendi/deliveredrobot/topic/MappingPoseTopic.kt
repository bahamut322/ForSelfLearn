package com.sendi.deliveredrobot.topic

import com.sendi.deliveredrobot.LaserObject
import com.sendi.deliveredrobot.ros.dto.RosResult
import com.sendi.deliveredrobot.utils.LogUtil
import geometry_msgs.Pose2D
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import sensor_msgs.PointCloud

/**
 * @author heky
 * @date 2022-09-23
 * @description 重定位时的位置以及方向
 */
object MappingPoseTopic {
    private val mainScope = MainScope()
    private val mutex = Mutex()

    fun handle(rosResult: RosResult<*>) {
        mainScope.launch(Dispatchers.Default) {
            mutex.withLock {
                val pose2D = rosResult.response as Pose2D?
                if (pose2D != null) {
                    with(LaserObject) {
                        withContext(Dispatchers.Main) {
                            liveRobotPose.value = pose2D
                        }
                    }
                }
            }
        }
    }
}
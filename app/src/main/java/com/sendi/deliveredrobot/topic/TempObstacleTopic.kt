package com.sendi.deliveredrobot.topic

import com.sendi.deliveredrobot.LaserObject
import com.sendi.deliveredrobot.ros.dto.RosResult
import com.sendi.deliveredrobot.utils.LogUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import sensor_msgs.PointCloud

object TempObstacleTopic {
    private val mainScope = MainScope()
    private val mutex = Mutex()

    fun handle(rosResult: RosResult<*>) {
        mainScope.launch(Dispatchers.Default) {
            mutex.withLock {
                val pointCloud = rosResult.response as PointCloud
                val mPointCloud = pointCloud.points
                if (mPointCloud != null) {
                    with(LaserObject) {
                        LogUtil.d("【TOPIC】:TEMP_OBSTACLE")
                        withContext(Dispatchers.Main) {
                            tempObstacle.value = mPointCloud
                        }
                    }
                }
            }
        }
    }
}
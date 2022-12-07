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

object RoutePoseInfoTopic {
    private val mainScope = MainScope()
    private val mutex = Mutex()

    fun handle(rosResult: RosResult<*>?) {
        LogUtil.d("收到路径点数据")
        mainScope.launch(Dispatchers.Default) {
            mutex.withLock {
                val mPose2D = rosResult?.response as Pose2D
                withContext(Dispatchers.Main) {
                    LaserObject.routePoints.value = mPose2D
                }
            }
        }
    }
}
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
import map_msgs.Sub_map_info

object SubMapInfoTopic {
    private val mainScope = MainScope()
    private val mutex = Mutex()

    fun handle(rosResult: RosResult<*>?) {
        // 显示实时激光地图 - hard了一批,ma de
        // 拿点
        mainScope.launch(Dispatchers.Default) {
            mutex.withLock {
                val subMapInfo = rosResult?.response as Sub_map_info
                // val points = subMapInfo.subMap.points
                val points = subMapInfo.subMap.data
                val frameId = subMapInfo.id
                val status = subMapInfo.status
                val robotPose = subMapInfo.robotPose
                if (points != null) {
                    with(LaserObject) {
                        this.frameId = frameId
                        this.status = status
                        LogUtil.d("【TOPIC】:status:${status}")
                        this.robotPose = robotPose
                        LogUtil.d("【TOPIC】:frameId:${frameId}")
                        LogUtil.d("【TOPIC】:robotPose:${robotPose}")
                        LogUtil.d("【TOPIC】:points:${points.contentToString()}")
                        withContext(Dispatchers.Main) {
                            livePoints.value = points
                        }
                    }
                }
            }
        }
    }
}
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
import map_msgs.Current_global_laser

object CurrentLaserTopic {
    private val mainScope = MainScope()
    private val mutex = Mutex()

    fun handle(rosResult: RosResult<*>?) {
        mainScope.launch(Dispatchers.Default) {
            mutex.withLock {
                val currentGlobalLaser = rosResult?.response as Current_global_laser
                val mPointCloud = currentGlobalLaser.globalLaser.data
                if (mPointCloud != null) {
                    with(LaserObject) {
                        LogUtil.d("【TOPIC】:GLOBAL_LASER")
                        withContext(Dispatchers.Main) {
                            livePoints.value = mPointCloud
                        }
                    }
                }
            }
        }
    }
}
package com.sendi.deliveredrobot.topic

import android.content.Intent
import com.sendi.deliveredrobot.*
import com.sendi.deliveredrobot.ACTION_NAVIGATE
import com.sendi.deliveredrobot.NAVIGATE_ID
import com.sendi.deliveredrobot.ros.SubManager
import com.sendi.deliveredrobot.ros.constant.ClientConstant
import com.sendi.deliveredrobot.ros.dto.RosResult
import com.sendi.deliveredrobot.utils.LogUtil
import map_msgs.Pause_check

object PauseCheckTopic {
    fun handle(rosResult: RosResult<*>?) {
        // 显示40帧子图
        // 1.取消订阅SUB_MAP_INFO
        SubManager.unsub(ClientConstant.SUB_MAP_INFO)
        // 2.显示
        val pauseCheck = rosResult?.response as Pause_check
        // LaserObject.pauseCheckPoints = pauseCheck.subMap.points
        LaserObject.pauseCheckPoints = pauseCheck.subMap.data
        MyApplication.instance!!.sendBroadcast(Intent().apply {
            action = ACTION_NAVIGATE
            putExtra(NAVIGATE_ID, R.id.confirmSubMapFragment)
        })
        LogUtil.d("【TOPIC】:PAUSE_CHECK")
//        }
    }
}
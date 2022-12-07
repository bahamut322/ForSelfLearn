package com.sendi.deliveredrobot.topic

import com.sendi.deliveredrobot.helpers.LiftHelper
import com.sendi.deliveredrobot.navigationtask.BillManager
import com.sendi.deliveredrobot.ros.dto.RosResult

object NearIndoorLiftTopic {
    fun handle(rosResult: RosResult<*>) {
        //释放开门
        LiftHelper.releaseLiftDoor(BillManager.currentBill()?.currentTask()?.taskModel?.elevator?:"")
    }
}
package com.sendi.deliveredrobot.navigationtask.task

import android.content.Intent
import com.sendi.deliveredrobot.ACTION_NAVIGATE
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.NAVIGATE_ID
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.helpers.ROSHelper
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.AbstractTask
import com.sendi.deliveredrobot.service.TaskDto
import com.sendi.deliveredrobot.service.TaskStageEnum
import com.sendi.deliveredrobot.utils.LogUtil
import com.sendi.deliveredrobot.utils.ToastUtil

/**
 *   @author: heky
 *   @date: 2021/8/24 16:06
 *   @describe: 导航到充电重置点
 */
class NavToFarPointTask(taskModel: TaskModel) : AbstractTask(taskModel) {
    override fun configEnum(): TaskStageEnum {
        return TaskStageEnum.NavToFarPointTask
    }

    override suspend fun beforeReportData(taskDto: TaskDto) {
        super.beforeReportData(taskDto)
    }

    override suspend fun execute() {
        //step1设置速度
        ROSHelper.setSpeed("${basicSettingViewModel.value.basicConfig.guideSpeed}")
//        val dao = DataBaseDeliveredRobotMap.getDatabase(MyApplication.instance!!).getDao()
//        val queryChargeFarPoint = dao.queryChargeFarPoint()
        val queryChargePoint = dao.queryChargePoint()
        if (queryChargePoint == null) {
            ToastUtil.show(MyApplication.instance!!.getString(R.string.db_query_charge_point_fail))
            LogUtil.e(MyApplication.instance!!.getString(R.string.db_query_charge_point_fail))
            return
        }
//        taskModel = TaskModel(location = queryChargePoint.apply {
//            pointName = MyApplication.instance!!.getString(R.string.fix_point)
//        })
        taskModel?.apply {
            location = queryChargePoint.apply {
                pointName = MyApplication.instance!!.getString(R.string.fix_point)
            }
        }
//        ROSHelper.navigateTo(queryChargeFarPoint)
        ROSHelper.advanceMoveTo(cmd = 2, location = queryChargePoint)
        MyApplication.instance!!.sendBroadcast(Intent().apply {
            action = ACTION_NAVIGATE
            putExtra(NAVIGATE_ID, R.id.guidingFragment)
        })
    }
}
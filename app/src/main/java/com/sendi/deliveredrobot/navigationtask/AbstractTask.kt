package com.sendi.deliveredrobot.navigationtask

import androidx.lifecycle.ViewModelLazy
import com.sendi.deliveredrobot.BuildConfig
import com.sendi.deliveredrobot.MainActivity
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.interfaces.ITask
import com.sendi.deliveredrobot.helpers.ReportDataHelper
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.task.GuideArriveTask
import com.sendi.deliveredrobot.room.database.DataBaseDeliveredRobotMap
import com.sendi.deliveredrobot.service.TaskDto
import com.sendi.deliveredrobot.service.TaskStageEnum
import com.sendi.deliveredrobot.utils.LogUtil
import com.sendi.deliveredrobot.viewmodel.BasicSettingViewModel
import com.sendi.deliveredrobot.viewmodel.SendPlaceBin1ViewModel
import com.sendi.deliveredrobot.viewmodel.SendPlaceBin2ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * describe：导航任务
 */
abstract class AbstractTask(
    var taskModel: TaskModel? = null
): ITask {
    companion object{
        val viewModelBin1 = ViewModelLazy(
            SendPlaceBin1ViewModel::class,
            { MainActivity.instance.viewModelStore },
            { MainActivity.instance.defaultViewModelProviderFactory}
        )
        val viewModelBin2 = ViewModelLazy(
            SendPlaceBin2ViewModel::class,
            { MainActivity.instance.viewModelStore },
            { MainActivity.instance.defaultViewModelProviderFactory}
        )
        val basicSettingViewModel = ViewModelLazy(
            BasicSettingViewModel::class,
            { MainActivity.instance.viewModelStore },
            { MainActivity.instance.defaultViewModelProviderFactory}
        )
        val mainScope = MainScope()
        val dao = DataBaseDeliveredRobotMap.getDatabase(MyApplication.instance!!).getDao()
    }
    val taskDto = TaskDto().apply { status = 1 }
    lateinit var enum: TaskStageEnum
    fun taskExecute(){
        enum = configEnum()
        LogUtil.i(javaClass.name)
        mainScope.launch(Dispatchers.Default){
            beforeReportData(taskDto)
            if (BuildConfig.IS_REPORT) {
//                MainScope().launch {
                reportData()
//                }
            }
            execute()
        }
    }

    open suspend fun beforeReportData(taskDto: TaskDto){}

    open fun reportTaskDto(){
        ReportDataHelper.reportTaskDto(taskModel, enum, taskDto)
    }

    /**
     * @describe 上报信息
     */
    private fun reportData() {
        reportTaskDto()
    }

    fun taskModel(): TaskModel?{
        return taskModel
    }
}
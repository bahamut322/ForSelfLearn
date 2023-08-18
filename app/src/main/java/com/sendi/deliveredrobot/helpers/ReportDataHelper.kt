package com.sendi.deliveredrobot.helpers

import android.util.Log
import com.sendi.deliveredrobot.*
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.AbstractTask
import com.sendi.deliveredrobot.ros.RosPointArrUtil
import com.sendi.deliveredrobot.service.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.collections.ArrayList

/**
 * @author heky
 * @date 2022-06-13
 * @description 上报任务
 */
object ReportDataHelper {
    private val mutex = Mutex()
    private val mainScope = MainScope()

    fun reportTaskDto(taskModel: TaskModel?, enum: TaskStageEnum, taskDto: TaskDto){
        //构建实体
        mainScope.launch(Dispatchers.Default) {
            mutex.withLock {
                val data = generateData(taskModel, enum, taskDto)
                CloudMqttService.publish(data.toString())
                //上报则重置
                if(taskDto.mileage != 0){
                    RobotMileageHelper.resetRobotMileage()
                }
            }
        }
    }

    fun reportTaskStartDto(type:Int, taskModel: TaskModel){
        mainScope.launch(Dispatchers.Default) {
            mutex.withLock {
                val data = generateStartDate(type, taskModel)
                CloudMqttService.publish(data.toString())
            }
        }
    }

    /**
     * @describe 机器人每完成一个任务节点上报位置信息所需实体
     */
    private fun generateData(taskModel: TaskModel?, enum: TaskStageEnum, taskDto: TaskDto): RobotPoseStageData {
        val robotPoseStageData = RobotPoseStageData().apply {
            taskList = ArrayList<TaskDto>()
            type = "taskPoseData"
        }
        with(taskDto) {
            taskId = taskModel?.taskId?:""
            target = taskModel?.location?.pointName?:""
            val pose = ROSHelper.getPose()
            val poseArray = floatArrayOf(pose?.x?.toFloat()?:-1f, pose?.y?.toFloat()?:-1f, pose?.theta?.toFloat()?:-1f)
            robotPose = poseArray
            val tempArrayList = ArrayList<Float>()
            if (ROSHelper.getNowLaser()) {
                for (floats in RosPointArrUtil.updateMap) {
                    floats.map {
                        tempArrayList.add(it)
                    }
                }
            }
            updateMap = tempArrayList.toFloatArray()
            taskStage = enum.code
            time = System.currentTimeMillis()
            Log.d("TAG", "任务上报发送的时间戳: $time")
            endTarget = taskModel?.endTarget?:""
            gate = when(taskModel?.location?.binMark){
                AbstractTask.viewModelBin1.value.binMarkBin1 -> 1
                AbstractTask.viewModelBin2.value.binMarkBin2 -> 2
                else -> -1
            }
        }
        robotPoseStageData.taskList.add(taskDto)
        return robotPoseStageData
    }

    /**
     * @describe 生成开始任务的实体
     */
    private fun generateStartDate(type:Int, taskModel: TaskModel):RobotPoseStageData{
        val robotPoseStageData = RobotPoseStageData()
        robotPoseStageData.taskList = java.util.ArrayList<TaskDto>()
        robotPoseStageData.type = "taskPoseData"
        val pose = ROSHelper.getPose()
        val poseArray = floatArrayOf(pose?.x?.toFloat()?:-1f,pose?.y?.toFloat()?:-1f,pose?.theta?.toFloat()?:-1f)
        val tempUpdateMap:FloatArray
        val tempArrayList = java.util.ArrayList<Float>()
        if (ROSHelper.getNowLaser()) {
            for (floats in RosPointArrUtil.updateMap) {
                floats.map {
                    tempArrayList.add(it)
                }
            }
        }
        tempUpdateMap = tempArrayList.toFloatArray()
        when(type){
            TYPE_GUIDE -> {
                val taskDto = TaskDto()
                with(taskDto){
                    taskId = taskModel.taskId
                    target = taskModel.location?.pointName?:""
                    robotPose = poseArray
                    updateMap = tempUpdateMap
                    taskStage = TaskStageEnum.ALLStartTask.code
                    status = 1
                    time = System.currentTimeMillis()
                    gate = when(taskModel.location?.binMark){
                        AbstractTask.viewModelBin1.value.binMarkBin1 -> 1
                        AbstractTask.viewModelBin2.value.binMarkBin2 -> 2
                        else -> -1
                    }
                    endTarget = taskModel.endTarget
                }
                robotPoseStageData.taskList.add(taskDto)
            }
            TYPE_SEND -> {
                if(taskModel.location2 != null){
                    //双任务
//                    val date = Date()
                    val taskDto1 = TaskDto()
                    with(taskDto1){
                        taskId = taskModel.taskId
                        target = taskModel.location?.pointName?:""
                        robotPose = poseArray
                        updateMap = tempUpdateMap
                        taskStage = TaskStageEnum.ALLStartTask.code
                        status = 1
                        time = System.currentTimeMillis()
                        gate = when(taskModel.location?.binMark){
                            AbstractTask.viewModelBin1.value.binMarkBin1 -> 1
                            AbstractTask.viewModelBin2.value.binMarkBin2 -> 2
                            else -> -1
                        }
                        endTarget = taskModel.endTarget
                    }
//                    val tempTaskId = TaskIdGenerator.getInstance().generateTaskId(TaskTypeEnum.DELIVERY, DoorEnum.SECOND_DOOR, date)
//                    TaskQueue.nextTaskId = tempTaskId
                    robotPoseStageData.taskList.add(taskDto1)
                }else{
                    //单任务
                    val taskDto = TaskDto()
                    with(taskDto){
                        taskId = taskModel.taskId
                        status = 1
                        target = taskModel.location?.pointName?:""
                        robotPose = poseArray
                        updateMap = tempUpdateMap
                        taskStage = TaskStageEnum.ALLStartTask.code
                        time = System.currentTimeMillis()
                        gate = when(taskModel.location?.binMark){
                            AbstractTask.viewModelBin1.value.binMarkBin1 -> 1
                            AbstractTask.viewModelBin2.value.binMarkBin2 -> 2
                            else -> -1
                        }
                        endTarget = taskModel.endTarget
                    }
                    robotPoseStageData.taskList.add(taskDto)
                }
            }
            TYPE_REMOTE_ORDER_TAKE, TYPE_REMOTE_ORDER_SEND -> {
                val taskDto = TaskDto()
                with(taskDto){
                    taskId = taskModel.taskId
                    target = when (taskModel.remoteOrderType) {
                        PAGE_TYPE_PUT -> taskModel.remoteOrderModel?.from?.pointName?:""
                        PAGE_TYPE_TAKE -> taskModel.remoteOrderModel?.to?.pointName?:""
                        else -> ""
                    }
                    robotPose = poseArray
                    status = 1
                    updateMap = tempUpdateMap
                    taskStage = TaskStageEnum.ALLStartTask.code
                    time = System.currentTimeMillis()
                    gate = when(taskModel.remoteOrderModel?.from?.binMark){
                        AbstractTask.viewModelBin1.value.binMarkBin1 -> 1
                        AbstractTask.viewModelBin2.value.binMarkBin2 -> 2
                        else -> -1
                    }
                    endTarget = taskModel.endTarget
                }
                robotPoseStageData.taskList.add(taskDto)
            }
            TYPE_WELCOME -> {
                val taskDto = TaskDto()
                with(taskDto){
                    taskId = taskModel.taskId
                    target = taskModel.location?.pointName?:""
                    robotPose = poseArray
                    updateMap = tempUpdateMap
                    taskStage = TaskStageEnum.ALLStartTask.code
                    status = 1
                    time = System.currentTimeMillis()
                    gate = when(taskModel.location?.binMark){
                        AbstractTask.viewModelBin1.value.binMarkBin1 -> 1
                        AbstractTask.viewModelBin2.value.binMarkBin2 -> 2
                        else -> -1
                    }
                    endTarget = taskModel.endTarget
                }
                robotPoseStageData.taskList.add(taskDto)
            }
            TYPE_EXPLAN ->{
                val taskDto = TaskDto()
                with(taskDto){
                    taskId = taskModel.taskId
                    target = taskModel.location?.pointName?:""
                    robotPose = poseArray
                    updateMap = tempUpdateMap
                    taskStage = TaskStageEnum.ALLStartTask.code
                    status = 1
                    time = System.currentTimeMillis()
                    gate = when(taskModel.location?.binMark){
                        AbstractTask.viewModelBin1.value.binMarkBin1 -> 1
                        AbstractTask.viewModelBin2.value.binMarkBin2 -> 2
                        else -> -1
                    }
                    endTarget = taskModel.endTarget
                }
                robotPoseStageData.taskList.add(taskDto)
            }
        }
        return robotPoseStageData
    }
}
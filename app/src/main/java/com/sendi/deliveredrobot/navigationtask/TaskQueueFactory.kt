package com.sendi.deliveredrobot.navigationtask

import androidx.lifecycle.ViewModelLazy
import com.sendi.deliveredrobot.*
import com.sendi.deliveredrobot.helpers.DialogHelper
import com.sendi.deliveredrobot.helpers.IdleGateDataHelper
import com.sendi.deliveredrobot.helpers.ROSHelper
import com.sendi.deliveredrobot.model.RemoteOrderModel
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.task.*
import com.sendi.deliveredrobot.room.database.DataBaseDeliveredRobotMap
import com.sendi.deliveredrobot.room.entity.BasicConfig
import com.sendi.deliveredrobot.room.entity.QueryPointEntity
import com.sendi.deliveredrobot.service.DoorEnum
import com.sendi.deliveredrobot.service.TaskIdGenerator
import com.sendi.deliveredrobot.service.TaskTypeEnum
import com.sendi.deliveredrobot.viewmodel.SendPlaceBin1ViewModel
import com.sendi.deliveredrobot.viewmodel.SendPlaceBin2ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.*


/**
 * @describe 任务工厂
 */
object TaskQueueFactory {
//    // 选择舱体
//    val viewModelBin1 = ViewModelLazy(
//        SendPlaceBin1ViewModel::class,
//        { MainActivity.instance.viewModelStore },
//        { MainActivity.instance.defaultViewModelProviderFactory}
//    )
//    val viewModelBin2 = ViewModelLazy(
//        SendPlaceBin2ViewModel::class,
//        { MainActivity.instance.viewModelStore },
//        { MainActivity.instance.defaultViewModelProviderFactory}
//    )
//    lateinit var basicConfig: BasicConfig
//    val dao = DataBaseDeliveredRobotMap.getDatabase(MyApplication.instance!!)
//        .getDao()
//    val mutex = Mutex()
//
//    /**
//     * @description 创建小程序任务链
//     */
//    suspend fun createTask(remoteOrderModel: RemoteOrderModel): Boolean{
//        mutex.withLock {
//            DialogHelper.loadingDialog.show()
//            if (!IdleGateDataHelper.minusCount()){
//                DialogHelper.loadingDialog.dismiss()
//                return false
//            }
//            val bin1 = with(viewModelBin1.value){
//                val result = previousTaskFinished /*&& previousRemoteOrderPutFinished*/ && previousRemoteOrderSendFinished
//                if(result){
//                    // 1仓有空
//                    with(remoteOrderModel){
//                        from.apply {
//                            binMark = binMarkBin1
//                        }
//                        to.apply {
//                            binMark = binMarkBin1
//                        }
//                        viewModelBin1.value.remoteOrderModel = this
//                        if (RobotStatus.currentStatus == TYPE_GO_BACK || RobotStatus.currentStatus == TYPE_CHARGING || RobotStatus.currentStatus == TYPE_IDLE) {
//                            if(RobotStatus.docking){//重置重试次数
//                                RobotStatus.retryDockTimes = 0
//                                RobotStatus.docking = false
//                                ROSHelper.controlDock(RobotCommand.CMD_STOP)
//                            }
//                            // step 0：清空队列
//                            TaskQueues.clearQueue()
//                            //step:0.5:
//                            createRemoteOrderTask(
//                                this,
//                                taskType = when(taskType){
//                                    "送物" -> TYPE_REMOTE_ORDER_SEND
//                                    "取物" -> TYPE_REMOTE_ORDER_TAKE
//                                    else -> TYPE_REMOTE_ORDER_SEND
//                                }
//                            )
//                            withContext(Dispatchers.Default){
//                                ROSHelper.manageRobotUntilDone(RobotCommand.MANAGE_STATUS_STOP)
//                            }
//                        }else{
//                            createRemoteOrderTask(
//                                this,
//                                taskType = when(taskType){
//                                    "送物" -> TYPE_REMOTE_ORDER_SEND
//                                    "取物" -> TYPE_REMOTE_ORDER_TAKE
//                                    else -> TYPE_REMOTE_ORDER_SEND
//                                }
//                            )
//                        }
//
//                    }
//                }
//                result
//            }
//            var bin2 = false
//            if(!bin1){
//                bin2 = with(viewModelBin2.value){
//                    val result = previousTaskFinished /*&& previousRemoteOrderPutFinished*/ && previousRemoteOrderSendFinished
//                    if(result){
//                        // 2仓有空
//                        with(remoteOrderModel){
//                            from.apply {
//                                binMark = binMarkBin2
//                            }
//                            to.apply {
//                                binMark = binMarkBin2
//                            }
//                            viewModelBin2.value.remoteOrderModel = this
//                            if (RobotStatus.currentStatus == TYPE_GO_BACK || RobotStatus.currentStatus == TYPE_CHARGING || RobotStatus.currentStatus == TYPE_IDLE) {
//                                if(RobotStatus.docking){//重置重试次数
//                                    RobotStatus.retryDockTimes = 0
//                                    RobotStatus.docking = false
//                                    ROSHelper.controlDock(RobotCommand.CMD_STOP)
//                                }
//                                // 如果是返回过程，则打断，并执行下一个任务
//                                TaskQueues.clearQueue()
//                                createRemoteOrderTask(
//                                    this,
//                                    taskType = when(taskType){
//                                        "送物" -> TYPE_REMOTE_ORDER_SEND
//                                        "取物" -> TYPE_REMOTE_ORDER_TAKE
//                                        else -> TYPE_REMOTE_ORDER_SEND
//                                    }
//                                )
//                                withContext(Dispatchers.Default){
//                                    ROSHelper.manageRobotUntilDone(RobotCommand.MANAGE_STATUS_STOP)
//                                }
//                            }else{
//                                // 如果不是返回过程，则创建小程序任务链添加到队列尾部，等待执行
//                                createRemoteOrderTask(
//                                    this,
//                                    taskType = when(taskType){
//                                        "送物" -> TYPE_REMOTE_ORDER_SEND
//                                        "取物" -> TYPE_REMOTE_ORDER_TAKE
//                                        else -> TYPE_REMOTE_ORDER_SEND
//                                    }
//                                )
//                            }
//                        }
//                    }
//                    result
//                }
//            }
//            DialogHelper.loadingDialog.dismiss()
//            return bin1 || bin2
//        }
//    }
//
//    /**
//     * @describe 创建线下任务链
//     */
//    suspend fun createTask(
//        location1: QueryPointEntity,
//        location2: QueryPointEntity? = null,
//        taskType: Int
//    ): LinkedList<Task> {
//        mutex.withLock {
//            DialogHelper.loadingDialog.show()
////        withContext(Dispatchers.Default) {
//            val tempQueue = createQueue(location1, location2, taskType)
//            DialogHelper.loadingDialog.dismiss()
//            return tempQueue
//        }
//    }
//
//    /**
//     * @description 创建小程序任务链
//     */
//    private fun createRemoteOrderTask(
//        remoteOrderModel: RemoteOrderModel,
//        taskType: Int
//    ){
//        when (taskType) {
//            TYPE_REMOTE_ORDER_SEND,
//            TYPE_REMOTE_ORDER_TAKE -> {
//                RobotStatus.lowPowerBacking = false
//                TaskQueues.addLastEndTarget(remoteOrderModel.from.pointName?:"")
//                TaskQueues.addLastEndTarget(remoteOrderModel.to.pointName?:"")
//                TaskQueues.remainPlus()
//                createRemoteOrderTaskQueue(remoteOrderModel, taskType)
//            }
//        }
//    }
//
//    private fun createQueue(
//        location1: QueryPointEntity,
//        location2: QueryPointEntity? = null,
//        taskType: Int
//    ): LinkedList<Task> {
//        // step 2：判断任务类型（送物/引领）
//        when (taskType) {
//            TYPE_GUIDE -> {
//                RobotStatus.lowPowerBacking = false
//                return createGuideTaskQueue(location1)
//            }
//            TYPE_SEND -> {
//                RobotStatus.lowPowerBacking = false
//                return if (location2 != null) {
//                    //双任务
//                    createSendDoubleTaskQueue(location1, location2)
//                } else {
//                    // 单任务
//                    createSendSingleTaskQueue(location1)
//                }
//            }
//            TYPE_GO_BACK -> {
//                //生成当前任务ID
//                return createGoBackTaskQueue()
//            }
//        }
//        return LinkedList<Task>()
//    }
//
//
//    /**
//     * @describe 创建返回任务队列
//     */
//    private fun createGoBackTaskQueue(): LinkedList<Task> {
//        val tempTaskId = TaskQueues.currentTask?.taskModel?.taskId?:""
//        val tempQueue = LinkedList<Task>()
//        tempQueue.apply {
//            //step 13: 回到充电桩
//            add(GoBackTask(
//                TaskModel(
//                    location = RobotStatus.originalLocation!!,
//                    endTarget = RobotStatus.originalLocation?.pointName?:"",
//                    taskId = tempTaskId
//                )
//            ))
//            add(
//                JudgeFloorTask(
//                    TaskModel(
//                        location = RobotStatus.originalLocation!!,
//                        endTarget = RobotStatus.originalLocation?.pointName?:"",
//                        taskId = tempTaskId
//                    ),
//                    TYPE_GO_BACK
//                )
//            )
//            add(
//                AdvanceGuidingTask(
//                    cmd = 1,
//                    taskModel = TaskModel(
//                        location = RobotStatus.originalLocation,
//                        endTarget = RobotStatus.originalLocation?.pointName?:"",
//                        taskId = tempTaskId
//                    ),
//                    navigateId = R.id.goBackFragment
//                )
//            )
//            add(GoBackFinishTask(
//                TaskModel(
//                    location = RobotStatus.originalLocation,
//                    endTarget = RobotStatus.originalLocation?.pointName?:"",
//                    taskId = tempTaskId
//                )
//            ))
//            add(BeginDockTask(
//                TaskModel(
//                    location = RobotStatus.originalLocation,
//                    endTarget = RobotStatus.originalLocation?.pointName?:"",
//                    taskId = tempTaskId
//                )
//            ))
//            add(FinishDockTask(
//                TaskModel(
//                    location = RobotStatus.originalLocation,
//                    endTarget = RobotStatus.originalLocation?.pointName?:"",
//                    taskId = tempTaskId
//                )
//            ))
//            add(AllFinishTask(
//                TaskModel(
//                    location = RobotStatus.originalLocation,
//                    endTarget = RobotStatus.originalLocation?.pointName?:"",
//                    taskId = tempTaskId
//                )
//            ))
//        }
//        return tempQueue
//    }
//
//    /**
//     * @describe 创建送物单任务队列
//     */
//    private fun createSendSingleTaskQueue(
//        location1: QueryPointEntity
//    ): LinkedList<Task> {
//        val tempTaskId = when(location1.binMark){
//                                0x11 -> TaskIdGenerator.getInstance().generateTaskId(TaskTypeEnum.DELIVERY, DoorEnum.FIRST_DOOR)
//                                0xff -> TaskIdGenerator.getInstance().generateTaskId(TaskTypeEnum.DELIVERY, DoorEnum.SECOND_DOOR)
//                                else -> ""
//                            }
//        val tempQueue = LinkedList<Task>()
//        tempQueue.apply {
//            add(StartSingleSendTask(
//                TaskModel(
//                    location = location1,
//                    endTarget = location1.pointName?:"",
//                    taskId = tempTaskId
//                )))
//            add(OutDockTask(
//                TaskModel(
//                    location = location1,
//                    endTarget = location1.pointName?:"",
//                    taskId = tempTaskId
//                )
//            ))
//            add(
//                JudgeFloorTask(
//                    TaskModel(
//                        location = location1,
//                        endTarget = location1.pointName?:"",
//                        taskId = tempTaskId
//                    ),
//                    TYPE_SEND
//                )
//            )
//            // step 10：到目的地task
//            add(
//                SendingTask(
//                    TaskModel(
//                        location = location1,
//                        endTarget = location1.pointName?:"",
//                        taskId = tempTaskId
//                    )
//                )
//            )
//            // step 11：送物：呼叫客房task 引领：step 11
//            add(
//                CallRoomTask(
//                    TaskModel(
//                        location = location1,
//                        endTarget = location1.pointName?:"",
//                        taskId = tempTaskId
//                    )
//                )
//            )
//            // step 12：完成任务task
//            add(
//                CallRoomFinishTask(
//                    TaskModel(
//                        location = location1,
//                        endTarget = location1.pointName?:"",
//                        taskId = tempTaskId
//                    )
//                )
//            )
//            add(FinishSendTask(
//                TaskModel(
//                    location = location1,
//                    endTarget = location1.pointName?:"",
//                    taskId = tempTaskId
//                )
//            ))
//            add(AllFinishSendTask(
//                TaskModel(
//                    location = location1,
//                    endTarget = location1.pointName?:"",
//                    taskId = tempTaskId
//                )
//            ))
//        }
//        return tempQueue
//    }
//
//    /**
//     * @describe 创建送物双任务队列
//     */
//    private fun createSendDoubleTaskQueue(
//        location1: QueryPointEntity,
//        location2: QueryPointEntity,
//    ): LinkedList<Task> {
//        val date = Date()
//        val tempTaskId = TaskIdGenerator.getInstance().generateTaskId(TaskTypeEnum.DELIVERY, DoorEnum.FIRST_DOOR, date)
//        val tempTaskId2 = TaskIdGenerator.getInstance().generateTaskId(TaskTypeEnum.DELIVERY, DoorEnum.SECOND_DOOR, date)
//        val tempQueue = LinkedList<Task>()
//        tempQueue.apply {
//            add(
//                StartDoubleSendTask(
//                    TaskModel(
//                        location = location1,
//                        location2 = location2,
//                        endTarget = location1.pointName?:"",
//                        taskId = tempTaskId
//                    )
//                )
//            )
//            add(OutDockTask(
//                TaskModel(
//                    location = location1,
//                    endTarget = location1.pointName?:"",
//                    taskId = tempTaskId
//                )
//            ))
//            // 双任务
//            if (location1.pointId != location2.pointId) {
//                RobotStatus.twoSamePlace = false
//                add(
//                    JudgeFloorTask(
//                        TaskModel(
//                            location = location1,
//                            endTarget = location1.pointName?:"",
//                            taskId = tempTaskId
//                        ),
//                    type = TYPE_SEND
//                    )
//                )
//                // step 10：到目的地task
//                add(
//                    SendingTask(
//                        TaskModel(
//                            location = location1,
//                            endTarget = location1.pointName?:"",
//                            taskId = tempTaskId
//                        )
//                    )
//                )
//                // step 11：送物：呼叫客房task 引领：step 11
//                add(
//                    CallRoomTask(
//                        TaskModel(
//                            location = location1,
//                            endTarget = location1.pointName?:"",
//                            taskId = tempTaskId
//                        )
//                    )
//                )
//                // step 12：完成任务task
//                add(
//                    CallRoomFinishTask(
//                        TaskModel(
//                            location = location1,
//                            endTarget = location1.pointName?:"",
//                            taskId = tempTaskId
//                        )
//                    )
//                )
//                add(FinishSendTask(
//                    TaskModel(
//                        location = location1,
//                        endTarget = location1.pointName?:"",
//                        taskId = tempTaskId
//                    )
//                ))
//                add(
//                    StartDoubleSecondSendTask(
//                        TaskModel(
//                            location = location2,
//                            endTarget = location2.pointName ?: "",
//                            taskId = tempTaskId2
//                        )
//                    )
//                )
//                add(
//                    JudgeFloorTask(
//                        TaskModel(
//                            location = location2,
//                            endTarget = location2.pointName ?: "",
//                            taskId = tempTaskId2
//                        ),
//                    type = TYPE_SEND
//                    )
//                )
//                // step 10：到目的地task
//                add(
//                    SendingTask(
//                        TaskModel(
//                            location = location2,
//                            endTarget = location2.pointName ?: "",
//                            taskId = tempTaskId2
//                        )
//                    )
//                )
//                // step 11：送物：呼叫客房task 引领：step 11
//                add(
//                    CallRoomTask(
//                        TaskModel(
//                            location = location2,
//                            endTarget = location2.pointName ?: "",
//                            taskId = tempTaskId2
//                        )
//                    )
//                )
//                // step 12：完成任务task
//                add(
//                    CallRoomFinishTask(
//                        TaskModel(
//                            location = location2,
//                            endTarget = location2.pointName ?: "",
//                            taskId = tempTaskId2
//                        )
//                    )
//                )
//                add(FinishSendTask(
//                    TaskModel(
//                        location = location2,
//                        endTarget = location2.pointName ?: "",
//                        taskId = tempTaskId2
//                    )
//                ))
//                add(AllFinishSendTask(
//                    TaskModel(
//                        location = location2,
//                        endTarget = location2.pointName ?: "",
//                        taskId = tempTaskId2
//                    )
//                ))
//            } else {
//                RobotStatus.twoSamePlace = true
//                // 两个任务为同一地点
//                add(
//                    JudgeFloorTask(
//                        TaskModel(
//                            location = location1,
//                            endTarget = location1.pointName ?: "",
//                            taskId = tempTaskId
//                        ),
//                    type = TYPE_SEND
//                    )
//                )
//                // step 10：到目的地task
//                add(
//                    SendingTask(
//                        TaskModel(
//                            location = location1,
//                            endTarget = location1.pointName ?: "",
//                            taskId = tempTaskId
//                        )
//                    )
//                )
//                // step 11：送物：呼叫客房task 引领：step 11
//                add(
//                    CallRoomTask(
//                        TaskModel(
//                            location = location1,
//                            endTarget = location1.pointName ?: "",
//                            taskId = tempTaskId
//                        )
//                    )
//                )
//                // step 12：完成任务task
//                add(
//                    CallRoomFinishTask(
//                        TaskModel(
//                            location = location1,
//                            endTarget = location1.pointName ?: "",
//                            taskId = tempTaskId
//                        )
//                    )
//                )
//                add(FinishSendTask(
//                    TaskModel(
//                        location = location1,
//                        endTarget = location1.pointName ?: "",
//                        taskId = tempTaskId
//                    )
//                ))
//                add(
//                    StartDoubleSecondSendTask(
//                        TaskModel(
//                            location2,
//                            endTarget = location2.pointName ?: "",
//                            taskId = tempTaskId2
//                        )
//                    )
//                )
//                add(
//                    CallRoomFinishTask(
//                        TaskModel(
//                            location = location2,
//                            endTarget = location2.pointName ?: "",
//                            taskId = tempTaskId2
//                        )
//                    )
//                )
//                add(FinishSendTask(
//                    TaskModel(
//                        location = location2,
//                        endTarget = location2.pointName ?: "",
//                        taskId = tempTaskId2
//                    )
//                ))
//                add(AllFinishSendTask(
//                    TaskModel(
//                        location = location2,
//                        endTarget = location2.pointName ?: "",
//                        taskId = tempTaskId2
//                    )
//                ))
//            }
//        }
//        return tempQueue
//    }
//
//    /**
//     * @describe 创建引领任务队列
//     */
//    private fun createGuideTaskQueue(
//        location1: QueryPointEntity
//    ): LinkedList<Task> {
//        val tempTaskId = TaskIdGenerator.getInstance().generateTaskId(TaskTypeEnum.GUIDING)
//        val tempQueue = LinkedList<Task>()
//        tempQueue.apply {
//            add(
//                StartGuideTask(
//                    TaskModel(
//                        location = location1,
//                        endTarget = location1.pointName ?: "",
//                        taskId = tempTaskId
//                    )
//                )
//            )
//            add(
//                OutDockTask(
//                    TaskModel(
//                        location = location1,
//                        endTarget = location1.pointName ?: "",
//                        taskId = tempTaskId
//                    )
//                )
//            )
//            // step 2.5: 切换地图
//            add(
//                JudgeFloorTask(
//                    TaskModel(
//                        location = location1,
//                        endTarget = location1.pointName ?: "",
//                        taskId = tempTaskId
//                    ),
//                    type = TYPE_GUIDE
//                )
//            )
//            // step 10：到目的地task
//            add(
//                GuidingTask(
//                    TaskModel(
//                        location = location1,
//                        endTarget = location1.pointName ?: "",
//                        taskId = tempTaskId
//                    ),
//                    R.id.guidingFragment
//                )
//            )
//            // step 11：到达目的地
//            add(
//                GuideArriveTask(
//                    TaskModel(
//                        location = location1,
//                        endTarget = location1.pointName ?: "",
//                        taskId = tempTaskId
//                    )
//                )
//            )
//            // step 12：完成任务task
//            add(
//                FinishGuideTask(
//                    taskModel = TaskModel(
//                        location = location1,
//                        endTarget = location1.pointName ?: "",
//                        taskId = tempTaskId
//                    )
//                )
//            )
//            add(
//                AllFinishGuideTask(
//                    TaskModel(
//                        location = location1,
//                        endTarget = location1.pointName ?: "",
//                        taskId = tempTaskId
//                    )
//                )
//            )
//        }
//        return tempQueue
//    }
//
//    /**
//     * @describe 创建跑腿任务队列
//     */
//    private fun createRemoteOrderTaskQueue(
//        remoteOrderModel: RemoteOrderModel,
//        type: Int
//    ){
//        TaskQueues.queue.apply {
//            add(StartRemoteOrderPutTask(
//                TaskModel(
//                    remoteOrderModel = remoteOrderModel,
//                    endTarget = remoteOrderModel.from.pointName ?: "",
//                    taskId = remoteOrderModel.taskId
//                ), type))
//            add(StartPutTask(
//                    TaskModel(
//                        remoteOrderModel.from,
//                        endTarget = remoteOrderModel.from.pointName ?: "",
//                        taskId = remoteOrderModel.taskId
//                    )
//                )
//            )
//            add(OutDockTask(
//                TaskModel(
//                    remoteOrderModel.from,
//                    endTarget = remoteOrderModel.from.pointName ?: "",
//                    taskId = remoteOrderModel.taskId
//                )
//            ))
//            add(
//                JudgeFloorTask(
//                    TaskModel(
//                        location = remoteOrderModel.from,
//                        endTarget = remoteOrderModel.from.pointName ?: "",
//                        taskId = remoteOrderModel.taskId
//                    ),
//                    TYPE_SEND
//                )
//            )
//            add(
//                SendingTask(
//                    TaskModel(
//                        location = remoteOrderModel.from,
//                        endTarget = remoteOrderModel.from.pointName ?: "",
//                        taskId = remoteOrderModel.taskId
//                    )
//                )
//            )
//            add(FinishPutTask(
//                TaskModel(
//                    remoteOrderModel.from,
//                    endTarget = remoteOrderModel.from.pointName ?: "",
//                    taskId = remoteOrderModel.taskId
//                )
//            ))
//            add(
//                CallPutObjectTask(
//                    TaskModel(
//                        location = remoteOrderModel.from,
//                        remoteOrderModel = remoteOrderModel,
//                        endTarget = remoteOrderModel.from.pointName ?: "",
//                        taskId = remoteOrderModel.taskId
//                    ),
//                    type = type
//                )
//            )
//            add(
//                CallPutObjectFinishTask(
//                    TaskModel(
//                        location = remoteOrderModel.from,
//                        endTarget = remoteOrderModel.from.pointName ?: "",
//                        taskId = remoteOrderModel.taskId
//                    ),
//                    type = type
//                )
//            )
//            add(
//                FinishRemoteOrderPutTask(
//                    TaskModel(
//                        location = remoteOrderModel.from,
//                        remoteOrderModel = remoteOrderModel,
//                        endTarget = remoteOrderModel.from.pointName ?: "",
//                        taskId = remoteOrderModel.taskId
//                    ),
//                    type = type
//                )
//            )
//        }
//
//    }
//
//    /**
//     * @description 生成送物任务链
//     */
//    fun createRemoteOrderSendTask(remoteOrderModel: RemoteOrderModel, type: Int){
//        TaskQueues.queue.apply {
//            add(0,StartRemoteOrderSendTask(
//                TaskModel(
//                    remoteOrderModel = remoteOrderModel,
//                    endTarget = remoteOrderModel.to.pointName ?: "",
//                    taskId = remoteOrderModel.taskId
//                )
//                , type))
//            add(1,StartTakeTask(
//                TaskModel(
//                    remoteOrderModel.to,
//                    endTarget = remoteOrderModel.to.pointName ?: "",
//                    taskId = remoteOrderModel.taskId
//                )
//            ))
//            add(2,
//                JudgeFloorTask(
//                    TaskModel(
//                        location = remoteOrderModel.to,
//                        endTarget = remoteOrderModel.to.pointName ?: "",
//                        taskId = remoteOrderModel.taskId
//                    ),
//                    TYPE_SEND
//                )
//            )
//            add(3,
//                SendingTask(
//                    TaskModel(
//                        location = remoteOrderModel.to,
//                        endTarget = remoteOrderModel.to.pointName ?: "",
//                        taskId = remoteOrderModel.taskId
//                    )
//                )
//            )
//            add(4,FinishTakeTask(
//                TaskModel(
//                    remoteOrderModel.to,
//                    endTarget = remoteOrderModel.to.pointName ?: "",
//                    taskId = remoteOrderModel.taskId
//                )
//            ))
//            add(5,
//                CallTakeObjectTask(
//                    TaskModel(
//                        location = remoteOrderModel.to,
//                        remoteOrderModel = remoteOrderModel,
//                        endTarget = remoteOrderModel.to.pointName ?: "",
//                        taskId = remoteOrderModel.taskId
//                    ),
//                    type = type
//                )
//            )
//            add(6,
//                CallTakeObjectFinishTask(
//                    TaskModel(
//                        location = remoteOrderModel.to,
//                        endTarget = remoteOrderModel.to.pointName ?: "",
//                        taskId = remoteOrderModel.taskId
//                    ),
//                    type = type
//                )
//            )
//            add(7,
//                FinishRemoteOrderSendTask(
//                    TaskModel(
//                        location = remoteOrderModel.to,
//                        remoteOrderModel = remoteOrderModel,
//                        endTarget = remoteOrderModel.to.pointName ?: "",
//                        taskId = remoteOrderModel.taskId
//                    ),
//                    type = type
//                )
//            )
//            add(8,
//                AllFinishSendTask(
//                    TaskModel(
//                        location = remoteOrderModel.to,
//                        endTarget = remoteOrderModel.to.pointName ?: "",
//                        taskId = remoteOrderModel.taskId
//                    )
//                )
//            )
//        }
//    }
//    // 选择舱体
//    val viewModelBin1 = ViewModelLazy(
//        SendPlaceBin1ViewModel::class,
//        { MainActivity.instance.viewModelStore },
//        { MainActivity.instance.defaultViewModelProviderFactory}
//    )
//    val viewModelBin2 = ViewModelLazy(
//        SendPlaceBin2ViewModel::class,
//        { MainActivity.instance.viewModelStore },
//        { MainActivity.instance.defaultViewModelProviderFactory}
//    )
//    lateinit var basicConfig: BasicConfig
//    val dao = DataBaseDeliveredRobotMap.getDatabase(MyApplication.instance!!)
//        .getDao()
//    val mutex = Mutex()
//
//    /**
//     * @description 创建小程序任务链
//     */
//    suspend fun createTask(remoteOrderModel: RemoteOrderModel): Boolean{
//        mutex.withLock {
//            DialogHelper.loadingDialog.show()
//            if (!IdleGateDataHelper.minusCount()){
//                DialogHelper.loadingDialog.dismiss()
//                return false
//            }
//            val bin1 = with(viewModelBin1.value){
//                val result = previousTaskFinished /*&& previousRemoteOrderPutFinished*/ && previousRemoteOrderSendFinished
//                if(result){
//                    // 1仓有空
//                    with(remoteOrderModel){
//                        from.apply {
//                            binMark = binMarkBin1
//                        }
//                        to.apply {
//                            binMark = binMarkBin1
//                        }
//                        viewModelBin1.value.remoteOrderModel = this
//                        if (RobotStatus.currentStatus == TYPE_GO_BACK || RobotStatus.currentStatus == TYPE_CHARGING || RobotStatus.currentStatus == TYPE_IDLE) {
//                            if(RobotStatus.docking){//重置重试次数
//                                RobotStatus.retryDockTimes = 0
//                                RobotStatus.docking = false
//                                ROSHelper.controlDock(RobotCommand.CMD_STOP)
//                            }
//                            // step 0：清空队列
//                            TaskQueues.clearQueue()
//                            //step:0.5:
//                            createRemoteOrderTask(
//                                this,
//                                taskType = when(taskType){
//                                    "送物" -> TYPE_REMOTE_ORDER_SEND
//                                    "取物" -> TYPE_REMOTE_ORDER_TAKE
//                                    else -> TYPE_REMOTE_ORDER_SEND
//                                }
//                            )
//                            withContext(Dispatchers.Default){
//                                ROSHelper.manageRobotUntilDone(RobotCommand.MANAGE_STATUS_STOP)
//                            }
//                        }else{
//                            createRemoteOrderTask(
//                                this,
//                                taskType = when(taskType){
//                                    "送物" -> TYPE_REMOTE_ORDER_SEND
//                                    "取物" -> TYPE_REMOTE_ORDER_TAKE
//                                    else -> TYPE_REMOTE_ORDER_SEND
//                                }
//                            )
//                        }
//
//                    }
//                }
//                result
//            }
//            var bin2 = false
//            if(!bin1){
//                bin2 = with(viewModelBin2.value){
//                    val result = previousTaskFinished /*&& previousRemoteOrderPutFinished*/ && previousRemoteOrderSendFinished
//                    if(result){
//                        // 2仓有空
//                        with(remoteOrderModel){
//                            from.apply {
//                                binMark = binMarkBin2
//                            }
//                            to.apply {
//                                binMark = binMarkBin2
//                            }
//                            viewModelBin2.value.remoteOrderModel = this
//                            if (RobotStatus.currentStatus == TYPE_GO_BACK || RobotStatus.currentStatus == TYPE_CHARGING || RobotStatus.currentStatus == TYPE_IDLE) {
//                                if(RobotStatus.docking){//重置重试次数
//                                    RobotStatus.retryDockTimes = 0
//                                    RobotStatus.docking = false
//                                    ROSHelper.controlDock(RobotCommand.CMD_STOP)
//                                }
//                                // 如果是返回过程，则打断，并执行下一个任务
//                                TaskQueues.clearQueue()
//                                createRemoteOrderTask(
//                                    this,
//                                    taskType = when(taskType){
//                                        "送物" -> TYPE_REMOTE_ORDER_SEND
//                                        "取物" -> TYPE_REMOTE_ORDER_TAKE
//                                        else -> TYPE_REMOTE_ORDER_SEND
//                                    }
//                                )
//                                withContext(Dispatchers.Default){
//                                    ROSHelper.manageRobotUntilDone(RobotCommand.MANAGE_STATUS_STOP)
//                                }
//                            }else{
//                                // 如果不是返回过程，则创建小程序任务链添加到队列尾部，等待执行
//                                createRemoteOrderTask(
//                                    this,
//                                    taskType = when(taskType){
//                                        "送物" -> TYPE_REMOTE_ORDER_SEND
//                                        "取物" -> TYPE_REMOTE_ORDER_TAKE
//                                        else -> TYPE_REMOTE_ORDER_SEND
//                                    }
//                                )
//                            }
//                        }
//                    }
//                    result
//                }
//            }
//            DialogHelper.loadingDialog.dismiss()
//            return bin1 || bin2
//        }
//    }
//
//    /**
//     * @describe 创建线下任务链
//     */
//    suspend fun createTask(
//        location1: QueryPointEntity,
//        location2: QueryPointEntity? = null,
//        taskType: Int
//    ): LinkedList<Task> {
//        mutex.withLock {
//            DialogHelper.loadingDialog.show()
////        withContext(Dispatchers.Default) {
//            val tempQueue = createQueue(location1, location2, taskType)
//            DialogHelper.loadingDialog.dismiss()
//            return tempQueue
//        }
//    }
//
//    /**
//     * @description 创建小程序任务链
//     */
//    private fun createRemoteOrderTask(
//        remoteOrderModel: RemoteOrderModel,
//        taskType: Int
//    ){
//        when (taskType) {
//            TYPE_REMOTE_ORDER_SEND,
//            TYPE_REMOTE_ORDER_TAKE -> {
//                RobotStatus.lowPowerBacking = false
//                createRemoteOrderTaskQueue(remoteOrderModel, taskType)
//            }
//        }
//    }
//
//    private fun createQueue(
//        location1: QueryPointEntity,
//        location2: QueryPointEntity? = null,
//        taskType: Int
//    ): LinkedList<Task> {
////        // step 0：清空队列
////        TaskQueues.clearQueue()
////        //step:0.5:
////        ROSHelper.manageRobotUntilDone(RobotCommand.MANAGE_STATUS_STOP)
//        // step 2：判断任务类型（送物/引领）
//        when (taskType) {
//            TYPE_GUIDE -> {
//                RobotStatus.lowPowerBacking = false
//                return createGuideTaskQueue(location1)
//            }
//            TYPE_SEND -> {
//                RobotStatus.lowPowerBacking = false
//                return if (location2 != null) {
//                    //双任务
//                    createSendDoubleTaskQueue(location1, location2)
//                } else {
//                    // 单任务
//                    createSendSingleTaskQueue(location1)
//                }
//            }
//            TYPE_GO_BACK -> {
//                //生成当前任务ID
//                return createGoBackTaskQueue()
//            }
//        }
//        //记录当前任务类型
////        RobotStatus.currentStatus = taskType
//        return LinkedList<Task>()
//    }
//
//
//    /**
//     * @describe 创建返回任务队列
//     */
//    private fun createGoBackTaskQueue(): LinkedList<Task> {
//        val tempTaskId = TaskQueues. currentTask?.taskModel?.taskId?:""
//        val tempQueue = LinkedList<Task>()
//        tempQueue.apply {
//            //step 13: 回到充电桩
//            add(GoBackTask(
//                TaskModel(
//                    location = RobotStatus.originalLocation!!,
//                    endTarget = RobotStatus.originalLocation?.pointName?:"",
//                    taskId = tempTaskId
//                )
//            ))
//            add(
//                JudgeFloorTask(
//                    TaskModel(
//                        location = RobotStatus.originalLocation!!,
//                        endTarget = RobotStatus.originalLocation?.pointName?:"",
//                        taskId = tempTaskId
//                    ),
//                    TYPE_GO_BACK
//                )
//            )
//            add(
//                AdvanceGuidingTask(
//                    cmd = 1,
//                    taskModel = TaskModel(
//                        location = RobotStatus.originalLocation,
//                        endTarget = RobotStatus.originalLocation?.pointName?:"",
//                        taskId = tempTaskId
//                    ),
//                    navigateId = R.id.goBackFragment
//                )
//            )
//            add(GoBackFinishTask(
//                TaskModel(
//                    location = RobotStatus.originalLocation,
//                    endTarget = RobotStatus.originalLocation?.pointName?:"",
//                    taskId = tempTaskId
//                )
//            ))
//            add(BeginDockTask(
//                TaskModel(
//                    location = RobotStatus.originalLocation,
//                    endTarget = RobotStatus.originalLocation?.pointName?:"",
//                    taskId = tempTaskId
//                )
//            ))
//            add(FinishDockTask(
//                TaskModel(
//                    location = RobotStatus.originalLocation,
//                    endTarget = RobotStatus.originalLocation?.pointName?:"",
//                    taskId = tempTaskId
//                )
//            ))
//            add(AllFinishTask(
//                TaskModel(
//                    location = RobotStatus.originalLocation,
//                    endTarget = RobotStatus.originalLocation?.pointName?:"",
//                    taskId = tempTaskId
//                )
//            ))
//        }
//        return tempQueue
//    }
//
//    /**
//     * @describe 创建送物单任务队列
//     */
//    private fun createSendSingleTaskQueue(
//        location1: QueryPointEntity
//    ): LinkedList<Task> {
//        val tempTaskId = when(location1.binMark){
//                                0x11 -> TaskIdGenerator.getInstance().generateTaskId(TaskTypeEnum.DELIVERY, DoorEnum.FIRST_DOOR)
//                                0xff -> TaskIdGenerator.getInstance().generateTaskId(TaskTypeEnum.DELIVERY, DoorEnum.SECOND_DOOR)
//                                else -> ""
//                            }
//        val tempQueue = LinkedList<Task>()
//        tempQueue.apply {
//            add(StartSingleSendTask(
//                TaskModel(
//                    location = location1,
//                    endTarget = location1.pointName?:"",
//                    taskId = tempTaskId
//                )))
////            // step 2.5: 切换地图
////            add(
////                SwitchSubMapTask(
////                    taskModel = TaskModel(
////                        location = RobotStatus.currentLocation
////                    ),
////                    needSwitch = false
////                )
////            )
//            add(OutDockTask(
//                TaskModel(
//                    location = location1,
//                    endTarget = location1.pointName?:"",
//                    taskId = tempTaskId
//                )
//            ))
//            add(
//                JudgeFloorTask(
//                    TaskModel(
//                        location = location1,
//                        endTarget = location1.pointName?:"",
//                        taskId = tempTaskId
//                    ),
//                    TYPE_SEND
//                )
//            )
//            // step 10：到目的地task
//            add(
//                SendingTask(
//                    TaskModel(
//                        location = location1,
//                        endTarget = location1.pointName?:"",
//                        taskId = tempTaskId
//                    )
//                )
//            )
//            // step 11：送物：呼叫客房task 引领：step 11
//            add(
//                CallRoomTask(
//                    TaskModel(
//                        location = location1,
//                        endTarget = location1.pointName?:"",
//                        taskId = tempTaskId
//                    )
//                )
//            )
//            // step 12：完成任务task
//            add(
//                CallRoomFinishTask(
//                    TaskModel(
//                        location = location1,
//                        endTarget = location1.pointName?:"",
//                        taskId = tempTaskId
//                    )
//                )
//            )
//            add(FinishSendTask(
//                TaskModel(
//                    location = location1,
//                    endTarget = location1.pointName?:"",
//                    taskId = tempTaskId
//                )
//            ))
//            add(AllFinishSendTask(
//                TaskModel(
//                    location = location1,
//                    endTarget = location1.pointName?:"",
//                    taskId = tempTaskId
//                )
//            ))
//        }
//        return tempQueue
//    }
//
//    /**
//     * @describe 创建送物双任务队列
//     */
//    private fun createSendDoubleTaskQueue(
//        location1: QueryPointEntity,
//        location2: QueryPointEntity,
//    ): LinkedList<Task> {
//        val date = Date()
//        val tempTaskId = TaskIdGenerator.getInstance().generateTaskId(TaskTypeEnum.DELIVERY, DoorEnum.FIRST_DOOR, date)
//        val tempTaskId2 = TaskIdGenerator.getInstance().generateTaskId(TaskTypeEnum.DELIVERY, DoorEnum.SECOND_DOOR, date)
//        val tempQueue = LinkedList<Task>()
//        tempQueue.apply {
//            add(
//                StartDoubleSendTask(
//                    TaskModel(
//                        location = location1,
//                        location2 = location2,
//                        endTarget = location1.pointName?:"",
//                        taskId = tempTaskId
//                    )
//                )
//            )
//            add(OutDockTask(
//                TaskModel(
//                    location = location1,
//                    endTarget = location1.pointName?:"",
//                    taskId = tempTaskId
//                )
//            ))
//            // 双任务
//            if (location1.pointId != location2.pointId) {
//                RobotStatus.twoSamePlace = false
//                add(
//                    JudgeFloorTask(
//                        TaskModel(
//                            location = location1,
//                            endTarget = location1.pointName?:"",
//                            taskId = tempTaskId
//                        ),
//                    type = TYPE_SEND
//                    )
//                )
//                // step 10：到目的地task
//                add(
//                    SendingTask(
//                        TaskModel(
//                            location = location1,
//                            endTarget = location1.pointName?:"",
//                            taskId = tempTaskId
//                        )
//                    )
//                )
//                // step 11：送物：呼叫客房task 引领：step 11
//                add(
//                    CallRoomTask(
//                        TaskModel(
//                            location = location1,
//                            endTarget = location1.pointName?:"",
//                            taskId = tempTaskId
//                        )
//                    )
//                )
//                // step 12：完成任务task
//                add(
//                    CallRoomFinishTask(
//                        TaskModel(
//                            location = location1,
//                            endTarget = location1.pointName?:"",
//                            taskId = tempTaskId
//                        )
//                    )
//                )
//                add(FinishSendTask(
//                    TaskModel(
//                        location = location1,
//                        endTarget = location1.pointName?:"",
//                        taskId = tempTaskId
//                    )
//                ))
//                add(
//                    StartDoubleSecondSendTask(
//                        TaskModel(
//                            location = location2,
//                            endTarget = location2.pointName ?: "",
//                            taskId = tempTaskId2
//                        )
//                    )
//                )
//                add(
//                    JudgeFloorTask(
//                        TaskModel(
//                            location = location2,
//                            endTarget = location2.pointName ?: "",
//                            taskId = tempTaskId2
//                        ),
//                    type = TYPE_SEND
//                    )
//                )
//                // step 10：到目的地task
//                add(
//                    SendingTask(
//                        TaskModel(
//                            location = location2,
//                            endTarget = location2.pointName ?: "",
//                            taskId = tempTaskId2
//                        )
//                    )
//                )
//                // step 11：送物：呼叫客房task 引领：step 11
//                add(
//                    CallRoomTask(
//                        TaskModel(
//                            location = location2,
//                            endTarget = location2.pointName ?: "",
//                            taskId = tempTaskId2
//                        )
//                    )
//                )
//                // step 12：完成任务task
//                add(
//                    CallRoomFinishTask(
//                        TaskModel(
//                            location = location2,
//                            endTarget = location2.pointName ?: "",
//                            taskId = tempTaskId2
//                        )
//                    )
//                )
//                add(FinishSendTask(
//                    TaskModel(
//                        location = location2,
//                        endTarget = location2.pointName ?: "",
//                        taskId = tempTaskId2
//                    )
//                ))
//                add(AllFinishSendTask(
//                    TaskModel(
//                        location = location2,
//                        endTarget = location2.pointName ?: "",
//                        taskId = tempTaskId2
//                    )
//                ))
//            } else {
//                RobotStatus.twoSamePlace = true
//                // 两个任务为同一地点
//                add(
//                    JudgeFloorTask(
//                        TaskModel(
//                            location = location1,
//                            endTarget = location1.pointName ?: "",
//                            taskId = tempTaskId
//                        ),
//                    type = TYPE_SEND
//                    )
//                )
//                // step 10：到目的地task
//                add(
//                    SendingTask(
//                        TaskModel(
//                            location = location1,
//                            endTarget = location1.pointName ?: "",
//                            taskId = tempTaskId
//                        )
//                    )
//                )
//                // step 11：送物：呼叫客房task 引领：step 11
//                add(
//                    CallRoomTask(
//                        TaskModel(
//                            location = location1,
//                            endTarget = location1.pointName ?: "",
//                            taskId = tempTaskId
//                        )
//                    )
//                )
//                // step 12：完成任务task
//                add(
//                    CallRoomFinishTask(
//                        TaskModel(
//                            location = location1,
//                            endTarget = location1.pointName ?: "",
//                            taskId = tempTaskId
//                        )
//                    )
//                )
//                add(FinishSendTask(
//                    TaskModel(
//                        location = location1,
//                        endTarget = location1.pointName ?: "",
//                        taskId = tempTaskId
//                    )
//                ))
//                add(
//                    StartDoubleSecondSendTask(
//                        TaskModel(
//                            location2,
//                            endTarget = location2.pointName ?: "",
//                            taskId = tempTaskId2
//                        )
//                    )
//                )
//                add(
//                    CallRoomFinishTask(
//                        TaskModel(
//                            location = location2,
//                            endTarget = location2.pointName ?: "",
//                            taskId = tempTaskId2
//                        )
//                    )
//                )
//                add(FinishSendTask(
//                    TaskModel(
//                        location = location2,
//                        endTarget = location2.pointName ?: "",
//                        taskId = tempTaskId2
//                    )
//                ))
//                add(AllFinishSendTask(
//                    TaskModel(
//                        location = location2,
//                        endTarget = location2.pointName ?: "",
//                        taskId = tempTaskId2
//                    )
//                ))
//            }
//        }
////        TaskQueues.queue.addAll(0,tempQueue)
//        return tempQueue
//    }
//
//    /**
//     * @describe 创建引领任务队列
//     */
//    private fun createGuideTaskQueue(
//        location1: QueryPointEntity
//    ): LinkedList<Task> {
//        val tempTaskId = TaskIdGenerator.getInstance().generateTaskId(TaskTypeEnum.GUIDING)
//        val tempQueue = LinkedList<Task>()
//        tempQueue.apply {
//            add(
//                StartGuideTask(
//                    TaskModel(
//                        location = location1,
//                        endTarget = location1.pointName ?: "",
//                        taskId = tempTaskId
//                    )
//                )
//            )
//            add(
//                OutDockTask(
//                    TaskModel(
//                        location = location1,
//                        endTarget = location1.pointName ?: "",
//                        taskId = tempTaskId
//                    )
//                )
//            )
//            // step 2.5: 切换地图
//            add(
//                JudgeFloorTask(
//                    TaskModel(
//                        location = location1,
//                        endTarget = location1.pointName ?: "",
//                        taskId = tempTaskId
//                    ),
//                    type = TYPE_GUIDE
//                )
//            )
//            // step 10：到目的地task
//            add(
//                GuidingTask(
//                    TaskModel(
//                        location = location1,
//                        endTarget = location1.pointName ?: "",
//                        taskId = tempTaskId
//                    ),
//                    R.id.guidingFragment
//                )
//            )
//            // step 11：到达目的地
//            add(
//                GuideArriveTask(
//                    TaskModel(
//                        location = location1,
//                        endTarget = location1.pointName ?: "",
//                        taskId = tempTaskId
//                    )
//                )
//            )
//            // step 12：完成任务task
//            add(
//                FinishGuideTask(
//                    taskModel = TaskModel(
//                        location = location1,
//                        endTarget = location1.pointName ?: "",
//                        taskId = tempTaskId
//                    )
//                )
//            )
//            add(
//                AllFinishGuideTask(
//                    TaskModel(
//                        location = location1,
//                        endTarget = location1.pointName ?: "",
//                        taskId = tempTaskId
//                    )
//                )
//            )
//        }
//        return tempQueue
//    }
//
//    /**
//     * @describe 创建跑腿任务队列
//     */
//    private fun createRemoteOrderTaskQueue(
//        remoteOrderModel: RemoteOrderModel,
//        type: Int
//    ){
//        TaskQueues.queue.apply {
//            add(StartRemoteOrderPutTask(
//                TaskModel(
//                    remoteOrderModel = remoteOrderModel,
//                    endTarget = remoteOrderModel.from.pointName ?: "",
//                    taskId = remoteOrderModel.taskId
//                ), type))
//            add(StartPutTask(
//                    TaskModel(
//                        remoteOrderModel.from,
//                        endTarget = remoteOrderModel.from.pointName ?: "",
//                        taskId = remoteOrderModel.taskId
//                    )
//                )
//            )
//            add(OutDockTask(
//                TaskModel(
//                    remoteOrderModel.from,
//                    endTarget = remoteOrderModel.from.pointName ?: "",
//                    taskId = remoteOrderModel.taskId
//                )
//            ))
//            add(
//                JudgeFloorTask(
//                    TaskModel(
//                        location = remoteOrderModel.from,
//                        endTarget = remoteOrderModel.from.pointName ?: "",
//                        taskId = remoteOrderModel.taskId
//                    ),
//                    TYPE_SEND
//                )
//            )
//            add(
//                SendingTask(
//                    TaskModel(
//                        location = remoteOrderModel.from,
//                        endTarget = remoteOrderModel.from.pointName ?: "",
//                        taskId = remoteOrderModel.taskId
//                    )
//                )
//            )
//            add(FinishPutTask(
//                TaskModel(
//                    remoteOrderModel.from,
//                    endTarget = remoteOrderModel.from.pointName ?: "",
//                    taskId = remoteOrderModel.taskId
//                )
//            ))
//            add(
//                CallPutObjectTask(
//                    TaskModel(
//                        location = remoteOrderModel.from,
//                        remoteOrderModel = remoteOrderModel,
//                        endTarget = remoteOrderModel.from.pointName ?: "",
//                        taskId = remoteOrderModel.taskId
//                    ),
//                    type = type
//                )
//            )
//            add(
//                CallPutObjectFinishTask(
//                    TaskModel(
//                        location = remoteOrderModel.from,
//                        endTarget = remoteOrderModel.from.pointName ?: "",
//                        taskId = remoteOrderModel.taskId
//                    ),
//                    type = type
//                )
//            )
//            add(
//                FinishRemoteOrderPutTask(
//                    TaskModel(
//                        location = remoteOrderModel.from,
//                        remoteOrderModel = remoteOrderModel,
//                        endTarget = remoteOrderModel.from.pointName ?: "",
//                        taskId = remoteOrderModel.taskId
//                    ),
//                    type = type
//                )
//            )
//        }
//
//    }
//
//    /**
//     * @description 生成送物任务链
//     */
//    fun createRemoteOrderSendTask(remoteOrderModel: RemoteOrderModel, type: Int){
//        TaskQueues.queue.apply {
//            add(0,StartRemoteOrderSendTask(
//                TaskModel(
//                    remoteOrderModel = remoteOrderModel,
//                    endTarget = remoteOrderModel.to.pointName ?: "",
//                    taskId = remoteOrderModel.taskId
//                )
//                , type))
//            add(1,StartTakeTask(
//                TaskModel(
//                    remoteOrderModel.to,
//                    endTarget = remoteOrderModel.to.pointName ?: "",
//                    taskId = remoteOrderModel.taskId
//                )
//            ))
//            add(2,
//                JudgeFloorTask(
//                    TaskModel(
//                        location = remoteOrderModel.to,
//                        endTarget = remoteOrderModel.to.pointName ?: "",
//                        taskId = remoteOrderModel.taskId
//                    ),
//                    TYPE_SEND
//                )
//            )
//            add(3,
//                SendingTask(
//                    TaskModel(
//                        location = remoteOrderModel.to,
//                        endTarget = remoteOrderModel.to.pointName ?: "",
//                        taskId = remoteOrderModel.taskId
//                    )
//                )
//            )
//            add(4,FinishTakeTask(
//                TaskModel(
//                    remoteOrderModel.to,
//                    endTarget = remoteOrderModel.to.pointName ?: "",
//                    taskId = remoteOrderModel.taskId
//                )
//            ))
//            add(5,
//                CallTakeObjectTask(
//                    TaskModel(
//                        location = remoteOrderModel.to,
//                        remoteOrderModel = remoteOrderModel,
//                        endTarget = remoteOrderModel.to.pointName ?: "",
//                        taskId = remoteOrderModel.taskId
//                    ),
//                    type = type
//                )
//            )
//            add(6,
//                CallTakeObjectFinishTask(
//                    TaskModel(
//                        location = remoteOrderModel.to,
//                        endTarget = remoteOrderModel.to.pointName ?: "",
//                        taskId = remoteOrderModel.taskId
//                    ),
//                    type = type
//                )
//            )
//            add(7,
//                FinishRemoteOrderSendTask(
//                    TaskModel(
//                        location = remoteOrderModel.to,
//                        remoteOrderModel = remoteOrderModel,
//                        endTarget = remoteOrderModel.to.pointName ?: "",
//                        taskId = remoteOrderModel.taskId
//                    ),
//                    type = type
//                )
//            )
//            add(8,
//                AllFinishSendTask(
//                    TaskModel(
//                        location = remoteOrderModel.to,
//                        endTarget = remoteOrderModel.to.pointName ?: "",
//                        taskId = remoteOrderModel.taskId
//                    )
//                )
//            )
//        }
//    }
}
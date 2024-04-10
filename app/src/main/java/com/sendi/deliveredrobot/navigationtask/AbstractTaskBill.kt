package com.sendi.deliveredrobot.navigationtask

import androidx.lifecycle.ViewModelLazy
import com.sendi.deliveredrobot.MainActivity
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.RobotCommand
import com.sendi.deliveredrobot.entity.entitySql.QuerySql
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.task.*
import com.sendi.deliveredrobot.room.PointType
import com.sendi.deliveredrobot.room.database.DataBaseDeliveredRobotMap
import com.sendi.deliveredrobot.viewmodel.SendPlaceBin1ViewModel
import com.sendi.deliveredrobot.viewmodel.SendPlaceBin2ViewModel
import java.util.*

abstract class AbstractTaskBill(private val taskModel: TaskModel?): ITaskBill {
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
        val dao = DataBaseDeliveredRobotMap.getDatabase(MyApplication.instance!!).getDao()
    }
    private var endTarget: String = ""
    private var taskId: String = ""
    val taskQueue = LinkedList<AbstractTask>()
    var currentTask: AbstractTask? = null
    lateinit var floorName: String

    override fun add(task: AbstractTask) {
        taskQueue.add(task)
    }

    override fun addAll(index: Int, list: LinkedList<AbstractTask>) {
        if(index > -1){
            taskQueue.addAll(index, list)
        }else{
            taskQueue.addAll(list)
        }
    }

    override fun hasRemain(): Boolean {
        return taskQueue.size > 0
    }

    override fun nextTask(): AbstractTask? {
        return taskQueue.poll()
    }

    override fun endTarget(): String {
        return endTarget
    }

    override fun taskId(): String {
        return taskId
    }

    override fun taskQueue(): LinkedList<AbstractTask>{
        return taskQueue
    }

    override fun setEndTarget(endTarget: String){
        this.endTarget = endTarget
    }

    override fun setTaskId(taskId: String){
        this.taskId = taskId
    }

    override fun firstPeek(): AbstractTask? {
        return taskQueue.peek()
    }

    override fun removeAll() {
        taskQueue.clear()
    }

    override fun addCurrentTask() {
        if (currentTask != null) {
            taskQueue.add(0, currentTask!!)
        }
    }

    override fun currentTask(): AbstractTask? {
        return currentTask
    }

    override fun executeNextTask() {
        //急停按下则不执行任务
        if (RobotStatus.stopButtonPressed.value == RobotCommand.STOP_BUTTON_PRESSED) return
        if (this !is StandStillTaskBill) {
            RobotStatus.batteryStateNumber.postValue(false)
        }
        if (hasRemain()) {
            val tempTask = nextTask()
            currentTask = tempTask
            tempTask?.taskExecute()
        }else{
            if(BillManager.billList().size > 1){
                BillManager.removeBill()
            }else{
                val taskId = BillManager.currentBill()?.taskId()?:""
                val currentBillIsExplainTaskBill = BillManager.currentBill() is ExplainTaskBill
                BillManager.removeBill()
                val notGoBack = QuerySql.QueryBasic().explainFinishedNotGoBack == 1
                if (notGoBack && currentBillIsExplainTaskBill){
                    val bill = StandStillBillFactory.createBill(TaskModel(
                        taskId = taskId
                    ))
                    BillManager.addAllAtIndex(bill)
                }else{
                    val readyPoint = dao.queryReadyPoint()
                    when (readyPoint?.type) {
                        PointType.CHARGE_POINT -> {
                            // 如果后续没有任务，则返回充电桩
                            val bill = GoBackTaskBillFactory.createBill(TaskModel(
                                taskId = taskId
                            ))
                            BillManager.addAllAtIndex(bill)
                        }
                        PointType.READY_POINT -> {
                            val bill = GoBackReadyPointBillFactory.createBill(TaskModel(location = readyPoint, taskId = taskId))
                            BillManager.addAllAtIndex(bill)
                        }
                    }
                }
            }
            BillManager.currentBill()?.executeNextTask()
        }
    }

    override fun addRetryIntoLiftQueue(needHoldLiftDoor: Boolean){
        RobotStatus.needDelay = true
        val tempQueue = LinkedList<AbstractTask>()
        tempQueue.apply {
            add(
                OutLiftTask(
                    TaskModel(
                        RobotStatus.currentLocation,
                        endTarget = taskModel?.endTarget?:"",
                        taskId = taskId(),
                        bill = this@AbstractTaskBill,
                        elevator = currentTask?.taskModel?.elevator?:""
                    ),
                    needGetPose = false,
                    needHoldLiftDoor = needHoldLiftDoor,
                )
            )
            add(
                OutLiftFinishTask(
                    TaskModel(
                        RobotStatus.currentLocation,
                        endTarget = taskModel?.endTarget?:"",
                        taskId = taskId(),
                        bill = this@AbstractTaskBill,
                        elevator = currentTask?.taskModel?.elevator?:""
                    ),
                    reset = false
                )
            )
            add(
                CallLiftTask(
                    TaskModel(
                        RobotStatus.currentLocation,
                        endTarget = taskModel?.endTarget?:"",
                        taskId = taskId(),
                        bill = this@AbstractTaskBill,
                        elevator = currentTask?.taskModel?.elevator?:""
                    )
                )
            )
            add(
                ScanLiftTask(
                    TaskModel(
                        location = RobotStatus.currentLocation,
                        endTarget = taskModel?.endTarget?:"",
                        taskId = taskId(),
                        bill = this@AbstractTaskBill,
                        elevator = currentTask?.taskModel?.elevator?:""
                    )
                )
            )
            add(
                IntoLiftTask(
                    TaskModel(
                        RobotStatus.currentLocation,
                        endTarget = taskModel?.endTarget?:"",
                        taskId = taskId(),
                        bill = this@AbstractTaskBill,
                        elevator = currentTask?.taskModel?.elevator?:""
                    ))
            )
        }
//        TaskQueue.queue.addAll(0, tempQueue)
        taskQueue.addAll(0, tempQueue)
    }

    override fun addRetryOutLiftQueue(){
        var tempTask: AbstractTask
        while (true){
            tempTask = taskQueue.peek() as AbstractTask
            when(tempTask){
                is OutLiftFinishTask -> {
                    val tempQueue = LinkedList<AbstractTask>()
                    // 以此为依据判断 是否为 进梯失败后 -> 出梯失败
                    if (tempTask.reset) {
                        tempQueue.apply {
                            //创建呼梯、出梯任务
                            add(
                                LiftMoveTask(
                                    TaskModel(
                                        location = RobotStatus.expectLocation,
                                        endTarget = taskModel?.endTarget?:"",
                                        taskId = taskId(),
                                        bill = this@AbstractTaskBill,
                                        elevator = currentTask?.taskModel?.elevator?:""
                                    ))
                            )
                            add(
                                OutLiftTask(
                                    TaskModel(
                                        location = RobotStatus.expectLocation,
                                        endTarget = taskModel?.endTarget?:"",
                                        taskId = taskId(),
                                        bill = this@AbstractTaskBill,
                                        elevator = currentTask?.taskModel?.elevator?:""
                                    ), needGetPose = false)
                            )
                        }
                        taskQueue.addAll(0, tempQueue)
                    }
                    //延迟10秒后执行，避免电梯还未开始运行
                    RobotStatus.needDelay = true
                    break
                }
                else -> taskQueue.poll()
            }
        }
    }
}
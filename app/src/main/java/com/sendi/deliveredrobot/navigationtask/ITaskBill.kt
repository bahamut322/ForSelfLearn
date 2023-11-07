package com.sendi.deliveredrobot.navigationtask

import com.sendi.deliveredrobot.model.TaskModel
import java.util.*

/**
 * @author heky
 * @date 2022-08-23
 * @description 任务清单
 */

interface ITaskBill {
    /**
     * @description 中止任务
     */
    suspend fun earlyFinish()

    /**
     * @description 添加
     */
    fun add(task: AbstractTask)

    /**
     * @description 添加
     */
    fun addAll(index: Int = -1, list: LinkedList<AbstractTask>)

    /**
     * @description 清单中是否有剩余任务
     */
    fun hasRemain(): Boolean

    /**
     * @description 取出下一个任务
     */
    fun nextTask(): AbstractTask?

    /**
     * @description 设置终点
     */
    fun setEndTarget(endTarget: String)

    /**
     * @description 终点
     */
    fun endTarget(): String

    /**
     * @description 设置任务ID
     */
    fun setTaskId(taskId: String)

    /**
     * @description 任务ID
     */
    fun taskId(): String

    /**
     * @description 异常处理
     */
    suspend fun exception()

    /**
     * @description 执行下一个任务
     */
    fun executeNextTask(){
        RobotStatus.batteryStateNumber.value = false
    }

    /**
     * @description 创建任务
     */
    fun createTaskQueue(taskModel: TaskModel?): LinkedList<AbstractTask>

    /**
     * @description 任务队列
     */
    fun taskQueue(): LinkedList<AbstractTask>

    /**
     * @description 构建billList
     */
    fun billBuild(): List<ITaskBill>

    /**
     * @description 删除
     */
    fun removeAll()

    /**
     * @description 重试进梯
     */
    fun addRetryIntoLiftQueue(needHoldLiftDoor: Boolean)

    /**
     * @description 重试出梯
     */
    fun addRetryOutLiftQueue()

    /**
     * @description 头部
     */
    fun firstPeek():AbstractTask?

    /**
     * @description add当前至头部
     */
    fun addCurrentTask()

    /**
     * @description 当前任务
     */
    fun currentTask(): AbstractTask?
}
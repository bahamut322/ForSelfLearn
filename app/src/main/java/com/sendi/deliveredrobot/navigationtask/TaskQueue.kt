package com.sendi.deliveredrobot.navigationtask

import android.content.Intent
import com.sendi.deliveredrobot.*
import com.sendi.deliveredrobot.ACTION_NAVIGATE
import com.sendi.deliveredrobot.NAVIGATE_ID
import com.sendi.deliveredrobot.NAVIGATE_TO_HOME
import java.util.*

/**
 * describe:导航任务队列
 */
object TaskQueue {
//    var queue = LinkedList<Task>()
//    var currentTask: Task? = null
//    var autoCruiseQueue = LinkedList<Task>()
//    var endTargetList = LinkedList<String>() //任务目标点集合
//    private var remain: Int = 0 //剩余任务数

//     fun executeNextTask() {
//        //急停按下则不执行任务
//        val tempTask: Task?
//        if (queue.peek() !is PauseGuideTask) {
//            if (RobotStatus.stopButtonPressed.value == RobotCommand.STOP_BUTTON_PRESSED) return
//            tempTask = queue.poll()
//            previousTask = currentTask
//            currentTask = tempTask
//        } else {
//            tempTask = queue.poll()
//        }
//        tempTask?.taskExecute()
//            ?: //如检测到队列里无任务，则跳到首页，对应统一处理与底盘、云平台等交互失败的异常情况
//            MyApplication.instance?.sendBroadcast(Intent().apply {
//                action = ACTION_NAVIGATE
//                putExtra(NAVIGATE_ID, NAVIGATE_TO_HOME)
//            })
//    }

//    fun addFirst(task: Task) {
//        if (currentTask != null) {
//            queue.add(0, currentTask!!)
//        }
//        queue.add(0, task)
//    }
//
//    fun addCurrentTask(){
//        if (currentTask != null) {
//            queue.add(0, currentTask!!)
//        }
//    }

//    fun clearQueue(){
//        queue.clear()
//        currentTask = null
//        previousTask = null
//    }

//    fun addLastEndTarget(target: String){
//        endTargetList.add(target)
//    }

//    fun addFirstEndTarget(target: String){
//        endTargetList.push(target)
//    }
//
//    fun popEndTarget(){
//        endTargetList.pop()
//    }
//
//    fun nextEndTarget(): String{
//        return when(endTargetList.size > 1){
//            true -> endTargetList[1]
//            false -> ""
//        }
//    }
//
//    /**
//     * @description 获取剩余任务数
//     */
//    fun remainTaskCount(): Int {
//        return when (remain > 0) {
//            true -> remain - 1
//            else -> remain
//        }
//    }
//
//    fun remainPlus(count: Int = 1){
//        remain += count
//    }
//
//    fun remainMinus(){
//        remain--
//    }
//    fun executeNextTask() {
//        //急停按下则不执行任务
//        if (RobotStatus.stopButtonPressed.value == RobotCommand.STOP_BUTTON_PRESSED) return
//        val tempTask = queue.poll()
//        currentTask = tempTask
//        tempTask?.taskExecute()
//            ?: //如检测到队列里无任务，则跳到首页，对应统一处理与底盘、云平台等交互失败的异常情况
//            MyApplication.instance?.sendBroadcast(Intent().apply {
//                action = ACTION_NAVIGATE
//                putExtra(NAVIGATE_ID, NAVIGATE_TO_HOME)
//            })
//    }
//
//    fun addFirst(task: Task) {
//        if (currentTask != null) {
//            queue.add(0, currentTask!!)
//        }
//        queue.add(0, task)
//    }
//
//    fun addCurrentTask() {
//        if (currentTask != null) {
//            queue.add(0, currentTask!!)
//        }
//    }
//
//    fun clearQueue() {
//        queue.clear()
//        currentTask = null
//    }
}
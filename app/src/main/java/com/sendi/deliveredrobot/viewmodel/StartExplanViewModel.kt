package com.sendi.deliveredrobot.viewmodel

import android.text.SpannableStringBuilder
import android.util.Log
import androidx.lifecycle.ViewModel
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.RobotCommand
import com.sendi.deliveredrobot.baidutts.BaiduTTSHelper
import com.sendi.deliveredrobot.entity.QuerySql
import com.sendi.deliveredrobot.entity.Universal
import com.sendi.deliveredrobot.helpers.ROSHelper
import com.sendi.deliveredrobot.model.MyResultModel
import com.sendi.deliveredrobot.model.SecondModel
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.BillManager
import com.sendi.deliveredrobot.navigationtask.BillManager.currentBill
import com.sendi.deliveredrobot.navigationtask.ExplanationBill.createBill
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.navigationtask.RobotStatus.SecondModel
import com.sendi.deliveredrobot.navigationtask.RobotStatus.ready
import com.sendi.deliveredrobot.navigationtask.RobotStatus.sdScreenStatus
import com.sendi.deliveredrobot.navigationtask.RobotStatus.selectRoutMapItem
import com.sendi.deliveredrobot.room.database.DataBaseDeliveredRobotMap
import com.sendi.deliveredrobot.ros.constant.MyCountDownTimer
import com.sendi.deliveredrobot.service.TaskDto
import com.sendi.deliveredrobot.service.UpdateReturn
import com.sendi.deliveredrobot.utils.LogUtil.i
import com.sendi.deliveredrobot.view.widget.TaskNext
import kotlinx.coroutines.*
import java.util.*
import kotlin.math.pow


class StartExplanViewModel : ViewModel() {
    var mDatas: ArrayList<MyResultModel?>? = ArrayList()
    lateinit var mainScope: CoroutineScope
    var countDownTimer: MyCountDownTimer? = null


    fun inForListData(): ArrayList<MyResultModel?>? {
        mDatas = ArrayList()
        mDatas = QuerySql.queryMyData(selectRoutMapItem!!.value!!)
        return mDatas
    }

    fun finishTask() {
        MainScope().launch {
            for (iTaskBill in BillManager.billList()) {
                iTaskBill.earlyFinish()
            }
//            BillManager.currentBill()!!.earlyFinish()
            ROSHelper.manageRobot(RobotCommand.MANAGE_STATUS_STOP)
//            Universal.Model = "结束讲解"
//            ready.postValue(0)

            BaiduTTSHelper.getInstance().stop()
            TaskNext.setToDo("0")
            RobotStatus.ArrayPointExplan.postValue(0)
        }
    }

    fun recombine(selectName: String, array: Boolean) {
        var position = 0
        Universal.selectMapPoint = true
        for (i in mDatas!!.indices) {
            if (mDatas!![i]!!.name == selectName) {
                Log.d("TAG", "onClick: $i")
                position = i
                break
            }
        }
        MainScope().launch(Dispatchers.Default) {
            for (iTaskBill in BillManager.billList()) {
                iTaskBill.earlyFinish()
            }
            BillManager.billList().clear()
            BaiduTTSHelper.getInstance().stop()
//            Universal.Model = "结束讲解"

            i("选择地点的索引：$position")
            for (index in position until inForListData()!!.size) {
                val taskModel = TaskModel(
                    location = DataBaseDeliveredRobotMap.getDatabase(MyApplication.context).getDao()
                        .queryPoint(inForListData()!![index]!!.name),
                )
                val bill = createBill(taskModel = taskModel)
                BillManager.addAllLast(bill)
            }
            if (!array) {
                ROSHelper.manageRobot(RobotCommand.MANAGE_STATUS_STOP)
            }
            BaiduTTSHelper.getInstance().stop()
            TaskNext.setToDo("0")
            RobotStatus.ArrayPointExplan.postValue(0)
            Universal.selectMapPoint = false
            if (array) {
                currentBill()?.executeNextTask()
            }
        }
    }


    fun pause() {
        mainScope.launch {
            ROSHelper.manageRobot(RobotCommand.MANAGE_STATUS_PAUSE)
        }
    }

    fun resume() {
        mainScope.launch {
            ROSHelper.manageRobot(RobotCommand.MANAGE_STATUS_CONTINUE)
        }
    }

    fun downTimer() {
        countDownTimer = MyCountDownTimer(
            millisInFuture = QuerySql.QueryExplainConfig().stayTime * 1000L, // 倒计时总时长，单位为毫秒
            countDownInterval = 1000, // 倒计时间隔，单位为毫秒
            onTick = { millisUntilFinished ->
                // 更新 UI，显示剩余时间
                val seconds = millisUntilFinished / 1000
                i("倒计时器：${seconds}s")
            },
            onFinish = {
                TaskNext.setToDo("1")
                // 倒计时结束，执行操作
//                currentBill()?.executeNextTask()
                ready.postValue(0)
//                Universal.Model = "结束一段讲解"
            }
        )
    }

    /**
     * 路径加入列队方法
     */
    fun start() {
        MainScope().launch(Dispatchers.Default) {
            for (i in mDatas!!.indices) {
                val taskModel = TaskModel(
                    location = DataBaseDeliveredRobotMap.getDatabase(MyApplication.context).getDao()
                        .queryPoint(mDatas!![i]!!.name),
                )
                val bill = createBill(taskModel = taskModel)
//                BillManager.addAllLast(bill)
                BillManager.addAllAtIndex(bill, i)
//                Universal.Model = "开始讲解"
            }
            ready.postValue(0)
            currentBill()?.executeNextTask()
        }
    }


    /**
     * 将数字转换为汉字
     *
     * @param number
     * @return
     */
    fun intToChinese(number: Int): String {
        var number = number
        val value = number.toString()
        val spannable = SpannableStringBuilder()
        val length = value.length
        if (length > 1) {
            /**
             * 对于长度大于1的数，对首位进行赋值；
             * 对于两位数： 如果首位为“1”，则拼接的字符串为“”；
             */
            spannable.append(
                getChinese(
                    number / 10.0.pow((length - 1).toDouble()).toInt(),
                    length
                )
            )
                .append(getUnitChinese(length))
            // 如果该数值取余数为0，则直接返回已有字符（例如：100，直接返回一百）
            if (number % 10.0.pow((length - 1).toDouble()).toInt() == 0) {
                return spannable.toString()
            }
        }
        // 数字为一位数
        if (length == 1) {
            spannable.append(getChinese(number, 1))
        }
        // 数字为两位数
        if (length == 2) {
            // 拼接个位的数值： 如果各位为“0”，则拼接的字符串为“”;
            spannable.append(getChinese(number % 10, 0))
        }
        // 数字为三位数
        if (length == 3) {
            if (number % 100 < 10) {
                spannable.append("零")
                    .append(getChinese(number % 100, 3))
            } else {
                spannable.append(getChinese(number % 100 / 10, 3))
                    .append("十")
                    .append(getChinese(number % 10, 0))
            }
        }
        // 数字为四位数
        if (length == 4) {
            if (number % 1000 < 10) {
                spannable.append("零").append(getChinese(number % 1000, 3))
            } else if (number % 1000 < 100) {
                spannable.append("零")
                    .append(getChinese(number % 1000 / 10, 3))
                    .append("十")
                    .append(getChinese(number % 10, 0))
            } else {
                number %= 1000
                spannable.append(intToChinese(number))
            }
        }
        return spannable.toString()
    }

    /**
     * 根据不同的情况获取对应的中文
     *
     * @param key
     * @param length
     * @return
     */
    private fun getChinese(key: Int, length: Int): String {
        when (key) {
            1 -> {
                return if (length == 2) {
                    ""
                } else "一"
            }

            2 -> return "二"
            3 -> return "三"
            4 -> return "四"
            5 -> return "五"
            6 -> return "六"
            7 -> return "七"
            8 -> return "八"
            9 -> return "九"
            0 -> {
                return if (length == 1) {
                    "零"
                } else ""
            }
        }
        return ""
    }

    /**
     * 根据数字的位数返回最大位数的单位
     *
     * @param length
     * @return
     */
    private fun getUnitChinese(length: Int): String {
        when (length) {
            2 -> return "十"
            3 -> return "百"
            4 -> return "千"
        }
        return ""
    }


    fun mainScope() {
        mainScope = MainScope()

    }


    //下一个任务
    fun nextTask(array: Boolean) {
//        UpdateReturn().stop()
        currentBill()?.executeNextTask()
        Universal.progress = 0
        Universal.taskNum = 0
        RobotStatus.speakNumber.postValue(null)
        if (!array) {
            Universal.nextPointGo = 1
            UpdateReturn().stop()
        }
        TaskNext.setToDo("0")
        RobotStatus.ArrayPointExplan.postValue(0)
    }

    fun cancelMainScope() {
        mainScope.cancel()
    }

    fun secondScreenModel(position: Int, mData: ArrayList<MyResultModel?>) {
        SecondModel?.postValue(
            SecondModel(
                picPlayTime = mData[position]!!.big_picplaytime,
                file = mData[position]!!.big_imagefile?.toString(),
                type = mData[position]!!.big_type,
                textPosition = mData[position]!!.big_textposition,
                fontLayout = mData[position]!!.big_fontlayout,
                fontContent = mData[position]!!.big_fontcontent?.toString(),
                fontBackGround = mData[position]!!.big_fontbackground?.toString(),
                fontColor = mData[position]!!.big_fontcolor?.toString(),
                fontSize = mData[position]!!.big_fontsize,
                picType = mData[position]!!.big_pictype,
                videolayout = mData[position]!!.videolayout,
                videoAudio = mData[position]!!.big_videoaudio
            )
        )
        sdScreenStatus!!.postValue(2)
        i("图片位置：${mData[position]!!.big_imagefile?.toString()}")
    }

    fun splitString(input: String, length: Int): List<String> {
        val result: MutableList<String> = ArrayList()
        var startIndex = 0
        while (startIndex < input.length) {
            var endIndex = startIndex + length
            if (endIndex > input.length) {
                endIndex = input.length
            }
            val substring = input.substring(startIndex, endIndex)
            result.add(substring)
            startIndex = endIndex
        }
        return result
    }
}



package com.sendi.deliveredrobot.viewmodel

import android.text.SpannableStringBuilder
import androidx.lifecycle.ViewModel
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.RobotCommand
import com.sendi.deliveredrobot.baidutts.BaiduTTSHelper
import com.sendi.deliveredrobot.entity.QuerySql
import com.sendi.deliveredrobot.entity.Universal
import com.sendi.deliveredrobot.helpers.MediaPlayerHelper
import com.sendi.deliveredrobot.helpers.ROSHelper
import com.sendi.deliveredrobot.model.MyResultModel
import com.sendi.deliveredrobot.model.SecondModel
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.BillManager.addAllAtIndex
import com.sendi.deliveredrobot.navigationtask.BillManager.currentBill
import com.sendi.deliveredrobot.navigationtask.ConsumptionTask
import com.sendi.deliveredrobot.navigationtask.ExplanationBill.createBill
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.navigationtask.RobotStatus.selectRoutMapItem
import com.sendi.deliveredrobot.room.database.DataBaseDeliveredRobotMap
import com.sendi.deliveredrobot.utils.LogUtil.d
import kotlinx.coroutines.*
import kotlin.math.pow


class StartExplanViewModel : ViewModel() {
    var mDatas: ArrayList<MyResultModel?>? = ArrayList()
    lateinit var mainScope: CoroutineScope


    fun inForListData() {
        mDatas = ArrayList()
        mDatas = QuerySql.queryMyData(selectRoutMapItem!!.value!!)
    }

    /**
     * 路径加入列队方法
     */
    fun start() {
        for (i in 0 until mDatas!!.size) {
            MainScope().launch(Dispatchers.Default) {
                val taskModel = TaskModel(
                    location = DataBaseDeliveredRobotMap.getDatabase(MyApplication.context).getDao()
                        .queryPoint(mDatas!![i]!!.name),
                    speakString = mDatas!![i]?.explanationtext
                )
                val bill = createBill(taskModel = taskModel)
                addAllAtIndex(bill, i)
                currentBill()!!.executeNextTask()
            }
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

    //结束讲解
    fun finish() {
        mainScope.launch {
            currentBill()?.earlyFinish()
            ROSHelper.manageRobot(RobotCommand.MANAGE_STATUS_STOP)
        }

    }

    //下一个任务
    fun nextTask() {
        currentBill()?.executeNextTask()
    }

    fun cancelMainScope() {
        mainScope.cancel()
    }

    fun secondScreenModel(position: Int, mData: ArrayList<MyResultModel?>):SecondModel {
        return SecondModel(
            picPlayTime = mData[position]!!.big_picplaytime,
            file = Universal.robotFile + mData[position]!!.rootmapname + "/" + mData[position]!!.routename + "/big/",
            type = mData[position]!!.big_type,
            textPosition = mData[position]!!.big_textposition,
            fontLayout = mData[position]!!.big_fontlayout,
            fontContent = mData[position]!!.big_fontcontent.toString(),
            fontBackGround = mData[position]!!.big_fontbackground.toString(),
            fontColor = mData[position]!!.big_fontcolor.toString(),
            fontSize = mData[position]!!.big_fontsize
        )
    }
}



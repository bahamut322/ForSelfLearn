package com.sendi.deliveredrobot.viewmodel

import android.text.SpannableStringBuilder
import android.util.Log
import androidx.lifecycle.ViewModel
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.RobotCommand
import com.sendi.deliveredrobot.entity.Universal
import com.sendi.deliveredrobot.entity.entitySql.QuerySql
import com.sendi.deliveredrobot.helpers.DialogHelper
import com.sendi.deliveredrobot.helpers.ExplainManager
import com.sendi.deliveredrobot.helpers.MediaPlayerHelper
import com.sendi.deliveredrobot.helpers.ROSHelper
import com.sendi.deliveredrobot.helpers.ReportDataHelper.reportTaskDto
import com.sendi.deliveredrobot.helpers.SpeakHelper
import com.sendi.deliveredrobot.model.MyResultModel
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.BillManager
import com.sendi.deliveredrobot.navigationtask.ExplanationBillFactory
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.room.database.DataBaseDeliveredRobotMap
import com.sendi.deliveredrobot.ros.constant.MyCountDownTimer
import com.sendi.deliveredrobot.service.TaskStageEnum
import com.sendi.deliveredrobot.service.UpdateReturn
import com.sendi.deliveredrobot.utils.LogUtil
import com.sendi.deliveredrobot.view.widget.TaskNext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.Objects
import kotlin.math.pow


class StartExplainViewModel : ViewModel() {
    private var mData: ArrayList<MyResultModel?>? = null
    var mainScope: CoroutineScope = MainScope()
    var countDownTimer: MyCountDownTimer? = null
    val dao = DataBaseDeliveredRobotMap.getDatabase(MyApplication.context).getDao()


    fun inForListData(): ArrayList<MyResultModel?>? {
        if (mData == null) {
            mData = QuerySql.queryPointDate(RobotStatus.selectRouteMapItemId)
        }
        return mData
    }

    fun finishTask() {
        DialogHelper.loadingDialog.show()
        mainScope.launch {
            for (iTaskBill in BillManager.billList()) {
                iTaskBill.earlyFinish()
            }
            SpeakHelper.stop()
            ROSHelper.manageRobot(RobotCommand.MANAGE_STATUS_STOP)
            TaskNext.setToDo("0")
        }
    }

    fun recombine(selectName: String) {
        if (inForListData() == null) return
        DialogHelper.loadingDialog.show()
        var position = 0
        Universal.selectMapPoint = true
        for (i in inForListData()!!.indices) {
            if (inForListData()!![i]?.name == selectName) {
                Log.d("TAG", "onClick: $i")
                position = i
                break
            }
        }
        mainScope.launch(Dispatchers.Default) {
            for (iTaskBill in BillManager.billList()) {
                iTaskBill.earlyFinish()
            }
            BillManager.clearBillList()
            SpeakHelper.stop()
//            BaiduTTSHelper.getInstance().stop()
//            Universal.Model = "结束讲解"

            LogUtil.i("选择地点的索引：$position")
//            for (index in position until mData!!.size) {
            for (index in position until inForListData()!!.size) {
                val taskModel = TaskModel(
                    location = dao.queryPoint(inForListData()!![index]!!.name),
                )
                val bill = ExplanationBillFactory.createBill(taskModel = taskModel)
                BillManager.addAllLast(bill)
            }
//            if (!array) {
                ROSHelper.manageRobot(RobotCommand.MANAGE_STATUS_STOP)
//            }
            TaskNext.setToDo("0")
            Universal.selectMapPoint = false
//            if (array) {
//                BillManager.currentBill()?.executeNextTask()
//            }
        }
    }

    fun downTimer() {
        countDownTimer = MyCountDownTimer(
            millisInFuture = QuerySql.QueryExplainConfig().stayTime * 1000L, // 倒计时总时长，单位为毫秒
            countDownInterval = 1000, // 倒计时间隔，单位为毫秒
            onTick = { millisUntilFinished ->
                // 更新 UI，显示剩余时间
                val seconds = millisUntilFinished / 1000
                LogUtil.i("倒计时器：${seconds}s")
//                if (seconds <= 1){
//                    RobotStatus.downTime.postValue(true)
//                }
            },
            onFinish = {
                RobotStatus.progress.postValue(0)
                TaskNext.setToDo("1")
            }
        )
    }

    /**
     * 路径加入列队方法
     */
    fun start() {
        if (inForListData().isNullOrEmpty()) return
        ExplainManager.routes = inForListData()
        BillManager.billList().clear()
        mainScope.launch(Dispatchers.Default) {
            for (data in inForListData()!!) {
                val taskModel = TaskModel(
                    location = dao.queryPoint(data?.name?:""),
                )
                val bill = ExplanationBillFactory.createBill(taskModel = taskModel)
//                BillManager.addAllLast(bill)
                BillManager.addAllLast(bill)
//                Universal.Model = "开始讲解"
            }
            BillManager.currentBill()?.executeNextTask()
            LogUtil.d("任务长度："+ BillManager.billList().size)
            if (inForListData()!!.size != BillManager.billList().size){
                LogUtil.d("正在重新添加："+ BillManager.billList().size)
                start()
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
     * 任务上报
     */
    fun getTask( enum: TaskStageEnum =TaskStageEnum.FinishChannelBroadcast ){
        reportTaskDto(
            Objects.requireNonNull(
                Objects.requireNonNull(
                    BillManager.currentBill()
                )?.currentTask()
            )?.taskModel(),
            enum,
            UpdateReturn.taskDto()
        )

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

    //下一个任务
    fun nextTask() {
        DialogHelper.loadingDialog.show()
        mainScope.launch {
            MediaPlayerHelper.getInstance().stop()
            SpeakHelper.releaseUserCallback()
            SpeakHelper.stop()
            countDownTimer?.pause()
            BillManager.currentBill()?.earlyFinish()
            ROSHelper.manageRobot(RobotCommand.MANAGE_STATUS_STOP)
            TaskNext.setToDo("0")
        }
    }

    fun cancelMainScope() {
        mainScope.cancel()
    }
}



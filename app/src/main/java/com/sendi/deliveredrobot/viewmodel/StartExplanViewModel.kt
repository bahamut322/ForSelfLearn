package com.sendi.deliveredrobot.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.text.SpannableStringBuilder
import android.widget.ImageView
import androidx.lifecycle.ViewModel
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.RobotCommand
import com.sendi.deliveredrobot.helpers.AudioMngHelper
import com.sendi.deliveredrobot.helpers.ROSHelper
import com.sendi.deliveredrobot.model.ExplanationTraceModel
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.BillManager.addAllAtIndex
import com.sendi.deliveredrobot.navigationtask.BillManager.currentBill
import com.sendi.deliveredrobot.navigationtask.ExplanationBill.createBill
import com.sendi.deliveredrobot.room.entity.QueryPointEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.pow


class StartExplanViewModel : ViewModel() {
    var mDatas: ArrayList<ExplanationTraceModel?>? = ArrayList()
    lateinit var mainScope: CoroutineScope
    private var videoAudio : Int = 0


    fun infoList() {
        mDatas = ArrayList()
        val explanation = ExplanationTraceModel()
        //模拟数据1
        explanation.pointName = QueryPointEntity(
            floorName = "1",
            name = "map-0209-1",
            pointDirection = "左右",
            pointId = 877,
            pointName = "room1",
            rootMapId = 13,
            routeId = 42,
            routePath = "path_2022-12-07_15-49-05.txt",
            subMapId = 56,
            subPath = "/home/rpdzkj/robot-data/laser_map/2022-12-07#15-40-13",
            w = 1.4441915648753874,
            x = -15.958836.toFloat(),
            y = -8.263622.toFloat(),
            elevator = null,
            publicAreaName = null,
            publicAreaId = null,
            type = 0,
            speakString = "广州市申迪计算机系统有限公司于1998年10月在广州市创立，是一家专业从事计算机软件开发、系统集成和技术服务的民营企业。公司已通过ISO9001质量管理体系认证、ISO20000 IT服务管理体系认证、ISO27001信息安全管理系统认证、ISO14001环境管理体系认证和ISO45001职业健康安全管理体系认证；被评定为国家高新技术企业，具有中国电子工业标准化技术协会颁发的ITSS等级资质和广东省网络空间安全协会颁发的计算机信息系统安全服务等级资质，并被认定为省市软件企业、广东省大数据培育企业，具有国际机构颁发的CMMI5认证证书。"
        )
        explanation.acceptStation =
            "广州市申迪计算机系统有限公司于1998年10月在广州市创立，是一家专业从事计算机软件开发、系统集成和技术服务的民营企业。公司已通过ISO9001质量管理体系认证、ISO20000 IT服务管理体系认证、ISO27001信息安全管理系统认证、ISO14001环境管理体系认证和ISO45001职业健康安全管理体系认证；被评定为国家高新技术企业，具有中国电子工业标准化技术协会颁发的ITSS等级资质和广东省网络空间安全协会颁发的计算机信息系统安全服务等级资质，并被认定为省市软件企业、广东省大数据培育企业，具有国际机构颁发的CMMI5认证证书。"
        explanation.pointImage =
            "http://172.168.201.34:9055/management_res//68/NDg1MDk2NDA4NTYzZGU5ZTYxZTA1MWUyMDY2YTE2YzU1ZDM0ODEuanBlZzE2Nzc4MTM3Nzg5Njk.jpeg"
        mDatas!!.add(explanation)

    }

    /**
     * 路径加入列队方法
     */
    fun start(){
        for (i in 0 until mDatas!!.size) {
            val taskModel = TaskModel(location = mDatas!![i]?.pointName, speakString = mDatas!![i]?.acceptStation)
            val bill = createBill(taskModel = taskModel)
            addAllAtIndex(bill, i)
            currentBill()!!.executeNextTask()
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



    fun  mainScope(){
        mainScope = MainScope()

    }
    //结束讲解
    fun finish(){
        mainScope.launch {
            currentBill()?.earlyFinish()
            ROSHelper.manageRobot(RobotCommand.MANAGE_STATUS_STOP)
        }

    }
    //下一个任务
    fun nextTask(){
        currentBill()?.executeNextTask()
    }
    fun cancelMainScope(){
        mainScope.cancel()
    }
    fun videoAudio() {
        val sp: SharedPreferences = MyApplication.instance!!.getSharedPreferences("data", Context.MODE_PRIVATE)
        videoAudio = sp.getFloat("videoAudio", 0f).toInt() // 视频音量
        AudioMngHelper(MyApplication.context).setVoice100(videoAudio)

    }
}



package com.sendi.deliveredrobot.helpers

import android.content.Context
import android.media.MediaScannerConnection
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.room.database.DataBaseDeliveredRobotMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * @describe 通用helper
 */
object CommonHelper {
    val dao = DataBaseDeliveredRobotMap.getDatabase(MyApplication.instance!!).getDao()

    fun getTimeSpan(it: Int, times: Float): SpannableString {
        val span = SpannableString("${it}s")
        span.setSpan(
            RelativeSizeSpan(times),
            0,
            span.length - 1,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return span
    }

    /**
     * @param startOffset 开始位置
     * @param string1 目标string
     * @param string2 全部string
     * @param color 目标color
     * @param color 目标缩放倍数
     */
    fun getTipsSpan(
        startOffset: Int,
        string1: String,
        string2: String,
        color: Int,
        times: Float = 1f
    ): SpannableStringBuilder {
        val spannable = SpannableStringBuilder(string2)
        val intLength = string1.length
        spannable.setSpan(
            ForegroundColorSpan(
                color
            ),
            startOffset, // start
            startOffset + intLength, // end
            Spannable.SPAN_EXCLUSIVE_INCLUSIVE
        )
        spannable.setSpan(
            RelativeSizeSpan(times),
            startOffset,
            startOffset + intLength,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return spannable
    }

    fun getPresentSpan(it: Int, times: Float): SpannableString {
        val span = SpannableString("$it %")
        span.setSpan(
            RelativeSizeSpan(times),
            0,
            span.length - 1,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return span
    }

    fun getBottomTextSpan(
        string1: String,
        string2: String = "",
        color: Int
    ): SpannableStringBuilder {
        val spannable = SpannableStringBuilder(string1 + string2)
        spannable.setSpan(
            ForegroundColorSpan(
                color
            ),
            string1.length, // start
            string1.length + string2.length, // end
            Spannable.SPAN_EXCLUSIVE_INCLUSIVE
        )
//        spannable.setSpan(
//            StyleSpan(
//                Typeface.BOLD
//            ),
//            string1.length, // start
//            string1.length + string2.length, // end
//            Spannable.SPAN_EXCLUSIVE_INCLUSIVE
//        )
        return spannable
    }

    /**
     * @describe 导入导出
     * @param string1 第一个变色的字符串
     * @param string2 不变色的字符串
     * @param string3 第二个变色的字符串
     */
    fun getExportImportTextSpan(
        string1: String,
        string2: String,
        string3: String = "",
        color: Int
    ): SpannableStringBuilder {
        val spannable = SpannableStringBuilder(string1 + string2 + string3)
        spannable.setSpan(
            ForegroundColorSpan(
                color
            ),
            0,
            string1.length,
            Spannable.SPAN_EXCLUSIVE_INCLUSIVE
        )
        spannable.setSpan(
            ForegroundColorSpan(
                color
            ),
            string1.length + string2.length,
            spannable.length,
            Spannable.SPAN_EXCLUSIVE_INCLUSIVE
        )
        return spannable
    }

    /**
     * @describe 两点间距离
     * @return 距离
     */
    fun getDistance(x1: Float, x2: Float, y1: Float, y2: Float): Float {
        return sqrt((x2 - x1).pow(2) + (y2 - y1).pow(2))
    }

    /**
     * @describe 删除机关图
     * @param string1 不变色
     * @param string2 变色
     * @param string3 不变色
     */
    fun getDeleteLaserMapTextSpan(
        string1: String,
        string2: String,
        string3: String,
        color:Int
    ): SpannableStringBuilder {
        val spannable = SpannableStringBuilder(string1 + string2 + string3)
        spannable.setSpan(
            ForegroundColorSpan(
                color
            ),
            string1.length,
            string1.length + string2.length,
            Spannable.SPAN_EXCLUSIVE_INCLUSIVE
        )
        return spannable
    }

    fun checkAuthProperties(context: Context, file: File){
        if (!file.exists()) {
            file.parentFile?.mkdirs()
            file.createNewFile()
            val inputStream: InputStream = context.assets.open("auth.properties")
            val outputStream: OutputStream = FileOutputStream(file)
            val byteArray = ByteArray(1024)
            var result: Int
            do {
                result = inputStream.read(byteArray, 0, byteArray.size)
                if (result > 0) {
                    outputStream.write(byteArray, 0, result)
                }
            } while (result != -1)
            inputStream.close()
            outputStream.close()
        }
        MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), arrayOf("text/plain")
        ) { _, _ -> }
    }

    /**
     * @describe 调整角度
     */
    fun adjustAngel(w: Double): Double {
        var tempW:Double = w
        if (tempW > Math.PI) {
            tempW -= 2 * Math.PI
        } else if (tempW < -Math.PI) {
            tempW += 2 * Math.PI
        }
        return tempW
    }

    fun checkLiftCommandProperties(context: Context, file: File){
        if (!file.exists()) {
            file.parentFile?.mkdirs()
            file.createNewFile()
            val inputStream: InputStream = context.assets.open("lift_command.properties")
            val outputStream: OutputStream = FileOutputStream(file)
            val byteArray = ByteArray(1024)
            var result: Int
            do {
                result = inputStream.read(byteArray, 0, byteArray.size)
                if (result > 0) {
                    outputStream.write(byteArray, 0, result)
                }
            } while (result != -1)
            inputStream.close()
            outputStream.close()
        }
        MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), arrayOf("text/plain")
        ) { _, _ -> }
    }

    suspend fun atChargePointFloor():Boolean {
        val result:Boolean
        withContext(Dispatchers.Default){
            val chargePoint = dao.queryChargePoint()
//            result = chargePoint?.floorCode == RobotStatus.currentLocation?.floorCode
            result = chargePoint?.floorName == RobotStatus.currentLocation?.floorName
        }
        return result
    }
}



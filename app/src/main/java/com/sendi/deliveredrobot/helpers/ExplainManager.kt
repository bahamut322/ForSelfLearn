package com.sendi.deliveredrobot.helpers

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.sendi.deliveredrobot.model.ExplainStatusModel
import com.sendi.deliveredrobot.model.MyResultModel
import com.sendi.deliveredrobot.model.SecondModel
import com.sendi.deliveredrobot.utils.LogUtil
import com.sendi.deliveredrobot.view.widget.Advance
import com.sendi.deliveredrobot.viewmodel.BaseViewModel
import java.io.File

/**
 * @author heky
 * @date 2024-03-27
 * @description 途径播报管理类
 */
object ExplainManager {
    var routes: ArrayList<MyResultModel?>? = null
    val explainStatusModel = MutableLiveData<ExplainStatusModel?>(null)


    fun getFilesAllName(path: String?, picType: Int, picPlayTime: Int): MutableList<Advance> {
        val fileList = ArrayList<Advance>()
        try {
            if (path.isNullOrEmpty()) return fileList
            val file = File(path)
            if (file.isFile()) {
                // This is a file
                if (BaseViewModel.checkIsImageFile(file.path)) {
                    fileList.add(Advance(file.path, "2", picType, picPlayTime)) // image
                } else {
                    fileList.add(Advance(file.path, "1", 1, picPlayTime)) // video
                }
                return fileList
            } else if (file.isDirectory()) {
                // This is a directory
                val files = file.listFiles()
                if (files != null) {
                    for (value in files) {
                        if (BaseViewModel.checkIsImageFile(value.path)) {
                            fileList.add(Advance(value.path, "2", picType, picPlayTime)) // image
                        } else {
                            fileList.add(Advance(value.path, "1", 1, picPlayTime)) // video
                        }
                    }
                    return fileList
                }
            }
        } catch (e: Exception) {
            Log.d("TAG", "轮播数据读取异常: $e")
            return fileList
        }
        return fileList
    }

    fun secondScreenModel(route: MyResultModel?) {
        var file = ""
        if (route?.big_videofile != null) {
            file = route.big_videofile.toString()
        } else if (route?.big_imagefile != null) {
            file = route.big_imagefile.toString()
        }
        SecondScreenManageHelper.refreshSecondScreen(
            SecondScreenManageHelper.STATE_EXPLAIN, SecondModel(
                picPlayTime = route?.big_picplaytime,
                file = file,
                type = route?.big_type ?: 0,
                textPosition = route?.big_textposition,
                fontLayout = route?.big_fontlayout,
                fontContent = route?.big_fontcontent?.toString(),
                fontBackGround = route?.big_fontbackground?.toString(),
                fontColor = route?.big_fontcolor?.toString(),
                fontSize = route?.big_fontsize,
                picType = route?.big_pictype,
                videolayout = route?.videolayout,
                videoAudio = route?.big_videoaudio,
                false
            )
        )
        LogUtil.i("图片位置：${route?.big_imagefile?.toString()}")
    }
}
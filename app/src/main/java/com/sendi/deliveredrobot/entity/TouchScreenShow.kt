package com.sendi.deliveredrobot.entity

import android.graphics.Color
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.sendi.deliveredrobot.view.widget.Advance
import com.sendi.deliveredrobot.view.widget.AdvanceView
import com.sendi.deliveredrobot.view.widget.VerticalTextView
import com.sendi.deliveredrobot.viewmodel.BaseViewModel
import java.io.File

/**
 * @Author Swn
 * @Data 2024/1/17
 * @describe
 */
class TouchScreenShow {
    private var layoutParamsVertical: ConstraintLayout.LayoutParams? = null
    private var layoutParamsHorizontal: ConstraintLayout.LayoutParams? = null
    /**
     * @param bgCon          背景颜色布局
     * @param verticalTV     垂直文字
     * @param horizontalTV   横向文字
     * @param pointImage     轮播图片/视频
     * @param picPlayTime    轮播时间
     * @param file           路径
     * @param type           类型： 1-图片 2-视频 6-文字 7-图片+文字
     * @param textPosition   文字x位置
     * @param fontLayout     文字方向：1-横向，2-纵向
     * @param fontContent    文字
     * @param fontBackGround 背景颜色
     * @param fontColor      文字颜色
     * @param fontSize       文字大小：1-大，2-中，3-小,
     * @param picType        图片样式
     */
     fun layoutThis(
        bgCon : View,
        verticalTV: VerticalTextView,
        horizontalTV : TextView,
        pointImage: AdvanceView,
        picPlayTime: Int?,
        file: String?,
        type: Int?,
        textPosition: Int?,
        fontLayout: Int?,
        fontContent: String?,
        fontBackGround: String?,
        fontColor: String?,
        fontSize: Int?,
        picType: Int?
    ) {
        when (type) {
            1, 2 -> {
                //读取文件
                getFilesAllName(file, picType!!, picPlayTime!!,pointImage)
                verticalTV.visibility = View.GONE
                horizontalTV.visibility = View.GONE
                pointImage.visibility = View.VISIBLE
            }

            6 -> {
                pointImage.visibility = View.GONE
                layoutParamsVertical =
                    verticalTV.layoutParams as ConstraintLayout.LayoutParams
                layoutParamsHorizontal =
                    horizontalTV.layoutParams as ConstraintLayout.LayoutParams
                when (textPosition) {
                    0 -> {
//                        binding.horizontalTV.gravity = Gravity.CENTER //居中
                        textLayoutThis(
                            bgCon ,
                            verticalTV,
                            horizontalTV,
                            fontLayout!!,
                            fontContent!!,
                            fontBackGround!!,
                            fontColor!!,
                            fontSize!!
                        )
                    }

                    1 -> {
//                        binding.horizontalTV.gravity = Gravity.TOP //居上
                        layoutParamsHorizontal!!.bottomToBottom =
                            ConstraintLayout.LayoutParams.UNSET
                        horizontalTV.layoutParams = layoutParamsHorizontal
                        layoutParamsVertical!!.bottomToBottom = ConstraintLayout.LayoutParams.UNSET
                        verticalTV.layoutParams = layoutParamsVertical
                        textLayoutThis(
                            bgCon ,
                            verticalTV,
                            horizontalTV,
                            fontLayout!!,
                            fontContent!!,
                            fontBackGround!!,
                            fontColor!!,
                            fontSize!!
                        )
                    }

                    2 -> {
//                        binding.horizontalTV.gravity = Gravity.BOTTOM //居下
                        layoutParamsHorizontal!!.topToTop = ConstraintLayout.LayoutParams.UNSET
                        horizontalTV.layoutParams = layoutParamsHorizontal
                        layoutParamsVertical!!.topToTop = ConstraintLayout.LayoutParams.UNSET
                        verticalTV.layoutParams = layoutParamsVertical
                        textLayoutThis(
                            bgCon ,
                            verticalTV,
                            horizontalTV,
                            fontLayout!!,
                            fontContent!!,
                            fontBackGround!!,
                            fontColor!!,
                            fontSize!!
                        )
                    }
                }
            }

            7 -> {
                //读取文件
                getFilesAllName(file, picType!!, picPlayTime!!,pointImage)
                layoutParamsVertical =
                   verticalTV.layoutParams as ConstraintLayout.LayoutParams
                layoutParamsHorizontal =
                    horizontalTV.layoutParams as ConstraintLayout.LayoutParams
                when (textPosition) {
                    0 -> {
                        horizontalTV.gravity = Gravity.CENTER //居中
                        textLayoutThis(
                            bgCon ,
                            verticalTV,
                            horizontalTV,
                            fontLayout!!,
                            fontContent!!,
                            fontBackGround!!,
                            fontColor!!,
                            fontSize!!
                        )
                    }

                    1 -> {
//                        binding.horizontalTV.gravity = Gravity.TOP //居上
                        layoutParamsHorizontal!!.bottomToBottom =
                            ConstraintLayout.LayoutParams.UNSET
                        horizontalTV.layoutParams = layoutParamsHorizontal
                        layoutParamsVertical!!.bottomToBottom = ConstraintLayout.LayoutParams.UNSET
                        verticalTV.layoutParams = layoutParamsVertical
                        textLayoutThis(
                            bgCon ,
                            verticalTV,
                            horizontalTV,
                            fontLayout!!,
                            fontContent!!,
                            fontBackGround!!,
                            fontColor!!,
                            fontSize!!,
                        )
                    }

                    2 -> {
//                        binding.horizontalTV.gravity = Gravity.BOTTOM //居下
                        layoutParamsHorizontal!!.topToTop = ConstraintLayout.LayoutParams.UNSET
                        horizontalTV.layoutParams = layoutParamsHorizontal
                        layoutParamsVertical!!.topToTop = ConstraintLayout.LayoutParams.UNSET
                        verticalTV.layoutParams = layoutParamsVertical
                        textLayoutThis(
                            bgCon ,
                            verticalTV,
                            horizontalTV,
                            fontLayout!!,
                            fontContent!!,
                            fontBackGround!!,
                            fontColor!!,
                            fontSize!!
                        )
                    }
                }
                pointImage.visibility = View.VISIBLE
            }
        }
    }
    /**
     * @param fontLayout     文字方向：1-横向，2-纵向
     * @param fontContent    文字
     * @param fontBackGround 背景颜色
     * @param fontColor      文字颜色
     * @param fontSize       文字大小：1-大，2-中，3-小,
     */
    private fun textLayoutThis(
        bgCon : View,
        verticalTV: VerticalTextView,
        horizontalTV : TextView,
        fontLayout: Int,
        fontContent: String,
        fontBackGround: String,
        fontColor: String,
        fontSize: Int
    ) {

        //横向
        if (fontLayout == 1) {
            //隐藏纵向文字，显示横向文字
            verticalTV.visibility = View.GONE
            horizontalTV.visibility = View.VISIBLE
            //显示内容
            horizontalTV.text = BaseViewModel().getLength(fontContent)
            //背景颜色&图片
            bgCon.setBackgroundColor(Color.parseColor(fontBackGround + ""))
            //文字颜色
            horizontalTV.setTextColor(Color.parseColor(fontColor + ""))
            //字体大小
            when (fontSize) {
                1 -> {
                    horizontalTV.textSize = 30F
                }

                2 -> {
                    horizontalTV.textSize = 20F
                }

                3 -> {
                    horizontalTV.textSize = 10F
                }
            }
        } else {
            //纵向
            //隐藏横向文字，显示纵向文字
            verticalTV.visibility = View.VISIBLE
            horizontalTV.visibility = View.GONE
            //显示内容
            verticalTV.text = fontContent
            //背景颜色
            bgCon.setBackgroundColor(Color.parseColor(fontBackGround + ""))
            //文字颜色
            verticalTV.textColor = Color.parseColor(fontColor + "")
            //字体大小
            when (fontSize) {
                1 -> {
                   verticalTV.textSize = 30
                }

                2 -> {
                    verticalTV.textSize = 20
                }

                3 -> {
                   verticalTV.textSize = 10
                }
            }
        }
    }

    private fun getFilesAllName(path: String?, picType: Int, picPlayTime: Int, pointImage: AdvanceView,) {
        try {
            val file = File(path!!)
            if (file.isFile) {
                // This is a file
                val fileList: MutableList<Advance> = ArrayList()
                if (BaseViewModel.checkIsImageFile(file.path)) {
                    fileList.add(Advance(file.path, "2", picType, picPlayTime)) // image
                } else {
                    fileList.add(Advance(file.path, "1", 1, picPlayTime)) // video
                }
                pointImage.setData(fileList)
            } else if (file.isDirectory) {
                // This is a directory
                val files = file.listFiles()
                if (files != null) {
                    val fileList: MutableList<Advance> = ArrayList()
                    for (value in files) {
                        if (BaseViewModel.checkIsImageFile(value.path)) {
                            fileList.add(Advance(value.path, "2", picType, picPlayTime)) // image
                        } else {
                            fileList.add(Advance(value.path, "1", 1, picPlayTime)) // video
                        }
                    }
                    pointImage.setData(fileList)
                }
            }
        } catch (e: Exception) {
            Log.d("TAG", "轮播数据读取异常: $e")
        }
    }

}
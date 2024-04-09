package com.sendi.deliveredrobot.view.widget
import android.app.Presentation
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Display
import android.view.Gravity
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.entity.Universal
import com.sendi.deliveredrobot.entity.entitySql.QuerySql
import com.sendi.deliveredrobot.helpers.AudioMngHelper
import com.sendi.deliveredrobot.viewmodel.BaseViewModel
import java.io.File
import java.io.IOException

class MyPresentation(context: Context?, display: Display?) :
    Presentation(context, display) {
    var frameLayout: ConstraintLayout? = null //副屏Fragment
    var advanceView: AdvanceView? = null //轮播图&视频控件
    var horizontalTV: TextView? = null //横向文字
    var verticalTV: VerticalTextView? = null //纵向文字
    var constraintLayout2: ConstraintLayout? = null //布局
    private var layoutParamsVertical: ConstraintLayout.LayoutParams? = null
    private var layoutParamsHorizontal: ConstraintLayout.LayoutParams? = null

    //副屏的生命周期
    override fun onCreate(savedInstanceState: Bundle?) {
        // Be sure to call the super class.
        super.onCreate(savedInstanceState)
        // Get the resources for the context of the presentation.
        // Notice that we are getting the resources from the context of the presentation.
//            Resources resources = getContext().getResources();
//            mDisplayManager.getDisplay(90);
        // Inflate the layout.
        setContentView(R.layout.presentation_content)
        frameLayout = findViewById(R.id.frameLayout)
        constraintLayout2 = findViewById(R.id.constraintLayout2)
        advanceView = findViewById(R.id.Spread_out)
        horizontalTV = findViewById(R.id.horizontalTV) //横向文字
        verticalTV = findViewById(R.id.verticalTV) //纵向文字
        //          AdvancePagerAdapter.time = Universal.picPlayTime;
        //一定要在副屏的生命中中设置一下音量，否则刷新副屏的时候默认为最大声音
        AudioMngHelper(MyApplication.context).setVoice100(QuerySql.QueryBasic().videoVolume)
        //将控件设置成副屏尺寸，并且旋转270度
        constraintLayout2?.viewTreeObserver?.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                // 移除监听，避免重复调用
                constraintLayout2?.viewTreeObserver?.removeOnGlobalLayoutListener(this)
                // 获取constraintLayout2的LayoutParams
                val params =
                    constraintLayout2?.layoutParams as ConstraintLayout.LayoutParams
                params.height = 1920
                params.width = 1080
                constraintLayout2?.layoutParams = params
                // 宽度和高度符合要求，进行旋转操作
                constraintLayout2?.rotation = 270f
            }
        })
        MediaStatusManager.setOnChangeListener {
            if (it) {
                advanceView?.mediaStop()
            } else {
                advanceView?.mediaRestart()
            }
        }
    }

    /**
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
     * @param videoLayout    视频显示样式
     * @param allVideoAudio  是否播放声音
     */
    fun layoutThis(
        picPlayTime: Int,
        file: String,
        type: Int,
        textPosition: Int,
        fontLayout: Int,
        fontContent: String,
        fontBackGround: String,
        fontColor: String,
        fontSize: Int,
        picType: Int,
        videoLayout: Int,
        allVideoAudio: Int,
        adv: Boolean
    ) {
        //重新恢复约束绑定
        layoutParamsHorizontal = horizontalTV!!.layoutParams as ConstraintLayout.LayoutParams
        layoutParamsHorizontal!!.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
        layoutParamsHorizontal!!.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
        horizontalTV!!.layoutParams = layoutParamsHorizontal
        layoutParamsVertical = verticalTV!!.layoutParams as ConstraintLayout.LayoutParams
        layoutParamsVertical!!.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
        layoutParamsVertical!!.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
        verticalTV!!.layoutParams = layoutParamsVertical
        when (type) {
            1, 2 -> {
                //读取文件
                try {
                    getFilesAllName(file, picPlayTime, picType, videoLayout, allVideoAudio)
                } catch (ignored: IOException) {
                }
                verticalTV!!.visibility = View.GONE
                horizontalTV!!.visibility = View.GONE
                advanceView!!.visibility = View.VISIBLE
            }

            6 -> {
                advanceView!!.visibility = View.GONE
                try {
                    getFilesAllName(file, picPlayTime, picType, videoLayout, allVideoAudio)
                } catch (ignored: IOException) {
                }
                if (textPosition == 0) {
//                    horizontalTV.setGravity(Gravity.CENTER);//居中
                    textLayoutThis(
                        fontLayout,
                        fontContent,
                        fontBackGround,
                        fontColor,
                        fontSize,
                        adv
                    )
                } else if (textPosition == 1) {
//                    horizontalTV.setGravity(Gravity.TOP );//居上
                    layoutParamsHorizontal!!.bottomToBottom = ConstraintLayout.LayoutParams.UNSET
                    horizontalTV!!.layoutParams = layoutParamsHorizontal
                    layoutParamsVertical!!.bottomToBottom = ConstraintLayout.LayoutParams.UNSET
                    verticalTV!!.layoutParams = layoutParamsVertical
                    textLayoutThis(
                        fontLayout,
                        fontContent,
                        fontBackGround,
                        fontColor,
                        fontSize,
                        adv
                    )
                } else if (textPosition == 2) {
//                    horizontalTV.setGravity(Gravity.BOTTOM);//居下
                    layoutParamsHorizontal!!.topToTop = ConstraintLayout.LayoutParams.UNSET
                    horizontalTV!!.layoutParams = layoutParamsHorizontal
                    layoutParamsVertical!!.topToTop = ConstraintLayout.LayoutParams.UNSET
                    verticalTV!!.layoutParams = layoutParamsVertical
                    textLayoutThis(
                        fontLayout,
                        fontContent,
                        fontBackGround,
                        fontColor,
                        fontSize,
                        adv
                    )
                }
            }

            7 -> {
                //读取文件
                try {
                    getFilesAllName(file, picPlayTime, picType, videoLayout, allVideoAudio)
                } catch (ignored: IOException) {
                }
                if (textPosition == 0) {
                    horizontalTV!!.gravity = Gravity.CENTER //居中
                    textLayoutThis(
                        fontLayout,
                        fontContent,
                        fontBackGround,
                        fontColor,
                        fontSize,
                        adv
                    )
                } else if (textPosition == 1) {
//                    horizontalTV.setGravity(Gravity.TOP );//居上
                    layoutParamsHorizontal!!.bottomToBottom = ConstraintLayout.LayoutParams.UNSET
                    horizontalTV!!.layoutParams = layoutParamsHorizontal
                    layoutParamsVertical!!.bottomToBottom = ConstraintLayout.LayoutParams.UNSET
                    verticalTV!!.layoutParams = layoutParamsVertical
                    textLayoutThis(
                        fontLayout,
                        fontContent,
                        fontBackGround,
                        fontColor,
                        fontSize,
                        adv
                    )
                } else if (textPosition == 2) {
//                    horizontalTV.setGravity(Gravity.BOTTOM);//居下
                    layoutParamsHorizontal!!.topToTop = ConstraintLayout.LayoutParams.UNSET
                    horizontalTV!!.layoutParams = layoutParamsHorizontal
                    layoutParamsVertical!!.topToTop = ConstraintLayout.LayoutParams.UNSET
                    verticalTV!!.layoutParams = layoutParamsVertical
                    textLayoutThis(
                        fontLayout,
                        fontContent,
                        fontBackGround,
                        fontColor,
                        fontSize,
                        adv
                    )
                }
                advanceView!!.visibility = View.VISIBLE
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
        fontLayout: Int,
        fontContent: String,
        fontBackGround: String,
        fontColor: String,
        fontSize: Int,
        adv: Boolean
    ) {

        //横向
        if (fontLayout == 1) {
            //隐藏纵向文字，显示横向文字
            verticalTV!!.visibility = View.GONE
            horizontalTV!!.visibility = View.VISIBLE
            //显示内容
            horizontalTV!!.text = getLength(fontContent)
            //背景颜色&图片
            if (!adv) {
                horizontalTV!!.setBackgroundColor(Color.parseColor(fontBackGround + ""))
            } else {
                constraintLayout2!!.setBackgroundColor(Color.parseColor(fontBackGround + ""))
            }
            //文字颜色
            horizontalTV!!.setTextColor(Color.parseColor(fontColor + ""))
            //字体大小
            if (fontSize == 1) {
                horizontalTV!!.textSize = 90f
            } else if (fontSize == 2) {
                horizontalTV!!.textSize = 70f
            } else if (fontSize == 3) {
                horizontalTV!!.textSize = 50f
            }
        } else {
            //纵向
            //隐藏横向文字，显示纵向文字
            verticalTV!!.visibility = View.VISIBLE
            horizontalTV!!.visibility = View.GONE
            //显示内容
            verticalTV!!.text = fontContent
            //背景颜色
            if (!adv) {
                verticalTV!!.setBackgroundColor(Color.parseColor(fontBackGround + ""))
            } else {
                constraintLayout2!!.setBackgroundColor(Color.parseColor(fontBackGround + ""))
            }
            //文字颜色
            verticalTV!!.textColor = Color.parseColor(fontColor + "")
            //字体大小
            if (fontSize == 1) {
                verticalTV!!.textSize = 80
            } else if (fontSize == 2) {
                verticalTV!!.textSize = 60
            } else if (fontSize == 3) {
                verticalTV!!.textSize = 40
            }
        }
    }

    @Throws(IOException::class)
    private fun getFilesAllName(
        path: String,
        picPlayTime: Int,
        picType: Int,
        videoLayout: Int,
        allVideoAudio: Int
    ) {
//        Universal.time = picPlayTime;
        Universal.allVideoAudio = allVideoAudio
        try {
            val file = File(path)
                advanceView!!.removeAllViews()
                advanceView!!.initView()
            if (file.isFile) {
                // This is a file
                val fileList: MutableList<Advance> = ArrayList()
                if (BaseViewModel.checkIsImageFile(file.path)) {
                    fileList.add(Advance(file.path, "2", picType, picPlayTime)) // image
                } else {
                    fileList.add(Advance(file.path, "1", videoLayout, picPlayTime)) // video
                }
                advanceView!!.setData(fileList)
            } else if (file.isDirectory) {
                // This is a directory
                val files = file.listFiles()
                if (files != null) {
                    val fileList: MutableList<Advance> = ArrayList()
                    for (value in files) {
                        if (BaseViewModel.checkIsImageFile(value.path)) {
                            fileList.add(Advance(value.path, "2", picType, picPlayTime)) // image
                        } else {
                            fileList.add(
                                Advance(
                                    value.path,
                                    "1",
                                    videoLayout,
                                    picPlayTime
                                )
                            ) // video
                        }
                    }
                    advanceView!!.setData(fileList)
                }
            }
        } catch (ignored: Exception) {
        }
    }

    private fun getLength(string: String): String {
        //记录一共有多少位字符
        var valueLength = 0.0
        //中文编码
        val chinese = "[\u4e00-\u9fa5]"
        //定义一个StringBuffer存储数据
        val stringBuffer = StringBuilder()
        //遍历判断哪些是中文和非中文
        for (i in string.indices) {
            // 获取一个字符
            val temp = string.substring(i, i + 1)
            // 判断是否为中文字符
            valueLength += if (temp.matches(chinese.toRegex())) {
                // 中文字符长度为+1
                1.0
            } else {
                // 其他字符长度为+0.5
                0.5
            }
            //每个数据放入StringBuffer中
            stringBuffer.append(temp)
            //如果长度为5，开始换行
            if (valueLength >= 5) {
                stringBuffer.append("\n")
                //清空valueLength
                valueLength = 0.0
            }
        }
        //返回数据样式
        return String(stringBuffer)
    }
}
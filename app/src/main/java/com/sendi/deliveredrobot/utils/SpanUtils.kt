package com.sendi.deliveredrobot.utils

import android.content.Context
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.UnderlineSpan
import android.widget.TextView
import com.sendi.deliveredrobot.view.widget.CustomUrlSpan
import com.thanosfisherman.wifiutils.LocationUtils
import java.util.LinkedList
import java.util.regex.Matcher
import java.util.regex.Pattern


class SpanUtils(mContext: Context?) {

    private var mContext: Context? = null

    @Volatile
    private var uniqueInstance: SpanUtils? = null
    init {
        this.mContext = mContext
    }

    //采用Double CheckLock(DCL)实现单例
    fun getInstance(context: Context?): SpanUtils? {
        if (uniqueInstance == null) {
            synchronized(LocationUtils::class.java) {
                if (uniqueInstance == null) {
                    uniqueInstance = SpanUtils(context)
                }
            }
        }
        return uniqueInstance
    }

    /**
     * 拦截超链接
     * @param tv
     */
    fun interceptHyperLink(tv: TextView) {
        tv.movementMethod = LinkMovementMethod.getInstance()
        val text = tv.text
        val stringBuilder = identifyUrl(text)
        tv.text = stringBuilder
    }

    private var mStringList = LinkedList<String>()
    private var mUrlInfos = LinkedList<UrlInfo>()
    val pattern =
        "(((htt|ft|m)ps?):\\/\\/)?([\\da-z\\.-]+)\\.([a-z]{2,6})(:\\d{1,5})?([\\/\\w\\.-]*)*\\/?(#[\\S]+)?"
    var r: Pattern = Pattern.compile(pattern)
    var m: Matcher? = null
    var flag = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE

    class UrlInfo {
        var start = 0
        var end = 0
    }


    private fun identifyUrl(text: CharSequence?): SpannableStringBuilder {
        mStringList.clear()
        mUrlInfos.clear()
        val tempText = text ?: ""
        val contextText: CharSequence = tempText
        m = r.matcher(contextText)
        //匹配成功
        while (m?.find() == true) {
            //得到网址数
            val info = UrlInfo()
            info.start = m?.start()?: 0
            info.end = m?.end()?:0
            mStringList.add(m?.group()?:"")
            mUrlInfos.add(info)
        }
        return joinText(null, contextText)
    }

    /** 拼接文本  */
    private fun joinText(
        clickSpanText: CharSequence?,
        contentText: CharSequence
    ): SpannableStringBuilder {
        val spanBuilder: SpannableStringBuilder = if (clickSpanText != null) {
            SpannableStringBuilder(clickSpanText)
        } else {
            SpannableStringBuilder()
        }
        if (mStringList.size > 0) {
            //只有一个网址
            if (mStringList.size == 1) {
                val preStr = contentText.toString().substring(0, mUrlInfos[0].start)
                spanBuilder.append(preStr)
                val url = mStringList[0]
                val customUrlSpan = mContext?.let { CustomUrlSpan(it, url) }
                val start = spanBuilder.length
                spanBuilder.append(url, UnderlineSpan(), flag)
                val end = spanBuilder.length
                if (start >= 0 && end > 0 && end > start) {
                    spanBuilder.setSpan(customUrlSpan, start, end, flag)
                }
                val nextStr = contentText.toString().substring(mUrlInfos[0].end)
                spanBuilder.append(nextStr)
            } else {
                //有多个网址
                for (i in mStringList.indices) {
                    val url = mStringList[i]
                    val customUrlSpan = mContext?.let { CustomUrlSpan(it, url) }
                    if (i == 0) {
                        //拼接第1个span的前面文本
                        val headStr = contentText.toString().substring(0, mUrlInfos[0].start)
                        spanBuilder.append(headStr)
                    }
                    if (i == mStringList.size - 1) {
                        //拼接最后一个span的后面的文本
                        val start = spanBuilder.length
                        spanBuilder.append(mStringList[i], UnderlineSpan(), flag)
                        val end = spanBuilder.length
                        if (start >= 0 && end > 0 && end > start) {
                            spanBuilder.setSpan(customUrlSpan, start, end, flag)
                        }
                        val footStr = contentText.toString().substring(mUrlInfos[i].end)
                        spanBuilder.append(footStr)
                    }
                    if (i != mStringList.size - 1) {
                        //拼接两两span之间的文本
                        val start = spanBuilder.length
                        spanBuilder.append(mStringList[i], UnderlineSpan(), flag)
                        val end = spanBuilder.length
                        if (start >= 0 && end > 0 && end > start) {
                            spanBuilder.setSpan(customUrlSpan, start, end, flag)
                        }
                        val betweenStr = contentText.toString()
                            .substring(
                                mUrlInfos[i].end,
                                mUrlInfos[i + 1].start
                            )
                        spanBuilder.append(betweenStr)
                    }
                }
            }
        } else {
            spanBuilder.append(contentText)
        }
        return spanBuilder
    }

}
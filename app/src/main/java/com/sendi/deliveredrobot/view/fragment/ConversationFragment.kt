package com.sendi.deliveredrobot.view.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.plexpt.chatgpt.ChatGPT
import com.plexpt.chatgpt.util.Proxys
import com.sendi.deliveredrobot.ACTION_NAVIGATE
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.NAVIGATE_ID
import com.sendi.deliveredrobot.POP_BACK_STACK
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.databinding.FragmentConversationBinding
import com.sendi.deliveredrobot.helpers.SpeakHelper
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.view.widget.MyFlowLayout
import com.sendi.fooddeliveryrobot.VoiceRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.Proxy

/**
 * @author heky
 * @date 2023-10-24
 * @description 人机对话fragment
 */
class ConversationFragment : Fragment() {
    var binding: FragmentConversationBinding? = null
    val mainScope = MainScope()
    private var totalHeight: Int = 0
    private val proxy: Proxy? = Proxys.http("192.168.62.20", 1080)
    private val chatGPT: ChatGPT = ChatGPT.builder()
        .apiKey("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")
        .proxy(proxy)
        .apiHost("https://api.openai.com/") //反向代理地址
        .build()
        .init()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_conversation, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val voiceRecorder = VoiceRecorder.getInstance()
        voiceRecorder.callback = { conversation, pinyinString ->
            if (pinyinString.contains("TUICHU")) {
                MyApplication.instance!!.sendBroadcast(Intent().apply {
                    action = ACTION_NAVIGATE
                    putExtra(NAVIGATE_ID, POP_BACK_STACK)
                })
            } else {
                if (conversation.isNotEmpty() && !RobotStatus.ttsIsPlaying) {
                    mainScope.launch(Dispatchers.Main) {
                        if (RobotStatus.ttsIsPlaying) {
                            return@launch
                        }
                        val result = addConversationView(conversation)
                        if (result.isNullOrEmpty()) {
                            return@launch
                        }
                        withContext(Dispatchers.Default){
                            SpeakHelper.speakWithoutStop(result)
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("InflateParams")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)
        val myFlowLayout = view.findViewById<MyFlowLayout>(R.id.my_flow_layout)
        val questions = arrayOf(
            "广州天气如何?广州天气如何?广州天气如何?广州天气如何?广州天气如何?广州天气如何?广州天气如何?广州天气如何?广州天气如何?广州天气如何?",
            "如果美国星链进行干扰通信会怎样?",
            "在吗?",
            "中国电信什么时候成立，成立的意义又是什么？",
            "联通和电信哪个更适苹果？",
            "如果英国国星链进行干扰通信会怎样?",
            "上海天气如何?",
            "如果中国星链进行干扰通信会怎样?",
            "在吗?",
            "美国电信什么时候成立，成立的意义又是什么？",
            "联通和移动哪个更适苹果？",
            )



//        repeat(20) {
//            val textView = LayoutInflater.from(requireContext())
//                .inflate(R.layout.layout_conversation_text_view_left, null) as TextView
//            textView.text = "测".repeat(random.nextInt(2, 20))
//            textView.setOnClickListener {
//                mainScope.launch(Dispatchers.Main) {
//                    val result = addConversationView((it as TextView).text.toString())
//                    if (result.isNullOrEmpty()) {
//                        return@launch
//                    }
//                    withContext(Dispatchers.Default){
//                        SpeakHelper.speakWithoutStop(result)
//                    }
//                }
//            }
//            myFlowLayout.addView(textView)
//        }
        questions.forEach { text ->
            val linearLayoutCompat = LayoutInflater.from(requireContext())
                .inflate(R.layout.layout_conversation_text_view_left, null) as LinearLayoutCompat
            val textView = linearLayoutCompat.findViewById<TextView>(R.id.tv_content)
            val viewHead = linearLayoutCompat.findViewById<View>(R.id.view_head)
            viewHead.visibility = View.GONE
            textView.text = text
            linearLayoutCompat.setOnClickListener {
                mainScope.launch(Dispatchers.Main) {
                    val result = addConversationView(text)
                    if (result.isNullOrEmpty()) {
                        return@launch
                    }
                    withContext(Dispatchers.Default){
                        SpeakHelper.speakWithoutStop(result)
                    }
                }
            }
            myFlowLayout.addView(linearLayoutCompat)
        }
    }

    private suspend fun addConversationView(conversation: String): String? {
        binding?.flHome?.apply {
            setOnClickListener {
                MyApplication.instance!!.sendBroadcast(Intent().apply {
                    action = ACTION_NAVIGATE
                    putExtra(NAVIGATE_ID, POP_BACK_STACK)
                })
            }
        }
        binding?.group1?.apply {
            if (visibility == View.VISIBLE) {
                visibility = View.GONE
            }
        }
        binding?.group2?.apply {
            if (visibility == View.GONE) {
                visibility = View.VISIBLE
            }
        }
        binding?.linearLayoutConversation?.apply {
            val linearLayoutCompat = LayoutInflater.from(requireContext())
                .inflate(R.layout.layout_conversation_text_view_right, null) as LinearLayoutCompat
            val textView = linearLayoutCompat.findViewById<TextView>(R.id.tv_content)
            textView.text = conversation
            addView(linearLayoutCompat)
            val emptyView = View(requireContext()).apply {
                layoutParams = LinearLayoutCompat.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 96)
                }
            }
            addView(emptyView)
            linearLayoutCompat.post {
                linearLayoutCompat.layoutParams = LinearLayoutCompat.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.END
//                                setMargins(0,0,0,96)
                }
                totalHeight += (linearLayoutCompat.measuredHeight + 96 * 3)
                binding?.scrollViewConversation?.smoothScrollTo(0, totalHeight)
            }
            emptyView.post {
                totalHeight += (linearLayoutCompat.measuredHeight + 96 * 3)
                binding?.scrollViewConversation?.smoothScrollTo(0, totalHeight)
            }

            return withContext(Dispatchers.Default) {
                val res: String = chatGPT.chat(conversation)
                val linearLayoutCompat2 = LayoutInflater.from(requireContext())
                    .inflate(R.layout.layout_conversation_text_view_left, null) as LinearLayoutCompat
                val textView2 = linearLayoutCompat2.findViewById<TextView>(R.id.tv_content)
                textView2.text = res
                val emptyView2 = View(requireContext()).apply {
                    layoutParams = LinearLayoutCompat.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(0, 0, 0, 96)
                    }
                }
                withContext(Dispatchers.Main) {
                    addView(linearLayoutCompat2)
                    addView(emptyView2)
                    linearLayoutCompat2.post {
                        linearLayoutCompat2.layoutParams = LinearLayoutCompat.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        ).apply {
                            gravity = Gravity.START
//                                        setMargins(0,0,0,96)
                        }
                        totalHeight += (linearLayoutCompat2.measuredHeight + 96 * 3)
                        binding?.scrollViewConversation?.smoothScrollTo(0, totalHeight)
                    }
                    emptyView2.post {
                        totalHeight += (linearLayoutCompat2.measuredHeight + 96 * 3)
                        binding?.scrollViewConversation?.smoothScrollTo(0, totalHeight)
                    }
                }
                return@withContext res
            }
        }
        return null
    }
}
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
import com.sendi.deliveredrobot.ACTION_NAVIGATE
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.NAVIGATE_ID
import com.sendi.deliveredrobot.POP_BACK_STACK
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.databinding.FragmentConversationBinding
import com.sendi.deliveredrobot.helpers.ReplyIntentHelper
import com.sendi.deliveredrobot.helpers.ReplyQaConfigHelper
import com.sendi.deliveredrobot.helpers.SpeakHelper
import com.sendi.deliveredrobot.model.QueryIntentModel
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.service.CloudMqttService
import com.sendi.deliveredrobot.view.widget.MyFlowLayout
import com.sendi.fooddeliveryrobot.VoiceRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.Timer

/**
 * @author heky
 * @date 2023-10-24
 * @description 人机对话fragment
 */
class ConversationFragment : Fragment() {
    var binding: FragmentConversationBinding? = null
    val mainScope = MainScope()
    val timer = Timer()
    private var startTime: Long = 0
    private var totalHeight: Int = 0
//    private val proxy: Proxy? = Proxys.http("192.168.62.20", 1080)
//    private val chatGPT: ChatGPT = ChatGPT.builder()
//        .apiKey("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")
//        .proxy(proxy)
//        .apiHost("https://api.openai.com/") //反向代理地址
//        .build()
//        .init()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_conversation, container, false)
    }

    override fun onResume() {
        super.onResume()
        startTime = System.currentTimeMillis()
        timer.schedule(object : java.util.TimerTask() {
            override fun run() {
                val durationSeconds = (System.currentTimeMillis() - startTime) / 1000
                if(durationSeconds > 30){
                    MyApplication.instance!!.sendBroadcast(Intent().apply {
                        action = ACTION_NAVIGATE
                        putExtra(NAVIGATE_ID, POP_BACK_STACK)
                    })
                }
            }
        }, Date(),1000)
    }

    override fun onStop() {
        super.onStop()
        timer.cancel()
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
                        addQuestionView(conversation)
                        question(conversation, System.currentTimeMillis())
//                        val result = addConversationView(conversation)
//                        if (result.isNullOrEmpty()) {
//                            return@launch
//                        }
//                        withContext(Dispatchers.Default){
//                            SpeakHelper.speakWithoutStop(result)
//                        }
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
        ReplyQaConfigHelper.replyQaConfigLiveData.observe(viewLifecycleOwner) {
            it.forEach { text ->
                val linearLayoutCompat = LayoutInflater.from(requireContext())
                    .inflate(
                        R.layout.layout_conversation_text_view_left,
                        null
                    ) as LinearLayoutCompat
                val textView = linearLayoutCompat.findViewById<TextView>(R.id.tv_content)
                val viewHead = linearLayoutCompat.findViewById<View>(R.id.view_head)
                viewHead.visibility = View.GONE
                textView.text = text
                linearLayoutCompat.setOnClickListener {
                    mainScope.launch(Dispatchers.Main) {
                        addQuestionView(text)
                        question(text, System.currentTimeMillis())
                    }
                }
                myFlowLayout.addView(linearLayoutCompat)
            }
        }
        ReplyIntentHelper.replyIntentLiveData.observe(viewLifecycleOwner) {
            //addConversationView
            val answer = it.questionAnswer
            if (answer.isNullOrEmpty()) return@observe
            addAnswerView(answer)
            SpeakHelper.speakWithoutStop(answer)
        }

//        val questions = arrayOf(
//            "广州天气如何?广州天气如何?广州天气如何?广州天气如何?广州天气如何?广州天气如何?广州天气如何?广州天气如何?广州天气如何?广州天气如何?",
//            "如果美国星链进行干扰通信会怎样?",
//            "在吗?",
//            "中国电信什么时候成立，成立的意义又是什么？",
//            "联通和电信哪个更适苹果？",
//            "如果英国国星链进行干扰通信会怎样?",
//            "上海天气如何?",
//            "如果中国星链进行干扰通信会怎样?",
//            "在吗?",
//            "美国电信什么时候成立，成立的意义又是什么？",
//            "联通和移动哪个更适苹果？",
//        )
//        questions.forEach { text ->
//            val linearLayoutCompat = LayoutInflater.from(requireContext())
//                .inflate(R.layout.layout_conversation_text_view_left, null) as LinearLayoutCompat
//            val textView = linearLayoutCompat.findViewById<TextView>(R.id.tv_content)
//            val viewHead = linearLayoutCompat.findViewById<View>(R.id.view_head)
//            viewHead.visibility = View.GONE
//            textView.text = text
//            linearLayoutCompat.setOnClickListener {
//                mainScope.launch(Dispatchers.Main) {
//                    val result = addConversationView(text)
//                    if (result.isNullOrEmpty()) {
//                        return@launch
//                    }
//                    withContext(Dispatchers.Default){
//                        SpeakHelper.speakWithoutStop(result)
//                    }
//                }
//            }
//            myFlowLayout.addView(linearLayoutCompat)
//        }
        binding?.flHome?.apply {
            setOnClickListener {
                MyApplication.instance!!.sendBroadcast(Intent().apply {
                    action = ACTION_NAVIGATE
                    putExtra(NAVIGATE_ID, POP_BACK_STACK)
                })
            }
        }
    }

    private fun addQuestionView(conversation: String) {
        startTime = System.currentTimeMillis()
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
                }
                totalHeight += (linearLayoutCompat.measuredHeight + 96 * 3)
                binding?.scrollViewConversation?.smoothScrollTo(0, totalHeight)
            }
            emptyView.post {
                totalHeight += (linearLayoutCompat.measuredHeight + 96 * 3)
                binding?.scrollViewConversation?.smoothScrollTo(0, totalHeight)
            }
        }
    }

    private fun addAnswerView(answer: String) {
        val linearLayoutCompat = LayoutInflater.from(requireContext())
            .inflate(R.layout.layout_conversation_text_view_left, null) as LinearLayoutCompat
        val textView = linearLayoutCompat.findViewById<TextView>(R.id.tv_content)
        textView.text = answer
        val emptyView = View(requireContext()).apply {
            layoutParams = LinearLayoutCompat.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 96)
            }
        }
        binding?.linearLayoutConversation?.apply {
            addView(linearLayoutCompat)
            addView(emptyView)
            linearLayoutCompat.post {
                linearLayoutCompat.layoutParams = LinearLayoutCompat.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.START
                }
                totalHeight += (linearLayoutCompat.measuredHeight + 96 * 3)
                binding?.scrollViewConversation?.smoothScrollTo(0, totalHeight)
            }
            emptyView.post {
                totalHeight += (linearLayoutCompat.measuredHeight + 96 * 3)
                binding?.scrollViewConversation?.smoothScrollTo(0, totalHeight)
            }
        }
    }

    private suspend fun question(question: String, questionNumber: Long){
        withContext(Dispatchers.IO) {
            CloudMqttService.publish(QueryIntentModel(
                questionContent = question,
                questionNumber = questionNumber
            ).toString())
        }
    }
}
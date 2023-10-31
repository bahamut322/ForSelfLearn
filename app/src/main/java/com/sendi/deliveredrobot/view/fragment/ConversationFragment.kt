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
import com.sendi.deliveredrobot.view.widget.MyFlowLayout
import com.sendi.fooddeliveryrobot.VoiceRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.Proxy
import kotlin.random.Random

/**
 * @author heky
 * @date 2023-10-24
 * @description 人机对话fragment
 */
class ConversationFragment : Fragment() {
    var binding: FragmentConversationBinding? = null
    val mainScope = MainScope()
    var totalHeight:Int = 0
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
        val proxy: Proxy? = Proxys.http("192.168.62.20", 1080)
        val chatGPT: ChatGPT = ChatGPT.builder()
            .apiKey("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")
            .proxy(proxy)
            .apiHost("https://api.openai.com/") //反向代理地址
            .build()
            .init()
        val voiceRecorder = VoiceRecorder.getInstance()
        voiceRecorder.callback = {
                conversation, pinyinString ->
            if (pinyinString.contains("TUICHU")) {
                MyApplication.instance!!.sendBroadcast(Intent().apply {
                    action = ACTION_NAVIGATE
                    putExtra(NAVIGATE_ID, POP_BACK_STACK)
                })
            }else{
                if (conversation.isNotEmpty()){
                    binding?.group1?.apply {
                        if (visibility == View.VISIBLE) {
                            visibility = View.GONE
                        }
                    }
                    binding?.group2?.apply {
                        if(visibility == View.GONE){
                            visibility = View.VISIBLE
                        }
                    }
                    binding?.linearLayoutConversation?.apply {
                        val view = LayoutInflater.from(requireContext()).inflate(R.layout.layout_conversation_text_view_right,null) as TextView
                        view.text = conversation
                        addView(view)
                        val emptyView = View(requireContext()).apply {
                            layoutParams =  LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                                setMargins(0,0,0,96)
                            }
                        }
                        addView(emptyView)
                        view.post {
                            view.layoutParams = LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                                gravity = Gravity.END
//                                setMargins(0,0,0,96)
                            }
                            totalHeight += (view.measuredHeight + 96 * 3)
                            binding?.scrollViewConversation?.smoothScrollTo(0, totalHeight)
                        }
                        emptyView.post{
                            totalHeight += (view.measuredHeight + 96 * 3)
                            binding?.scrollViewConversation?.smoothScrollTo(0, totalHeight)
                        }

                        mainScope.launch(Dispatchers.Default) {
                            val res: String = chatGPT.chat(conversation)
                            val view2 = LayoutInflater.from(requireContext()).inflate(R.layout.layout_conversation_text_view_left,null) as TextView
                            view2.text = res
                            val emptyView2 = View(requireContext()).apply {
                                layoutParams =  LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                                    setMargins(0,0,0,96)
                                }
                            }
                            withContext(Dispatchers.Main){
                                addView(view2)
                                addView(emptyView2)
                                view2.post {
                                    view2.layoutParams = LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                                        gravity = Gravity.START
//                                        setMargins(0,0,0,96)
                                    }
                                    totalHeight += (view2.measuredHeight +  96 * 3)
                                    binding?.scrollViewConversation?.smoothScrollTo(0, totalHeight)
                                }
                                emptyView2.post{
                                    totalHeight += (view.measuredHeight + 96 * 3)
                                    binding?.scrollViewConversation?.smoothScrollTo(0, totalHeight)
                                }
                            }
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
        repeat(20){
            val view = LayoutInflater.from(requireContext()).inflate(R.layout.layout_conversation_text_view_left,null) as TextView
            view.text = "测".repeat(random.nextInt(2,20))
            myFlowLayout.addView(view)
        }
//        binding?.scrollViewConversation?.apply {
//            fullScroll(ScrollView.FOCUS_DOWN)
//        }
    }

    companion object{
        val random = Random(1012312512515L)
    }
}
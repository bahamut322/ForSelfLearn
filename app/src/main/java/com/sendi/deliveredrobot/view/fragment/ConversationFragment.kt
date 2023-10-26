package com.sendi.deliveredrobot.view.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.sendi.deliveredrobot.ACTION_NAVIGATE
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.NAVIGATE_ID
import com.sendi.deliveredrobot.POP_BACK_STACK
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.view.widget.MyFlowLayout
import com.sendi.fooddeliveryrobot.VoiceRecorder
import kotlin.random.Random

/**
 * @author heky
 * @date 2023-10-24
 * @description 人机对话fragment
 */
class ConversationFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_conversation, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        voiceRecorder = VoiceRecorder{
//                _, pinyinString ->
//            if (pinyinString.contains("TUICHU")) {
//                voiceRecorder?.stopRecording()
//                MyApplication.instance!!.sendBroadcast(Intent().apply {
//                    action = ACTION_NAVIGATE
//                    putExtra(NAVIGATE_ID, POP_BACK_STACK)
//                })
//            }
//        }
        val voiceRecorder = VoiceRecorder.getInstance()
        voiceRecorder.callback = {
                _, pinyinString ->
            if (pinyinString.contains("TUICHU")) {
                MyApplication.instance!!.sendBroadcast(Intent().apply {
                    action = ACTION_NAVIGATE
                    putExtra(NAVIGATE_ID, POP_BACK_STACK)
                })
            }
        }
    }

    @SuppressLint("InflateParams")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val myFlowLayout = view.findViewById<MyFlowLayout>(R.id.my_flow_layout)
        repeat(20){
            val view = LayoutInflater.from(requireContext()).inflate(R.layout.layout_conversation_text_view,null) as TextView
            view.text = "测".repeat(random.nextInt(2,20))
            myFlowLayout.addView(view)
        }
    }

    companion object{
        val random = Random(1012312512515L)
    }
}
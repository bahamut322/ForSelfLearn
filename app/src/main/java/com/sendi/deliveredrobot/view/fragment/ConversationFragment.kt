package com.sendi.deliveredrobot.view.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.SimpleTarget
import com.sendi.deliveredrobot.ACTION_NAVIGATE
import com.sendi.deliveredrobot.BuildConfig
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.NAVIGATE_ID
import com.sendi.deliveredrobot.POP_BACK_STACK
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.databinding.FragmentConversationBinding
import com.sendi.deliveredrobot.helpers.ReplyIntentHelper
import com.sendi.deliveredrobot.helpers.ReplyQaConfigHelper
import com.sendi.deliveredrobot.helpers.SpeakHelper
import com.sendi.deliveredrobot.model.QueryIntentModel
import com.sendi.deliveredrobot.model.ReplyIntentModel
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.service.CloudMqttService
import com.sendi.deliveredrobot.view.widget.MyFlowLayout
import com.sendi.fooddeliveryrobot.VoiceRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.SimpleTimeZone
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
    private var voiceRecorder: VoiceRecorder? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_conversation, container, false)
    }

    override fun onResume() {
        super.onResume()
        startTime = System.currentTimeMillis()
        timer.schedule(object : java.util.TimerTask() {
            override fun run() {
                if (RobotStatus.ttsIsPlaying) {
                    startTime = System.currentTimeMillis()
                }
                val durationSeconds = (System.currentTimeMillis() - startTime) / 1000
                if (durationSeconds > 30) {
                    MyApplication.instance!!.sendBroadcast(Intent().apply {
                        action = ACTION_NAVIGATE
                        putExtra(NAVIGATE_ID, POP_BACK_STACK)
                    })
                }
            }
        }, Date(), 1000)
        voiceRecorder = VoiceRecorder.getInstance()
        voiceRecorder?.recordCallback = { conversation, pinyinString ->
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
                    }
                }
            }
        }
        voiceRecorder?.talkingCallback = { talking ->
            when (talking) {
                true -> {
                    println("****talking")
                    startTime = System.currentTimeMillis()
                }

                false -> {
                    println("not talking")
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        timer.cancel()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SpeakHelper.stop()
    }

    @SuppressLint("InflateParams")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)
        val myFlowLayout = view.findViewById<MyFlowLayout>(R.id.my_flow_layout).apply {
            layoutParams = this.layoutParams.apply {
                setPadding(40, 0, 40, 0)
            }
        }
        mainScope.launch {
            val list = ReplyQaConfigHelper.queryReplyConfig()
            list.forEach { text ->
                val linearLayoutCompat = LayoutInflater.from(requireContext())
                    .inflate(
                        R.layout.layout_conversation_text_view_left,
                        null
                    ) as LinearLayoutCompat
                val textView = linearLayoutCompat.findViewById<TextView>(R.id.tv_content)
                val viewHead = linearLayoutCompat.findViewById<View>(R.id.view_head)
                linearLayoutCompat.visibility = View.VISIBLE
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
            if (it == null) return@observe
            //addConversationView
            addAnswerView(it)
            val answer = it.questionAnswer
            if (answer.isNullOrEmpty()) return@observe
            SpeakHelper.speakWithoutStop(answer)
        }

        binding?.flHome?.apply {
            setOnClickListener {
                MyApplication.instance!!.sendBroadcast(Intent().apply {
                    action = ACTION_NAVIGATE
                    putExtra(NAVIGATE_ID, POP_BACK_STACK)
                })
            }
        }
        binding?.viewClose?.apply {
            setOnClickListener {
                binding?.group3?.visibility = View.GONE
            }
        }
    }

    @SuppressLint("InflateParams")
    private fun addQuestionView(conversation: String) {
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
            val emptyView = View(requireContext()).apply {
                layoutParams = LinearLayoutCompat.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 96)
                }
            }
            linearLayoutCompat.post {
                linearLayoutCompat.layoutParams = LinearLayoutCompat.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.END
                }
                linearLayoutCompat.visibility = View.VISIBLE
                totalHeight += (linearLayoutCompat.measuredHeight + 96 * 3)
                binding?.scrollViewConversation?.smoothScrollTo(0, totalHeight)
            }
            emptyView.post {
                totalHeight += (linearLayoutCompat.measuredHeight + 96 * 3)
                binding?.scrollViewConversation?.smoothScrollTo(0, totalHeight)
            }
            addView(linearLayoutCompat)
            addView(emptyView)
        }
    }

    @SuppressLint("InflateParams")
    private fun addAnswerView(replyIntentModel: ReplyIntentModel) {
        val linearLayoutCompat = LayoutInflater.from(requireContext())
            .inflate(R.layout.layout_conversation_text_view_left, null) as LinearLayoutCompat
        val textView = linearLayoutCompat.findViewById<TextView>(R.id.tv_content)
        val linearLayoutContent =
            linearLayoutCompat.findViewById<LinearLayoutCompat>(R.id.linear_layout_content)
        textView.text = replyIntentModel.questionAnswer ?: ""
        val emptyView = View(requireContext()).apply {
            layoutParams = LinearLayoutCompat.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 96)
            }
        }
        val images = replyIntentModel.images
        images?.forEach { imagePath ->
            val imageView = ImageView(requireContext())
            imageView.apply {
                layoutParams = LinearLayoutCompat.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 16, 0, 0)
                    scaleType = ImageView.ScaleType.CENTER_CROP
                }
                setOnClickListener {
                    Glide.with(requireContext())
                        .asBitmap()
                        .load("${BuildConfig.HTTP_HOST}$imagePath")
//                        .preload()
                        .into(object :SimpleTarget<Bitmap>(){
                            override fun onResourceReady(
                                resource: Bitmap,
                                transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?
                            ) {
                                var finalWidth = resource.width
                                var finalHeight = resource.height
                                if (finalWidth > resources.displayMetrics.widthPixels){
                                    finalWidth = resources.displayMetrics.widthPixels
                                    finalHeight = resource.height * finalWidth / resource.width
                                }
                                if(finalHeight > resources.displayMetrics.heightPixels){
                                    finalHeight = resources.displayMetrics.heightPixels
                                    finalWidth = finalHeight * resource.width / resource.height
                                }
                                binding?.imageViewPreview?.layoutParams = binding?.imageViewPreview?.layoutParams.apply {
                                    this?.width = finalWidth
                                    this?.height = finalHeight
                                }
                                Glide.with(requireContext())
                                    .load("${BuildConfig.HTTP_HOST}$imagePath")
                                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                                    .override(finalWidth, finalHeight)
                                    .into(binding!!.imageViewPreview)
                                binding?.group3?.visibility = View.VISIBLE
                            }
                        })
                }
            }
            linearLayoutContent.addView(imageView)
            imageView.post {
                Glide.with(requireContext())
                    .load("${BuildConfig.HTTP_HOST}$imagePath")
                    .override(319, 240)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(imageView)
            }
        }
        val frames = replyIntentModel.frames
        frames?.forEach { framePath ->
            val frameLayout = LayoutInflater.from(requireContext())
                .inflate(R.layout.layout_video_thumbnail, null) as FrameLayout
            frameLayout.apply {
                layoutParams = LinearLayoutCompat.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 16, 0, 0)
                }
            }
            val imageView = frameLayout.findViewById<ImageView>(R.id.image_view_thumbnail)
            linearLayoutContent.addView(frameLayout)
            imageView.post {
                Glide.with(requireContext())
                    .load("${BuildConfig.HTTP_HOST}$framePath")
                    .override(320, 180)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(imageView)
            }
        }
        binding?.linearLayoutConversation?.apply {
            linearLayoutCompat.post {
                linearLayoutCompat.layoutParams = LinearLayoutCompat.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.START
                }
                linearLayoutCompat.visibility = View.VISIBLE
                totalHeight += (linearLayoutCompat.measuredHeight + 96 * 3)
                binding?.scrollViewConversation?.smoothScrollTo(0, totalHeight)
            }
            emptyView.post {
                totalHeight += (linearLayoutCompat.measuredHeight + 96 * 3)
                binding?.scrollViewConversation?.smoothScrollTo(0, totalHeight)
            }
            addView(linearLayoutCompat)
            addView(emptyView)
        }
        ReplyIntentHelper.replyIntentLiveData.value = null
    }

    private suspend fun question(question: String, questionNumber: Long) {
        withContext(Dispatchers.IO) {
            CloudMqttService.publish(
                QueryIntentModel(
                    questionContent = question,
                    questionNumber = questionNumber
                ).toString()
            )
        }
    }
}
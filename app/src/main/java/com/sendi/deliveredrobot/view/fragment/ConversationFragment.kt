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
import android.widget.SeekBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
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
import com.sendi.deliveredrobot.utils.GenerateReplyToX8Utils
import com.sendi.deliveredrobot.utils.SpanUtils
import com.sendi.deliveredrobot.view.widget.MyFlowLayout
import com.sendi.fooddeliveryrobot.BaseVoiceRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.Timer
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * @author heky
 * @date 2023-10-24
 * @description 人机对话fragment
 */
class ConversationFragment : Fragment() {
    var binding: FragmentConversationBinding? = null
    val mainScope = MainScope()
    lateinit var timer:Timer
    private var startTime: Long = 0
    private var totalHeight: Int = 0
    private var voiceRecorder: BaseVoiceRecorder? = null
    private var talkingView: LinearLayoutCompat? = null
    private val defaultTalkingStr = "...."
    private var talkingStr = defaultTalkingStr
        set(value) {
            field = ".".repeat(value.length % 4 + 1)
        }
    private var waitTalk = true
    val pattern =
        "(((htt|ft|m)ps?):\\/\\/)?([\\da-zA-Z\\.-]+)\\.?([a-z]{2,6})(:\\d{1,5})?([\\/\\w\\.-]*)*\\/?(#[\\S]+)?"
//    var guidePoint: QueryPointEntity? = null
//    val yesWords = arrayOf("是","是的","对","对的","好","好的","嗯","嗯嗯","恩","恩恩","可以","可以的","行","行的","行行","行行行","行行行行","行行行行行","行行行行行行","行行行行行行行","行行行行行行行行","行行行行行行行行行")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_conversation, container, false)
    }

    override fun onResume() {
        super.onResume()
        startTime = System.currentTimeMillis()
        timer = Timer()
        timer.schedule(object : java.util.TimerTask() {
            override fun run() {
                if (RobotStatus.ttsIsPlaying || binding?.videoView?.isPlaying == true) {
                    startTime = System.currentTimeMillis()
                }
                val durationSeconds = (System.currentTimeMillis() - startTime) / 1000
                if (durationSeconds > 30) {
                    MyApplication.instance!!.sendBroadcast(Intent().apply {
                        action = ACTION_NAVIGATE
                        putExtra(NAVIGATE_ID, POP_BACK_STACK)
                    })
                }
                binding?.seekBar?.progress = binding?.videoView?.currentPosition ?: 0
                if (talkingView != null && waitTalk) {
                    talkingStr = talkingStr
                    mainScope.launch(Dispatchers.Main) {
                        talkingView?.findViewById<TextView>(R.id.tv_content)?.text = talkingStr
                    }
                }
            }
        }, Date(), 1000)
        voiceRecorder = BaseVoiceRecorder.getInstance()
        voiceRecorder?.recordCallback = { conversation, pinyinString ->
            if (pinyinString.contains("TUICHU")) {
                MyApplication.instance!!.sendBroadcast(Intent().apply {
                    action = ACTION_NAVIGATE
                    putExtra(NAVIGATE_ID, POP_BACK_STACK)
                })
            } else {
                if (conversation.isNotEmpty() && !RobotStatus.ttsIsPlaying && !binding?.videoView?.isPlaying!!) {
                    mainScope.launch(Dispatchers.Main) {
                        if (RobotStatus.ttsIsPlaying) {
                            return@launch
                        }
                        if(binding?.videoView?.isPlaying == true){
                            return@launch
                        }
                        when (BaseVoiceRecorder.VOICE_RECORD_TYPE) {
                            BaseVoiceRecorder.VOICE_RECORD_TYPE_SENDI -> {
                                addQuestionView(conversation, talkingView)
                                question(conversation, System.currentTimeMillis())
                            }

                            BaseVoiceRecorder.VOICE_RECORD_TYPE_AIXIAOYUE -> {
                                addQuestionView(conversation, talkingView)
//                                if (conversation.startsWith("带我去")) {
//                                    withContext(Dispatchers.IO){
//                                        val pointName = conversation.substring(conversation.indexOf("带我去") + 3)
//                                        val point = DataBaseDeliveredRobotMap.getDatabase(requireContext()).getDao().queryPoint(pointName)
//                                        if (point == null) {
//                                            addAnswer2("没有找到【${pointName}】的位置")
//                                            return@withContext
//                                        }
//                                        guidePoint = point
//                                        val answer = "是否要带领到【${pointName}】"
//                                        addAnswer2(answer)
//                                        SpeakHelper.speakWithoutStop(answer)
//                                    }
//                                }else if(yesWords.contains(conversation)){
//                                    //引领到
//                                    withContext(Dispatchers.IO){
//                                        if (guidePoint != null) {
//                                            println("开始引领到【${guidePoint?.pointName?:""}】")
//                                            val bill = GuideTaskBillFactory.createBill(TaskModel(location = guidePoint))
//                                            BillManager.addAllAtIndex(bill)
//                                            ROSHelper.manageRobot(RobotCommand.MANAGE_STATUS_STOP)
//                                        }
//                                    }
//                                } else{
                                    withContext(Dispatchers.IO){
                                        var res = question2(conversation)
                                        if (res.isEmpty()) return@withContext
                                        addAnswer2(res)
                                        if (res.contains("我猜您可能对以下内容感兴趣")) {
                                            res = res.substringBefore("我猜您可能对以下内容感兴趣")
                                        }
                                        res = res.replace(Regex(pattern),"")
                                        SpeakHelper.speakWithoutStop(res)
                                    }
//                                }
                            }
                        }
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

        voiceRecorder?.recordStatusCallback = { startRecord ->
            when (startRecord) {
                true -> {
                    //开始录音
                    when (BaseVoiceRecorder.VOICE_RECORD_TYPE) {
                        BaseVoiceRecorder.VOICE_RECORD_TYPE_SENDI -> {
                            mainScope.launch(Dispatchers.Main) {
                                if (talkingView == null) {
                                    addQuestionView(defaultTalkingStr)
                                }
                            }
                        }

                        BaseVoiceRecorder.VOICE_RECORD_TYPE_AIXIAOYUE -> {
                            mainScope.launch(Dispatchers.Main) {
                                if (talkingView == null) {
                                    addQuestionView(defaultTalkingStr)
                                }
                            }
                        }
                    }
                }
                false -> {
                    //结束录音
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
                        when (BaseVoiceRecorder.VOICE_RECORD_TYPE) {
                            BaseVoiceRecorder.VOICE_RECORD_TYPE_SENDI -> {
                                addQuestionView(defaultTalkingStr)
                                addQuestionView(text, talkingView)
                                question(text, System.currentTimeMillis())
                            }

                            BaseVoiceRecorder.VOICE_RECORD_TYPE_AIXIAOYUE -> {
                                addQuestionView(defaultTalkingStr)
                                addQuestionView(text,talkingView)
                                withContext(Dispatchers.IO){
                                    var res = question2(text)
                                    if (res.isEmpty()) return@withContext
                                    addAnswer2(res)
                                    if (res.contains("我猜您可能对以下内容感兴趣")) {
                                        res = res.substringBefore("我猜您可能对以下内容感兴趣")
                                    }
                                    res = res.replace(Regex(pattern),"")
                                    SpeakHelper.speakWithoutStop(res)
                                }
                            }
                        }
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
                    SpeakHelper.stop()
                })
            }
        }
        binding?.viewClose?.apply {
            setOnClickListener {
                binding?.group3?.visibility = View.GONE
            }
        }
        binding?.viewClose2?.apply {
            setOnClickListener {
                binding?.group4?.visibility = View.GONE
                binding?.videoView?.stopPlayback()
            }
        }
        binding?.videoView?.apply {
            setOnClickListener {
                if (isPlaying){
                    binding?.viewPlay?.visibility = View.VISIBLE
                    pause()
                }else{
                    binding?.viewPlay?.visibility = View.GONE
                    start()
                }
            }
        }
    }

    @SuppressLint("InflateParams")
    private fun addQuestionView(conversation: String, view:LinearLayoutCompat? = null) {
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
            val linearLayoutCompat = when (view == null) {
                true -> {
                    waitTalk = true
                    LayoutInflater.from(requireContext())
                        .inflate(
                            R.layout.layout_conversation_text_view_right,
                            null
                        ) as LinearLayoutCompat
                }

                false -> {
                    waitTalk = false
                    view
                }
            }
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
            if (view == null) {
                addView(linearLayoutCompat)
                addView(emptyView)
            }
            linearLayoutCompat.post {
                linearLayoutCompat.layoutParams = LinearLayoutCompat.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.END
                    linearLayoutCompat.visibility = View.VISIBLE
                }
                totalHeight += (linearLayoutCompat.measuredHeight + 96 * 3)
                binding?.scrollViewConversation?.smoothScrollTo(0, totalHeight)
            }
            emptyView.post {
                totalHeight += (linearLayoutCompat.measuredHeight + 96 * 3)
                binding?.scrollViewConversation?.smoothScrollTo(0, totalHeight)
            }
            if (conversation == defaultTalkingStr) {
                talkingView = linearLayoutCompat
            }
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
        SpanUtils.interceptHyperLink(textView, findNavController())
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
        frames?.forEachIndexed { index, framePath ->
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
            val imageView = frameLayout.findViewById<ImageView>(R.id.image_view_thumbnail).apply {
                setOnClickListener {
                    val videoView = binding?.videoView
                    videoView?.apply {
                        setVideoPath("${BuildConfig.HTTP_HOST}${replyIntentModel.videos?.get(index)?:""}")
                        setOnPreparedListener {
                            it.isLooping = true
                            binding?.seekBar?.apply{
                                max = it.duration
                                setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
                                    override fun onProgressChanged(
                                        seekBar: SeekBar?,
                                        progress: Int,
                                        fromUser: Boolean
                                    ) {
                                        if (fromUser){
                                            it.seekTo(progress)
                                        }
                                    }

                                    override fun onStartTrackingTouch(seekBar: SeekBar?) {

                                    }

                                    override fun onStopTrackingTouch(seekBar: SeekBar?) {

                                    }

                                })
                            }
                            post {
                                var finalWidth = 0
                                var finalHeight = 0
                                layoutParams = layoutParams.apply {
                                    if(videoView.height > videoView.width){
                                        finalWidth = videoView.width * resources.displayMetrics.heightPixels / videoView.height
                                        finalWidth = when(finalWidth > resources.displayMetrics.widthPixels){
                                            true -> {
                                                finalHeight = resources.displayMetrics.widthPixels * videoView.height / videoView.width
                                                resources.displayMetrics.widthPixels
                                            }
                                            false -> {
                                                finalHeight = resources.displayMetrics.heightPixels
                                                finalWidth
                                            }
                                        }
                                        this.width = finalWidth
                                        this.height = finalHeight

                                    }
                                    if(videoView.width > videoView.height){
                                        finalWidth = resources.displayMetrics.widthPixels
                                        finalHeight = when((videoView.height * resources.displayMetrics.widthPixels / videoView.width) > resources.displayMetrics.heightPixels){
                                            true -> {
                                                finalWidth = resources.displayMetrics.heightPixels * videoView.width / videoView.height
                                                resources.displayMetrics.heightPixels
                                            }
                                            false -> {
                                                finalWidth = resources.displayMetrics.widthPixels
                                                videoView.height * resources.displayMetrics.widthPixels / videoView.width
                                            }
                                        }
                                        this.width = finalWidth
                                        this.height = finalHeight
                                    }
                                }
                                binding?.seekBar?.layoutParams = binding?.seekBar?.layoutParams.apply {
                                    this?.width = finalWidth
                                    this?.height = 64
                                }
                                start()
                            }
                        }
                    }
                    binding?.group4?.visibility = View.VISIBLE
                    binding?.viewPlay?.visibility = View.GONE
                }
            }
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
        talkingView = null
    }

    private suspend fun addAnswer2(conversation: String){
        binding?.linearLayoutConversation?.apply {
            val linearLayoutCompat = LayoutInflater.from(requireContext())
                .inflate(R.layout.layout_conversation_text_view_left, null) as LinearLayoutCompat
            val textView = linearLayoutCompat.findViewById<TextView>(R.id.tv_content)
            textView.text = conversation
            SpanUtils.interceptHyperLink(textView, findNavController())
            val emptyView2 = View(requireContext()).apply {
                layoutParams = LinearLayoutCompat.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 96)
                }
            }
            withContext(Dispatchers.Main) {
                addView(linearLayoutCompat)
                addView(emptyView2)
                linearLayoutCompat.post {
                    linearLayoutCompat.layoutParams = LinearLayoutCompat.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply {
                        gravity = Gravity.START
                        linearLayoutCompat.visibility = View.VISIBLE
//                                        setMargins(0,0,0,96)
                    }
                    totalHeight += (linearLayoutCompat.measuredHeight + 96 * 3)
                    binding?.scrollViewConversation?.smoothScrollTo(0, totalHeight)
                }
                emptyView2.post {
                    totalHeight += (linearLayoutCompat.measuredHeight + 96 * 3)
                    binding?.scrollViewConversation?.smoothScrollTo(0, totalHeight)
                }
            }
            talkingView = null
        }
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

    private suspend fun question2(conversation: String): String = suspendCoroutine {
        it.resume(GenerateReplyToX8Utils.generateReplyToX8(conversation))
    }

}
package com.sendi.deliveredrobot.view.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.iflytek.aiui.AIUIAgent
import com.iflytek.aiui.AIUIConstant
import com.iflytek.aiui.AIUIEvent
import com.iflytek.aiui.AIUIListener
import com.iflytek.vtncaetest.engine.AiuiEngine
import com.iflytek.vtncaetest.engine.EngineConstants
import com.iflytek.vtncaetest.recorder.AudioRecorder
import com.iflytek.vtncaetest.recorder.RecorderFactory
import com.iflytek.vtncaetest.recorder.SystemRecorder
import com.iflytek.vtncaetest.utils.CopyAssetsUtils
import com.iflytek.vtncaetest.utils.ErrorCode
import com.iflytek.vtncaetest.utils.FileUtil
import com.iflytek.vtncaetest.utils.StreamingAsrUtil
import com.iflytek.vtncaetest.utils.senselessWordUtil
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.databinding.FragmentConversationBinding
import com.sendi.deliveredrobot.helpers.ReplyQaConfigHelper
import com.sendi.deliveredrobot.helpers.SpeakHelper
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.utils.GenerateReplyToX8Utils
import com.sendi.deliveredrobot.utils.LogUtil
import com.sendi.deliveredrobot.utils.SpanUtils
import com.sendi.deliveredrobot.view.widget.MyFlowLayout
import com.sendi.fooddeliveryrobot.GetVFFileToTextModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.util.Date
import java.util.Timer
import java.util.concurrent.ConcurrentHashMap
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
    lateinit var timer: Timer
    private var startTime: Long = 0
    private var totalHeight: Int = 0
    private var talkingView: LinearLayoutCompat? = null
//    private val hashMap = ConcurrentHashMap<String, ReplyIntentModel>()
    private val hashMap = ConcurrentHashMap<String, Array<String>>()
    private var answerPriority: Array<Int>? = null
//    private val mutex = Mutex()
    private var waitTalk = true
//    private val pattern =
//        "(((htt|ft|m)ps?):\\/\\/)?([\\da-zA-Z\\.-]+)\\.?([a-z]{2,6})(:\\d{1,5})?([\\/\\w\\.-]*)*\\/?([#=][\\S]+)?"
//    val observable = Observer<ReplyIntentModel>{
//        mainScope.launch {
//            mutex.withLock {
//                if (context == null) return@launch
//                if (it == null) return@launch
//                //addConversationView
//                when(BaseVoiceRecorder.VOICE_RECORD_TYPE){
//                    BaseVoiceRecorder.VOICE_RECORD_TYPE_SENDI -> {
//                        addAnswerView(it)
//                        val answer = it.questionAnswer
//                        if (answer.isNullOrEmpty()) return@launch
//                        SpeakHelper.speakWithoutStop(answer)
//                    }
//                    BaseVoiceRecorder.VOICE_RECORD_TYPE_AIXIAOYUE -> {
//                        val replyIntentModel = hashMap["${it.questionNumber}"]
//                        when (replyIntentModel == null) {
//                            true -> {
//                                //如果艾小越没有返回，则缓存答案，等待艾小越返回后判断用哪个答案
//                                hashMap["${it.questionNumber ?: -1}"] = it
//                                LogUtil.i("艾小越没有返回")
//                            }
//                            false -> {
//                                //如果艾小越已经返回，则判断用哪个答案
//                                LogUtil.i("艾小越有返回")
//                                val finalReplyIntentModel = findFinalAnswer(it, replyIntentModel)
//                                if (finalReplyIntentModel == null) {
//                                    hashMap.remove("${it.questionNumber ?: -1}")
//                                }else{
//                                    mainScope.launch(Dispatchers.Main) {
//                                        addAnswerView(finalReplyIntentModel)
//                                    }
//                                    hashMap.remove("${finalReplyIntentModel.questionNumber}")
//                                    val answer = finalReplyIntentModel.questionAnswer
//                                    if (answer.isNullOrEmpty()) return@launch
//                                    SpeakHelper.speakWithoutStop(answer)
//                                }
//
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }

        private var aiuiListener: AIUIListener? =
            AIUIListener { event: AIUIEvent ->
                when (event.eventType) {
                    AIUIConstant.EVENT_CONNECTED_TO_SERVER -> {
                        val uid = event.data.getString("uid")
                        LogUtil.i("已连接服务器,uid：$uid")
                    }

                    AIUIConstant.EVENT_SERVER_DISCONNECTED -> LogUtil.i("与服务器断开连接")

                    AIUIConstant.EVENT_WAKEUP -> LogUtil.i("进入识别状态")

                    AIUIConstant.EVENT_RESULT -> {
                        //处理语音合成结果
                        if (event.info.contains("\"sub\":\"tts")) {
                            //保存云端的tts音频文件，用于离线播放
                            if (EngineConstants.saveTTS) {
                                val audio =
                                    event.data.getByteArray("0") //tts音频数据，16k，16bit格式
                                FileUtil.writeFile(
                                    audio,
                                    "/sdcard/tts.pcm"
                                )
                            }
                            //如果使用sdk的播放器合成，可以不用解析tts
                            return@AIUIListener
                        }
                        //处理识别结果
                        if (event.info.contains("\"sub\":\"iat")) {
                            val json = event.data.getByteArray("0").let {
                                val result = when(it == null){
                                    true -> null
                                    false -> String(it, StandardCharsets.UTF_8)
                                }
                                result
                            }?:return@AIUIListener
                            val cntJson = JSONObject(json)
                            val text = cntJson.getJSONObject("text")
                            //识别结果
                            val asrResult = StreamingAsrUtil.processIATResult(text)
                            if (!asrResult.isNullOrEmpty()) {
                                addQuestionView(asrResult, talkingView)
                            }
                            //最终识别结果
                            if (text.getBoolean("ls")) {
                                LogUtil.i("识别结果=$asrResult")
                                //sid是每次交互的id，提供给讯飞可以查云端音频和结果
                                val sid = event.data.getString("sid")
                                LogUtil.i("sid=$sid")
                            }
                        } else if (event.info.contains("\"sub\":\"nlp")) {
                            val json = event.data.getByteArray("0").let {
                                val result = when(it == null){
                                    true -> null
                                    false -> String(it, StandardCharsets.UTF_8)
                                }
                                result
                            }?:return@AIUIListener
                            try {
                                val cntJson = JSONObject(json)
                                val nlpResult = cntJson.getJSONObject("intent") ?: return@AIUIListener
                                //nlp无结果不处理
                                //如果只判断asr结果中的无意义词，若nlp先返回就可能被错误判断为无意义词
                                val asrResult = nlpResult.getString("text")
                                EngineConstants.meaningful =
                                    senselessWordUtil.isMeaningful_filter1word(asrResult)
                                //无意义词不处理
                                if (!EngineConstants.meaningful) {
                                    LogUtil.i("无意义词不处理")
                                    AiuiEngine.MSG_reset_wakeup()
                                    //在线语义结果,rc=0语义理解成功，rc≠0语义理解失败
                                    val answer = "无匹配结果，请换个方式提问"
                                    startTTS(answer)
                                    mainScope.launch {
                                        addAnswer2(answer)
                                    }
                                    return@AIUIListener
                                }
                                LogUtil.i(
                                    "nlp result :$nlpResult"
                                )
                                AiuiEngine.MSG_reset_wakeup()
                                //在线语义结果,rc=0语义理解成功，rc≠0语义理解失败
                                val answer = "${nlpResult.getJSONObject("answer")["text"]}"
                                startTTS(answer)
                                mainScope.launch {
                                    addAnswer2(answer)
                                }
                            }catch (e: JSONException){
                                LogUtil.i("nlpResult异常")
                            }
                        } else if (event.info.contains("\"sub\":\"tpp")) {
                            val json = event.data.getByteArray("0").let {
                                val result = when(it == null){
                                    true -> null
                                    false -> String(it, StandardCharsets.UTF_8)
                                }
                                result
                            }?:return@AIUIListener
                            val cntJson = JSONObject(json)
                            LogUtil.i(
                                "tpp后处理结果"
                            )
                            LogUtil.i(
                                cntJson.toString()
                            )
                        }
                    }

                    AIUIConstant.EVENT_ERROR -> {
                        LogUtil.i("错误码: " + event.arg1)
                        LogUtil.i("错误信息:" + event.info)
                        LogUtil.i(
                            """解决方案:${ErrorCode.getError(event.arg1)}错误解决详情参考：https://www.yuque.com/iflyaiui/zzoolv/igbuol"""
                        )
                        AiuiEngine.MSG_reset_wakeup()
                        //在线语义结果,rc=0语义理解成功，rc≠0语义理解失败
                        val answer = "发生异常，请重试...（错误码:${event.arg1} 错误信息:${event.info}）"
                        startTTS(answer)
                        mainScope.launch {
                            addAnswer2(answer)
                        }
//                    AiuiEngine.MSG_wakeup(EngineConstants.WAKEUPTYPE_VOICE)
                        talkingView = null
                    }

                    AIUIConstant.EVENT_VAD -> if (AIUIConstant.VAD_BOS == event.arg1) {
                        LogUtil.i(
                            "开始说话 vad_bos"
                        )
                        startTime = System.currentTimeMillis()
                    } else if (AIUIConstant.VAD_BOS_TIMEOUT == event.arg1) {
                        LogUtil.i(
                            "长时间不说话,前端点超时"
                        )
                    } else if (AIUIConstant.VAD_EOS == event.arg1) {
                        LogUtil.i(
                            "结束说话 vad_eos"
                        )
                        startTime = System.currentTimeMillis()
                    } else if (AIUIConstant.VAD_VOL == event.arg1) {
                        //回调太多，一般情况下可以注释掉
//                    Log.i(TAG, "vad vol,说话音量:" + event.arg2);
                    }

                    AIUIConstant.EVENT_SLEEP -> LogUtil.i(
                        "设备休眠"
                    )

                    AIUIConstant.EVENT_START_RECORD -> LogUtil.i(
                        "开始录音"
                    )

                    AIUIConstant.EVENT_STOP_RECORD -> stopRecord()
                    AIUIConstant.EVENT_STATE -> {
                        EngineConstants.mAIUIState = event.arg1
                        if (AIUIConstant.STATE_IDLE == EngineConstants.mAIUIState) {
                            // 闲置状态，AIUI未开启
                            LogUtil.i(
                                "aiui状态:STATE_IDLE"
                            )
                        } else if (AIUIConstant.STATE_READY == EngineConstants.mAIUIState) {
                            // AIUI已就绪，等待唤醒
                            LogUtil.i(
                                "aiui状态:STATE_READY"
                            )
                        } else if (AIUIConstant.STATE_WORKING == EngineConstants.mAIUIState) {
                            // AIUI工作中，可进行交互
                            LogUtil.i(
                                "aiui状态:STATE_WORKING"
                            )
                        }
                    }

                    AIUIConstant.EVENT_TTS -> {
                        when (event.arg1) {
                            AIUIConstant.TTS_SPEAK_BEGIN -> LogUtil.i(
                                "tts:开始播放"
                            )

                            AIUIConstant.TTS_SPEAK_PROGRESS -> {
                                startTime = System.currentTimeMillis()
                            }
                            AIUIConstant.TTS_SPEAK_PAUSED -> LogUtil.i(
                                "tts:暂停播放"
                            )

                            AIUIConstant.TTS_SPEAK_RESUMED ->LogUtil.i(
                                "tts:恢复播放"
                            )

                            AIUIConstant.TTS_SPEAK_COMPLETED -> {
                                LogUtil.i(
                                    "tts:播放完成"
                                )
                                AiuiEngine.MSG_wakeup(EngineConstants.WAKEUPTYPE_VOICE)
                                talkingView = null
                            }

                            else -> {}
                        }
                    }

                    else -> {}
                }
            }

        private var recorder: AudioRecorder? = null
        private var mAIUIAgent: AIUIAgent? = null

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            LogUtil.i("conversation onCreateView")
            return inflater.inflate(R.layout.fragment_conversation, container, false)
        }

        override fun onResume() {
            super.onResume()
            LogUtil.i("conversation onResume")
            startTime = System.currentTimeMillis()
            timer = Timer()
            timer.schedule(object : java.util.TimerTask() {
                override fun run() {
                    if (RobotStatus.ttsIsPlaying || binding?.videoView?.isPlaying == true) {
                        startTime = System.currentTimeMillis()
                    }
                    val durationSeconds = (System.currentTimeMillis() - startTime) / 1000
                    if (durationSeconds > 30) {
                        quitFragment()
                        findNavController().popBackStack(R.id.homeFragment,false)
                    }
                }
            }, Date(), 1000)
//        initVoiceRecord()
            Handler().postDelayed({
                LogUtil.i("conversation initSDK")
                // 资源拷贝
                CopyAssetsUtils.portingFile(requireContext())
                initSDK()
                startRecord()
            }, 1000)
            SpeakHelper.speakUserCallback = object : SpeakHelper.SpeakUserCallback {
                override fun speakAllFinish() {
                    LogUtil.i("tts:播放完成")
                    AiuiEngine.MSG_wakeup(EngineConstants.WAKEUPTYPE_VOICE)
                    talkingView = null
                }

                override fun progressChange(utteranceId: String, progress: Int) {
                    startTime = System.currentTimeMillis()
                }
            }

        }

        override fun onStart() {
            super.onStart()
            LogUtil.i("conversation onStart")
        }

        override fun onStop() {
            super.onStop()
            timer.cancel()
        }

        @SuppressLint("InflateParams")
        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            LogUtil.i("conversation onViewCreated")
            binding = DataBindingUtil.bind(view)
            val myFlowLayout = view.findViewById<MyFlowLayout>(R.id.my_flow_layout).apply {
                layoutParams = this.layoutParams.apply {
                    setPadding(40, 0, 40, 0)
                }
            }
            mainScope.launch {
                val list = ReplyQaConfigHelper.queryReplyConfig()
                list.forEach { text ->
                    val linearLayoutCompat = LayoutInflater.from(requireContext()).inflate(
                        R.layout.layout_conversation_text_view_left,
                        null
                    ) as LinearLayoutCompat
                    val textView = linearLayoutCompat.findViewById<TextView>(R.id.tv_content)
                    val viewHead = linearLayoutCompat.findViewById<View>(R.id.view_head)
                    linearLayoutCompat.visibility = View.VISIBLE
                    viewHead.visibility = View.GONE
                    textView.text = text
                    linearLayoutCompat.setOnClickListener {
//                        mainScope.launch(Dispatchers.Main) {
//                        mutex.withLock {
//                            when (BaseVoiceRecorder.VOICE_RECORD_TYPE) {
//                                BaseVoiceRecorder.VOICE_RECORD_TYPE_SENDI -> {
//                                    addQuestionView(defaultTalkingStr)
//                                    addQuestionView(text, talkingView)
//                                    question(text, System.currentTimeMillis())
//                                }
//
//                                BaseVoiceRecorder.VOICE_RECORD_TYPE_AIXIAOYUE -> {
//                                    addQuestionView(defaultTalkingStr)
//                                    addQuestionView(text,talkingView)
//                                    withContext(Dispatchers.IO){
//                                        val currentTimeMillis = System.currentTimeMillis()
//                                        question(text, currentTimeMillis)
//                                        val getVFFileToTextModel = question2(text, currentTimeMillis)
//                                        val axyAnswer = GenerateReplyToX8Utils.getReplyInfoModel(getVFFileToTextModel)
//                                        val finalAnswer = when (axyAnswer.code) {
//                                            200 -> axyAnswer //axy有答案
//                                            204 -> hashMap[axyAnswer.questionNumber?:"-1"] //axy无答案
//                                            else -> null
//                                        }
//                                        if (finalAnswer != null) {
//                                            addAnswerView(finalAnswer)
//                                            if (hashMap["${finalAnswer.questionNumber}"] == null) {
//                                                hashMap["${finalAnswer.questionNumber}"] = finalAnswer
//                                            }else{
//                                                hashMap.remove("${finalAnswer.questionNumber}")
//                                            }
//                                            var speakAnswer:String? = axyAnswer.questionAnswer
//                                            if (speakAnswer?.contains("我猜您可能对以下内容感兴趣") == true) {
//                                                speakAnswer = speakAnswer.substringBefore("我猜您可能对以下内容感兴趣")
//                                            }
//                                            speakAnswer = speakAnswer?.replace(Regex(pattern),"")?:""
//                                            SpeakHelper.speakWithoutStop(speakAnswer)
//                                        }else{
//                                            hashMap["${axyAnswer.questionNumber ?:-1}"] = axyAnswer
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                        }
                        AiuiEngine.MSG_sendTextForNlp(text, "main")
                        addQuestionView(text)
                    }
                    myFlowLayout.addView(linearLayoutCompat)
                }
            }
            binding?.flHome?.apply {
                setOnClickListener {
                    quitFragment()
//                MyApplication.instance!!.sendBroadcast(Intent().apply {
//                    action = ACTION_NAVIGATE
//                    putExtra(NAVIGATE_ID, NAVIGATE_TO_HOME)
////                    SpeakHelper.stop()
//                })
                    findNavController().popBackStack(R.id.homeFragment,false)
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
                        LayoutInflater.from(requireContext()).inflate(
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
                talkingView = linearLayoutCompat
            }
        }

        private suspend fun addAnswer2(conversation: String){
            LogUtil.i("回复--->$conversation")
            binding?.linearLayoutConversation?.apply {
                val linearLayoutCompat = LayoutInflater.from(requireContext()).inflate(R.layout.layout_conversation_text_view_left, null) as LinearLayoutCompat
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
            }
        }

        private suspend fun question2(conversation: String, questionNumber: Long): GetVFFileToTextModel? = suspendCoroutine {
            LogUtil.i("提问--->$conversation")
            it.resume(GenerateReplyToX8Utils.generateReplyToX8(conversation, questionID ="$questionNumber"))
        }

//    private fun initVoiceRecord(){
//        val voiceRecorder = BaseVoiceRecorder.getInstance()
//        voiceRecorder?.clearCache()
//        LogUtil.i("conversation 设置recordCallback")
//        voiceRecorder?.recordCallback = { conversation, pinyinString ,takeTime->
//            LogUtil.i("ASR耗时${takeTime}s")
//            if (pinyinString.contains("TUICHU")) {
//                MyApplication.instance!!.sendBroadcast(Intent().apply {
//                    action = ACTION_NAVIGATE
//                    putExtra(NAVIGATE_ID, POP_BACK_STACK)
//                })
//            } else {
//                if (conversation.isNotEmpty() && !RobotStatus.ttsIsPlaying && !binding?.videoView?.isPlaying!!) {
//                    mainScope.launch(Dispatchers.Main) {
//                        mutex.withLock {
//                            if (RobotStatus.ttsIsPlaying) {
//                                return@launch
//                            }
//                            if(binding?.videoView?.isPlaying == true){
//                                return@launch
//                            }
//                            when (BaseVoiceRecorder.VOICE_RECORD_TYPE) {
//                                BaseVoiceRecorder.VOICE_RECORD_TYPE_SENDI -> {
//                                    addQuestionView(conversation, talkingView)
//                                    question(conversation, System.currentTimeMillis())
//                                }
//
//                                BaseVoiceRecorder.VOICE_RECORD_TYPE_AIXIAOYUE -> {
//                                    addQuestionView(conversation, talkingView)
//                                    withContext(Dispatchers.IO){
//                                        val currentTimeMillis = System.currentTimeMillis()
//                                        question(conversation, currentTimeMillis)
//                                        val getVFFileToTextModel = question2(conversation, currentTimeMillis)
//                                        val axyAnswer = GenerateReplyToX8Utils.getReplyInfoModel(getVFFileToTextModel)
//                                        LogUtil.i("number-->${axyAnswer.questionNumber}")
//                                        val finalAnswer = when (axyAnswer.code) {
//                                            200 -> axyAnswer //axy有答案
//                                            204 -> hashMap["${axyAnswer.questionNumber?:-1}"] //axy无答案
//                                            else -> null
//                                        }
//                                        LogUtil.i("finalAnswer-->${finalAnswer.toString()}")
//                                        if (finalAnswer != null) {
//                                            mainScope.launch(Dispatchers.Main) {
//                                                addAnswerView(finalAnswer)
//                                            }
//                                            if (hashMap["${finalAnswer.questionNumber}"] == null) {
//                                                hashMap["${finalAnswer.questionNumber}"] = finalAnswer
//                                            }else{
//                                                hashMap.remove("${finalAnswer.questionNumber}")
//                                            }
//                                            var speakAnswer:String? = axyAnswer.questionAnswer
//                                            if (speakAnswer?.contains("我猜您可能对以下内容感兴趣") == true) {
//                                                speakAnswer = speakAnswer.substringBefore("我猜您可能对以下内容感兴趣")
//                                            }
//                                            speakAnswer = speakAnswer?.replace(Regex(pattern),"")?:""
//                                            SpeakHelper.speakWithoutStop(speakAnswer)
//                                        }else{
//                                            hashMap["${axyAnswer.questionNumber ?:-1}"] = axyAnswer
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        voiceRecorder?.talkingCallback = { talking ->
//            when (talking) {
//                true -> {
//                    println("****talking")
//                    startTime = System.currentTimeMillis()
//                }
//
//                false -> {
//                    println("not talking")
//                }
//            }
//        }

//        voiceRecorder?.recordStatusCallback = { startRecord ->
//            when (startRecord) {
//                true -> {
//                    //开始录音
//                    when (BaseVoiceRecorder.VOICE_RECORD_TYPE) {
//                        BaseVoiceRecorder.VOICE_RECORD_TYPE_SENDI -> {
//                            mainScope.launch(Dispatchers.Main) {
//                                if (talkingView == null) {
//                                    addQuestionView(defaultTalkingStr)
//                                }
//                            }
//                        }
//
//                        BaseVoiceRecorder.VOICE_RECORD_TYPE_AIXIAOYUE -> {
//                            mainScope.launch(Dispatchers.Main) {
//                                if (talkingView == null) {
//                                    addQuestionView(defaultTalkingStr)
//                                }
//                            }
//                        }
//                    }
//                }
//                false -> {
//                    //结束录音
//                }
//            }
//        }
//    }

//    private fun judgeExistAnswer(replyIntentModel: ReplyIntentModel): Boolean{
//        return when(replyIntentModel.code) {
//                200 -> true
//                204 -> false
//                else -> false
//            }
//    }
//    private fun findFinalAnswer(sdAnswer:ReplyIntentModel, axyAnswer:ReplyIntentModel):ReplyIntentModel?{
//        return when(judgeExistAnswer(axyAnswer)){
//            false -> sdAnswer
//            true -> null
//        }
//    }

        private fun startTTS(text: String) {
//            //构建合成参数
//            val params = StringBuffer()
//            //合成发音人，发音人列表：https://www.yuque.com/iflyaiui/zzoolv/iwxf76#d1uw4
//            params.append("vcn=x4_lingxiaoying_em_v2")
//            //语速，取值范围[0,100]
//            params.append(",speed=55")
//            //音调，取值范围[0,100]
//            params.append(",pitch=50")
//            //音量，取值范围[0,100]
//            params.append(",volume=55")
//            AiuiEngine.TTS_start(text, params)
            SpeakHelper.speakWithoutStop(text)
        }

        private fun initSDK() {
            //状态初始化
            EngineConstants.isRecording = false
            //注意事项1: sn每台设备需要唯一！！！！WakeupEngine的sn和AIUI的sn要一致
            //注意事项2: 获取的值要保持稳定，否则会重复授权，浪费授权量
        EngineConstants.serialNumber = "sendi-${RobotStatus.SERIAL_NUMBER}"
            // 初始化AIUI(识别+语义+合成）
            mAIUIAgent = AiuiEngine.getInstance(aiuiListener, "cfg/aiui.cfg")
            if (mAIUIAgent != null) {
                LogUtil.i("AIUI初始化成功")
            } else {
                LogUtil.i("AIUI初始化失败")
            }

            //对音频的处理为不降噪唤醒直接送去识别，只保留1声道音频,
            SystemRecorder.AUDIO_TYPE_ASR = true
            //初始化录音
            if (recorder == null) {
                recorder = RecorderFactory.getRecorder()
            }
            if (recorder != null) {
                LogUtil.i("录音机初始化成功")
            } else {
                LogUtil.i("录音机初始化失败")
            }
        }


        private fun startRecord() {
            if (recorder != null) {
                when (recorder?.startRecord()) {
                    0 -> {
                        LogUtil.i("开启录音成功！")
                    }
                    111111 -> {
                        LogUtil.i("异常,AlsaRecorder is null ...")
                    }
                    else -> {
                        LogUtil.i("开启录音失败，请查看/dev/snd/下的设备节点是否有777权限！\nAndroid 8.0 以上需要暂时使用setenforce 0 命令关闭Selinux权限！")
                        destroyRecord()
                    }
                }
            }
        }

        private fun stopRecord() {
            if (recorder != null) {
                recorder?.stopRecord()
                LogUtil.i("停止录音")
            }
        }

        private fun destroyRecord() {
            stopRecord()
            recorder = null
            LogUtil.d("destroy is Done!")
        }

        fun quitFragment() {
            if (EngineConstants.isRecording) {
                stopRecord()
            }
            if (recorder != null) {
                recorder!!.destroyRecord()
                recorder = null
            }
            if (aiuiListener != null) {
                aiuiListener = null
            }
            SystemRecorder.AUDIO_TYPE_ASR = false
            //销毁aiui
            AiuiEngine.destroy()
            SpeakHelper.stop()
        }
    }

package com.sendi.deliveredrobot.view.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.iflytek.aikitdemo.ability.AbilityCallback
import com.iflytek.aikitdemo.ability.AbilityConstant
import com.iflytek.aikitdemo.ability.tts.TtsHelper
import com.iflytek.aiui.AIUIAgent
import com.iflytek.aiui.AIUIConstant
import com.iflytek.aiui.AIUIEvent
import com.iflytek.aiui.AIUIListener
import com.iflytek.vtncaetest.StreamingAsrModel
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
import com.qmuiteam.qmui.kotlin.sp
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.databinding.FragmentConversationBinding
import com.sendi.deliveredrobot.entity.entitySql.QuerySql
import com.sendi.deliveredrobot.enum.ASROrNlpModelTypeEnum
import com.sendi.deliveredrobot.helpers.AudioMngHelper
import com.sendi.deliveredrobot.helpers.DialogHelper
import com.sendi.deliveredrobot.helpers.ReplyQaConfigHelper
import com.sendi.deliveredrobot.helpers.SpeakHelper
import com.sendi.deliveredrobot.model.BasicModel
import com.sendi.deliveredrobot.model.ConversationAnswerModel
import com.sendi.deliveredrobot.model.ConversationModel
import com.sendi.deliveredrobot.model.GetVFFileToTextModel
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.service.PlaceholderEnum
import com.sendi.deliveredrobot.utils.GenerateReplyToX8Utils
import com.sendi.deliveredrobot.utils.LogUtil
import com.sendi.deliveredrobot.utils.SpanUtils
import com.sendi.deliveredrobot.view.widget.MyFlowLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.util.Date
import java.util.Random
import java.util.Timer
import kotlin.concurrent.thread
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * @author heky
 * @date 2023-10-24
 * @description 人机对话fragment
 */
class ConversationFragment : Fragment() {
    companion object{
        private const val AI_XIAO_YUE_DEFAULT_ANSWER = "如需获取更多服务，可微信扫描以下二维码，添加艾小越微信号获取更多帮助。"
        private const val RESUME_SPEAK_TEXT = "我在，请问您有什么问题？"
    }

    var binding: FragmentConversationBinding? = null
    val mainScope = MainScope()
    lateinit var timer: Timer
    private var startTime: Long = 0
    private var totalHeight: Int = 0
    private var talkingView: LinearLayoutCompat? = null
    private val hashMap = HashMap<String, Array<ConversationModel>?>()
    private var answerPriority: Array<String>? = ASROrNlpModelTypeEnum.answerPriority // 优先级，下标越小优先级越高
    private var waitTalk = true
//    private val defaultAnswer = "我没明白您的意思，可以换个方式提问吗？"
    private val defaultAnswer = PlaceholderEnum.replaceText("%唤醒词%未听清您说的话，可以再说一遍吗？")
    private val pattern = "(((htt|ft|m)ps?):\\/\\/)?([\\da-zA-Z\\.-]+)\\.?([a-z]{2,6})(:\\d{1,5})?([\\/\\w\\.-]*)*\\/?([#=][\\S]+)?"
    private var aiuiListener: AIUIListener? = null
    private var recorder: AudioRecorder? = null
    private var mAIUIAgent: AIUIAgent? = null
    private var aiSoundHelper: TtsHelper? = null
    private var basicModel: BasicModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        basicModel = QuerySql.QueryBasic()
        initXTTS()
    }

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
                    findNavController().popBackStack(R.id.homeFragment, false)
                }
            }
        }, Date(), 1000)
//        initVoiceRecord()
            DialogHelper.loadingDialog.show()
            thread {
                LogUtil.i("conversation initSDK")
                // 资源拷贝
                CopyAssetsUtils.portingFile(MyApplication.context)
                aiuiListener = AIUIListener { event: AIUIEvent ->
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
                                val result = when (it == null) {
                                    true -> null
                                    false -> String(it, StandardCharsets.UTF_8)
                                }
                                result
                            } ?: return@AIUIListener
                            val cntJson = JSONObject(json)
                            val text = cntJson.getJSONObject("text")
                            //识别结果
                            val streamingAsrModel: StreamingAsrModel
                            try {
                                streamingAsrModel = StreamingAsrUtil.processIATResult(text)
                                if (!streamingAsrModel.asrResult.isNullOrEmpty()) {
                                    DialogHelper.loadingDialog.show()
                                    addQuestionView(streamingAsrModel.asrResult, talkingView)
                                }
                            }catch (e:NullPointerException){
                                LogUtil.i("nlp结果为null")
                                addQuestionView("语音解析异常，请重试", talkingView)
                                return@AIUIListener
                            }
                            //最终识别结果
                            if (text.getBoolean("ls")) {
                                LogUtil.i("识别结果=${streamingAsrModel.asrResult}")
                                //sid是每次交互的id，提供给讯飞可以查云端音频和结果
                                val sid = event.data.getString("sid")
                                LogUtil.i("sid=$sid")
                                hashMap[sid?:""] = when(answerPriority == null){
                                    true -> null
                                    false -> {
                                        val array = Array(answerPriority!!.size){
                                            ConversationModel(
                                                streamingAsrModel = streamingAsrModel
                                            )
                                        }
                                        array
                                    }
                                }
                                mainScope.launch(Dispatchers.IO) {
//                                    val test1 = test1()
//                                    findFinalAnswerAndStartTTS(sid?:"", ASROrNlpModelTypeEnum.TEST_1.getCode(),test1)
//                                    val test2 = test2()
//                                    findFinalAnswerAndStartTTS(sid?:"", ASROrNlpModelTypeEnum.TEST_2.getCode(),test2)
                                    if (answerPriority?.contains(ASROrNlpModelTypeEnum.AI_XIAO_YUE.getCode()) == true) {
                                        val answerAiXiaoYue = questionAiXiaoYue(streamingAsrModel.asrResult, sid?:"")
                                        findFinalAnswerAndStartTTS(
                                            sid?:"",
                                            ASROrNlpModelTypeEnum.AI_XIAO_YUE.getCode(),
                                            when(answerAiXiaoYue?.code) {
                                                200 -> answerAiXiaoYue.data.reply
                                                204 -> ""
                                                else -> ""
                                            }
                                        )
                                    }
                                }
                            }
                        } else if (event.info.contains("\"sub\":\"nlp")) {
                            val json = event.data.getByteArray("0").let {
                                val result = when (it == null) {
                                    true -> null
                                    false -> String(it, StandardCharsets.UTF_8)
                                }
                                result
                            } ?: return@AIUIListener
                            val cntJson = JSONObject(json)
                            val nlpResult = cntJson.getJSONObject("intent") ?: return@AIUIListener
                            try {
//                                val cntJson = JSONObject(json)
//                                val nlpResult = cntJson.getJSONObject("intent") ?: return@AIUIListener
                                //nlp无结果不处理
                                //如果只判断asr结果中的无意义词，若nlp先返回就可能被错误判断为无意义词
                                val asrResult = nlpResult.getString("text")
                                EngineConstants.meaningful = senselessWordUtil.isMeaningful_filter1word(asrResult)
                                //无意义词不处理
                                if (!EngineConstants.meaningful) {
                                    LogUtil.i("无意义词不处理")
//                                    AiuiEngine.MSG_reset_wakeup()
                                    //在线语义结果,rc=0语义理解成功，rc≠0语义理解失败
//                                    DialogHelper.loadingDialog.dismiss()
//                                    startTTS(defaultAnswer)
//                                    mainScope.launch {
//                                        addAnswer2(defaultAnswer)
//                                    }
                                    return@AIUIListener
                                }
                                LogUtil.i("nlp result :$nlpResult")
//                                AiuiEngine.MSG_reset_wakeup()
                                //在线语义结果,rc=0语义理解成功，rc≠0语义理解失败
                                val answer = "${nlpResult.getJSONObject("answer")["text"]}"
                                val sid = nlpResult.getString("sid")
                                if(answerPriority?.contains(ASROrNlpModelTypeEnum.AIUI.getCode()) == true){
                                    findFinalAnswerAndStartTTS(sid, ASROrNlpModelTypeEnum.AIUI.getCode(),answer)
                                }
                            } catch (e: JSONException) {
                                val sid = nlpResult.getString("sid")
                                findFinalAnswerAndStartTTS(sid, ASROrNlpModelTypeEnum.AIUI.getCode(),defaultAnswer)
                                LogUtil.i("nlpResult异常")
                            }
                        } else if (event.info.contains("\"sub\":\"tpp")) {
                            val json = event.data.getByteArray("0").let {
                                val result = when (it == null) {
                                    true -> null
                                    false -> String(it, StandardCharsets.UTF_8)
                                }
                                result
                            } ?: return@AIUIListener
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
                        val answer =
                            "发生异常，请重试...（错误码:${event.arg1} 错误信息:${event.info}）"
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

                            AIUIConstant.TTS_SPEAK_RESUMED -> LogUtil.i(
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
                Thread.sleep(1000)
                if(!isResumed){
                    DialogHelper.loadingDialog.dismiss()
                    return@thread
                }
                initSDK()
                startRecord()
                Thread.sleep(2000)
                DialogHelper.loadingDialog.dismiss()
            }
            SpeakHelper.setUserCallback(object : SpeakHelper.SpeakUserCallback {
                override fun speakAllFinish() {
                    LogUtil.i("tts:播放完成")
                    AiuiEngine.MSG_wakeup(EngineConstants.WAKEUPTYPE_VOICE)
                    talkingView = null
                }

            override fun progressChange(utteranceId: String, progress: Int) {
                startTime = System.currentTimeMillis()
            }
        })
        SpeakHelper.speak(RESUME_SPEAK_TEXT)

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
                    AiuiEngine.MSG_sendTextForNlp(text, "main")
                    addQuestionView(text)
                }
                myFlowLayout.addView(linearLayoutCompat)
            }
        }
        binding?.flHome?.apply {
            setOnClickListener {
                quitFragment()
                findNavController().popBackStack(R.id.homeFragment, false)
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
                if (isPlaying) {
                    binding?.viewPlay?.visibility = View.VISIBLE
                    pause()
                } else {
                    binding?.viewPlay?.visibility = View.GONE
                    start()
                }
            }
        }
    }

    @SuppressLint("InflateParams")
    private fun addQuestionView(conversation: String, view: LinearLayoutCompat? = null) {
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

    private suspend fun addAnswer2(conversation: String) {
        LogUtil.i("回复--->$conversation")
        AiuiEngine.MSG_reset_wakeup()
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
        }
    }

    private suspend fun addAnswer3() {
        binding?.linearLayoutConversation?.apply {
            val linearLayoutCompat = LayoutInflater.from(requireContext())
                .inflate(R.layout.layout_conversation_text_view_left_2, null) as LinearLayoutCompat
            val textView = linearLayoutCompat.findViewById<TextView>(R.id.tv_content)
            textView.text = AI_XIAO_YUE_DEFAULT_ANSWER
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

    private suspend fun questionAiXiaoYue(
        conversation: String,
        sid: String
    ): GetVFFileToTextModel? = suspendCoroutine {
        LogUtil.i("提问--->$conversation")
        it.resume(
            GenerateReplyToX8Utils.generateReplyToX8(
                conversation,
                questionID = "$sid"
            )
        )
    }

    private fun findPriorityIndex(priority: String): Int {
        return answerPriority?.indexOf(priority) ?: -1
    }

    /**
     * @description 定义 未返回 null，无答案 ""
     * @description 如果当前有答案，则查看 < index 的答案，如果都有返回且均没有答案，则使用当前答案，并从集合中清除此项；如果存在未返回，则缓存当前答案。
     *              如果当前没有答案，如果 < index 存在未返回，则缓存当前未无答案，否则往后遍历，如果未遇到未返回，则使用首个答案，如果遇到队列末尾，则输出一个默认语句；如果遇到未返回，则缓存当前为无答案。
     *
     */
    private fun findFinalAnswer(sid: String, answerType: String, answer: String): ConversationAnswerModel? {
        val cacheAnswers = hashMap[sid] ?: return null
        val index = findPriorityIndex(answerType)
        if (index == -1) return null
        if (answer.isEmpty()) {
            for (i in 0 until index) {
                val cache = cacheAnswers[i].conversationAnswerModel
                if (cache == null) {
                    // 如果存在未返回，则缓存当前答案
                    cacheAnswers[index].conversationAnswerModel = ConversationAnswerModel(answerType = answerType, answer = answer)
                    return null
                }
            }
            var latterAnswer: ConversationAnswerModel? = null
            for (i in index + 1 until cacheAnswers.size) {
                val cache = cacheAnswers[i].conversationAnswerModel
                if (cache == null) {
                    // 如果存在未返回，则缓存当前答案
                    cacheAnswers[index].conversationAnswerModel = ConversationAnswerModel(answerType = answerType, answer = answer)
                    return null
                }
                if (cache.answer.isNotEmpty()) {
                    latterAnswer = cache
                    break
                }
            }
            // 使用当前答案，并从集合中清除此项
            hashMap[sid] = null
            return latterAnswer ?: ConversationAnswerModel(answerType = ASROrNlpModelTypeEnum.AIUI.getCode(), answer = defaultAnswer)
        } else {
            for (i in 0 until index) {
                val cache = cacheAnswers[i].conversationAnswerModel
                if (cache == null) {
                    // 如果存在未返回，则缓存当前答案
                    cacheAnswers[index].conversationAnswerModel = ConversationAnswerModel(answerType = answerType, answer = answer)
                    return null
                }
            }
            // 使用当前答案，并从集合中清除此项
            hashMap[sid] = null
            return ConversationAnswerModel(answerType = answerType, answer = answer)
        }
    }

    private fun findFinalAnswerAndStartTTS(sid: String, answerType: String, answer: String) {
        val vcn = when (hashMap[sid]?.get(0)?.streamingAsrModel?.language) {
            StreamingAsrUtil.MANDARIN -> "xiaoyan"
            StreamingAsrUtil.GUANGDONG -> "xiaomei"
            else -> "xiaomei"
        }
        val finalAnswer = findFinalAnswer(sid, answerType, answer)
        if (finalAnswer != null) {
            DialogHelper.loadingDialog.dismiss()
            aiSoundHelper?.setVCN(vcn)
            mainScope.launch {
                addAnswer2(finalAnswer.answer)
                val finalAnswerStr :String
                when (finalAnswer.answerType) {
                     ASROrNlpModelTypeEnum.AI_XIAO_YUE.getCode() -> {
                         finalAnswerStr = "${finalAnswer.answer},$AI_XIAO_YUE_DEFAULT_ANSWER"
                         addAnswer3()
                     }
                    else -> {
                        finalAnswerStr = finalAnswer.answer
                    }
                }
                startTTS(finalAnswerStr)
            }
        }
    }

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
//        SpeakHelper.speakWithoutStop(text.replace(Regex(pattern),""))
        aiSoundHelper?.apply {
            AudioMngHelper(requireContext()).setVoice100(65)
            var speed = basicModel?.speechSpeed?.times(7f)?.toInt()?:50
            speed = speed.let {
                if(it > 100) 100
                else it
            }
            setSpeed(speed)
            setVolume(basicModel?.voiceVolume?:50)
            setPitch(50)
            speechText(text.replace(Regex(pattern),""))
        }
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
        thread {
            LogUtil.i("conversation quitFragment")
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
            aiSoundHelper?.stop()
            LogUtil.i("conversation quitFragment is done")
        }
    }

    val random = Random()
    fun test1(): String {
        return when (random.nextInt(100) > 50) {
            true -> "test1"
            false -> ""
        }
    }

    fun test2(): String{
        return "test2"
    }

    private fun initXTTS() {
        if (aiSoundHelper != null) {
            aiSoundHelper!!.release()
        }
        aiSoundHelper = TtsHelper().apply {
            val callback = object : AbilityCallback {

                override fun onAbilityBegin() {
                    LogUtil.i("xtts开始合成数据")
                }

                override fun onAbilityResult(result: String) {
                    startTime = System.currentTimeMillis()
                }

                override fun onAbilityError(code: Int, error: Throwable?) {
                    LogUtil.i("xtts合成失败:${error?.message}")
                }

                override fun onAbilityEnd() {
                    LogUtil.i("xtts播放结束=====\n")
                    SpeakHelper.speakUserCallback?.speakAllFinish()
                    if (aiSoundHelper != null) {
                        aiSoundHelper!!.stop()
                    }
                }
            }
            onCreate(AbilityConstant.XTTS_ID, callback)
        }

    }
}

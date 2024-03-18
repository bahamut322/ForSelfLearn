package com.sendi.deliveredrobot.view.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.iflytek.vtncaetest.engine.EngineConstants
import com.iflytek.vtncaetest.engine.WakeupEngine
import com.iflytek.vtncaetest.engine.WakeupListener
import com.iflytek.vtncaetest.recorder.AudioRecorder
import com.iflytek.vtncaetest.recorder.RecorderFactory
import com.iflytek.vtncaetest.recorder.SystemRecorder
import com.iflytek.vtncaetest.utils.CopyAssetsUtils
import com.iflytek.vtncaetest.utils.ErrorCode
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.databinding.FragmentStandbyBinding
import com.sendi.deliveredrobot.entity.Universal
import com.sendi.deliveredrobot.entity.entitySql.QuerySql
import com.sendi.deliveredrobot.helpers.WakeupWordHelper
import com.sendi.deliveredrobot.interfaces.FaceDataListener
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.utils.LogUtil
import com.sendi.deliveredrobot.view.widget.Advance
import com.sendi.deliveredrobot.view.widget.FaceRecognition
import com.sendi.deliveredrobot.viewmodel.BaseViewModel
import com.sendi.deliveredrobot.viewmodel.BaseViewModel.checkIsImageFile
import com.sendi.fooddeliveryrobot.BaseVoiceRecorder
import java.io.File


class StandbyFragment : Fragment() {

    private lateinit var binding: FragmentStandbyBinding
    private var imagePaths: List<Advance> = ArrayList()
    private var controller: NavController? = null
    private var baseViewModel: BaseViewModel? = null
    private var sendFace: Int = 0 //用来只接收一次人脸数据，否则多次跳转页面时会报错

    /**
     * 唤醒回调
     */
    private var wakeupListener: WakeupListener? = null

    private var recorder: AudioRecorder? = null

    override fun onResume() {
        super.onResume()
        binding.Standby.setResume()
    }

    override fun onPause() {
        super.onPause()
        binding.Standby.setPause()
        quitFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_standby, container, false)
        binding = DataBindingUtil.bind(view)!!
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        controller = Navigation.findNavController(requireView())
        baseViewModel = ViewModelProvider(this).get(BaseViewModel::class.java)
        getFilesAllNames(Universal.Standby)
        val str = QuerySql.robotConfig().wakeUpList
        val contains1 = str.contains("1")
        val contains2 = str.contains("2")
        val contains3 = str.contains("3")
        if (contains1) {
            println("字符串中包含数字1,点击屏幕")
            binding.imageButton.setOnClickListener {
                controller!!.navigate(R.id.action_standbyFragment_to_homeFragment)
            }
        }
        if (contains2) {
            println("字符串中包含数字2,检测到人脸")
            FaceRecognition.suerFaceInit(
                extractFeature = false,
                needEtiquette = false
            )
            FaceDataListener.setOnChangeListener {
                try {
                    if (FaceDataListener.getFaceModels().isNotEmpty() && sendFace == 0) {
                        sendFace++
                        controller!!.navigate(R.id.action_standbyFragment_to_homeFragment)

                    }
                } catch (_: Exception) {
                }
            }
        }
        if (contains3) {
            println("字符串中包含数字3,唤醒词")
//            BaseVoiceRecorder.getInstance()?.recordCallback = { _, pinyinString, _ ->
//                if (pinyinString.contains(WakeupWordHelper.wakeupWordPinyin ?: "")) {
//                    Log.i("AudioChannel", "包含${WakeupWordHelper.wakeupWord}")
//                    controller!!.navigate(R.id.action_standbyFragment_to_homeFragment)
//                }
//            }

            wakeupListener = WakeupListener { angle, beam, score, keyWord ->
                LogUtil.i("angle:$angle,beam:$beam,score:$score,keyWord:$keyWord")
                controller!!.navigate(R.id.action_standbyFragment_to_homeFragment)
            }
            Handler(Looper.getMainLooper()).postDelayed({
                // 资源拷贝
                CopyAssetsUtils.portingFile(requireContext())
                initSDK()
                startRecord()
            }, 1000)
        }
    }

    private fun getFilesAllNames(path: String?) {
        // 传入指定文件夹的路径
        val file = path?.let { File(it) }
        if (file?.exists() == true) { // 检查文件夹是否存在
            val files = file.listFiles()
            if (files != null && files.isNotEmpty()) { // 检查文件夹内容是否为空
                imagePaths = ArrayList()
                for (value in files) {
                    if (checkIsImageFile(value.path)) {
                        // 图片
                        (imagePaths as ArrayList<Advance>).add(Advance(value.path, "2", 1, 3))
                    } else {
                        // 视频
                        (imagePaths as ArrayList<Advance>).add(Advance(value.path, "1", 0, 3))
                    }
                }
                binding.Standby.setData(imagePaths) // 将数据传入到控件中显示
            }
        } else {
            (imagePaths as ArrayList<Advance>).add(Advance(Universal.gifDefault, "2", 0, 3))
            binding.Standby.setData(imagePaths) // 将数据传入到控件中显示
        }
    }



    override fun onStop() {
        FaceDataListener.removeOnChangeListener()
        FaceRecognition.onDestroy()
        super.onStop()
    }

    private fun initSDK() {
        //状态初始化
        EngineConstants.isRecording = false
        //注意事项1: sn每台设备需要唯一！！！！WakeupEngine的sn和AIUI的sn要一致
        //注意事项2: 获取的值要保持稳定，否则会重复授权，浪费授权量
        EngineConstants.serialNumber = "sendi-${RobotStatus.SERIAL_NUMBER}"
//        EngineConstants.serialNumber = "iflytek-test"
        LogUtil.i("sn : " + EngineConstants.serialNumber)
        //对音频的处理为降噪唤醒再送去识别,
        SystemRecorder.AUDIO_TYPE_ASR = false
        //初始化wakeupEngine(降噪+唤醒)
        val initResult = WakeupEngine.getInstance(wakeupListener)
        if (initResult == 0) {
            LogUtil.i("wakeupEngine初始化成功")
        } else {
            LogUtil.i(
                "wakeupEngine初始化失败，错误码$initResult " + ErrorCode.getError(
                    initResult
                ) + "  \n错误解决详情参考：https://www.yuque.com/iflyaiui/zzoolv/igbuol")
            LogUtil.i( "wakeupEngine初始化失败")
        }

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
            val ret = recorder!!.startRecord()
            if (0 == ret) {
                LogUtil.i("开启录音成功！")
            } else if (111111 == ret) {
                LogUtil.i("异常,AlsaRecorder is null ...")
            } else {
                LogUtil.i("开启录音失败，请查看/dev/snd/下的设备节点是否有777权限！\nAndroid 8.0 以上需要暂时使用setenforce 0 命令关闭Selinux权限！")
                destroyRecord()
            }
        }
    }

    private fun stopRecord() {
        if (recorder != null) {
            recorder!!.stopRecord()
            LogUtil.i("停止录音")
        }
    }

    private fun destroyRecord() {
        stopRecord()
        recorder = null
        LogUtil.i("destroy is Done!")
    }

    private fun quitFragment(){
        if (EngineConstants.isRecording) {
            stopRecord()
        }
        if (recorder != null) {
            recorder!!.destroyRecord()
            recorder = null
        }
        if (wakeupListener != null) {
            wakeupListener = null
        }
        WakeupEngine.destroy()
    }



}
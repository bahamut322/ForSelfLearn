//package com.sendi.deliveredrobot.view.fragment
//
//import android.annotation.SuppressLint
//import android.os.Bundle
//import android.os.Environment
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.SeekBar
//import androidx.core.content.ContextCompat
//import androidx.databinding.DataBindingUtil
//import androidx.fragment.app.Fragment
//import androidx.fragment.app.viewModels
//import chassis_msgs.DoorState
//import com.sendi.deliveredrobot.BuildConfig
//import com.sendi.deliveredrobot.MyApplication
//import com.sendi.deliveredrobot.R
//import com.sendi.deliveredrobot.RobotCommand
//import com.sendi.deliveredrobot.databinding.FragmentBasicSettingBinding
//import com.sendi.deliveredrobot.helpers.*
//import com.sendi.deliveredrobot.model.PhoneConfirmModel
//import com.sendi.deliveredrobot.model.RequestVersionStatusModel
//import com.sendi.deliveredrobot.navigationtask.RobotStatus
//import com.sendi.deliveredrobot.navigationtask.virtualTaskExecute
//import com.sendi.deliveredrobot.room.database.DataBaseDeliveredRobotMap
//import com.sendi.deliveredrobot.service.CloudMqttService
//import com.sendi.deliveredrobot.topic.DoorStateTopic
//import com.sendi.deliveredrobot.utils.InstallApkUtils
//import com.sendi.deliveredrobot.utils.LogUtil
//import com.sendi.deliveredrobot.utils.ToastUtil
//import com.sendi.deliveredrobot.view.widget.DownApkDialog
//import com.sendi.deliveredrobot.view.widget.UpdateConfirmDialog
//import com.sendi.deliveredrobot.viewmodel.BasicSettingViewModel
//import com.sendi.deliveredrobot.viewmodel.SendPlaceBin1ViewModel
//import com.sendi.deliveredrobot.viewmodel.SendPlaceBin2ViewModel
//import kotlinx.coroutines.*
//import kotlinx.coroutines.sync.Mutex
//import kotlinx.coroutines.sync.withLock
//import java.io.File
//import java.io.FileOutputStream
//import java.io.IOException
//import java.io.InputStream
//import java.net.HttpURLConnection
//import java.net.MalformedURLException
//import java.net.URL
//import java.net.URLConnection
//import kotlin.properties.Delegates
//
///**
// * @author heky
// * @describe 基础设置
// */
//class BasicSettingFragment : Fragment() {
//    lateinit var binding: FragmentBasicSettingBinding
//    private val viewModel by viewModels<BasicSettingViewModel>({ requireActivity() })
//    private val viewModelBin1 by viewModels<SendPlaceBin1ViewModel>({ requireActivity() })
//    private val viewModelBin2 by viewModels<SendPlaceBin2ViewModel>({ requireActivity() })
//    private var buttonStatus = BUTTON_STATUS_REQUEST_NEW_VERSION
//    private var downloadDialog: DownApkDialog? = null
//    private var updateConfirmDialog: UpdateConfirmDialog? = null
//    private var downloadPercent by Delegates.observable(0){
//            _, _, newValue ->
//        getDownloadDialog()?.progressBar?.progress = newValue
//    }
//    private lateinit var mainScope: CoroutineScope
//    private val mutex = Mutex()
//
//    companion object{
//        const val BUTTON_STATUS_REQUEST_NEW_VERSION = 0x11
//        const val BUTTON_STATUS_DOWNLOAD_APK = 0x22
//        private var doorOpenClicked = false
//    }
//
//    override fun onStop() {
//        super.onStop()
//        mainScope.cancel()
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_basic_setting, container, false)
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        mainScope = MainScope()
//        binding = DataBindingUtil.bind(view)!!
//        binding.seekBarBrightness.apply {
//            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
//                @SuppressLint("SetTextI18n")
//                override fun onProgressChanged(
//                    seekBar: SeekBar?,
//                    progress: Int,
//                    fromUser: Boolean
//                ) {
//                    with(binding.textViewBrightnessPercent) {
//                        text = "$progress%"
//                    }
//                    with(binding.imageViewBrightness) {
//                        when (progress) {
//                            0 -> setBackgroundResource(R.drawable.ic_brightness_0)
//                            in 1 until 100 -> setBackgroundResource(R.drawable.ic_brightness_50)
//                            100 -> setBackgroundResource(R.drawable.ic_brightness_100)
//                        }
//                    }
//                    BasicSettingHelper.setBrightness(requireActivity(), progress)
//                    mainScope.launch {
//                        viewModel.basicConfig.brightness = progress
//                        withContext(Dispatchers.Default) {
//                            DataBaseDeliveredRobotMap.getDatabase(requireContext()).getDao()
//                                .updateBasicConfig(viewModel.basicConfig)
//                        }
//                    }
//                }
//
//                override fun onStartTrackingTouch(seekBar: SeekBar?) {
//                }
//
//                override fun onStopTrackingTouch(seekBar: SeekBar?) {
//                }
//            })
//            // 系统亮度值范围：0～255，应用窗口亮度范围：0.0f～1.0f。
//            val currentBright = viewModel.basicConfig.brightness ?: 0f
//            progress = 10086
//            progress = currentBright.toInt()
//        }
//        binding.textViewAppVersion.apply {
//            text = String.format(
//                resources.getString(R.string.current_version),
//                BuildConfig.VERSION_NAME
//            )
//        }
//        DoorStateTopic.setDoorStateListener {
//            doorState ->
//            val state = doorState.state
//            val door = doorState.door
//            when (state) {
//                DoorState.STATE_OPENED -> {
//                    when (door) {
//                        DoorState.DOOR_ONE -> {
//                            ROSHelper.controlBin(RobotCommand.CMD_OPEN, DoorState.DOOR_TWO)
//                            binding.textViewOpenDoor.apply {
//                                isEnabled = true
//                            }
//                            CloudMqttService.publish(PhoneConfirmModel(number = RobotStatus.currentLocation?.floorName?:"").toString())
//                        }
//                        DoorState.DOOR_TWO -> {
//                            doorOpenClicked = true
//                            binding.textViewOpenDoor.apply {
//                                text = resources.getString(R.string.close_door)
//                                isEnabled = true
//                            }
//                            viewModelBin1.previousTaskFinished = true
//                            viewModelBin1.previousRemoteOrderPutFinished = true
//                            viewModelBin1.previousRemoteOrderSendFinished = true
//                            viewModelBin1.clearSelected()
//                            viewModelBin2.previousTaskFinished = true
//                            viewModelBin2.previousRemoteOrderPutFinished = true
//                            viewModelBin2.previousRemoteOrderSendFinished = true
//                            viewModelBin2.clearSelected()
//                            // 恢复两个仓门都可以使用
//                            IdleGateDataHelper.addCount()
//                            IdleGateDataHelper.addCount()
//                        }
//                    }
//                }
//                DoorState.STATE_CLOSED -> {
//                    when (door) {
//                        DoorState.DOOR_ONE -> {
//                            ROSHelper.controlBin(RobotCommand.CMD_CLOSE, DoorState.DOOR_TWO)
//                            binding.textViewOpenDoor.apply {
//                                isEnabled = true
//                            }
//                        }
//                        DoorState.DOOR_TWO -> {
//                            doorOpenClicked = false
//                            binding.textViewOpenDoor.apply {
//                                text = resources.getString(R.string.open_door)
//                                isEnabled = true
//                            }
//                        }
//                    }
//                }
//                DoorState.STATE_OPENING -> {
//                }
//                DoorState.STATE_CLOSING -> {
//                    SpeakHelper.speak(MyApplication.instance!!.getString(R.string.door_closing_take_care_hands))
//                }
//                DoorState.STATE_OPEN_FAILED -> {
//                    binding.textViewOpenDoor.apply {
//                        isEnabled = true
//                    }
//                }
//                DoorState.STATE_CLOSE_FAILED -> {
//                    binding.textViewOpenDoor.apply {
//                        isEnabled = true
//                    }
//                }
//                DoorState.STATE_HALF_OPEN -> {
//                    binding.textViewOpenDoor.apply {
//                        isEnabled = true
//                    }
//                }
//            }
//
//
//        }
//        binding.textViewOpenDoor.apply {
//            isClickable = true
//            text = when (doorOpenClicked) {
//                true -> resources.getString(R.string.close_door)
//                false -> resources.getString(R.string.open_door)
//            }
//            setOnClickListener {
//                isEnabled = false
//                mainScope.launch {
//                    mutex.withLock {
//                        if (!doorOpenClicked) {
////                            if (!RobotStatus.settingControlDoor) {
////                                RobotStatus.settingControlDoor = true
////                                doorOpenClicked = true
////                                text = resources.getString(R.string.close_door)
//                                val state = ROSHelper.controlBin(cmd = RobotCommand.CMD_CHECK,door = DoorState.DOOR_ONE)
//                                if(state.toByte() != DoorState.STATE_OPENED){
//                                    ROSHelper.controlBin(RobotCommand.CMD_OPEN, DoorState.DOOR_ONE)
////                                    virtualTaskExecute(4)
//                                }else{
//                                    ROSHelper.controlBin(RobotCommand.CMD_OPEN, DoorState.DOOR_TWO)
////                                    virtualTaskExecute(2)
//                                }
//
////                                isEnabled = true
////                                viewModelBin1.previousTaskFinished = true
////                                viewModelBin1.previousRemoteOrderPutFinished = true
////                                viewModelBin1.previousRemoteOrderSendFinished = true
////                                viewModelBin1.clearSelected()
////                                viewModelBin1.place.value = ""
////                                viewModelBin2.previousTaskFinished = true
////                                viewModelBin2.previousRemoteOrderPutFinished = true
////                                viewModelBin2.previousRemoteOrderSendFinished = true
////                                viewModelBin2.clearSelected()
////                                viewModelBin2.place.value = ""
////                                // 恢复两个仓门都可以使用
////                                IdleGateDataHelper.addCount()
////                                IdleGateDataHelper.addCount()
////                            }
//                        } else {
////                            if (!RobotStatus.settingControlDoor) {
////                                RobotStatus.settingControlDoor = true
////                                doorOpenClicked = false
////                                text = resources.getString(R.string.open_door)
//                                val state = ROSHelper.controlBin(cmd = RobotCommand.CMD_CHECK,door = DoorState.DOOR_ONE)
//                                if(state.toByte() != DoorState.STATE_CLOSED){
//                                    ROSHelper.controlBin(RobotCommand.CMD_CLOSE, DoorState.DOOR_ONE)
////                                    virtualTaskExecute(4)
//                                }else{
//                                    ROSHelper.controlBin(RobotCommand.CMD_CLOSE, DoorState.DOOR_TWO)
////                                    virtualTaskExecute(2)
//                                }
//                                SpeakHelper.speakWithoutStop(MyApplication.instance!!.getString(R.string.door_closing_take_care_hands))
////                            }
//                        }
//                    }
//                }
//            }
//        }
//        binding.textViewCheckVersion.apply {
//            isClickable = true
//            isEnabled = true
//            setOnClickListener {
//                when (buttonStatus) {
//                    BUTTON_STATUS_REQUEST_NEW_VERSION -> {
//                        //mqtt查询版本更新
//                        CloudMqttService.publish(
//                            RequestVersionStatusModel(
//                                versionName = BuildConfig.VERSION_NAME,
//                                versionCode = BuildConfig.VERSION_CODE,
//                                chassisVersion = RobotStatus.chassisVersionName,
//                                chassisVersionCode = RobotStatus.chassisVersionName.replace(".","")
//                            ).toString()
//                        )
//                    }
//                    BUTTON_STATUS_DOWNLOAD_APK -> {
//                        getUpdateConfirmDialog()?.show()
//                    }
//                }
//            }
//        }
//        RobotStatus.versionStatusModel.observe(viewLifecycleOwner){
//            binding.linearLayoutAppVersion.visibility = View.VISIBLE
//            if (it.flag) {
//                //有版本更新
//                buttonStatus = BUTTON_STATUS_DOWNLOAD_APK
//                binding.textViewCheckVersion.apply {
//                    isClickable = true
//                    isEnabled = true
//                    text = resources.getString(R.string.update_version)
//                    setTextColor(ContextCompat.getColor(requireContext(), R.color.color_FEFEFF))
//                    background = ContextCompat.getDrawable(requireContext(), R.drawable.selector_bg_button)
//                }
//                binding.textViewAppVersionDescription.apply {
//                    text = String.format(resources.getString(R.string.find_new_version), it.version)
//                }
//                binding.viewAppVersionImage.apply {
//                    background = ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_error_outline_24)
//                }
//            }else{
//                //无版本更新
//                binding.textViewCheckVersion.apply {
//                    isClickable = false
//                    isEnabled = false
//                }
//                binding.textViewAppVersionDescription.apply {
//                    visibility = View.VISIBLE
//                    text = resources.getString(R.string.current_version_latest)
//                }
//                binding.viewAppVersionImage.apply {
//                    background = ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_check_circle_outline_30)
//                }
//            }
//        }
//    }
//
//    /**
//     * @describe 下载apk
//     */
//    private fun downloadApk(path: String): Boolean{
//        if (path == "") {
//            getDownloadDialog()?.dismiss()
//            return false
//        }
//        val infoUrl: URL?
//        var inStream: InputStream? = null
//        var httpConnection: HttpURLConnection? = null
//        try {
//            infoUrl =
//                URL("${BuildConfig.HTTP_HOST}/$path")
//            val connection: URLConnection = infoUrl.openConnection().apply {
//                connectTimeout = 5000
//            }
//            httpConnection = connection as HttpURLConnection
//            val responseCode: Int = httpConnection.responseCode
//            if (responseCode == HttpURLConnection.HTTP_OK) {
//                val filePath = "${Environment.getExternalStorageDirectory().absolutePath}/$path"
//                val file = File(filePath)
//                //清除下载失败缓存
//                if (file.exists()) {
//                    file.delete()
//                }
//                file.parentFile?.mkdirs()
//                file.createNewFile()
//                inStream = httpConnection.inputStream
//                val fos = FileOutputStream(file)
//                val buf = ByteArray(1024 * 8)
//                var totalLength = 0
//                var len:Int
//                while (-1 != (inStream.read(buf,0, buf.size).also { len = it })) {
//                    totalLength += len
//                    val percent = (1f * totalLength / RobotStatus.versionStatusModel.value?.size!!) * 100
//                    downloadPercent = percent.toInt()
//                    fos.write(buf,0,len)
//                }
//                LogUtil.i("fileSize:${file.length()}")
//            }
//        } catch (e: MalformedURLException) {
//            e.printStackTrace()
//            getDownloadDialog()?.dismiss()
//            return false
//        } catch (e: IOException) {
//            ToastUtil.show("连接下载地址超时")
//            getDownloadDialog()?.dismiss()
//            e.printStackTrace()
//            return false
//        } finally {
//            try {
//                inStream?.close()
//                httpConnection?.disconnect()
//            } catch (e: IOException) {
//                e.printStackTrace()
//            } catch (ex: Exception) {
//                ex.printStackTrace()
//            }
//        }
//        return true
//    }
//
//    /**
//     * @describe 下载Apk Dialog
//     */
//    private fun getDownloadDialog(): DownApkDialog?{
//        if (downloadDialog == null) {
//            downloadDialog = DownApkDialog(requireContext())
//        }
//        return downloadDialog!!
//    }
//
//    /**
//     * @describe 确认下载 Dialog
//     */
//    private fun getUpdateConfirmDialog(): UpdateConfirmDialog?{
//        if (updateConfirmDialog == null) {
//            updateConfirmDialog = UpdateConfirmDialog(requireContext()).apply {
//                setOnItemClickListener(object : UpdateConfirmDialog.OnButtonClickCallback{
//                    override fun onConfirmButtonClick(dialog:UpdateConfirmDialog) {
//                        //下载apk
//                        getDownloadDialog()?.show()
//                        mainScope.launch {
//                            withContext(Dispatchers.Default){
//                                val result = downloadApk(RobotStatus.versionStatusModel.value?.path?:"")
//                                if (result) {
//                                    val root = Environment.getExternalStorageDirectory().absolutePath
//                                    val path = RobotStatus.versionStatusModel.value?.path?:""
//                                    val filePath = "$root/$path"
//                                    InstallApkUtils.executeSuCMD(filePath)
////                                    val deletePath = "$root/${findApkRoot(path)}"
////                                    if(deletePath === "$root/") return@withContext
////                                    deleteFile(deletePath)
//                                }else{
//                                    getDownloadDialog()?.dismiss()
//                                    ToastUtil.show("下载失败")
//                                }
//                            }
//                        }
//                        dialog.dismiss()
//                    }
//
//                    override fun onCancelButtonClick(dialog:UpdateConfirmDialog) {
//                        dialog.dismiss()
//                    }
//                })
//            }
//        }
//        return updateConfirmDialog!!
//    }
//
//    /**
//     * @describe 删除目标文件夹及其子结构
//     */
//    private fun deleteFile(path: String){
//        if (path == "") return
//        val file = File(path)
//        if(file.isDirectory){
//            for (listFile in file.listFiles()!!) {
//                deleteFile(listFile.absolutePath)
//            }
//        }
//        file.delete()
//    }
//
//    /**
//     * @describe 获取apk的存储的相对根部文件夹名
//     */
//    private fun findApkRoot(path:String):String{
//        val index = path.indexOf("/")
//        return path.substring(0,index)
//    }
//}
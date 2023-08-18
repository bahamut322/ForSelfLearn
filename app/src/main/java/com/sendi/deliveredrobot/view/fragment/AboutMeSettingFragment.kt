package com.sendi.deliveredrobot.view.fragment

import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.sendi.deliveredrobot.BuildConfig
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.databinding.FragmentAboutMeSettingBinding
import com.sendi.deliveredrobot.model.RequestVersionStatusModel
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.service.DeliverMqttService
import com.sendi.deliveredrobot.utils.InstallApkUtils
import com.sendi.deliveredrobot.utils.LogUtil
import com.sendi.deliveredrobot.utils.ToastUtil
import com.sendi.deliveredrobot.view.widget.DownApkDialog
import com.sendi.deliveredrobot.view.widget.UpdateConfirmDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.net.URLConnection
import kotlin.properties.Delegates

//Android 怎么封装百度TTS,能监听播放状态以及暂停播放或者继续播放
class AboutMeSettingFragment : Fragment() {
    lateinit var binding: FragmentAboutMeSettingBinding
    var controller: NavController? = null
    private var previousClickTime: Long = 0
    private var buttonStatus = BUTTON_STATUS_REQUEST_NEW_VERSION
    private var updateConfirmDialog: UpdateConfirmDialog? = null
    private var downloadDialog: DownApkDialog? = null
    private var downloadPercent by Delegates.observable(0){
            _, _, newValue ->
        getDownloadDialog()?.progressBar?.progress = newValue
    }
    lateinit var mainScope: CoroutineScope

    companion object {
        const val OPERATE_MAX = 7
        const val BUTTON_STATUS_REQUEST_NEW_VERSION = 0x11
        const val BUTTON_STATUS_DOWNLOAD_APK = 0x22
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_about_me_setting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        controller = Navigation.findNavController(view)
        mainScope = MainScope()
        binding = DataBindingUtil.bind(view)!!
        binding.serialNumber = RobotStatus.SERIAL_NUMBER
        binding.version = "V " + BuildConfig.VERSION_NAME
        binding.viewProducer.apply {
            var count = 0
            setOnClickListener {
                val currentTime = System.currentTimeMillis()
                if (((currentTime - previousClickTime) / 1000) < 1) {
                    count++
                } else {
                    count = 1
                }
                previousClickTime = currentTime
                if (count in 3 until OPERATE_MAX) {
                    ToastUtil.show(
                        String.format(
                            getString(R.string.need_steps_to_into_debug_mode),
                            "${OPERATE_MAX - count}"
                        )
                    )
                } else if (count == OPERATE_MAX) {
                    count = 0
                    controller!!.navigate(R.id.action_settingHomeFragment_to_verifyToDebugFragment)

                }
            }
        }

        RobotStatus.versionStatusModel.observe(viewLifecycleOwner){
            if (it.flag) {
                //有版本更新
                buttonStatus = BUTTON_STATUS_DOWNLOAD_APK
                binding.textViewCheckVersion.apply {
                    text = resources.getString(R.string.update_version)
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.color_FEFEFF))
                    background = ContextCompat.getDrawable(requireContext(), R.drawable.selector_bg_button)
                }
                ToastUtil.show("发现新版本（${it.version})")
                LogUtil.i("发现新版本（${it.version})")
            }else{
                ToastUtil.show(resources.getString(R.string.current_version_latest))
            }
        }

        binding.textViewCheckVersion.apply {
            setOnClickListener{
                    when (buttonStatus) {
                        BUTTON_STATUS_REQUEST_NEW_VERSION -> {
                            //mqtt查询版本更新
                            DeliverMqttService.publish(
                                RequestVersionStatusModel(
                                    versionName = BuildConfig.VERSION_NAME,
                                    versionCode = BuildConfig.VERSION_CODE,
                                    chassisVersion = RobotStatus.chassisVersionName,
                                    chassisVersionCode = RobotStatus.chassisVersionName.replace(".","")
                                ).toString()
                            )
                        }
                        BUTTON_STATUS_DOWNLOAD_APK -> {
                            getUpdateConfirmDialog()?.show()
                        }
                }
            }
        }

        binding.textViewSerialNumber.apply {
            text = RobotStatus.SERIAL_NUMBER
        }
        binding.textViewRobotVersion.apply {
            text = RobotStatus.chassisVersionName
        }
    }
    /**
     * @describe 下载Apk Dialog
     */
    private fun getDownloadDialog(): DownApkDialog?{
        if (downloadDialog == null) {
            downloadDialog = DownApkDialog(requireContext())
        }
        return downloadDialog!!
    }
    /**
     * @describe 确认下载 Dialog
     */
    private fun getUpdateConfirmDialog(): UpdateConfirmDialog?{
        if (updateConfirmDialog == null) {
            updateConfirmDialog = UpdateConfirmDialog(requireContext()).apply {
                setOnItemClickListener(object : UpdateConfirmDialog.OnButtonClickCallback{
                    override fun onConfirmButtonClick(dialog: UpdateConfirmDialog) {
                        //下载apk
                        getDownloadDialog()?.show()
                        mainScope.launch {
                            withContext(Dispatchers.Default){
                                val result = downloadApk(RobotStatus.versionStatusModel.value?.path?:"")
                                if (result) {
                                    val root = Environment.getExternalStorageDirectory().absolutePath
                                    val path = RobotStatus.versionStatusModel.value?.path?:""
                                    val filePath = "$root/$path"
                                    InstallApkUtils.executeSuCMD(filePath)
//                                    val deletePath = "$root/${findApkRoot(path)}"
//                                    if(deletePath === "$root/") return@withContext
//                                    deleteFile(deletePath)
                                }else{
                                    getDownloadDialog()?.dismiss()
                                    ToastUtil.show("下载失败")
                                    LogUtil.i("APP更新，下载失败")
                                }
                            }
                        }
                        dialog.dismiss()
                    }

                    override fun onCancelButtonClick(dialog: UpdateConfirmDialog) {
                        dialog.dismiss()
                    }
                })
            }
        }
        return updateConfirmDialog!!
    }

    /**
     * @describe 下载apk
     */
    private fun downloadApk(path: String): Boolean{
        LogUtil.i("开始下载APK,存储路径：${Environment.getExternalStorageDirectory().absolutePath}")
        if (path == "") {
            getDownloadDialog()?.dismiss()
            return false
        }
        val infoUrl: URL?
        var inStream: InputStream? = null
        var httpConnection: HttpURLConnection? = null
        try {
            infoUrl =
                URL("${BuildConfig.HTTP_HOST}/$path")
            LogUtil.i("新版本APP下载地址：$infoUrl")
            val connection: URLConnection = infoUrl.openConnection().apply {
                connectTimeout = 5000
            }
            httpConnection = connection as HttpURLConnection
            val responseCode: Int = httpConnection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val filePath = "${Environment.getExternalStorageDirectory().absolutePath}/$path"
                val file = File(filePath)
                //清除下载失败缓存
                if (file.exists()) {
                    file.delete()
                }
                file.parentFile?.mkdirs()
                file.createNewFile()
                inStream = httpConnection.inputStream
                val fos = FileOutputStream(file)
                val buf = ByteArray(1024 * 8)
                var totalLength = 0
                var len:Int
                while (-1 != (inStream.read(buf,0, buf.size).also { len = it })) {
                    totalLength += len
                    val percent = (1f * totalLength / RobotStatus.versionStatusModel.value?.size!!) * 100
                    downloadPercent = percent.toInt()
                    fos.write(buf,0,len)
                }
                LogUtil.i("fileSize:${file.length()}")
            }
        } catch (e: MalformedURLException) {
            e.printStackTrace()
            getDownloadDialog()?.dismiss()
            return false
        } catch (e: IOException) {
            ToastUtil.show("连接下载地址超时")
            getDownloadDialog()?.dismiss()
            e.printStackTrace()
            return false
        } finally {
            try {
                inStream?.close()
                httpConnection?.disconnect()
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
        return true
    }
    override fun onDestroyView() {
        super.onDestroyView()
        mainScope.cancel()
    }
}
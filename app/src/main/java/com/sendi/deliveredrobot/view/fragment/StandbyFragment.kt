package com.sendi.deliveredrobot.view.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.databinding.FragmentStandbyBinding
import com.sendi.deliveredrobot.entity.Universal
import com.sendi.deliveredrobot.entity.entitySql.QuerySql
import com.sendi.deliveredrobot.helpers.WakeupWordHelper
import com.sendi.deliveredrobot.interfaces.FaceDataListener
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
    private val fastRecognition: FaceRecognition = FaceRecognition()
    private var sendFace: Int = 0 //用来只接收一次人脸数据，否则多次跳转页面时会报错

    override fun onResume() {
        super.onResume()
        binding.Standby.setResume()
    }

    override fun onPause() {
        super.onPause()
        binding.Standby.setPause()
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
            fastRecognition.suerFaceInit(
                extractFeature = false,
                surfaceView = binding.SurfaceView,
                owner = this,
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
            BaseVoiceRecorder.getInstance()?.recordCallback = { _, pinyinString,_ ->
                if (pinyinString.contains(WakeupWordHelper.wakeupWordPinyin ?: "")) {
                    Log.i("AudioChannel", "包含${WakeupWordHelper.wakeupWord}")
                    controller!!.navigate(R.id.action_standbyFragment_to_homeFragment)
                }
            }
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
            } else {
                (imagePaths as ArrayList<Advance>).add(Advance(Universal.gifDefault, "2", 0, 3))
                binding.Standby.setData(imagePaths) // 将数据传入到控件中显示
            }
        } else {
            LogUtil.e("待机时传入无效路径")
        }
    }

    override fun onStop() {
        FaceDataListener.removeOnChangeListener()
        fastRecognition.onDestroy()
        super.onStop()
    }

}
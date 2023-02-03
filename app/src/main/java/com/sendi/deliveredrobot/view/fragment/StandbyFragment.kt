package com.sendi.deliveredrobot.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.sendi.deliveredrobot.BaseFragment
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.databinding.FragmentStandbyBinding
import com.sendi.deliveredrobot.entity.Universal
import com.sendi.deliveredrobot.view.widget.Advance
import com.sendi.deliveredrobot.view.widget.FromeSettingDialog
import com.sendi.deliveredrobot.viewmodel.BaseViewModel
import com.sendi.deliveredrobot.viewmodel.BaseViewModel.checkIsImageFile
import java.io.File
import java.util.*


class StandbyFragment : BaseFragment() {

    private lateinit var binding: FragmentStandbyBinding
    private var imagePaths: List<Advance> = ArrayList()
    private var controller: NavController? = null
    private var baseViewModel: BaseViewModel? = null

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
        controller = Navigation.findNavController(view)
        baseViewModel = ViewModelProvider(this).get(BaseViewModel::class.java)
        getFilesAllNames(Universal.Standby)
        ShowPresentationByDisplaymanager()
        //返回主页面
        binding.imageButton.setOnClickListener {
            controller!!.navigate(R.id.action_standbyFragment_to_homeFragment)
        }
    }

    private fun getFilesAllNames(path: String?) {
        //传入指定文件夹的路径
        val file = File(path)
        val files = file.listFiles()!!
        imagePaths = ArrayList()
        binding.Standby.initView()
        for (value in files) {
            if (checkIsImageFile(value.path)) {
                //图片
                (imagePaths as ArrayList<Advance>).add(Advance(value.path, "2"))
            }else {
                //视频
                (imagePaths as ArrayList<Advance>).add(Advance(value.path, "1"))
            }
            binding.Standby.setData(imagePaths) //将数据传入到控件中显示
        }
    }

//    private fun checkIsImageFiles(fName: String): Boolean {
//        val isImageFile: Boolean
//        //获取拓展名
//        val fileEnd = fName.substring(fName.lastIndexOf(".") + 1).lowercase(Locale.getDefault())
//        isImageFile =
//            fileEnd == "jpg" || fileEnd == "png" || fileEnd == "jpeg" || fileEnd == "bmp"|| fileEnd == "gif"
//        return isImageFile
//    }

}
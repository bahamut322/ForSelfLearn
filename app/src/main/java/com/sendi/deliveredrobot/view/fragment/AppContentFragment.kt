package com.sendi.deliveredrobot.view.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.adapter.ApplicationAdapter
import com.sendi.deliveredrobot.baidutts.BaiduTTSHelper
import com.sendi.deliveredrobot.databinding.FragmentAppContentBinding
import com.sendi.deliveredrobot.entity.FunctionSkip
import com.sendi.deliveredrobot.helpers.WakeupWordHelper
import com.sendi.deliveredrobot.model.ApplicationModel
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.utils.LogUtil
import com.sendi.deliveredrobot.view.widget.FromeSettingDialog
import com.sendi.fooddeliveryrobot.BaseVoiceRecorder


class AppContentFragment : Fragment() {

    private lateinit var binding: FragmentAppContentBinding
    private var controller: NavController? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_app_content, container, false)
        binding = DataBindingUtil.bind(view)!!
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)!!
        controller = Navigation.findNavController(requireView())
        val toSettingDialog = FromeSettingDialog(context)
        updateDataAndRefreshList()
        if (FunctionSkip.selectFunction() == 4) {
            binding.firstFragment.visibility = View.GONE
            binding.llReturn.visibility = View.VISIBLE
        } else {
            binding.firstFragment.visibility = View.VISIBLE
            binding.llReturn.visibility = View.GONE
        }
        //返回按钮
        binding.llReturn.setOnClickListener {
            BaiduTTSHelper.getInstance().stop()
            controller!!.navigate(R.id.homeFragment)
        }
        //设置按钮
        binding.imageViewSetting.setOnClickListener {
            toSettingDialog.show()
            RobotStatus.PassWordToSetting.observe(viewLifecycleOwner) {
                if (RobotStatus.PassWordToSetting.value == true) {
                    try {
                        BaiduTTSHelper.getInstance().stop()
                        controller!!.navigate(R.id.planSettingFragment)
                    } catch (_: Exception) {
                    }
                    toSettingDialog.dismiss()
                    RobotStatus.PassWordToSetting.postValue(false)
                }
            }
            Toast.makeText(context, "点击了：设置", Toast.LENGTH_SHORT).show()
        }

        binding.bubbleTv.setOnClickListener {
            controller?.navigate(R.id.conversationFragment)
        }

        binding.applicationGv.setMaxHeight(520)
        assignment()
    }


    private fun assignment() {
        val applications = listOf(
            ApplicationModel(name = "消防安全", url = "http://www.qmxf119.org.cn/gmxf_list.html"),
            ApplicationModel(name = "党史学习", url = "http://www.12371.cn/dsxx/"),
            ApplicationModel(name = "党建动态", url = "https://www.gzdj.gov.cn/"),
            ApplicationModel(name = "农讲所纪念馆", url = "https://www.gznjs.cn/"),
            )

        // 打印列表中的每个ApplicationModel对象
        applications.forEach { application ->
            println("Name: ${application.name}, URL: ${application.url}")
            //初始化适配器
        }
        binding.applicationGv.adapter = context?.let { ApplicationAdapter(it, applications) }

        binding.applicationGv.onItemClickListener =   AdapterView.OnItemClickListener{
                _, _, position, _ ->
            LogUtil.i("点击了第：${position}项,点击名字：${applications[position].name},链接：${applications[position].url}")
            val args: Bundle = Bundle().apply {
                // 设置 Bundle 对象参数数据
                this.putString("ManagerUrl", applications[position].url)
                this.putString("name",applications[position].name)
            }
            controller?.navigate(R.id.appManagerFragment, args)
        }
    }

    //刷新
    private fun updateDataAndRefreshList() {
        RobotStatus.robotConfig?.observe(viewLifecycleOwner){
            binding.bubbleTv.text = String.format(getString(R.string.ask), it.wakeUpWord)
        }
    }

    override fun onResume() {
        super.onResume()
        BaseVoiceRecorder.getInstance()?.recordCallback = { _, pinyinString,_ ->
            if (pinyinString.contains(WakeupWordHelper.wakeupWordPinyin ?: "")) {
                Log.i("AudioChannel", "包含${WakeupWordHelper.wakeupWord}")
                controller?.navigate(R.id.conversationFragment)
            }
        }
    }

}
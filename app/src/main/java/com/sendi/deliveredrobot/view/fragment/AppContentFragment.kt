package com.sendi.deliveredrobot.view.fragment

import android.os.Bundle
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
import com.sendi.deliveredrobot.helpers.ReplyAppletConfigHelper
import com.sendi.deliveredrobot.helpers.SecondScreenManageHelper
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.utils.InstallApkUtils
import com.sendi.deliveredrobot.utils.LogUtil
import com.sendi.deliveredrobot.utils.UiUtils
import com.sendi.deliveredrobot.view.widget.FromeSettingDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class AppContentFragment : Fragment() {

    companion object{
        const val MAX_GRID_LENGTH = 4
        const val APPLET_TYPE_URL = 1
        const val APPLET_TYPE_APK = 2
        const val APPLET_TYPE_RICH_TEXT = 3
    }

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
            RobotStatus.passWordToSetting.observe(viewLifecycleOwner) {
                if (RobotStatus.passWordToSetting.value == true) {
                    try {
                        BaiduTTSHelper.getInstance().stop()
                        controller!!.navigate(R.id.planSettingFragment)
                    } catch (_: Exception) {
                    }
                    toSettingDialog.dismiss()
                    RobotStatus.passWordToSetting.postValue(false)
                }
            }
            Toast.makeText(context, "点击了：设置", Toast.LENGTH_SHORT).show()
        }
        assignment()
        SecondScreenManageHelper.refreshSecondScreen(SecondScreenManageHelper.STATE_IDLE)
    }


    private fun assignment() {
//        val applications = listOf(
//            ApplicationModel(name = "党史学习", url = "http://www.12371.cn/dsxx/"),
//            ApplicationModel(name = "党建动态", url = "https://www.gzdj.gov.cn/"),
//            ApplicationModel(name = "学习强国", url = "https://www.xuexi.cn/"),
//            ApplicationModel(name = "消防安全", url = "http://www.qmxf119.org.cn/gmxf_list.html"),
//            ApplicationModel(name = "农讲所纪念馆", url = "https://www.gznjs.cn/"),
//        )
        CoroutineScope(Dispatchers.Default).launch {
            val applications = ReplyAppletConfigHelper.queryApplicationModelList()
            withContext(Dispatchers.Main){
                // 打印列表中的每个ApplicationModel对象
                applications.forEach { application ->
                    println("Name: ${application.name}, URL: ${application.url}")
                    //初始化适配器
                }
                binding.applicationGv.apply {
                    val tempNumCounts =  when(applications.size > MAX_GRID_LENGTH){
                        true -> {
                            setPadding(0,UiUtils.dip2px(requireContext(), 168f),0,0)
                            MAX_GRID_LENGTH
                        }
                        false -> {
                            setPadding(0,UiUtils.dip2px(requireContext(), 248f),0,0)
                            applications.size
                        }
                    }
                    numColumns = tempNumCounts
                    val spacing: Int = requestedHorizontalSpacing
                    layoutParams.apply{
                        width = (UiUtils.dip2px(requireContext(), 240f) * tempNumCounts) + spacing * (tempNumCounts - 1)

                    }
                    adapter = context?.let { ApplicationAdapter(it, applications) }
                    onItemClickListener =
                        AdapterView.OnItemClickListener { _, _, position, _ ->
                            LogUtil.i("点击了第：${position}项,点击名字：${applications[position].name},链接：${applications[position].url}")
                            val clickItem = applications[position]
                            when (clickItem.appletType) {
                                APPLET_TYPE_URL -> {
                                    Bundle().apply {
                                        // 设置 Bundle 对象参数数据
                                        this.putString(AppManagerFragment.URL, clickItem.url)
                                        this.putString(AppManagerFragment.NAME, clickItem.name)
                                        this.putInt(AppManagerFragment.TYPE, clickItem.appletType?:-1)
                                        controller?.navigate(R.id.appManagerFragment, this)
                                    }
                                }
                                APPLET_TYPE_APK -> {
                                    val result = InstallApkUtils.launchApp(clickItem.packageName?:"")
                                    if (result) {
                                        Toast.makeText(context, "打开成功", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "打开失败", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                APPLET_TYPE_RICH_TEXT -> {
                                    Bundle().apply {
                                        // 设置 Bundle 对象参数数据
                                        this.putString(AppManagerFragment.URL, clickItem.url)
                                        this.putString(AppManagerFragment.NAME, clickItem.name)
                                        this.putInt(AppManagerFragment.TYPE, clickItem.appletType?:-1)
                                        this.putString(AppManagerFragment.RICH_TEXT, clickItem.content)
                                        controller?.navigate(R.id.appManagerFragment, this)
                                    }
                                }
                                else -> {}
                            }

                            //刷新大屏
                            SecondScreenManageHelper.refreshSecondScreen(SecondScreenManageHelper.STATE_APPLET, applications[position].secondModel)
                        }

                }
            }
        }
    }
}
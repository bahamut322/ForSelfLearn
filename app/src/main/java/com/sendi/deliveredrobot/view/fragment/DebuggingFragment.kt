package com.sendi.deliveredrobot.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.alibaba.fastjson.JSONObject
import com.sendi.deliveredrobot.BuildConfig
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.databinding.FragmentDebuggingBinding
import com.sendi.deliveredrobot.service.UpdateReturn
import com.sendi.deliveredrobot.utils.LogUtil
import com.sendi.deliveredrobot.utils.ToastUtil
import com.sendi.deliveredrobot.viewmodel.DebuggingViewModel
import kotlin.concurrent.thread


/**
 * @author lsz
 * @desc 调试入口
 * @date 2021/9/2 14:31
 **/
class DebuggingFragment : Fragment() {

    protected var isNavigationViewInit = false//记录是否已经初始化过一次视图
    private var mView: View? = null
    private lateinit var binding: FragmentDebuggingBinding
    var controller: NavController? = null

    private val tabName = arrayOf(
        "标签设置",
        "激光地图设置",
        "路径设置",
        "目标点设置",
        "巡航定点设置",
        "单行道设置",
        "虚拟墙设置",
        "限速区设置",
        "调试"
    )
    private var fragmentList = ArrayList<Fragment>()
    val viewModel by viewModels<DebuggingViewModel>({ requireActivity() })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogUtil.d("DebuggingFragment onCreate")
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        LogUtil.d("DebuggingFragment onCreateView")
        mView = inflater.inflate(R.layout.fragment_debugging, container, false)
        binding = DataBindingUtil.bind(mView!!)!!
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        controller = Navigation.findNavController(view)
        if (!isNavigationViewInit) {//初始化过视图则不再进行view和data初始化
            isNavigationViewInit = true
        } else {
            binding.llayoutLogin.visibility = View.GONE
        }
        super.onViewCreated(view, savedInstanceState)
        initView(view)
        initData()
    }

    private fun initData() {
        if (BuildConfig.IS_DEBUG) {
            binding.edtAccount.setText("admin")
            binding.edtPwd.setText("sendirobot")
        }
        fragmentList.clear()
        fragmentList.add(GeneralViewListFragment())
        fragmentList.add(LaserListFragment())
        fragmentList.add(RouteMapListFragment())
        fragmentList.add(TargetMapListFragment())
        fragmentList.add(LimitSpeedListFragment())
        fragmentList.add(VirtualWallListFragment())
        fragmentList.add(DebugSettingFragment())
        fragmentList.add(AutoCruiseFragment())

        viewModel.currentSettingPosition.observe(viewLifecycleOwner) {
            if (it < fragmentList.size) {
                val bt = parentFragmentManager.beginTransaction()
                bt.replace(R.id.flayout_container, fragmentList[it])
                bt.commit()
            }
        }
    }

    fun initView(view: View) {
        binding.btnLogin.apply {
            setOnClickListener {
                var accountStr = binding.edtAccount.getText().toString().trim { it <= ' ' }
                var passwordStr = binding.edtPwd.getText().toString().trim { it <= ' ' }
                if (accountStr == "" || passwordStr == "") {
                    ToastUtil.show(resources.getString(R.string.str_debug_login_input_tip))
                } else {
                    if (accountStr.equals("admin") && passwordStr.equals("sendirobot")) {
                        binding.llayoutLogin.visibility = View.GONE
                    } else {
                        ToastUtil.show(resources.getString(R.string.str_debug_login_error_tip))
                    }
                }
            }
        }
        binding.llReturn.apply {
            setOnClickListener {
                findNavController().popBackStack()
            }
        }

        binding.llayoutReturn.apply {
            setOnClickListener {
                UpdateReturn.method()
                controller!!.navigate(R.id.action_debuggingFragment_to_settingHomeFragment)

            }
        }
        binding.tablayoutDebug.apply {
            setOnTabChangeListener {
                viewModel.currentSettingPosition.value = it
            }
        }
        val debugItems =
            MyApplication.instance!!.resources!!.getStringArray(R.array.debug_items)
        binding.tablayoutDebug.initTab(debugItems, viewModel.currentSettingPosition.value!!)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.currentSettingPosition.value = 0
    }


}
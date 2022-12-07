package com.sendi.deliveredrobot.view.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.hacknife.wifimanager.WifiManager
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.apn.APN
import com.sendi.deliveredrobot.databinding.FragmentDebugSettingBinding
import com.sendi.deliveredrobot.helpers.BasicSettingHelper
import com.sendi.deliveredrobot.helpers.CommonHelper
import com.sendi.deliveredrobot.helpers.ROSHelper
import com.sendi.deliveredrobot.room.dao.DeliveredRobotDao
import com.sendi.deliveredrobot.room.database.DataBaseDeliveredRobotMap
import com.sendi.deliveredrobot.room.entity.BasicConfig
import com.sendi.deliveredrobot.utils.LogUtil
import com.sendi.deliveredrobot.utils.NavigationBarUtil
import com.sendi.deliveredrobot.utils.ToastUtil
import com.sendi.deliveredrobot.view.widget.HideNavigationBarDialog
import com.sendi.deliveredrobot.view.widget.ResetFactoryDataDialog
import com.sendi.deliveredrobot.view.widget.ResetFactoryDataStatusDialog
import com.sendi.deliveredrobot.viewmodel.BasicSettingViewModel
import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

/**
 * @author heky
 * @date 2022-03-18
 * @describe 调试里的设置
 */
class DebugSettingFragment : Fragment() {
    companion object {
        private val EDIT_TEXT_SELECT_COLOR = Color.parseColor("#A0BAEF")
        private val EDIT_TEXT_NOT_SELECT_COLOR = Color.parseColor("#4D6FBE")
    }

    private lateinit var binding: FragmentDebugSettingBinding
    private var apn: APN? = null
    private var resettingDialog: Dialog? = null
    private lateinit var mainScope: CoroutineScope
    private lateinit var dao: DeliveredRobotDao
    private val basicSettingViewModel: BasicSettingViewModel by viewModels({ requireActivity() })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        apn = APN(requireActivity())
        dao = DataBaseDeliveredRobotMap.getDatabase(requireContext()).getDao()
        resettingDialog = getResettingDialog()
    }

    override fun onStart() {
        super.onStart()
        mainScope = MainScope()
    }

    override fun onStop() {
        super.onStop()
        mainScope.cancel()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_debug_setting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)!!
        binding.editTextApnName.apply {
            viewTreeObserver.addOnGlobalLayoutListener {
                val window = activity?.window ?: return@addOnGlobalLayoutListener
                NavigationBarUtil.hideNavigationBar(window)
            }
            setOnFocusChangeListener { _, hasFocus ->
                isSelected = hasFocus
                if (hasFocus) {
                    this.setTextColor(EDIT_TEXT_SELECT_COLOR)
                } else {
                    this.setTextColor(EDIT_TEXT_NOT_SELECT_COLOR)
                }
            }
        }
        binding.editTextMcc.apply {
            setOnFocusChangeListener { _, hasFocus ->
                isSelected = hasFocus
                if (hasFocus) {
                    this.setTextColor(EDIT_TEXT_SELECT_COLOR)
                } else {
                    this.setTextColor(EDIT_TEXT_NOT_SELECT_COLOR)
                }
            }
            setText(apn?.mcc)
            setSelection(apn?.mcc?.length ?: 0)
        }
        binding.editTextMnc.apply {
            setOnFocusChangeListener { _, hasFocus ->
                isSelected = hasFocus
                if (hasFocus) {
                    this.setTextColor(EDIT_TEXT_SELECT_COLOR)
                } else {
                    this.setTextColor(EDIT_TEXT_NOT_SELECT_COLOR)
                }
            }
            setText(apn?.mnc)
            setSelection(apn?.mnc?.length ?: 0)
        }
        binding.textViewApply.apply {
            isClickable = true
            setOnClickListener {
                val apnName = binding.editTextApnName.text.toString()
                val mcc = binding.editTextMcc.text.toString()
                val mnc = binding.editTextMnc.text.toString()
                if (TextUtils.isEmpty(apnName)) {
                    ToastUtil.show("请输入apn")
                    return@setOnClickListener
                }
                if (TextUtils.isEmpty(mcc)) {
                    ToastUtil.show("请输入mcc")
                    return@setOnClickListener
                }
                if (TextUtils.isEmpty(mnc)) {
                    ToastUtil.show("请输入mnc")
                    return@setOnClickListener
                }
                if (apn?.checkAPN(apnName, Uri.parse("content://telephony/carriers")) == true) {
                    ToastUtil.show("${apnName}已经配置过了")
                    return@setOnClickListener
                }
                apn?.mcc = mcc
                apn?.mnc = mnc
                val id = apn?.addAPN(Uri.parse("content://telephony/carriers"), apnName, apnName)
                if (id != null) {
                    if (id > -1) {
                        ToastUtil.show("${apnName}配置成功")
                    }
                }
            }
        }
        binding.textViewFactoryDataReset.apply {
            setOnClickListener {
                getResetFactoryDataDialog().show()
            }
        }
    }

    /**
     * @describe 确认恢复出厂设置
     */
    @SuppressLint("InflateParams")
    private fun getResetFactoryDataDialog(): Dialog {
        val mWindowWidth: Int
        val mWindowHeight: Int
        val dialog =
            HideNavigationBarDialog(requireContext(), R.style.simpleDialogStyle)
        dialog.setCancelable(false)
        val dialogView: View = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_factory_data_reset, null)
        val displayMetrics = MyApplication.instance!!.resources.displayMetrics
        dialogView.findViewById<TextView>(R.id.textViewMessage).apply {
            text = CommonHelper.getDeleteLaserMapTextSpan(
                string1 = "确定要",
                string2 = " 恢复出厂设置 ",
                string3 = "吗？",
                color = ContextCompat.getColor(requireContext(), R.color.color_FF8282)
            )
        }
        dialogView.findViewById<TextView>(R.id.textViewCancel).apply {
            isClickable = true
            setOnClickListener {
                dialog.dismiss()
            }
        }
        dialogView.findViewById<TextView>(R.id.textViewConfirm).apply {
            isClickable = true
            setOnClickListener {
                dialog.dismiss()
                mainScope.launch {
                    getResettingDialog()?.show()
                    val seconds = measureTimeMillis {
                        val resetChassisAsync = async {
                            val result = withContext(Dispatchers.Default) {
                                val chassisResetResponse = ROSHelper.resetFactoryData(1)
                                if (chassisResetResponse != null) {
                                    if (!chassisResetResponse.success) {
                                        LogUtil.e(chassisResetResponse.statusMessage)
                                        ToastUtil.show(chassisResetResponse.statusMessage)
                                        false
                                    } else {
                                        LogUtil.i("底盘恢复出厂成功")
                                        true
                                    }
                                } else {
                                    LogUtil.e("底盘恢复出厂失败")
                                    ToastUtil.show("底盘恢复出厂失败")
                                    false
                                }
                            }
                            result
                        }
                        val resetDatabaseAsync = async {
                            val basicConfig: BasicConfig
                            withContext(Dispatchers.Default){
                                basicConfig = dao.deleteAllData()
                            }
                            //设置亮度
                            BasicSettingHelper.setBrightness(
                                requireActivity(),
                                basicConfig.brightness?: 40
                            )
                            basicSettingViewModel.basicConfig = basicConfig
                            //关闭wifi
                            val manager = WifiManager.create(requireContext())
                            manager.closeWifi()
                            LogUtil.i("清除数据库成功")
                            true
                        }
                        val result = resetChassisAsync.await() && resetDatabaseAsync.await()
                        //延时1秒为了dialog显示一段时间
                        delay(1000)
                        getResettingDialog()?.dismiss()
                        if (result) {
                            //重置成功
                            val resetResultStatusDialog = getResetResultDialog(ResetFactoryDataStatusDialog.RESET_TYPE_SUCCESS)
                            resetResultStatusDialog.show()
                            delay(1000)
                            resetResultStatusDialog.dismiss()
                        }else{
                            //重置失败
                            val resetResultStatusDialog = getResetResultDialog(ResetFactoryDataStatusDialog.RESET_TYPE_FAILURE)
                            resetResultStatusDialog.show()
                            delay(1000)
                            resetResultStatusDialog.dismiss()
                        }
                    }
                    LogUtil.i("恢复出厂用时${seconds - 2000}ms")
                }
            }
        }
        mWindowWidth = displayMetrics.widthPixels
        mWindowHeight = displayMetrics.heightPixels
        dialog.setContentView(
            dialogView, ViewGroup.MarginLayoutParams(
                mWindowWidth,
                mWindowHeight
            )
        )
        return dialog
    }

    /**
     * describe 正在重置Dialog
     */
    private fun getResettingDialog(): Dialog? {
        if (resettingDialog == null) {
            resettingDialog = ResetFactoryDataDialog(requireContext())
        }
        return resettingDialog
    }

    private fun getResetResultDialog(status: Int):Dialog{
        return ResetFactoryDataStatusDialog(requireContext(),status)
    }
}
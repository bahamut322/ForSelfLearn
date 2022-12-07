package com.sendi.deliveredrobot.view.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import chassis_msgs.DoorState
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.RobotCommand
import com.sendi.deliveredrobot.databinding.FragmentPasswordResetBinding
import com.sendi.deliveredrobot.helpers.CommonHelper
import com.sendi.deliveredrobot.helpers.ROSHelper
import com.sendi.deliveredrobot.model.LineInfoModel
import com.sendi.deliveredrobot.room.database.DataBaseDeliveredRobotMap
import com.sendi.deliveredrobot.room.entity.SubMap
import com.sendi.deliveredrobot.utils.ToastUtil
import com.sendi.deliveredrobot.view.widget.CustomIndicatorTextView
import com.sendi.deliveredrobot.view.widget.CustomKeyBoardView
import com.sendi.deliveredrobot.view.widget.HideNavigationBarDialog
import com.sendi.deliveredrobot.viewmodel.BasicSettingViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


/**
 * @author heky
 * @date 2022-08-15
 * @description 重置密码页面
 */
class PasswordResetFragment : Fragment() {
    private lateinit var binding: FragmentPasswordResetBinding
    private val basicSettingViewModel: BasicSettingViewModel by viewModels({ requireActivity() })
    private val dao = DataBaseDeliveredRobotMap.getDatabase(MyApplication.instance!!).getDao()
    private val mainScope = MainScope()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_password_reset, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)!!
        binding.customKeyBoardView.apply {
            setKeyBoardListener(object : CustomKeyBoardView.KeyBoardCallback(){
                override fun onPushText(char: Char) {
                    binding.customIndicatorTextView.addText(char)
                }

                override fun onRemoveText() {
                    super.onRemoveText()
                    binding.customIndicatorTextView.removeText()
                }

                override fun onConfirm() {
                    super.onConfirm()
                    val text = binding.customIndicatorTextView.getText()
                    if (text.length != 5) {
                        ToastUtil.show("密码需为5位")
                        return
                    }
                    getPasswordResetDialog().show()
                }
            })
        }
        binding.customIndicatorTextView.apply {
            val verifyPassword = basicSettingViewModel?.basicConfig?.verifyPassword?:"00000"
            setTextLength(verifyPassword.length)
            for (c in verifyPassword) {
                addText(c)
            }
            setIndicatorTextViewListener(object :
                CustomIndicatorTextView.IndicatorTextViewCallback {
                override fun fullText(text: String) {
                }

                override fun notFull() {
                }

                override fun hasText(text: String) {
                }

                override fun empty() {
                }
            })
        }
    }

    /**
     * @describe 修改密码
     */
    @SuppressLint("InflateParams", "NotifyDataSetChanged")
    private fun getPasswordResetDialog(): Dialog {
        val mWindowWidth: Int
        val mWindowHeight: Int
        val dialog =
            HideNavigationBarDialog(requireContext(), R.style.simpleDialogStyle)
        dialog.setCancelable(false)
        val dialogView: View = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_delete, null)
        val displayMetrics = MyApplication.instance!!.resources.displayMetrics
        dialogView.findViewById<TextView>(R.id.textViewMessage).apply {
            text = "是否确认修改密码？"
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
                val text = binding.customIndicatorTextView.getText()
                basicSettingViewModel.basicConfig.verifyPassword = text
                mainScope.launch(Dispatchers.Default) {
                    dao.updateBasicConfig(basicSettingViewModel.basicConfig)
                }
                if (!checkDoor()){
                    ToastUtil.show(getString(R.string.please_ensure_door_close))
                    return@setOnClickListener
                }
                val jumpStatus: Boolean = this@PasswordResetFragment.findNavController().popBackStack(R.id.homeFragment, false)
                if (!jumpStatus) {
                    //如果开机后没有到达过主页
                    this@PasswordResetFragment.findNavController().popBackStack()
                    this@PasswordResetFragment.findNavController().navigate(R.id.homeFragment)
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
     * @describe 检测仓门状态
     */
    private fun checkDoor(): Boolean{
        val doorOneState = ROSHelper.controlBin(RobotCommand.CMD_CHECK, DoorState.DOOR_ONE)
        val doorTwoState = ROSHelper.controlBin(RobotCommand.CMD_CHECK, DoorState.DOOR_TWO)
        if (doorOneState.toByte() != DoorState.STATE_CLOSED || doorTwoState.toByte() != DoorState.STATE_CLOSED) {
            return false
        }
        return true
    }
}
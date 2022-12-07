package com.sendi.deliveredrobot.view.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.sendi.deliveredrobot.*
import com.sendi.deliveredrobot.databinding.FragmentConfirmSubMapBinding
import com.sendi.deliveredrobot.helpers.DialogHelper
import com.sendi.deliveredrobot.ros.debug.MapLaserServiceImpl
import com.sendi.deliveredrobot.view.widget.HideNavigationBarDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @describe 确认子图完成页面
 */
class ConfirmSubMapFragment : Fragment() {
    lateinit var binding: FragmentConfirmSubMapBinding

    //    private val laserScanViewModel: LaserScanViewModel by viewModels({requireActivity()})
    private val mapLaserServiceImpl = MapLaserServiceImpl.getInstance()
    private lateinit var dialog: HideNavigationBarDialog
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_confirm_sub_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)!!
        dialog = getConfirmDialog()
        binding.laserPointsView.apply {
            if (LaserObject.pauseCheckPoints != null) {
                setLaserPoints(LaserObject.pauseCheckPoints!!)
            }
        }
        binding.textViewReset.apply {
            isClickable = true
            setOnClickListener {
                isClickable = false
                dialog.show()
            }
        }
        binding.textViewConfirm.apply {
            isClickable = true
            setOnClickListener {
                isClickable = false
                //返回激光扫描页面
//                MyApplication.instance!!.sendBroadcast(
//                    Intent().apply {
//                        action = ACTION_NAVIGATE
//                        putExtra(NAVIGATE_ID, POP_BACK_STACK)
//                    }
//                )
                findNavController().popBackStack()
                MainScope().launch {
                    DialogHelper.loadingDialog.show()
                    withContext(Dispatchers.IO) {
                        //下发确认地图
                        mapLaserServiceImpl.saveSubMap()
                    }
                    DialogHelper.loadingDialog.dismiss()
                }
            }
        }
    }

    /**
     * @describe 是否重置子图弹框
     */
    @SuppressLint("InflateParams")
    private fun getConfirmDialog(): HideNavigationBarDialog {
        val mWindowWidth: Int
        val mWindowHeight: Int
        val dialog =
            HideNavigationBarDialog(requireContext(), R.style.simpleDialogStyle)
        val dialogView: View = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_reset_sub_map, null)
        val displayMetrics = resources.displayMetrics
        val textViewConfirm = dialogView.findViewById<TextView>(R.id.textViewConfirm).apply {
            isClickable = true
            setOnClickListener {
                MainScope().launch {
                    isClickable = false
                    LaserObject.clear()
                    withContext(Dispatchers.IO) {
                        mapLaserServiceImpl.resetSubMap()
                    }
                    dialog.dismiss()
                    this@ConfirmSubMapFragment.findNavController().popBackStack(R.id.debuggingFragment,false)
                }
            }
        }
        dialogView.findViewById<TextView>(R.id.textViewCancel).apply {
            isClickable = true
            setOnClickListener {
                textViewConfirm.isClickable = true
                dialog.dismiss()
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
}
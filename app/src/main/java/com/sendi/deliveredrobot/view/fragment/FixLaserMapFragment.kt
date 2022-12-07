package com.sendi.deliveredrobot.view.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayout
import com.sendi.deliveredrobot.*
import com.sendi.deliveredrobot.databinding.FragmentFixLaserMapBinding
import com.sendi.deliveredrobot.ros.debug.MapLaserServiceImpl
import com.sendi.deliveredrobot.utils.PxUtil
import com.sendi.deliveredrobot.view.widget.HideNavigationBarDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @describe 修正激光图页面
 */
class FixLaserMapFragment : Fragment() {
    private lateinit var binding: FragmentFixLaserMapBinding
    private val mapLaserServiceImpl = MapLaserServiceImpl.getInstance()
    private val fragments = arrayListOf(ShowFixLaserMapFragment(), ShowOriginMapFragment())
    private val tabNames = arrayOf("修正图", "原始图")
    private lateinit var dialog: HideNavigationBarDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_fix_laser_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val from = arguments?.getString(KEY_NAVIGATE_FROM)
        binding = DataBindingUtil.bind(view)!!
        dialog = getFixMapDialog()
        binding.tabLayout.apply {
            tabMode = TabLayout.MODE_AUTO
            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    val textView = TextView(activity).apply {
                        textSize = PxUtil.sp2px(context, 36f).toFloat()
                        setTextColor(Color.WHITE)
                        text = tab!!.text
                        gravity = Gravity.CENTER
                    }
                    tab?.customView = textView
                    val bt = parentFragmentManager.beginTransaction()
                    bt.replace(R.id.frameLayoutContainer, fragments[tab!!.position])
                    bt.commit()
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                    tab?.customView = null
                }

                override fun onTabReselected(tab: TabLayout.Tab?) {
//                    val bt = parentFragmentManager.beginTransaction()
//                    bt.detach(fragments[tab!!.position])
//                    bt.attach(fragments[tab.position])
//                    bt.commit()
                }
            })
            for (name in tabNames) {
                addTab(newTab().apply {
                    text = name
                })
            }
            getTabAt(0)?.select()
        }
        binding.textViewGoBack.apply {
            setOnClickListener {
                MainScope().launch {
                    withContext(Dispatchers.Default) {
                        mapLaserServiceImpl.quit()
                    }
                    when (from) {
                        NAVIGATE_FROM_FIX_LASER -> {
                            findNavController().popBackStack(R.id.debuggingFragment,false)
//                            findNavController().navigate(
//                                R.id.debuggingFragment,
//                                Bundle(),
//                                NavOptions.Builder().setPopUpTo(R.id.settingHomeFragment, false).build()
//                            )
                        }
                        NAVIGATE_FROM_RELOCATION -> {
                            findNavController().popBackStack()
                        }
                    }

                }
            }
        }
        binding.textViewFix.apply {
            setOnClickListener {
                dialog.show()
            }
        }
        binding.textViewClearAllFix.apply {
            isClickable = true
            setOnClickListener{
                getCanClearFixDialog().show()
            }
        }
    }

    /**
     * @describe 选择修正图ID Dialog
     */
    @SuppressLint("InflateParams")
    private fun getFixMapDialog(): HideNavigationBarDialog {
        val mWindowWidth: Int
        val mWindowHeight: Int
        val dialog =
            HideNavigationBarDialog(requireContext(), R.style.simpleDialogStyle)
        val dialogView: View = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_choose_fix_map_id, null)
        val displayMetrics = resources.displayMetrics
        val editTextSubMap1 =
            dialogView.findViewById<EditText>(R.id.editTextSubMap1)
        val editTextSubMap2 =
            dialogView.findViewById<EditText>(R.id.editTextSubMap2)
        dialogView.findViewById<TextView>(R.id.textViewFixCommit).apply {
            isClickable = true
            setOnClickListener {
                isEnabled = false
                val text1 = editTextSubMap1.text
                val text2 = editTextSubMap2.text
                if (text1.isBlank() || text2.isBlank()) {
                    isEnabled = true
                    return@setOnClickListener
                }
                try {
                    this@FixLaserMapFragment.findNavController()
                        .navigate(R.id.adjustFixMapFragment, Bundle().apply {
                            putInt(FIRST_MAP_ID, text1.toString().trim().toInt())
                            putInt(SECOND_MAP_ID, text2.toString().trim().toInt())
                        })
                    dialog.dismiss()
                } catch (e: Exception) {
                    isEnabled = true
                }
            }
        }
        dialogView.findViewById<View>(R.id.viewDismiss).apply {
            isClickable = true
            setOnClickListener {
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

    /**
     * @describe 清除所有修正
     */
    @SuppressLint("InflateParams")
    private fun getCanClearFixDialog(): Dialog {
        val mWindowWidth: Int
        val mWindowHeight: Int
        val dialog =
            HideNavigationBarDialog(requireContext(), R.style.simpleDialogStyle)
        dialog.setCancelable(false)
        val dialogView: View = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_can_not_delete_laser_map, null)
        val displayMetrics = MyApplication.instance!!.resources.displayMetrics
        dialogView.findViewById<TextView>(R.id.textViewMessage).apply {
            text = "确认要清除所有修正吗？"
        }
        dialogView.findViewById<TextView>(R.id.textViewConfirm).apply {
            isClickable = true
            setOnClickListener {
                mapLaserServiceImpl.clearUpdateMessage()
                dialog.dismiss()
                val position = binding.tabLayout.selectedTabPosition
                binding.tabLayout.getTabAt(position)?.select()
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
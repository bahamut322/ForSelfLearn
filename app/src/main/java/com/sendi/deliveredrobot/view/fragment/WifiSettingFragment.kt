package com.sendi.deliveredrobot.view.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.hacknife.wifimanager.*
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.adapter.WifiAdapter
import com.sendi.deliveredrobot.adapter.base.i.OnItemClickListener
import com.sendi.deliveredrobot.databinding.FragmentWifiSettingBinding
import com.sendi.deliveredrobot.room.database.DataBaseDeliveredRobotMap
import com.sendi.deliveredrobot.utils.LogUtil
import com.sendi.deliveredrobot.view.widget.HideNavigationBarDialog
import com.sendi.deliveredrobot.viewmodel.BasicSettingViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class WifiSettingFragment : Fragment(), OnWifiChangeListener, OnWifiConnectListener,
    OnWifiStateChangeListener {
    private lateinit var binding: FragmentWifiSettingBinding
    private lateinit var manager: IWifiManager
    private lateinit var wifiAdapter: WifiAdapter
    private val basicSettingViewModel: BasicSettingViewModel by viewModels({ requireActivity() })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_wifi_setting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)!!
        manager = WifiManager.create(context)
        manager.setOnWifiChangeListener(this)
        manager.setOnWifiConnectListener(this)
        manager.setOnWifiStateChangeListener(this)
        wifiAdapter = WifiAdapter().apply {
            setOnRecyclerViewListener(object : OnItemClickListener<IWifi> {
                @SuppressLint("InflateParams")
                override fun onItemClick(wifi: IWifi?) {
                    if (wifi?.isConnected!!) {
                        // wifi已连接
                    } else if (wifi.isSaved) {
                        manager.connectSavedWifi(wifi)
                    } else if (!wifi.isEncrypt) {
                        manager.connectOpenWifi(wifi)
                    } else {
                        val mWindowWidth: Int
                        val mWindowHeight: Int
                        val dialog =
                            HideNavigationBarDialog(requireContext(), R.style.Dialog)
                        val dialogView: View = LayoutInflater.from(requireContext())
                            .inflate(R.layout.dialog_wifi_password, null)
                        val displayMetrics = this@WifiSettingFragment.resources.displayMetrics
                        val editTextWifiPassword =
                            dialogView.findViewById<EditText>(R.id.editTextWifiPassword)
                        dialogView.findViewById<TextView>(R.id.textViewTitle).apply {
                            text = String.format(
                                this@WifiSettingFragment.resources.getString(
                                    R.string.connect_wifi_reminder,
                                    wifi.name()
                                )
                            )
                        }
                        dialogView.findViewById<TextView>(R.id.textViewWifiCommit).apply {
                            setOnClickListener {
                                manager.connectEncryptWifi(
                                    wifi,
                                    editTextWifiPassword.text.toString()
                                )
                                dialog.dismiss()
                            }
                        }
                        dialogView.findViewById<ImageView>(R.id.imageViewDialogCancel).apply {
                            isClickable = true
                            setOnClickListener {
                                dialog.dismiss()
                            }
                        }
//                        dialogView.apply {
//                            setOnClickListener {
//                                dialog.dismiss()
//                            }
//                        }
                        mWindowWidth = displayMetrics.widthPixels
                        mWindowHeight = displayMetrics.heightPixels
                        dialog.setContentView(
                            dialogView, ViewGroup.MarginLayoutParams(
                                mWindowWidth,
                                mWindowHeight
                            )
                        )
                        dialog.show()
                    }
                }
            })
        }
        binding.recyclerViewWifi.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = wifiAdapter
        }
        binding.switchOpenWifi.apply {
            setOnCheckedChangeListener { _, isChecked ->
                basicSettingViewModel.basicConfig.wifiOpen = when (isChecked) {
                    true -> {
                        manager.openWifi()
                        with(binding.frameLayoutWifiOpenDown) {
                            visibility = View.VISIBLE
                        }
                        with(binding.recyclerViewWifi) {
                            visibility = View.VISIBLE
                        }
                        1
                    }
                    false -> {
                        manager.closeWifi()
                        with(binding.frameLayoutWifiOpenDown) {
                            visibility = View.GONE
                        }
                        with(binding.recyclerViewWifi) {
                            visibility = View.GONE
                        }
                        0
                    }
                }
                val dao = DataBaseDeliveredRobotMap.getDatabase(MyApplication.instance!!).getDao()
                MainScope().launch {
                    withContext(Dispatchers.Default){
                        dao.updateBasicConfig(basicConfig = basicSettingViewModel.basicConfig)
                    }
                }
            }
            isChecked = (basicSettingViewModel.basicConfig.wifiOpen ?: 0) == 1
            //因为switch的状态与之前相同的情况下不会进回调，所以额外写一次
            when (isChecked) {
                true -> {
                    with(binding.frameLayoutWifiOpenDown) {
                        visibility = View.VISIBLE
                    }
                    with(binding.recyclerViewWifi) {
                        visibility = View.VISIBLE
                    }
                }
                false -> {
                    with(binding.frameLayoutWifiOpenDown) {
                        visibility = View.GONE
                    }
                    with(binding.recyclerViewWifi) {
                        visibility = View.GONE
                    }
                }
            }
        }
    }

    override fun onWifiChanged(wifis: MutableList<IWifi>?) {
        if (wifis != null && wifis.size > 0) {
            val connectWifi = wifis[0]
            binding.textViewConnectWifi.text = connectWifi.name() ?: ""
            binding.textViewDescribe.text = connectWifi.description2() ?: ""
            with(binding.imageViewConnectStatus) {
                if (!connectWifi.isEncrypt) {
                    when {
                        connectWifi.level() <= -100 -> this.setBackgroundResource(R.drawable.ic_wifi_level_0)
                        connectWifi.level() in -99..-88 || connectWifi.level() in -87..-66 || connectWifi.level() in -65 until 55 -> setBackgroundResource(
                            R.drawable.ic_wifi_level_1
                        )
                        connectWifi.level() >= -55 -> setBackgroundResource(R.drawable.ic_wifi_level_2)
                    }
                } else {
                    when {
                        connectWifi.level() <= -100 -> setBackgroundResource(R.drawable.ic_wifi_level_lock_0)
                        connectWifi.level() in -99..-88 || connectWifi.level() in -87..-66 || connectWifi.level() in -65 until 55 -> setBackgroundResource(
                            R.drawable.ic_wifi_level_lock_1
                        )
                        connectWifi.level() >= -55 -> setBackgroundResource(R.drawable.ic_wifi_level_lock_2)
                    }
                }
            }
        }
        wifiAdapter.bindData(wifis)

    }

    override fun onConnectChanged(status: Boolean) {
        LogUtil.i("wifi：status-->$status")
    }

    override fun onStateChanged(state: State?) {
        LogUtil.i("wifi：state-->$state")
    }
}
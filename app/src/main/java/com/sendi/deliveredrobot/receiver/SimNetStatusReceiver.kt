package com.sendi.deliveredrobot.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build
import android.telephony.PhoneStateListener
import android.telephony.SignalStrength
import android.telephony.TelephonyManager
import androidx.annotation.RequiresApi
import com.sendi.deliveredrobot.MainActivity
import com.sendi.deliveredrobot.databinding.ActivityMainBinding
import com.sendi.deliveredrobot.enum.NetworkOperator

/**
 *   @author: heky
 *   @date: 2021/7/7 15:04
 *   @describe: 移动网络状态
 */
class SimNetStatusReceiver : BroadcastReceiver() {
    lateinit var binding: ActivityMainBinding

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            ConnectivityManager.CONNECTIVITY_ACTION -> { // 监听网络连接，包括wifi和移动数据的打开和关闭,以及连接上可用的连接都会接到监听
                //获取联网状态的NetworkInfo对象
                val info: NetworkInfo? =
                    intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO)
                if (info != null) {
                    //如果当前的网络连接成功并且网络连接可用
                    if (NetworkInfo.State.CONNECTED == info.state && info.isAvailable) {
                        if (info.type == ConnectivityManager.TYPE_WIFI
                            || info.type == ConnectivityManager.TYPE_MOBILE
                        ) {
                            val tm =
                                context?.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                            tm.listen(object : PhoneStateListener() {
                                @RequiresApi(Build.VERSION_CODES.M)
                                override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
                                    super.onSignalStrengthsChanged(signalStrength)
                                    with(binding.imageViewNetStatus) {
                                        visibility = android.view.View.VISIBLE
                                        when (signalStrength.level) {
                                            0 -> setBackgroundResource(com.sendi.deliveredrobot.R.drawable.ic_net_status_0)
                                            1 -> setBackgroundResource(com.sendi.deliveredrobot.R.drawable.ic_net_status_1)
                                            2 -> setBackgroundResource(com.sendi.deliveredrobot.R.drawable.ic_net_status_2)
                                            3 -> setBackgroundResource(com.sendi.deliveredrobot.R.drawable.ic_net_status_3)
                                            4 -> setBackgroundResource(com.sendi.deliveredrobot.R.drawable.ic_net_status_4)
                                        }
                                    }
                                }
                            }, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS)
                        }
                    } else {
                        with(binding.imageViewNetStatus) {
                            visibility = android.view.View.GONE
                        }
                    }
                }
            }

            MainActivity.ACTION_SIM_STATE_CHANGED -> {
                val tm = context?.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                when (tm.simState) {
                    TelephonyManager.SIM_STATE_READY -> {
                        val mcc = context.resources.configuration.mcc
                        val mnc = context.resources.configuration.mnc
                        val operator = NetworkOperator.from(mcc * 100 + mnc)
                        with(binding.textViewNetCompany) {
                            visibility = android.view.View.VISIBLE
                            text = operator.opName
                        }
                    }
                    TelephonyManager.SIM_STATE_UNKNOWN, TelephonyManager.SIM_STATE_ABSENT, TelephonyManager.SIM_STATE_PIN_REQUIRED, TelephonyManager.SIM_STATE_PUK_REQUIRED, TelephonyManager.SIM_STATE_NETWORK_LOCKED -> {

                    }
                    else -> {
                        with(binding.textViewNetCompany) {
                            visibility = android.view.View.GONE
                        }
                    }
                }
            }
        }
    }
}
package com.sendi.deliveredrobot.holder

import android.net.wifi.ScanResult
import android.text.TextUtils
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sendi.deliveredrobot.R

class WifiListHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val frameLayoutContainer: FrameLayout = itemView.findViewById(R.id.frameLayoutContainer)
    private val textViewWifiName = itemView.findViewById<TextView>(R.id.textViewWifiName)
    private val imageViewWifiStatus = itemView.findViewById<ImageView>(R.id.imageViewWifiStatus)
    var data: ScanResult? = null
        set(value) {
            with(textViewWifiName) {
                text = value?.SSID
            }
            with(imageViewWifiStatus) {
                if (TextUtils.isEmpty(value?.capabilities)) {
                    when {
                        value?.level!! <= -100 -> setBackgroundResource(R.drawable.ic_wifi_level_0)
                        value.level in -99..-88 || value.level in -87..-66 || value.level in -65 until 55 -> setBackgroundResource(
                            R.drawable.ic_wifi_level_1
                        )
                        value.level >= -55 -> setBackgroundResource(R.drawable.ic_wifi_level_2)
                    }
                } else {
                    when {
                        value?.level!! <= -100 -> setBackgroundResource(R.drawable.ic_wifi_level_lock_0)
                        value.level in -99..-88 || value.level in -87..-66 || value.level in -65 until 55 -> setBackgroundResource(
                            R.drawable.ic_wifi_level_lock_1
                        )
                        value.level >= -55 -> setBackgroundResource(R.drawable.ic_wifi_level_lock_2)
                    }
                }
            }
            field = value
        }
}
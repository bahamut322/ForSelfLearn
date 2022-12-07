package com.sendi.deliveredrobot.holder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.hacknife.wifimanager.IWifi
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.adapter.base.i.BaseRecyclerViewHolder

class WifiViewHolder(itemView: View?) : BaseRecyclerViewHolder<IWifi>(itemView) {
    private var textViewWifiName: TextView = itemView!!.findViewById(R.id.textViewWifiName)
    private var imageViewWifiStatus: ImageView = itemView!!.findViewById(R.id.imageViewWifiStatus)

    override fun bindData(entity: IWifi) {
        textViewWifiName.text = entity.name()

        with(imageViewWifiStatus) {
            if (!entity.isEncrypt) {
                when {
                    entity.level() <= -100 -> this.setBackgroundResource(R.drawable.ic_wifi_level_0)
                    entity.level() in -99..-88 || entity.level() in -87..-66 || entity.level() in -65 until 55 -> setBackgroundResource(
                        R.drawable.ic_wifi_level_1
                    )
                    entity.level() >= -55 -> setBackgroundResource(R.drawable.ic_wifi_level_2)
                }
            } else {
                when {
                    entity.level() <= -100 -> setBackgroundResource(R.drawable.ic_wifi_level_lock_0)
                    entity.level() in -99..-88 || entity.level() in -87..-66 || entity.level() in -65 until 55 -> setBackgroundResource(
                        R.drawable.ic_wifi_level_lock_1
                    )
                    entity.level() >= -55 -> setBackgroundResource(R.drawable.ic_wifi_level_lock_2)
                }
            }
        }
    }
}
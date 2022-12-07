package com.sendi.deliveredrobot.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.model.SettingHomeModel

class SettingHomeViewModel : ViewModel() {
    var currentSettingPosition = MutableLiveData(0)
    val data = ArrayList<SettingHomeModel>()

    init {
        val settingItems =
            MyApplication.instance!!.resources!!.getStringArray(R.array.setting_items)
        for (index in settingItems.indices) {
            data.add(SettingHomeModel(false, settingItems[index]))
        }
        data[0].selected = true
    }
}
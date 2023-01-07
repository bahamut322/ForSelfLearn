package com.sendi.deliveredrobot.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.model.DebuggingModel

class DebuggingViewModel : ViewModel() {
    var currentSettingPosition = MutableLiveData(0)
    val data = ArrayList<DebuggingModel>()

    init {
        val debugItems =
            MyApplication.instance!!.resources!!.getStringArray(R.array.debug_items)
        for (index in debugItems.indices) {
            data.add(DebuggingModel(false, debugItems[index]))
        }
        data[0].selected = true
    }
}
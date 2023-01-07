package com.sendi.deliveredrobot.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.model.CommonHandleExceptionModel

/**
 * @author heky
 * @date 2022-08-01
 * @description 通用处理异常Model
 */

class CommonHandleExceptionViewModel : ViewModel() {
    var currentPosition = MutableLiveData(0)
    val data = ArrayList<CommonHandleExceptionModel>()

    init {
        val items =
            MyApplication.instance!!.resources!!.getStringArray(R.array.common_handle_exception)
        for (index in items.indices) {
            data.add(CommonHandleExceptionModel(false, items[index]))
        }
        data[0].selected = true
    }
}
package com.sendi.deliveredrobot.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 *   @author: heky
 *   @date: 2021/9/12 10:04
 *   @describe:
 */
class DateViewModel :ViewModel() {
    val date : MutableLiveData<String> = MutableLiveData()
}
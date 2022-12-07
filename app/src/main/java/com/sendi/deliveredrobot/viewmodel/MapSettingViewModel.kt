package com.sendi.deliveredrobot.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MapSettingViewModel : ViewModel() {
    val currentMapName  = MutableLiveData<String>("")
    val currentChargePile = MutableLiveData<String>("")
}
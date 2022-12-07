package com.sendi.deliveredrobot.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sendi.deliveredrobot.model.PlaceModel
import com.sendi.deliveredrobot.model.RemoteOrderModel
import com.sendi.deliveredrobot.navigationtask.BillManager
import com.sendi.deliveredrobot.navigationtask.ITaskBill
import com.sendi.deliveredrobot.room.entity.QueryPointEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class SendPlaceBin2ViewModel : BasePlaceBinViewModel() {
    val binMarkBin2 = 0xff
    override fun binMarBin(): Int {
        return binMarkBin2
    }
}
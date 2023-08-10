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

abstract class BasePlaceBinViewModel: ViewModel() {
    var place = MutableLiveData<String>()
    var data = ArrayList<ArrayList<PlaceModel>>()
    var previousRow = MutableLiveData(-1)
    var previousCol = MutableLiveData(-1)
    var previousTaskFinished = true
    var previousRemoteOrderPutFinished = true  //小程序下单 放物 是否成功
    var previousRemoteOrderSendFinished = true //小程序下单 送物 是否成功
    var remoteOrderModel: RemoteOrderModel? = null //小程序下单
    private var bill: ITaskBill? = null

    fun setCurrentSelected(fragmentIndex: Int, position: Int) {
        if (previousRow.value!! > -1 || previousCol.value!! > -1)
            data[previousRow.value!!][previousCol.value!!].selected = false
        data[fragmentIndex][position].selected = true
        previousRow.value = fragmentIndex
        previousCol.value = position

    }

    fun getCurrentSelectedName(): String {
        if (previousRow.value!! > -1 && previousCol.value!! > -1)
            return "${data[previousRow.value!!][previousCol.value!!].location.pointName}"
        return ""
    }

    fun getCurrentSelectedLocation(): QueryPointEntity? {
        if (previousRow.value!! > -1 && previousCol.value!! > -1)
            return data[previousRow.value!!][previousCol.value!!].location.apply {
//                binMark = binMarBin()
            }
        return null
    }

    fun clearSelected() {
        MainScope().launch(Dispatchers.Main) {
            if (previousRow.value!! > -1 && previousCol.value!! > -1) {
                data[previousRow.value!!][previousCol.value!!].selected = false
                previousRow.value = -1
                previousCol.value = -1
            }
            place.value = ""
        }
    }

    fun resetBill(){
        try {
            BillManager.clearBill(bill)
        }finally {
            bill = null
        }
    }

    fun setBill(bill: ITaskBill?){
        this.bill = bill
    }

    fun hasBill(): Boolean{
        return bill != null
    }

    abstract fun binMarBin(): Int
}
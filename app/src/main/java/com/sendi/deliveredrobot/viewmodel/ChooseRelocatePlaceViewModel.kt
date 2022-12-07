package com.sendi.deliveredrobot.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sendi.deliveredrobot.model.PlaceModel
import com.sendi.deliveredrobot.room.entity.QueryPointEntity

class ChooseRelocatePlaceViewModel:ViewModel(){
    var place = MutableLiveData<String>()
    var data = ArrayList<ArrayList<PlaceModel>>()
    var previousRow = MutableLiveData(-1)
    var previousCol = MutableLiveData(-1)

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
            return data[previousRow.value!!][previousCol.value!!].location
        return null
    }

    fun clearSelected() {
        if (previousRow.value!! > -1 && previousCol.value!! > -1) {
            data[previousRow.value!!][previousCol.value!!].selected = false
            previousRow.value = -1
            previousCol.value = -1
        }
    }
}
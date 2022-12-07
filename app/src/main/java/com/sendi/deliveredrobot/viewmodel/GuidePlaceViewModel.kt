package com.sendi.deliveredrobot.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sendi.deliveredrobot.model.PlaceModel
import com.sendi.deliveredrobot.room.entity.QueryPointEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GuidePlaceViewModel : ViewModel() {
//    var place = MutableLiveData<String>()
    lateinit var data: ArrayList<ArrayList<PlaceModel>>
    private var previousRow = MutableLiveData(-1)
    private var previousCol = MutableLiveData(-1)

    fun setCurrentSelected(fragmentIndex: Int, position: Int) {
        if (previousRow.value!! > -1 || previousCol.value!! > -1)
            data[previousRow.value!!][previousCol.value!!].selected = false
        data[fragmentIndex][position].selected = true
        previousRow.value = fragmentIndex
        previousCol.value = position
    }

    fun getCurrentSelected(): QueryPointEntity? {
        if (previousRow.value!! > -1 || previousCol.value!! > -1) {
            return data[previousRow.value!!][previousCol.value!!].location
        }
        return null
    }

    fun getCurrentSelectedName(): String {
        if (previousRow.value!! > -1 && previousCol.value!! > -1)
            return "${data[previousRow.value!!][previousCol.value!!].location.pointName}"
        return ""
    }

    suspend fun clearGuidePlace(){
        withContext(Dispatchers.Main){
            previousRow.value = -1
            previousCol.value = -1
        }

    }
}
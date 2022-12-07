package com.sendi.deliveredrobot.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sendi.deliveredrobot.model.AllMapRelationshipModel
import com.sendi.deliveredrobot.room.entity.QueryPointEntity

class CreateGeneralViewViewModel : ViewModel() {

    var relationshipData = ArrayList<AllMapRelationshipModel>()
    var stepIndex = MutableLiveData(1)

    var previewRow = MutableLiveData(-1)
    var previewCol = MutableLiveData(-1)
    var previewTaskFinished = true


    fun getCurrentSelectedLocation(): QueryPointEntity? {
        if (previewRow.value!! > -1 && previewCol.value!! > -1)
            return null
        return null
    }


}
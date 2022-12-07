package com.sendi.deliveredrobot.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModelLazy
import com.sendi.deliveredrobot.MainActivity
import com.sendi.deliveredrobot.viewmodel.SendPlaceBin1ViewModel
import com.sendi.deliveredrobot.viewmodel.SendPlaceBin2ViewModel

/**
 *   @author: heky
 *   @date: 2021/8/11 11:26
 *   @describe:送物任务结束Receiver
 */
class SendTaskFinishReceiver : BroadcastReceiver() {
    val viewModelBin1 = ViewModelLazy(
        SendPlaceBin1ViewModel::class,
        { MainActivity.instance.viewModelStore },
        { MainActivity.instance.defaultViewModelProviderFactory}
    )
    val viewModelBin2 = ViewModelLazy(
        SendPlaceBin2ViewModel::class,
        { MainActivity.instance.viewModelStore },
        { MainActivity.instance.defaultViewModelProviderFactory}
    )
    override fun onReceive(context: Context?, intent: Intent?) {
        with(viewModelBin1.value) {
            if (previousTaskFinished) {
                clearSelected()
//                previewTaskFinished = false
            }
        }
        with(viewModelBin2.value) {
            if (previousTaskFinished) {
                clearSelected()
//                previewTaskFinished = false
            }
        }
    }
}
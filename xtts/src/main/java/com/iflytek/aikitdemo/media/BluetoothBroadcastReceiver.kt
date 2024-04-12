package com.iflytek.aikitdemo.media

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothAdapter.*
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager

/**
 * @Desc:
 * @Author leon
 * @Date 2023/10/9-10:34
 * Copyright 2023 iFLYTEK Inc. All Rights Reserved.
 */
class BluetoothBroadcastReceiver : BroadcastReceiver() {

    private var audioManager: AudioDeviceManager? = null
    override fun onReceive(context: Context?, intent: Intent?) {
        if (audioManager == null && context != null) {
            audioManager = AudioDeviceManager(context)
        }
        val action = intent?.action ?: return
        when (action) {
            BluetoothDevice.ACTION_ACL_CONNECTED -> {
                audioManager?.startBluetoothSco()
            }

            BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                audioManager?.stopBluetoothSco()
            }

            BluetoothAdapter.ACTION_STATE_CHANGED -> {
                when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0)) {
                    BluetoothAdapter.STATE_OFF -> {
                        audioManager?.stopBluetoothSco()
                    }

                    BluetoothAdapter.STATE_ON -> {
                        audioManager?.startBluetoothSco()
                    }
                }
            }
        }
    }
}
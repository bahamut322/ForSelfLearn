package com.sendi.deliveredrobot.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.sendi.deliveredrobot.*
import com.sendi.deliveredrobot.NAVIGATE_BUNDLE
import com.sendi.deliveredrobot.NAVIGATE_ID
import com.sendi.deliveredrobot.NAVIGATE_TO_HOME
import com.sendi.deliveredrobot.POP_BACK_STACK
import com.sendi.deliveredrobot.navigationtask.virtualTaskExecute
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 *   @author: heky
 *   @date: 2021/7/7 15:04
 *   @describe: 导航任务Receiver
 */
class NavigationReceiver : BroadcastReceiver() {
    lateinit var navController: NavController
    override fun onReceive(context: Context?, intent: Intent?) {
        val bundle = intent?.getBundleExtra(NAVIGATE_BUNDLE)
        intent?.getIntExtra(NAVIGATE_ID, -1)?.let {
            when (it) {
                POP_BACK_STACK -> navController.popBackStack()
                NAVIGATE_TO_HOME -> {
                    navController.popBackStack(R.id.homeFragment,false)
                }
                R.id.guidingFragment -> {
                    navController.navigate(
                        R.id.guidingFragment,
                        Bundle(),
                        NavOptions.Builder()
//                            .setPopUpTo(R.id.homeFragment, true)
                            .setEnterAnim(R.anim.fade_in)
                            .setExitAnim(R.anim.fade_out)
                            .setPopEnterAnim(R.anim.fade_in)
                            .setPopExitAnim(R.anim.fade_out)
                            .build()
                    )
                }
                R.id.sendingFragment -> {
                    navController.navigate(
                        R.id.sendingFragment,
                        Bundle(),
                        NavOptions.Builder()
//                            .setPopUpTo(R.id.homeFragment, true)
                            .setEnterAnim(R.anim.fade_in)
                            .setExitAnim(R.anim.fade_out)
                            .setPopEnterAnim(R.anim.fade_in)
                            .setPopExitAnim(R.anim.fade_out)
                            .build()
                    )
                }
                else -> {
                    if(navController.currentDestination?.label == "callRoomFragment"
                        || navController.currentDestination?.label == "guideArriveFragment"){
                            //如果当前是呼叫房间或者到达地点，延时两秒再跳页面
                        MainScope().launch {
                            virtualTaskExecute(2, "呼叫房间或者到达地点")
                            navController.navigate(
                                it, bundle, NavOptions.Builder()
                                    .setEnterAnim(R.anim.fade_in)
                                    .setExitAnim(R.anim.fade_out)
                                    .setPopEnterAnim(R.anim.fade_in)
                                    .setPopExitAnim(R.anim.fade_out)
                                    .build()
                            )
                        }
                        return
                    }
                    navController.navigate(
                        it, bundle, NavOptions.Builder()
                            .setEnterAnim(R.anim.fade_in)
                            .setExitAnim(R.anim.fade_out)
                            .setPopEnterAnim(R.anim.fade_in)
                            .setPopExitAnim(R.anim.fade_out)
                            .build()
                    )
                }
            }
        }
    }
}
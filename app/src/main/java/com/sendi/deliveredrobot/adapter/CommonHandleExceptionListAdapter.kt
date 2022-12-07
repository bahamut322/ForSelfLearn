package com.sendi.deliveredrobot.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import chassis_msgs.DoorState
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.RobotCommand
import com.sendi.deliveredrobot.helpers.ROSHelper
import com.sendi.deliveredrobot.holder.CommonHandleExceptionHolder
import com.sendi.deliveredrobot.topic.DoorStateTopic
import com.sendi.deliveredrobot.view.fragment.HandleExceptionControlDoorFragment
import com.sendi.deliveredrobot.viewmodel.CommonHandleExceptionViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class CommonHandleExceptionListAdapter(val viewModel: CommonHandleExceptionViewModel) :
    Adapter<CommonHandleExceptionHolder>() {
    private val mainScope = MainScope()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommonHandleExceptionHolder {
        return CommonHandleExceptionHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_common_handle_exception, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return viewModel.data.size
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: CommonHandleExceptionHolder, position: Int) {
        with(holder.textView) {
            isClickable = true
            setOnClickListener {
                when (position) {
                    0 -> {
//                        for (model in viewModel.data) {
//                            model.selected = false
//                        }
//                        viewModel.data[position].selected = true
//                        holder.data = viewModel.data[position]
//                        viewModel.currentPosition.value = position
//                        notifyDataSetChanged()
                        switchTab(position, holder)
                    }
                    1 -> {
                        val resultCode = checkDoor()
                        when (resultCode) {
                            0 -> {
                                //都关了
                                switchTab(position, holder)
                            }
                            1, 3 -> {
                                //1没关
                                ROSHelper.controlBin(cmd = RobotCommand.CMD_CLOSE, door = DoorState.DOOR_ONE)
                            }
                            2 -> {
                                //2没关
                                ROSHelper.controlBin(cmd = RobotCommand.CMD_CLOSE, door = DoorState.DOOR_TWO)
                            }
                        }
                        DoorStateTopic.setDoorStateListener {
                                doorState ->
                            val state = doorState.state
                            val door = doorState.door
                            when (state) {
                                DoorState.STATE_OPENED -> {
                                }
                                DoorState.STATE_CLOSED -> {
                                    HandleExceptionControlDoorFragment.controling = false
                                    when (door) {
                                        DoorState.DOOR_ONE -> {
                                            when (resultCode) {
                                                1 -> {
                                                    mainScope.launch(Dispatchers.Main){
                                                        switchTab(position, holder)
                                                    }
                                                }
                                                3 -> {
                                                    ROSHelper.controlBin(cmd = RobotCommand.CMD_CLOSE, door = DoorState.DOOR_TWO)
                                                }
                                            }
                                        }
                                        DoorState.DOOR_TWO -> {
                                            mainScope.launch(Dispatchers.Main){
                                                switchTab(position, holder)
                                            }
                                        }
                                    }
                                }
                                DoorState.STATE_OPENING -> {
                                }
                                DoorState.STATE_CLOSING -> {
                                    HandleExceptionControlDoorFragment.controling = true
                                }
                                DoorState.STATE_OPEN_FAILED -> {
                                }
                                DoorState.STATE_CLOSE_FAILED -> {
                                    HandleExceptionControlDoorFragment.controling = false
                                }
                                DoorState.STATE_HALF_OPEN -> {
                                    HandleExceptionControlDoorFragment.controling = false
                                }
                            }
                        }
                    }
                }

            }
            holder.data = viewModel.data[position]
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun switchTab(position: Int, holder: CommonHandleExceptionHolder){
        for (model in viewModel.data) {
            model.selected = false
        }
        viewModel.data[position].selected = true
        holder.data = viewModel.data[position]
        viewModel.currentPosition.value = position
        notifyDataSetChanged()
    }

    /**
     * @describe 检测仓门状态
     */
    private fun checkDoor(): Int {
        var result = 0
        // 检测仓门是否关闭
        var state =  ROSHelper.controlBin(cmd = RobotCommand.CMD_CHECK, door = DoorState.DOOR_ONE)
        if(state != 2){
            result = result or 1
        }
        state =  ROSHelper.controlBin(cmd = RobotCommand.CMD_CHECK, door = DoorState.DOOR_TWO)
        if(state != 2){
            result = result or 2
        }
        return result
    }
}
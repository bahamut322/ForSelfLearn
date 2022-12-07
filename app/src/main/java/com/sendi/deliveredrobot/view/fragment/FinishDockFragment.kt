package com.sendi.deliveredrobot.view.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.sendi.deliveredrobot.*
import com.sendi.deliveredrobot.ACTION_NAVIGATE
import com.sendi.deliveredrobot.NAVIGATE_ID
import com.sendi.deliveredrobot.NAVIGATE_TO_HOME
import com.sendi.deliveredrobot.databinding.FragmentFinishDockBinding
import com.sendi.deliveredrobot.navigationtask.virtualTaskExecute
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * @describe 结束自主充电
 */
class FinishDockFragment : Fragment() {
    private lateinit var binding: FragmentFinishDockBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_finish_dock, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)!!
        Glide.with(this).asGif().load(R.raw.dock_success).into(binding.imageViewFinishDock)
        MainScope().launch {
            virtualTaskExecute(2, "结束自主充电")
            MyApplication.instance!!.sendBroadcast(
                Intent().apply {
                    action = ACTION_NAVIGATE
                    putExtra(NAVIGATE_ID, NAVIGATE_TO_HOME)
                }
            )
        }
    }
}
package com.sendi.deliveredrobot.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.databinding.FragmentContainBinding
import com.sendi.deliveredrobot.entity.QuerySql
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.navigationtask.RobotStatus.selectRoutMapItem
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import java.lang.Exception
import java.util.*

/**
 * @describe 被围堵
 */
class ContainFragment : Fragment() {
    private lateinit var binding: FragmentContainBinding
    private var timer = Timer()
    private val mainScope = MainScope()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_contain, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)!!
        try {
            if (QuerySql.queryPointDate(selectRoutMapItem?.value!!)[RobotStatus.pointItem!!.value!!]?.touch_type == 4) {
                binding.imageView.visibility = View.GONE
                binding.imageView1.apply {
                    Glide.with(this).asGif()
                        .load(QuerySql.queryPointDate(selectRoutMapItem!!.value!!)[RobotStatus.pointItem!!.value!!].touch_blockPic)
                        .placeholder(R.drawable.ic_warming) // 设置默认图片
                        .into(this)
                }
            }
        } catch (_: Exception) {
        }

        binding.motionLayoutContain.apply {
            transitionToState(R.id.state2)
        }
//        binding.bottomAlarmTextView.apply {
//            bottomAlarmText1 = resources.getString(R.string.please_do_not_crowd_me)
//        }
//        val excuseMe = getString(R.string.excuse_me)
//        timer.schedule(object :TimerTask(){
//            override fun run() {
//                if(RobotStatus.stopButtonPressed.value != RobotCommand.STOP_BUTTON_PRESSED){
//                    mainScope.launch(Dispatchers.IO) {
//                        SpeakHelper.speak(excuseMe)
//                    }
//                }
//            }
//        },Date(),6000)
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()
        mainScope.cancel()
    }
}
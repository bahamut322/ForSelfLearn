package com.sendi.deliveredrobot.view.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.Glide
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.RobotCommand
import com.sendi.deliveredrobot.databinding.FragmentGoBackBinding
import com.sendi.deliveredrobot.entity.Universal
import com.sendi.deliveredrobot.entity.entitySql.QuerySql
import com.sendi.deliveredrobot.helpers.*
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.navigationtask.virtualTaskExecute
import com.sendi.deliveredrobot.topic.SafeStateTopic
import com.sendi.deliveredrobot.utils.LogUtil
import com.sendi.deliveredrobot.viewmodel.BasicSettingViewModel
import kotlinx.coroutines.*
import java.lang.Exception
import java.util.*
import kotlin.properties.Delegates

class GoBackFragment : Fragment() {
    private lateinit var binding: FragmentGoBackBinding
    private lateinit var seconds: MutableLiveData<Int>
    private lateinit var mainScope: CoroutineScope


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_go_back, container, false)
    }

    override fun onStop() {
        super.onStop()
        SafeStateTopic.resetSafeStateListener()
        mainScope.cancel()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainScope = MainScope()
        binding = DataBindingUtil.bind(view)!!
        seconds = MutableLiveData(30)
        RobotStatus.repeatedReading = 0
        try {
            if (QuerySql.queryPointDate(RobotStatus.selectRoutMapItem?.value!!)[RobotStatus.pointItem!!.value!!]?.touch_type == 4) {
                binding.goBackTv.visibility = View.GONE
                binding.imageViewGoBack.apply {
                    Glide.with(this)
                        .asGif()
                        .load(QuerySql.queryPointDate(RobotStatus.selectRoutMapItem!!.value!!)[RobotStatus.pointItem!!.value!!].touch_overTaskPic)
                        .placeholder(R.drawable.ic_warming) // 设置默认图片
                        .into(this)
                }
            }
            Log.d("TAG", "返回查询: ${QuerySql.selectGuideFouConfig().touchScreenConfig?.touch_type},${Universal.guideTask}")
            if (QuerySql.selectGuideFouConfig().touchScreenConfig?.touch_type == 4 && Universal.guideTask) {
                binding.goBackTv.visibility = View.GONE
                binding.imageViewGoBack.apply {
                    Glide.with(this)
                        .asGif()
                        .load(QuerySql.selectGuideFouConfig().touchScreenConfig?.touch_overTaskPic)
                        .placeholder(R.drawable.ic_warming) // 设置默认图片
                        .into(this)
                }
            }
            if (QuerySql.SelectActionData(
                    QuerySql.robotConfig().mapName,
                    Universal.businessTask,
                    Universal.shoppingType
                ).touchScreenConfig?.touch_type == 4 && Universal.businessTask.isNullOrEmpty()
            ) {
                binding.goBackTv.visibility = View.GONE
                binding.imageViewGoBack.apply {
                    Glide.with(this)
                        .asGif()
                        .load(
                            QuerySql.SelectActionData(
                                QuerySql.robotConfig().mapName,
                                Universal.businessTask,
                                Universal.shoppingType
                            ).touchScreenConfig?.touch_overTaskPic
                        )
                        .placeholder(R.drawable.ic_warming) // 设置默认图片
                        .into(this)
                }
            }
        } catch (_: Exception) {
        }
    }

}
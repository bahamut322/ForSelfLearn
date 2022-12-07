package com.sendi.deliveredrobot.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.RobotCommand
import com.sendi.deliveredrobot.TYPE_SEND
import com.sendi.deliveredrobot.databinding.FragmentHandleExceptionRelocationBinding
import com.sendi.deliveredrobot.helpers.CommonHelper
import com.sendi.deliveredrobot.helpers.DialogHelper
import com.sendi.deliveredrobot.helpers.ROSHelper
import com.sendi.deliveredrobot.helpers.SpeakHelper
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.*
import com.sendi.deliveredrobot.navigationtask.task.JudgeFloorTask
import com.sendi.deliveredrobot.navigationtask.task.SendingTask
import com.sendi.deliveredrobot.navigationtask.task.StartDoubleSecondSendTask
import com.sendi.deliveredrobot.room.database.DataBaseDeliveredRobotMap
import com.sendi.deliveredrobot.utils.LogUtil
import com.sendi.deliveredrobot.utils.ToastUtil
import com.sendi.deliveredrobot.view.widget.CustomIndicatorTextView
import com.sendi.deliveredrobot.view.widget.CustomKeyBoardView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

/**
 * @author heky
 * @date 2022-08-01
 * @description 通用处理异常-位置校准
 */
class HandleExceptionRelocationFragment : Fragment() {
    private lateinit var binding: FragmentHandleExceptionRelocationBinding
    private var inputFull = false
    private var firstInput = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_handle_exception_relocation, container, false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.customIndicatorTextView.clearText()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)!!

        binding.textViewEnsureRobotLocation.apply {
            setOnClickListener {
                binding.groupWarningRemind.visibility = View.GONE
                binding.groupRelocation.visibility = View.VISIBLE
            }
        }

        binding.textViewEnsure.apply {
            isEnabled = false
            isClickable = false
            setOnClickListener {
                isEnabled = false
                var text = binding.customIndicatorTextView.getText()
                if (text.startsWith("0")) {
                    text = text.substring(1)
                }
                MainScope().launch {
                    withContext(Dispatchers.Default) {
                        DialogHelper.loadingDialog.show()
                        val point =
                            DataBaseDeliveredRobotMap.getDatabase(requireContext()).getDao()
                                .queryPoint(text)
                        if (point == null) {
                            ToastUtil.show(
                                String.format(
                                    getString(R.string.room_number_mismatch),
                                    text
                                )
                            )
                            isEnabled = true
                            isClickable = true
                            DialogHelper.loadingDialog.dismiss()
                            return@withContext
                        }

                        val resultSwitch = ROSHelper.setNavigationMap(
                            point.subPath!!,
                            point.routePath!!
                        )
                        if (!resultSwitch) {
                            ToastUtil.show(getString(R.string.switch_map_fail))
                            LogUtil.e(getString(R.string.switch_map_fail))
                            DialogHelper.loadingDialog.dismiss()
                            return@withContext
                        }
                        virtualTaskExecute(1, "切换锚点前")
                        ROSHelper.setPoseClient(point.apply {
                            w = CommonHelper.adjustAngel(w!! + Math.PI)
                        })
                        //查看切换锚点是否成功
                        var result: Boolean
                        var retryTime = 10
                        do {
                            virtualTaskExecute(2, "查看切换锚点是否成功")
                            withContext(Dispatchers.Default) {
                                result = ROSHelper.getParam("/finish_update_pose") == "1"
                            }
                            retryTime--
                        } while (!result && retryTime > 0)
                        if (retryTime > 0) {
                            LogUtil.i("finish_update_pose成功")
                        } else {
                            ToastUtil.show("设置地图失败")
                        }
                        DialogHelper.loadingDialog.dismiss()
                        RobotStatus.currentLocation = point
                        BillManager.currentBill()?.executeNextTask()
                    }
                }
            }
        }

        binding.customIndicatorTextView.apply {
            setTextLength(4)
            isEnabled = false
            isClickable = false
            setIndicatorTextViewListener(object :
                CustomIndicatorTextView.IndicatorTextViewCallback {
                override fun fullText(text: String) {
                    inputFull = true
                }

                override fun notFull() {
                    inputFull = false
                }

                override fun hasText(text: String) {
                    with(binding.textViewEnsure) {
                        isEnabled = true
                        isClickable = true
                        setTextColor(android.graphics.Color.WHITE)
                    }
                }

                override fun empty() {
                    with(binding.textViewEnsure) {
                        isEnabled = false
                        isClickable = false
                        setTextColor(
                            androidx.core.content.ContextCompat.getColor(
                                context,
                                R.color.color_A0BAEF
                            )
                        )
                    }
                }
            })
        }

        binding.customKeyBoardView.apply {
            setKeyBoardListener(object : CustomKeyBoardView.KeyBoardCallback() {
                override fun onPushText(char: Char) {
                    if (!inputFull) {
                        binding.customIndicatorTextView.addText(char)
                        if (firstInput) {
                            firstInput = false
                            SpeakHelper.speak(char.toString())
                        } else {
                            SpeakHelper.speakWithoutStop(char.toString())
                        }

                    }
                }

                override fun onRemoveText() {
                    binding.customIndicatorTextView.removeText()
                }

                override fun onClearText() {
                    binding.customIndicatorTextView.clearText()
                }

            })
        }

    }
}
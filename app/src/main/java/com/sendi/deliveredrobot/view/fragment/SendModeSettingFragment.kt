package com.sendi.deliveredrobot.view.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.databinding.FragmentSendModeSettingBinding
import com.sendi.deliveredrobot.helpers.AudioMngHelper
import com.sendi.deliveredrobot.helpers.SpeakHelper
import com.sendi.deliveredrobot.room.database.DataBaseDeliveredRobotMap
import com.sendi.deliveredrobot.viewmodel.BasicSettingViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SendModeSettingFragment : Fragment() {
    private lateinit var binding: FragmentSendModeSettingBinding
    private val viewModel by viewModels<BasicSettingViewModel>({ requireActivity() })
    private val dao = DataBaseDeliveredRobotMap.getDatabase(MyApplication.instance!!).getDao()
    private val mainScope = MainScope()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_send_mode_setting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)!!
        binding.seekBarSpeedSetting.apply {
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                @SuppressLint("SetTextI18n")
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    with(binding.imageViewSpeedSetting) {
                        when (progress) {
                            in 0 until 4 -> setBackgroundResource(R.drawable.ic_speed_level_1)
                            in 4 until 8 -> setBackgroundResource(R.drawable.ic_speed_level_2)
                            in 8..10 -> setBackgroundResource(R.drawable.ic_speed_level_3)
                        }
                    }
                    with(binding.textViewSpeedSetting) {
                        text = "${progress * 0.1f + 0.2f}m/s"
                    }
                    mainScope.launch(Dispatchers.Default) {
                        viewModel.basicConfig.sendSpeed = progress * 0.1f + 0.2f
                        dao.updateBasicConfig(viewModel.basicConfig)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }
            })
            val sendSpeed = ((viewModel.basicConfig.sendSpeed!! - 0.2f) / 0.1f).toInt()
            progress = 10086
            progress = sendSpeed
        }
        binding.seekBarVolumeSettingLobby.apply {
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                @SuppressLint("SetTextI18n")
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    with(binding.textViewVolumeSettingLobby) {
                        text = "$progress%"
                    }
                    mainScope.launch(Dispatchers.Default) {
                        viewModel.basicConfig.sendVolumeLobby = progress
                        dao.updateBasicConfig(viewModel.basicConfig)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    AudioMngHelper(requireContext()).setVoice100(progress / 2)
                    SpeakHelper.speak(getString(R.string.current_volume))
                }
            })
            val sendVolumeLobby = viewModel.basicConfig.sendVolumeLobby
            progress = 10086
            progress = sendVolumeLobby
        }
        binding.seekBarVolumeSettingLift.apply {
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                @SuppressLint("SetTextI18n")
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    with(binding.textViewVolumeSettingLift) {
                        text = "$progress%"
                    }
                    mainScope.launch(Dispatchers.Default) {
                        viewModel.basicConfig.sendVolumeLift = progress
                        dao.updateBasicConfig(viewModel.basicConfig)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    AudioMngHelper(requireContext()).setVoice100(progress / 2)
                    SpeakHelper.speak(getString(R.string.current_volume))
                }
            })
            val sendVolumeLift = viewModel.basicConfig.sendVolumeLift
            progress = 10086
            progress = sendVolumeLift
        }
        binding.seekBarVolumeSettingRoom.apply {
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                @SuppressLint("SetTextI18n")
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    with(binding.textViewVolumeSettingRoom) {
                        text = "$progress%"
                    }
                    mainScope.launch(Dispatchers.Default) {
                        viewModel.basicConfig.sendVolume = progress
                        dao.updateBasicConfig(viewModel.basicConfig)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    AudioMngHelper(requireContext()).setVoice100(progress / 2)
                    SpeakHelper.speak(getString(R.string.current_volume))
                }
            })
            val sendVolumeRoom = viewModel.basicConfig.sendVolume!!
            progress = 10086
            progress = sendVolumeRoom
        }
        binding.seekBarPutObjectDurationSetting.apply {
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                @SuppressLint("SetTextI18n")
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    val time = progress + 30
                    with(binding.textViewPutObjectDurationSetting) {
                        text = "${time}s"
                    }
                    viewModel.basicConfig.sendPutObjectTime = time
                    mainScope.launch(Dispatchers.Default) {
                        dao.updateBasicConfig(viewModel.basicConfig)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }
            })
            val sendPutObjectTime = viewModel.basicConfig.sendPutObjectTime!!
            progress = 10086
            progress = sendPutObjectTime - 30
        }
        binding.seekBarWaitTakeObjectDurationSetting.apply {
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                @SuppressLint("SetTextI18n")
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    val time = progress + 300
                    with(binding.textViewWaitTakeObjectDurationSetting) {
                        text = "${time}s"
                    }
                    mainScope.launch(Dispatchers.Default) {
                        viewModel.basicConfig.sendWaitTakeObjectTime = time
                        dao.updateBasicConfig(viewModel.basicConfig)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }
            })
            val sendWaitTakeObjectTime = viewModel.basicConfig.sendWaitTakeObjectTime!!
            progress = 10086
            progress = sendWaitTakeObjectTime - 300
        }

        binding.seekBarTakeObjectDurationSetting.apply {
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                @SuppressLint("SetTextI18n")
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    val time = progress + 30
                    with(binding.textViewTakeObjectDurationSetting) {
                        text = "${time}s"
                    }
                    mainScope.launch(Dispatchers.Default) {
                        viewModel.basicConfig.sendTakeObjectTime = time
                        dao.updateBasicConfig(viewModel.basicConfig)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }
            })
            val sendTakeObjectTime = viewModel.basicConfig.sendTakeObjectTime!!
            progress = 10086
            progress = sendTakeObjectTime - 30
        }

        binding.switchNeedTakeObjectPassword.apply {
            setOnCheckedChangeListener { _, isChecked ->
                viewModel.basicConfig.needTakeObjectPassword = when (isChecked) {
                    true -> 1
                    false -> 0
                }
                mainScope.launch(Dispatchers.Default) {
                    dao.updateBasicConfig(viewModel.basicConfig)
                }
            }
            isChecked = viewModel.basicConfig.needTakeObjectPassword == 1
        }

        binding.switchNeedDeliveryPassword.apply {
            setOnCheckedChangeListener { _, isChecked ->
                viewModel.basicConfig.sendModeVerifyPassword = when (isChecked) {
                    true -> 1
                    false -> 0
                }
                mainScope.launch(Dispatchers.Default) {
                    dao.updateBasicConfig(viewModel.basicConfig)
                }
            }
            isChecked = viewModel.basicConfig.sendModeVerifyPassword == 1
        }
        binding.switchOpenDelivery.apply {
            setOnCheckedChangeListener { _, isChecked ->
                viewModel.basicConfig.sendModeOpen = when (isChecked) {
                    true -> 1
                    false -> 0
                }
                mainScope.launch(Dispatchers.Default) {
                    dao.updateBasicConfig(viewModel.basicConfig)
                }
            }
            isChecked = viewModel.basicConfig.sendModeOpen == 1
        }
    }
}
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
import com.sendi.deliveredrobot.databinding.FragmentGuideModeSettingBinding
import com.sendi.deliveredrobot.helpers.AudioMngHelper
import com.sendi.deliveredrobot.helpers.SpeakHelper
import com.sendi.deliveredrobot.room.database.DataBaseDeliveredRobotMap
import com.sendi.deliveredrobot.viewmodel.BasicSettingViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GuideModeSettingFragment : Fragment() {
    private lateinit var binding: FragmentGuideModeSettingBinding
    private val viewModel by viewModels<BasicSettingViewModel>({ requireActivity() })
    private val dao = DataBaseDeliveredRobotMap.getDatabase(MyApplication.instance!!).getDao()
    private val mainScope = MainScope()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_guide_mode_setting, container, false)
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
                        viewModel.basicConfig.guideSpeed = progress * 0.1f + 0.2f
                        dao.updateBasicConfig(viewModel.basicConfig)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }
            })
            val guideSpeed = ((viewModel.basicConfig.guideSpeed!! - 0.2f) / 0.1f).toInt()
            progress = 10086
            progress = guideSpeed
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
                    viewModel.basicConfig.guideVolume = progress
                    mainScope.launch(Dispatchers.Default) {
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
            val guideVolumeRoom = viewModel.basicConfig.guideVolume!!
            progress = 10086
            progress = guideVolumeRoom
        }

        binding.seekBarWalkPauseDuration.apply {
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                @SuppressLint("SetTextI18n")
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    val time = progress + 20
                    with(binding.textViewWalkPauseDuration) {
                        text = String.format(getString(R.string.seconds),time)
                    }
                    viewModel.basicConfig.guideWalkPauseTime = time
                    mainScope.launch(Dispatchers.Default) {
                        dao.updateBasicConfig(viewModel.basicConfig)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }
            })
            val guideWalkPauseTime = viewModel.basicConfig.guideWalkPauseTime!!
            progress = 10086
            progress = guideWalkPauseTime - 20
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
                    viewModel.basicConfig.guideVolumeLobby = progress
                    mainScope.launch(Dispatchers.Default) {
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
            val guideVolumeLobby = viewModel.basicConfig.guideVolumeLobby
            progress = 10086
            progress = guideVolumeLobby
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
                    viewModel.basicConfig.guideVolumeLift = progress
                    mainScope.launch(Dispatchers.Default) {
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
            val guideVolumeLift = viewModel.basicConfig.guideVolumeLift
            progress = 10086
            progress = guideVolumeLift
        }

        binding.switchNeedDeliveryPassword.apply {
            setOnCheckedChangeListener { _, isChecked ->
                viewModel.basicConfig.guideModeVerifyPassword = when (isChecked) {
                    true -> 1
                    false -> 0
                }
                mainScope.launch(Dispatchers.Default) {
                    dao.updateBasicConfig(viewModel.basicConfig)
                }
            }
            isChecked = viewModel.basicConfig.guideModeVerifyPassword == 1
        }
        binding.switchOpenGuide.apply {
            setOnCheckedChangeListener { _, isChecked ->
                viewModel.basicConfig.guideModeOpen = when (isChecked) {
                    true -> 1
                    false -> 0
                }
                mainScope.launch(Dispatchers.Default) {
                    dao.updateBasicConfig(viewModel.basicConfig)
                }
            }
            isChecked = viewModel.basicConfig.guideModeOpen == 1
        }
    }


}
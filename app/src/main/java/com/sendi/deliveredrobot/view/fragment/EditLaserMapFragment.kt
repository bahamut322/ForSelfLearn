package com.sendi.deliveredrobot.view.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.addCallback
import androidx.core.view.get
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.sendi.deliveredrobot.*
import com.sendi.deliveredrobot.SUB_MAP_ID
import com.sendi.deliveredrobot.SUB_MAP_NAME
import com.sendi.deliveredrobot.SUB_MAP_NAME_DATABASE
import com.sendi.deliveredrobot.databinding.FragmentEditLaserMapBinding
import com.sendi.deliveredrobot.helpers.DialogHelper
import com.sendi.deliveredrobot.room.dao.DeliveredRobotDao
import com.sendi.deliveredrobot.room.database.DataBaseDeliveredRobotMap
import com.sendi.deliveredrobot.ros.debug.MapLaserServiceImpl
import com.sendi.deliveredrobot.ros.debug.dto.MapResult
import com.sendi.deliveredrobot.ros.RosPointArrUtil
import com.sendi.deliveredrobot.utils.LogUtil
import com.sendi.deliveredrobot.utils.ToastUtil
import com.sendi.deliveredrobot.view.widget.HideNavigationBarDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.ArrayList

/**
 * @describe 查看/编辑激光图页面
 */
class EditLaserMapFragment : Fragment() {
    private lateinit var binding: FragmentEditLaserMapBinding
    private val mapLaserServiceImpl = MapLaserServiceImpl.getInstance()
    private lateinit var dialog: HideNavigationBarDialog
    private lateinit var laserMapNameDatabase: String
    private var laserMapId: Int = -1
    private var back = false
    private lateinit var dao: DeliveredRobotDao
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            LogUtil.i("back from previous")
            back = true
        }
        dao = DataBaseDeliveredRobotMap.getDatabase(requireContext()).getDao()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_laser_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)!!
        val laserMapName = arguments?.getString(SUB_MAP_NAME)
        laserMapNameDatabase = arguments?.getString(SUB_MAP_NAME_DATABASE, "") ?: ""
        laserMapId = arguments?.getInt(SUB_MAP_ID) ?: -1
        dialog = initCopyLaserMapDialog()
        if (laserMapName?.isNotEmpty() == true) {
            if (!back) {
                MainScope().launch {
                    DialogHelper.loadingDialog.show()
                    val mapResult: MapResult
                    withContext(Dispatchers.Default) {
                        mapResult = mapLaserServiceImpl.showLaserMap(laserMapName)
                    }
                    DialogHelper.loadingDialog.dismiss()
                    if (mapResult.isFlag) {
                        try {
                            val staticMap: ArrayList<FloatArray>? = RosPointArrUtil.staticMap
//                            val updateMap:ArrayList<FloatArray>? = RosPointArrUtil.updateMap
                            binding.laserPointsView.apply {
                                if (staticMap != null) {
                                    setStaticPoints(staticMap)
                                    invalidate()
                                }
//                                if (updateMap != null) {
//                                    setUpdatePoints(updateMap)
//                                }
                            }
                        } catch (e: Exception) {
                            LogUtil.e(e.toString())
                        }
                    }
                }
            } else {
                try {
                    val staticMap = RosPointArrUtil.staticMap
//                    val updateMap = RosPointArrUtil.updateMap
                    binding.laserPointsView.apply {
                        setStaticPoints(staticMap)
//                        setUpdatePoints(updateMap)
                    }
                } catch (e: Exception) {
                    LogUtil.e(e.toString())
                }
            }
        }
        binding.textViewSourceEdit.apply {
            isClickable = true
            setOnClickListener {
                mapLaserServiceImpl.updateOriginalFile(1)
//                MyApplication.instance!!.sendBroadcast(Intent().apply {
//                    action = ACTION_NAVIGATE
//                    putExtra(NAVIGATE_ID, R.id.relocationLaserFragment)
//                })
                findNavController().navigate(R.id.relocationLaserFragment)
            }
        }
        binding.textViewCopyEdit.apply {
            isClickable = true
            setOnClickListener {
                dialog.show()
            }
        }
        binding.textViewGoback.apply {
            isClickable = true
            setOnClickListener {
//                MyApplication.instance!!.sendBroadcast(Intent().apply {
//                    action = ACTION_NAVIGATE
//                    putExtra(NAVIGATE_ID, POP_BACK_STACK)
//                })
                findNavController().popBackStack()
            }
        }
    }

    @SuppressLint("InflateParams")
    private fun initCopyLaserMapDialog(): HideNavigationBarDialog {
        val mWindowWidth: Int
        val mWindowHeight: Int
        val dialog =
            HideNavigationBarDialog(requireContext(), R.style.simpleDialogStyle)
        val dialogView: View = LayoutInflater.from(requireContext())
            .inflate(R.layout.laser_pop_ups, null)
        val displayMetrics = resources.displayMetrics
        val popUpsEditText =
            dialogView.findViewById<EditText>(R.id.pop_ups_edittext).apply {
                setText(laserMapNameDatabase)
                setSelection(laserMapNameDatabase.length)
            }
        val floorNameArray = ElevatorObject.floorNameArray
        val spinnerFloorName = dialogView.findViewById<Spinner>(R.id.spinnerFloorName).apply {
            val arrayAdapter = ArrayAdapter(
                context,
                R.layout.item_spinner_sort,
                R.id.textViewContent,
                floorNameArray ?: arrayOf()
            ).apply {
                setDropDownViewResource(R.layout.item_spinner_drop_down_sort)
            }
            adapter = arrayAdapter
            setSelection(0)
        }
        dialogView.findViewById<TextView>(R.id.laser_confirm_button).apply {
            isEnabled = true
            isClickable = true
            setOnClickListener {
                MainScope().launch {
                    if (popUpsEditText.text.isNullOrEmpty()) {
                        ToastUtil.show(getString(R.string.please_input_laser_name))
                        return@launch
                    }
                    val queryLaserNameIdResult: Boolean
                    withContext(Dispatchers.Default) {
                        queryLaserNameIdResult =
                            dao.queryLaserNameId(popUpsEditText.text.toString()) > 0
                    }
                    if (queryLaserNameIdResult) {
                        ToastUtil.show(getString(R.string.laser_name_existed))
                        return@launch
                    }
//                    if(editTextFloorName.text.isNullOrEmpty()){
//                        ToastUtil.show(getString(R.string.please_input_floor_name))
//                        return@launch
//                    }
//                    val floorCode = editTextFloorCode.text.toString()
//                    if(floorCode.isEmpty()){
//                        ToastUtil.show(getString(R.string.please_input_floor_code))
//                        return@launch
//                    }
//                    val floorCodeInt: Int
//                    try {
//                        floorCodeInt = floorCode.toInt()
//                    }catch (e:Exception){
//                        ToastUtil.show(getString(R.string.floor_code_must_be_integer))
//                        return@launch
//                    }
                    withContext(Dispatchers.Default) {
                        mapLaserServiceImpl.updateCopyFile(
                            2,
                            popUpsEditText.text.toString(),
                            floorNameArray?.get(spinnerFloorName.selectedItemPosition) ?: ""
                        )
                    }
                    dialog.dismiss()
                    this@EditLaserMapFragment.findNavController()
                        .navigate(R.id.relocationLaserFragment)
                }
            }
        }
        dialogView.findViewById<ImageView>(R.id.back_imageButton).apply {
            isClickable = true
            setOnClickListener {
                dialog.dismiss()
            }
        }
        mWindowWidth = displayMetrics.widthPixels
        mWindowHeight = displayMetrics.heightPixels
        dialog.setContentView(
            dialogView, ViewGroup.MarginLayoutParams(
                mWindowWidth,
                mWindowHeight
            )
        )
        return dialog
    }
}
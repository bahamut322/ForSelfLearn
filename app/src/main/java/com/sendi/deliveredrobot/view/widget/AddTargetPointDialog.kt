package com.sendi.deliveredrobot.view.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import com.sendi.deliveredrobot.ElevatorObject
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.room.PointType
import com.sendi.deliveredrobot.room.dao.DeliveredRobotDao
import com.sendi.deliveredrobot.room.database.DataBaseDeliveredRobotMap
import com.sendi.deliveredrobot.room.entity.Point
import com.sendi.deliveredrobot.room.entity.PublicArea
import com.sendi.deliveredrobot.utils.PxUtil
import com.sendi.deliveredrobot.utils.ToastUtil
import com.sendi.deliveredrobot.view.inputfilter.NumRangeInputFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddTargetPointDialog(context: Context,
                           private var subMapId: Int,
                           areaListData: ArrayList<PublicArea>,
                           /** 0:添加目标点模式 1：编辑目标点模式*/
                           private var model: Int,
                           private var editPoint: Point?,
                           addAreaDialogListener: AddTargetPointDialogListener
) : HideNavigationBarDialog(
    context, R.style.simpleDialogStyle, needBlur = false
) {
    private var addPointDialogListener: AddTargetPointDialogListener? = addAreaDialogListener
    private var areaListData :ArrayList<PublicArea>
    private var areaStrList :ArrayList<String> = arrayListOf()
    private lateinit var tvSortTips:TextView
    private var direction = ""
    private var mContext :Context = context
    private var dao: DeliveredRobotDao = DataBaseDeliveredRobotMap.getDatabase(mContext).getDao()
    private var originName: String
    private var elevatorNameArray: Array<String>
    init {
        this.areaListData = when(model){
            0 -> {
                areaListData.filter {
                    "电梯内部停靠点" != it.name
                } as ArrayList<PublicArea>
            }
            1 -> {
                areaListData
            }
            else -> areaListData
        }
        for (item in this.areaListData){
            areaStrList.add(item.name?:"")
        }
        originName = editPoint?.name?:""
        elevatorNameArray = ElevatorObject.elevatorNameArray?: arrayOf()
        initView()
    }

    @SuppressLint("InflateParams")
    private fun initView() {
        val dialogView: View = LayoutInflater.from(MyApplication.instance!!)
            .inflate(R.layout.dialog_add_target_point, null)
        setContentView(dialogView)
        val spSort = findViewById<Spinner>(R.id.spSort)
        val tvLeftRight: TextView = findViewById(R.id.tvLeftRight)
        val tvFrontBack: TextView = findViewById(R.id.tvFrontBack)
        val tvDelete: TextView = findViewById(R.id.tvDelete)
        setCanceledOnTouchOutside(false)
        tvSortTips = findViewById(R.id.tvSortTips)
        val edtName = findViewById<EditText>(R.id.edtName)
        val tvRange = findViewById<TextView>(R.id.tvRange)
        val edtRange = findViewById<EditText>(R.id.edtRange)
        val tvElevator = findViewById<TextView>(R.id.tvElevator)
        val spElevator = findViewById<Spinner>(R.id.spElevator)
        val viewFrame = findViewById<View>(R.id.viewFrame)
        this.window?.setBackgroundDrawable(
            ColorDrawable(
                ContextCompat.getColor(
                    context,
                    R.color.transparency
                )
            )
        )
        //强制隐藏键盘
//        val imm =
//            context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
////              imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
//        imm.hideSoftInputFromWindow(window.getWindowToken, 0);

        spSort.apply {
            val arrayAdapter = ArrayAdapter(
                mContext,
                R.layout.item_spinner_sort,
                R.id.textViewContent,
                areaStrList
            ).apply {
                setDropDownViewResource(R.layout.item_spinner_drop_down_sort)
            }
            when (model) {
                0 -> onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        when (listOf("电梯外部停靠点","电梯锚点").contains(areaListData[position].name)) {
                            true -> {
                                tvRange.visibility = View.VISIBLE
                                edtRange.visibility = View.VISIBLE
                                tvElevator.visibility = View.VISIBLE
                                spElevator.visibility = View.VISIBLE
                                viewFrame.apply {
                                    val layoutParams = layoutParams
                                    layoutParams.height = PxUtil.dp2px(context, 600f)
                                    setLayoutParams(layoutParams)
                                }
                            }
                            false -> {
                                tvRange.visibility = View.GONE
                                edtRange.visibility = View.GONE
                                tvElevator.visibility = View.GONE
                                spElevator.visibility = View.GONE
                                viewFrame.apply {
                                    val layoutParams = layoutParams
                                    layoutParams.height = PxUtil.dp2px(context, 400f)
                                    setLayoutParams(layoutParams)
                                }
                            }
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {

                    }
                }

                1 -> onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        when (listOf("电梯外部停靠点","电梯锚点","电梯内部停靠点").contains(areaListData[position].name)) {
                            true -> {
//                                tvRange.visibility = View.VISIBLE
//                                edtRange.visibility = View.VISIBLE
                                tvElevator.visibility = View.VISIBLE
                                spElevator.visibility = View.VISIBLE
                                viewFrame.apply {
                                    val layoutParams = layoutParams
                                    layoutParams.height = PxUtil.dp2px(context, 500f)
                                    setLayoutParams(layoutParams)
                                }
                            }
                            false -> {
//                                tvRange.visibility = View.GONE
//                                edtRange.visibility = View.GONE
                                tvElevator.visibility = View.GONE
                                spElevator.visibility = View.GONE
                                viewFrame.apply {
                                    val layoutParams = layoutParams
                                    layoutParams.height = PxUtil.dp2px(context, 400f)
                                    setLayoutParams(layoutParams)
                                }
                            }
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {

                    }
                }
            }
            adapter = arrayAdapter
            setSelection(0)
        }

        findViewById<TextView>(R.id.tvSortConfirm).setOnClickListener {
            MainScope().launch {
                val mapName = edtName.text.toString().trim()
                var targetExist: Boolean
                if (originName != mapName) {
                    withContext(Dispatchers.Default) {
                        targetExist = dao.queryTargetNameExist(subMapId, mapName) > 0
                    }
                    if (targetExist) {
                        ToastUtil.show("目标点名已存在")
                        return@launch
                    }
                }
                if (mapName != "") {
                    if (model == 0) {
                        val newPoint = Point(
                            -1,
                            mapName,
                            direction,
                            0f,
                            0f,
                            0.0,
                            subMapId,
                            areaListData[spSort.selectedItemPosition].id,
                            elevator = spElevator.selectedItem?.toString()
                        )
                        try {
                            when (newPoint.type) {
                                PointType.LIFT_OUTSIDE -> {addPointDialogListener?.confirm(newPoint, edtRange.text.toString().toDouble())}
                                else -> {addPointDialogListener?.confirm(newPoint, 0.0)}
                            }
                        }catch (e: NumberFormatException){
                            ToastUtil.show("请填入正确格式的距离")
                        }
                    } else {
                        val newPoint = Point(
                            editPoint?.id ?: 0,
                            mapName,
                            direction,
                            editPoint?.x,
                            editPoint?.y,
                            editPoint?.w,
                            subMapId,
                            areaListData[spSort.selectedItemPosition].id,
                            elevator = spElevator.selectedItem?.toString()
                        )
                        addPointDialogListener?.confirm(newPoint,0.0)
                    }
                } else {
                    ToastUtil.show(context.getString(R.string.please_input_target_name))
                }
            }
        }

        findViewById<ImageView>(R.id.ivClose).setOnClickListener {
            addPointDialogListener?.cancel()
            dismiss()
        }
        direction = "左右"
        tvLeftRight.apply {
            isSelected = true
            setOnClickListener {
                tvLeftRight.isSelected = true
                tvFrontBack.isSelected = false
                direction = "左右"
                tvLeftRight.setTextColor(ContextCompat.getColor(context!!, R.color.white))
                tvFrontBack.setTextColor(ContextCompat.getColor(context!!, R.color.color_2170E7))
            }
        }

        tvFrontBack.setOnClickListener {
            tvLeftRight.isSelected = false
            tvFrontBack.isSelected = true
            direction = "前后"
            tvLeftRight.setTextColor(ContextCompat.getColor(context, R.color.color_2170E7))
            tvFrontBack.setTextColor(ContextCompat.getColor(context, R.color.white))
        }
        tvDelete.setOnClickListener {
            editPoint?.let { it1 -> addPointDialogListener?.delete(it1) }
        }
        tvRange.apply {
            visibility = when (model) {
                0 -> View.VISIBLE
                else -> View.GONE
            }
        }
        edtRange.apply {
            val inputFilter = NumRangeInputFilter()
            filters = arrayOf(inputFilter)
            visibility = when (model) {
                0 -> View.VISIBLE
                else -> View.GONE
            }
            isEnabled = when (model) {
                0 -> true
                else -> false
            }
            setText("1.5")
            setSelection(text.length)
        }
        tvElevator.apply {
            visibility = when (model) {
                0 -> View.VISIBLE
                else -> View.GONE
            }
        }
        spElevator.apply {
            visibility = when (model) {
                0 -> View.VISIBLE
                else -> View.GONE
            }
            val arrayAdapter = ArrayAdapter(
                mContext,
                R.layout.item_spinner_sort,
                R.id.textViewContent,
                elevatorNameArray
            ).apply {
                setDropDownViewResource(R.layout.item_spinner_drop_down_sort)
            }
            adapter = arrayAdapter
            setSelection(0)
        }

//        viewFrame.apply {
//            val layoutParams = layoutParams
//            when (model) {
//                0 -> layoutParams.height = PxUtil.dp2px(context, 600f)
//                else -> layoutParams.height = PxUtil.dp2px(context, 400f)
//            }
//            setLayoutParams(layoutParams)
//        }

        if (model == 1) {
            tvDelete.visibility = View.VISIBLE
            if (editPoint != null) {
                edtName.setText(editPoint!!.name)
                edtName.setSelection(editPoint!!.name?.length ?: 0)
                if (editPoint!!.direction.equals("左右")) {
                    direction = "左右"
                    tvLeftRight.isSelected = true
                    tvLeftRight.callOnClick()
                } else if (editPoint!!.direction.equals("前后")) {
                    direction = "前后"
                    tvLeftRight.isSelected = false
                    tvFrontBack.isSelected = true
                    tvFrontBack.callOnClick()
                }
                for (index in areaListData.indices) {
                    if (areaListData[index].id == editPoint!!.type) {
                        spSort.setSelection(index)
                        break
                    }
                }
                for (index in elevatorNameArray.indices) {
                    if (elevatorNameArray[index] == editPoint?.elevator) {
                        spElevator.setSelection(index)
                        break
                    }
                }
            }
        }
    }

    fun setTips(content:String) {
        tvSortTips.text = content
    }

    interface AddTargetPointDialogListener {
        fun cancel()
        fun confirm(newPoint: Point, range: Double)
        fun delete(editPoint: Point)
    }

}
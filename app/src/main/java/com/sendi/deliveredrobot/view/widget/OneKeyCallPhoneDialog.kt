package com.sendi.deliveredrobot.view.widget

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.adapter.OneKeyPhoneNumberListAdapter
import com.sendi.deliveredrobot.model.OneKeyCallPhoneModel

class OneKeyCallPhoneDialog(
    context: Context,
    themeResId: Int = R.style.simpleDialogStyle,
    needBlur: Boolean = true
) : HideNavigationBarDialog(context = context, themeResId = themeResId, needBlur = needBlur) {
    private var constraintLayoutClose: ConstraintLayout? = null
    private var recyclerViewPhoneNumber: RecyclerView? = null
    private var phoneNumberList: List<OneKeyCallPhoneModel>? = null
    init {
        val mWindowWidth: Int
        val mWindowHeight: Int
        setCancelable(false)
        val dialogView: View = LayoutInflater.from(MyApplication.instance!!)
            .inflate(R.layout.one_key_call_phone_dialog, null)
        val displayMetrics = MyApplication.instance!!.resources.displayMetrics
        constraintLayoutClose = dialogView.findViewById<ConstraintLayout?>(R.id.constraint_layout_close).apply {
            setOnClickListener {
                dismiss()
            }
        }
        phoneNumberList = ArrayList<OneKeyCallPhoneModel>().apply {
            add(OneKeyCallPhoneModel("社区居委会1", "(020)83368771"))
            add(OneKeyCallPhoneModel("社区居委会2", "(020)83179130"))
//            add("18925090633")
        }
        recyclerViewPhoneNumber = dialogView.findViewById(R.id.recycler_view_phone_number)
        recyclerViewPhoneNumber?.apply {
            layoutManager = LinearLayoutManager(MyApplication.instance)
            adapter = OneKeyPhoneNumberListAdapter()
                .apply {
                    setData(phoneNumberList!!)
                }
        }
        mWindowWidth = displayMetrics.widthPixels
        mWindowHeight = displayMetrics.heightPixels
        setContentView(
            dialogView, ViewGroup.MarginLayoutParams(
                mWindowWidth,
                mWindowHeight
            )
        )
    }
}
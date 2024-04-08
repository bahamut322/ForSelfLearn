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
import com.sendi.deliveredrobot.model.PhoneConfigModel

class OneKeyCallPhoneDialog(
    context: Context,
    themeResId: Int = R.style.simpleDialogStyle,
    needBlur: Boolean = true
) : HideNavigationBarDialog(context = context, themeResId = themeResId, needBlur = needBlur) {
    private var constraintLayoutClose: ConstraintLayout? = null
    private var recyclerViewPhoneNumber: RecyclerView? = null
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
        recyclerViewPhoneNumber = dialogView.findViewById(R.id.recycler_view_phone_number)
        recyclerViewPhoneNumber?.apply {
            layoutManager = LinearLayoutManager(MyApplication.instance)
            adapter = OneKeyPhoneNumberListAdapter()
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

    fun setData(phoneNumberList: List<PhoneConfigModel>){
        (recyclerViewPhoneNumber?.adapter as OneKeyPhoneNumberListAdapter).setData(phoneNumberList)
    }
}
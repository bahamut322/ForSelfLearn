package com.sendi.deliveredrobot.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.model.MyResultModel
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.BillManager
import com.sendi.deliveredrobot.navigationtask.ExplanationBill
import com.sendi.deliveredrobot.room.database.DataBaseDeliveredRobotMap
import com.sendi.deliveredrobot.view.widget.ChangingPointDialog
import com.sendi.deliveredrobot.view.widget.ExitCameraDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


class ChangePointGridViewAdapter(private val context: Context, private val items: List<MyResultModel?>?,private val nameString: String) : BaseAdapter() {

    override fun getCount(): Int {
        return items!!.size
    }

    override fun getItem(position: Int): Any {
        return items!![position]!!
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        val holder: ViewHolder

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_changing_point, parent, false)
            holder = ViewHolder(view)
            view.tag = holder
        } else {
            holder = view.tag as ViewHolder
        }
        if (items!![position]!!.name == nameString){
            holder.itemTextView.setBackgroundResource(R.drawable.bg_button_1)
            holder.itemTextView.isEnabled = false;
        }
        holder.itemTextView.text = items[position]!!.name
        holder.itemTextView.setOnClickListener {

            //弹窗
            val changePointDialog = ChangingPointDialog(context)

            MainScope().launch(Dispatchers.Default) {
                changePointDialog.show()
                changePointDialog.askTv.text = items[position]!!.name
                changePointDialog.Sure.setOnClickListener {
                    val taskModel = TaskModel(
                        location = DataBaseDeliveredRobotMap.getDatabase(MyApplication.context)
                            .getDao()
                            .queryPoint(items[position]!!.name)
                    )
                    val bill = ExplanationBill.createBill(taskModel = taskModel)
                    BillManager.addAllAtIndex(bill, position)
                    BillManager.currentBill()!!.currentTask()
                }
                changePointDialog.No.setOnClickListener{
                    changePointDialog.dismiss();
                }
            }
        }
        return view!!
    }

    private class ViewHolder(view: View) {
        val itemTextView: Button = view.findViewById(R.id.pointName)
    }

}
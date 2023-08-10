//package com.sendi.deliveredrobot.adapter
//
//import android.annotation.SuppressLint
//import android.view.LayoutInflater
//import android.view.ViewGroup
//import androidx.recyclerview.widget.RecyclerView.Adapter
//import com.sendi.deliveredrobot.R
//import com.sendi.deliveredrobot.holder.FileListHolder
//import com.sendi.deliveredrobot.holder.PlaceListHolder
//import com.sendi.deliveredrobot.model.FileInfoModel
//import com.sendi.deliveredrobot.viewmodel.ChooseRelocatePlaceViewModel
//import com.sendi.deliveredrobot.viewmodel.SendPlaceBin1ViewModel
//
//class FileListAdapter() :
//    Adapter<FileListHolder>() {
//    private var data: List<FileInfoModel>? = null
//
//    @SuppressLint("NotifyDataSetChanged")
//    fun setData(data: List<FileInfoModel>) {
//        this.data = data
//        notifyDataSetChanged()
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileListHolder {
//        return FileListHolder(
//            LayoutInflater.from(parent.context).inflate(R.layout.activity_main, parent, false)
//        )
//    }
//
//    override fun onBindViewHolder(holder: FileListHolder, position: Int) {
//        holder.data = data?.get(position)
//    }
//
//    override fun getItemCount(): Int {
//        return data?.size?:0
//    }
//}
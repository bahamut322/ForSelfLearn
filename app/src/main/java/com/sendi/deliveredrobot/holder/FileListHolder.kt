//package com.sendi.deliveredrobot.holder
//
//import android.annotation.SuppressLint
//import android.view.View
//import android.widget.TextView
//import androidx.core.content.ContextCompat
//import androidx.recyclerview.widget.RecyclerView
//import com.sendi.deliveredrobot.BuildConfig
//import com.sendi.deliveredrobot.R
//import com.sendi.deliveredrobot.helpers.DialogHelper
//import com.sendi.deliveredrobot.model.FileInfoModel
//import com.sendi.deliveredrobot.model.PlaceModel
//import com.sendi.deliveredrobot.utils.ToastUtil
//
//@SuppressLint("ResourceAsColor")
//class FileListHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//    val textView: TextView = itemView.findViewById(R.id.textViewName)
//    private val viewDownload: View = itemView.findViewById(R.id.viewDownload)
//    private val viewDelete: View = itemView.findViewById(R.id.viewDelete)
//    var data: FileInfoModel? = null
//        @SuppressLint("SetTextI18n")
//        set(data) {
//            field = data
//            val model = field
//            textView.apply {
//                text = "${model?.fileName?:""}${model?.fileSuffix?:""}"
//                setOnClickListener {
//                    if (data?.fileType.let { it == 0 || it == 1 }) {
//                        ToastUtil.show("格式错误")
//                        return@setOnClickListener
//                    }
//                    val url = "${BuildConfig.UPLOAD_FILE_HTTP_HOST}/musics-store${model?.fileUrl?:""}"
//                    DialogHelper.getPreviewPictureDialog(url).show()
//                }
//            }
//            viewDownload.setOnClickListener {
//                ToastUtil.show("${model?.fileUrl}")
//            }
//            viewDelete.setOnClickListener {
//
//            }
//        }
//}
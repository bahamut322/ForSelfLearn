package com.sendi.deliveredrobot.apn

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.telephony.TelephonyManager
import android.util.Log

/**
 * @author heky
 * @date 2022-03-18
 */
class APN(private val context:Context){
    var mcc:String = ""
    var mnc:String = ""
    init {
        val numEric = sIMInfo
        if(numEric.length > 3){
            mcc = numEric.substring(0, 3)
            mnc = numEric.substring(3, numEric.length)
        }
    }
        fun addAPN(apnUri: Uri, name: String?, apn: String?): Int {
            var id = -1
            val numEric = sIMInfo ?: return -1
            val resolver: ContentResolver = context.contentResolver
            val values = ContentValues()

//        SIMCardInfo siminfo = new SIMCardInfo(MainActivity.this);
            // String user = siminfo.getNativePhoneNumber().substring(start);
            values.put("name", name) //apn中文描述
            values.put("apn", apn) //apn名称
            values.put("type", "default") //apn类型
            if(sIMInfo.length > 3){
                values.put("numeric", sIMInfo)
                values.put("mcc", mcc)
                values.put("mnc", mnc)
            }
            values.put("proxy", "") //代理
            values.put("port", "") //端口
            values.put("mmsproxy", "") //彩信代理
            values.put("mmsport", "") //彩信端口
            values.put("user", "") //用户名
            values.put("server", "") //服务器
            values.put("password", "") //密码
            values.put("mmsc", "") //MMSC
            var c: Cursor? = null
            val newRow: Uri? = resolver.insert(apnUri, values)
            if (newRow != null) {
                c = resolver.query(newRow, null, null, null, null)
                val idIndex: Int = c?.getColumnIndex("_id")?:-1
                c?.moveToFirst()
                id = c?.getShort(idIndex)?.toInt()?:-1
            }
            c?.close()
            return id
        }

        private val sIMInfo: String
            get() {
                val iPhoneManager: TelephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                return iPhoneManager.simOperator
            }

        // 设置接入点
        fun updateApn(id: Int, apnUri: Uri) {
            val resolver: ContentResolver = context.contentResolver
            val values = ContentValues()
            values.put("apn_id", id)
            resolver.update(apnUri, values, null, null)
            // resolver.delete(url, where, selectionArgs)
        }

        fun deleteApn(apnName: String, apnUri: Uri){
            val resolver: ContentResolver = context.contentResolver
            resolver.delete(apnUri, "apn=\"${apnName}\"",null)
        }

        @SuppressLint("Recycle", "Range")
        fun queryApn(apnUri: Uri){
            val resolver: ContentResolver = context.contentResolver
            val list = arrayListOf<String>()
            val cr: Cursor? = resolver.query(apnUri, arrayOf("apn"),null,null,null)
            while (cr != null && cr.moveToNext()) {
                if(list.size > 99){
                    list.clear()
                }
                list.add(cr.getString(0))
            }
            cr?.close()
        }

        @SuppressLint("Range")
        fun checkAPN(apn: String?, apnUri: Uri):Boolean {
            var hasAPN = false
            // 检查当前连接的APN
            val cr: Cursor? = context.contentResolver
                .query(apnUri, null, null, null, null)
            while (cr != null && cr.moveToNext()) {
                if (cr.getString(cr.getColumnIndex("apn")).equals(apn)) {
                    hasAPN = true
                    break
                }
            }
            cr?.close()
            return hasAPN
        }

        companion object {
            var hasAPN = false
            private const val cmiot_ID = 0
            private const val apn_1_ID = 0
            private const val apn_2_ID = 0
            private const val apn_3_ID = 0
        }
    }
package com.sendi.deliveredrobot.entity

import com.google.gson.annotations.SerializedName
import com.sendi.deliveredrobot.model.Applet
import org.litepal.crud.LitePalSupport

data class Table_Applet_Config(
    var id:Int?,
    var bigScreenConfig: Table_Big_Screen?,
    var icon: String?,
    @SerializedName(value = "id") var appletId: Int?,
    var name: String?,
    var timeStamp: Long?,
    var type: Int?,
    var url: String?
): LitePalSupport(){
    constructor():this(null,null,null,null,null,null,null,null)

    companion object{
        fun create(applet: Applet): Table_Applet_Config {
            return Table_Applet_Config(
                -1,
                Table_Big_Screen.create(applet.bigScreenConfig),
                applet.icon,
                applet.appletId,
                applet.name,
                applet.timeStamp,
                applet.type,
                applet.url
            )
        }
    }
}
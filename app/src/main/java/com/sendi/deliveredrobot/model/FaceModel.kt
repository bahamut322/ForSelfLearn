package com.sendi.deliveredrobot.model

import android.graphics.Rect
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type
import kotlin.math.abs

/**
 * @Author Swn
 * @Data 2023/12/7
 * @describe 人脸位置/相识度Model
 */
data class FaceModel(
    @SerializedName("box") val box: Rect? = null,
    @SerializedName("feat") val feat: List<Double>? = null,
    @SerializedName("maskState") val maskState: Int? = 0
)
class RectDeserializer : JsonDeserializer<Rect> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Rect {
        val jsonArray = json?.asJsonArray
        if (jsonArray != null && jsonArray.size() == 4) {
            val left = abs(jsonArray[0].asInt)
            val top = abs(jsonArray[1].asInt)
            val right = abs(jsonArray[2].asInt)
            val bottom = abs(jsonArray[3].asInt)
            return Rect(left, top, right, bottom)
        }
        return Rect()
    }
}

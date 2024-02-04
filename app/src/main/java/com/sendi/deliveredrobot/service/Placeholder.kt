package com.sendi.deliveredrobot.service

import com.sendi.deliveredrobot.entity.entitySql.QuerySql

/**
 * @Author Swn
 * @Data 2024/1/31
 * @describe 文字替换
 */

enum class Placeholder(placeholder: String) {
    WAKE_UP_WORD("%唤醒词%"),
    FACE_NAME("%人脸姓名%"),
    POINT_NAME("%目标点名称%"),
    ROUTE_NAME("%路线名称%"),
    BUSINESS_NAME("%业务名称%");

    private val placeholder: String

    init {
        this.placeholder = placeholder
    }

    fun getPlaceholder(): String {
        return placeholder
    }


    companion object{
        /**
         * 根据标识位替换文字
         * @param text 需要替换的文字
         * @param name 姓名
         * @param pointName 目标点名称
         * @param route 路线名称
         * @param business 业务名称
         */
        fun replaceText(
            text: String,
            name: String = "",
            pointName: String = "",
            route: String = "",
            business: String = "",
        ): String {
            return text.replace(WAKE_UP_WORD.getPlaceholder(), QuerySql.robotConfig().wakeUpWord)
                .replace(FACE_NAME.getPlaceholder(), name)
                .replace(POINT_NAME.getPlaceholder(), pointName)
                .replace(ROUTE_NAME.getPlaceholder(), route)
                .replace(BUSINESS_NAME.getPlaceholder(), business)
        }
    }
}


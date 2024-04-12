package com.iflytek.aikitdemo.tool

/**
 * @Desc:通用工具类
 * @Author leon
 * @Date 2023/9/21-16:05
 * Copyright 2023 iFLYTEK Inc. All Rights Reserved.
 */

fun ByteArray.bytesToInt(): Int {
    var result = 0
    var abyte: Byte
    for (i in this.indices) {
        abyte = this[i]
        result += abyte.toInt() and 0xFF shl 8 * i
    }
    return result
}

fun String.javaStringToCStringLength(): Int {
    var length = 0
    for (element in this) {
        val c = element
        length += if (c.code <= 0x7F) {
            1
        } else if (c.code <= 0x7FF) {
            2
        } else if (c.code <= 0xFFFF) {
            3
        } else {
            4
        }
    }
    return length // 加1是为了计算结尾符'\0'的个数
}
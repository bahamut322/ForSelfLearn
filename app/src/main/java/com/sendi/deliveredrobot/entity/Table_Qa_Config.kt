package com.sendi.deliveredrobot.entity

import org.litepal.crud.LitePalSupport

/**
 * @Author Swn
 * @Data 2023/11/15
 * @describe 问答配置
 */
data class Table_Qa_Config(
    var qaJson : String? = ""
): LitePalSupport()

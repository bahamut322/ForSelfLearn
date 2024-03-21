package com.sendi.deliveredrobot.enum

enum class ASROrNlpModelTypeEnum(private var code: String) {
    AIUI("0"),
    AI_XIAO_YUE("1"),
    TEST_1("2"),
    TEST_2("3");
    fun getCode(): String {
        return code
    }

    companion object{
        private val pattern = "(?<=\\d)".toRegex()
        var voiceRecordType: String? = null
            set(value) {
                field = value
                answerPriority = splitPriority(value!!)
            }
        var answerPriority: Array<String>? = null
        private fun splitPriority(voiceRecordType: String): Array<String> {
            return voiceRecordType.split(pattern).toTypedArray()
        }
    }
}
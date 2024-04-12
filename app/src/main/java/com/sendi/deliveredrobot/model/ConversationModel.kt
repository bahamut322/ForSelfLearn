package com.sendi.deliveredrobot.model

import com.iflytek.vtncaetest.StreamingAsrModel

data class ConversationModel(
    var conversationAnswerModel: ConversationAnswerModel? = null,
    var streamingAsrModel: StreamingAsrModel? = null
)

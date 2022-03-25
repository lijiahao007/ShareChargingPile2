package com.lijiahao.sharechargingpile2.data


data class ImageMsgBody(
    val localPath: String,
    var remotePath: String,
    val extra: String
) : MsgBody(MsgType.IMAGE)
package com.lijiahao.sharechargingpile2.data

data class TextMsgBody(val message:String, val extra:String?): MsgBody(MsgType.TEXT)
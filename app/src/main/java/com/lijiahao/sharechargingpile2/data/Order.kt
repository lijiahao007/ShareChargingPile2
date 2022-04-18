package com.lijiahao.sharechargingpile2.data

data class Order(
    val id: String,
    val createTime: String,
    val completeTime:String,
    val updateTime:String,
    val beginChargeTime:String,
    val state:String,
    val price:Float,
    val pileId:String,
    val userId:String,
    val uuid:String
) {
    companion object {
        const val STATE_USING = "待完成"
        const val STATE_FINISH = "已完成"
        const val STATE_CANCEL = "已取消"
        const val STATE_UNPAID = "待支付"
    }
}

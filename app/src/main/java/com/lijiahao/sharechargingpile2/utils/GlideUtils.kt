package com.lijiahao.sharechargingpile2.utils

class GlideUtils {
    companion object {
        @JvmStatic
        fun getPileQRCodeRemotePath(localPath: String) :String{
            // 从ChargingPile的qrcodeurl转换为一个Glide能够直接加载的路径
            return "$SERVER_BASE_HTTP_URL/chargingPile/getPileQrCodePic?url=$localPath"
        }
    }
}
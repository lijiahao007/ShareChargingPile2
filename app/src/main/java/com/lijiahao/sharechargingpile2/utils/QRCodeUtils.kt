package com.lijiahao.sharechargingpile2.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.huawei.hms.hmsscankit.ScanUtil
import com.huawei.hms.ml.scan.HmsBuildBitmapOption
import com.huawei.hms.ml.scan.HmsScan

class QRCodeUtils {
    companion object {
        @JvmStatic
        fun generateQRCode(msg: String, width:Int=200, height:Int=200): Bitmap {
            val type = HmsScan.QRCODE_SCAN_TYPE
            val options = HmsBuildBitmapOption.Creator().setBitmapBackgroundColor(Color.TRANSPARENT)
                .setBitmapColor(Color.BLUE).setBitmapMargin(3).create()
            // 如果未设置HmsBuildBitmapOption对象，生成二维码参数options置null。
            return ScanUtil.buildBitmap(msg, type, width, height, options)
        }
    }
}
package com.lijiahao.sharechargingpile2.ui.QRCodeModule

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.huawei.hms.hmsscankit.ScanUtil
import com.huawei.hms.hmsscankit.WriterException
import com.huawei.hms.ml.scan.HmsBuildBitmapOption
import com.lijiahao.sharechargingpile2.R
import com.lijiahao.sharechargingpile2.databinding.FragmentQRCodeScanBinding
import com.huawei.hms.ml.scan.HmsScan

import com.huawei.hms.ml.scan.HmsScanAnalyzerOptions
import com.lijiahao.sharechargingpile2.di.GlideApp

class QRCodeScanFragment : Fragment() {

    private val binding: FragmentQRCodeScanBinding by lazy {
        FragmentQRCodeScanBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        init()
        return binding.root
    }

    private fun init() {
        // 1. 指定只扫描 QRCode
        val options =
            HmsScanAnalyzerOptions.Creator().setHmsScanTypes(HmsScan.QRCODE_SCAN_TYPE).create()

        // 2. 启动Default View
        binding.button.setOnClickListener {
            ScanUtil.startScan(requireActivity(), REQUEST_CODE_SCAN_ONE, options)
        }

        // 3.
        binding.btnGenerateQrCode.setOnClickListener {
            val content = "QR Code Content"
            val type = HmsScan.QRCODE_SCAN_TYPE
            val width = 200
            val height = 200
            val options = HmsBuildBitmapOption.Creator().setBitmapBackgroundColor(Color.TRANSPARENT)
                .setBitmapColor(Color.BLUE).setBitmapMargin(3).create()
            try {
                // 如果未设置HmsBuildBitmapOption对象，生成二维码参数options置null。
                val qrBitmap = ScanUtil.buildBitmap(content, type, width, height, options)
                context?.let { it1 ->
                    GlideApp.with(it1).load(qrBitmap).into(binding.qrCode)
                }
            } catch (e: WriterException) {
                Log.w("buildBitmap", e)
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK || data == null) {
            return
        }

        if (requestCode == REQUEST_CODE_SCAN_ONE) {
            // 导入图片扫描返回结果
            val obj = data.getParcelableExtra(ScanUtil.RESULT) as HmsScan?
            obj?.let {
                Log.i(TAG, "originalValue：${it.originalValue}") // 获取二维码数据。
            }

        }
    }

    companion object {
        const val REQUEST_CODE_SCAN_ONE = 10
        const val TAG = "QRCodeScanFragment"
    }

}
package com.lijiahao.sharechargingpile2.ui.QRCodeModule

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.huawei.hms.hmsscankit.ScanUtil
import com.huawei.hms.ml.scan.HmsScan
import com.huawei.hms.ml.scan.HmsScanAnalyzerOptions
import com.lijiahao.sharechargingpile2.data.QRCodeContent
import com.lijiahao.sharechargingpile2.databinding.FragmentQRCodeScanBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class QRCodeScanFragment : Fragment() {

    private val binding: FragmentQRCodeScanBinding by lazy {
        FragmentQRCodeScanBinding.inflate(layoutInflater)
    }
    private val gson = Gson()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        init()
        return binding.root
    }

    private fun init() {
        // 1. 指定只扫描 QRCode
        val options =
            HmsScanAnalyzerOptions.Creator().setHmsScanTypes(HmsScan.QRCODE_SCAN_TYPE).create()

        // 2. 启动Default View
        ScanUtil.startScan(requireActivity(), REQUEST_CODE_SCAN_ONE, options)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK || data == null) {
            requireActivity().finish()
            return
        }

        if (requestCode == REQUEST_CODE_SCAN_ONE) {
            // 导入图片扫描返回结果
            val obj = data.getParcelableExtra(ScanUtil.RESULT) as HmsScan?
            obj?.let { scan ->
                val originalValue = scan.originalValue
                originalValue?.let {
                    val qrContent =  gson.fromJson(it, QRCodeContent::class.java)
                    val stationId = qrContent.stationID
                    val pileId = qrContent.pileId
                    val action = QRCodeScanFragmentDirections.actionQRCodeScanFragmentToGenerateOrderFragment(stationId, pileId)
                    findNavController().navigate(action)
                }
            }
        } else {
            requireActivity().finish()
        }
    }

    companion object {
        const val REQUEST_CODE_SCAN_ONE = 10
        const val TAG = "QRCodeScanFragment"
    }

}
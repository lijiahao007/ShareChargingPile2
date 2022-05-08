package com.lijiahao.sharechargingpile2.ui.QRCodeModule

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.huawei.hms.hmsscankit.ScanUtil
import com.huawei.hms.ml.scan.HmsScan
import com.huawei.hms.ml.scan.HmsScanAnalyzerOptions
import com.lijiahao.sharechargingpile2.R
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
        Log.e(TAG, "启动扫描Activity的是 ${requireActivity()}")

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
                    var limitPileId: Int? = null
                    if (requireActivity() is QRCodeActivity) {
                        limitPileId = (requireActivity() as QRCodeActivity).limitPileId
                    }

                    if (limitPileId != null && pileId.toInt() != limitPileId) {
                        // 不符合
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle("错误")
                            .setMessage("该充电桩不是您预约的充电桩")
                            .setNegativeButton("取消") { dialog, which ->
                                dialog.cancel()
                                requireActivity().finish()
                            }
                            .show()
                        Log.e(TAG, "扫码充电桩不符合")
                    } else {
                        Log.e(TAG, "扫码充电桩正确")
                        val action = QRCodeScanFragmentDirections.actionQRCodeScanFragmentToGenerateOrderFragment(stationId, pileId)
                        findNavController().navigate(action)
                    }
                }
            }
        } else {
            requireActivity().finish()
        }
    }

    companion object {
        const val REQUEST_CODE_SCAN_ONE = 10
        const val TAG = "QRCodeScanFragment"
        const val PILE_ID_RESULT_KEY = "PILE_ID_FRAGMENT_RESULT_KEY"
        const val PILE_ID_BUNDLE_KEY = "PILE_ID_BUNDLE_KEY"
    }

}
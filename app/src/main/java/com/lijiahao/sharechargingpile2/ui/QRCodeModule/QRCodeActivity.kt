package com.lijiahao.sharechargingpile2.ui.QRCodeModule

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.lijiahao.sharechargingpile2.R
import com.lijiahao.sharechargingpile2.databinding.ActivityQrcodeBinding
import com.lijiahao.sharechargingpile2.utils.PermissionTool
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class QRCodeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQrcodeBinding

    var limitPileId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrcodeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (PermissionTool.usedOnCreate(this, permissions, permissionsHint)) {
            init()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (PermissionTool.usedOnRequestPermissionsResult(requestCode, permissions, grantResults, this, permissionsHint)) {
            init()
        }
    }

    private fun init() {
        // 初始化操作
        val pileId = intent.getIntExtra("pileId", 0)
        if (pileId != 0) {
            limitPileId = pileId
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // 将回调传递到Fragment中
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.qrcode_module_fragment_container_view)
        navHostFragment?.let { hostFragment ->
            hostFragment.childFragmentManager.fragments.forEach {
                it.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    companion object {
        const val TAG = "MAPACTIVITY"
        private val permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        )

        private val permissionsHint = arrayOf(
            "读取拓展存储空间",
            "使用相机"
        )
    }
}
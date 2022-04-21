package com.lijiahao.sharechargingpile2.ui.mainModule.notifications

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.launch
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.lijiahao.sharechargingpile2.databinding.FragmentModifyUserInfoBinding
import com.lijiahao.sharechargingpile2.di.GlideApp
import com.lijiahao.sharechargingpile2.network.request.ModifyUserInfoRequest
import com.lijiahao.sharechargingpile2.network.service.UserService
import com.lijiahao.sharechargingpile2.ui.mainModule.notifications.viemodel.NotificationsViewModel
import com.lijiahao.sharechargingpile2.ui.publishStationModule.AddStationFragment
import com.lijiahao.sharechargingpile2.utils.SHARED_PREFERENCES_NAME
import com.lijiahao.sharechargingpile2.utils.USER_ID_IN_PREFERENCES
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@AndroidEntryPoint
class ModifyUserInfoFragment : Fragment() {

    private val binding: FragmentModifyUserInfoBinding by lazy {
        FragmentModifyUserInfoBinding.inflate(layoutInflater)
    }

    private val viewModel: NotificationsViewModel by activityViewModels()

    private lateinit var albumLauncher: ActivityResultLauncher<Unit>

    @Inject
    lateinit var userService: UserService

    override fun onCreate(savedInstanceState: Bundle?) {
        albumLauncher = registerForActivityResult(
            object : ActivityResultContract<Unit, Uri?>() {
                override fun createIntent(context: Context, input: Unit): Intent {
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                    intent.type = "image/*"
                    return intent
                }

                override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
                    if (resultCode == Activity.RESULT_OK) {
                        intent?.data?.let { uri ->
                            Log.i(
                                AddStationFragment.TAG,
                                "uri=$uri \n encodePath=${uri.encodedPath}"
                            )
                            return uri
                        }
                    }
                    return null
                }
            }
        ) {
            it?.let {
                viewModel.avatarUrl.postValue(it)
            }
        }
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        initUI()
        initSubmit()
        return binding.root
    }

    private fun initUI() {

        binding.close.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.ivAvatar.setOnClickListener {
            albumLauncher.launch()
        }

        viewModel.userInfo.value?.let {
            it.avatarUrl?.let {
                GlideApp.with(this).load(it).into(binding.ivAvatar)
            }

            binding.userName.setText(it.name)
            binding.userRemark.setText(it.remark)

        }


        viewModel.avatarUrl.observe(this) {
            // 1. 将图片显示在头像中
            it?.let {
                val bitmap =
                    requireActivity().contentResolver.openFileDescriptor(it, "r")?.use {
                        BitmapFactory.decodeFileDescriptor(it.fileDescriptor)
                    }
                binding.ivAvatar.setImageBitmap(bitmap)
            }
        }
    }


    private fun initSubmit() {
        binding.submit.setOnClickListener {
            // 提交信息
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {

                // 1. 获取名字、个性签名、userId
                val name = binding.userName.text.toString()
                val remark = binding.userRemark.text.toString()
                var userId =
                    context!!.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
                        .getString(USER_ID_IN_PREFERENCES, "")
                if (userId == null) {
                    userId = ""
                }

                // 2. 构建请求类
                val request = ModifyUserInfoRequest(name, remark, userId)


                // 3. 提交图片
                // 3.1 将图片文件存储在应用filesDir中
                val uri = viewModel.avatarUrl.value
                uri?.let {
                    val inputStream = requireActivity().contentResolver.openInputStream(uri)
                    val outputFile = File.createTempFile("111", ".jpg", context!!.filesDir)
                    val outputStream = FileOutputStream(outputFile)
                    uri?.let {
                        try {
                            val buffer = ByteArray(1024)
                            while (true) {
                                val len = inputStream!!.read(buffer, 0, 1024)
                                if (len == -1) {
                                    break;
                                }
                                outputStream.write(buffer)
                            }
                            Log.i(
                                AddStationFragment.TAG,
                                "filename:${outputFile.name}  outputFileLen = ${outputFile.length()}"
                            )
                        } finally {
                            inputStream?.close()
                            outputStream.close()
                        }
                    }

                    // 3.2 构建MultipartBody.Part
                    val avatarPart = MultipartBody.Part.createFormData(
                        "avatar",
                        outputFile.name,
                        outputFile.asRequestBody("multipart/form-data".toMediaType())
                    )

                    // 4. 发起请求
                    try {
                        val url = userService.modifyUserInfo(avatarPart, request)
                        Log.i(TAG, "res = $url")

                        // 4.1 成功后将修改应用到viewModel中
                        withContext(Dispatchers.Main) {
                            viewModel.updateAvatarUrl(url)
                            viewModel.avatarUrl.postValue(null) // 设置为空，防止重复提交
                            Snackbar.make(binding.root, "更新成功", Snackbar.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        withContext(Dispatchers.Main) {
                            Snackbar.make(binding.root, "网络异常，更新失败", Snackbar.LENGTH_SHORT).show()
                        }
                    }
                }

                // 5. 当没有更改图片时
                if (uri == null) {
                    // 没有改变图片
                    try {
                        val res = userService.modifyUserInfoWithoutPic(request)
                        Log.i(TAG, "without picture. res = $res ")
                        withContext(Dispatchers.Main) {
                            Snackbar.make(binding.root, "更新成功", Snackbar.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        withContext(Dispatchers.Main) {
                            Snackbar.make(binding.root, "网络异常，更新失败", Snackbar.LENGTH_SHORT).show()
                        }
                    }
                }

                // 6. 更行name与remark
                viewModel.updateNameRemark(name, remark)

            }
        }
    }

    companion object {
        const val TAG = "ModifyUseInfoFragment"
    }

}
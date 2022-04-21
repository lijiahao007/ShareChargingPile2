package com.lijiahao.sharechargingpile2.ui.mainModule.notifications

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.lijiahao.sharechargingpile2.data.UserExtendInfo
import com.lijiahao.sharechargingpile2.databinding.FragmentUserExtendInfoBinding
import com.lijiahao.sharechargingpile2.network.request.ModifyExtendInfoRequest
import com.lijiahao.sharechargingpile2.network.service.UserService
import com.lijiahao.sharechargingpile2.ui.mainModule.notifications.adapter.UserExtendInfoAdapter
import com.lijiahao.sharechargingpile2.ui.mainModule.notifications.viemodel.NotificationsViewModel
import com.lijiahao.sharechargingpile2.utils.SHARED_PREFERENCES_NAME
import com.lijiahao.sharechargingpile2.utils.USER_ID_IN_PREFERENCES
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class UserExtendInfoFragment : Fragment() {

    private val binding: FragmentUserExtendInfoBinding by lazy {
        FragmentUserExtendInfoBinding.inflate(layoutInflater)
    }

    private val viewModel: NotificationsViewModel by activityViewModels()
    private val adapter = UserExtendInfoAdapter()

    @Inject
    lateinit var userService: UserService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        initUi()
        return binding.root
    }

    private fun initUi() {
        binding.userInfoRecyclerview.adapter = adapter
        val map = viewModel.userInfo.value?.extend
        val list = ArrayList<UserExtendInfo>()
        map?.forEach { (field, value) ->
            val info = UserExtendInfo(field, value)
            list.add(info)
        }
        adapter.submitList(list)

        binding.ivAdd.setOnClickListener {
            val list = ArrayList(adapter.currentList)
            list.add(UserExtendInfo("TODO", "TODO"))
            adapter.submitList(list)
        }

        binding.submit.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val userId =
                    context?.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
                        ?.getString(USER_ID_IN_PREFERENCES, "")
                userId?.let {
                    val modifyExtendInfoRequest = ModifyExtendInfoRequest(adapter.currentList, userId)
                    val res = userService.modifyExtendInfo(modifyExtendInfoRequest)
                    Log.i(TAG, "res = $res")
                    // 将数据更新到Viewmodel中
                    viewModel.addExtendInfo(adapter.currentList)
                    Snackbar.make(binding.root, "上传成功", Snackbar.LENGTH_SHORT).show()
                }
            }
        }

        binding.close.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    companion object {
        const val TAG = "UserExtendInfoFragment"
    }
}
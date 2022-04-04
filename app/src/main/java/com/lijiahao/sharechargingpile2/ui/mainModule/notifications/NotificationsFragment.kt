package com.lijiahao.sharechargingpile2.ui.mainModule.notifications

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.lijiahao.sharechargingpile2.databinding.FragmentNotificationsBinding
import com.lijiahao.sharechargingpile2.di.GlideApp
import com.lijiahao.sharechargingpile2.network.service.UserService
import com.lijiahao.sharechargingpile2.ui.loginRegisterModule.LoginActivity
import com.lijiahao.sharechargingpile2.utils.SHARED_PREFERENCES_NAME
import com.lijiahao.sharechargingpile2.utils.USER_ID_IN_PREFERENCES
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NotificationsFragment : Fragment() {

    private val binding: FragmentNotificationsBinding by lazy {
        FragmentNotificationsBinding.inflate(layoutInflater)
    }
    private val viewModel: NotificationsViewModel by activityViewModels()

    @Inject
    lateinit var userService: UserService

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        initUI()
        return binding.root
    }


    private fun initUI() {
        viewModel // 加载一下viewModel
        binding.lifecycleOwner = viewLifecycleOwner // 设置一下数据绑定的lifecycleOwner ，让LiveData自动更新
        binding.viewModel = viewModel

        viewModel.userInfo.observe(this) {
            val url = viewModel.userInfo.value?.avatarUrl
            GlideApp.with(this).load(url).into(binding.userImg) // 加载
        }

        binding.signOut.setOnClickListener {
            // 1. 将token无效化
            context?.apply {
                val sharedPreferences =
                    getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.remove("token")
                editor.apply()
            }
            // 2. 跳转到LoginActivity
            val intent = Intent(context, LoginActivity::class.java)
            startActivity(intent)
        }

        binding.modifyPwd.setOnClickListener {
            val action = NotificationsFragmentDirections.actionNavigationNotificationsToModifyPwdFragment()
            findNavController().navigate(action)
        }

        binding.otherInfo.setOnClickListener {
            val action = NotificationsFragmentDirections.actionNavigationNotificationsToUserExtendInfoFragment()
            findNavController().navigate(action)
        }

        binding.modify.setOnClickListener {
            val action = NotificationsFragmentDirections.actionNavigationNotificationsToModifyUserInfoFragment()
            findNavController().navigate(action)
        }

        binding.orderInfo.setOnClickListener {
            val action = NotificationsFragmentDirections.actionNavigationNotificationsToOrderListFragment()
            findNavController().navigate(action)
        }

    }

    companion object {
        const val TAG = "NotificationsFragment"
    }

}
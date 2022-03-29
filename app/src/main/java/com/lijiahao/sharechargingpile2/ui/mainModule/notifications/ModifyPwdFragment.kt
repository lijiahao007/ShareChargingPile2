package com.lijiahao.sharechargingpile2.ui.mainModule.notifications

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.lijiahao.sharechargingpile2.R
import com.lijiahao.sharechargingpile2.databinding.FragmentModifyPwdBinding
import com.lijiahao.sharechargingpile2.network.request.ModifyPwdRequest
import com.lijiahao.sharechargingpile2.network.service.UserService
import com.lijiahao.sharechargingpile2.utils.SHARED_PREFERENCES_NAME
import com.lijiahao.sharechargingpile2.utils.USER_ID_IN_PREFERENCES
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ModifyPwdFragment : Fragment() {
    private val binding: FragmentModifyPwdBinding by lazy {
        FragmentModifyPwdBinding.inflate(layoutInflater)
    }

    @Inject
    lateinit var userService: UserService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        initUI()

        return binding.root
    }

    private fun initUI() {
        binding.close.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.submit.setOnClickListener {
            val oldPwd = binding.oldPwd.text.toString()
            val newPwd = binding.newPwd.text.toString()
            val newPwd2 = binding.newPwd2.text.toString()

            if (newPwd != newPwd2) {
                Snackbar.make(binding.root, "两次密码不相同", Snackbar.LENGTH_SHORT).show()
            } else {
                lifecycleScope.launch(Dispatchers.IO) {
                    val userId = context?.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)?.getString(USER_ID_IN_PREFERENCES, "")
                    val request = ModifyPwdRequest(userId!!, oldPwd, newPwd)
                    val res = userService.modifyPwd(request)
                    if (res == "success") {
                        Snackbar.make(binding.root, "修改成功", Snackbar.LENGTH_SHORT).show()
                    } else {
                        Snackbar.make(binding.root, "原密码输入错误", Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }


}
package com.lijiahao.sharechargingpile2.ui.loginRegisterModule

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.lijiahao.sharechargingpile2.databinding.ActivitySignUpBinding
import com.lijiahao.sharechargingpile2.network.request.SignUpRequest
import com.lijiahao.sharechargingpile2.network.service.LoginService
import com.lijiahao.sharechargingpile2.ui.mainModule.MainActivity
import com.lijiahao.sharechargingpile2.utils.SHARED_PREFERENCES_NAME
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding


    @Inject
    lateinit var loginService: LoginService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initUI()

    }

    private fun initUI() {

        binding.signup.setOnClickListener {
            val username = binding.username.text.toString()
            val password = binding.password.text.toString()
            val password2 = binding.password2.text.toString()
            Log.i(TAG, "username:$username, password:$password, password2:$password2")

            if (password != password2) {
                Snackbar.make(binding.root, "两次密码不一样", Snackbar.LENGTH_SHORT).show()
            } else {
                val request = SignUpRequest(username, password)
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        val response = loginService.signup(request)
                        Log.i(TAG, "response: $response")
                        if (response.code == "success") {
                            // 注册成功 执行登录操作
                            // 1. 将userID 存入SharedPreference
                            val sharedPreferences =
                                getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
                            val editor = sharedPreferences.edit()
                            editor.putString("userId", response.userId)
                            editor.apply()

                            // 2. 跳转MainActivity
                            withContext(Dispatchers.Main) {
                                val intent = Intent(this@SignUpActivity, MainActivity::class.java)
                                startActivity(intent)
                            }
                        } else {
                            Snackbar.make(binding.root, "该账号已存在", Snackbar.LENGTH_SHORT).show()
                            binding.username.text.clear()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Snackbar.make(binding.root, "注册失败", Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
        }



    }

    companion object {
        const val TAG = "SignUpActivity"
    }
}
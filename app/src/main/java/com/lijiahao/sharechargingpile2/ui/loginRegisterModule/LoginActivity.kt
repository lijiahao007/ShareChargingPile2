package com.lijiahao.sharechargingpile2.ui.loginRegisterModule

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.lijiahao.sharechargingpile2.databinding.ActivityLoginBinding
import com.lijiahao.sharechargingpile2.network.request.LoginRequest
import com.lijiahao.sharechargingpile2.network.service.LoginService
import com.lijiahao.sharechargingpile2.ui.mainModule.MainActivity
import com.lijiahao.sharechargingpile2.utils.LOGIN_OUT_OF_TIME
import com.lijiahao.sharechargingpile2.utils.SHARED_PREFERENCES_NAME
import com.lijiahao.sharechargingpile2.utils.TOKEN_IN_PREFERENCES
import com.lijiahao.sharechargingpile2.utils.USER_ID_IN_PREFERENCES
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    @Inject
    lateinit var loginService: LoginService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initUI()
    }


    private fun initUI() {

        binding.username.setText("13535853646")
        binding.password.setText("123456")


        binding.login.setOnClickListener {
            val username = binding.username.text.toString()
            val password = binding.password.text.toString()
            Log.i(TAG, "username:$username,  password:$password")
            val request = LoginRequest(username, password)
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val loginResponse = loginService.login(request)
                    Log.i(TAG, "response: $loginResponse")
                    when (loginResponse.code) {
                        "success" -> {
                            // 登录成功
                            // 1. 将userID 存入SharedPreference
                            // 2. 将token 存入SharedPreference
                            val sharedPreferences =
                                getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
                            val editor = sharedPreferences.edit()
                            editor.putString(USER_ID_IN_PREFERENCES, loginResponse.userId)
                            editor.putString(TOKEN_IN_PREFERENCES, loginResponse.token)
                            editor.apply()

                            Log.i(TAG, "userId: ${loginResponse.userId}")
                            Log.i(TAG, "token: ${loginResponse.token}")

                            // 2. 跳转MainActivity
                            withContext(Dispatchers.Main) {
                                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                startActivity(intent)
                            }

                        }
                        "wrong pwd" -> {
                            Snackbar.make(binding.root, "密码错误", Snackbar.LENGTH_SHORT).show()
                        }
                        else -> {
                            Snackbar.make(binding.root, "账号不存在", Snackbar.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Snackbar.make(binding.root, "登录失败", Snackbar.LENGTH_SHORT).show()
                }
            }

        }

        binding.signup.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

    }


    companion object {
        const val TAG = "LoginActivity"
    }
}
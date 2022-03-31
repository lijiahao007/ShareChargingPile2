package com.lijiahao.sharechargingpile2.ui.loginRegisterModule

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.lijiahao.sharechargingpile2.data.SharedPreferenceData
import com.lijiahao.sharechargingpile2.databinding.ActivityLoginBinding
import com.lijiahao.sharechargingpile2.network.request.LoginRequest
import com.lijiahao.sharechargingpile2.network.service.LoginService
import com.lijiahao.sharechargingpile2.ui.mainModule.MainActivity
import com.lijiahao.sharechargingpile2.utils.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    @Inject
    lateinit var loginService: LoginService

    @Inject
    lateinit var sharedPreferenceData: SharedPreferenceData

    private val job = Job() // job相关协程，只有在当前Activity可见的时候才会执行，当stop是会取消所有协程。
    private val viewScope = CoroutineScope(job)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initUI()
        autoLogin()
    }

    private fun autoLogin() {
        // 自动登录
        viewScope.launch(Dispatchers.IO) {
            try {
                val response = loginService.login(LoginRequest(sharedPreferenceData.account, sharedPreferenceData.password))
                Log.i(TAG, "自动登录response: $response,  data = $sharedPreferenceData")
                if (response.code == "success") {
                    // 跳转MainActivity
                    withContext(Dispatchers.Main) {
                        Log.i(TAG, "自动登录了")
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onStop() {
        job.cancel()
        super.onStop()
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
                            editor.putString(USER_ACCOUNT_IN_PREFERENCES, username)
                            editor.putString(USER_PASSWORD_IN_PREFERENCES, password)

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
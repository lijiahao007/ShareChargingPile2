package com.lijiahao.sharechargingpile2.ui.loginRegisterModule

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.lijiahao.sharechargingpile2.data.SharedPreferenceData
import com.lijiahao.sharechargingpile2.data.TokenInfo
import com.lijiahao.sharechargingpile2.databinding.ActivityLoginBinding
import com.lijiahao.sharechargingpile2.network.request.LoginRequest
import com.lijiahao.sharechargingpile2.network.service.LoginService
import com.lijiahao.sharechargingpile2.ui.mainModule.MainActivity
import com.lijiahao.sharechargingpile2.utils.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.time.LocalDateTime
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    @Inject
    lateinit var loginService: LoginService

    @Inject
    lateinit var sharedPreferenceData: SharedPreferenceData

    private var isAutoLogin: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initUI()
    }


    override fun onResume() {
        super.onResume()
        if (isAutoLogin) {
            autoLogin()
        }
    }

    private fun autoLogin() {
        // 自动登录
        if (sharedPreferenceData.token != "" && sharedPreferenceData.userId != "") {
            val token = sharedPreferenceData.token
            val userId = sharedPreferenceData.userId
            val tokenInfo = TokenInfo.getTokenInfoFromToken(token)
            if (tokenInfo.aud == userId && LocalDateTime.now().isBefore(tokenInfo.exp)) {
                // token合法
                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                startActivity(intent)
            } else {
                Snackbar.make(binding.root, "登录过期，请重新登录", Snackbar.LENGTH_SHORT).show()
            }
        }
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
                            isAutoLogin = true

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

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.getStringExtra(NEW_INTENT_EXTRA)?.let {
            when (it) {
                NOTIFICATIONFRAGMENT_TO_LOGINACTIVITY -> {
                    isAutoLogin = false
                    Snackbar.make(binding.root, "注销成功，请重新登录", Snackbar.LENGTH_SHORT).show()
                }
                TOKENINTERCEPTOR_TO_LOGINACTIVITY -> {
                    isAutoLogin = false
                    Snackbar.make(binding.root, "登录过期，请重新登录", Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        const val TAG = "LoginActivity"
        const val NEW_INTENT_EXTRA = "message"
        const val NOTIFICATIONFRAGMENT_TO_LOGINACTIVITY = "NOTIFICATIONFRAGMENT_TO_LOGINACTIVITY"
        const val TOKENINTERCEPTOR_TO_LOGINACTIVITY = "TOKENINTERCEPTOR_TO_LOGINACTIVITY"

    }
}
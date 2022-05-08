package com.lijiahao.sharechargingpile2.ui.chatModule.viewmodel

import android.util.Log
import androidx.annotation.MainThread
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lijiahao.sharechargingpile2.dao.MessageDao
import com.lijiahao.sharechargingpile2.data.SharedPreferenceData
import com.lijiahao.sharechargingpile2.network.response.UserInfoResponse
import com.lijiahao.sharechargingpile2.network.service.UserService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import java.lang.Exception
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject

@HiltViewModel
class MessageListViewModel @Inject constructor(
    private val messageDao: MessageDao,
    private val sharedPreferenceData: SharedPreferenceData,
    private val userService: UserService,
) : ViewModel() {

    val userInfoResponseList: MutableLiveData<List<UserInfoResponse>> =
        MutableLiveData(ArrayList<UserInfoResponse>())

    lateinit var curUserInfo: UserInfoResponse

    init {
        getData()
    }

    @MainThread
    fun addUserInfoResponse(info: UserInfoResponse) {
        userInfoResponseList.value?.let {
            val list = ArrayList(it)
            list.add(info)
            userInfoResponseList.value = list
        }
    }

    fun postUserInfoResponse(info: UserInfoResponse) {
        userInfoResponseList.value?.let {
            val list = ArrayList(it)
            list.add(info)
            userInfoResponseList.postValue(list)
        }
    }

    fun postUserInfoResponse(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val userInfoResponse = userService.getUserInfo(userId)
            postUserInfoResponse(userInfoResponse)
        }
    }

    fun getData() {
        val userId = sharedPreferenceData.userId
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. 从数据库中获取有过聊天记录的 UserID
                val userIdList = messageDao.queryMessageAllUserIdExceptCurUser(userId)


                // 2. 从服务端获取所有UserId的信息
                val userInfoResponseList = CopyOnWriteArrayList<UserInfoResponse>()
                val tasks = ArrayList<Deferred<Boolean>>()
                userIdList.forEach {
                    val task = async {
                        val userInfoResponse = userService.getUserInfo(it)
                        userInfoResponseList.add(userInfoResponse)
                    }
                    tasks.add(task)
                }


                // 3. 获取用户本人的信息
                curUserInfo = userService.getUserInfo(sharedPreferenceData.userId)


                // 4. 将数据保存在liveData中
                tasks.forEach { it.await() }
                this@MessageListViewModel.userInfoResponseList.postValue(userInfoResponseList)
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(TAG, "Message数据获取异常")
            }

        }

    }

    companion object {
        const val TAG = "MessageListViewModel"
    }
}
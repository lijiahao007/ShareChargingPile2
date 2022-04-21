package com.lijiahao.sharechargingpile2.ui.mainModule.notifications.viemodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.*
import com.lijiahao.sharechargingpile2.data.SharedPreferenceData
import com.lijiahao.sharechargingpile2.data.UserExtendInfo
import com.lijiahao.sharechargingpile2.network.response.UserInfoResponse
import com.lijiahao.sharechargingpile2.network.service.UserService
import com.lijiahao.sharechargingpile2.ui.mainModule.notifications.NotificationsFragment
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val sharedPreferenceData: SharedPreferenceData,
    private val userService: UserService
) : ViewModel() {

    private val _userInfo: MutableLiveData<UserInfoResponse> = MutableLiveData()
    val userInfo: LiveData<UserInfoResponse> = _userInfo
    val avatarUrl: MutableLiveData<Uri?> = MutableLiveData(null) // 该url是修改后本地图片uri，不是远程图片地址

    init {
        loadData()
    }

    fun addExtendInfo(list: List<UserExtendInfo>) {
        val userInfoResponse = _userInfo.value
        userInfoResponse?.let {
            val map = HashMap<String, String>()
            list.forEach {
                map[it.field] = it.value
            }
            userInfoResponse.extend = map
            _userInfo.postValue(userInfoResponse!!)
        }
    }

    fun updateAvatarUrl(url: String) {
        val userInfo = _userInfo.value
        userInfo?.let {
            it.avatarUrl = url
            _userInfo.postValue(it)
        }
    }


    fun updateNameRemark(name:String, remark:String) {
        val userInfo = _userInfo.value
        userInfo?.let {
            it.name = name
            it.remark = remark
            _userInfo.postValue(it)
        }
    }


    private fun loadData() {
        viewModelScope.launch(Dispatchers.IO) {
            val userId = sharedPreferenceData.userId
            val response = userService.getUserInfo(userId!!)
            Log.i(NotificationsFragment.TAG, "response = $response")
            _userInfo.postValue(response)
        }
    }

}
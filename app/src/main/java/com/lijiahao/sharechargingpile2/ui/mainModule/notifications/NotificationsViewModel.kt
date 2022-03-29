package com.lijiahao.sharechargingpile2.ui.mainModule.notifications

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.annotation.MainThread
import androidx.lifecycle.*
import com.lijiahao.sharechargingpile2.data.UserExtendInfo
import com.lijiahao.sharechargingpile2.network.response.UserInfoResponse
import com.lijiahao.sharechargingpile2.network.service.UserService
import com.lijiahao.sharechargingpile2.utils.SHARED_PREFERENCES_NAME
import com.lijiahao.sharechargingpile2.utils.USER_ID_IN_PREFERENCES
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    @ApplicationContext val context: Context,
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
            val sharedPreferences =
                context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
            val userId = sharedPreferences?.getString(USER_ID_IN_PREFERENCES, "")
            val response = userService.getUserInfo(userId!!)
            Log.i(NotificationsFragment.TAG, "response = $response")
            _userInfo.postValue(response)
        }
    }

}
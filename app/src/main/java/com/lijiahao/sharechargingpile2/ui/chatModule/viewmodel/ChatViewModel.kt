package com.lijiahao.sharechargingpile2.ui.chatModule.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(): ViewModel() {
    private val _picUrlList: MutableLiveData<Uri> = MutableLiveData()
    val picUrlList: LiveData<Uri> = _picUrlList

    fun setUri(uri: Uri) {
        _picUrlList.value = uri
    }


}
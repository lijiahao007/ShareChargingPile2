package com.lijiahao.sharechargingpile2.ui.mainModule.notifications.viemodel

import android.util.Log
import androidx.lifecycle.*
import com.lijiahao.sharechargingpile2.data.SharedPreferenceData
import com.lijiahao.sharechargingpile2.network.response.QueryOrderResponse
import com.lijiahao.sharechargingpile2.network.service.OrderService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderViewModel @Inject constructor(
    val orderService: OrderService,
    val sharedPreferenceData: SharedPreferenceData
) : ViewModel() {


    val queryOrderResponse = MutableLiveData<QueryOrderResponse>()

    fun getData() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val queryOrderResponse = orderService.queryOrderByUserId(sharedPreferenceData.userId.toInt())
                Log.i("OrderViewModel", "queryResponse: $queryOrderResponse")
                this@OrderViewModel.queryOrderResponse.postValue(queryOrderResponse)
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("OrderViewModel", "查询订单出错")
            }
        }
    }
}
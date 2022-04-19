package com.lijiahao.sharechargingpile2.ui.mainModule.notifications

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lijiahao.sharechargingpile2.data.Order
import com.lijiahao.sharechargingpile2.data.SharedPreferenceData
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
    // 进行中的Order
    val processingOrder = MutableLiveData<List<Order>>(ArrayList())

    // 完成的Order
    val finishOrder = MutableLiveData<List<Order>>(ArrayList())

    // 提供服务的订单
    val serviceOrder = MutableLiveData<Map<String, Map<String, List<Order>>>>(HashMap())

    init {
        getData()
    }

    fun getData() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val queryOrderResponse = orderService.queryOrder(sharedPreferenceData.userId.toInt())
                Log.i("OrderViewModel", "queryResponse: $queryOrderResponse")
                processingOrder.postValue(queryOrderResponse.processingOrder)
                finishOrder.postValue(queryOrderResponse.finishOrder)
                serviceOrder.postValue(queryOrderResponse.serviceOrder)
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("OrderViewModel", "查询订单出错")
            }
        }
    }
}
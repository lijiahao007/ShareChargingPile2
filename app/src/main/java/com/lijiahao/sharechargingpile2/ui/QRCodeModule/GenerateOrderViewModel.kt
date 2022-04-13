package com.lijiahao.sharechargingpile2.ui.QRCodeModule

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.lijiahao.sharechargingpile2.data.ChargingPile
import com.lijiahao.sharechargingpile2.data.Order
import com.lijiahao.sharechargingpile2.network.response.StationInfo
import com.lijiahao.sharechargingpile2.network.response.UserInfoResponse
import com.lijiahao.sharechargingpile2.network.service.ChargingPileStationService
import com.lijiahao.sharechargingpile2.network.service.UserService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception
import java.lang.IllegalArgumentException
import java.lang.RuntimeException
import java.lang.reflect.InvocationTargetException
import javax.inject.Inject

class GenerateOrderViewModel(
    private val stationId: String,
    private val pileId: String,
    private val chargingPileStationService: ChargingPileStationService,
    private val userService: UserService
) : ViewModel() {


    // 1. 获取充电站信息
    private val _stationInfo: MutableLiveData<StationInfo> = MutableLiveData()
    val stationInfo: LiveData<StationInfo> = _stationInfo


    // 2. 获取充电桩信息
    val pileInfo: LiveData<ChargingPile> = _stationInfo.map { station ->
        station.pileList.find { it.id.toString() == pileId } ?: ChargingPile()
    }

    // 3. 获取用户信息
    val userInfo: LiveData<UserInfoResponse> = _stationInfo.switchMap {
        val userId = it.station.userId
        liveData {
            emit(userService.getUserInfo(userId.toString()))
        }
    }

    // 4. 订单信息
    private val _order:MutableLiveData<Order> = MutableLiveData()
    val order: LiveData<Order> = _order



    init {
        getData()
        Log.i("GenerateOrderViewModel", "init!!!")
    }


    fun getData() {
        viewModelScope.launch(Dispatchers.IO) {
            val stationInfo = chargingPileStationService.getStationInfoByStationId(stationId)
            _stationInfo.postValue(stationInfo)
        }
    }

    fun setOrder(order:Order) {
        _order.value = order
    }


    companion object {
        fun getGenerateOrderViewModelFactory(
            stationId: String,
            pileId: String,
            chargingPileStationService: ChargingPileStationService,
            userService: UserService
        ) = object : ViewModelProvider.NewInstanceFactory() {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return if (modelClass.isAssignableFrom(GenerateOrderViewModel::class.java)) {
                    GenerateOrderViewModel(
                        stationId,
                        pileId,
                        chargingPileStationService,
                        userService
                    ) as T
                } else {
                    throw IllegalArgumentException("GenerateOrderViewModel 参数错误。 需要stationId&pileId")
                }
            }
        }
    }


}
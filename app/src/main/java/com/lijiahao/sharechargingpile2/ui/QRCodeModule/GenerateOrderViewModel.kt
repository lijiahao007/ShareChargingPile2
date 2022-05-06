package com.lijiahao.sharechargingpile2.ui.QRCodeModule

import android.util.Log
import androidx.lifecycle.*
import com.lijiahao.sharechargingpile2.data.ChargingPile
import com.lijiahao.sharechargingpile2.data.Order
import com.lijiahao.sharechargingpile2.network.response.StationInfo
import com.lijiahao.sharechargingpile2.network.response.UserInfoResponse
import com.lijiahao.sharechargingpile2.network.service.ChargingPileStationService
import com.lijiahao.sharechargingpile2.network.service.UserService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// 如果能够设置_order MutableLiveData的话，就不用设置orderId (默认设置为"0")
// 如果不能够设置_order MutableLiveData的话，就直接传入一个orderId就好了。(一个大于0的数字符串)
class GenerateOrderViewModel(
    var stationId: String,
    var pileId: String,
    private val chargingPileStationService: ChargingPileStationService,
    private val userService: UserService,
) : ViewModel() {

    // 1. 获取充电站信息
    private val _stationInfo: MutableLiveData<StationInfo> = MutableLiveData()
    val stationInfo: LiveData<StationInfo> = _stationInfo


    // 2. 获取充电桩信息
    val pileInfo: LiveData<ChargingPile> = _stationInfo.map { station ->
        station.pileList.find { it.id.toString() == pileId } ?: ChargingPile()
    }

    // 3. 通过网络获取用户信息
    val userInfo: LiveData<UserInfoResponse> = _stationInfo.switchMap {
        val userId = it.station.userId
        liveData {
            emit(userService.getUserInfo(userId.toString()))
        }
    }

    // 4. 订单信息
    private val _order: MutableLiveData<Order> = MutableLiveData()
    val order: LiveData<Order> = _order


    init {
        getData()
        Log.i("GenerateOrderViewModel", "init!!!")
    }


    fun getData() {
        viewModelScope.launch(Dispatchers.IO) {
            // 1. 获取StationInfo
            val stationInfo = chargingPileStationService.getStationInfoByStationId(stationId)
            _stationInfo.postValue(stationInfo)
        }
    }


    fun setOrder(order: Order) {
        _order.value = order
    }


    companion object {

        const val DEFAULT_ORDER_ID = "0"

        fun getGenerateOrderViewModelFactory(
            stationId: String,
            pileId: String,
            chargingPileStationService: ChargingPileStationService,
            userService: UserService,
        ) = object : ViewModelProvider.NewInstanceFactory() {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return if (modelClass.isAssignableFrom(GenerateOrderViewModel::class.java)) {
                    GenerateOrderViewModel(
                        stationId,
                        pileId,
                        chargingPileStationService,
                        userService,
                    ) as T
                } else {
                    throw IllegalArgumentException("GenerateOrderViewModel 参数错误。 需要stationId&pileId")
                }
            }
        }
    }


}
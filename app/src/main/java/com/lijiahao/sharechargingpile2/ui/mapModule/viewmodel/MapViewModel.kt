package com.lijiahao.sharechargingpile2.ui.mapModule.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.amap.api.maps.Projection
import com.amap.api.maps.model.LatLng
import com.lijiahao.sharechargingpile2.data.*
import com.lijiahao.sharechargingpile2.network.service.ChargingPileStationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val chargingPileStationService: ChargingPileStationService,
) : ViewModel() {
    var mapCenterPos: LatLng = LatLng(0.0, 0.0) // 屏幕中心位置
    var bluePointPos: LatLng = LatLng(0.0, 0.0) // 定位蓝点位置（当前位置, 在addOnMyLocationChangeListener 中赋予值）
    lateinit var projection: Projection
    var stationList: List<ChargingPileStation> = ArrayList<ChargingPileStation>()
    var stationTagMap: Map<String, List<Tags>> = HashMap<String, List<Tags>>()
    var stationPileMap: Map<String, List<ChargingPile>> = HashMap<String, List<ChargingPile>>()
    var stationOpenTimeMap: Map<String, List<OpenTime>> = HashMap<String, List<OpenTime>>()
    var stationOpenDayMap: Map<String, List<OpenDayInWeek>> = HashMap()
    var stationElectricChargePeriodMap: Map<String, List<ElectricChargePeriod>> = HashMap()
    private val _isRemoteDataReady: MutableLiveData<Boolean> = MutableLiveData<Boolean>(false)
    val isRemoteDataReady: LiveData<Boolean> = _isRemoteDataReady
    private val _isLocationReady:MutableLiveData<Boolean> = MutableLiveData(false)
    val isLocationReady: LiveData<Boolean> = _isLocationReady


    // 全部信息都在这里
    val stationInfoMap: HashMap<String, StationListItemViewModel> =
        HashMap<String, StationListItemViewModel>()

    val naviEndPoint:MutableLiveData<LatLng> = MutableLiveData()
    val finishNavi:MutableLiveData<Boolean> = MutableLiveData()


    fun dataReady() {
        _isRemoteDataReady.postValue(true);
    }

    fun locationReady() {
        _isLocationReady.postValue(true);
    }


    init {
        getData()
    }


    fun getData() {
        viewModelScope.launch(Dispatchers.IO) {
            // 2. 请求数据
            val stationTask = async {
                try {
                    val list = chargingPileStationService.getStations()
                    stationList = list
                    list.forEach {
                        Log.i("stationTask", it.toString())
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e("stationTask", "网络请求出错 chargingPileStationService.getStations")
                }
            }
            val tagsTask = async {
                try {
                    val stationTags = chargingPileStationService.getStationTags()
                    stationTagMap = stationTags
                    stationTags.forEach { (id, list) ->
                        Log.i("tagsTask", "id = $id , $list")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e("tagsTask", "网络请求出错 chargingPileStationService.getStationTags")
                }
            }
            val pileTask = async {
                try {
                    val stationPiles = chargingPileStationService.getStationPiles()
                    stationPileMap = stationPiles
                    stationPiles.forEach { (id, list) ->
                        Log.i("pileTask", "id=$id, $list")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e("pileTask", "网络请求出错 chargingPileStationService.getStationPiles")

                }
            }
            val openTimeTask = async {
                try {
                    val stationOpenTime = chargingPileStationService.getStationOpenTime()

                    stationOpenTime.forEach { (key, value) ->
                        value.forEach {
                            it.beginTime = it.beginTime.substring(0, 5)
                            it.endTime = it.endTime.substring(0, 5)
                        }
                    }

                    stationOpenTimeMap = stationOpenTime
                    stationOpenTime.forEach { (id, list) ->
                        Log.i(TAG, "id=$id, $list")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e("openTimeTask", "网络请求出错 chargingPileStationService.getStationOpenTime")
                }
            }

            val openDayTask = async {
                try {
                    val stationOpenDay = chargingPileStationService.getStationOpenDay()
                    stationOpenDayMap=  stationOpenDay
                    stationOpenDay.forEach { (id, list) ->
                        Log.i(TAG, "id=$id, $list")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e("openTimeTask", "网络请求出错 chargingPileStationService.getStationOpenTime")
                }
            }

            val electricChargeTask = async {
                try {
                    val stationElectricChargePeriod = chargingPileStationService.getStationElectricCharge()

                    stationElectricChargePeriod.forEach { key, value ->
                        value.forEach { period ->
                            period.beginTime = period.beginTime.substring(0, 5)
                            period.endTime = period.endTime.substring(0, 5)
                        }
                    }
                    stationElectricChargePeriodMap = stationElectricChargePeriod
                    stationElectricChargePeriod.forEach { (id, list) ->
                        Log.i(TAG, "id=$id, $list")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e("openTimeTask", "网络请求出错 chargingPileStationService.getStationElectricCharges")
                }
            }

            stationTask.await()
            tagsTask.await()
            pileTask.await()
            openTimeTask.await()
            openDayTask.await()
            electricChargeTask.await()

            stationList.forEach { station ->
                val id = station.id.toString()
                val tags = stationTagMap[id] ?: ArrayList<Tags>()
                val piles = stationPileMap[id] ?: ArrayList<ChargingPile>()
                val openTimes = stationOpenTimeMap[id] ?: ArrayList<OpenTime>()
                val openDays = stationOpenDayMap[id] ?: ArrayList<OpenDayInWeek>()
                val electricCharges = stationElectricChargePeriodMap[id]?: ArrayList()
                val itemViewModel = StationListItemViewModel(station, tags, piles, openTimes, openDays, electricCharges,0.0f)
                stationInfoMap[id] = itemViewModel
            }
            dataReady() // 准备好了

        }
    }

    companion object {
        const val TAG = "MapViewModel"
    }


}
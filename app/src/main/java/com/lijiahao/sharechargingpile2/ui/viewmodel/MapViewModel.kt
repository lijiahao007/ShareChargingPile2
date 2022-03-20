package com.lijiahao.sharechargingpile2.ui.viewmodel

import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.*
import com.amap.api.maps.AMapUtils
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MarkerOptions
import com.lijiahao.sharechargingpile2.R
import com.lijiahao.sharechargingpile2.data.ChargingPile
import com.lijiahao.sharechargingpile2.data.ChargingPileStation
import com.lijiahao.sharechargingpile2.data.OpenTime
import com.lijiahao.sharechargingpile2.data.Tags
import com.lijiahao.sharechargingpile2.network.service.ChargingPileStationService
import com.lijiahao.sharechargingpile2.ui.MapActivity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val chargingPileStationService: ChargingPileStationService,
) : ViewModel() {
    var mapCenterPos: LatLng = LatLng(0.0, 0.0) // 屏幕中心位置
    var bluePointPos: LatLng = LatLng(0.0, 0.0) // 定位蓝点位置（当前位置, 在addOnMyLocationChangeListener 中赋予值）
    var stationList: List<ChargingPileStation> = ArrayList<ChargingPileStation>()
    var stationTagMap: Map<String, List<Tags>> = HashMap<String, List<Tags>>()
    var stationPileMap: Map<String, List<ChargingPile>> = HashMap<String, List<ChargingPile>>()
    var stationOpenTimeMap: Map<String, List<OpenTime>> = HashMap<String, List<OpenTime>>()
    private val _isReady: MutableLiveData<Boolean> = MutableLiveData<Boolean>(false)
    val isReady: LiveData<Boolean> = _isReady
    val stationInfoMap: HashMap<String, StationListItemViewModel> =
        HashMap<String, StationListItemViewModel>()


    fun ready() {
        _isReady.postValue(true);
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
                    stationOpenTimeMap = stationOpenTime
                    stationOpenTime.forEach { (id, list) ->
                        Log.i(MapActivity.TAG, "id=$id, $list")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e("openTimeTask", "网络请求出错 chargingPileStationService.getStationOpenTime")
                }
            }

            stationTask.await()
            tagsTask.await()
            pileTask.await()
            openTimeTask.await()

            stationList.forEach { station ->
                val stationPos = LatLng(station.latitude, station.longitude)
                val id = station.id.toString()
                val tags = stationTagMap[id] ?: ArrayList<Tags>()
                val piles = stationPileMap[id] ?: ArrayList<ChargingPile>()
                val openTimes = stationOpenTimeMap[id] ?: ArrayList<OpenTime>()
                val itemViewModel = StationListItemViewModel(station, tags, piles, openTimes, 0.0f)
                stationInfoMap[id] = itemViewModel

            }

            ready() // 准备好了

        }
    }


}
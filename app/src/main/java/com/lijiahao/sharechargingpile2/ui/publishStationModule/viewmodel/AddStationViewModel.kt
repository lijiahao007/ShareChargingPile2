package com.lijiahao.sharechargingpile2.ui.publishStationModule.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.annotation.MainThread
import androidx.databinding.BaseObservable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lijiahao.sharechargingpile2.data.ChargingPile
import com.lijiahao.sharechargingpile2.data.ChargingPileStation
import com.lijiahao.sharechargingpile2.data.ElectricChargePeriod
import com.lijiahao.sharechargingpile2.data.SharedPreferenceData
import com.lijiahao.sharechargingpile2.ui.publishStationModule.AddStationFragment
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class AddStationViewModel @Inject constructor(
    val sharedPreferenceData: SharedPreferenceData
) : ViewModel() {

    // TODO: 将下面属性包含到一个ChargingStation中
    var latitude: Double = 0.0
    var longitude: Double = 0.0
    var posDescription: String = ""
    var pileList: ArrayList<ChargingPile> = ArrayList<ChargingPile>()
    var stationName: String = ""
    var parkFee: Double = -1.0
    var remark: String = ""
    var stationId = 0
    var stationCollection = 0
    var electricPeriodChargeList: ArrayList<ElectricChargePeriod> = ArrayList()


    // 下面四个LiveData用于图片的显示（其中RemoteUriList主要用于Station信息修改界面的远程图片显示）
    private val _stationPicUriList: MutableLiveData<ArrayList<Uri>> =
        MutableLiveData<ArrayList<Uri>>(ArrayList<Uri>())
    val stationPicUriList: LiveData<ArrayList<Uri>> = _stationPicUriList
    private val _stationPicRemoteUriList: MutableLiveData<ArrayList<String>> =
        MutableLiveData(ArrayList<String>())
    val stationPicRemoteUriList: LiveData<ArrayList<String>> = _stationPicRemoteUriList

    fun clear() {
        // 清空所有数据
        latitude = 0.0
        longitude = 0.0
        posDescription = ""
        pileList = ArrayList<ChargingPile>()
        stationName = ""
        parkFee = -1.0
        remark = ""
        stationId = 0
        stationCollection = 0
        _stationPicUriList.value = ArrayList<Uri>()
        _stationPicRemoteUriList.value = ArrayList<String>()
        electricPeriodChargeList = ArrayList()
    }


    @MainThread
    fun setRemoteUriList(list: List<String>) {
        _stationPicRemoteUriList.value?.let {
            _stationPicRemoteUriList.value = list as ArrayList<String>
        }
    }

    @MainThread
    fun addLocalUri(uri: Uri) {
        val list = ArrayList<Uri>(_stationPicUriList.value as ArrayList<Uri>)
        list.add(uri)
        _stationPicUriList.value = list
    }

    @MainThread
    fun refreshLocalImage() {
        // 单纯的通知
        _stationPicUriList.value = _stationPicUriList.value
    }


    fun removeImage(index: Int) {
        if (_stationPicRemoteUriList.value == null || _stationPicUriList.value == null) {
            return
        }

        val remoteSize = _stationPicRemoteUriList.value!!.size
        val localSize = _stationPicUriList.value!!.size
        if (index < remoteSize) {
            // 0~remoteSize-1
            val list = ArrayList(_stationPicRemoteUriList.value!!)
            list.removeAt(index)
            _stationPicRemoteUriList.value = list
        } else {
            val list = ArrayList(_stationPicUriList.value!!)
            list.removeAt(index - remoteSize)
            _stationPicUriList.value = list
        }
    }

    @MainThread
    fun removeLocalUri(index: Int) {
        val list = ArrayList<Uri>(_stationPicUriList.value as ArrayList<Uri>)
        if (index < list.size) {
            list.remove(list[index])
            _stationPicUriList.value = list
        } else {
            throw ArrayIndexOutOfBoundsException("list size = ${list.size}, and you index = $index")
        }
    }

    fun getStation(): ChargingPileStation {
        return ChargingPileStation(
            stationId,
            latitude,
            longitude,
            stationName,
            posDescription,
            parkFee.toFloat(),
            stationCollection,
            sharedPreferenceData.userId.toInt(),
            "",
            "",
            remark
        )
    }
}
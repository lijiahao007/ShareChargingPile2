package com.lijiahao.sharechargingpile2.ui.publishStationModule.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lijiahao.sharechargingpile2.data.ChargingPile
import com.lijiahao.sharechargingpile2.data.ChargingPileStation
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AddStationViewModel @Inject constructor(): ViewModel() {

    var latitude: Double = 0.0
    var longitude: Double = 0.0
    var posDescription: String = ""
    var pileList: ArrayList<ChargingPile> = ArrayList<ChargingPile>()
    var stationName:String = ""
    var chargeFee:Double = 0.0
    var parkFee:Double = 0.0
    var remark:String = ""

    private val _stationPicUriList:MutableLiveData<ArrayList<Uri>> = MutableLiveData<ArrayList<Uri>>(ArrayList<Uri>())
    val stationPicUriList: LiveData<ArrayList<Uri>> = _stationPicUriList

    fun addUri(uri: Uri) {
        val list = ArrayList<Uri>(_stationPicUriList.value as ArrayList<Uri>)
        list.add(uri)
        _stationPicUriList.value = list
    }

    fun removeUri(index:Int) {
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
            0,
            latitude,
            longitude,
            stationName,
            posDescription,
            parkFee.toFloat(),
            0,
            0,
            "",
            "",
            remark
        )
    }
}
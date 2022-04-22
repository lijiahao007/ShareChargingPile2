package com.lijiahao.sharechargingpile2.ui.mapModule.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.lijiahao.sharechargingpile2.data.Comment
import com.lijiahao.sharechargingpile2.network.response.UserInfoResponse
import com.lijiahao.sharechargingpile2.network.service.CommentService
import com.lijiahao.sharechargingpile2.network.service.UserService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class CommentViewModel @Inject constructor(
    commentService: CommentService,
    userService: UserService
): ViewModel() {

    private val _stationId = MutableLiveData<Int>()

    val commentList: LiveData<List<Comment>> = _stationId.switchMap { stationId ->
        liveData(Dispatchers.IO) {
            try {
                val list = commentService.queryCommentByStationId(stationId)
                emit(list)
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(TAG, "网络异常， 获取stationId=$stationId 评论失败")
            }
        }
    }

    val relateUserMap:LiveData<Map<Int, UserInfoResponse>> = commentList.switchMap { comments ->
        liveData(Dispatchers.IO){
            val resMap = HashMap<Int, UserInfoResponse>()
            comments.forEach {  comment ->
                val userId = comment.userId
                if (!resMap.containsKey(userId)) {
                    try {
                        val curUserResponse = userService.getUserInfo(userId.toString())
                        resMap[userId] = curUserResponse
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Log.e(TAG, "网络异常，获取userId=$userId 信息失败")
                    }
                }
            }
            emit(resMap)
        }
    }

    // 分数
    val score:LiveData<Double> = commentList.switchMap {
        if (it.isEmpty()) {
            MutableLiveData(0.0)
        } else {
            MutableLiveData(it.sumOf { comment ->
                comment.star.toDouble()
            }/it.size)
        }
    }

    fun setStationId(id: Int) {
        if (_stationId.value == null || _stationId.value != id) {
            _stationId.value = id
        }
    }


    companion object {
        const val TAG = "CommentViewModel"
    }
}
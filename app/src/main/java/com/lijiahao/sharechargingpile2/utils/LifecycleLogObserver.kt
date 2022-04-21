package com.lijiahao.sharechargingpile2.utils

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

class LifecycleLogObserver(val TAG: String) : DefaultLifecycleObserver {
    override fun onCreate(owner: LifecycleOwner) {
        Log.e(TAG, "onCreate $owner")
    }

    override fun onStart(owner: LifecycleOwner) {
        Log.e(TAG, "onStart $owner")
    }

    override fun onResume(owner: LifecycleOwner) {
        Log.e(TAG, "onResume $owner")
    }

    override fun onPause(owner: LifecycleOwner) {
        Log.e(TAG, "onPause $owner")
    }

    override fun onStop(owner: LifecycleOwner) {
        Log.e(TAG, "onStop $owner")
    }

    override fun onDestroy(owner: LifecycleOwner) {
        Log.e(TAG, "onDestroy $owner")
    }
}
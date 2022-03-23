package com.lijiahao.sharechargingpile2.utils

import android.app.Activity
import android.view.View
import android.view.inputmethod.InputMethodManager

class SoftKeyBoardUtils {

    companion object {
        @JvmStatic
        fun hideKeyBoard(activity: Activity) {
            val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            var view = activity.currentFocus
            if (view == null) {
                view = View(activity);
            }
            imm.hideSoftInputFromWindow(view.windowToken, 0);
        }
    }
}
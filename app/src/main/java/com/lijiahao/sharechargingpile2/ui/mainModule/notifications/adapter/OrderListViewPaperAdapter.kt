package com.lijiahao.sharechargingpile2.ui.mainModule.notifications.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.lijiahao.sharechargingpile2.ui.mainModule.notifications.MyOrderFragment
import com.lijiahao.sharechargingpile2.ui.mainModule.notifications.ServiceOrderFragment
import java.lang.IndexOutOfBoundsException

class OrderListViewPaperAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle): FragmentStateAdapter(fragmentManager, lifecycle) {

    private val tabFragmentMap = mapOf<Int, ()->Fragment>(
        MY_ORDER_FRAGMENT_INDEX to  {MyOrderFragment()},
        SERVICE_ORDER_FRAGMENT_INDEX to {ServiceOrderFragment()}
    )

    override fun getItemCount() = tabFragmentMap.size

    override fun createFragment(position: Int): Fragment {
        return tabFragmentMap[position]?.invoke() ?: throw  IndexOutOfBoundsException("ViewPaper中的Tab只有${itemCount}个， position=$position")
    }

    companion object {
        const val MY_ORDER_FRAGMENT_INDEX = 0
        const val SERVICE_ORDER_FRAGMENT_INDEX = 1
    }

}
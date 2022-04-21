package com.lijiahao.sharechargingpile2.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.lijiahao.sharechargingpile2.R
import com.lijiahao.sharechargingpile2.data.ChargingPileStation
import com.lijiahao.sharechargingpile2.data.Order
import com.lijiahao.sharechargingpile2.ui.mainModule.notifications.adapter.OrderAdapter

/**
 * 同一个按照Station分类的ExpandView
 */
class StationOrderExpandView(
    private val mContext: Context,
    attrs: AttributeSet,
): LinearLayout(mContext, attrs){



    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mHeading: MaterialCardView
    private lateinit var tvStationName: TextView
    private lateinit var ivExpand: ImageView
    private var isExpand = false
    private lateinit var adapter: OrderAdapter
    private var root:View = LayoutInflater.from(mContext).inflate(R.layout.view_station_order_expand, this, true)
    private var fragment: Fragment? = null

    init {
        initView()
    }

    private fun initView() {
        mRecyclerView = root.findViewById<RecyclerView>(R.id.recycler_view)
        mHeading = root.findViewById<MaterialCardView>(R.id.heading)
        tvStationName = root.findViewById<TextView>(R.id.tv_station_name)
        ivExpand = root.findViewById<ImageView>(R.id.iv_expand)

        // 1. 初始化展开和收缩的操作
        mHeading.setOnClickListener {
            if (isExpand) {
                ivExpand.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_caret_right, null))
                // TODO("加入隐藏动画")
                mRecyclerView.visibility = View.GONE
                isExpand = false
            } else {
                ivExpand.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_down, null))
                // TODO("加入显示动画")
                mRecyclerView.visibility = View.VISIBLE
                isExpand = true
            }
        }

        // 2. 设置RecyclerView的Adapter
        adapter = OrderAdapter()
        mRecyclerView.adapter = adapter
    }

    /**
     * @param station
     */
    fun setData(station:ChargingPileStation, pileIds: List<String>, processingOrders:List<Order>, finishOrders:List<Order>, fragment: Fragment?=null) {
        val pileStationMap: HashMap<String, String> = HashMap()
        pileIds.forEach { id ->
            pileStationMap[id] = station.id.toString()
        }
        val stationMap = HashMap<String, ChargingPileStation>();
        stationMap[station.id.toString()] = station
        adapter.setInitData(pileStationMap, stationMap, processingOrders, finishOrders, fragment)
        tvStationName.text = station.name
        
    }

}
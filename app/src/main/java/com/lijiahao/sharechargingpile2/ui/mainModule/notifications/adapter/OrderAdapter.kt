package com.lijiahao.sharechargingpile2.ui.mainModule.notifications.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.lijiahao.sharechargingpile2.R
import com.lijiahao.sharechargingpile2.data.ChargingPileStation
import com.lijiahao.sharechargingpile2.data.Order
import com.lijiahao.sharechargingpile2.databinding.ItemOrderDividerBinding
import com.lijiahao.sharechargingpile2.databinding.ItemOrderListBinding
import com.lijiahao.sharechargingpile2.ui.QRCodeModule.GenerateOrderViewModel
import com.lijiahao.sharechargingpile2.ui.mainModule.notifications.MyOrderFragment
import com.lijiahao.sharechargingpile2.ui.mainModule.notifications.OrderListFragment
import com.lijiahao.sharechargingpile2.ui.mainModule.notifications.OrderListFragmentDirections
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class OrderAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // 下面两个列表的开头都是分割线
    private val processingOrderList = ArrayList<Order>() // 0 ~ processingOrderList.size - 1
    private val finishOrderList =
        ArrayList<Order>() // processingOrderList.size ~ (processingOrderList.size+finishOrderList.size+1)
    private val relateStationInfoMap = HashMap<String, ChargingPileStation>(); // 相关充电站信息
    private val pileStationMap = HashMap<String, String>() // pileId -> stationId对应表
    private var fragment: Fragment? = null  // 注意：如果需要点击跳转，需要传入OrderListFragment的引用

    init {
        // 用id存放分割线的信息
        processingOrderList.add(
            Order(
                PROCESSING_DIVIDER_MESSAGE,
                "",
                "",
                "",
                "",
                "",
                0f,
                "",
                "",
                ""
            )
        )
        finishOrderList.add(Order(FINISH_DIVIDER_MESSAGE, "", "", "", "", "", 0f, "", "", ""))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == DIVIDER_TYPE) {
            DividerViewHolder(
                processingOrderList,
                finishOrderList,
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_order_divider,
                    parent,
                    false
                )
            )
        } else {
            OrderViewHolder(
                fragment,
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_order_list,
                    parent,
                    false
                )
            )
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0 || position == processingOrderList.size) {
            DIVIDER_TYPE
        } else {
            ORDER_TYPE
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is OrderViewHolder) {
            val order = getItem(position)
            val stationId = pileStationMap[order.pileId]
            val station = relateStationInfoMap[stationId]
            holder.bind(order, station)
        } else if (holder is DividerViewHolder) {
            holder.bind(getItem(position).id)
        }
    }

    fun getItem(position: Int): Order {
        val processSize = processingOrderList.size
        return if (position >= 0 && position <= processSize - 1) {
            processingOrderList[position]
        } else {
            finishOrderList[position - processSize]
        }
    }

    override fun getItemCount() = finishOrderList.size + processingOrderList.size


    // 这里有点击事件
    fun setInitData(
        pileStationMap: Map<String, String>,
        stationInfoMap: Map<String, ChargingPileStation>,
        processingOrder: List<Order>,
        finishOrder: List<Order>,
        fragment: Fragment? = null
    ) {
        addPileStationMap(pileStationMap)
        addRelateStationInfo(stationInfoMap)
        setProcessingOrder(processingOrder)
        setFinishOrder(finishOrder)
        fragment?.let {
            setFragment(fragment)
        }
        if (fragment == null) {
            Log.e(TAG, "OrderAdapter未设置Fragment， 着会导致Item点击事件失效")
        }
    }

    // TODO("优化一下，notifyDataSetChange可能会有性能问题")
    @SuppressLint("NotifyDataSetChanged")
    private fun setProcessingOrder(orders: List<Order>) {
        val divider = processingOrderList[0]
        processingOrderList.clear()
        processingOrderList.add(divider)
        processingOrderList.addAll(orders)
        notifyDataSetChanged()
    }


    // TODO("优化一下，notifyDataSetChange可能会有性能问题")
    @SuppressLint("NotifyDataSetChanged")
    private fun setFinishOrder(orders: List<Order>) {
        val divider = finishOrderList[0]
        finishOrderList.clear()
        finishOrderList.add(divider)
        finishOrderList.addAll(orders)
        notifyDataSetChanged()
    }

    private fun setFragment(fragment: Fragment) {
        this.fragment = fragment
        try {
            if (fragment !is OrderListFragment) {
                throw Exception("fragment错误，需要OrderListFragment, 但给的是$fragment， 后果：OrderList中列表项点击事件失效")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(
                MyOrderFragment.TAG,
                "fragment错误，需要OrderListFragment, 但给的是$fragment, 后果：OrderList中列表项点击事件失效"
            )
        }

    }

    private fun addRelateStationInfo(infoMap: Map<String, ChargingPileStation>) {
        relateStationInfoMap.putAll(infoMap)
        notifyItemRangeChanged(0, itemCount - 1)
    }

    private fun addPileStationMap(map: Map<String, String>) {
        pileStationMap.putAll(map)
        notifyItemRangeChanged(0, itemCount - 1)
    }

    class OrderViewHolder(
        val fragment: Fragment?,
        val binding: ItemOrderListBinding,
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(order: Order, station: ChargingPileStation?) {
            station?.let {
                // 1. 设置基本信息
                binding.tvStationName.text = station.name
                binding.tvState.text = order.state
                binding.tvChargingPileId.text = order.pileId
                binding.tvStationPosition.text = station.posDescription
                val createTime = LocalDateTime.parse(order.createTime)
                binding.tvCreateTime.text =
                    createTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                if (order.state != Order.STATE_FINISH) {
                    binding.priceLayout.visibility = View.INVISIBLE
                }
                binding.tvPrice.text = order.price.toString()

                // 2. 设置点击事件
                if (fragment != null && fragment is OrderListFragment) {
                    binding.orderCardLayout.setOnClickListener {
                        val stationId = station.id.toString()
                        val pileId = order.pileId

                        // 2.1 设置ViewModel中用到的变量
                        val generateOrderViewModel = ViewModelProvider(fragment.requireActivity(), GenerateOrderViewModel.getGenerateOrderViewModelFactory(
                            stationId,
                            pileId,
                            fragment.chargingPileStationService,
                            fragment.userService
                        ))[GenerateOrderViewModel::class.java]
                        generateOrderViewModel.stationId = stationId
                        generateOrderViewModel.pileId = pileId
                        generateOrderViewModel.setOrder(order)
                        when (order.state) {
                            // 2.2 使用中订单跳转PileUsingFragment
                            Order.STATE_USING -> {
                                val action =
                                    OrderListFragmentDirections.actionOrderListFragmentToPileUsingFragment(
                                        stationId,
                                        pileId
                                    )
                                fragment.findNavController().navigate(action)
                            }
                            // 2.3 未支付订单跳转 OrderPayFragment
                            Order.STATE_UNPAID -> {
                                val action =
                                    OrderListFragmentDirections.actionOrderListFragmentToOrderPayFragment(
                                        stationId,
                                        pileId
                                    )
                                fragment.findNavController().navigate(action)
                            }
                            // 2.4 跳转订单详情
                            Order.STATE_FINISH -> {
                                val action = OrderListFragmentDirections.actionOrderListFragmentToOrderDetailFragment(
                                    stationId,
                                    pileId
                                )
                                fragment.findNavController().navigate(action)
                            }
                        }
                    }

                }
            }
        }
    }

    class DividerViewHolder(
        private val processingOrder: List<Order>,
        private val finishOrder: List<Order>,
        val binding: ItemOrderDividerBinding
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: String) {
            binding.dividerMessage.text = message
            when (message) {
                PROCESSING_DIVIDER_MESSAGE -> {
                    if (processingOrder.size == 1) {
                        binding.dividerLayout.visibility = View.GONE
                    } else {
                        binding.dividerLayout.visibility = View.VISIBLE
                    }
                }

                FINISH_DIVIDER_MESSAGE -> {
                    if (finishOrder.size == 1) {
                        binding.dividerLayout.visibility = View.GONE
                    } else {
                        binding.dividerLayout.visibility = View.VISIBLE
                    }
                }
            }
        }
    }


    companion object {
        const val DIVIDER_TYPE = 1
        const val ORDER_TYPE = 2
        const val PROCESSING_DIVIDER_MESSAGE = "进行中订单"
        const val FINISH_DIVIDER_MESSAGE = "历史订单"
        const val TAG = "OrderAdapter"
    }


}
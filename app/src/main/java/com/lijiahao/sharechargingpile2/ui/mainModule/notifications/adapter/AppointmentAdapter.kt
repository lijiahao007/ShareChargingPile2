package com.lijiahao.sharechargingpile2.ui.mainModule.notifications.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.lijiahao.sharechargingpile2.R
import com.lijiahao.sharechargingpile2.data.Appointment
import com.lijiahao.sharechargingpile2.data.AppointmentInfo
import com.lijiahao.sharechargingpile2.databinding.ItemAppointmentListBinding
import com.lijiahao.sharechargingpile2.ui.mainModule.notifications.OrderListFragment
import com.lijiahao.sharechargingpile2.ui.mainModule.notifications.OrderListFragmentDirections
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class AppointmentAdapter(val fragment: OrderListFragment) : RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder>() {

    private val list = ArrayList<AppointmentInfo>()

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newList: List<AppointmentInfo>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        return AppointmentViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_appointment_list,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        val info = getItem(position)
        holder.bind(info)
        holder.binding.layoutAppointmentItem.setOnClickListener {
            val action = OrderListFragmentDirections.actionOrderListFragmentToBookPileFragment2(info.station.id)
            fragment.findNavController().navigate(action)
        }
    }

    fun getItem(position: Int): AppointmentInfo = list[position]

    override fun getItemCount() = list.size

    class AppointmentViewHolder(val binding: ItemAppointmentListBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(info: AppointmentInfo) {
            binding.tvStationName.text = info.station.name
            val beginDateTime = LocalDateTime.parse(info.appointment.beginDateTime)
            val endDateTime = LocalDateTime.parse(info.appointment.endDateTime)
            val date = beginDateTime.toLocalDate()
            val beginTime = beginDateTime.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"))
            val endTime = endDateTime.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"))
            binding.tvDate.text = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            binding.tvTime.text = "$beginTime~$endTime"
            binding.tvState.text = info.appointment.state
        }
    }
}
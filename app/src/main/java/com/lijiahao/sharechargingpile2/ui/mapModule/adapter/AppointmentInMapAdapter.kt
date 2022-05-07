package com.lijiahao.sharechargingpile2.ui.mapModule.adapter

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.lijiahao.sharechargingpile2.R
import com.lijiahao.sharechargingpile2.data.Appointment
import com.lijiahao.sharechargingpile2.databinding.ItemAppointmentInBookPileBinding
import java.time.format.DateTimeFormatter
import android.animation.AnimatorSet
import com.lijiahao.sharechargingpile2.ui.mapModule.BookPileFragment

class AppointmentInMapAdapter(val fragment: BookPileFragment) : RecyclerView.Adapter<AppointmentInMapAdapter.AppointmentViewHolder>() {

    private val list = ArrayList<Appointment>()

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newList: List<Appointment>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        return AppointmentViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_appointment_in_book_pile,
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        holder.bind(list[position])
        val cardLayout = holder.binding.cardLayout
        cardLayout.setOnClickListener {
            val xAnimator = ObjectAnimator.ofFloat(cardLayout, "scaleX", 1f, 0.9f, 1f)
            val yAnimator = ObjectAnimator.ofFloat(cardLayout, "scaleY", 1f, 0.9f, 1f)
            val animSet = AnimatorSet()
            animSet.play(xAnimator).with(yAnimator)
            animSet.duration = 100
            animSet.start()
            fragment.showDeletePopUpWindow(list[position], cardLayout)
        }
    }

    override fun getItemCount() = list.size

    class AppointmentViewHolder(val binding: ItemAppointmentInBookPileBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(appointment: Appointment) {
            binding.tvDate.text =
                appointment.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
            val time = "${appointment.getBeginTime().format(timeFormatter)}~${appointment.getEndTime().format(timeFormatter)}"
            binding.tvTime.text = time
            binding.tvState.text = appointment.state
        }

    }
}
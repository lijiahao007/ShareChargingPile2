package com.lijiahao.sharechargingpile2.ui.publishStationModule.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import androidx.annotation.MainThread
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.lijiahao.sharechargingpile2.R
import com.lijiahao.sharechargingpile2.data.ElectricChargePeriod
import com.lijiahao.sharechargingpile2.databinding.ItemAddLayoutBinding
import com.lijiahao.sharechargingpile2.databinding.ItemElectricPeriodChargeBinding
import com.lijiahao.sharechargingpile2.exception.EndTimeBeforeBeginTimeException
import java.time.LocalTime
import java.time.format.DateTimeParseException

// 该Adapter中设置的ElectricChargePeriod中id与StationId都是0，如果需要使用
class ElectricPeriodChargeAdapter(
    val fragment: Fragment
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val list: ArrayList<ElectricChargePeriod> = ArrayList()

    init {
        list.add(ElectricChargePeriod(0, "", "", 0, 0f)) // AddButton
    }

    @MainThread
    fun addElectricChargePeriod(electricChargePeriod: ElectricChargePeriod) {
        try {
            val beginTime = LocalTime.parse(electricChargePeriod.beginTime)
            val endTime = LocalTime.parse(electricChargePeriod.endTime)
            if (!beginTime.isBefore(endTime)) {
                throw EndTimeBeforeBeginTimeException()
            }
            val index = list.size - 1
            list.add(index, electricChargePeriod)
            notifyItemInserted(index)
        } catch (e: DateTimeParseException) {
            e.printStackTrace()
            fragment.view?.let { Snackbar.make(it, "日期格式错误", Snackbar.LENGTH_SHORT).show() }
        } catch (e: EndTimeBeforeBeginTimeException) {
            e.printStackTrace()
            fragment.view?.let { Snackbar.make(it, "结束时间早于开始时间", Snackbar.LENGTH_SHORT).show() }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @MainThread
    fun setList(newList: List<ElectricChargePeriod>) {
        list.clear()
        list.addAll(newList)
        list.add(ElectricChargePeriod(0, "", "", 0, 0f)) // AddButton
        notifyDataSetChanged()
    }


    fun currentList(): List<ElectricChargePeriod> {
        return list.subList(0, list.lastIndex)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {

        if (viewType == ELECTRIC_CHARGE_TYPE) {
            return ElectricPeriodChargeViewHolder(
                fragment, this,
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_electric_period_charge,
                    parent,
                    false
                )
            )
        } else {
            return AddLayoutViewHolder(
                fragment,
                this,
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_add_layout,
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is AddLayoutViewHolder) {
            holder.bind()
        } else if (holder is ElectricPeriodChargeViewHolder) {
            holder.bind(list[position])
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == list.lastIndex) {
            ADD_LAYOUT_TYPE
        } else {
            ELECTRIC_CHARGE_TYPE
        }
    }

    class ElectricPeriodChargeViewHolder(
        val fragment: Fragment,
        val adapter: ElectricPeriodChargeAdapter,
        val binding: ItemElectricPeriodChargeBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(electricChargePeriod: ElectricChargePeriod) {
            binding.ivEdit.setOnClickListener {
                timePicker(electricChargePeriod)
            }
            binding.ivDelete.setOnClickListener {
                val index = adapter.list.indexOf(electricChargePeriod)
                if (index != -1) {
                    adapter.list.removeAt(index)
                    adapter.notifyItemRemoved(index)
                }
            }
            binding.tvBeginTime.text = electricChargePeriod.beginTime
            binding.tvEndTime.text = electricChargePeriod.endTime
            binding.tvElectricCharge.text = electricChargePeriod.electricCharge.toString()
        }

        private fun timePicker(electricChargePeriod: ElectricChargePeriod) {
            val pickBeginTime = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(0)
                .setMinute(0)
                .setTitleText("选择开始时间")
                .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
                .build()
            pickBeginTime.show(fragment.parentFragmentManager, "beginTimePicker")
            pickBeginTime.addOnPositiveButtonClickListener {
                val pickEndTime = MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_24H)
                    .setHour(0)
                    .setMinute(0)
                    .setTitleText("选择结束时间")
                    .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
                    .build()
                pickEndTime.show(fragment.parentFragmentManager, "endTimePicker")
                pickEndTime.addOnPositiveButtonClickListener {
                    val beginTime = String.format("%02d", pickBeginTime.hour) + ":" + String.format(
                        "%02d",
                        pickBeginTime.minute
                    )
                    val endTime = String.format("%02d", pickEndTime.hour) + ":" + String.format(
                        "%02d",
                        pickEndTime.minute
                    )

                    fragment.context?.let { context ->
                        val childView = LayoutInflater.from(context)
                            .inflate(R.layout.dialog_set_electric_charge, null)
                        MaterialAlertDialogBuilder(context)
                            .setTitle("设置电费")
                            .setView(childView)
                            .setPositiveButton("确定") { _, _ ->
                                var electricChargeStr = ""
                                try {
                                    val editView =
                                        childView.findViewById<EditText>(R.id.iv_dialog_electric_charge)
                                    electricChargeStr = editView.text.toString()
                                    val electricCharge = electricChargeStr.toFloat()
                                    binding.tvElectricCharge.text = electricChargeStr
                                    binding.tvBeginTime.text = beginTime
                                    binding.tvEndTime.text = endTime
                                    electricChargePeriod.beginTime = beginTime
                                    electricChargePeriod.endTime = endTime
                                    electricChargePeriod.electricCharge = electricCharge
                                } catch (e: NumberFormatException) {
                                    e.printStackTrace()
                                    Snackbar.make(
                                        binding.root,
                                        "电费：$electricChargeStr 格式错误",
                                        Snackbar.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            .setNegativeButton("取消") { dialog, _ -> dialog.cancel() }
                            .show()
                    }
                }
            }
        }
    }


    class AddLayoutViewHolder(
        val fragment: Fragment,
        val adapter: ElectricPeriodChargeAdapter,
        val binding: ItemAddLayoutBinding,
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.btnAdd.setOnClickListener {
                timePicker()
            }
        }

        private fun timePicker() {
            val pickBeginTime = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(0)
                .setMinute(0)
                .setTitleText("选择开始时间")
                .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
                .build()
            pickBeginTime.show(fragment.parentFragmentManager, "beginTimePicker")
            pickBeginTime.addOnPositiveButtonClickListener {
                val pickEndTime = MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_24H)
                    .setHour(0)
                    .setMinute(0)
                    .setTitleText("选择结束时间")
                    .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
                    .build()
                pickEndTime.show(fragment.parentFragmentManager, "endTimePicker")
                pickEndTime.addOnPositiveButtonClickListener {
                    val beginTime = String.format("%02d", pickBeginTime.hour) + ":" + String.format(
                        "%02d",
                        pickBeginTime.minute
                    )
                    val endTime = String.format("%02d", pickEndTime.hour) + ":" + String.format(
                        "%02d",
                        pickEndTime.minute
                    )

                    fragment.context?.let { context ->
                        val childView = LayoutInflater.from(context)
                            .inflate(R.layout.dialog_set_electric_charge, null)
                        MaterialAlertDialogBuilder(context)
                            .setTitle("设置电费")
                            .setView(childView)
                            .setPositiveButton("确定") { _, _ ->
                                var electricChargeStr = ""
                                try {
                                    val editView =
                                        childView.findViewById<EditText>(R.id.iv_dialog_electric_charge)
                                    electricChargeStr = editView.text.toString()
                                    val electricCharge = electricChargeStr.toFloat()
                                    val electricChargePeriod = ElectricChargePeriod(
                                        0,
                                        beginTime,
                                        endTime,
                                        0,
                                        electricCharge
                                    )
                                    adapter.addElectricChargePeriod(electricChargePeriod)
                                } catch (e: NumberFormatException) {
                                    e.printStackTrace()
                                    Snackbar.make(
                                        binding.root,
                                        "电费：$electricChargeStr 格式错误",
                                        Snackbar.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            .setNegativeButton("取消") { dialog, _ -> dialog.cancel() }
                            .show()
                    }
                }
            }
        }


    }


    companion object {
        const val ADD_LAYOUT_TYPE = 1
        const val ELECTRIC_CHARGE_TYPE = 2
    }
}
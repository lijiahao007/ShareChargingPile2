package com.lijiahao.sharechargingpile2.ui.publishStationModule

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.lijiahao.sharechargingpile2.R
import com.lijiahao.sharechargingpile2.data.ElectricChargePeriod
import com.lijiahao.sharechargingpile2.databinding.FragmentSetElectricPeriodChargeBinding
import com.lijiahao.sharechargingpile2.exception.TimeNotCoverAllDayException
import com.lijiahao.sharechargingpile2.ui.publishStationModule.adapter.ElectricPeriodChargeAdapter
import com.lijiahao.sharechargingpile2.ui.publishStationModule.viewmodel.AddStationViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.sql.Time
import java.time.LocalTime

class SetElectricPeriodChargeFragment : Fragment() {

    private val binding: FragmentSetElectricPeriodChargeBinding by lazy {
        FragmentSetElectricPeriodChargeBinding.inflate(layoutInflater)
    }

    private val viewModel: AddStationViewModel by activityViewModels()

    private lateinit var adapter: ElectricPeriodChargeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        initUI()

        return binding.root
    }

    private fun initUI() {
        adapter = ElectricPeriodChargeAdapter(this)
        binding.recyclerView.adapter = adapter

        if (viewModel.electricPeriodChargeList.isNotEmpty()) {
            adapter.setList(viewModel.electricPeriodChargeList)
        }

        binding.confirm.setOnClickListener {
            // 验证是否正确
            val list = ArrayList(adapter.currentList())
            // 按照时间顺序排列
            list.sortWith(Comparator<ElectricChargePeriod> { o1, o2 ->
                if (o1 != null && o2 != null) {
                    val o1BeginTime = LocalTime.parse(o1.beginTime)
                    val o2BeginTime = LocalTime.parse(o2.beginTime)
                    if (o1BeginTime.isBefore(o2BeginTime)) {
                        return@Comparator -1;
                    } else if (o1BeginTime.equals(o2BeginTime)) {
                        return@Comparator 0;
                    } else return@Comparator 1;
                } else if (o1 == null && o2 == null) {
                    return@Comparator 0;
                } else if (o1 == null){
                    return@Comparator -1;
                } else {
                    return@Comparator 1;
                }
            })

            // 判断时间是否连续
            try {
                for (i in 0 until list.size) {
                    if (i == 0) {
                        if (list[0].beginTime != "00:00") {
                            throw TimeNotCoverAllDayException("开始时间${list[0].beginTime} != 00:00")
                        }
                    } else if (i == list.size-1) {
                        if (list[i].endTime != "23:59") {
                            throw TimeNotCoverAllDayException("结束时间${list[0].endTime} != 23:59")
                        }
                    } else {
                        if (list[i].beginTime != list[i-1].endTime) {
                            throw TimeNotCoverAllDayException("第${i-1}结束时间${list[i-1].endTime} != 第${i}开始时间${list[i].beginTime}")
                        }
                        if (list[i].endTime != list[i+1].beginTime) {
                            throw TimeNotCoverAllDayException("第${i}结束时间${list[i].endTime} != 第${i+1}开始时间${list[i+1].beginTime}")
                        }
                    }
                }

                // 如果没有问题，就进行保存
                viewModel.electricPeriodChargeList = list
                Toast.makeText(context, "保存成功", Toast.LENGTH_SHORT).show()
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                    delay(1000)
                    findNavController().navigateUp()
                }
            } catch (e: TimeNotCoverAllDayException) {
                e.printStackTrace()
                Snackbar.make(binding.root, e.message?:"时间按顺序填写", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

}
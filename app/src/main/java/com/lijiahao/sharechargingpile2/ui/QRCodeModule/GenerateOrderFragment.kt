package com.lijiahao.sharechargingpile2.ui.QRCodeModule

import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.lijiahao.sharechargingpile2.R
import com.lijiahao.sharechargingpile2.data.OpenTime
import com.lijiahao.sharechargingpile2.data.SharedPreferenceData
import com.lijiahao.sharechargingpile2.databinding.FragmentGenerateOrderBinding
import com.lijiahao.sharechargingpile2.network.service.ChargingPileStationService
import com.lijiahao.sharechargingpile2.network.service.OrderService
import com.lijiahao.sharechargingpile2.network.service.UserService
import com.lijiahao.sharechargingpile2.ui.view.OpenTimeWithChargeFeeLayout
import com.lijiahao.sharechargingpile2.utils.TimeUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class GenerateOrderFragment : Fragment() {

    private val binding: FragmentGenerateOrderBinding by lazy {
        FragmentGenerateOrderBinding.inflate(layoutInflater)
    }

    private val args: GenerateOrderFragmentArgs by navArgs()

    private val stationId: String by lazy { args.stationId }
    private val pileId: String by lazy { args.pileId }

    @Inject
    lateinit var chargingPileStationService: ChargingPileStationService
    @Inject
    lateinit var userService: UserService
    @Inject
    lateinit var orderService: OrderService
    @Inject
    lateinit var sharedPreferenceData: SharedPreferenceData

    private val viewModel: GenerateOrderViewModel by activityViewModels() {
        GenerateOrderViewModel.getGenerateOrderViewModelFactory(
            stationId,
            pileId,
            chargingPileStationService,
            userService
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        initUI()
        return binding.root
    }

    private fun initUI() {
        loadData()

        binding.btnBeginUse.setOnClickListener {
            // 先请求看能不能生成订单
            // TODO: 判断是否在营业时间中
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                val userId = sharedPreferenceData.userId
                try {
                    val generateOrderResponse = orderService.generateOrder(pileId, userId)
                    withContext(Dispatchers.Main) {
                        when (generateOrderResponse.code) {
                            "success" -> {
                                viewModel.setOrder(generateOrderResponse.order)
                                val action =
                                    GenerateOrderFragmentDirections.actionGenerateOrderFragmentToPileUsingFragment(
                                        stationId,
                                        pileId
                                    )
                                findNavController().navigate(action)
                            }
                            "suspend" -> {
                                Snackbar.make(binding.root, "当前充电桩未营业", Snackbar.LENGTH_SHORT)
                                    .show()
                            }
                            "using" -> {
                                Snackbar.make(binding.root, "当前充电桩使用中", Snackbar.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        Snackbar.make(binding.root, "网络异常", Snackbar.LENGTH_SHORT).show()
                    }
                }

            }
        }
    }

    private fun loadData() {
        viewModel.stationInfo.observe(viewLifecycleOwner) { stationInfo ->
            binding.itStationName.text = stationInfo.station.name
            binding.tvStationParkFee.text = stationInfo.station.parkFee.toString()
            setOpenTime(stationInfo.openTimeList)
        }

        viewModel.pileInfo.observe(viewLifecycleOwner) { pile ->
            binding.tvPileState.text = pile.state
            binding.tvElectricType.text = pile.electricType
            binding.tvPowerRate.text = pile.powerRate.toString()
        }

        viewModel.userInfo.observe(viewLifecycleOwner) { userInfo ->
            binding.tvUserName.text = userInfo.name
            binding.tvUserPhone.text = userInfo.phone
        }
    }

    private fun setOpenTime(openTimes: List<OpenTime>) {
        val linearLayout = binding.openTimeLinearLayout
        linearLayout.removeAllViews()
        context?.let {
            openTimes.forEach { openTime ->
                val content = OpenTimeWithChargeFeeLayout(it)
                content.setOpenTime(openTime)
                linearLayout.addView(content)
            }
        }

    }


}
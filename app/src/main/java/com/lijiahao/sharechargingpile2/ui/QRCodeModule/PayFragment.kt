package com.lijiahao.sharechargingpile2.ui.QRCodeModule

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.lijiahao.sharechargingpile2.R
import com.lijiahao.sharechargingpile2.databinding.FragmentPayBinding
import com.lijiahao.sharechargingpile2.network.service.OrderService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@AndroidEntryPoint
class PayFragment : Fragment() {

    private val binding : FragmentPayBinding by lazy {
        FragmentPayBinding.inflate(layoutInflater)
    }

    private val args: PayFragmentArgs by navArgs()

    @Inject lateinit var  orderService: OrderService

    private val price: Float by lazy {
        args.price
    }
    private val orderId:String by lazy {
        args.orderId
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        initUI()


        return binding.root
    }

    private fun initUI() {
        // 1. 设置RadioButton
        binding.btnPay.text = "微信支付 $price 元"
        binding.rbAlipay.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.rbWechatPay.isChecked = false
                binding.btnPay.text = "支付宝支付 $price 元"
            }
        }

        binding.rbWechatPay.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.rbAlipay.isChecked = false
                binding.btnPay.text = "微信支付 $price 元"
            }
        }

        binding.tvPrice.text = price.toString()

        binding.btnPay.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val res = orderService.payOrder(orderId)
                    withContext(Dispatchers.Main) {
                        if (res == "success") {
                            Toast.makeText(context, "支付完成", Toast.LENGTH_SHORT).show()
                            delay(1000)
                            requireActivity().finish()
                        } else {
                            Toast.makeText(context, "支付失败", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, "网络异常，支付失败", Toast.LENGTH_SHORT).show()
                }
            }

        }
    }

}
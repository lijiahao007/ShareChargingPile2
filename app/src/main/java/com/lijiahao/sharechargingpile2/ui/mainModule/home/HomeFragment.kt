package com.lijiahao.sharechargingpile2.ui.mainModule.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.lijiahao.sharechargingpile2.databinding.FragmentHomeBinding
import com.lijiahao.sharechargingpile2.ui.QRCodeModule.QRCodeActivity
import com.lijiahao.sharechargingpile2.ui.mapModule.MapModuleActivity
import com.lijiahao.sharechargingpile2.ui.publishStationModule.PublishStationActivity
import java.time.Duration
import java.time.LocalDateTime

class HomeFragment : Fragment() {


    private lateinit var homeViewModel: HomeViewModel
    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var exitFlag = false
    private var lastClickTime:LocalDateTime = LocalDateTime.now()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

//        requireActivity().window.apply {
//            statusBarColor = Color.TRANSPARENT
//            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//        }

        homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.cardPublishChargingStation.setOnClickListener {
            val intent = Intent(context, PublishStationActivity::class.java)
            startActivity(intent)
        }

        binding.cardSearchPublish.setOnClickListener {
            val intent = Intent(context, MapModuleActivity::class.java)
            startActivity(intent)
        }

        binding.cardScanInfo.setOnClickListener {
            val intent = Intent(context, QRCodeActivity::class.java)
            startActivity(intent)
        }

        setNavigateUpBehavior()
        return root
    }


    private fun setNavigateUpBehavior() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            val now = LocalDateTime.now()
            val millis = Duration.between(lastClickTime, now).toMillis()
            if (exitFlag && millis <= 2000) {
                exitFlag = false
                requireActivity().finishAffinity()
                requireActivity().finish()
                android.os.Process.killProcess(android.os.Process.myPid())
            } else {
                exitFlag = true
                lastClickTime = now
                Toast.makeText(context, "再次返回退出应用", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
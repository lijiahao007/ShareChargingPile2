package com.lijiahao.sharechargingpile2.ui.publishStationModule

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.WindowCompat
import androidx.databinding.DataBindingUtil
import com.lijiahao.sharechargingpile2.R
import com.lijiahao.sharechargingpile2.databinding.ActivityPublishStationBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PublishStationActivity : AppCompatActivity() {

    lateinit var binding:ActivityPublishStationBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView<ActivityPublishStationBinding>(this, R.layout.activity_publish_station)
    }
}
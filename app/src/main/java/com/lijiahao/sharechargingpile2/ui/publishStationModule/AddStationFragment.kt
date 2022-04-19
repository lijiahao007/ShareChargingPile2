package com.lijiahao.sharechargingpile2.ui.publishStationModule

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.launch
import androidx.core.view.children
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.MaterialTimePicker.INPUT_MODE_CLOCK
import com.google.android.material.timepicker.TimeFormat
import com.lijiahao.sharechargingpile2.R
import com.lijiahao.sharechargingpile2.data.SharedPreferenceData
import com.lijiahao.sharechargingpile2.databinding.FragmentAddStationBinding
import com.lijiahao.sharechargingpile2.repository.ChargingPileStationRepository
import com.lijiahao.sharechargingpile2.network.request.StationInfoRequest
import com.lijiahao.sharechargingpile2.network.service.ChargingPileStationService
import com.lijiahao.sharechargingpile2.ui.publishStationModule.viewmodel.AddStationViewModel
import com.lijiahao.sharechargingpile2.utils.FileUtils
import com.lijiahao.sharechargingpile2.utils.FileUtils.Companion.getLocalPicsFromUris
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.lang.NumberFormatException
import javax.inject.Inject

@AndroidEntryPoint
class AddStationFragment : Fragment() {

    val binding: FragmentAddStationBinding by lazy {
        FragmentAddStationBinding.inflate(layoutInflater)
    }

    val viewModel: AddStationViewModel by activityViewModels() // 这个VieModel用于获取LocationMapFragment与AddPileFragment中获取的信息

    @Inject
    lateinit var chargingPileStationService: ChargingPileStationService

    @Inject
    lateinit var chargingPileStationRepository: ChargingPileStationRepository

    @Inject
    lateinit var sharedPreferenceData: SharedPreferenceData

    private lateinit var albumLauncher: ActivityResultLauncher<Unit>

    // 图片相关的View
    private val imgList: Array<ImageView> by lazy {
        arrayOf(
            binding.imStationPic1,
            binding.stationPic2,
            binding.stationPic3,
            binding.stationPic4,
            binding.stationPic5
        )
    }
    private val cardList: Array<MaterialCardView> by lazy {
        arrayOf(
            binding.cardStationPic1,
            binding.cardStationPic2,
            binding.cardStationPic3,
            binding.cardStationPic4,
            binding.cardStationPic5
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // 带有类型安全的Activity结果返回。（即能够限制返回类型）
        albumLauncher = registerForActivityResult(
            object : ActivityResultContract<Unit, Uri?>() {
                override fun createIntent(context: Context, input: Unit): Intent {
                    // 创建需要跳转Activity的Intent
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                    intent.type = "image/*"
                    return intent
                }

                override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
                    // 在从跳转Activity返回后，从intent中获取结果
                    if (resultCode == Activity.RESULT_OK) {
                        intent?.data?.let { uri ->
                            Log.i(TAG, "uri=$uri \n encodePath=${uri.encodedPath}")
                            return uri
                        }
                    }
                    return null
                }
            }
        ) {


            // 通过parseResult中获取的结果后，执行具体的处理逻辑
            it?.let {
                viewModel.addLocalUri(it)
                Log.i(TAG, "uriList size = ${viewModel.stationPicUriList.value?.size}")
            }
        }

        // 注意上面的registerForActivityResult必须在super.onCreate()上方
        super.onCreate(savedInstanceState)
        viewModel.clear()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        initUI()
        return binding.root
    }

    private fun initUI() {
        initUIListener()
        initTimePick()
        initSubmit()
    }

    private fun initUIListener() {
        binding.ivToChoosePosition.setOnClickListener {
            navigateToLocationMapFragment()
        }

        // 从LocationMapFragment中获取位置
        setFragmentResultListener(LOCATION_MAP_TO_ADD_STATION_BUNDLE) { _, bundle ->
            val successGetDescription = bundle.getBoolean("isDescription")
            if (successGetDescription) {
                binding.tvPosition.text = viewModel.posDescription
            } else {
                Snackbar.make(binding.root, "位置获取失败，请重新获取", Snackbar.LENGTH_SHORT).show()
            }
        }


        binding.itStationName.addTextChangedListener {
            it?.let {
                viewModel.stationName = binding.itStationName.text.toString()
            }

        }


        binding.etParkFee.addTextChangedListener {
            it?.let {

                try {
                    viewModel.parkFee = binding.etParkFee.text.toString().toDouble()
                } catch (e: NumberFormatException) {
                    e.printStackTrace()
                    Snackbar.make(
                        binding.root,
                        "停车费用格式错误 ${binding.etParkFee.text.toString()}",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        }

        binding.ivToAddPile.setOnClickListener {
            navigateToAddPileFragment()
        }

        // 获取充电桩添加情况
        setFragmentResultListener(ADD_PILE_TO_ADD_STATION_BUNDLE) { _, _ ->
            val dcNum = viewModel.pileList.count { it.electricType == "直流" }
            val acNum = viewModel.pileList.size - dcNum
            binding.dcNum.text = dcNum.toString()
            binding.acNum.text = acNum.toString()
        }

        binding.cardAddPic.setOnClickListener {
            // 打开相册
            viewModel.stationPicUriList.value?.let {
                if (it.size < 5) {
                    albumLauncher.launch()
                } else {
                    Snackbar.make(binding.root, "最多上传5张图片, 长按对应图片删除", Snackbar.LENGTH_SHORT).show()
                }
            }

        }

        binding.remark.editText?.addTextChangedListener {
            it?.let {
                viewModel.remark = binding.remark.editText!!.text.toString()
            }
        }



        viewModel.stationPicUriList.observe(this) { uriList ->
            // 根据uri将图片显示在imageView中
            Log.i(TAG, " observe $uriList")
            for (i in 0 until uriList.size) {
                val bitmap =
                    requireActivity().contentResolver.openFileDescriptor(uriList[i], "r")?.use {
                        BitmapFactory.decodeFileDescriptor(it.fileDescriptor)
                    }
                imgList[i].setImageBitmap(bitmap)
                cardList[i].visibility = View.VISIBLE
            }
            for (i in uriList.size until 5) {
                cardList[i].visibility = View.GONE
                imgList[i].setImageBitmap(null)
            }
        }
        cardList.forEachIndexed { index, card ->
            card.setOnLongClickListener {
                viewModel.removeLocalUri(index)
                true
            }
        }

        binding.ivToSetElectricCharge.setOnClickListener {
            val action = AddStationFragmentDirections.actionAddStationFragmentToSetElectricPeriodChargeFragment()
            findNavController().navigate(action)

        }
    }

    private fun verify(stationInfoRequest: StationInfoRequest): Boolean {

        // 1. 校验开放日期
        if (stationInfoRequest.openDayInWeek.size <= 0) {
            Snackbar.make(binding.root, "请选择开放日期", Snackbar.LENGTH_SHORT).show()
            return false
        }

        // 2. 校验开放时间
        if (stationInfoRequest.openTime.size <= 0) {
            Snackbar.make(binding.root, "请选择开放时间", Snackbar.LENGTH_SHORT).show()
            return false
        }

        // 3. 校验充电站数据
        val station = stationInfoRequest.station
        // 3.1 位置信息
        if (station.latitude == 0.0 || station.longitude == 0.0 || station.posDescription == "") {
            Snackbar.make(binding.root, "位置未选取", Snackbar.LENGTH_SHORT).show()
            return false
        }
        // 3.2 充电名字
        if (station.name == "") {
            Snackbar.make(binding.root, "充电站名字为选取", Snackbar.LENGTH_SHORT).show()
            return false
        }
        // 3.3 停车费用
        if (station.parkFee < 0) {
            Snackbar.make(binding.root, "停车未用未填写", Snackbar.LENGTH_SHORT).show()
            return false
        }

        // 3.4 充电桩数量
        if (stationInfoRequest.chargingPiles.isEmpty()) {
            Snackbar.make(binding.root, "请添加至少一个充电桩", Snackbar.LENGTH_SHORT).show()
            return false
        }

        // 3.5 各时间段电费
        if (stationInfoRequest.electricChargePeriods.isEmpty()) {
            return false
        }
        return true
    }

    private fun initSubmit() {
        binding.btnSubmit.setOnClickListener {
            // 保存日期
            val dayList = ArrayList<String>()
            binding.chipGroupDayPick.checkedChipIds.forEach { chipId ->
                when (chipId) {
                    R.id.chip_Mon -> {
                        dayList.add("周一")
                    }
                    R.id.chip_Tue -> {
                        dayList.add("周二")
                    }
                    R.id.chip_Wen -> {
                        dayList.add("周三")
                    }
                    R.id.chip_Thu -> {
                        dayList.add("周四")
                    }
                    R.id.chip_Fri -> {
                        dayList.add("周五")
                    }
                    R.id.chip_Sat -> {
                        dayList.add("周六")
                    }
                    R.id.chip_Sun -> {
                        dayList.add("周日")
                    }
                }
            }


            // 保存时间
            val timeList = ArrayList<String>()
            binding.chipGroupTime.children.forEach { view ->
                val timeText = (view as Chip).text.toString()
                timeList.add(timeText)
            }
            Log.i(TAG, "$dayList\n$timeList")

            val stationInfoRequest = StationInfoRequest(
                dayList,
                timeList,
                viewModel.getStation(),
                viewModel.pileList,
                viewModel.electricPeriodChargeList,
                sharedPreferenceData.userId
            )

            if (!verify(stationInfoRequest)) return@setOnClickListener

            // 这里有一个巨坑！！！！！ fileList在IO线程中的数据不会更新到主线程中，因为两者是同步的，IO线程还需要
            lifecycleScope.launch(Dispatchers.IO) {
                val uris = viewModel.stationPicUriList.value
                var fileList = ArrayList<File>()
                try {
                    // 根据uri获取文件
                    uris?.let {
                        fileList =
                            FileUtils.getLocalPicsFromUris(requireActivity(), it) as ArrayList<File>
                    }


                    try {
                        val res = chargingPileStationRepository.uploadStationInfo(
                            stationInfoRequest,
                            fileList
                        )

                        withContext(Dispatchers.Main) {
                            Snackbar.make(
                                binding.root,
                                "上传结果：$res ",
                                Snackbar.LENGTH_SHORT
                            ).show()
                            if (res == "success") {
                                findNavController().navigateUp() // 如果成功，则返回列表列
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        withContext(Dispatchers.Main) {
                            Snackbar.make(binding.root, "网络传输有问题，请重新尝试", Snackbar.LENGTH_SHORT)
                                .show()
                        }
                    }
                } finally {
                    fileList.forEach {
                        it.delete()
                    }
                }
            }

        }
    }

    private fun initTimePick() {

        val chipGroupTimeType = binding.chipGroupTimeType
        val chipGroupTime = binding.chipGroupTime

        binding.chipAllDayTime.setOnCloseIconClickListener {
            (it.parent as ChipGroup).removeView(it) // 把自己删掉
        }

        binding.chipAllDay.setOnClickListener {
            chipGroupTime.removeAllViews()
            addChipToChipGroup(resources.getString(R.string.all_day_time), chipGroupTime)
        }

        binding.chipSpecialTime.setOnClickListener {
            chipGroupTime.children.forEach { chip ->
                // 删除 00:00~23:59
                if ((chip as Chip).text == resources.getString(R.string.all_day_time)) {
                    chipGroupTime.removeView(chip)
                }
            }
        }

        binding.btnSelectTime.setOnClickListener {
            val pickBeginTime = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(0)
                .setMinute(0)
                .setTitleText("选择开始时间")
                .setInputMode(INPUT_MODE_CLOCK)
                .build()
            pickBeginTime.show(parentFragmentManager, "beginTimePicker")
            pickBeginTime.addOnPositiveButtonClickListener {
                val pickEndTime = MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_24H)
                    .setHour(0)
                    .setMinute(0)
                    .setTitleText("选择结束时间")
                    .setInputMode(INPUT_MODE_CLOCK)
                    .build()
                pickEndTime.show(parentFragmentManager, "endTimePicker")
                pickEndTime.addOnPositiveButtonClickListener {
                    val beginTime = String.format("%02d", pickBeginTime.hour) + ":" + String.format(
                        "%02d",
                        pickBeginTime.minute
                    )
                    val endTime = String.format("%02d", pickEndTime.hour) + ":" + String.format(
                        "%02d",
                        pickEndTime.minute
                    )
                    val pickedTime = Chip(chipGroupTime.context)
                    pickedTime.text = "$beginTime~$endTime"
                    pickedTime.isChecked = true
                    pickedTime.isCloseIconVisible = true
                    pickedTime.setOnCloseIconClickListener {
                        (it.parent as ChipGroup).removeView(it) // 把自己删掉
                    }
                    chipGroupTime.addView(pickedTime)
                }
            }
        }
    }

    private fun addChipToChipGroup(text: String, group: ChipGroup) {
        val allDayTimeChip = Chip(group.context)
        allDayTimeChip.text = text
        allDayTimeChip.isChecked = true
        allDayTimeChip.isCloseIconVisible = true
        allDayTimeChip.setOnCloseIconClickListener {
            group.removeView(it) // 把自己删掉
        }
        group.addView(allDayTimeChip)
    }

    private fun navigateToLocationMapFragment() {
        val action = AddStationFragmentDirections.actionAddStationFragmentToLocationMapFragment()
        findNavController().navigate(action)
    }

    private fun navigateToAddPileFragment() {
        val action = AddStationFragmentDirections.actionAddStationFragmentToAddPileFragment()
        findNavController().navigate(action)
    }

    companion object {
        const val LOCATION_MAP_TO_ADD_STATION_BUNDLE = "LOCATION"
        const val ADD_PILE_TO_ADD_STATION_BUNDLE = "ADD_PILE"
        const val FROM_ALBUM = 1
        const val TAG = "AddStationFragment"
    }

}
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
import com.lijiahao.sharechargingpile2.databinding.FragmentAddStationBinding
import com.lijiahao.sharechargingpile2.di.GlideApp
import com.lijiahao.sharechargingpile2.repository.ChargingPileStationRepository
import com.lijiahao.sharechargingpile2.network.request.StationInfoRequest
import com.lijiahao.sharechargingpile2.network.service.ChargingPileStationService
import com.lijiahao.sharechargingpile2.ui.publishStationModule.viewmodel.AddStationViewModel
import com.lijiahao.sharechargingpile2.ui.publishStationModule.viewmodel.StationManagerViewModel
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

    val viewModel: AddStationViewModel by activityViewModels()
    private val viewModelStationManager: StationManagerViewModel by activityViewModels()

    @Inject
    lateinit var chargingPileStationService: ChargingPileStationService

    @Inject
    lateinit var chargingPileStationRepository: ChargingPileStationRepository

    private lateinit var albumLauncher: ActivityResultLauncher<Unit>

    private var isChange: Boolean = false
    private var stationId: String = ""

    // 图片相关的View
    private val imgList:Array<ImageView> by lazy {
        arrayOf(
            binding.imStationPic1,
            binding.stationPic2,
            binding.stationPic3,
            binding.stationPic4,
            binding.stationPic5
        )
    }
    private val cardList:Array<MaterialCardView> by lazy{
        arrayOf(
            binding.cardStationPic1,
            binding.cardStationPic2,
            binding.cardStationPic3,
            binding.cardStationPic4,
            binding.cardStationPic5
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        albumLauncher = registerForActivityResult(
            object : ActivityResultContract<Unit, Uri?>() {
                override fun createIntent(context: Context, input: Unit): Intent {
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                    intent.type = "image/*"
                    return intent
                }

                override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
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
            it?.let {
                viewModel.addUri(it)
                Log.i(TAG, "uriList size = ${viewModel.stationPicUriList.value?.size}")
            }
        }

        // 注意上面的registerForActivityResult必须在super.onCreate()上方
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        initUI()
        return binding.root
    }

    private fun initUI() {
        initIfChange()
        initUIListener()
        initTimePick()
        initSubmit()
    }


    private fun initIfChange() {
        // 如果是修改充电桩，则将对应的消息补上
        setFragmentResultListener(StationManagerFragment.CHANGE_STATION) { _, bundle ->
            isChange = bundle.get(StationManagerFragment.IS_CHANGE) as Boolean
            stationId = bundle.get(StationManagerFragment.STATION_ID).toString()
            if (isChange) {
                // 设置对应消息
                val userStationInfo = viewModelStationManager.userStationInfo.value
                userStationInfo?.let { info ->

                    val station = info.stations.find { it.id.toString() == stationId }

                    // 1. 设置开放星期数
                    info.openDayMap[stationId]?.forEach { it ->
                        when (it.day) {
                            "周一" -> {
                                binding.chipMon.isChecked = true
                            }
                            "周二" -> {
                                binding.chipTue.isChecked = true
                            }
                            "周三" -> {
                                binding.chipWen.isChecked = true
                            }
                            "周四" -> {
                                binding.chipThu.isChecked = true
                            }
                            "周五" -> {
                                binding.chipFri.isChecked = true
                            }
                            "周六" -> {
                                binding.chipSat.isChecked = true
                            }
                            "周日" -> {
                                binding.chipSun.isChecked = true
                            }
                        }
                    }

                    // 2. 设置日期
                    val openTimeList = info.openTimeMap[stationId]
                    openTimeList?.let {
                        if (it.size == 1 && it[0].beginTime == "00:00:00" && it[0].endTime == "23:59:59") {
                            binding.chipAllDay.isChecked = true
                        } else {
                            binding.chipSpecialTime.isChecked = true
                            it.forEach { openTime ->
                                val time = openTime.beginTime.substring(
                                    0,
                                    5
                                ) + "~" + openTime.endTime.substring(0, 5)
                                addChipToChipGroup(time, binding.chipGroupTime)
                            }
                        }
                    }

                    // 3. 设置地点
                    station?.let {
                        binding.tvPosition.text = station.posDescription
                        viewModel.latitude = station.latitude
                        viewModel.longitude = station.longitude
                    }


                    // 4. 设置名字
                    binding.itStationName.setText(station?.name)

                    // 5. 设置停车费
                    binding.etParkFee.setText(station?.parkFee.toString())

                    //6. 设置电费
                    val openTimes = info.openTimeMap[stationId]
                    openTimes?.let {
                        if (it.isNotEmpty()) {
                            binding.etChargeFee.setText(it[0].electricCharge.toString())
                        }
                    }

                    // 7.设置remark
                    station?.let {
                        binding.remark.editText?.setText(it.remark)
                    }

                    // 8. 设置充电站中的充电桩
                    val list = info.pileMap[stationId]
                    list?.let {
                        viewModel.pileList = ArrayList(list)
                    }
                    binding.acNum.text = list?.count { it.electricType == "交流" }.toString()
                    binding.dcNum.text = list?.count { it.electricType == "直流" }.toString()

                    // 9. 加载图片
                    val urls = info.picMap[stationId]
                    urls?.let {
                        it.forEachIndexed { index, url ->
                            GlideApp.with(context!!).load(url).into(imgList[index])
                            cardList[index].visibility = View.VISIBLE
                        }
                    }

                    // 10 设置图片删除功能
                    // TODO: 将添加充电站模块中的图片管理， 与修改充电站模块中的图片管理结合一起。
                    // TODO: 修稿充电站模块的提交按钮功能实现。（感觉可以修改一下之前提交的逻辑，每次都设置一下stationId, 后台的话如果stationId不为0设置为修改）
                }


            }

        }
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

        binding.etChargeFee.addTextChangedListener {
            it?.let {

                try {
                    viewModel.chargeFee = binding.etChargeFee.text.toString().toDouble()
                } catch (e: NumberFormatException) {
                    e.printStackTrace()
                    Snackbar.make(
                        binding.root,
                        "充电费用格式错误 ${binding.etChargeFee.text.toString()}",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
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

        setFragmentResultListener(ADD_PILE_TO_ADD_STATION_BUNDLE) { _, bundle ->
            val isok = bundle.getBoolean("IS_OK")
            if (isok) {
                val dcNum = viewModel.pileList.count { it.electricType == "直流" }
                val acNum = viewModel.pileList.size - dcNum
                binding.dcNum.text = dcNum.toString()
                binding.acNum.text = acNum.toString()
            } else {
                Snackbar.make(binding.root, "未添加充电桩", Snackbar.LENGTH_SHORT).show()
            }
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
                viewModel.removeUri(index)
                true
            }
        }


        // TODO: 添加清空内容按钮（将保存按钮改为清空按钮）


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
                        dayList.add("周七")
                    }
                }
            }


            // 保存时间
            val timeList = ArrayList<String>()
            val timeCharge = ArrayList<Float>()
            binding.chipGroupTime.children.forEach { view ->
                val timeText = (view as Chip).text.toString()
                timeList.add(timeText)
                timeCharge.add(viewModel.chargeFee.toFloat())
            }
            Log.i(TAG, "$dayList\n$timeList")


            // 图片uri

            // 这里有一个巨坑！！！！！ fileList在IO线程中的数据不会更新到主线程中，因为两者是同步的，IO线程还需要
            lifecycleScope.launch(Dispatchers.IO) {
                val uris = viewModel.stationPicUriList.value
                val fileList = ArrayList<File>()
                try {
                    // 根据uri获取文件
                    uris?.forEach { uri ->
                        val inputStream = requireActivity().contentResolver.openInputStream(uri)
                        val outputFile = File.createTempFile("111", ".jpg", context!!.filesDir)
                        val outputStream = FileOutputStream(outputFile)
                        try {
                            val buffer = ByteArray(1024)
                            while (true) {
                                val len = inputStream!!.read(buffer, 0, 1024)
                                if (len == -1) {
                                    break;
                                }
                                outputStream.write(buffer)
                            }
                            Log.i(
                                TAG,
                                "filename:${outputFile.name}  outputFileLen = ${outputFile.length()}"
                            )
                            fileList.add(outputFile)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Log.i(TAG, "图片获取失败了")

                        } finally {
                            inputStream?.close()
                            outputStream.close()
                        }
                    }
                    val stationInfoRequest = StationInfoRequest(
                        dayList,
                        timeList,
                        timeCharge,
                        viewModel.getStation(),
                        viewModel.pileList,
                        "1",
                    )

                    try {
                        val stationId =
                            chargingPileStationService.uploadStationInfo(stationInfoRequest)
                        Log.i(TAG, "stationId = $stationId")
                        val res =
                            chargingPileStationRepository.uploadStationPics(stationId, fileList)

                        withContext(Dispatchers.Main) {
                            Snackbar.make(
                                binding.root,
                                "上传结果：stationId:$1 + filesize=$res ",
                                Snackbar.LENGTH_SHORT
                            ).show()
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
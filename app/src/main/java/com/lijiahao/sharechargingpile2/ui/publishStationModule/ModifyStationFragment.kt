package com.lijiahao.sharechargingpile2.ui.publishStationModule

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.launch
import androidx.core.view.children
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.lijiahao.sharechargingpile2.R
import com.lijiahao.sharechargingpile2.data.SharedPreferenceData
import com.lijiahao.sharechargingpile2.databinding.FragmentModifyStationBinding
import com.lijiahao.sharechargingpile2.di.GlideApp
import com.lijiahao.sharechargingpile2.network.request.StationInfoRequest
import com.lijiahao.sharechargingpile2.network.service.ChargingPileStationService
import com.lijiahao.sharechargingpile2.repository.ChargingPileStationRepository
import com.lijiahao.sharechargingpile2.ui.publishStationModule.viewmodel.AddStationViewModel
import com.lijiahao.sharechargingpile2.ui.publishStationModule.viewmodel.StationManagerViewModel
import com.lijiahao.sharechargingpile2.utils.FileUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class ModifyStationFragment : Fragment() {

    private val binding: FragmentModifyStationBinding by lazy {
        FragmentModifyStationBinding.inflate(layoutInflater)
    }

    val viewModel: AddStationViewModel by activityViewModels()
    private val viewModelStationManager: StationManagerViewModel by activityViewModels()
    private val args: ModifyStationFragmentArgs by navArgs()
    private val stationId: String by lazy {
        args.stationId
    }
    private var createViewNumber = 0

    @Inject
    lateinit var chargingPileStationService: ChargingPileStationService

    @Inject
    lateinit var chargingPileStationRepository: ChargingPileStationRepository

    @Inject
    lateinit var sharedPreferenceData: SharedPreferenceData

    private lateinit var albumLauncher: ActivityResultLauncher<Unit>


    // ???????????????View
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
                            Log.i(
                                AddStationFragment.TAG,
                                "uri=$uri \n encodePath=${uri.encodedPath}"
                            )
                            return uri
                        }
                    }
                    return null
                }
            }
        ) {
            it?.let {
                viewModel.addLocalUri(it)
                Log.i(
                    AddStationFragment.TAG,
                    "uriList size = ${viewModel.stationPicUriList.value?.size}"
                )
            }
        }

        // ???????????????registerForActivityResult?????????super.onCreate()??????
        super.onCreate(savedInstanceState)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        createViewNumber++
        initUI()
        return binding.root
    }

    private fun initUI() {
        putInfoToView()
        initUIListener()
        initTimePick()
        initSubmit()
    }

    // ???????????????????????????????????????View??? (!!!???????????????????????????????????????)
    private fun putInfoToView() {
        if (createViewNumber != 1) return

        val userStationInfo = viewModelStationManager.userStationInfo.value
        userStationInfo?.let { info ->
            // 1. ???????????????????????????, ?????????ViewModel????????????
            val station = info.stations.find { it.id.toString() == stationId }
            station?.let {
                viewModel.stationName = it.name
                viewModel.posDescription = it.posDescription
                viewModel.longitude = it.longitude
                viewModel.latitude = it.latitude
                viewModel.parkFee = it.parkFee.toDouble()
                viewModel.remark = it.remark ?: ""
                viewModel.stationCollection = it.collection
                viewModel.stationId = it.id
                val electricChargePeriods = info.electricChargePeriodMap[it.id.toString()]
                electricChargePeriods?.let { list ->
                    viewModel.electricPeriodChargeList = ArrayList(list)
                } ?: {
                    viewModel.electricPeriodChargeList = ArrayList()
                }
            }

            // 2. ?????????????????????
            info.openDayMap[stationId]?.forEach { it ->
                when (it.day) {
                    "??????" -> {
                        binding.chipMon.isChecked = true
                    }
                    "??????" -> {
                        binding.chipTue.isChecked = true
                    }
                    "??????" -> {
                        binding.chipWen.isChecked = true
                    }
                    "??????" -> {
                        binding.chipThu.isChecked = true
                    }
                    "??????" -> {
                        binding.chipFri.isChecked = true
                    }
                    "??????" -> {
                        binding.chipSat.isChecked = true
                    }
                    "??????" -> {
                        binding.chipSun.isChecked = true
                    }
                }
            }

            // 3. ????????????
            val openTimeList = info.openTimeMap[stationId]
            binding.chipGroupTime.removeAllViews()
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


            // 4. ????????????
            station?.let {
                binding.tvPosition.text = station.posDescription
            }


            // 5. ????????????
            binding.itStationName.setText(station?.name)

            // 6. ???????????????
            binding.etParkFee.setText(station?.parkFee.toString())

            // 7.??????remark
            station?.let {
                binding.remark.editText?.setText(it.remark)
            }

            // 8. ??????????????????????????????
            val list = info.pileMap[stationId]
            list?.let {
                viewModel.pileList = ArrayList(list)
            }
            binding.acNum.text = list?.count { it.electricType == "??????" }.toString()
            binding.dcNum.text = list?.count { it.electricType == "??????" }.toString()

            // 9. ????????????
            // 9.1 ??????????????????????????????
            imgList.forEach {
                it.setImageBitmap(null)
            }
            cardList.forEach {
                it.visibility = View.GONE
            }
            // 9.2 ???????????????????????????
            val urls = info.picMap[stationId]
            urls?.let {
                viewModel.setRemoteUriList(it)
            }
        }

    }

    private fun addChipToChipGroup(text: String, group: ChipGroup) {
        val allDayTimeChip = Chip(group.context)
        allDayTimeChip.text = text
        allDayTimeChip.isChecked = true
        allDayTimeChip.isCloseIconVisible = true
        allDayTimeChip.setOnCloseIconClickListener {
            group.removeView(it) // ???????????????
        }
        group.addView(allDayTimeChip)
    }


    private fun initUIListener() {
        binding.ivToChoosePosition.setOnClickListener {
            navigateToLocationMapFragment()
        }

        // ???LocationMapFragment???????????????
        setFragmentResultListener(AddStationFragment.LOCATION_MAP_TO_ADD_STATION_BUNDLE) { _, bundle ->
            val successGetDescription = bundle.getBoolean("isDescription")
            if (successGetDescription) {
                binding.tvPosition.text = viewModel.posDescription
            } else {
                Snackbar.make(binding.root, "????????????????????????????????????", Snackbar.LENGTH_SHORT).show()
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
                        "???????????????????????? ${binding.etParkFee.text.toString()}",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        }

        binding.ivToAddPile.setOnClickListener {
            navigateToAddPileFragment()
        }

        setFragmentResultListener(AddStationFragment.ADD_PILE_TO_ADD_STATION_BUNDLE) { _, _ ->
            Log.i(TAG, "curList = ${viewModel.pileList}")
            val dcNum = viewModel.pileList.count { it.electricType == "??????" }
            val acNum = viewModel.pileList.size - dcNum
            binding.dcNum.text = dcNum.toString()
            binding.acNum.text = acNum.toString()
        }

        binding.remark.editText?.addTextChangedListener {
            it?.let {
                viewModel.remark = binding.remark.editText!!.text.toString()
            }
        }

        binding.cardAddPic.setOnClickListener {
            // ????????????
            viewModel.stationPicUriList.value?.let {
                if (it.size < 5) {
                    albumLauncher.launch()
                } else {
                    Snackbar.make(binding.root, "????????????5?????????, ????????????????????????", Snackbar.LENGTH_SHORT).show()
                }
            }

        }

        // ??????0~remoteUriList.size-1 ?????????
        viewModel.stationPicRemoteUriList.observe(this) { remoteUriList ->
            for (i in 0 until remoteUriList.size) {
                cardList[i].visibility = View.VISIBLE
                GlideApp.with(context!!).load(remoteUriList[i]).into(imgList[i])
            }
            // ???????????????????????????
            viewModel.refreshLocalImage()
        }

        // ??????remoteUriList.size ~ 5 ?????????
        viewModel.stationPicUriList.observe(this) { uriList ->
            // ??????uri??????????????????imageView???
            Log.i(AddStationFragment.TAG, " observe $uriList")
            val remoteUriNum = viewModel.stationPicRemoteUriList.value?.size ?: 0

            for (i in 0 until uriList.size) {
                val index = i + remoteUriNum
                val bitmap =
                    requireActivity().contentResolver.openFileDescriptor(uriList[i], "r")?.use {
                        BitmapFactory.decodeFileDescriptor(it.fileDescriptor)
                    }
                imgList[index].setImageBitmap(bitmap)
                cardList[index].visibility = View.VISIBLE
            }
            for (i in uriList.size + remoteUriNum until 5) {
                cardList[i].visibility = View.GONE
                imgList[i].setImageBitmap(null)
            }
        }

        cardList.forEachIndexed { index, card ->
            card.setOnLongClickListener {
                viewModel.removeImage(index)
                true
            }
        }


        binding.ivToSetElectricCharge.setOnClickListener {
            val action = ModifyStationFragmentDirections.actionModifyStationFragmentToSetElectricPeriodChargeFragment()
            findNavController().navigate(action)
        }

    }

    // ??????????????????????????????????????????????????????
    private fun verify(stationInfoRequest: StationInfoRequest): Boolean {

        // 1. ??????????????????
        if (stationInfoRequest.openDayInWeek.size <= 0) {
            Snackbar.make(binding.root, "?????????????????????", Snackbar.LENGTH_SHORT).show()
            return false
        }

        // 2. ??????????????????
        if (stationInfoRequest.openTime.size <= 0) {
            Snackbar.make(binding.root, "?????????????????????", Snackbar.LENGTH_SHORT).show()
            return false
        }

        // 3. ?????????????????????
        val station = stationInfoRequest.station
        // 3.1 ????????????
        if (station.latitude == 0.0 || station.longitude == 0.0 || station.posDescription == "") {
            Snackbar.make(binding.root, "???????????????", Snackbar.LENGTH_SHORT).show()
            return false
        }
        // 3.2 ????????????
        if (station.name == "") {
            Snackbar.make(binding.root, "????????????????????????", Snackbar.LENGTH_SHORT).show()
            return false
        }
        // 3.3 ????????????
        if (station.parkFee < 0) {
            Snackbar.make(binding.root, "?????????????????????", Snackbar.LENGTH_SHORT).show()
            return false
        }

        // 3.4 ??????????????????
        if (stationInfoRequest.electricChargePeriods.isEmpty()) {
            Snackbar.make(binding.root, "???????????????????????????", Snackbar.LENGTH_SHORT).show()
            return false
        }

        // 3.5 ???????????????
        if (stationInfoRequest.chargingPiles.isEmpty()) {
            Snackbar.make(binding.root, "??????????????????????????????", Snackbar.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun initSubmit() {
        binding.btnSubmit.setOnClickListener {

            // 1. ????????????
            val dayList = ArrayList<String>()
            binding.chipGroupDayPick.checkedChipIds.forEach { chipId ->
                when (chipId) {
                    R.id.chip_Mon -> {
                        dayList.add("??????")
                    }
                    R.id.chip_Tue -> {
                        dayList.add("??????")
                    }
                    R.id.chip_Wen -> {
                        dayList.add("??????")
                    }
                    R.id.chip_Thu -> {
                        dayList.add("??????")
                    }
                    R.id.chip_Fri -> {
                        dayList.add("??????")
                    }
                    R.id.chip_Sat -> {
                        dayList.add("??????")
                    }
                    R.id.chip_Sun -> {
                        dayList.add("??????")
                    }
                }
            }

            // 2. ????????????
            val timeList = ArrayList<String>()
            binding.chipGroupTime.children.forEach { view ->
                val timeText = (view as Chip).text.toString()
                timeList.add(timeText)
            }
            Log.i(AddStationFragment.TAG, "$dayList\n$timeList")

            // 3. ??????StationInfo
            val stationInfoRequest = StationInfoRequest(
                dayList,
                timeList,
                viewModel.getStation(),
                viewModel.pileList,
                viewModel.electricPeriodChargeList,
                sharedPreferenceData.userId
            )


            // 3.5 ??????????????????????????????
            if (!verify(stationInfoRequest)) return@setOnClickListener

            // 4. ????????????remotePics
            val remoteUrls = ArrayList(viewModel.stationPicRemoteUriList.value!!)
            // 4.1 ??????remoteUrls???url=????????????????????????
            for (i in 0 until remoteUrls.size) {
                remoteUrls[i] = remoteUrls[i].split("=")[1]
            }

            viewModel.stationPicUriList.value?.let {
                lifecycleScope.launch(Dispatchers.IO) {
                    var fileList = ArrayList<File>()
                    try {// 5. ????????????????????????
                        fileList =
                            FileUtils.getLocalPicsFromUris(requireActivity(), it) as ArrayList<File>

                        // 6. ????????????
                        val modifyStationResponse = chargingPileStationRepository.modifyStationInfo(
                            stationInfoRequest,
                            remoteUrls,
                            fileList
                        )

                        withContext(Dispatchers.Main) {
                            // 7. ?????????????????????
                            val curPiles = modifyStationResponse.curChargingPiles
                            viewModel.pileList = ArrayList(modifyStationResponse.curChargingPiles)
                            Snackbar.make(binding.root, "??????????????????", Snackbar.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        withContext(Dispatchers.Main) {
                            Snackbar.make(binding.root, "??????????????????", Snackbar.LENGTH_SHORT).show()
                        }
                    } finally {
                        fileList.forEach {
                            it.delete()
                        }
                    }

                }

            }
        }
    }

    private fun initTimePick() {

        val chipGroupTimeType = binding.chipGroupTimeType
        val chipGroupTime = binding.chipGroupTime

        binding.chipAllDayTime.setOnCloseIconClickListener {
            (it.parent as ChipGroup).removeView(it) // ???????????????
        }

        binding.chipAllDay.setOnClickListener {
            chipGroupTime.removeAllViews()
            addChipToChipGroup(resources.getString(R.string.all_day_time), chipGroupTime)
        }

        binding.chipSpecialTime.setOnClickListener {
            chipGroupTime.children.forEach { chip ->
                // ?????? 00:00~23:59
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
                .setTitleText("??????????????????")
                .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
                .build()
            pickBeginTime.show(parentFragmentManager, "beginTimePicker")
            pickBeginTime.addOnPositiveButtonClickListener {
                val pickEndTime = MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_24H)
                    .setHour(0)
                    .setMinute(0)
                    .setTitleText("??????????????????")
                    .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
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
                        (it.parent as ChipGroup).removeView(it) // ???????????????
                    }
                    chipGroupTime.addView(pickedTime)
                }
            }
        }
    }


    private fun navigateToLocationMapFragment() {
        val action =
            ModifyStationFragmentDirections.actionModifyStationFragmentToLocationMapFragment()
        findNavController().navigate(action)
    }

    private fun navigateToAddPileFragment() {
        val action = ModifyStationFragmentDirections.actionModifyStationFragmentToAddPileFragment()
        findNavController().navigate(action)
    }

    companion object {
        const val CHANGE_STATION = "ChangeStation"
        const val TAG = "ModifyStationFragment"
    }

}
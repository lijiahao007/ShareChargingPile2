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
import com.lijiahao.sharechargingpile2.network.request.StationInfoRequest
import com.lijiahao.sharechargingpile2.network.service.ChargingPileStationService
import com.lijiahao.sharechargingpile2.repository.ChargingPileStationRepository
import com.lijiahao.sharechargingpile2.ui.publishStationModule.viewmodel.AddStationViewModel
import com.lijiahao.sharechargingpile2.utils.FileUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class AddStationFragment : Fragment() {

    val binding: FragmentAddStationBinding by lazy {
        FragmentAddStationBinding.inflate(layoutInflater)
    }

    val viewModel: AddStationViewModel by activityViewModels() // ??????VieModel????????????LocationMapFragment???AddPileFragment??????????????????

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
        // ?????????????????????Activity????????????????????????????????????????????????
        albumLauncher = registerForActivityResult(
            object : ActivityResultContract<Unit, Uri?>() {
                override fun createIntent(context: Context, input: Unit): Intent {
                    // ??????????????????Activity???Intent
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                    intent.type = "image/*"
                    return intent
                }

                override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
                    // ????????????Activity???????????????intent???????????????
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


            // ??????parseResult???????????????????????????????????????????????????
            it?.let {
                viewModel.addLocalUri(it)
                Log.i(TAG, "uriList size = ${viewModel.stationPicUriList.value?.size}")
            }
        }

        // ???????????????registerForActivityResult?????????super.onCreate()??????
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

        // ???LocationMapFragment???????????????
        setFragmentResultListener(LOCATION_MAP_TO_ADD_STATION_BUNDLE) { _, bundle ->
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

        // ???????????????????????????
        setFragmentResultListener(ADD_PILE_TO_ADD_STATION_BUNDLE) { _, _ ->
            val dcNum = viewModel.pileList.count { it.electricType == "??????" }
            val acNum = viewModel.pileList.size - dcNum
            binding.dcNum.text = dcNum.toString()
            binding.acNum.text = acNum.toString()
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

        binding.remark.editText?.addTextChangedListener {
            it?.let {
                viewModel.remark = binding.remark.editText!!.text.toString()
            }
        }



        viewModel.stationPicUriList.observe(this) { uriList ->
            // ??????uri??????????????????imageView???
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

        // 3.4 ???????????????
        if (stationInfoRequest.chargingPiles.isEmpty()) {
            Snackbar.make(binding.root, "??????????????????????????????", Snackbar.LENGTH_SHORT).show()
            return false
        }

        // 3.5 ??????????????????
        if (stationInfoRequest.electricChargePeriods.isEmpty()) {
            return false
        }
        return true
    }

    private fun initSubmit() {
        binding.btnSubmit.setOnClickListener {
            // ????????????
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


            // ????????????
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

            // ???????????????????????????????????? fileList???IO???????????????????????????????????????????????????????????????????????????IO???????????????
            lifecycleScope.launch(Dispatchers.IO) {
                val uris = viewModel.stationPicUriList.value
                var fileList = ArrayList<File>()
                try {
                    // ??????uri????????????
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
                                "???????????????$res ",
                                Snackbar.LENGTH_SHORT
                            ).show()
                            if (res == "success") {
                                findNavController().navigateUp() // ?????????????????????????????????
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        withContext(Dispatchers.Main) {
                            Snackbar.make(binding.root, "???????????????????????????????????????", Snackbar.LENGTH_SHORT)
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
                .setInputMode(INPUT_MODE_CLOCK)
                .build()
            pickBeginTime.show(parentFragmentManager, "beginTimePicker")
            pickBeginTime.addOnPositiveButtonClickListener {
                val pickEndTime = MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_24H)
                    .setHour(0)
                    .setMinute(0)
                    .setTitleText("??????????????????")
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
                        (it.parent as ChipGroup).removeView(it) // ???????????????
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
            group.removeView(it) // ???????????????
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
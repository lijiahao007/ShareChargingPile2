package com.lijiahao.sharechargingpile2.ui.mapModule

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.amap.api.location.AMapLocationClient
import com.amap.api.maps.*
import com.amap.api.maps.model.*
import com.amap.api.services.core.AMapException
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.core.ServiceSettings
import com.amap.api.services.geocoder.*
import com.amap.api.services.route.DriveRouteResult
import com.amap.api.services.route.RouteSearch
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import com.lijiahao.sharechargingpile2.R
import com.lijiahao.sharechargingpile2.databinding.FragmentMapBinding
import com.lijiahao.sharechargingpile2.databinding.MapActivityBottomSheetBinding
import com.lijiahao.sharechargingpile2.network.service.ChargingPileStationService
import com.lijiahao.sharechargingpile2.ui.mapModule.listener.BaseOnRouteSearchListener
import com.lijiahao.sharechargingpile2.ui.mapModule.observer.MapViewObserver
import com.lijiahao.sharechargingpile2.ui.mapModule.overlay.DrivingRouteOverlay
import com.lijiahao.sharechargingpile2.ui.mapModule.viewmodel.MapViewModel
import com.lijiahao.sharechargingpile2.utils.SoftKeyBoardUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class MapFragment : Fragment() {

    private val viewModel: MapViewModel by activityViewModels()
    val binding: FragmentMapBinding by lazy {
        FragmentMapBinding.inflate(layoutInflater)
    }
    private val bottomSheetBinding: MapActivityBottomSheetBinding by lazy { binding.bottomSheetInclude }
    private val bottomSheetBehavior: BottomSheetBehavior<View> by lazy {
        BottomSheetBehavior.from(
            bottomSheetBinding.bottomSheetLayout
        )
    }
    private val mapView: MapView by lazy { binding.mapView }   // ????????????
    private lateinit var aMap: AMap // ???????????????
    private lateinit var uiSettings: UiSettings // ?????????????????????
    private var mapCenterMarker: Marker? = null // ??????????????????
    private lateinit var geocodeSearch: GeocodeSearch // ?????? <-> ????????? ????????????
    private var firstFlag = true
    private var isStationMarkerShowed: Boolean = true
    private lateinit var mapViewObserver: MapViewObserver
    private lateinit var prevLatLngBounds: LatLngBounds
    private var isAfterGetStationList:Boolean = false

    // AMap ??????????????????
    private fun privacyInit() {
        // ??????????????????????????????????????????AMap???????????????????????????
        // 1. ???????????????????????????
        // 2. ????????????????????????
        MapsInitializer.updatePrivacyShow(context, true, true)
        MapsInitializer.updatePrivacyAgree(context, true)
        AMapLocationClient.updatePrivacyShow(context, true, true)
        AMapLocationClient.updatePrivacyAgree(context, true)
        ServiceSettings.updatePrivacyShow(context, true, true)
        ServiceSettings.updatePrivacyAgree(context, true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        privacyInit()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        WindowCompat.setDecorFitsSystemWindows(requireActivity().window, false) // ??????????????????z
        bottomSheetBehavior.saveFlags = BottomSheetBehavior.SAVE_ALL
        mapView.onCreate(savedInstanceState)
        init()
        return binding.root
    }

    private fun init() {
        viewModel // ????????????ViewModel???
        initMap()
        initUi()
        initGeoSearch()
        initMarker() // ???????????????marker
        initRouteSearch()
    }

    private fun initMap() {

        // 1. ??????????????????????????????
        aMap = mapView.map

        // 2. ???????????????????????????
        uiSettings = aMap.uiSettings

        // 3. ??????????????????
        uiSettings.isZoomControlsEnabled = false

        // 4. ??????????????????
        val locationStyle = MyLocationStyle().apply {
            interval(2000) // 2?????????????????????
            myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER) //??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        }
        aMap.myLocationStyle = locationStyle
        aMap.isMyLocationEnabled = true

        // 5. ????????????AMap????????????????????????????????????
        setAMapListener()

        // 6. ?????? MapViewObserver ?????????MapView???????????????
        mapViewObserver = MapViewObserver(mapView)
        viewLifecycleOwner.lifecycle.addObserver(mapViewObserver)

    }

    // ?????????AMap???????????????Listener
    private fun setAMapListener() {

        // 1. ????????????????????????????????????????????????
        aMap.addOnMyLocationChangeListener { location ->
            // ????????????????????????????????????
            viewModel.bluePointPos = LatLng(location.latitude, location.longitude)
            if (firstFlag) {
                aMap.animateCamera(CameraUpdateFactory.newLatLng(viewModel.bluePointPos))
                firstFlag = false
                viewModel.projection.value = aMap.projection
            }
        }

        // 2. ???????????????????????????
        aMap.addOnCameraChangeListener(object : AMap.OnCameraChangeListener {
            override fun onCameraChange(p0: CameraPosition?) {
                if (p0 != null) {
                    setCenterMarkerPosition(p0.target)
                }
            }

            override fun onCameraChangeFinish(p0: CameraPosition?) {
                if (p0 != null) {
                    setCenterMarkerPosition(p0.target)
                }
                viewModel.projection.value = aMap.projection
            }
        })

        // 2.1 ??????ViewModel ???Projection?????????????????????
        viewModel.stationInfoMapInProjection.observe(viewLifecycleOwner) {
            isAfterGetStationList = true
        }

        // 3. AMap.OnMapLoadedListener (?????????????????????AMap????????????????????????)
        aMap.setOnMapLoadedListener {
            // 7.1 ??????marker
        }

        // 4. marker ????????????
        aMap.setOnMarkerClickListener { marker ->
            if (marker.isClickable && marker.title != null) {
                val id = marker.title.toInt()
                findBottomSheetNavController().popBackStack(
                    R.id.StationListFragment,
                    false
                )
                navigationToDetailFragment(id)
                true
            } else {
                false
            }
        }

    }

    @Inject
    lateinit var chargingPileStationService: ChargingPileStationService

    // ????????????UI?????????
    private fun initUi() {
        // ????????????
        binding.btnLocation.setOnClickListener {
            // ??????????????????????????????
//            onceLocation()
            aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(viewModel.bluePointPos, 12f))
            Log.i("AMapLocation", "FAB click")
        }

        // ??????????????????
        binding.btnSearch.setOnClickListener {
            // 1. ????????????BottomSheet
            bottomSheetBehavior.isHideable = true
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

            // 2. ?????????????????????
            binding.tfSearch.visibility = View.VISIBLE
            binding.tfSearch.setEndIconOnClickListener {
                SoftKeyBoardUtils.hideKeyBoard(requireActivity())

                val address = binding.tfSearch.editText?.text.toString()

                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        val resList =
                            geocodeSearch.getFromLocationName(GeocodeQuery(address, "shantou"))
                        val loc = resList[0]
                        withContext(Dispatchers.Main) {
                            aMap.animateCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(
                                        loc.latLonPoint.latitude,
                                        loc.latLonPoint.longitude
                                    ),
                                    16f
                                )
                            )
                            binding.tfSearch.visibility = View.GONE
                            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                            bottomSheetBehavior.isHideable = false
                            binding.tfSearch.editText?.text?.clear()
                        }
                    } catch (e: AMapException) {
                        e.printStackTrace()
                        Log.e(TAG, "AMap ??????->?????? ????????????, address=$address")
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "????????????", Toast.LENGTH_SHORT).show()
                            binding.tfSearch.editText?.text?.clear()
                        }
                    }
                }
            }

            binding.tfSearch.setStartIconOnClickListener {
                binding.tfSearch.visibility = View.GONE
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                bottomSheetBehavior.isHideable = false
            }
        }

        // ??????????????????
        binding.btnInsert.setOnClickListener {
            mapCenterMarker?.position?.let {
                lifecycleScope.launch(Dispatchers.IO) {

                    try {
                        val lat = viewModel.mapCenterPos.latitude
                        val lon = viewModel.mapCenterPos.longitude
                        val result = withContext(Dispatchers.IO) {
                            val query =
                                RegeocodeQuery(LatLonPoint(lat, lon), 10f, GeocodeSearch.AMAP)
                            geocodeSearch.getFromLocation(query)
                        }
                        Log.i("btnInsertGeoSearch", result.formatAddress)
                        val insertRes = chargingPileStationService.insertTestChargingPile(
                            lon,
                            lat,
                            result.formatAddress
                        )
                        Log.i("btnInsertGeoSearch", insertRes)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Log.i("btnInsertGeoSearch", "IO??????")
                    } finally {

                    }
                }
            }

        }
    }

    // ?????????->????????????
    private fun initGeoSearch() {
        geocodeSearch = GeocodeSearch(context)

        // 1. ?????????????????? (??????????????????)
        geocodeSearch.setOnGeocodeSearchListener(object : GeocodeSearch.OnGeocodeSearchListener {
            override fun onRegeocodeSearched(result: RegeocodeResult?, rCode: Int) {
                // ????????? -> ????????????
                if (rCode == 1000) {
                    // ??????
                    val address = result?.regeocodeAddress?.formatAddress
                    Log.i("ReGeocodeSearch", "address=$address")
                } else {
                    Log.e("ReGeocodeSearch", "rCode=$rCode, result=$result")
                }
            }

            override fun onGeocodeSearched(result: GeocodeResult?, rCode: Int) {
                // ???????????? -> ?????????
                if (rCode == 1000) {
                    // ??????
                    val latLonPoint = result?.geocodeAddressList?.get(0)?.latLonPoint
                    Log.i(
                        "GeocodeSearch",
                        "latitude=${latLonPoint?.latitude}  longitude=${latLonPoint?.longitude}"
                    )
                } else {
                    Log.e("GeocodeSearch", "rCode=$rCode, result=$result")
                }
            }
        })

        // 2. ?????????????????????
        val address = "????????????????????????????????????322?????????????????????-????????? ????????????"
        val city = "shantou"
        val geoQuery = GeocodeQuery(address, city)
        geocodeSearch.getFromLocationNameAsyn(geoQuery) // ????????????

        // 3. ???????????????????????????
        val reGeoQuery =
            RegeocodeQuery(LatLonPoint(23.410183, 116.635294), 100f, GeocodeSearch.AMAP)
        geocodeSearch.getFromLocationAsyn(reGeoQuery)

    }

    // ?????????????????????UI??? ??????????????????
    private fun initMarker() {
        viewModel.stationList.observe(this) {
            Log.i("initMarker", "viewModel.stationList.size=${it.size}")
            // ????????????
            setStationMarker()
        }

    }

    private fun setStationMarker() {
        isStationMarkerShowed = true
        viewModel.stationList.value?.forEach { station ->
            aMap.addMarker(
                MarkerOptions()
                    .visible(true)
                    .draggable(false)
                    .position(LatLng(station.latitude, station.longitude))
                    .icon(
                        BitmapDescriptorFactory.fromBitmap(
                            BitmapFactory.decodeResource(
                                resources,
                                R.drawable.station_marker
                            )
                        )
                    )
                    .title(station.id.toString())
            ) // ??????title??????id???????????????
        }
    }

    private fun initRouteSearch() {
        val routeSearch = RouteSearch(context)
        routeSearch.setRouteSearchListener(object : BaseOnRouteSearchListener() {
            override fun onDriveRouteSearched(result: DriveRouteResult?, rCode: Int) {
                if (rCode == 1000) {
                    if (result != null && !result.paths.isNullOrEmpty()) {
                        aMap.clear()
                        isStationMarkerShowed = false
                        val path = result.paths[0]
                        val drivingRouteOverlay = DrivingRouteOverlay(
                            requireActivity().applicationContext, aMap, path,
                            result.startPos,
                            result.targetPos, null
                        )
                        drivingRouteOverlay.setNodeIconVisibility(false) //????????????marker????????????
                        drivingRouteOverlay.setIsColorfulline(false) //???????????????????????????????????????
                        drivingRouteOverlay.removeFromMap()
                        drivingRouteOverlay.addToMap()
                        drivingRouteOverlay.zoomToSpan()
                    } else {
                        Snackbar.make(binding.root, "????????????????????????", Snackbar.LENGTH_SHORT).show()
                    }
                } else {
                    Snackbar.make(binding.root, "????????????????????????", Snackbar.LENGTH_SHORT).show()
                }
            }
        })

        // ????????????????????????????????????????????????????????????marker??????????????????????????????marker
        viewModel.finishNavi.observe(this) {
            if (it && !isStationMarkerShowed) {
                aMap.clear()
                setStationMarker()
                // ?????????????????? (???????????????????????????????????????????????????)
                mapCenterMarker = null
            }
        }

        // ??????StationDetailFragment??????????????????
        viewModel.naviEndPoint.observe(this) {
            val startPoint =
                LatLonPoint(viewModel.bluePointPos.latitude, viewModel.bluePointPos.longitude)
            val endPoint = LatLonPoint(it.latitude, it.longitude)
            val fromAndTo = RouteSearch.FromAndTo(startPoint, endPoint)
            val query = RouteSearch.DriveRouteQuery(
                fromAndTo,
                RouteSearch.DRIVING_MULTI_STRATEGY_FASTEST_SHORTEST_AVOID_CONGESTION,
                null,
                null,
                ""
            )
            routeSearch.calculateDriveRouteAsyn(query)
        }

    }

    // ???mapCenterMarker?????????????????????
    private fun setCenterMarkerPosition(latLng: LatLng) {
        if (mapCenterMarker == null) {
            mapCenterMarker = aMap.addMarker(
                MarkerOptions().position(latLng)
                    .draggable(false)
                    .visible(true)
                    .icon(
                        BitmapDescriptorFactory.fromBitmap(
                            BitmapFactory.decodeResource(
                                resources,
                                R.drawable.map_center_marker
                            )
                        )
                    )
            )
            mapCenterMarker?.isClickable = false
        } else {
            mapCenterMarker?.position = latLng
        }
//        Log.i("CenterMarker", "??????latitude:${latLng.latitude}, ??????longitude:${latLng.longitude}")
        viewModel.mapCenterPos = latLng
    }

    private fun navigationToDetailFragment(id: Int) {
        val navController = findBottomSheetNavController()
        val action =
            StationListFragmentDirections.actionStationListFragmentToStationDetailFragment(id)
        navController.navigate(action)
        // ?????????BottomSheet?????????
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED

    }

    private fun findBottomSheetNavController(): NavController {
        return requireActivity().findNavController(R.id.bottom_sheet_fragment_container_view)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView.onDestroy()
        viewLifecycleOwner.lifecycle.removeObserver(mapViewObserver)
    }

    companion object {
        const val TAG = "MapFragment"
    }

}
package com.lijiahao.sharechargingpile2.ui.chatModule

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.WindowCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.lijiahao.sharechargingpile2.data.UserExtendInfo
import com.lijiahao.sharechargingpile2.databinding.FragmentUserInfoBinding
import com.lijiahao.sharechargingpile2.di.GlideApp
import com.lijiahao.sharechargingpile2.network.response.UserInfoResponse
import com.lijiahao.sharechargingpile2.ui.chatModule.adapter.UserExtendInfoAdapter
import com.lijiahao.sharechargingpile2.ui.chatModule.viewmodel.MessageListViewModel

class UserInfoFragment : Fragment() {

    val binding:FragmentUserInfoBinding by lazy {
        FragmentUserInfoBinding.inflate(layoutInflater)
    }

    private val messageListViewModel: MessageListViewModel by activityViewModels()
    private val args:UserInfoFragmentArgs by navArgs()

    // 这里不能空呀。
    private val targetUserInfo: UserInfoResponse by lazy {
        messageListViewModel.userInfoResponseList.value!!.find {
            it.userId == args.userId
        }!!
    }


    private val adapter = UserExtendInfoAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        WindowCompat.setDecorFitsSystemWindows(requireActivity().window, true) // 不全屏显示
        initUI()
        return binding.root
    }

    private fun initUI() {

        binding.userName.text = targetUserInfo.name
        binding.userPhone.text = targetUserInfo.phone
        binding.userRemark.text = targetUserInfo.remark
        context?.let {
            GlideApp.with(it).load(targetUserInfo.avatarUrl).into(binding.userImg)
        }


        binding.userExtendInfoRecycleView.adapter = adapter
        val list = ArrayList<UserExtendInfo>()
        targetUserInfo.extend.entries.forEach {
            val extendInfo = UserExtendInfo(it.key, it.value)
            list.add(extendInfo)
        }
        adapter.submitList(list)



    }

}
package com.lijiahao.sharechargingpile2.ui.chatModule

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.launch
import androidx.core.view.WindowCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.lijiahao.sharechargingpile2.dao.MessageDao
import com.lijiahao.sharechargingpile2.data.*
import com.lijiahao.sharechargingpile2.databinding.FragmentChatBinding
import com.lijiahao.sharechargingpile2.network.request.MessageRequest
import com.lijiahao.sharechargingpile2.network.response.UserInfoResponse
import com.lijiahao.sharechargingpile2.network.service.MessageService
import com.lijiahao.sharechargingpile2.repository.MessageRepository
import com.lijiahao.sharechargingpile2.ui.broadcastreceiver.MessageReceiver
import com.lijiahao.sharechargingpile2.ui.chatModule.adapter.ChatAdapter
import com.lijiahao.sharechargingpile2.ui.chatModule.viewmodel.ChatViewModel
import com.lijiahao.sharechargingpile2.ui.chatModule.viewmodel.MessageListViewModel
import com.lijiahao.sharechargingpile2.ui.publishStationModule.AddStationFragment
import com.lijiahao.sharechargingpile2.utils.SoftKeyBoardUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

@AndroidEntryPoint
class ChatFragment : Fragment() {

    private val binding: FragmentChatBinding by lazy {
        FragmentChatBinding.inflate(layoutInflater)
    }

    private val args: ChatFragmentArgs by navArgs()
    private val messageListViewModel: MessageListViewModel by activityViewModels()


    // TODO: targetUserInfo???curUserInfo ??????????????????????????????Fragment?????????????????????????????????
    // ??????????????????ChatFragment??????????????????messageListViewModel???userInfoResponseList????????????????????????UserInfoResponse????????????????????????????????????????????????????????????ChatFragment???
    private val targetUserInfo: UserInfoResponse by lazy {
        val info = messageListViewModel.userInfoResponseList.value?.find { it.userId == args.userId }
        info!!
    }

    private val curUserInfo: UserInfoResponse by lazy {
        messageListViewModel.curUserInfo
    }


    @Inject
    lateinit var messageRepository: MessageRepository

    @Inject
    lateinit var messageService: MessageService

    @Inject
    lateinit var messageDao: MessageDao

    @Inject
    lateinit var sharedPreferenceData: SharedPreferenceData

    private lateinit var adapter: ChatAdapter

    val viewModel: ChatViewModel by viewModels()

    // ?????????????????????
    lateinit var messageReceiver: MessageReceiver

    private lateinit var albumLauncher: ActivityResultLauncher<Unit> // ????????????????????????????????????

    override fun onCreate(savedInstanceState: Bundle?) {
        viewModel
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
                viewModel.setUri(it)
            }
        }
        super.onCreate(savedInstanceState)
        initBroadcastReceiver()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        WindowCompat.setDecorFitsSystemWindows(requireActivity().window, true) // ??????????????????z
        initUI()
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        context?.unregisterReceiver(messageReceiver)
    }

    private fun initUI() {
        initToolBar()
        initChatView()
        initBottomSendLayout()
        initSubmitText()
        initSubmitImage()
        loadMessageFromRoom()
    }

    private fun initToolBar() {
        binding.ivNavigationUp.setOnClickListener {
            navigateUp()
        }

        binding.tvUserName.text = targetUserInfo.name

        binding.tvUserInfo.setOnClickListener {
            val action =
                ChatFragmentDirections.actionChatFragmentToUserInfoFragment(targetUserInfo.userId)
            findNavController().navigate(action)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initBottomSendLayout() {
        binding.ivAdd.setOnClickListener {
            if (binding.addLayout.visibility == View.GONE) {
                binding.addLayout.visibility = View.VISIBLE
            } else {
                binding.addLayout.visibility = View.GONE
            }
        }

        binding.etContent.addTextChangedListener {
            if (binding.etContent.text.toString() != "") {
                binding.ivAdd.visibility = View.GONE
                binding.btnSend.visibility = View.VISIBLE
                binding.addLayout.visibility = View.GONE
            } else {
                binding.ivAdd.visibility = View.VISIBLE
                binding.btnSend.visibility = View.GONE
            }
        }

        // ????????????????????????
        binding.rvChatList.setOnTouchListener { _, _ ->
            hideSoftKeyboard()
            false
        }


    }

    private fun initSubmitText() {
        binding.btnSend.setOnClickListener {
            // ????????????
            val list = ArrayList<Message>(adapter.currentList)
            val text = binding.etContent.text.toString()

            val message = Message(
                0,
                UUID.randomUUID().toString(),
                sharedPreferenceData.userId,
                targetUserInfo.userId,
                System.currentTimeMillis(),
                true,
                MsgType.TEXT,
                TextMsgBody(text, ""),
                MsgState.SENDING
            )
            // ???????????????
            binding.etContent.setText("")
            list.add(message)
            adapter.submitList(list)
            adapter.notifyItemInserted(list.size - 1)

            // ???????????????????????????????????????????????????Room????????????
            lifecycleScope.launch(Dispatchers.IO) {
                // ??????Http?????????????????????
                try {
                    messageRepository.sendAndSaveTextMessage(message)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Snackbar.make(binding.root, "?????????", Snackbar.LENGTH_SHORT).show()
                }
                withContext(Dispatchers.Main) {
                    val list1 = ArrayList<Message>(adapter.currentList)
                    val index = list1.indexOfFirst { it.uuid == message.uuid }
                    list1[index].state = MsgState.SENT
                    adapter.submitList(list1)
                    adapter.notifyItemChanged(index)
                    binding.rvChatList.smoothScrollToPosition(index)
                }
            }
        }
    }

    private fun initSubmitImage() {
        // ????????????
        binding.cardAddPic.setOnClickListener {
            // ??????????????????
            albumLauncher.launch()
        }

        // ???????????????????????????
        viewModel.picUrlList.observe(this) { uri ->
            lifecycleScope.launch(Dispatchers.IO) {
                val inputStream = requireActivity().contentResolver.openInputStream(uri)
                val imgPath = File("${context!!.filesDir}/imgs")
                imgPath.mkdir()
                val outputFile =
                    File.createTempFile("chat", ".jpg", imgPath)
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
                    val part = MultipartBody.Part.createFormData(
                        "pic",
                        outputFile.name,
                        outputFile.asRequestBody("multipart/form-data".toMediaType())
                    )
                    val uuid = UUID.randomUUID().toString()

                    val sendId = sharedPreferenceData.userId

                    val targetId = targetUserInfo.userId

                    val sendTime = System.currentTimeMillis()

                    // ???????????????
                    val message = Message(
                        0,
                        uuid,
                        sendId,
                        targetId,
                        sendTime,
                        true,
                        MsgType.IMAGE,
                        ImageMsgBody(outputFile.path, "", ""),
                        MsgState.SENDING
                    )

                    // ??????Adapter
                    withContext(Dispatchers.Main) {
                        val list = ArrayList<Message>(adapter.currentList)
                        list.add(message)
                        adapter.submitList(list)
                        val pos = list.size - 1
                        adapter.notifyItemInserted(pos)
                        binding.rvChatList.smoothScrollToPosition(pos)
                    }

                    val request = MessageRequest(
                        uuid,
                        "IMAGE",
                        sendId,
                        targetId,
                        "",
                        sendTime
                    )

                    Log.i(TAG, "outputFile: ${outputFile.name}    size=${outputFile.length()}")


                    // ??????Http?????????????????????
                    // ???????????????????????????
                    try {
                        val remotePath = messageService.sendImageMessage(part, request)
                        message.state = MsgState.SENT
                        (message.msgBody as ImageMsgBody).remotePath = remotePath
                    } catch (e: Exception) {
                        message.state = MsgState.FAILED
                    } finally {
                        // ?????????Room???
                        val list = messageDao.insertMessage(message)
                    }

                    // ??????Adapter??????
                    withContext(Dispatchers.Main) {
                        val list = ArrayList<Message>(adapter.currentList)
                        var pos = 0
                        list.forEachIndexed { index, msg ->
                            if (msg.uuid == message.uuid) {
                                pos = index
                                msg.state = message.state
                                msg.msgBody = message.msgBody
                                return@forEachIndexed
                            }
                        }
                        adapter.submitList(list)
                        adapter.notifyItemChanged(pos)
                    }


                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.i(AddStationFragment.TAG, "?????????????????????")
                } finally {
                    inputStream?.close()
                    outputStream.close()
                }
            }
        }
    }

    private fun initChatView() {
        adapter = ChatAdapter(sharedPreferenceData.userId, curUserInfo, targetUserInfo)
        binding.rvChatList.adapter = adapter
    }

    // ???Room???????????????
    private fun loadMessageFromRoom() {

        val targetId = targetUserInfo.userId.toInt()
        val messageNum = 10
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            var messageList = messageDao.queryMessageByUserId(targetId, messageNum)
            messageList = messageList.reversed()
            messageList.forEach {
                if (it.state == MsgState.UNCHECKED) {
                    it.state = MsgState.CHECKED
                }
            }
            messageDao.updateMessageCheckState(messageList)
            withContext(Dispatchers.Main) {
                adapter.submitList(messageList)
            }
        }

        // ????????????????????????
        binding.swipeChat.setOnRefreshListener {
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                val list = ArrayList<Message>(adapter.currentList)
                val time = if (list.size > 0) list.first().sendTime else System.currentTimeMillis()
                val num = 7
                var messageList = messageDao.queryMessageByUserIdAndTime(targetId, time, num)
                messageList = messageList.reversed()
                list.addAll(0, messageList)
                withContext(Dispatchers.Main) {
                    adapter.submitList(list)
                    binding.swipeChat.isRefreshing = false // ??????????????????
                }
            }
        }

    }

    // ????????????????????????
    private fun initBroadcastReceiver() {
        messageReceiver = object : MessageReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                super.onReceive(context, intent)
                message?.let {
                    if (it.sendId != targetUserInfo.userId && it.targetId != targetUserInfo.userId) {
                        // ?????????????????????????????????????????????
                        return
                    }

                    // ???????????????
                    // 1. ??????????????????recyclerView???
                    val list = LinkedList(adapter.currentList)
                    list.add(it)
                    adapter.submitList(list)
                    adapter.notifyItemInserted(list.size - 1)
                    binding.rvChatList.smoothScrollToPosition(list.size - 1)
                }
            }
        }

        context?.registerReceiver(messageReceiver, messageReceiver.getIntentFilter())
    }


    private fun navigateUp() {
        findNavController().navigateUp()
    }

    private fun hideSoftKeyboard() {
        SoftKeyBoardUtils.hideKeyBoard(requireActivity())
    }


    companion object {
        const val TAG = "ChatFragment"
    }

}
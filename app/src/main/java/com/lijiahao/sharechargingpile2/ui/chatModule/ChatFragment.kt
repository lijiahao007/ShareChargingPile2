package com.lijiahao.sharechargingpile2.ui.chatModule

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.launch
import androidx.core.view.WindowCompat
import androidx.core.widget.addTextChangedListener
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
import com.lijiahao.sharechargingpile2.network.service.UserService
import com.lijiahao.sharechargingpile2.repository.MessageRepository
import com.lijiahao.sharechargingpile2.ui.broadcastreceiver.MessageReceiver
import com.lijiahao.sharechargingpile2.ui.chatModule.adapter.ChatAdapter
import com.lijiahao.sharechargingpile2.ui.chatModule.viewmodel.ChatViewModel
import com.lijiahao.sharechargingpile2.ui.chatModule.viewmodel.MessageListViewModel
import com.lijiahao.sharechargingpile2.ui.mainModule.MainActivity
import com.lijiahao.sharechargingpile2.ui.publishStationModule.AddStationFragment
import com.lijiahao.sharechargingpile2.utils.NetworkUtils
import com.lijiahao.sharechargingpile2.utils.SoftKeyBoardUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.WebSocket
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

    // 注意：要开启ChatFragment的前提时，在messageListViewModel的userInfoResponseList中存在目标用户的UserInfoResponse。如果没有就需要先获取新用户消息，在转到ChatFragment中
    private val targetUserInfo: UserInfoResponse by lazy {
        Log.i(TAG, "messageListViewModel.userInfoResponseList= ${messageListViewModel.userInfoResponseList.value}")
        Log.i(TAG, "args.userId = ${args.userId}")
        val userInfo = messageListViewModel.userInfoResponseList.value!!.find {
            it.userId == args.userId
        }
        userInfo!!
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

    // 消息广播接收器
    lateinit var messageReceiver: MessageReceiver

    private lateinit var albumLauncher: ActivityResultLauncher<Unit> // 作用：打开相册，选取相片

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
        WindowCompat.setDecorFitsSystemWindows(requireActivity().window, true) // 设置全屏显示z
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
            val action = ChatFragmentDirections.actionChatFragmentToUserInfoFragment(targetUserInfo.userId)
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

        // 空白地方说起键盘
        binding.rvChatList.setOnTouchListener { _, _ ->
            hideSoftKeyboard()
            false
        }


    }

    private fun initSubmitText() {
        binding.btnSend.setOnClickListener {
            // 发送按钮
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
            // 设置成空白
            binding.etContent.setText("")
            list.add(message)
            adapter.submitList(list)
            adapter.notifyItemInserted(list.size - 1)

            // 把文本消息通过发送出去。并且存储在Room数据库中
            lifecycleScope.launch(Dispatchers.IO) {
                // 通过Http表单的形式传输
                try {
                    messageRepository.sendAndSaveTextMessage(message)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Snackbar.make(binding.root, "出错了", Snackbar.LENGTH_SHORT).show()
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
        // 图片上传
        binding.cardAddPic.setOnClickListener {
            // 打开图片选择
            albumLauncher.launch()
        }

        // 监听图片选择的结果
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

                    // 创建消息类
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

                    // 更新Adapter
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


                    // 通过Http表单的形式传输
                    // 将图片消息发送出去
                    try {
                        val remotePath = messageService.sendImageMessage(part, request)
                        message.state = MsgState.SENT
                        (message.msgBody as ImageMsgBody).remotePath = remotePath
                    } catch (e: Exception) {
                        message.state = MsgState.FAILED
                    } finally {
                        // 存储在Room中
                        val list = messageDao.insertMessage(message)
                    }

                    // 更新Adapter状态
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
                    Log.i(AddStationFragment.TAG, "图片上传失败了")
                } finally {
                    inputStream?.close()
                    outputStream.close()
                }
            }
        }
    }

    private fun initChatView() {
        adapter = ChatAdapter(sharedPreferenceData.userId)
        binding.rvChatList.adapter = adapter
    }

    // 从Room中获取数据
    private fun loadMessageFromRoom() {

        val targetId = targetUserInfo.userId.toInt()
        val messageNum = 10
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            var messageList = messageDao.queryMessageByUserId(targetId, messageNum)
            messageList = messageList.reversed()
            withContext(Dispatchers.Main) {
                adapter.submitList(messageList)
            }
        }

        // 刷新加载更多数据
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
                    binding.swipeChat.isRefreshing = false // 取消加载符号
                }
            }
        }

    }

    // 初始化广播接收器
    private fun initBroadcastReceiver() {
        messageReceiver = object:MessageReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                super.onReceive(context, intent)
                message?.let{
                    if(it.sendId != targetUserInfo.userId && it.targetId != targetUserInfo.userId) {
                        // 与该聊天用户不相关的消息不显示
                        return
                    }

                    // 收到消息后
                    // 1. 将消息显示在recyclerView中
                    val list = LinkedList(adapter.currentList)
                    list.add(it)
                    adapter.submitList(list)
                    adapter.notifyItemInserted(list.size-1)
                    binding.rvChatList.smoothScrollToPosition(list.size-1)
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
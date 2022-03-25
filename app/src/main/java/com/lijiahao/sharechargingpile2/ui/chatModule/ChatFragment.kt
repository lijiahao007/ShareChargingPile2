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
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.lijiahao.sharechargingpile2.dao.MessageDao
import com.lijiahao.sharechargingpile2.data.*
import com.lijiahao.sharechargingpile2.databinding.FragmentChatBinding
import com.lijiahao.sharechargingpile2.network.request.MessageRequest
import com.lijiahao.sharechargingpile2.network.service.MessageService
import com.lijiahao.sharechargingpile2.repository.MessageRepository
import com.lijiahao.sharechargingpile2.ui.chatModule.adapter.ChatAdapter
import com.lijiahao.sharechargingpile2.ui.chatModule.viewmodel.ChatViewModel
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

    @Inject
    lateinit var messageRepository: MessageRepository

    @Inject
    lateinit var messageService: MessageService

    @Inject
    lateinit var messageDao: MessageDao

    private lateinit var adapter: ChatAdapter

    val viewModel: ChatViewModel by viewModels()


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
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        initUI()
        return binding.root
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

            // TODO: 获取sendId, targetId
            val message = Message(
                0,
                UUID.randomUUID().toString(),
                "1",
                "2",
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

            // 把文本消息通过发送出去。并且存储在Room数据库中
            lifecycleScope.launch(Dispatchers.IO) {
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
                val outputFile =
                    File.createTempFile("chat", ".jpg", File("${context!!.filesDir}/imgs"))
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

                    // TODO 获取sendId
                    val sendId = "1"

                    // TODO 获取targetId
                    val targetId = "2"

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
        adapter = ChatAdapter("1")
        binding.rvChatList.adapter = adapter
    }

    // 从Room中获取数据
    private fun loadMessageFromRoom() {
        //TODO 获取targetId
        val targetId = 2
        // TODO 设置初始加载的消息数量
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
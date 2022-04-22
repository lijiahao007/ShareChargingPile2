package com.lijiahao.sharechargingpile2.ui.mainModule.notifications

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.lijiahao.sharechargingpile2.R
import com.lijiahao.sharechargingpile2.data.SharedPreferenceData
import com.lijiahao.sharechargingpile2.databinding.FragmentCommentBinding
import com.lijiahao.sharechargingpile2.network.request.CommentRequest
import com.lijiahao.sharechargingpile2.network.service.CommentService
import com.lijiahao.sharechargingpile2.utils.SoftKeyBoardUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class CommentFragment : Fragment() {

    private val binding: FragmentCommentBinding by lazy {
        FragmentCommentBinding.inflate(layoutInflater)
    }

    private val args: CommentFragmentArgs by navArgs()
    private val stationId: String by lazy {
        args.stationId
    }
    private val pileId: String by lazy {
        args.pileId
    }

    @Inject
    lateinit var commentService: CommentService

    @Inject
    lateinit var sharedPreferenceData: SharedPreferenceData

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        initUI()
        return binding.root
    }

    private fun initUI() {
        binding.close.setOnClickListener {
            findNavController().navigateUp()
        }


        binding.btnSendComment.setOnClickListener {
            val text = binding.commentTextInputLayout.editText?.text.toString()
            val star = binding.rbStar.rating.toInt().toString()
            val userId = sharedPreferenceData.userId.toInt()
            if (text == "null" || text == "") {
                Snackbar.make(binding.root, "请填写评论", Snackbar.LENGTH_SHORT).show()
            } else {
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        val request =
                            CommentRequest(text, star, userId, stationId.toInt(), pileId.toInt())
                        val res = commentService.publishComment(request)
                        SoftKeyBoardUtils.hideKeyBoard(requireActivity())

                        if (res == "success") {
                            withContext(Dispatchers.Main) {
                                Snackbar.make(binding.root, "评论成功", Snackbar.LENGTH_SHORT).show()
                                delay(500)
                                findNavController().navigateUp()
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                Snackbar.make(binding.root, "评论失败", Snackbar.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        withContext(Dispatchers.Main) {
                            SoftKeyBoardUtils.hideKeyBoard(requireActivity())
                            Snackbar.make(binding.root, "网络异常", Snackbar.LENGTH_SHORT).show()
                        }
                        Log.e(TAG, "网络异常")
                    }
                }
            }
        }
    }


    companion object {
        const val TAG = "CommentFragment"
    }
}
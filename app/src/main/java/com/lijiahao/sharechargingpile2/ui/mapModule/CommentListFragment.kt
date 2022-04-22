package com.lijiahao.sharechargingpile2.ui.mapModule

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.lijiahao.sharechargingpile2.data.SharedPreferenceData
import com.lijiahao.sharechargingpile2.databinding.FragmentCommentListBinding
import com.lijiahao.sharechargingpile2.network.service.CommentService
import com.lijiahao.sharechargingpile2.ui.mapModule.adapter.CommentListAdapter
import com.lijiahao.sharechargingpile2.ui.mapModule.viewmodel.CommentViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CommentListFragment : Fragment() {

    private val binding: FragmentCommentListBinding by lazy {
        FragmentCommentListBinding.inflate(layoutInflater)
    }

    private val args: CommentListFragmentArgs by navArgs()
    private val stationId: Int by lazy {
        args.stationId
    }
    private val viewModel: CommentViewModel by activityViewModels()
    lateinit var adapter: CommentListAdapter

    @Inject lateinit var commentService: CommentService
    @Inject lateinit var sharedPreferenceData: SharedPreferenceData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        loadData()
        initUI()
        return binding.root
    }

    private fun loadData() {
        viewModel.setStationId(stationId)
    }

    private fun initUI() {
        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }

        adapter = CommentListAdapter(this)
        binding.recyclerView.adapter = adapter

        viewModel.commentList.observe(viewLifecycleOwner) { comments ->
            adapter.submitList(comments)
            binding.commentCount.text = comments.size.toString()
            binding.commentScore.text = String.format("%.1f", comments.sumOf { comment -> comment.star.toDouble() } / comments.size)
        }

        viewModel.relateUserMap.observe(viewLifecycleOwner) { map ->
            adapter.addRelativeUserInfo(map)
        }

    }


    companion object {
        const val TAG = "CommentListFragment"
    }
}
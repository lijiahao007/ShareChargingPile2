package com.lijiahao.sharechargingpile2.ui.mapModule.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.lijiahao.sharechargingpile2.R
import com.lijiahao.sharechargingpile2.data.Comment
import com.lijiahao.sharechargingpile2.databinding.ItemCommentBinding
import com.lijiahao.sharechargingpile2.di.GlideApp
import com.lijiahao.sharechargingpile2.network.response.UserInfoResponse
import com.lijiahao.sharechargingpile2.ui.mainModule.notifications.CommentFragment
import com.lijiahao.sharechargingpile2.ui.mapModule.CommentListFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class CommentListAdapter(
    val fragment: Fragment
) : RecyclerView.Adapter<CommentListAdapter.CommentViewHolder>() {

    private val list = ArrayList<Comment>()
    private val relateUserInfoMap = HashMap<Int, UserInfoResponse>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        return CommentViewHolder.create(parent, relateUserInfoMap, fragment)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setInitData(newList: List<Comment>, map: Map<Int, UserInfoResponse>) {
        addRelativeUserInfo(map)
        submitList(newList)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newList: List<Comment>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

    // 需要先设置。
    @SuppressLint("NotifyDataSetChanged")
    fun addRelativeUserInfo(map: Map<Int, UserInfoResponse>) {
        relateUserInfoMap.putAll(map)
        notifyDataSetChanged()
    }

    class CommentViewHolder(
        val context: Context,
        val fragment: Fragment,
        private val relateUserInfoMap: Map<Int, UserInfoResponse>,
        val binding: ItemCommentBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(comment: Comment) {
            binding.tvCommentLikeNum.text = comment.like.toString()
            binding.tvCommentText.text = comment.text
            binding.tvCommentTime.text = LocalDateTime.parse(comment.createTime).format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd")
            )
            binding.rbStar.rating = comment.star.toFloat()
            // 点赞
            binding.cbLike.setOnCheckedChangeListener { _, isCheck ->
                if (fragment is CommentListFragment) {
                    val commentId = comment.id.toInt()
                    val userId = fragment.sharedPreferenceData.userId.toInt()
                    fragment.viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                        try {
                            val curLikeNum = if (isCheck) {
                                fragment.commentService.likeComment(commentId, userId)
                            } else {
                                fragment.commentService.unlikeComment(commentId, userId)
                            }
                            withContext(Dispatchers.Main) {
                                binding.tvCommentLikeNum.text = curLikeNum
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Log.e(TAG, "网络异常，点赞、取消点赞失败")
                            withContext(Dispatchers.Main) {
                                binding.cbLike.isChecked = false
                            }
                        }
                    }
                }
            }

            val userInfo = relateUserInfoMap[comment.userId]
            userInfo?.let {
                binding.tvUserName.text = it.name
                GlideApp.with(context).load(it.avatarUrl).into(binding.ivAvatar)
            }
        }

        companion object {
            fun create(
                parent: ViewGroup,
                map: Map<Int, UserInfoResponse>,
                fragment: Fragment
            ): CommentViewHolder {
                return CommentViewHolder(
                    parent.context,
                    fragment,
                    map,
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.item_comment,
                        parent,
                        false
                    )
                )
            }
        }
    }

    companion object {
        const val TAG = "CommentAdapter"
    }
}
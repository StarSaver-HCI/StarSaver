package com.hci.starsaver.util

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hci.starsaver.data.bookMark.BookMark
import com.hci.starsaver.databinding.ItemLinkBinding


class LinkAdapter : ListAdapter<BookMark, LinkAdapter.ViewHolder>(diffUtil) {

    private var onLinkClickedListener: OnLinkClickedListener? = null

    interface OnLinkClickedListener {
        fun onLinkClicked(bookMark: BookMark)
    }

    private var onDetailClickedListener: OnDetailClickedListener? = null

    interface OnDetailClickedListener {
        fun onDetailClicked(bookMark: BookMark)
    }

    inner class ViewHolder(private val binding: ItemLinkBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(link: BookMark) {
            binding.titleTextView.text = link.title
            binding.linkTextView.text = link.link
            binding.starImageView.visibility = if (link.isStar) View.VISIBLE else View.GONE

            // 비트맵 캐싱, 링크 아이콘 띄우기
            binding.imageView.setImageBitmap(link.bitmap)

            binding.imageView.setOnClickListener {
                onLinkClickedListener?.onLinkClicked(link)
            }
            binding.linkTextView.setOnClickListener {
                onLinkClickedListener?.onLinkClicked(link)
            }
            binding.titleTextView.setOnClickListener {
                onLinkClickedListener?.onLinkClicked(link)
            }
            binding.editButton.setOnClickListener {
                onDetailClickedListener?.onDetailClicked(link)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemLinkBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<BookMark>() {
            override fun areItemsTheSame(oldItem: BookMark, newItem: BookMark): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: BookMark, newItem: BookMark): Boolean {
                return oldItem == newItem
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(list: List<BookMark>) {
        submitList(list)
        notifyDataSetChanged()
    }

    fun setOnLinkClickedListener(lambda: (BookMark) -> Unit) {
        onLinkClickedListener = object : OnLinkClickedListener {
            override fun onLinkClicked(link: BookMark) {
                lambda(link)
            }
        }
    }

    fun setOnDetailClickedListener(lambda: (BookMark) -> Unit) {
        onDetailClickedListener = object : OnDetailClickedListener {
            override fun onDetailClicked(link: BookMark) {
                lambda(link)
            }
        }
    }
}

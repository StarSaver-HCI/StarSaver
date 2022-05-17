package com.hci.starsaver.util

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hci.starsaver.data.bookMark.BookMark
import com.hci.starsaver.databinding.ItemRemindFolderBinding

class RemindFolderAdapter : ListAdapter<BookMark, RemindFolderAdapter.ViewHolder>(diffUtil) {
    private var isAll = false

    interface OnCheckedListener {
        fun onItemClicked(folder: BookMark, view: View)
    }

    var onCheckedListener: OnCheckedListener? = null

    inner class ViewHolder(private val binding: ItemRemindFolderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(folder: BookMark) {
            binding.titleTextView.text = folder.title
            binding.checkbox.isChecked = folder.isRemind
            if (isAll) {
                binding.checkbox.visibility = View.VISIBLE
                binding.root.isClickable = true
                binding.root.isEnabled = true
            } else {
                binding.checkbox.visibility = View.INVISIBLE
                binding.root.isClickable = false
                binding.root.isEnabled = false
            }
            binding.root.setOnClickListener {
                binding.checkbox.isChecked = binding.checkbox.isChecked.not()
                onCheckedListener?.onItemClicked(folder, binding.checkbox)
            }
            binding.checkbox.setOnClickListener {
                onCheckedListener?.onItemClicked(folder, it)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemRemindFolderBinding.inflate(
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
    fun setData(list: List<BookMark>, expanded: Boolean) {
        isAll = expanded
        submitList(list)
        notifyDataSetChanged()
    }


}

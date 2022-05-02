package com.hci.starsaver.util

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hci.starsaver.data.bookMark.BookMark
import com.hci.starsaver.databinding.ItemFolderBinding

class FolderAdapter:ListAdapter<BookMark, FolderAdapter.ViewHolder>(diffUtil) {

    var onFolderClickedListener: OnFolderClickedListener? = null

    interface OnFolderClickedListener{
        fun onFolderClicked(folder:BookMark)
    }

    inner class ViewHolder(private val binding: ItemFolderBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(folder: BookMark){
            binding.titleTextView.text = folder.title
            binding.root.setOnClickListener {
                onFolderClickedListener?.onFolderClicked(folder)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemFolderBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    companion object{
        val diffUtil = object : DiffUtil.ItemCallback<BookMark>(){
            override fun areItemsTheSame(oldItem: BookMark, newItem: BookMark): Boolean {
                return oldItem.id==newItem.id
            }

            override fun areContentsTheSame(oldItem: BookMark, newItem: BookMark): Boolean {
                return oldItem==newItem
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(list: List<BookMark>){
        submitList(list)
        notifyDataSetChanged()
    }

    fun setOnFolderClickedListener( lambda: (BookMark)-> Unit ){
        onFolderClickedListener = object :OnFolderClickedListener{
            override fun onFolderClicked(folder: BookMark) {
                lambda(folder)
            }
        }
    }

}

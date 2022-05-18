package com.hci.starsaver.util

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hci.starsaver.databinding.ItemPathBinding


class PathAdapter: ListAdapter<String, PathAdapter.ViewHolder>(diffUtil) {

    private var onClicked = {pop:Int -> Unit}

    fun setOnClickedListener( lambda: (pop:Int)->Unit ){
        onClicked = lambda
    }

    inner class ViewHolder(val binding:ItemPathBinding):RecyclerView.ViewHolder(binding.root){
        fun bind(path:String){
            binding.pathTextView.text = path
            binding.pathTextView.setOnClickListener {
                onClicked(currentList.size - layoutPosition-1)
            }
            if(layoutPosition == currentList.size-1) binding.arrowImageView.visibility= View.GONE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder( ItemPathBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    companion object {
        val diffUtil = object :DiffUtil.ItemCallback<String>(){
            override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
                return oldItem==newItem
            }

            override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
                return oldItem==newItem
            }
        }
    }
}
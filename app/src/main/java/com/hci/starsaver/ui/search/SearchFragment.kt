package com.hci.starsaver.ui.search

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.hci.starsaver.data.bookMark.BookMark
import com.hci.starsaver.databinding.FragmentSearchBinding
import com.hci.starsaver.ui.home.HomeViewModel
import com.hci.starsaver.util.LinkAdapter

class SearchFragment : Fragment() {

    private lateinit var binding: FragmentSearchBinding
    private lateinit var viewModel: HomeViewModel
    private var adapter = LinkAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]


        initClearButton()
        initBackButton()
        initEditText()
        initRecyclerView()
        return binding.root
    }

    private fun initClearButton() {
        binding.cancelImageButton.setOnClickListener {
            binding.editText.setText("")
        }
    }

    private fun initBackButton() {
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun initEditText() {
        binding.editText.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }

            override fun afterTextChanged(s: Editable?) {
                roadRecyclerView()
            }
        })
    }

    private fun initRecyclerView() {
        binding.searchRecyclerView.adapter = adapter
        binding.searchRecyclerView.layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.VERTICAL, false)
        binding.searchRecyclerView.visibility = View.GONE
    }

    private fun roadRecyclerView() {
        val list = mutableListOf<BookMark>()
        val str = binding.editText.text.toString()
        viewModel.readAllDataByName.observe(viewLifecycleOwner) {
            it.forEach { bm->
                if(bm.title.contains(str) || bm.link?.contains(str) == true || bm.description.contains(str)){
                    list.add(bm)
                }
            }
            adapter.setData(list)
            if(adapter.currentList.size==0){
                binding.searchRecyclerView.visibility = View.GONE
            }else{
                binding.searchRecyclerView.visibility = View.VISIBLE
            }
        }
    }
}
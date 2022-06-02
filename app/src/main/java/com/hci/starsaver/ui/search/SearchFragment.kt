package com.hci.starsaver.ui.search

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.hci.starsaver.R
import com.hci.starsaver.data.bookMark.BookMark
import com.hci.starsaver.databinding.FragmentSearchBinding
import com.hci.starsaver.ui.editlink.EditLinkActivity
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
        binding.editText.addTextChangedListener {
            if(it.toString().isEmpty()){
                binding.searchRecyclerView.visibility = View.GONE
            }else{
                binding.searchRecyclerView.visibility = View.VISIBLE
                roadRecyclerView()
            }
        }
    }

    private fun initRecyclerView() {
        binding.searchRecyclerView.adapter = adapter
        binding.searchRecyclerView.layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.VERTICAL, false)
        binding.searchRecyclerView.visibility = View.GONE

        adapter.setOnLinkClickedListener {
            if (URLUtil.isValidUrl(it.link)) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it.link)))
            } else {
                Toast.makeText(this.context, getString(R.string.checkUrl), Toast.LENGTH_SHORT)
                    .show()
            }
        }

        adapter.setOnDetailClickedListener {
            val intent = Intent(this.context, EditLinkActivity::class.java)
            intent.putExtra("BookMark", it.copy(bitmap = null))
            startActivity(intent)
        }
    }

    private fun roadRecyclerView() {
        val list = mutableListOf<BookMark>()
        val str = binding.editText.text.toString()
        viewModel.readAllDataByName.observe(viewLifecycleOwner) {
            it.forEach { bm->
                if((bm.title.contains(str) || bm.link?.contains(str) == true || bm.description.contains(str)) && bm.isLink==1){
                    list.add(bm)
                }
            }
            adapter.setData(list)
            if(list.size==0){
                binding.searchRecyclerView.visibility = View.GONE
            }else{
                binding.searchRecyclerView.visibility = View.VISIBLE
            }
        }
    }
}
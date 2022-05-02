package com.hci.starsaver.ui.addlink

import android.R
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.hci.starsaver.data.bookMark.BookMark
import com.hci.starsaver.databinding.ActivityAddLinkBinding
import com.hci.starsaver.ui.home.HomeViewModel
import com.hci.starsaver.util.ImageDownloadManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.mm2d.touchicon.TouchIconExtractor

class AddLinkActivity:AppCompatActivity() {

    lateinit var binding: ActivityAddLinkBinding
    lateinit var viewModel: HomeViewModel
    private var currentId = 0L
    private var selectedParentId =0L
    private var isExternal = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddLinkBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]


        initButtons()
        initAction()
        initSpinner()
    }

    private fun addBitmap(link:BookMark){
        val extractor = TouchIconExtractor()
        var url:String?= null
        GlobalScope.launch(Dispatchers.IO) {
            extractor.fromPage(link.link!!, true)
                .let{
                    if(it.isNotEmpty()) url = it.last().url
            }
            if(url!=null){
                link.bitmap = ImageDownloadManager.getImage(url!!)
            }
            viewModel.addBookMark(link)
            finish()
        }
    }

    private fun showAddLinkPopup() {
        // todo 현재 함수만 구현하면 됩니다
        // 구현 후에 주석을 지워주세요
    }

    private fun initButtons() {
        binding.saveButton.isEnabled = binding.linkEditText.text.isNotBlank() && binding.linkNameEditText.text.isNotBlank()
        currentId = intent.getLongExtra("id",0)
        selectedParentId = currentId

        binding.saveButton.setOnClickListener {
            val isRemind = binding.reminderSwitch.isChecked
            val isStared = binding.starSwitch.isChecked
            val bm = BookMark(null
                ,selectedParentId
                ,binding.linkNameEditText.text.toString()
                ,binding.descriptionEditText.text.toString()
                ,1
                , binding.linkEditText.text.toString()
                ,isRemind
                ,isStared
            )

            // 앱 외부에서 링크 추가할 때
            if(isExternal) Toast.makeText(this,"링크를 저장했습니다.",Toast.LENGTH_SHORT).show()
            else{
                // todo 앱 내부에서 링크를 추가할 때 동작되는 부분입니다
                // 구현 후에 주석을 지워주세요
                showAddLinkPopup()
            }
            addBitmap(bm)
        }
        binding.cancelButton.setOnClickListener {
            finish()
        }
        binding.transparentView.setOnClickListener {
            finish()
        }
        binding.linkEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                binding.saveButton.isEnabled = binding.linkEditText.text.isNotBlank() && binding.linkNameEditText.text.isNotBlank()
            }
        })
        binding.linkNameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                binding.saveButton.isEnabled = binding.linkEditText.text.isNotBlank() && binding.linkNameEditText.text.isNotBlank()
            }
        })
    }

    private fun initAction() {
        when (intent?.action) {
            Intent.ACTION_SEND -> {
                if ("text/plain" == intent.type) {
                    handleSendText(intent) // Handle text being sent
                    isExternal = true
                }
            }
        }
    }

    private fun handleSendText(intent: Intent) {
        intent.getStringExtra(Intent.EXTRA_TEXT)?.let {
            binding.linkEditText.setText(it)
        }
    }

    private fun initSpinner() {
        viewModel.readAllData.observe(this){
            val idList = ArrayDeque<Long>()
            val spinList = ArrayDeque<String>()
            var b:BookMark? = null
            it.forEach { bm->
                if(bm.isLink==0) {
                    if(bm.id == currentId) {
                        b = bm
                    }else{
                        idList.addLast(bm.id!!)
                        spinList.addLast(bm.title)
                    }
                }
            }
            if(b!=null){
                idList.addFirst(b!!.id!!)
                spinList.addFirst(b!!.title)
            }else{
                idList.addFirst(0)
                spinList.addFirst("home")
            }


            val adapter = ArrayAdapter(this,
                R.layout.simple_spinner_dropdown_item,
                ArrayList<String>(spinList))
            binding.spinner.adapter = adapter

            binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    selectedParentId = idList[position]
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }
}
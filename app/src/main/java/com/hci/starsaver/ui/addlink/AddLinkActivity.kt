package com.hci.starsaver.ui.addlink

import android.R
import android.app.Dialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.hci.starsaver.data.bookMark.BookMark
import com.hci.starsaver.databinding.ActivityAddLinkBinding
import com.hci.starsaver.databinding.DialogAddLinkBinding
import com.hci.starsaver.ui.home.HomeViewModel
import com.hci.starsaver.util.ImageDownloadManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.mm2d.touchicon.TouchIconExtractor


class AddLinkActivity : AppCompatActivity() {

    lateinit var binding: ActivityAddLinkBinding
    lateinit var viewModel: HomeViewModel
    private var currentId = 0L
    private var selectedParentId = 0L
    private var isExternal = false
    private var isRemind = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddLinkBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]




        dragToClose()
        initButtons()
        initAction()
        initSpinner()
    }

    private fun dragToClose() {
        val view = binding.parentView
        var distance = 0f
        var oldY = 0f
        view.run {
            setOnTouchListener(object : View.OnTouchListener {
                override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                    when (event?.action) {
                        MotionEvent.ACTION_DOWN -> {
                            distance = event.getY()
                            oldY = binding.bottomSheetDashBoardLayout.getY()
                        }
                        MotionEvent.ACTION_MOVE -> {
                            if (distance - event.getY() < 0) {
                                binding.bottomSheetDashBoardLayout.setY(oldY - (distance - event.getY()))
                            }
                        }
                        MotionEvent.ACTION_UP -> {
                            distance -= event.getY()

                            if (Math.abs(distance) < 500) {
                                binding.bottomSheetDashBoardLayout.animate().y(oldY)
                                return false
                            }
                            if (distance < 0) {
                                finish()
                            }
                        }
                    }
                    return true
                }
            })
        }
    }

    private fun addBitmap(link: BookMark) {
        val extractor = TouchIconExtractor()
        var url: String? = null
        GlobalScope.launch(Dispatchers.IO) {
            extractor.fromPage(link.link!!, true)
                .let {
                    if (it.isNotEmpty()) url = it.last().url
                }
            if (url != null) {
                link.bitmap = ImageDownloadManager.getImage(url!!)
            }else{
                link.bitmap = BitmapFactory.decodeResource(resources, com.hci.starsaver.R.drawable.icon)
            }
            viewModel.addBookMark(link)
            finish()
        }
    }

    private fun showAddLinkPopup() {
        var addLinkDialog = Dialog(this)
        var dialogView = DialogAddLinkBinding.inflate(layoutInflater).root
        addLinkDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        addLinkDialog.window!!.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        addLinkDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        addLinkDialog.setContentView(dialogView)
        addLinkDialog.show()
    }

    private fun initButtons() {
        binding.saveButton.isEnabled =
            binding.linkEditText.text.isNotBlank() && binding.linkNameEditText.text.isNotBlank()
        currentId = intent.getLongExtra("id", 0)
        selectedParentId = currentId

        binding.saveButton.setOnClickListener {
            val isStared = binding.starSwitch.isChecked
            val bm = BookMark(
                null,
                selectedParentId,
                binding.linkNameEditText.text.toString(),
                binding.descriptionEditText.text.toString(),
                1,
                binding.linkEditText.text.toString(),
                isRemind,
                isStared
            )
            binding.parentView.visibility = View.GONE

            // ??? ???????????? ?????? ????????? ???
            if (isExternal) Toast.makeText(this, "????????? ??????????????????.", Toast.LENGTH_SHORT).show()
            else {
                showAddLinkPopup()
            }
            //?????? ??? ????????? ????????? ?????? ????????? ????????? ??????
            val handler = Handler()
            handler.postDelayed(Runnable { addBitmap(bm) }, 1500)
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
                binding.saveButton.isEnabled =
                    binding.linkEditText.text.isNotBlank() && binding.linkNameEditText.text.isNotBlank()
            }
        })
        binding.linkNameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                binding.saveButton.isEnabled =
                    binding.linkEditText.text.isNotBlank() && binding.linkNameEditText.text.isNotBlank()
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
        viewModel.readAllData.observe(this) {
            val idList = ArrayDeque<Long>()
            val spinList = ArrayDeque<String>()
            var b: BookMark? = null
            it.forEach { bm ->
                if (bm.isLink == 0) {
                    if (bm.id == currentId) {
                        b = bm
                    } else {
                        idList.addLast(bm.id!!)
                        spinList.addLast(bm.title)
                    }
                }
            }
            if (b != null) {
                idList.addFirst(b!!.id!!)
                spinList.addFirst(b!!.title)
            } else {
                idList.addFirst(0)
                spinList.addFirst("home")
            }


            val adapter = ArrayAdapter(
                this,
                R.layout.simple_spinner_dropdown_item,
                ArrayList<String>(spinList)
            )
            binding.spinner.adapter = adapter

            binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    selectedParentId = idList[position]
                    GlobalScope.launch {
                        viewModel.readAllData.value?.forEach {  b->
                            if(b.id == selectedParentId){
                                isRemind = b.isRemind
                            }
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }
}
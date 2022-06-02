package com.hci.starsaver.ui.editlink

import android.app.Dialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.hci.starsaver.R
import com.hci.starsaver.data.bookMark.BookMark
import com.hci.starsaver.databinding.ActivityEditLinkBinding
import com.hci.starsaver.databinding.DialogRemoveBookmarkBinding
import com.hci.starsaver.ui.home.HomeViewModel
import com.hci.starsaver.util.ImageDownloadManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.mm2d.touchicon.TouchIconExtractor

class EditLinkActivity : AppCompatActivity(), PopupMenu.OnMenuItemClickListener {

    private lateinit var binding: ActivityEditLinkBinding
    private lateinit var viewModel: HomeViewModel
    private lateinit var bm: BookMark
    private lateinit var adapter: ArrayAdapter<String>
    private var selectedParentId = 0L
    private var isRemoved = false
    private var isEditing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditLinkBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        initSpinner()
        initLayout()
        initBeforeEdit()
    }

    private fun initLayout() {
        bm = intent.getSerializableExtra("BookMark") as BookMark
        binding.titleTextView.setText(bm.title)
        binding.linkTextView.setText(bm.link)
        binding.descriptionEditText.setText(bm.description)
        binding.starSwitch.isChecked = bm.isStar


        binding.backButton.setOnClickListener {
            finish()
        }
        binding.shareOrRemoveButton.setOnClickListener {
            showPopup(it)
        }
    }

    private fun initBeforeEdit() {
        isEditing = false
        binding.apply {
            topBarTextView.visibility = View.INVISIBLE
            shareOrRemoveButton.visibility = View.VISIBLE
            cancelButton.visibility = View.INVISIBLE
            saveButton.visibility = View.INVISIBLE
            titleTextView.setOnClickListener { initEdit() }
            titleTextView.onFocusChangeListener = focus
            titleTextView.addTextChangedListener(watcher)
            linkTextView.setOnClickListener { initEdit() }
            linkTextView.onFocusChangeListener = focus
            linkTextView.addTextChangedListener(watcher)
            descriptionEditText.setOnClickListener { initEdit() }
            descriptionEditText.onFocusChangeListener = focus
            descriptionEditText.addTextChangedListener(watcher)
            starSwitch.setOnCheckedChangeListener { _, _ -> initEdit() }
            initSpinner()
        }
    }

    private fun initSpinner() {
        viewModel.readAllData.observe(this) {
            val idList = ArrayDeque<Long>()
            val spinList = ArrayDeque<String>()
            it.forEach { bookmark ->
                if (bookmark.isLink == 0) {
                    idList.addLast(bookmark.id!!)
                    spinList.addLast(bookmark.title)
                }
            }
            adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
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
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
            binding.spinner.setSelection(idList.indexOf(bm.parentId))
        }
    }

    private fun initEdit() {
        isEditing = true
        binding.apply {
            topBarTextView.visibility = View.VISIBLE
            shareOrRemoveButton.visibility = View.INVISIBLE
            cancelButton.visibility = View.VISIBLE
            saveButton.visibility = View.VISIBLE
            cancelButton.setOnClickListener {
                bm = intent.getSerializableExtra("BookMark") as BookMark
                binding.titleTextView.setText(bm.title)
                binding.linkTextView.setText(bm.link)
                binding.descriptionEditText.setText(bm.description)
                binding.starSwitch.isChecked = bm.isStar
                currentFocus?.clearFocus()
                initBeforeEdit()
            }
            saveButton.setOnClickListener {
                currentFocus?.clearFocus()
                addBitmap(getEditedBookMark())
                initBeforeEdit()
            }
        }
    }

    private fun showPopup(v: View) {
        val popup = PopupMenu(this, v) // PopupMenu 객체 선언
        popup.setOnMenuItemClickListener(this)
        popup.menuInflater.inflate(R.menu.link_menu, popup.menu) // 메뉴 레이아웃 inflate
        popup.show() // 팝업 보여주기
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
            } else {
                link.bitmap =
                    BitmapFactory.decodeResource(resources, com.hci.starsaver.R.drawable.icon)
            }
            viewModel.addBookMark(link)
        }
    }

    private fun getEditedBookMark(): BookMark {
        bm.parentId = selectedParentId
        return BookMark(
            bm.id,
            selectedParentId,
            binding.titleTextView.text.toString(),
            binding.descriptionEditText.text.toString(),
            1,
            binding.linkTextView.text.toString(),
            bm.isRemind,
            binding.starSwitch.isChecked,
        )
    }

    private fun sendToExternal() {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            val editedBm = getEditedBookMark()
            val text = "Shared By StarSaver\n\n" +
                    "title: ${editedBm.title}\n" +
                    "url: ${editedBm.link}\n" +
                    "description: ${editedBm.description}"
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.linkRemoveButton -> {
                showDialog()
            }
            R.id.linkShareButton -> sendToExternal()
        }
        return item != null
    }

    private fun showDialog() {
        var removeDialog = Dialog(this)
        var dialogView = DialogRemoveBookmarkBinding.inflate(layoutInflater)


        removeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        removeDialog.setContentView(dialogView.root)
        removeDialog.window!!.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        );
        removeDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialogView.cancelTextView.setOnClickListener {
            removeDialog.cancel()
        }

        dialogView.removeTextView.setOnClickListener {
            isRemoved = true
            viewModel.deleteBookMark(bm)
            finish()
        }

        removeDialog.window?.setGravity(Gravity.BOTTOM)
        removeDialog.show()


//        val dialogBuilder = AlertDialog.Builder(this)
//
//        dialogBuilder.setMessage("별 하나가 사라지려 합니다.")
//            .setCancelable(false)
//            .setPositiveButton("삭제") { dialog, id ->
//                isRemoved = true
//                viewModel.deleteBookMark(bm)
//                finish()
//            }
//            .setNegativeButton("취소") { dialog, id ->
//                dialog.cancel()
//            }
//        val alert = dialogBuilder.create()
//        alert.setTitle("정말 삭제 하시겠습니까?")
//        alert.window?.setGravity(Gravity.BOTTOM)
//        alert.show()
    }

    private val watcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {
            if (isEditing.not()) {
                initEdit()
            }
        }
    }

    private val focus = View.OnFocusChangeListener { v, hasFocus ->
        if (currentFocus == v && isEditing.not()) {
            initEdit()
        }
    }
}
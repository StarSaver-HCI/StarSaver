package com.hci.starsaver.ui.editfolder

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.hci.starsaver.R
import com.hci.starsaver.config.BookMarkApplication
import com.hci.starsaver.data.bookMark.BookMark
import com.hci.starsaver.databinding.ActivityEditFolderBinding
import com.hci.starsaver.ui.home.HomeFragment
import com.hci.starsaver.ui.home.HomeViewModel

class EditFolderActivity : AppCompatActivity(), PopupMenu.OnMenuItemClickListener {

    private lateinit var binding: ActivityEditFolderBinding
    private lateinit var viewModel: HomeViewModel
    private lateinit var bm: BookMark
    private lateinit var list: MutableList<BookMark>
    private var isRemoved = false
    private var isEditing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditFolderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = HomeFragment.viewModel

        initLayout()
        initBeforeEdit()
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
            descriptionEditText.setOnClickListener { initEdit() }
            descriptionEditText.onFocusChangeListener = focus
            descriptionEditText.addTextChangedListener(watcher)
            reminderSwitch.setOnCheckedChangeListener { _, _ -> initEdit() }
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
                currentFocus?.clearFocus()
                initBeforeEdit()
            }
            saveButton.setOnClickListener {
                currentFocus?.clearFocus()

                val b = getEditedBookMark()
                if (bm.id == 0L) {
                    BookMarkApplication.prefs.homeIsRemind = b.isRemind
                    BookMarkApplication.prefs.homeName = b.title
                    BookMarkApplication.prefs.homeDescription = b.description
                }
                viewModel.currentBookMark.value = b
                viewModel.addBookMark(b)

                initBeforeEdit()
            }
        }
    }

    private fun initLayout() {
        viewModel.readAllData.observe(this) {
            list = mutableListOf()
            it.forEach { b -> list.add(b) }
        }
        bm = intent.getSerializableExtra("BookMark") as BookMark
        binding.titleTextView.setText(bm.title)
        binding.descriptionEditText.setText(bm.description)
        binding.reminderSwitch.isChecked =
            if (bm.id != 0L) bm.isRemind else BookMarkApplication.prefs.homeIsRemind

        binding.backButton.setOnClickListener {
            finish()
        }

        binding.shareOrRemoveButton.setOnClickListener {
            showPopup(it)
        }
    }

    private fun showPopup(v: View) {
        val popup = PopupMenu(this, v) // PopupMenu 객체 선언
        popup.setOnMenuItemClickListener(this)
        popup.menuInflater.inflate(
            if (bm.id == 0L) R.menu.folder_menu else R.menu.link_menu,
            popup.menu
        )
        // 팝업 보여주기
        popup.show()
    }

    private fun getEditedBookMark(): BookMark {
        return BookMark(
            bm.id,
            bm.parentId,
            binding.titleTextView.text.toString(),
            binding.descriptionEditText.text.toString(),
            0,
            "",
            binding.reminderSwitch.isChecked,
            binding.reminderSwitch.isChecked
        )
    }

    private fun sendToExternal() {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            val editedBm = getEditedBookMark()
            val text = makeTree("Shared by StarSaver\n\n" + editedBm.title + "[folder]", bm.id!!, 0)
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    private fun makeTree(tmp: String, id: Long, depth: Int): String {
        var str = tmp

        for (b in list) {
            if (b.parentId == id) {
                str += "\n|"
                for (i in 0..depth) {
                    str += "___"
                }
                str = makeTree(str + getLink(b), b.id!!, depth + 1)
            }
        }

        return str
    }

    private fun getLink(b: BookMark): String {
        return if (b.isLink == 1) "${b.title} : ${b.link}" else "${b.title}[folder]"
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.linkRemoveButton -> {
                showDialog()
                if (isRemoved) finish()
            }
            R.id.linkShareButton -> {
                viewModel.addBookMark(getEditedBookMark())
                sendToExternal()
            }
            R.id.folderShareButton -> {
                viewModel.addBookMark(getEditedBookMark())
                sendToExternal()
            }
        }
        return item != null
    }

    private fun showDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setMessage("별들이 사라지려 합니다.")
            .setCancelable(false)
            .setPositiveButton("삭제") { _, _ ->
                isRemoved = true
                viewModel.deleteBookMark(bm)
                finish()
            }
            .setNegativeButton("취소") { dialog, _ ->
                dialog.cancel()
            }
        val alert = dialogBuilder.create()
        alert.setTitle("정말 삭제 하시겠습니까?")
        alert.show()
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
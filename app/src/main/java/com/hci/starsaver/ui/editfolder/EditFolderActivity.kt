package com.hci.starsaver.ui.editfolder

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.PopupMenu
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.hci.starsaver.R
import com.hci.starsaver.config.BookMarkApplication
import com.hci.starsaver.data.bookMark.BookMark
import com.hci.starsaver.databinding.ActivityEditFolderBinding
import com.hci.starsaver.databinding.DalogRemoveBinding
import com.hci.starsaver.databinding.DialogNotificationBinding
import com.hci.starsaver.ui.home.HomeFragment
import com.hci.starsaver.ui.home.HomeViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class EditFolderActivity : AppCompatActivity(){

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
        editListener()
    }

    private fun editListener(){
        binding.titleTextView.addTextChangedListener {changeButton()}
        binding.descriptionEditText.addTextChangedListener {changeButton()}
        binding.reminderSwitch.setOnClickListener {changeButton()}
    }

    private fun changeButton() {
        if (isEditing){
            if(binding.titleTextView.getText().toString() == bm.title &&
                binding.descriptionEditText.getText().toString() ==  bm.description &&
                ((bm.id != 0L && binding.reminderSwitch.isChecked == bm.isRemind) || (bm.id == 0L && binding.reminderSwitch.isChecked == BookMarkApplication.prefs.homeIsRemind))){
                binding.saveButton.setTextColor(Color.parseColor("#999999"))
                binding.saveButton.isClickable = false
            }
            else{
                binding.saveButton.setTextColor(Color.parseColor("#3162AC"))
                binding.saveButton.isClickable = true
            }
        }
    }

    private fun initBeforeEdit() {
        isEditing = false
        binding.apply {
            topBarTextView.visibility = View.INVISIBLE
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
            saveButton.visibility = View.VISIBLE
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
                GlobalScope.launch {
                    viewModel.readAllData.value!!.forEach {
                        if(it.parentId == bm.id){
                            it.isRemind = b.isRemind
                            viewModel.addBookMark(it)
                        }
                    }
                }
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
            if(isEditing){
                currentFocus?.clearFocus()
                initBeforeEdit()
            }
            else {finish()}
        }
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

    private fun showDialog() {
//        val dialogBuilder = AlertDialog.Builder(this)
//        dialogBuilder.setMessage("별들이 사라지려 합니다.")
//            .setCancelable(false)
//            .setPositiveButton("삭제") { _, _ ->
//                isRemoved = true
//                viewModel.deleteBookMark(bm)
//                finish()
//            }
//            .setNegativeButton("취소") { dialog, _ ->
//                dialog.cancel()
//            }
//        val alert = dialogBuilder.create()
//        alert.setTitle("정말 삭제 하시겠습니까?")
//        alert.show()


        var addLinkDialog = Dialog(this)
        var dialogView = DalogRemoveBinding.inflate(layoutInflater)
        dialogView.cancelButton.setOnClickListener {
            isRemoved = true
            viewModel.deleteBookMark(bm)
            finish()
        }
        dialogView.removeButton.setOnClickListener {
            addLinkDialog.dismiss()
        }
        addLinkDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        addLinkDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        addLinkDialog.setContentView(dialogView.root)
        addLinkDialog.show()
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
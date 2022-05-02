package com.hci.starsaver.ui.addfolder

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.hci.starsaver.data.bookMark.BookMark
import com.hci.starsaver.databinding.ActivityAddFolderBinding
import com.hci.starsaver.ui.home.HomeViewModel

class AddFolderActivity : AppCompatActivity() {

    lateinit var binding: ActivityAddFolderBinding
    lateinit var viewModel: HomeViewModel

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddFolderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]


        initButtons()
    }

    private fun initButtons() {
        binding.saveButton.isEnabled = binding.folderNameEditText.text.isNotBlank()
        val currentId = intent.getLongExtra("id", 0)

        binding.saveButton.setOnClickListener {
            val isRemind = binding.reminderSwitch.isChecked
            val bm = BookMark(
                null,
                currentId,
                binding.folderNameEditText.text.toString(),
                "",
                0,
                "",
                isRemind
            )
            viewModel.addBookMark(bm)
            finish()
        }
        binding.cancelButton.setOnClickListener {
            finish()
        }
        binding.transparentView.setOnClickListener {
            finish()
        }
        binding.folderNameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                binding.saveButton.isEnabled = binding.folderNameEditText.text.isNotBlank()
            }

        })
    }
}
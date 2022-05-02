package com.hci.starsaver.ui.notifications

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.hci.starsaver.R
import com.hci.starsaver.config.BookMarkApplication
import com.hci.starsaver.data.bookMark.BookMark
import com.hci.starsaver.databinding.FragmentNotificationsBinding
import com.hci.starsaver.ui.home.HomeFragment
import com.hci.starsaver.ui.home.HomeViewModel
import com.hci.starsaver.util.RemindFolderAdapter

class NotificationsFragment : Fragment() {

    private lateinit var binding: FragmentNotificationsBinding
    private lateinit var viewModel: HomeViewModel
    private lateinit var adapter: RemindFolderAdapter
    private var expanded = false
    private var timePicking = false
    private var numberPicking = false

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        viewModel = HomeFragment.viewModel

        initLayout()
        initButtons()
        updateRecyclerView()

        return binding.root
    }

    private fun initButtons() {
        binding.isAllFolderSwitchButton.setOnCheckedChangeListener { _, isChecked ->
            expanded = isChecked
            reloadList()
        }
    }

    private fun reloadList() {
        HomeFragment.viewModel.addBookMark(BookMark(-1, -1, "", "", -1, ""))
        HomeFragment.viewModel.deleteBookMark(BookMark(-1, -1, "", "", -1, ""))
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("SetTextI18n", "ResourceAsColor")
    private fun initLayout() {
        adapter = RemindFolderAdapter()
        adapter.onCheckedListener = object : RemindFolderAdapter.OnCheckedListener {
            override fun onItemClicked(folder: BookMark, view: View) {
                folder.isRemind = folder.isRemind.not()
                viewModel.addBookMark(folder)
                if (folder.id == 0L) {
                    BookMarkApplication.prefs.homeIsRemind = folder.isRemind
                }
            }
        }
        binding.remindFolderRecyclerView.adapter = adapter
        binding.remindFolderRecyclerView.layoutManager =
            LinearLayoutManager(this.context, LinearLayoutManager.VERTICAL, false)

        initTimePicker()
        initNumberPicker()

        binding.countTextView.setOnClickListener {
            if (numberPicking) {
                numberPicking = false
                it.background = null
                binding.numberPickerLayout.visibility = View.GONE
            } else {
                numberPicking = true
                it.setBackgroundResource(R.drawable.gray_corner_background)
                binding.timeTextView.background = null
                binding.numberPickerLayout.visibility = View.VISIBLE
                binding.timePicker.visibility = View.GONE
                binding.weekNumberPicker.value = BookMarkApplication.prefs.week!!
                binding.bunNumberPicker.value = BookMarkApplication.prefs.notificationBun!!
                binding.gaeNumberPicker.value = BookMarkApplication.prefs.notificationGae!!
            }
        }
        binding.timeTextView.setOnClickListener {
            if (timePicking) {
                timePicking = false
                it.background = null
                binding.timePicker.visibility = View.GONE
            } else {
                timePicking = true
                it.setBackgroundResource(R.drawable.gray_corner_background)
                binding.countTextView.background = null
                binding.numberPickerLayout.visibility = View.GONE
                binding.timePicker.visibility = View.VISIBLE
            }
        }

        binding.reminderSwitch.isChecked = BookMarkApplication.prefs.remindAvailable
        binding.reminderSwitch.setOnClickListener {
            BookMarkApplication.prefs.remindAvailable = binding.reminderSwitch.isChecked
        }
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.M)
    private fun initNumberPicker() {
        binding.countTextView.text = "${BookMarkApplication.prefs.week}주 " +
                "${BookMarkApplication.prefs.notificationBun}번 " +
                "${BookMarkApplication.prefs.notificationGae}개"

        binding.weekNumberPicker.value = BookMarkApplication.prefs.week!!
        binding.bunNumberPicker.value = BookMarkApplication.prefs.notificationBun!!
        binding.gaeNumberPicker.value = BookMarkApplication.prefs.notificationGae!!
        binding.weekNumberPicker.maxValue = 10
        binding.bunNumberPicker.maxValue = 10
        binding.gaeNumberPicker.maxValue = 10

        binding.weekNumberPicker.setOnValueChangedListener { picker, oldVal, newVal ->
            BookMarkApplication.prefs.week = newVal
            binding.countTextView.text = "${newVal}주 " +
                    "${BookMarkApplication.prefs.notificationBun}번 " +
                    "${BookMarkApplication.prefs.notificationGae}개"
        }
        binding.bunNumberPicker.setOnValueChangedListener { picker, oldVal, newVal ->
            BookMarkApplication.prefs.notificationBun = newVal
            binding.countTextView.text = "${BookMarkApplication.prefs.week}주 " +
                    "${newVal}번 " +
                    "${BookMarkApplication.prefs.notificationGae}개"
        }

        binding.gaeNumberPicker.setOnValueChangedListener { picker, oldVal, newVal ->
            BookMarkApplication.prefs.notificationGae = newVal
            binding.countTextView.text = "${BookMarkApplication.prefs.week}주 " +
                    "${BookMarkApplication.prefs.notificationBun}번 " +
                    "${newVal}개"

        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("SetTextI18n")
    private fun initTimePicker() {
        binding.timeTextView.text = "${BookMarkApplication.prefs.amOrPm} ${
            String.format(
                "%02d",
                BookMarkApplication.prefs.hour!!
            )
        }시 " +
                "${String.format("%02d", BookMarkApplication.prefs.minute!!)}분"

        val hour = BookMarkApplication.prefs.hour!!
        binding.timePicker.hour = if (BookMarkApplication.prefs.amOrPm == "오후") hour + 12 else hour
        binding.timePicker.minute = BookMarkApplication.prefs.minute!!
        binding.timePicker.setOnTimeChangedListener { _, hourOfDay, minute ->
            BookMarkApplication.prefs.hour = if (hourOfDay > 12) {
                BookMarkApplication.prefs.amOrPm = "오후"
                binding.timeTextView.text = "오후 ${String.format("%02d", hourOfDay - 12)}시" +
                        " ${String.format("%02d", minute)}분"
                hourOfDay - 12
            } else {
                BookMarkApplication.prefs.amOrPm = "오전"
                binding.timeTextView.text = "오전 ${String.format("%02d", hourOfDay)}시" +
                        " ${String.format("%02d", minute)}분"
                hourOfDay
            }
            BookMarkApplication.prefs.minute = minute
        }
    }

    private fun updateRecyclerView() {
        viewModel.readAllData.observe(viewLifecycleOwner) { list ->
            adapter.setData(
                list.filter {
                    (expanded && it.title.isNotBlank() && it.isLink == 0)
                            || (it.isLink == 0 && it.isRemind)
                },
                expanded
            )
        }
    }
}
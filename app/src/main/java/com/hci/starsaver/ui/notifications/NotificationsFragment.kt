package com.hci.starsaver.ui.notifications

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.hci.starsaver.R
import com.hci.starsaver.config.BookMarkApplication
import com.hci.starsaver.data.bookMark.BookMark
import com.hci.starsaver.databinding.FragmentNotificationsBinding
import com.hci.starsaver.ui.editlink.EditLinkActivity
import com.hci.starsaver.ui.home.HomeFragment
import com.hci.starsaver.ui.home.HomeViewModel
import com.hci.starsaver.util.RemindFolderAdapter
import kotlinx.coroutines.launch
import java.util.*

class NotificationsFragment : Fragment() {

    private lateinit var binding: FragmentNotificationsBinding
    private lateinit var viewModel: HomeViewModel
    private lateinit var adapter: RemindFolderAdapter
    private var expanded = false
    private var timePicking = false
    private var numberPicking = false
    private var week = 0
    private var bun = 0
    private var gae = 0
    private var hour = 1
    private var minute = 0
    private var amOrPm = 0
    private lateinit var tempSet: MutableSet<BookMark>

    private lateinit var bookmarkList:MutableList<BookMark>
    private lateinit var notificationSet:MutableSet<BookMark>

    private val channelID = "testChannel"
    private var notificationManager: NotificationManager? = null

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        viewModel = HomeFragment.viewModel

        tempSet = mutableSetOf()
        bookmarkList = mutableListOf()
        notificationSet = mutableSetOf()
        backButton()
        initLayout()
        initButtons()
        //updateRecyclerView()

        return binding.root
    }

    private fun backButton() {
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initButtons() {
        binding.isAllFolderSwitchButton.setOnClickListener {
            expanded = true
            binding.isAllFolderSwitchButton.visibility = View.GONE
            binding.buttonLayout.visibility = View.VISIBLE
            binding.saveButton.isClickable = true
            binding.cancelButton.isClickable = true
            binding.testButtonLayout.visibility = View.GONE
            showList()
        }

        binding.cancelButton.setOnClickListener {
            if (numberPicking) {
                binding.countTextView.text = "${BookMarkApplication.prefs.week}주     " +
                        "${BookMarkApplication.prefs.notificationBun}번     " +
                        "${BookMarkApplication.prefs.notificationGae}개"
                week = BookMarkApplication.prefs.week!!
                bun = BookMarkApplication.prefs.notificationBun!!
                gae = BookMarkApplication.prefs.notificationGae!!
                numberPicking = false
                binding.countTextView.background = null
                binding.numberPickerLayout.visibility = View.GONE
                binding.buttonLayout.visibility = View.GONE
                binding.saveButton.isClickable = false
                binding.cancelButton.isClickable = false
                binding.isAllFolderSwitchButton.visibility = View.VISIBLE
                binding.testButtonLayout.visibility = View.VISIBLE
            }
            if (timePicking) {
                binding.timeTextView.text = "${BookMarkApplication.prefs.amOrPm}    " +
                        "${String.format("%02d", BookMarkApplication.prefs.hour!!)}시    " +
                        "${String.format("%02d", BookMarkApplication.prefs.minute!!)}분"
                hour = BookMarkApplication.prefs.hour!!
                minute = BookMarkApplication.prefs.minute!!
                if (BookMarkApplication.prefs.amOrPm == "오전") {
                    amOrPm = 0
                } else {
                    amOrPm = 1
                }
                timePicking = false
                binding.timeTextView.background = null
                binding.timePickerLayout.visibility = View.GONE
                binding.buttonLayout.visibility = View.GONE
                binding.saveButton.isClickable = false
                binding.cancelButton.isClickable = false
                binding.isAllFolderSwitchButton.visibility = View.VISIBLE
                binding.testButtonLayout.visibility = View.VISIBLE
            }
            if (expanded) {
                expanded = false
                binding.isAllFolderSwitchButton.visibility = View.VISIBLE
                binding.buttonLayout.visibility = View.GONE
                binding.saveButton.isClickable = false
                binding.cancelButton.isClickable = false
                binding.testButtonLayout.visibility = View.VISIBLE
                showList()
            }
        }

        binding.saveButton.setOnClickListener {
            if (numberPicking) {
                BookMarkApplication.prefs.week = week
                BookMarkApplication.prefs.notificationBun = bun
                BookMarkApplication.prefs.notificationGae = gae
                numberPicking = false
                binding.countTextView.background = null
                binding.numberPickerLayout.visibility = View.GONE
                binding.buttonLayout.visibility = View.GONE
                binding.saveButton.isClickable = false
                binding.cancelButton.isClickable = false
                binding.isAllFolderSwitchButton.visibility = View.VISIBLE
                binding.testButtonLayout.visibility = View.VISIBLE
            }
            if (timePicking) {
                BookMarkApplication.prefs.hour = hour
                BookMarkApplication.prefs.minute = minute
                if (amOrPm == 0) {
                    BookMarkApplication.prefs.amOrPm = "오전"
                } else {
                    BookMarkApplication.prefs.amOrPm = "오후"
                }
                timePicking = false
                binding.timeTextView.background = null
                binding.timePickerLayout.visibility = View.GONE
                binding.buttonLayout.visibility = View.GONE
                binding.saveButton.isClickable = false
                binding.cancelButton.isClickable = false
                binding.isAllFolderSwitchButton.visibility = View.VISIBLE
                binding.testButtonLayout.visibility = View.VISIBLE
            }
            if (expanded) {
                for (folder in tempSet) {
                    viewModel.addBookMark(folder)
                    if (folder.id == 0L) {
                        BookMarkApplication.prefs.homeIsRemind = folder.isRemind
                    }
                }
                expanded = false
                binding.isAllFolderSwitchButton.visibility = View.VISIBLE
                binding.buttonLayout.visibility = View.GONE
                binding.saveButton.isClickable = false
                binding.cancelButton.isClickable = false
                binding.testButtonLayout.visibility = View.VISIBLE
                showList()
            }
        }

        binding.testButton.setOnClickListener {
            randomSelection()
            notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            createChannel(channelID, "TestChannel", "this is a test")

            var id = 1
            for (bm in notificationSet){
                createNotification(bm, id)
                id++
            }
            id = 0

            notificationSet.clear()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("SetTextI18n", "ResourceAsColor")
    private fun initLayout() {
        adapter = RemindFolderAdapter()
        adapter.onCheckedListener = object : RemindFolderAdapter.OnCheckedListener {
            override fun onItemClicked(folder: BookMark, view: View) {
                tempSet.add(folder.apply { isRemind = (view as CheckBox).isChecked })
            }
        }
        binding.remindFolderRecyclerView.adapter = adapter
        binding.remindFolderRecyclerView.layoutManager =
            LinearLayoutManager(this.context, LinearLayoutManager.VERTICAL, false)

        showList()
        initTimePicker2()
        initNumberPicker()

        binding.countTextView.setOnClickListener {
            if (numberPicking) {
                numberPicking = false
                it.background = null
                binding.countTextView.text = "${BookMarkApplication.prefs.week}주     " +
                        "${BookMarkApplication.prefs.notificationBun}번     " +
                        "${BookMarkApplication.prefs.notificationGae}개"
                week = BookMarkApplication.prefs.week!!
                bun = BookMarkApplication.prefs.notificationBun!!
                gae = BookMarkApplication.prefs.notificationGae!!
                binding.numberPickerLayout.visibility = View.GONE
                binding.buttonLayout.visibility = View.GONE
                binding.saveButton.isClickable = false
                binding.cancelButton.isClickable = false
                binding.testButtonLayout.visibility = View.VISIBLE
            } else {
                numberPicking = true
                timePicking = false
                it.setBackgroundResource(R.drawable.gray_corner_background)
                binding.timeTextView.background = null
                binding.numberPickerLayout.visibility = View.VISIBLE
                binding.timePickerLayout.visibility = View.GONE
                binding.buttonLayout.visibility = View.VISIBLE
                binding.saveButton.isClickable = true
                binding.cancelButton.isClickable = true
                binding.weekNumberPicker.value = BookMarkApplication.prefs.week!!
                binding.bunNumberPicker.value = BookMarkApplication.prefs.notificationBun!!
                binding.gaeNumberPicker.value = BookMarkApplication.prefs.notificationGae!!
                binding.testButtonLayout.visibility = View.GONE
            }
        }
        binding.timeTextView.setOnClickListener {
            if (timePicking) {
                timePicking = false
                it.background = null
                binding.timeTextView.text = "${BookMarkApplication.prefs.amOrPm}    " +
                        "${String.format("%02d", BookMarkApplication.prefs.hour!!)}시    " +
                        "${String.format("%02d", BookMarkApplication.prefs.minute!!)}분"
                hour = BookMarkApplication.prefs.hour!!
                minute = BookMarkApplication.prefs.minute!!
                if (BookMarkApplication.prefs.amOrPm == "오전") {
                    amOrPm = 0
                } else {
                    amOrPm = 1
                }
                binding.timePickerLayout.visibility = View.GONE
                binding.buttonLayout.visibility = View.GONE
                binding.saveButton.isClickable = false
                binding.cancelButton.isClickable = false
                binding.testButtonLayout.visibility = View.VISIBLE
            } else {
                timePicking = true
                numberPicking = false
                it.setBackgroundResource(R.drawable.gray_corner_background)
                binding.countTextView.background = null
                binding.numberPickerLayout.visibility = View.GONE
                binding.timePickerLayout.visibility = View.VISIBLE
                binding.buttonLayout.visibility = View.VISIBLE
                binding.saveButton.isClickable = true
                binding.cancelButton.isClickable = true
                binding.hourNumberPicker.value = BookMarkApplication.prefs.hour!!
                binding.minuteNumberPicker.value = BookMarkApplication.prefs.minute!!
                binding.testButtonLayout.visibility = View.GONE
                if (BookMarkApplication.prefs.amOrPm!! == "오전") {
                    binding.amOrPmNumberPicker.value = 0
                } else {
                    binding.amOrPmNumberPicker.value = 1
                }
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
        binding.countTextView.text = "${BookMarkApplication.prefs.week}주     " +
                "${BookMarkApplication.prefs.notificationBun}번     " +
                "${BookMarkApplication.prefs.notificationGae}개"

        binding.weekNumberPicker.value = BookMarkApplication.prefs.week!!
        binding.bunNumberPicker.value = BookMarkApplication.prefs.notificationBun!!
        binding.gaeNumberPicker.value = BookMarkApplication.prefs.notificationGae!!
        binding.weekNumberPicker.maxValue = 10
        binding.bunNumberPicker.maxValue = 10
        binding.gaeNumberPicker.maxValue = 10

        week = BookMarkApplication.prefs.week!!
        bun = BookMarkApplication.prefs.notificationBun!!
        gae = BookMarkApplication.prefs.notificationGae!!

        binding.weekNumberPicker.setOnValueChangedListener { picker, oldVal, newVal ->
            week = newVal
            binding.countTextView.text = "${newVal}주     " +
                    "${bun}번     " +
                    "${gae}개"
        }
        binding.bunNumberPicker.setOnValueChangedListener { picker, oldVal, newVal ->
            bun = newVal
            binding.countTextView.text = "${week}주     " +
                    "${newVal}번     " +
                    "${gae}개"
        }

        binding.gaeNumberPicker.setOnValueChangedListener { picker, oldVal, newVal ->
            gae = newVal
            binding.countTextView.text = "${week}주     " +
                    "${bun}번     " +
                    "${newVal}개"

        }
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.M)
    private fun initTimePicker2() {
        binding.timeTextView.text = "${BookMarkApplication.prefs.amOrPm}    " +
                "${String.format("%02d", BookMarkApplication.prefs.hour!!)}시    " +
                "${String.format("%02d", BookMarkApplication.prefs.minute!!)}분"


        binding.hourNumberPicker.minValue = 1
        binding.minuteNumberPicker.minValue = 0
        binding.amOrPmNumberPicker.minValue = 0
        binding.hourNumberPicker.maxValue = 12
        binding.minuteNumberPicker.maxValue = 59
        binding.amOrPmNumberPicker.maxValue = 1
        binding.amOrPmNumberPicker.displayedValues = arrayOf("오전", "오후")
        binding.hourNumberPicker.value = BookMarkApplication.prefs.hour!!
        binding.minuteNumberPicker.value = BookMarkApplication.prefs.minute!!
        if (BookMarkApplication.prefs.amOrPm == "오전") {
            binding.amOrPmNumberPicker.value = 0
        } else {
            binding.amOrPmNumberPicker.value = 1
        }

        hour = binding.hourNumberPicker.value
        minute = binding.minuteNumberPicker.value
        amOrPm = binding.amOrPmNumberPicker.value


        binding.hourNumberPicker.setOnValueChangedListener { picker, oldVal, newVal ->
            hour = newVal
            binding.timeTextView.text = "${
                if (amOrPm == 0) {
                    "오전"
                } else {
                    "오후"
                }
            }    " +
                    "${String.format("%02d", newVal)}시    " +
                    "${String.format("%02d", minute)}분"
        }
        binding.minuteNumberPicker.setOnValueChangedListener { picker, oldVal, newVal ->
            minute = newVal
            binding.timeTextView.text = "${
                if (amOrPm == 0) {
                    "오전"
                } else {
                    "오후"
                }
            }    " +
                    "${String.format("%02d", hour)}시    " +
                    "${String.format("%02d", newVal)}분"
        }

        binding.amOrPmNumberPicker.setOnValueChangedListener { picker, oldVal, newVal ->
            amOrPm = newVal
            binding.timeTextView.text = "${
                if (newVal == 0) {
                    "오전"
                } else {
                    "오후"
                }
            }    " +
                    "${String.format("%02d", hour)}시    " +
                    "${String.format("%02d", minute)}분"

        }
    }

    private fun showList() {
        lifecycleScope.launch {
            val list = viewModel.readAllData.value
            adapter.setData(
                list!!.filter {
                    (expanded && it.title.isNotBlank() && it.isLink == 0)
                            || (it.isLink == 0 && it.isRemind)
                },
                expanded
            )
        }
    }

    private fun randomSelection() {
        val random = Random()
        HomeFragment.viewModel.readAllData.observe(viewLifecycleOwner) { it ->
            HomeFragment.viewModel.getStarCount()
            it.forEach {
                if(it.isRemind && it.isLink == 1){
                    bookmarkList.add(it)
                }
            }
        }

        var sizeOfSet : Int = if(BookMarkApplication.prefs.notificationGae!! > bookmarkList.size){
            bookmarkList.size
        } else {
            BookMarkApplication.prefs.notificationGae!!
        }

        while (sizeOfSet != notificationSet.size){
            val num = random.nextInt(bookmarkList.size)
            notificationSet.add(bookmarkList[num])
        }
    }

    private fun createChannel(id: String, name: String, channelDescription: String) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(id, name, importance).apply {
                description = channelDescription
            }
            notificationManager?.createNotificationChannel(channel)
        }else{

        }
    }

    private fun createNotification(link : BookMark, id : Int) {
        val intent = Intent(this.context, EditLinkActivity::class.java).apply {

        }
        intent.putExtra("BookMark", link.copy(bitmap = null))

        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this.context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification: Notification = NotificationCompat.Builder(requireContext(), channelID)
            .setContentTitle("잊혀졌던 소중한 별을 만나보세요.")
            .setContentText(link.title)
            .setAutoCancel(true)
            .setSmallIcon(com.hci.starsaver.R.drawable.icon)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager?.notify(id, notification)
    }
}
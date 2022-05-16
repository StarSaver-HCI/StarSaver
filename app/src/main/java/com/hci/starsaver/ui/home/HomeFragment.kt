package com.hci.starsaver.ui.home

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.webkit.URLUtil
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.hci.starsaver.*
import com.hci.starsaver.data.bookMark.BookMark
import com.hci.starsaver.databinding.FragmentHomeBinding
import com.hci.starsaver.ui.addfolder.AddFolderActivity
import com.hci.starsaver.ui.addlink.AddLinkActivity
import com.hci.starsaver.ui.editfolder.EditFolderActivity
import com.hci.starsaver.ui.editlink.EditLinkActivity
import com.hci.starsaver.util.FolderAdapter
import com.hci.starsaver.util.LinkAdapter


class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    companion object {
        lateinit var viewModel: HomeViewModel
    }

    private lateinit var callback: OnBackPressedCallback
    private val folderAdapter = FolderAdapter()
    private val linkAdapter = LinkAdapter()
    private var isFabOpen = false
    private var isRoot = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        initViewModel()
        initButtons()
        initLayout()
        return binding.root
    }

    @SuppressLint("ResourceType", "SetTextI18n")
    private fun initViewModel() {
        viewModel.readAllData.observe(viewLifecycleOwner) { it ->
            viewModel.getStarCount()
            if (viewModel.sortByName.value!!) {
                val folderList = ArrayDeque<BookMark>()
                val linkList = ArrayDeque<BookMark>()
                val starLinkList = ArrayDeque<BookMark>()
                it.forEach {
                    if (it.parentId == viewModel.currentBookMark.value!!.id && it.isLink == 0) {
                        folderList.addLast(it)
                    } else if (it.parentId == viewModel.currentBookMark.value!!.id && it.isLink == 1) {
                        if (it.isStar) starLinkList.addFirst(it)
                        else linkList.add(it)
                    }
                }
                starLinkList.forEach { linkList.addFirst(it) }
                folderAdapter.setData(folderList)
                linkAdapter.setData(linkList)
                binding.sizeTextView.text = "${linkAdapter.currentList.size}개의 별이 담겨있어요"
                binding.emptyTextView.visibility =
                    if (folderList.size + linkList.size == 0) View.VISIBLE else View.GONE
            }
        }

        viewModel.readAllDataByName.observe(viewLifecycleOwner) { it ->
            viewModel.getStarCount()
            if (viewModel.sortByName.value!!.not()) {
                val folderList = ArrayDeque<BookMark>()
                val linkList = ArrayDeque<BookMark>()
                val starLinkList = ArrayDeque<BookMark>()
                it.forEach {
                    if (it.parentId == viewModel.currentBookMark.value!!.id && it.isLink == 0) {
                        folderList.addLast(it)
                    } else if (it.parentId == viewModel.currentBookMark.value!!.id && it.isLink == 1) {
                        if (it.isStar) starLinkList.addFirst(it)
                        else linkList.add(it)
                    }
                }
                starLinkList.forEach { linkList.addFirst(it) }
                folderAdapter.setData(folderList)
                linkAdapter.setData(linkList)
                binding.sizeTextView.text = "${linkAdapter.currentList.size}개의 별이 담겨있어요"
                binding.emptyTextView.visibility =
                    if (folderList.size + linkList.size == 0) View.VISIBLE else View.GONE
            }
        }

        viewModel.currentBookMark.observe(viewLifecycleOwner) {
            updateUI(it)
        }

        viewModel.sortByName.observe(viewLifecycleOwner) {
            binding.sortTypeSwitchButton.isChecked = it
            binding.sortTypeTextView.text = if (it) "만든 날짜 순" else "이름 순"
            reloadList()
        }

        viewModel.getStarCount().observe(viewLifecycleOwner) {
            if (isRoot)
                binding.titleTextView.text = "나의 우주 속\n${it}개의 별"
        }
    }

    @SuppressLint("ResourceAsColor")
    private fun updateUI(it: BookMark) {
        if (it.id != 0L && it.isLink == 0) {
            isRoot = false
            binding.topView.setBackgroundResource(R.color.gray_cc)
            binding.pathTextView.visibility = View.VISIBLE
            binding.backButton.visibility = View.VISIBLE
            binding.folderImageView.visibility = View.VISIBLE
            binding.sizeTextView.visibility = View.VISIBLE
            binding.titleTextView.text = it.title
            binding.titleTextView.setTextColor(resources.getColor(R.color.black))
            binding.pathTextView.text = viewModel.getPath()
            animateHeightTo(binding.topView, 320)
            animateHeightPosition(binding.searchLayout, -280f)
        } else {
            isRoot = true
            binding.topView.setBackgroundResource(R.drawable.topview_image)
            binding.sizeTextView.visibility = View.GONE
            binding.pathTextView.visibility = View.GONE
            binding.backButton.visibility = View.GONE
            binding.folderImageView.visibility = View.GONE
            binding.titleTextView.setTextColor(resources.getColor(R.color.white))
            animateHeightTo(binding.topView, 475)
            animateHeightPosition(binding.searchLayout, 0f)
        }
    }

    private fun initButtons() {
        binding.fabButton.setOnClickListener {
            toggleFab()
        }

        binding.addLinkButton.setOnClickListener {
            addLink()
        }

        binding.addFolderButton.setOnClickListener {
            addFolder()
        }

        binding.backButton.setOnClickListener {
            viewModel.popFolder()
            reloadList()
        }

        binding.sortTypeSwitchButton.setOnCheckedChangeListener { _, isChecked ->
            viewModel.sortByName.value = isChecked
        }

        binding.searchImageView.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_searchFragment)
        }

        binding.optionImageView.setOnClickListener {
            val intent = Intent(this.context, EditFolderActivity::class.java)
            intent.putExtra("BookMark", viewModel.currentBookMark.value)
            startActivity(intent)
        }
    }

    private fun addFolder() {
        val intent = Intent(this.context, AddFolderActivity::class.java)
        intent.putExtra("id", viewModel.currentBookMark.value!!.id)
        startActivity(intent)
    }

    private fun addLink() {
        val intent = Intent(this.context, AddLinkActivity::class.java)
        intent.putExtra("id", viewModel.currentBookMark.value!!.id)
        startActivity(intent)
    }

    private fun toggleFab() {
        // 플로팅 액션 버튼 닫기 - 열려있는 플로팅 버튼 집어넣는 애니메이션
        if (isFabOpen) {
            ObjectAnimator.ofFloat(binding.addFolderButton, "translationY", 0f).apply { start() }
            ObjectAnimator.ofFloat(binding.addLinkButton, "translationY", 0f).apply { start() }
            ObjectAnimator.ofFloat(binding.fabButton, View.ROTATION, 45f, 0f).apply { start() }
            val ani = AlphaAnimation(1.0f, 0.0f)
            ani.setDuration(500)
            binding.bookmarkAdd.visibility = View.GONE
            binding.bookmarkAdd.setAnimation(ani)
            binding.folderAdd.visibility = View.GONE
            binding.folderAdd.setAnimation(ani)
        } else { // 플로팅 액션 버튼 열기 - 닫혀있는 플로팅 버튼 꺼내는 애니메이션
            ObjectAnimator.ofFloat(binding.addFolderButton, "translationY", -360f).apply { start() }
            ObjectAnimator.ofFloat(binding.addLinkButton, "translationY", -180f).apply { start() }
            ObjectAnimator.ofFloat(binding.fabButton, View.ROTATION, 0f, 45f).apply { start() }
            val ani = AlphaAnimation(0.0f, 1.0f)
            ani.setDuration(500)
            binding.bookmarkAdd.visibility = View.VISIBLE
            binding.bookmarkAdd.setAnimation(ani)
            binding.folderAdd.visibility = View.VISIBLE
            binding.folderAdd.setAnimation(ani)
        }
        isFabOpen = !isFabOpen
    }

    private fun reloadList() {
        viewModel.addBookMark(BookMark(-1, -1, "", "", -1, ""))
        viewModel.deleteBookMark(BookMark(-1, -1, "", "", -1, ""))
    }

    private fun initLayout() {
        isFabOpen = false
        binding.linkRecyclerView.adapter = linkAdapter
        binding.folderRecyclerView.adapter = folderAdapter

        folderAdapter.setOnFolderClickedListener {
            Log.d("t", "${it.id}, ${it.parentId}")
            viewModel.moveFolder(it)
            reloadList()
        }

        linkAdapter.setOnLinkClickedListener {
            if (URLUtil.isValidUrl(it.link)) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it.link)))
            } else {
                Toast.makeText(this.context, getString(R.string.checkUrl), Toast.LENGTH_SHORT)
                    .show()
            }
        }

        linkAdapter.setOnDetailClickedListener {
            val intent = Intent(this.context, EditLinkActivity::class.java)
            intent.putExtra("BookMark", it.copy(bitmap = null))
            startActivity(intent)
        }

        binding.folderRecyclerView.layoutManager = object : GridLayoutManager(this.context, 4) {
            override fun canScrollVertically(): Boolean {
                return false
            }
        }

        binding.linkRecyclerView.layoutManager = object : LinearLayoutManager(this.context) {
            override fun canScrollVertically(): Boolean {
                return false
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (viewModel.bmStack.value!!.size > 1) {
                    viewModel.popFolder()
                    reloadList()
                } else {
                    activity?.finish()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    private fun animateHeightTo(view: View, height: Int) {
        val anim = ValueAnimator.ofInt(view.height, height)
        anim.setDuration(300)
            .addUpdateListener {
                val value = it.animatedValue
                view.layoutParams.height = value as Int
                view.requestLayout()
            }
        anim.start()
    }

    private fun animateHeightPosition(view: View, height: Float) {
        ObjectAnimator.ofFloat(view, "translationY", height).apply { start() }
    }
}
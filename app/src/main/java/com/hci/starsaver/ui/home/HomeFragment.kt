package com.hci.starsaver.ui.home

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.view.animation.AlphaAnimation
import android.webkit.URLUtil
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.hci.starsaver.R
import com.hci.starsaver.data.bookMark.BookMark
import com.hci.starsaver.databinding.DialogRemoveBookmarkBinding
import com.hci.starsaver.databinding.FragmentHomeBinding
import com.hci.starsaver.ui.addfolder.AddFolderActivity
import com.hci.starsaver.ui.addlink.AddLinkActivity
import com.hci.starsaver.ui.editfolder.EditFolderActivity
import com.hci.starsaver.ui.editlink.EditLinkActivity
import com.hci.starsaver.util.FolderAdapter
import com.hci.starsaver.util.LinkAdapter
import com.hci.starsaver.util.PathAdapter
import kotlinx.coroutines.launch


class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    companion object {
        lateinit var viewModel: HomeViewModel
    }

    private lateinit var list: MutableList<BookMark>
    private lateinit var callback: OnBackPressedCallback
    private val folderAdapter = FolderAdapter()
    private val linkAdapter = LinkAdapter()
    private val pathAdapter = PathAdapter()
    private var isFabOpen = false
    private var isRoot = true
    private var popUpObject: PopupMenu.OnMenuItemClickListener? = null

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
        initPathLayout()

        return binding.root
    }

    @SuppressLint("ResourceType", "SetTextI18n")
    private fun initViewModel() {
        list = mutableListOf()
        viewModel.readAllData.observe(viewLifecycleOwner) { it ->
            viewModel.getStarCount()
            if (viewModel.sortByName.value!!) {
                val folderList = ArrayDeque<BookMark>()
                val linkList = ArrayDeque<BookMark>()
                val starLinkList = ArrayDeque<BookMark>()
                it.forEach {
                    list.add(it)
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
                binding.sizeTextView.text = "${linkList.size}개의 별이 담겨있어요"
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

    @SuppressLint("ResourceAsColor", "NotifyDataSetChanged")
    private fun updateUI(it: BookMark) {
        if (it.id != 0L && it.isLink == 0) {
            isRoot = false
            binding.topView.setBackgroundResource(R.drawable.topview_image2)
            binding.sizeTextView.visibility = View.VISIBLE
            binding.titleTextView.text = it.title
            pathAdapter.submitList(viewModel.getPathList())
            binding.pathRecyclerView.visibility = View.VISIBLE
        } else {
            isRoot = true // 루트
            binding.topView.setBackgroundResource(R.drawable.topview_image)
            binding.sizeTextView.visibility = View.GONE
            binding.pathRecyclerView.visibility = View.GONE
        }

    }

    private fun initButtons() {
        binding.fabButton.setOnClickListener {
            toggleFab()
        }

        binding.addLinkButton.setOnClickListener {
            toggleFab()
            addLink()

        }

        binding.addFolderButton.setOnClickListener {
            toggleFab()
            addFolder()
        }

        binding.sortTypeSwitchButton.setOnCheckedChangeListener { _, isChecked ->
            viewModel.sortByName.value = isChecked
        }

        binding.searchImageView.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_searchFragment)
        }

        binding.optionImageView.setOnClickListener {
//            val intent = Intent(this.context, EditFolderActivity::class.java)
//            intent.putExtra("BookMark", viewModel.currentBookMark.value)
//            startActivity(intent)
            showPopup(it)
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

    private fun initPathLayout() {
        pathAdapter.setOnClickedListener {
            for (i in 0 until it) {
                viewModel.popFolder()
                reloadList()
            }
        }
        binding.pathRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.pathRecyclerView.adapter = pathAdapter

        popUpObject = PopupMenu.OnMenuItemClickListener { item ->
            when (item?.itemId) {
                R.id.folderRemoveButton -> {
                    showDialog()
                }
                R.id.folderEditButton -> {
                    val intent = Intent(this.context, EditFolderActivity::class.java)
                    intent.putExtra("BookMark", viewModel.currentBookMark.value)
                    startActivity(intent)
                }
                R.id.folderShareButton -> {
                    sendToExternal(viewModel.currentBookMark.value!!)
                }
            }
            item != null
        }
    }

    // 메뉴 보여주는 함수
    private fun showPopup(v: View) {
        val popup = PopupMenu(requireContext(), v)
        popup.setOnMenuItemClickListener(popUpObject)
        popup.menuInflater.inflate(
            if (viewModel.currentBookMark.value!!.id == 0L) R.menu.home_folder_menu else R.menu.folder_menu,
            popup.menu
        )
        popup.show()
    }

    // 삭제 다이얼로그
    private fun showDialog() {
        var removeDialog = Dialog(requireContext())
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
            lifecycleScope.launch {
                val idList = mutableListOf(viewModel.currentBookMark.value!!.id)
                viewModel.readAllData.value!!.forEach {
                    if (idList.contains(it.parentId)) {
                        viewModel.deleteBookMark(it)
                        idList.add(it.id)
                    }
                }
                viewModel.deleteBookMark(viewModel.currentBookMark.value!!)
                viewModel.popFolder()
                reloadList()
                removeDialog.dismiss()
            }
        }

        removeDialog.window?.setGravity(Gravity.BOTTOM)
        removeDialog.show()


//        var addLinkDialog = Dialog(requireContext())
//        var dialogView = DalogRemoveBinding.inflate(layoutInflater)
//        dialogView.cancelButton.setOnClickListener {
//            addLinkDialog.dismiss()
//        }
//        dialogView.removeButton.setOnClickListener {
//            viewModel.deleteBookMark(viewModel.currentBookMark.value!!)
//            viewModel.popFolder()
//            reloadList()
//            addLinkDialog.dismiss()
//        }
//        addLinkDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        addLinkDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
//        addLinkDialog.setContentView(dialogView.root)
//        addLinkDialog.show()
    }

    // 외부로 공유하기
    private fun sendToExternal(bm: BookMark) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            val text = makeTree("Shared by StarSaver\n\n" + bm.title + "[folder]", bm.id!!, 0)
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    // 트리를 만드는 함수
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
}
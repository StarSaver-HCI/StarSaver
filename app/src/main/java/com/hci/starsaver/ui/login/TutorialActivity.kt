package com.hci.starsaver.ui.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.hci.starsaver.MainActivity
import com.hci.starsaver.databinding.ActivityTutorialBinding


class TutorialActivity : AppCompatActivity() {

    lateinit var binding : ActivityTutorialBinding


    private var viewPager: ViewPager? = null
    private var pagerAdapter: PagerAdapter? = null
    private var dotsLayout: LinearLayout? = null
    private var dots: Array<TextView?>? = null
    var layouts: IntArray? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTutorialBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewPager = binding.viewPager
        dotsLayout = binding.layoutDots


        layouts = intArrayOf(
            com.hci.starsaver.R.layout.tutorial1,
            com.hci.starsaver.R.layout.tutorial2,
            com.hci.starsaver.R.layout.tutorial3,
            com.hci.starsaver.R.layout.tutorial4
        )

        addBottomDots(0)

        pagerAdapter = PagerAdapter()
        viewPager!!.setAdapter(pagerAdapter)
        viewPager!!.addOnPageChangeListener(viewPagerPageChangeListener)
        binding.startButton.setOnClickListener {
            val current: Int = getItem(+1)
            if (current == layouts!!.size) {
                val intent = Intent(this, LoadingActivity::class.java)
                startActivity(intent)
                finish()
            }
        }



    }

    private fun addBottomDots(currentPage: Int) {
        dots = arrayOfNulls(layouts!!.size) // 레이아웃 크기만큼 하단 점 배열에 추가
        val colorsActive = resources.getIntArray(com.hci.starsaver.R.array.array_dot_active)
        val colorsInactive = resources.getIntArray(com.hci.starsaver.R.array.array_dot_inactive)
        dotsLayout!!.removeAllViews()
        for (i in 0 until dots!!.size) {
            dots!![i] = TextView(this)
            dots!![i]!!.text = Html.fromHtml("&#8226;")
            dots!![i]!!.setTextSize(35F)
            dots!![i]!!.setTextColor(colorsInactive[currentPage])
            dotsLayout!!.addView(dots!![i])
        }
        if (dots!!.size > 0) dots!![currentPage]!!.setTextColor(colorsActive[currentPage])
    }

    private fun getItem(i: Int): Int {
        return viewPager!!.currentItem + i
    }

    var viewPagerPageChangeListener: OnPageChangeListener = object : OnPageChangeListener {
        override fun onPageSelected(position: Int) {
            addBottomDots(position)
        }

        override fun onPageScrolled(arg0: Int, arg1: Float, arg2: Int) {}
        override fun onPageScrollStateChanged(arg0: Int) {}
    }



    inner class PagerAdapter : androidx.viewpager.widget.PagerAdapter() {
        private var layoutInflater: LayoutInflater? = null
        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            layoutInflater =
                getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater?
            val view: View = layoutInflater!!.inflate(layouts!!.get(position), container, false)
            container.addView(view)
            return view
        }

        override fun getCount(): Int {
            return layouts!!.size
        }

        override fun isViewFromObject(view: View, obj: Any): Boolean {
            return view === obj
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            val view: View = `object` as View
            container.removeView(view)
        }
    }
}
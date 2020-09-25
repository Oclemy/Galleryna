package info.camposha.galleryna

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import info.camposha.galleryna.Utils.loadBackground

class ViewPagerActivity : AppCompatActivity() {
    private var urls: Array<String>? = arrayOf()
    private var clickedUrl: String? = null
    private var mPager: ViewPager? = null

    private fun getImgPosition(url: String): Int {
        for ((pos, u) in urls!!.withIndex()) {
            if (u.equals(url, ignoreCase = true)) {
                return pos
            }
        }
        return -1
    }

    private fun receiveData() {
        urls = intent.getStringArrayExtra(ALL_IMAGES_KEY)
        clickedUrl = intent.getStringExtra(CLICKED_IMAGE_KEY)
    }

    private fun setupViewPager() {
        mPager = findViewById<View>(R.id.pager) as ViewPager
        mPager!!.pageMargin = (resources.displayMetrics.density * 15).toInt()
        if (urls != null && urls!!.isNotEmpty()) {
            mPager!!.adapter = object : PagerAdapter() {
                override fun getCount(): Int {
                    return urls!!.size
                }

                override fun isViewFromObject(view: View, `object`: Any): Boolean {
                    return view === `object`
                }

                override fun instantiateItem(container: ViewGroup, position: Int): Any {
                    val layout = LayoutInflater.from(container.context)
                        .inflate(R.layout.slide, container, false)
                    val view: PhotoView = layout.findViewById(R.id.photoView)
                    //PhotoView view = new PhotoView(ViewPagerActivity.this);
                    view.enable()
                    view.scaleType = ImageView.ScaleType.FIT_CENTER
                    loadBackground(urls!![position], view)
                    val posTV = layout.findViewById<TextView>(R.id.pageTV)
                    val pos = (position + 1).toString()
                    posTV.text = pos + "/" + urls!!.size
                    container.addView(layout)
                    return layout
                }

                override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
                    container.removeView(`object` as View)
                }
            }
            if (clickedUrl != null && clickedUrl!!.length > 0) {
                mPager!!.currentItem = getImgPosition(clickedUrl!!)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_viewpager)

        this.receiveData()
        this.setupViewPager()
    }

    companion object {
    }
}
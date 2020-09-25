package info.camposha.galleryna

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.activity_main.*

const val ALL_IMAGES_KEY= "ALL_IMAGES_KEY"
const val CLICKED_IMAGE_KEY= "CLICKED_IMAGE_KEY"

open class GalleryActivity : AppCompatActivity() {

    private fun photos() = (169..216)
        .map { "https://picsum.photos/1000/700?image=$it" }

    private fun setupAdapter() {
        rv.apply {
            layoutManager = GridLayoutManager(context, 4)
            adapter = ImageAdapter(CacheManager.IMAGE_URLS.toList(), ::goToDetails)
        }
    }

    private fun goToDetails(url: String, imageView: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this,
                imageView,
                imageView.transitionName
            ).toBundle()
//            Intent(this, DetailActivity::class.java)
//                .putExtra(IMAGE_URL_KEY, url)
//                .let {
//                    startActivity(it, options)
//                }
            val i = Intent(this, ViewPagerActivity::class.java)
            i.putExtra("ALL_IMAGES_KEY", photos().toTypedArray())
            i.putExtra("CLICKED_IMAGE_KEY", url)
            startActivity(i)
        } else {
            val i = Intent(this, ViewPagerActivity::class.java)
            startActivity(i)
        }

    }

    override fun onResume() {
        super.onResume()
        setupAdapter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }


}
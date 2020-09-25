package info.camposha.galleryna

import android.os.Build
import android.os.Bundle
import android.transition.ChangeBounds
import android.transition.ChangeImageTransform
import android.transition.Fade
import android.transition.TransitionSet
import androidx.appcompat.app.AppCompatActivity
import androidx.core.transition.doOnEnd
import kotlinx.android.synthetic.main.activity_detail.*

class DetailActivity : AppCompatActivity() {

    private val url by lazy {
        intent.getStringExtra(CLICKED_IMAGE_KEY)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            supportPostponeEnterTransition()
            detailImage.transitionName = url
            Utils.loadImg(url, detailImage)
            supportStartPostponedEnterTransition()

            window.sharedElementEnterTransition = TransitionSet()
                .addTransition(ChangeImageTransform())
                .addTransition(ChangeBounds())
                .apply {
                    doOnEnd { Utils.loadImg(url, detailImage) }
                }
        } else {
            Utils.loadImg(url, detailImage)
        }
//        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.enterTransition = Fade().apply {
                excludeTarget(android.R.id.statusBarBackground, true)
                excludeTarget(android.R.id.navigationBarBackground, true)
                excludeTarget(R.id.action_bar_container, true)
            }
        }
        detailImage.setOnClickListener {
//            val i=Intent(this,ViewPagerActivity::class.java)
//            val i = Intent(this, ViewPagerActivity::class.java)
//            startActivity(i)
        }
    }
}

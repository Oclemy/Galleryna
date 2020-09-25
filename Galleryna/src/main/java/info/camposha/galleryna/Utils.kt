package info.camposha.galleryna

import android.graphics.drawable.Drawable
import android.os.Build
import android.widget.ImageView
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition

object Utils {

    @JvmStatic
    fun loadImg(url: String?, imageView: ImageView) {
        if(url.isNullOrEmpty() || imageView==null){
            return
        }
        val requestOptions = RequestOptions.placeholderOf(R.drawable.load_glass).error(R.drawable.gallery_roll)
            .dontTransform()
            .onlyRetrieveFromCache(false)

        Glide.with(imageView).load(url).apply(requestOptions).into(imageView)
    }

    @JvmStatic
    fun loadBackground(imageURL: String?, photoView: PhotoView?) {
        if (imageURL != null && imageURL.isNotEmpty() && photoView != null) {
            Glide.with(photoView.context)
                .load(imageURL)
                .into(object : CustomTarget<Drawable?>() {
                    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                    override fun onResourceReady(resource: Drawable, @Nullable transition: Transition<in Drawable?>?) {
                        photoView.setImageDrawable(resource)
                    }

                    override fun onLoadCleared(@Nullable placeholder: Drawable?) {}
                })
        }
    }
}
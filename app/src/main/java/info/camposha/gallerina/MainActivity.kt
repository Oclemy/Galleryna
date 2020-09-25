package info.camposha.gallerina

import android.os.Bundle
import info.camposha.galleryna.Config
import info.camposha.galleryna.GalleryActivity

class MainActivity : GalleryActivity() {

    private fun photos() = (169..216)
        .map { "https://picsum.photos/1000/700?image=$it" }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Config.setImageURLs(photos().toTypedArray())
    }


}
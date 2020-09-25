package info.camposha.galleryna

object Config {
    @JvmStatic
    fun setImageURLs(imageURLs: Array<String>?) {
        if(!imageURLs.isNullOrEmpty()){
           CacheManager.IMAGE_URLS=imageURLs
        }

    }
}
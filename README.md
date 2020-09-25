![](https://jitpack.io/v/Oclemy/Galleryna.svg)

# Galleryna
A ready made full screen master detail image gallery library with both listing and swipe view. Only one line of code to use it.


## Introduction
It is now easier than ever to include a full image gallery into your application. I always need this capability in most of the projects 
I work on so I decided to wrap it up in a library.

## Features:

1. Gallery listing view using recyclerview.
2. When an image is clicked it automatically opens the detail page.
3. In the detail page the image is zoomable using touch gestures like photoview.
4. The image is also swipeable
5. The image position is automatically shown in a textview in the detail page.
6. This library is very lightweight. The only dependency it uses is Glide to load images efficiently.

## Built with:
1. Androidx
2. Kotlin
3. Glide
4. PhotoView

This library is lite and fast. It uses glide to load and cache images.

### Step 1. Intallation
Add it in your root build.gradle at the end of repositories:

```groovy
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```
Then add the dependency in your module level build.gradle:
```
            dependencies {
	        implementation 'com.github.Oclemy:Galleryna:1.0.0'
	   }
```

## Usage

Using this library is as simple as it can get. 

1. Extend the `GalleyActivity`:

```kotlin
class MainActivity : GalleryActivity() {
```

2. Pass an array of images:

```kotlin
Config.setImageURLs(photos().toTypedArray())
```

Here is the full activity:
```kotlin
class MainActivity : GalleryActivity() {

    private fun photos() = (169..216)
        .map { "https://picsum.photos/1000/700?image=$it" }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Config.setImageURLs(photos().toTypedArray())
    }
}
```

Now in your Android Manifest file make sure you register the following two activities:

```xml
 	<activity
            android:name="info.camposha.galleryna.GalleryActivity"
            android:theme="@style/AppTheme.Fullscreen" />
        <activity
            android:name="info.camposha.galleryna.ViewPagerActivity"
            android:theme="@style/AppTheme.Fullscreen" />
```

And that's it. You will now get a full image gallery. When an image is clicked, a zoomable and swipeable full screen image is opened. The clicked image is selected.

Enjoy.

## Contact
 Email: oclemmi@gmail.com
 Founder of: https://camposha.info( android.camposha.info, flutter.camposha.info, flutter.camposha.info)
 Channel: ProgrammingWizards TV

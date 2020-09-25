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

## Uses
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

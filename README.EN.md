[英文版本更新可能不及时，点此查看中文版本 / English version may not be updated in time, click here for Chinese version.](https://github.com/founchoo/CampusHelper/blob/main/README.md)
<div align="center">

<img src="https://github.com/founchoo/GongYun-for-Android/assets/24630338/3d5c2914-0592-4058-9b54-00e958c62b63" alt="Logo" width="100">

# GongYun
</div>

## Introduction


Welcome to the GitHub open source page for **GongYun**, a third-party academic management system for students at **Hubei University of Technology**.
This is a third-party academic management system running on the **Android** platform with a minimum version of **Android 8.0 (API level 26)**.


This application is based on the Jetpack Compose framework and is written in Kotlin.


This application sends network requests to the official server of the school's academic affairs system by crawling the API.
This application stores sensitive user data locally during operation, which includes: student number, password, year of enrollment, current academic year and semester.
This information is only used to send network requests to the official university server and is not used for any other purpose.


The reason for storing the student number and password is as follows: the school's academic system makes login requests even if the Remember Me option is checked.
Even if the Remember Me option is checked in the login request, the cookie expiration date in the network return is only two hours after the login time.
In order to prevent users from having to repeatedly enter their student number and password to log in and obtain the login status when using the application after the two-hour interval, the application stores them locally.
The application stores them locally, and when a network request fails due to an expired login status, the application automatically performs the login operation for the user.


If you are in the school using the academic system for super star ( Hubei University of Technology , for example , the academic system address : **hbut.jw.chaoxing.com**) , then this open source project may be helpful to you .


## Features at a glance


Currently , the application provides the following functions:


1. View class schedule
   - View other school years and weeks of the class schedule
   - Provide desktop widgets to display the day schedule
   - View schedule notes (e.g., online classes, class setups)
  
2. View scheduled classes


3. Find available classrooms and classrooms for the same period of time.


4. Grade Inquiry
   - Ranking inquiry (including grade, major, class ranking)
   - Ranking histogram
   - Automatic calculation of grade point and arithmetic mean score
   - GPA change curve view
   - Grade screening (academic year, course type)
   - Histogram of grade distribution


## TODO


1. The TabRow for viewing empty classrooms/classrooms on the schedule page needs to be linked with Horizontal Pager. 2.
2. Add "sort by \*\*\*\*\*" function to the results module, such as grades, credits, etc.
3. The reset button in the popup window for switching class schedules on the schedule page is moved to TopAppBar.
4. Encrypted storage of local information
5. First time user guide


## Screenshots


Note: For privacy reasons, we have asterisked certain information that will not appear when you actually use the app.


<img src="https://github.com/founchoo/GongYun-for-Android/assets/24630338/4140a910-2893-4889-ad12-5cd8ca0882c0">


## Download


GitHub release: [Click here](https://github.com/founchoo/CampusHelper/releases/latest) link to the download page, the `.apk` file at the bottom of the page is the installer, download and install it.


Google Play Store: [Click here to go to Google Play Store](https://play.google.com/store/apps/details?id=com.dart.campushelper)


## Participate in development


Feel free to submit an issue or pull request.


## Acknowledgments


- https://stackoverflow.com/ StackOverflow


- https://m3.material.io/ Material Design Guide


- https://developer.android.com/reference/kotlin/androidx/compose/material3/package-summary Official Development Documentation


- https://google.github.io/accompanist/placeholder/ Placeholder for Jetpack Compose


- https://json2kt.com/ Converting `JSON` files to `.kt` files


- https://github.com/harmittaa/KoinExample Retrofit error handling


- https://github.com/patrykandpatrick/vico Chart Library


- https://plugins.jetbrains.com/plugin/18619-svg-to-compose svg to compose




## Open Source License


This project uses Apache License 2.0.

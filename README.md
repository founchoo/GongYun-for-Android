<div align="center">

<img src="https://github.com/founchoo/CampusHelper/assets/24630338/dc2346ad-033f-475f-a533-e4d1f5b5a16d" alt="Logo" width="100">

# 校园助手
</div>

## 简介

欢迎来到校园助手的 Github 开源页面, 这是一款专为**湖北工业大学**在校学生编写的
第三方教务管理系统，运行于 **Android** 平台，支持的最低版本为 **Android 8.0 (API level 26)**

本应用基于 Jetpack Compose 框架开发，使用 Kotlin 语言编写。

本应用通过爬取的 API 与学校教务系统官方服务器进行网络请求的发送，本应用
运行过程中会在本地存储用户敏感数据，这包括：学号、密码、入学年份、当前学年学期。
这些信息仅用于与学校官方服务器进行网络请求的发送，不会被用于其他用途。

存储学号和密码的原因如下：学校教务系统进行登录请求时即使勾选了记住我的选项，
接收到的网络返回中的 cookie 到期日期仅为登录时刻之后的两小时，
为了避免用户在间隔两小时之后使用本应用时需要重复输入学号和密码进行登录获取登录状态，
本应用将它们存储在本地，当网络请求由于登录状态失效而失败时，本应用将会自动为用户进行登录操作。

如果您所在的学校使用的教务系统为超星（以湖北工业大学为例，教务系统地址为：**hbut.jw.chaoxing.com**），那么此开源项目或许将对您有所帮助。

## 功能一览

目前，该应用提供如下功能：

- 查看课表
  - 切换到其他周查看其他周的课程
  - 提供桌面小组件显示当天课程
- 成绩查询
  - 排名查询（包括年级、专业、班级排名）
  - GPA 及算术平均分的自动计算
  - 成绩筛选（学年、课程类型）

## 屏幕截图

注意：考虑到隐私问题，我们已模糊处理某些信息，当您在实际使用该应用时，不会出现模糊现象。

<img src="https://github.com/founchoo/CampusHelper/assets/24630338/6cfb4de2-d946-48ba-ab30-307eebd8cd0e" alt="课表" width="300">
<img src="https://github.com/founchoo/CampusHelper/assets/24630338/5e51b66e-de49-4d17-adb5-842fc3e8ee9c" alt="成绩" width="300">
<img src="https://github.com/founchoo/CampusHelper/assets/24630338/569e023d-1731-495c-803a-a2046cbc4e69" alt="设置" width="300">

<img src="https://github.com/founchoo/CampusHelper/assets/24630338/5bbbc3f4-7ebb-464c-92dc-e9840674e104" alt="课表" width="300">
<img src="https://github.com/founchoo/CampusHelper/assets/24630338/38bd1006-d3e2-48e0-aeeb-4ef597c568d8" alt="成绩" width="300">
<img src="https://github.com/founchoo/CampusHelper/assets/24630338/2003885d-967a-4cca-802b-870249db0846" alt="设置" width="300">

## 下载

Github release: [点此](https://github.com/founchoo/CampusHelper/releases/latest)链接跳转到下载页面，页面下方的 `.apk` 文件即是安装包，下载安装即可。

Google Play Store: [点此前往谷歌应用商店下载](https://play.google.com/store/apps/details?id=com.dart.campushelper)

## 参与开发

欢迎提出 issue 或 pull request。

## 鸣谢

- https://www.composables.com/icons **Material Symbols for Jetpack Compose**。

- https://stackoverflow.com/ StackOverflow。

- https://m3.material.io/ Material Design 指南。

- https://developer.android.com/reference/kotlin/androidx/compose/material3/package-summary 官方开发文档。

- https://google.github.io/accompanist/placeholder/ Placeholder for Jetpack Compose.

- https://json2kt.com/ 将 `JSON` 文件转换为 `.kt` 文件。

- https://github.com/harmittaa/KoinExample Retrofit 错误处理。


## 开源协议

本项目使用 GPLv3 协议

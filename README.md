<div align="center">

<img src="https://github.com/founchoo/GongYun-for-Android/assets/24630338/3d5c2914-0592-4058-9b54-00e958c62b63" alt="Logo" width="100">

# 工韵
</div>

## 简介

欢迎来到**工韵**的 Github 开源页面, 这是一款专为**湖北工业大学**在校学生服务的
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

1. 查看课表
   - 查看其他学年及周数的课表
   - 提供桌面小组件显示当天课表
   - 查看课表备注信息（如网课、课设）
   - 检索教师课表
  
2. 查看计划课程

3. 查找同期空闲教室及授课教室

4. 成绩查询
   - 排名查询（包括年级、专业、班级排名）
   - 排名柱状图
   - 绩点及算术平均分的自动计算
   - 绩点变化曲线查看
   - 成绩筛选（学年、课程类型）
   - 成绩排序（分数、学分）
   - 成绩分布柱状图

## TODO

1. 课表页面切换课表弹窗内重置按钮移至 TopAppBar
2. 本地信息加密存储
3. 初次使用引导

## 屏幕截图

注意：考虑到隐私问题，我们已星号处理某些信息，当您实际正常使用该应用时，不会出现该现象。

<img src="https://github.com/founchoo/GongYun-for-Android/assets/24630338/4140a910-2893-4889-ad12-5cd8ca0882c0">

## 下载

Github release: [点此](https://github.com/founchoo/CampusHelper/releases/latest)链接跳转到下载页面，页面下方的 `.apk` 文件即是安装包，下载安装即可。

Google Play Store: [点此前往谷歌应用商店下载](https://play.google.com/store/apps/details?id=com.dart.campushelper)

## 参与开发

欢迎提出 issue 或 pull request。

## 鸣谢

- https://stackoverflow.com/ StackOverflow

- https://m3.material.io/ Material Design 指南

- https://developer.android.com/reference/kotlin/androidx/compose/material3/package-summary 官方开发文档

- https://google.github.io/accompanist/placeholder/ Placeholder for Jetpack Compose

- https://json2kt.com/ 将 `JSON` 文件转换为 `.kt` 文件

- https://github.com/harmittaa/KoinExample Retrofit 错误处理

- https://github.com/patrykandpatrick/vico 图表库

- https://plugins.jetbrains.com/plugin/18619-svg-to-compose svg 转 compose


## 开源协议

本项目使用 Apache License 2.0

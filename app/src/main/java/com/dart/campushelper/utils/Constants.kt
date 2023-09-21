package com.dart.campushelper.utils

import okhttp3.HttpUrl
import java.time.Instant
import java.time.format.DateTimeFormatter

class Constants {
    companion object {
        const val JOIN_QQ_GROUP_URL =
            "https://qm.qq.com/cgi-bin/qm/qr?authKey=dIZI8AIpgp06TR7BVycYTAsCPfOkaM%2BZI0wx%2FQuP3xjozvkoc5mP%2FUdkmvoAEEJJ&k=In-sAJIohCCBVcNlzwFoaKlAVep1Yv4a&noverify=0"
        const val LOGIN_URL = "https://hbut.jw.chaoxing.com/admin/login"
        val GRADE_HTTP_URL = HttpUrl.Builder()
            .scheme("https")
            .host("hbut.jw.chaoxing.com")
            .addPathSegment("admin")
            .addPathSegment("xsd")
            .addPathSegment("xsdzgcjcx")
            .addPathSegment("xsdQueryXszgcjList")
            .addQueryParameter("gridtype", "jqgrid")
            .addQueryParameter(
                "queryFields",
                "id,xnxq,kcmc,xf,kcxz,ksxs,xdxz,zhcj,hdxf,"
            )
            .addQueryParameter("_search", "false")
            .addQueryParameter(
                "nd",
                DateTimeFormatter.ISO_INSTANT.format(Instant.now())
            )
            .addQueryParameter("page.size", "500")
            .addQueryParameter("page.pn", "1")
            .addQueryParameter("sort", "id")
            .addQueryParameter("order", "asc")
            .addQueryParameter("startXnxq", "001")
            .addQueryParameter("endXnxq", "001")
            .addQueryParameter("sfjg", "")
            .addQueryParameter("query.startXnxq||", "001")
            .addQueryParameter("query.endXnxq||", "001")
            .addQueryParameter("query.sfjg||", "")
            .build()
        const val STUDENT_INFO_URL = "https://hbut.jw.chaoxing.com/admin/cjgl/xscjbbdy/printdgxscj"
        const val GITHUB_URL = "https://github.com/founchoo/CampusHelper"
        const val QQ_GROUP_NUMBER = "422915042"
    }
}
package com.example.my_app_giapha.ui.common

/** Chữ cái đầu của tên gọi (từ cuối): "Nguyễn Minh Khôi" -> "K". */
fun initialOf(name: String): String =
    name.trim().split(' ').lastOrNull { it.isNotEmpty() }?.first()?.uppercase() ?: "?"

/** Bỏ dấu tiếng Việt để tìm kiếm: "Thảo Nguyễn" -> "Thao Nguyen". */
fun khongDau(s: String): String =
    java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD)
        .replace(Regex("\\p{Mn}+"), "")
        .replace('đ', 'd').replace('Đ', 'D')
package com.example.my_app_giapha.ui.common

/** Chữ cái đầu của tên gọi (từ cuối): "Nguyễn Minh Khôi" -> "K". */
fun initialOf(name: String): String =
    name.trim().split(' ').lastOrNull { it.isNotEmpty() }?.first()?.uppercase() ?: "?"
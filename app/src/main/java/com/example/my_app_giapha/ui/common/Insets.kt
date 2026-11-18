package com.example.my_app_giapha.ui.common

import android.app.Activity
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

/** Icon thanh trạng thái tối (dùng cho màn nền sáng, không có thanh nâu). */
fun Activity.setLightStatusBar(light: Boolean) {
    WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = light
}

/** Đệm thêm chỗ cho thanh hệ thống và bàn phím. */
fun View.applySystemBarsPadding() {
    val l = paddingLeft; val t = paddingTop; val r = paddingRight; val b = paddingBottom
    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        val bars = insets.getInsets(
            WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
        )
        v.setPadding(l + bars.left, t + bars.top, r + bars.right, b + bars.bottom)
        insets
    }
}
package com.example.my_app_giapha.ui.common

import android.content.Context
import android.view.View
import android.widget.ImageButton
import androidx.activity.ComponentActivity
import com.example.my_app_giapha.R
import com.example.my_app_giapha.databinding.ViewAppBarBinding

/**
 * Gọi: binding.appBar.setup(this, "Hồ sơ cá nhân")
 * Tiêu đề dài: setup(this, "Quan hệ cha/mẹ", subtitle = "Nguyễn Văn Nam")
 * Màn gốc không có nút quay lại: showBack = false
 */
fun ViewAppBarBinding.setup(
    activity: ComponentActivity,
    title: String,
    subtitle: String? = null,
    showBack: Boolean = true
) {
    tvTitle.text = title
    tvSubtitle.text = subtitle
    tvSubtitle.visibility = if (subtitle.isNullOrEmpty()) View.GONE else View.VISIBLE
    btnBack.visibility = if (showBack) View.VISIBLE else View.GONE
    btnBack.setOnClickListener { activity.onBackPressedDispatcher.onBackPressed() }
}

/** Thêm một nút biểu tượng vào bên phải thanh tiêu đề (ví dụ ba chấm). */
fun ViewAppBarBinding.setAction(context: Context, iconRes: Int, description: String, onClick: () -> Unit) {
    actionContainer.removeAllViews()
    val button = ImageButton(context).apply {
        layoutParams = android.widget.FrameLayout.LayoutParams(
            (48 * resources.displayMetrics.density).toInt(),
            (48 * resources.displayMetrics.density).toInt()
        )
        setImageResource(iconRes)
        contentDescription = description
        setBackgroundResource(
            android.util.TypedValue().also {
                context.theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, it, true)
            }.resourceId
        )
        setOnClickListener { onClick() }
    }
    actionContainer.addView(button)
}
package com.example.my_app_giapha.ui.common

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.InsetDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.example.my_app_giapha.R
import com.example.my_app_giapha.databinding.DialogConfirmBinding

/**
 * Hộp thoại xác nhận dùng chung.
 * destructive = true: nút xác nhận đỏ (xóa, chặn, cấm, gỡ...); false: nút nâu.
 * Ví dụ:
 *   showConfirmDialog("Xóa Gia phả họ Nguyễn?", "Toàn bộ dữ liệu sẽ bị xóa vĩnh viễn.", "Xóa") { ... }
 * iconRes: biểu tượng riêng thay cho dấu cảnh báo mặc định (ví dụ ổ khóa).
 * onCancel: gọi khi người dùng bấm Hủy hoặc đóng hộp thoại mà chưa xác nhận.
 */
fun Context.showConfirmDialog(
    title: String,
    message: String,
    confirmText: String = "Xác nhận",
    cancelText: String = "Hủy",
    destructive: Boolean = true,
    showIcon: Boolean = destructive,
    iconRes: Int? = null,
    onCancel: (() -> Unit)? = null,
    onConfirm: () -> Unit
) {
    val b = DialogConfirmBinding.inflate(LayoutInflater.from(this))
    b.tvTitle.text = title
    b.tvMessage.visibility = if (message.isBlank()) android.view.View.GONE else android.view.View.VISIBLE
    b.btnCancel.text = cancelText
    b.btnConfirm.text = confirmText
    if (!destructive) {
        b.btnConfirm.backgroundTintList =
            ColorStateList.valueOf(ContextCompat.getColor(this, R.color.brown_primary))
    }
    if (!showIcon) b.iconWrap.visibility = android.view.View.GONE
    if (iconRes != null) b.ivIcon.setImageResource(iconRes)


    val dialog = AlertDialog.Builder(this).setView(b.root).create()
    var daXacNhan = false
    b.btnCancel.setOnClickListener { dialog.dismiss() }
    b.btnConfirm.setOnClickListener {
        daXacNhan = true
        dialog.dismiss()
        onConfirm()
    }
    dialog.setOnDismissListener { if (!daXacNhan) onCancel?.invoke() }
    dialog.show()

    val margin = (24 * resources.displayMetrics.density).toInt()
    dialog.window?.apply {
        setBackgroundDrawable(InsetDrawable(ColorDrawable(Color.TRANSPARENT), margin, 0, margin, 0))
        setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}
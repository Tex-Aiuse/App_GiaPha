package com.example.my_app_giapha.ui.common

import android.app.Activity
import android.util.TypedValue
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.my_app_giapha.R
import com.example.my_app_giapha.databinding.ViewAdminBarBinding
import com.example.my_app_giapha.ui.Routes

enum class TabAdmin { TAI_KHOAN, KHONG_GIAN }

/**
 * Thanh điều hướng của AdminApp (GD-21, GD-23). Gọi: binding.adminBar.setup(this, TabAdmin.TAI_KHOAN)
 * Chuyển tab thay cả ngăn xếp màn hình (hai màn này đều là màn gốc); "Đăng xuất" về GD-02.
 */
fun ViewAdminBarBinding.setup(activity: Activity, dangMo: TabAdmin) {
    val tv = TypedValue()
    root.context.theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)
    val barHeight = TypedValue.complexToDimensionPixelSize(tv.data, root.resources.displayMetrics)
    ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
        val top = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
        v.setPadding(0, top, 0, 0)
        v.layoutParams = v.layoutParams.apply { height = barHeight + top }
        insets
    }

    val dam = ResourcesCompat.getFont(root.context, R.font.be_vietnam_pro_bold)
    val thuong = ResourcesCompat.getFont(root.context, R.font.be_vietnam_pro)
    val taiKhoan = dangMo == TabAdmin.TAI_KHOAN
    tvTabAccounts.typeface = if (taiKhoan) dam else thuong
    tvTabAccounts.alpha = if (taiKhoan) 1f else 0.7f
    lineAccounts.visibility = if (taiKhoan) View.VISIBLE else View.INVISIBLE
    tvTabSpaces.typeface = if (taiKhoan) thuong else dam
    tvTabSpaces.alpha = if (taiKhoan) 0.7f else 1f
    lineSpaces.visibility = if (taiKhoan) View.INVISIBLE else View.VISIBLE

    tabAccounts.setOnClickListener { if (!taiKhoan) Routes.moDanhSachTaiKhoanHeThong(activity) }
    tabSpaces.setOnClickListener { if (taiKhoan) Routes.moDanhSachKhongGianHeThong(activity) }
    btnLogout.setOnClickListener { Routes.dangXuat(activity) }
}

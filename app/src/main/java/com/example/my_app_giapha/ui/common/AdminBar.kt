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
 * Thanh điều hướng của AdminApp (AdminHomeActivity). Gọi:
 *   binding.adminBar.setup(this, TabAdmin.TAI_KHOAN) { tab -> pager.setCurrentItem(tab.ordinal, true) }
 * Bấm tab chỉ báo ra ngoài qua khiChonTab (không mở màn hình mới); "Đăng xuất" về GD-02.
 * Khi trang đổi (vuốt ngang), gọi datTab(...) để tô lại tab đang mở.
 */
fun ViewAdminBarBinding.setup(activity: Activity, dangMo: TabAdmin, khiChonTab: (TabAdmin) -> Unit) {
    val tv = TypedValue()
    root.context.theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)
    val barHeight = TypedValue.complexToDimensionPixelSize(tv.data, root.resources.displayMetrics)
    ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
        val top = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
        v.setPadding(0, top, 0, 0)
        v.layoutParams = v.layoutParams.apply { height = barHeight + top }
        insets
    }
    datTab(dangMo)
    tabAccounts.setOnClickListener { khiChonTab(TabAdmin.TAI_KHOAN) }
    tabSpaces.setOnClickListener { khiChonTab(TabAdmin.KHONG_GIAN) }
    btnLogout.setOnClickListener { Routes.dangXuat(activity) }
}

/** Tô đậm tab đang mở, làm mờ tab còn lại. */
fun ViewAdminBarBinding.datTab(dangMo: TabAdmin) {
    val dam = ResourcesCompat.getFont(root.context, R.font.be_vietnam_pro_bold)
    val thuong = ResourcesCompat.getFont(root.context, R.font.be_vietnam_pro)
    val taiKhoan = dangMo == TabAdmin.TAI_KHOAN
    tvTabAccounts.typeface = if (taiKhoan) dam else thuong
    tvTabAccounts.alpha = if (taiKhoan) 1f else 0.7f
    lineAccounts.visibility = if (taiKhoan) View.VISIBLE else View.INVISIBLE
    tvTabSpaces.typeface = if (taiKhoan) thuong else dam
    tvTabSpaces.alpha = if (taiKhoan) 0.7f else 1f
    lineSpaces.visibility = if (taiKhoan) View.INVISIBLE else View.VISIBLE
}
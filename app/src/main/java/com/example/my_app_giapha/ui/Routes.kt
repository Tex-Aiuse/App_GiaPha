package com.example.my_app_giapha.ui

import android.app.Activity
import android.widget.Toast
import com.example.my_app_giapha.model.LoaiTaiKhoan
import com.example.my_app_giapha.model.TaiKhoan

object Routes {
    fun sauDangNhap(activity: Activity, tk: TaiKhoan) {
        // TẠM: bước 6c đổi thành mở GD-03; bước 11 đổi nhánh AdminApp thành GD-21.
        val dich = if (tk.loaiTaiKhoan == LoaiTaiKhoan.ADMIN_APP) "GD-21" else "GD-03"
        Toast.makeText(activity, "Xin chào ${tk.tenHienThi} → $dich", Toast.LENGTH_SHORT).show()
    }
}
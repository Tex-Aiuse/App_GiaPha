package com.example.my_app_giapha.ui

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import com.example.my_app_giapha.data.Services
import com.example.my_app_giapha.model.LoaiTaiKhoan
import com.example.my_app_giapha.model.TaiKhoan
import com.example.my_app_giapha.ui.auth.LoginActivity
import com.example.my_app_giapha.ui.profile.ProfileActivity
import com.example.my_app_giapha.ui.space.JoinCodeActivity
import com.example.my_app_giapha.ui.space.SpaceListActivity
import com.example.my_app_giapha.ui.tree.TreeActivity

/** Mọi lối đi giữa các màn hình. Dòng ghi "TẠM" sẽ được thay khi làm tới màn đó. */
object Routes {

    private fun Activity.tam(text: String) =
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()

    private fun Activity.moMoi(cls: Class<*>) {
        startActivity(Intent(this, cls).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
    }

    fun sauDangNhap(activity: Activity, tk: TaiKhoan) {
        if (tk.loaiTaiKhoan == LoaiTaiKhoan.ADMIN_APP) {
            activity.tam("TẠM: AdminApp → GD-21 (bước 11)")
        } else {
            activity.moMoi(SpaceListActivity::class.java)
        }
    }

    fun dangXuat(activity: Activity) {
        Services.repository.dangXuat()
        activity.moMoi(LoginActivity::class.java)
    }

    fun moKhongGian(activity: Activity, idKhongGian: Int) =
        activity.startActivity(
            Intent(activity, TreeActivity::class.java)
                .putExtra(TreeActivity.EXTRA_ID_KHONG_GIAN, idKhongGian)
        )

    // Các lối đi từ GD-06. Dòng "TẠM" được thay khi làm tới màn tương ứng.
    fun moChiTietO(activity: Activity, idKhongGian: Int, idO: Int) =
        activity.tam("TẠM: GD-08 Chi tiết Ô #$idO (bước 8)")

    fun moThemO(activity: Activity, idKhongGian: Int) =
        activity.tam("TẠM: GD-07 Thêm thành viên (bước 8)")

    fun moDanhSachThanhVien(activity: Activity, idKhongGian: Int) =
        activity.tam("TẠM: GD-14 Danh sách thành viên")

    fun moMaMoi(activity: Activity, idKhongGian: Int) =
        activity.tam("TẠM: GD-16 Mã mời")

    fun moCaiDatKhongGian(activity: Activity, idKhongGian: Int) =
        activity.tam("TẠM: GD-18 Cài đặt Không gian")

    fun moQuanLyVaiTro(activity: Activity, idKhongGian: Int) =
        activity.tam("TẠM: GD-19 Quản lý vai trò")

    fun moHoSo(activity: Activity) =
        activity.startActivity(Intent(activity, ProfileActivity::class.java))

    fun moNhapMa(activity: Activity) =
        activity.startActivity(Intent(activity, JoinCodeActivity::class.java))
}
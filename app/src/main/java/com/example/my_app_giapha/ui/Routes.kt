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
import com.example.my_app_giapha.ui.person.PersonDetailActivity
import com.example.my_app_giapha.ui.person.PersonFormActivity
import com.example.my_app_giapha.ui.person.ParentRelationActivity
import com.example.my_app_giapha.ui.person.MarriageActivity
import com.example.my_app_giapha.ui.person.BioActivity
import com.example.my_app_giapha.ui.person.BioEditActivity
import com.example.my_app_giapha.ui.person.ThanhTuuActivity
import com.example.my_app_giapha.ui.person.AnhDaiDienActivity
import com.example.my_app_giapha.ui.space.MemberListActivity
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

    /** Về lại GD-06, đóng mọi màn GD-08 đang chồng phía trên; GD-06 tự tải lại ở onResume. */
    fun veCay(activity: Activity, idKhongGian: Int) =
        activity.startActivity(
            Intent(activity, TreeActivity::class.java)
                .putExtra(TreeActivity.EXTRA_ID_KHONG_GIAN, idKhongGian)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        )

    // Các lối đi từ GD-06. Dòng "TẠM" được thay khi làm tới màn tương ứng.
    fun moChiTietO(activity: Activity, idKhongGian: Int, idO: Int) =
        activity.startActivity(
            Intent(activity, PersonDetailActivity::class.java)
                .putExtra(PersonDetailActivity.EXTRA_ID_KHONG_GIAN, idKhongGian)
                .putExtra(PersonDetailActivity.EXTRA_ID_O, idO)
        )

    fun moSuaO(activity: Activity, idKhongGian: Int, idO: Int) =
        activity.startActivity(
            Intent(activity, PersonFormActivity::class.java)
                .putExtra(PersonFormActivity.EXTRA_ID_KHONG_GIAN, idKhongGian)
                .putExtra(PersonFormActivity.EXTRA_ID_O, idO)
        )

    //fun xoaO(activity: Activity, idKhongGian: Int, idO: Int) =
    //    activity.tam("TẠM: xóa Ô #$idO (làm ở phần sau của 8a)")

    fun moTieuSu(activity: Activity, idKhongGian: Int, idO: Int) =
        activity.startActivity(
            Intent(activity, BioActivity::class.java)
                .putExtra(BioActivity.EXTRA_ID_KHONG_GIAN, idKhongGian)
                .putExtra(BioActivity.EXTRA_ID_O, idO)
        )

    fun moSoanTieuSu(activity: Activity, idKhongGian: Int, idO: Int) =
        activity.startActivity(
            Intent(activity, BioEditActivity::class.java)
                .putExtra(BioEditActivity.EXTRA_ID_KHONG_GIAN, idKhongGian)
                .putExtra(BioEditActivity.EXTRA_ID_O, idO)
        )

    fun moThanhTuu(activity: Activity, idKhongGian: Int, idO: Int) =
        activity.startActivity(
            Intent(activity, ThanhTuuActivity::class.java)
                .putExtra(ThanhTuuActivity.EXTRA_ID_KHONG_GIAN, idKhongGian)
                .putExtra(ThanhTuuActivity.EXTRA_ID_O, idO)
        )

    fun moAnhDaiDien(activity: Activity, idKhongGian: Int, idO: Int) =
        activity.startActivity(
            Intent(activity, AnhDaiDienActivity::class.java)
                .putExtra(AnhDaiDienActivity.EXTRA_ID_KHONG_GIAN, idKhongGian)
                .putExtra(AnhDaiDienActivity.EXTRA_ID_O, idO)
        )

    fun moQuanHeChaMe(activity: Activity, idKhongGian: Int, idO: Int) =
        activity.startActivity(
            Intent(activity, ParentRelationActivity::class.java)
                .putExtra(ParentRelationActivity.EXTRA_ID_KHONG_GIAN, idKhongGian)
                .putExtra(ParentRelationActivity.EXTRA_ID_O, idO)
        )


    fun moQuanHeHonNhan(activity: Activity, idKhongGian: Int, idO: Int) =
        activity.startActivity(
            Intent(activity, MarriageActivity::class.java)
                .putExtra(MarriageActivity.EXTRA_ID_KHONG_GIAN, idKhongGian)
                .putExtra(MarriageActivity.EXTRA_ID_O, idO)
        )

    fun moThemO(activity: Activity, idKhongGian: Int) =
        activity.startActivity(
            Intent(activity, PersonFormActivity::class.java)
                .putExtra(PersonFormActivity.EXTRA_ID_KHONG_GIAN, idKhongGian)
        )

    fun moDanhSachThanhVien(activity: Activity, idKhongGian: Int) =
        activity.startActivity(
            Intent(activity, MemberListActivity::class.java)
                .putExtra(MemberListActivity.EXTRA_ID_KHONG_GIAN, idKhongGian)
        )

    fun moMoiThanhVien(activity: Activity, idKhongGian: Int) =
        activity.tam("TẠM: GD-15 Mời thành viên")

    fun moGanO(activity: Activity, idKhongGian: Int, idTaiKhoan: Int) =
        activity.tam("TẠM: GD-17 Gán Ô cho tài khoản #$idTaiKhoan")

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
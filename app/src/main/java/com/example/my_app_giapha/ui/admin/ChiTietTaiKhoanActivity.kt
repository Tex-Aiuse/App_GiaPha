package com.example.my_app_giapha.ui.admin

import android.content.res.ColorStateList
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.my_app_giapha.R
import com.example.my_app_giapha.data.ChiTietTaiKhoan
import com.example.my_app_giapha.data.Kq
import com.example.my_app_giapha.data.Services
import com.example.my_app_giapha.databinding.ActivityChiTietTaiKhoanBinding
import com.example.my_app_giapha.databinding.ItemInfoRowBinding
import com.example.my_app_giapha.databinding.ItemSpaceOfAccountBinding
import com.example.my_app_giapha.model.HieuUngAvatar
import com.example.my_app_giapha.model.KhungAvatar
import com.example.my_app_giapha.model.LoaiDangNhap
import com.example.my_app_giapha.model.LoaiTaiKhoan
import com.example.my_app_giapha.model.TrangThaiTaiKhoan
import com.example.my_app_giapha.ui.Routes
import com.example.my_app_giapha.ui.common.applyBottomInsetPadding
import com.example.my_app_giapha.ui.common.formatPhone
import com.example.my_app_giapha.ui.common.initialOf
import com.example.my_app_giapha.ui.common.setBlockedBadge
import com.example.my_app_giapha.ui.common.setRoleBadge
import com.example.my_app_giapha.ui.common.setup
import com.example.my_app_giapha.ui.common.showConfirmDialog
import com.google.android.material.snackbar.Snackbar
import java.time.format.DateTimeFormatter

/**
 * GD-22 — Chi tiết tài khoản (AdminApp): xem thông tin, các không gian đang tham gia, cấm / gỡ cấm.
 * Cấm ở đây là cấm đăng nhập toàn hệ thống (khác với chặn trong một không gian ở GD-19).
 * Không có nút hành động với tài khoản AdminApp. Tải lại mỗi lần hiện lại; back về GD-21 tự cập nhật dòng.
 */
class ChiTietTaiKhoanActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ID_TAI_KHOAN = "idTaiKhoan"
    }

    private lateinit var b: ActivityChiTietTaiKhoanBinding
    private val repo get() = Services.repository
    private val fmtNgay = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private val fmtGio = DateTimeFormatter.ofPattern("dd/MM/yyyy, HH:mm")
    private var idTaiKhoan = 0
    private var daCoDuLieu = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityChiTietTaiKhoanBinding.inflate(layoutInflater)
        setContentView(b.root)
        idTaiKhoan = intent.getIntExtra(EXTRA_ID_TAI_KHOAN, 0)

        b.appBar.setup(this, "Chi tiết tài khoản")
        b.actionBar.applyBottomInsetPadding()
    }

    override fun onResume() {
        super.onResume()
        tai()
    }

    private fun tai() {
        if (!daCoDuLieu) b.stateView.showLoading()
        when (val r = repo.chiTietTaiKhoan(idTaiKhoan)) {
            is Kq.Loi -> {
                daCoDuLieu = false
                b.actionBar.visibility = View.GONE
                b.stateView.showError(r.thongBao) { tai() }
            }
            is Kq.Ok -> hien(r.giaTri)
        }
    }

    private fun hien(d: ChiTietTaiKhoan) {
        daCoDuLieu = true
        b.stateView.hide()
        val tk = d.taiKhoan
        val khoa = tk.trangThai == TrangThaiTaiKhoan.DA_KHOA

        // Thẻ thông tin cơ bản
        b.avatar.hien(null, initialOf(tk.tenHienThi), KhungAvatar.TRON, HieuUngAvatar.GOC, khongVien = true)
        b.tvName.text = tk.tenHienThi
        if (khoa) {
            b.tvStatus.setBlockedBadge("Đã khóa")
        } else {
            b.tvStatus.setRoleBadge("ThanhVien")      // kiểu nhãn xanh, đổi chữ thành "Hoạt động"
            b.tvStatus.text = "Hoạt động"
        }
        b.infoRows.removeAllViews()
        themDong("Số điện thoại", formatPhone(tk.sdt))
        themDong("Email", tk.email, trong = "Chưa cập nhật")
        themDong(
            "Loại đăng nhập", when (tk.loaiDangNhap) {
                LoaiDangNhap.SDT_OTP -> "Số điện thoại"
                LoaiDangNhap.MAT_KHAU -> "Mật khẩu"
                LoaiDangNhap.GOOGLE -> "Google"
                LoaiDangNhap.APPLE -> "Apple"
            }
        )
        themDong("Ngày tạo tài khoản", tk.ngayTao.format(fmtNgay))
        themDong("Lần đăng nhập gần nhất", tk.lanDangNhapCuoi?.format(fmtGio), trong = "Chưa đăng nhập")

        // Thẻ không gian đang tham gia
        b.listSpaces.removeAllViews()
        b.tvNoSpace.visibility = if (d.khongGian.isEmpty()) View.VISIBLE else View.GONE
        d.khongGian.forEachIndexed { i, k ->
            val r = ItemSpaceOfAccountBinding.inflate(layoutInflater, b.listSpaces, false)
            r.divider.visibility = if (i == 0) View.GONE else View.VISIBLE
            r.tvName.text = k.khongGian.tenKhongGian
            r.ivLock.visibility = if (k.khongGian.cheDoRiengTu) View.VISIBLE else View.GONE
            r.tvRole.setRoleBadge(k.vaiTro.db)
            r.root.setOnClickListener { Routes.moChiTietKhongGianHeThong(this, k.khongGian.idKhongGian) }
            b.listSpaces.addView(r.root)
        }

        // Nút hành động: không cấm được tài khoản AdminApp
        if (tk.loaiTaiKhoan == LoaiTaiKhoan.ADMIN_APP) {
            b.actionBar.visibility = View.GONE
            return
        }
        b.actionBar.visibility = View.VISIBLE
        if (khoa) {
            b.btnAction.text = "Gỡ cấm"
            b.btnAction.setIconResource(R.drawable.ic_lock_open)
            b.btnAction.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.brown_primary))
            b.btnAction.setOnClickListener { hoiGoCam(tk.tenHienThi) }
        } else {
            b.btnAction.text = "Cấm tài khoản"
            b.btnAction.setIconResource(R.drawable.ic_block)
            b.btnAction.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.danger))
            b.btnAction.setOnClickListener { hoiCam(tk.tenHienThi) }
        }
    }

    private fun themDong(nhan: String, giaTri: String?, trong: String = "—") {
        val row = ItemInfoRowBinding.inflate(layoutInflater, b.infoRows, false)
        row.tvLabel.text = nhan
        if (giaTri.isNullOrBlank()) {
            row.tvValue.text = trong
            row.tvValue.setTypeface(row.tvValue.typeface, Typeface.ITALIC)
            row.tvValue.setTextColor(ContextCompat.getColor(this, R.color.text_hint))
        } else {
            row.tvValue.text = giaTri
        }
        b.infoRows.addView(row.root)
    }

    // ------------------------------------------------------------------ cấm / gỡ cấm

    private fun hoiCam(ten: String) {
        showConfirmDialog(
            title = "Cấm tài khoản $ten?",
            message = "Tài khoản này sẽ không thể đăng nhập vào ứng dụng, ở mọi Không gian đang tham gia.",
            destructive = true
        ) { xuLy(repo.camTaiKhoan(idTaiKhoan), "Đã cấm tài khoản $ten") }
    }

    private fun hoiGoCam(ten: String) {
        showConfirmDialog(
            title = "Gỡ cấm tài khoản $ten?",
            message = "",
            destructive = false, showIcon = false
        ) { xuLy(repo.goCamTaiKhoan(idTaiKhoan), "Đã gỡ cấm tài khoản $ten") }
    }

    private fun xuLy(r: Kq<Unit>, thanhCong: String) {
        when (r) {
            is Kq.Loi -> Snackbar.make(b.root, r.thongBao, Snackbar.LENGTH_LONG).show()
            is Kq.Ok -> {
                tai()
                Snackbar.make(b.root, thanhCong, Snackbar.LENGTH_SHORT).show()
            }
        }
    }
}

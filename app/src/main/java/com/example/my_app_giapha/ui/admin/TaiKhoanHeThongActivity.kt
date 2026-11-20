package com.example.my_app_giapha.ui.admin

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.example.my_app_giapha.data.Kq
import com.example.my_app_giapha.data.Services
import com.example.my_app_giapha.data.TrangTaiKhoan
import com.example.my_app_giapha.databinding.ActivityTaiKhoanHeThongBinding
import com.example.my_app_giapha.databinding.ItemTaiKhoanHeThongBinding
import com.example.my_app_giapha.model.TaiKhoan
import com.example.my_app_giapha.model.TrangThaiTaiKhoan
import com.example.my_app_giapha.ui.Routes
import com.example.my_app_giapha.ui.common.TabAdmin
import com.example.my_app_giapha.ui.common.attachPhoneFormat
import com.example.my_app_giapha.ui.common.formatPhone
import com.example.my_app_giapha.ui.common.setBlockedBadge
import com.example.my_app_giapha.ui.common.setRoleBadge
import com.example.my_app_giapha.ui.common.setup
import java.time.format.DateTimeFormatter

/**
 * GD-21 — Danh sách tài khoản toàn hệ thống. Màn đầu tiên của AdminApp sau khi đăng nhập.
 * 20 dòng mỗi trang, tìm theo số điện thoại (gõ một phần cũng khớp), mới tạo xếp trước.
 * Tìm kiếm quay về trang 1. Tải lại mỗi lần hiện lại để thấy thay đổi từ GD-22.
 */
class TaiKhoanHeThongActivity : AppCompatActivity() {

    private lateinit var b: ActivityTaiKhoanHeThongBinding
    private val repo get() = Services.repository
    private val fmtNgay = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private var trang = 1
    private var tuKhoa = ""
    private var daCoDuLieu = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityTaiKhoanHeThongBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.adminBar.setup(this, TabAdmin.TAI_KHOAN)

        b.etSearch.attachPhoneFormat { digits ->
            if (digits != tuKhoa) {
                tuKhoa = digits
                trang = 1
                tai()
            }
        }
        b.etSearch.doAfterTextChanged {
            b.ivClear.visibility = if (it.isNullOrEmpty()) View.GONE else View.VISIBLE
        }
        b.ivClear.setOnClickListener { b.etSearch.setText("") }

        b.pagination.onPageChange = {
            trang = it
            tai()
            b.scroll.scrollTo(0, 0)
        }
    }

    override fun onResume() {
        super.onResume()
        tai()
    }

    private fun tai() {
        if (!daCoDuLieu) b.stateView.showLoading()
        when (val r = repo.danhSachTaiKhoan(tuKhoa, trang)) {
            is Kq.Loi -> {
                daCoDuLieu = false
                b.stateView.showError(r.thongBao) { tai() }
            }
            is Kq.Ok -> hien(r.giaTri)
        }
    }

    private fun hien(t: TrangTaiKhoan) {
        daCoDuLieu = true
        trang = t.trang
        b.pagination.setPage(t.trang, t.tongTrang)
        b.list.removeAllViews()
        if (t.ds.isEmpty()) {
            b.stateView.showEmpty(
                if (tuKhoa.isEmpty()) "Chưa có tài khoản nào" else "Không tìm thấy tài khoản với số điện thoại này"
            )
            return
        }
        b.stateView.hide()
        t.ds.forEach { b.list.addView(dong(it)) }
    }

    private fun dong(tk: TaiKhoan): View {
        val r = ItemTaiKhoanHeThongBinding.inflate(layoutInflater, b.list, false)
        r.tvName.text = tk.tenHienThi
        r.tvSub.text = "${formatPhone(tk.sdt)} · ${tk.ngayTao.format(fmtNgay)}"
        if (tk.trangThai == TrangThaiTaiKhoan.DA_KHOA) {
            r.tvStatus.setBlockedBadge("Đã khóa")
        } else {
            r.tvStatus.setRoleBadge("ThanhVien")      // lấy kiểu nhãn xanh, đổi chữ thành "Hoạt động"
            r.tvStatus.text = "Hoạt động"
        }
        r.root.setOnClickListener { Routes.moChiTietTaiKhoan(this, tk.idTaiKhoan) }
        return r.root
    }
}

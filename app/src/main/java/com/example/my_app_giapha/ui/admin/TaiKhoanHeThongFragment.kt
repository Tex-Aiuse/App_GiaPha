package com.example.my_app_giapha.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.core.widget.doAfterTextChanged
import com.example.my_app_giapha.data.Kq
import com.example.my_app_giapha.data.Services
import com.example.my_app_giapha.data.TrangTaiKhoan
import com.example.my_app_giapha.databinding.ActivityTaiKhoanHeThongBinding
import com.example.my_app_giapha.databinding.ItemTaiKhoanHeThongBinding
import com.example.my_app_giapha.model.TaiKhoan
import com.example.my_app_giapha.model.TrangThaiTaiKhoan
import com.example.my_app_giapha.ui.Routes
import com.example.my_app_giapha.ui.common.attachPhoneFormat
import com.example.my_app_giapha.ui.common.formatPhone
import com.example.my_app_giapha.ui.common.setBlockedBadge
import com.example.my_app_giapha.ui.common.setRoleBadge
import java.time.format.DateTimeFormatter

/**
 * GD-21 — Danh sách tài khoản toàn hệ thống. Màn đầu tiên của AdminApp sau khi đăng nhập.
 * 20 dòng mỗi trang, tìm theo số điện thoại (gõ một phần cũng khớp), mới tạo xếp trước.
 * Tìm kiếm quay về trang 1. Tải lại mỗi lần hiện lại để thấy thay đổi từ GD-22.
 */
/** Là trang đầu của AdminHomeActivity (vuốt ngang hoặc bấm tab để chuyển với tab Không gian). */
class TaiKhoanHeThongFragment : Fragment() {

    private var _b: ActivityTaiKhoanHeThongBinding? = null
    private val b get() = _b!!
    private val repo get() = Services.repository
    private val fmtNgay = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private var trang = 1
    private var tuKhoa = ""
    private var daCoDuLieu = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _b = ActivityTaiKhoanHeThongBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onDestroyView() {
        _b = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        daCoDuLieu = false
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
        r.root.setOnClickListener { Routes.moChiTietTaiKhoan(requireActivity(), tk.idTaiKhoan) }
        return r.root
    }
}
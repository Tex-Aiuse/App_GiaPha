package com.example.my_app_giapha.ui.admin

import com.example.my_app_giapha.ui.common.hienHopChonNgay
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.core.widget.doAfterTextChanged
import com.example.my_app_giapha.R
import com.example.my_app_giapha.data.BoLocKhongGian
import com.example.my_app_giapha.data.DongKhongGianHeThong
import com.example.my_app_giapha.data.Kq
import com.example.my_app_giapha.data.Services
import com.example.my_app_giapha.data.TrangKhongGianHeThong
import com.example.my_app_giapha.databinding.ActivityKhongGianHeThongBinding
import com.example.my_app_giapha.databinding.DialogLocKhongGianBinding
import com.example.my_app_giapha.databinding.ItemKhongGianHeThongBinding
import com.example.my_app_giapha.ui.Routes
import com.example.my_app_giapha.ui.common.attachPhoneFormat
import com.example.my_app_giapha.ui.common.formatPhone
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * GD-23 — Danh sách không gian toàn hệ thống (AdminApp). 20 dòng mỗi trang, mới tạo xếp trước.
 * Ô tìm theo tên không gian; nút bộ lọc thêm: tên chủ, số điện thoại chủ, ngày tạo (từ – đến). Các điều kiện cộng lại.
 * Đổi tìm hoặc lọc thì về trang 1. Bấm một dòng mở GD-24 (bước 11d).
 * Là trang thứ hai của AdminHomeActivity (vuốt ngang hoặc bấm tab để chuyển với tab Tài khoản).
 */
class KhongGianHeThongFragment : Fragment() {

    private var _b: ActivityKhongGianHeThongBinding? = null
    private val b get() = _b!!
    private val repo get() = Services.repository
    private val fmtNgay = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private var trang = 1
    private var loc = BoLocKhongGian()
    private var daCoDuLieu = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _b = ActivityKhongGianHeThongBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onDestroyView() {
        _b = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        daCoDuLieu = false
        b.etSearch.doAfterTextChanged {
            val v = it?.toString().orEmpty()
            b.ivClear.visibility = if (v.isEmpty()) View.GONE else View.VISIBLE
            if (v != loc.ten) {
                loc = loc.copy(ten = v)
                trang = 1
                tai()
            }
        }
        b.ivClear.setOnClickListener { b.etSearch.setText("") }

        b.btnFilter.setOnClickListener { moHopLoc() }
        b.tvBoLoc.setOnClickListener {
            loc = loc.boLocNangCao()
            trang = 1
            capNhatDaiLoc()
            tai()
        }

        b.pagination.onPageChange = {
            trang = it
            tai()
            b.scroll.scrollTo(0, 0)
        }
        capNhatDaiLoc()
    }

    override fun onResume() {
        super.onResume()
        tai()
    }

    private fun tai() {
        if (!daCoDuLieu) b.stateView.showLoading()
        when (val r = repo.danhSachKhongGianHeThong(loc, trang)) {
            is Kq.Loi -> {
                daCoDuLieu = false
                b.stateView.showError(r.thongBao) { tai() }
            }
            is Kq.Ok -> hien(r.giaTri)
        }
    }

    private fun hien(t: TrangKhongGianHeThong) {
        daCoDuLieu = true
        trang = t.trang
        b.pagination.setPage(t.trang, t.tongTrang)
        b.list.removeAllViews()
        if (t.ds.isEmpty()) {
            b.stateView.showEmpty(
                when {
                    loc.dangLoc -> "Không tìm thấy không gian phù hợp với bộ lọc"
                    loc.ten.isNotBlank() -> "Không tìm thấy không gian với tên này"
                    else -> "Chưa có không gian nào"
                }
            )
            return
        }
        b.stateView.hide()
        t.ds.forEach { b.list.addView(dong(it)) }
    }

    private fun dong(d: DongKhongGianHeThong): View {
        val r = ItemKhongGianHeThongBinding.inflate(layoutInflater, b.list, false)
        val kg = d.khongGian
        r.tvName.text = kg.tenKhongGian
        r.tvName.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, if (kg.cheDoRiengTu) R.drawable.ic_lock else 0, 0)
        r.tvSub.text = "Người tạo: ${d.tenChu} · ${kg.ngayTao.format(fmtNgay)}"
        r.root.setOnClickListener { Routes.moChiTietKhongGianHeThong(requireActivity(), kg.idKhongGian) }
        return r.root
    }

    // ---- dải "Đang lọc" và chấm trên nút bộ lọc ----

    private fun capNhatDaiLoc() {
        val dang = loc.dangLoc
        b.vDot.visibility = if (dang) View.VISIBLE else View.GONE
        b.stripLoc.visibility = if (dang) View.VISIBLE else View.GONE
        if (!dang) return
        val phan = buildList {
            if (loc.tenChu.isNotBlank()) add("tên chủ")
            if (loc.sdtChu.isNotBlank()) add("SĐT chủ")
            if (loc.tuNgay != null || loc.denNgay != null) add("ngày tạo")
        }
        b.tvLocInfo.text = "Đang lọc theo: " + phan.joinToString(" · ")
    }

    // ---- hộp bộ lọc ----

    private fun moHopLoc() {
        val d = DialogLocKhongGianBinding.inflate(LayoutInflater.from(requireContext()))
        val dialog = AlertDialog.Builder(requireContext()).setView(d.root).create()

        var tu: LocalDate? = loc.tuNgay
        var den: LocalDate? = loc.denNgay

        d.etSdt.attachPhoneFormat()
        d.etTenChu.setText(loc.tenChu)
        if (loc.sdtChu.isNotEmpty()) d.etSdt.setText(formatPhone(loc.sdtChu))

        fun hienNgay() {
            d.tvTu.text = tu?.format(fmtNgay) ?: ""
            d.tvDen.text = den?.format(fmtNgay) ?: ""
            d.ivXoaTu.visibility = if (tu == null) View.GONE else View.VISIBLE
            d.ivXoaDen.visibility = if (den == null) View.GONE else View.VISIBLE
            d.tvError.visibility = View.GONE
        }
        hienNgay()

        d.fieldTu.setOnClickListener { chonNgay(tu) { tu = it; hienNgay() } }
        d.fieldDen.setOnClickListener { chonNgay(den) { den = it; hienNgay() } }
        d.ivXoaTu.setOnClickListener { tu = null; hienNgay() }
        d.ivXoaDen.setOnClickListener { den = null; hienNgay() }

        d.btnXoaLoc.setOnClickListener {
            dialog.dismiss()
            loc = loc.boLocNangCao()
            trang = 1
            capNhatDaiLoc()
            tai()
        }
        d.btnApDung.setOnClickListener {
            val t = tu
            val n = den
            if (t != null && n != null && t.isAfter(n)) {
                d.tvError.text = "Ngày bắt đầu phải trước hoặc bằng ngày kết thúc"
                d.tvError.visibility = View.VISIBLE
                return@setOnClickListener
            }
            dialog.dismiss()
            loc = loc.copy(
                tenChu = d.etTenChu.text.toString().trim(),
                sdtChu = d.etSdt.text.toString().filter { it.isDigit() },
                tuNgay = t,
                denNgay = n
            )
            trang = 1
            capNhatDaiLoc()
            tai()
        }

        dialog.show()
        val le = (24 * resources.displayMetrics.density).toInt()
        dialog.window?.apply {
            setBackgroundDrawable(InsetDrawable(ColorDrawable(Color.TRANSPARENT), le, 0, le, 0))
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }

    private fun chonNgay(hienTai: LocalDate?, khiChon: (LocalDate) -> Unit) {
        hienHopChonNgay(parentFragmentManager, hienTai, chanTuongLai = false, khiChon = khiChon)
    }
}
package com.example.my_app_giapha.ui.admin

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.my_app_giapha.R
import com.example.my_app_giapha.data.ChiTietKhongGianHeThong
import com.example.my_app_giapha.data.Kq
import com.example.my_app_giapha.data.Services
import com.example.my_app_giapha.databinding.ActivityChiTietKhongGianHeThongBinding
import com.example.my_app_giapha.databinding.ItemInfoRowBadgeBinding
import com.example.my_app_giapha.databinding.ItemInfoRowBinding
import com.example.my_app_giapha.databinding.ItemThanhVienChiXemBinding
import com.example.my_app_giapha.ui.Routes
import com.example.my_app_giapha.ui.common.applyBottomInsetPadding
import com.example.my_app_giapha.ui.common.formatPhone
import com.example.my_app_giapha.ui.common.setBlockedBadge
import com.example.my_app_giapha.ui.common.setRoleBadge
import com.example.my_app_giapha.ui.common.setup
import java.time.format.DateTimeFormatter

/**
 * GD-24 — Chi tiết không gian (AdminApp): xem thông tin, danh sách thành viên, cây gia phả (chỉ xem).
 * Không có nút nào sửa vai trò, chặn thành viên hay đổi cây: đó là việc của Admin trong chính không gian.
 * Duy nhất một hành động: "Xóa không gian này" (sang GD-25, làm ở bước 11e).
 * Tải lại mỗi lần hiện lại; trạng thái mở rộng danh sách thành viên được giữ khi xoay màn hình.
 */
class ChiTietKhongGianHeThongActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ID_KHONG_GIAN = "idKhongGian"
        private const val SO_DONG_GON = 5
        private const val KEY_MO_RONG = "moRong"
    }

    private lateinit var b: ActivityChiTietKhongGianHeThongBinding
    private val repo get() = Services.repository
    private val fmtNgay = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private var idKhongGian = 0
    private var daCoDuLieu = false
    private var moRong = false
    private var duHienTai: ChiTietKhongGianHeThong? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityChiTietKhongGianHeThongBinding.inflate(layoutInflater)
        setContentView(b.root)
        idKhongGian = intent.getIntExtra(EXTRA_ID_KHONG_GIAN, 0)
        moRong = savedInstanceState?.getBoolean(KEY_MO_RONG) ?: false

        b.appBar.setup(this, "Chi tiết không gian")
        b.actionBar.applyBottomInsetPadding()
        b.cardTree.setOnClickListener { Routes.moCayChiXem(this, idKhongGian) }
        b.btnToggle.setOnClickListener {
            moRong = !moRong
            duHienTai?.let { veDanhSachThanhVien(it) }
        }
        b.btnDelete.setOnClickListener {
            duHienTai?.let { Routes.moXoaKhongGianHeThong(this, it.khongGian.idKhongGian, it.khongGian.tenKhongGian) }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_MO_RONG, moRong)
    }

    override fun onResume() {
        super.onResume()
        tai()
    }

    private fun tai() {
        if (!daCoDuLieu) b.stateView.showLoading()
        when (val r = repo.chiTietKhongGianHeThong(idKhongGian)) {
            is Kq.Loi -> {
                daCoDuLieu = false
                duHienTai = null
                b.actionBar.visibility = View.GONE
                b.stateView.showError(r.thongBao) { tai() }
            }
            is Kq.Ok -> hien(r.giaTri)
        }
    }

    private fun hien(d: ChiTietKhongGianHeThong) {
        daCoDuLieu = true
        duHienTai = d
        b.stateView.hide()
        b.actionBar.visibility = View.VISIBLE

        // Thẻ 1: thông tin cơ bản
        b.infoRows.removeAllViews()
        themDong("Tên không gian", d.khongGian.tenKhongGian)
        themDongNguoiTao(d)
        themDong("Ngày tạo", d.khongGian.ngayTao.format(fmtNgay))
        themDongTrangThai(d.khongGian.cheDoRiengTu)
        themDong("Số thành viên", d.soThanhVien.toString())
        themDong("Số Ô trên cây", d.soO.toString())
        // Dòng đầu của thẻ không cần đường kẻ phía trên
        (b.infoRows.getChildAt(0) as? android.view.ViewGroup)?.getChildAt(0)?.visibility = View.GONE

        veDanhSachThanhVien(d)
    }

    private fun themDong(nhan: String, giaTri: String): ItemInfoRowBinding {
        val row = ItemInfoRowBinding.inflate(layoutInflater, b.infoRows, false)
        row.tvLabel.text = nhan
        row.tvValue.text = giaTri
        b.infoRows.addView(row.root)
        return row
    }

    /** "Tên · SĐT" màu nâu kèm mũi tên, bấm vào mở GD-22 của người tạo. Không còn tài khoản thì chỉ là chữ thường. */
    private fun themDongNguoiTao(d: ChiTietKhongGianHeThong) {
        val tk = d.nguoiTao
        val row = themDong("Người tạo", if (tk == null) "Không rõ" else "${tk.tenHienThi} · ${formatPhone(tk.sdt)}")
        if (tk == null) return
        row.tvValue.setTextColor(getColor(R.color.brown_primary))
        row.tvValue.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_chevron_right, 0)
        row.tvValue.compoundDrawablePadding = (4 * resources.displayMetrics.density).toInt()
        row.root.isClickable = true
        row.root.setOnClickListener { Routes.moChiTietTaiKhoan(this, tk.idTaiKhoan) }
    }

    private fun themDongTrangThai(riengTu: Boolean) {
        val row = ItemInfoRowBadgeBinding.inflate(layoutInflater, b.infoRows, false)
        row.tvLabel.text = "Trạng thái"
        if (riengTu) {
            row.tvBadge.setRoleBadge("QuanLy")        // kiểu nhãn đào, đổi chữ thành "Riêng tư"
            row.tvBadge.text = "Riêng tư"
        } else {
            row.tvBadge.setRoleBadge("ThanhVien")     // kiểu nhãn xanh, đổi chữ thành "Công khai"
            row.tvBadge.text = "Công khai"
        }
        b.infoRows.addView(row.root)
    }

    // Thẻ 2: 5 dòng đầu, hoặc tất cả khi đã bấm "Xem tất cả"
    private fun veDanhSachThanhVien(d: ChiTietKhongGianHeThong) {
        val ds = d.thanhVien
        val coNutMoRong = ds.size > SO_DONG_GON
        val hien = if (moRong || !coNutMoRong) ds else ds.take(SO_DONG_GON)

        b.listMembers.removeAllViews()
        b.tvNoMember.visibility = if (ds.isEmpty()) View.VISIBLE else View.GONE
        hien.forEachIndexed { i, m ->
            val r = ItemThanhVienChiXemBinding.inflate(layoutInflater, b.listMembers, false)
            r.divider.visibility = if (i == 0) View.GONE else View.VISIBLE
            r.tvName.text = m.tenHienThi
            r.tvRole.setRoleBadge(m.vaiTro.db)
            if (m.biChan) {
                r.tvBlocked.setBlockedBadge("Đã bị chặn")
                r.tvBlocked.visibility = View.VISIBLE
                r.root.alpha = 0.55f
            }
            b.listMembers.addView(r.root)
        }

        b.btnToggle.visibility = if (coNutMoRong) View.VISIBLE else View.GONE
        if (moRong) {
            b.btnToggle.text = "Thu gọn"
            b.btnToggle.setIconResource(R.drawable.ic_expand_less)
        } else {
            b.btnToggle.text = "Xem tất cả (${ds.size})"
            b.btnToggle.setIconResource(R.drawable.ic_expand_more)
        }
    }
}

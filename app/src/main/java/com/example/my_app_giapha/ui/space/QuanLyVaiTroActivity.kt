package com.example.my_app_giapha.ui.space

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import com.example.my_app_giapha.R
import com.example.my_app_giapha.data.DanhSachThanhVien
import com.example.my_app_giapha.data.DongThanhVien
import com.example.my_app_giapha.data.Kq
import com.example.my_app_giapha.data.Services
import com.example.my_app_giapha.databinding.ActivityQuanLyVaiTroBinding
import com.example.my_app_giapha.databinding.DialogBlockMemberBinding
import com.example.my_app_giapha.databinding.ItemRoleMemberBinding
import com.example.my_app_giapha.model.VaiTro
import com.example.my_app_giapha.ui.common.applyBottomInsetPadding
import com.example.my_app_giapha.ui.common.hienCuaO
import com.example.my_app_giapha.ui.common.khongDau
import com.example.my_app_giapha.ui.common.setBlockedBadge
import com.example.my_app_giapha.ui.common.setRoleBadge
import com.example.my_app_giapha.ui.common.setup
import com.example.my_app_giapha.ui.common.showConfirmDialog
import com.google.android.material.snackbar.Snackbar

/**
 * GD-19 — Quản lý vai trò và chặn thành viên. Chỉ Admin.
 * Dùng chung nguồn dữ liệu với GD-14 (danhSachThanhVien), bỏ chính Admin đang xem.
 * Mỗi dòng có hai nút riêng: cấp/hạ Quản lý và chặn/gỡ chặn.
 * Nút vai trò: "Cấp Quản lý" (Thành viên, chưa bị chặn), "Hạ xuống Thành viên" (Quản lý);
 * ẩn với Thành viên hờ, Khách tạm và với người đang bị chặn nếu là Thành viên.
 */
class QuanLyVaiTroActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ID_KHONG_GIAN = "idKhongGian"
    }

    private lateinit var b: ActivityQuanLyVaiTroBinding
    private val repo get() = Services.repository
    private var idKhongGian = 0
    private var daCoDuLieu = false
    private var tatCa: List<DongThanhVien> = emptyList()
    private var idDangChon: Int? = null          // dòng đang có hộp thoại mở: viền nâu

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityQuanLyVaiTroBinding.inflate(layoutInflater)
        setContentView(b.root)
        idKhongGian = intent.getIntExtra(EXTRA_ID_KHONG_GIAN, 0)

        b.appBar.setup(this, "Quản lý vai trò & chặn")
        b.scroll.applyBottomInsetPadding()
        b.etSearch.doAfterTextChanged {
            b.ivClear.visibility = if (it.isNullOrEmpty()) View.GONE else View.VISIBLE
            ve()
        }
        b.ivClear.setOnClickListener { b.etSearch.setText("") }
    }

    override fun onResume() {
        super.onResume()
        tai()
    }

    private fun tai() {
        if (!daCoDuLieu) b.stateView.showLoading()
        when (val r = repo.danhSachThanhVien(idKhongGian)) {
            is Kq.Loi -> {
                daCoDuLieu = false
                b.stateView.showError(r.thongBao) { tai() }
            }
            is Kq.Ok -> hien(r.giaTri)
        }
    }

    private fun hien(d: DanhSachThanhVien) {
        if (d.toi.vaiTro != VaiTro.ADMIN) {
            daCoDuLieu = false
            b.stateView.showError("Chỉ Admin mới vào được màn hình này") { tai() }
            return
        }
        daCoDuLieu = true
        b.stateView.hide()
        b.appBar.tvSubtitle.text = d.khongGian.tenKhongGian
        b.appBar.tvSubtitle.visibility = View.VISIBLE
        tatCa = d.thanhVien.filter { it.idTaiKhoan != d.toi.idTaiKhoan }   // không tự thao tác lên mình
        ve()
    }

    // ------------------------------------------------------------------ danh sách

    private fun ve() {
        if (!daCoDuLieu) return
        val q = khongDau(b.etSearch.text.toString().trim())
        val loc = tatCa.filter { q.isEmpty() || khongDau(it.tenHienThi).contains(q, ignoreCase = true) }
        b.list.removeAllViews()
        b.emptyBox.visibility = if (loc.isEmpty()) View.VISIBLE else View.GONE
        b.tvEmpty.text = if (tatCa.isEmpty() && q.isEmpty()) "Chưa có thành viên nào khác" else "Không tìm thấy kết quả"
        loc.forEach { b.list.addView(dong(it)) }
    }

    private fun dong(m: DongThanhVien): View {
        val r = ItemRoleMemberBinding.inflate(layoutInflater, b.list, false)
        val nau = ContextCompat.getColor(this, R.color.brown_primary)
        val do_ = ContextCompat.getColor(this, R.color.danger)

        r.avatar.hienCuaO(m.anhO, m.tenHienThi)
        r.tvName.text = m.tenHienThi
        r.tvRole.setRoleBadge(m.vaiTro.db)
        if (m.biChan) {
            r.root.alpha = 0.6f
            r.tvBlocked.setBlockedBadge()
            r.tvBlocked.visibility = View.VISIBLE
        }
        if (m.idTaiKhoan == idDangChon) r.root.setBackgroundResource(R.drawable.bg_card_selected)

        // Nút vai trò: chỉ Thành viên và Quản lý; Thành viên bị chặn thì ẩn nút Cấp Quản lý
        val nutVaiTro = when {
            m.vaiTro == VaiTro.QUAN_LY -> "Hạ xuống Thành viên"
            m.vaiTro == VaiTro.THANH_VIEN && !m.biChan -> "Cấp Quản lý"
            else -> null
        }
        if (nutVaiTro == null) {
            r.btnRole.visibility = View.GONE
            // Chỉ còn nút chặn: kéo dài hết dòng
            (r.btnBlock.layoutParams as LinearLayout.LayoutParams).apply { width = 0; weight = 1f }
        } else {
            r.btnRole.text = nutVaiTro
            r.btnRole.setOnClickListener {
                if (m.vaiTro == VaiTro.QUAN_LY) hoiHaQuyen(m) else hoiCapQuyen(m)
            }
        }

        // Nút chặn / gỡ chặn
        if (m.biChan) {
            r.btnBlock.text = "Gỡ chặn"
            r.btnBlock.setOnClickListener { hoiGoChan(m) }
        } else {
            r.btnBlock.text = "Chặn"
            r.btnBlock.setTextColor(do_)
            r.btnBlock.strokeColor = ColorStateList.valueOf(do_)
            r.btnBlock.setOnClickListener { hopChan(m) }
        }
        if (m.biChan) r.btnBlock.setTextColor(nau)
        return r.root
    }

    // ------------------------------------------------------------------ hộp xác nhận

    private fun danhDau(m: DongThanhVien) {
        idDangChon = m.idTaiKhoan
        ve()
    }

    private fun boDanhDau() {
        if (idDangChon != null) {
            idDangChon = null
            ve()
        }
    }

    private fun hoiCapQuyen(m: DongThanhVien) {
        danhDau(m)
        showConfirmDialog(
            title = "Cấp quyền Quản lý cho ${m.tenHienThi}?",
            message = "Người này sẽ có quyền chỉnh sửa cây, mời thành viên, nhưng không có quyền xóa Không gian hay quản lý thành viên khác.",
            destructive = false, showIcon = false,
            onCancel = { boDanhDau() }
        ) {
            xuLy(repo.capQuanLy(idKhongGian, m.idTaiKhoan), "Đã cấp quyền Quản lý cho ${m.tenHienThi}")
        }
    }

    private fun hoiHaQuyen(m: DongThanhVien) {
        danhDau(m)
        showConfirmDialog(
            title = "Hạ ${m.tenHienThi} xuống Thành viên?",
            message = "",
            destructive = false, showIcon = false,
            onCancel = { boDanhDau() }
        ) {
            xuLy(repo.haXuongThanhVien(idKhongGian, m.idTaiKhoan), "Đã hạ ${m.tenHienThi} xuống Thành viên")
        }
    }

    private fun hoiGoChan(m: DongThanhVien) {
        danhDau(m)
        showConfirmDialog(
            title = "Gỡ chặn ${m.tenHienThi}?",
            message = "",
            destructive = false, showIcon = false,
            onCancel = { boDanhDau() }
        ) {
            xuLy(repo.goChan(idKhongGian, m.idTaiKhoan), "Đã gỡ chặn ${m.tenHienThi}")
        }
    }

    /** Hộp chặn có ô lý do (tùy chọn); nút Xác nhận luôn bật. */
    private fun hopChan(m: DongThanhVien) {
        danhDau(m)
        val d = DialogBlockMemberBinding.inflate(LayoutInflater.from(this))
        d.tvTitle.text = "Chặn ${m.tenHienThi} khỏi không gian này?"
        val dialog = AlertDialog.Builder(this).setView(d.root).create()
        var daXacNhan = false
        d.btnCancel.setOnClickListener { dialog.dismiss() }
        d.btnConfirm.setOnClickListener {
            daXacNhan = true
            dialog.dismiss()
            xuLy(repo.chanThanhVien(idKhongGian, m.idTaiKhoan, d.etReason.text.toString()), "Đã chặn ${m.tenHienThi}")
        }
        dialog.setOnDismissListener { if (!daXacNhan) boDanhDau() }
        dialog.show()
        val le = (24 * resources.displayMetrics.density).toInt()
        dialog.window?.apply {
            setBackgroundDrawable(InsetDrawable(ColorDrawable(Color.TRANSPARENT), le, 0, le, 0))
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }
    }

    private fun xuLy(r: Kq<Unit>, thanhCong: String) {
        idDangChon = null
        when (r) {
            is Kq.Loi -> {
                ve()
                Snackbar.make(b.root, r.thongBao, Snackbar.LENGTH_LONG).show()
            }
            is Kq.Ok -> {
                tai()
                Snackbar.make(b.root, thanhCong, Snackbar.LENGTH_SHORT).show()
            }
        }
    }
}

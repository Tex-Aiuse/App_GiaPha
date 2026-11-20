package com.example.my_app_giapha.ui.space

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.doAfterTextChanged
import com.example.my_app_giapha.R
import com.example.my_app_giapha.data.DanhSachThanhVien
import com.example.my_app_giapha.data.DongThanhVien
import com.example.my_app_giapha.data.Kq
import com.example.my_app_giapha.data.Services
import com.example.my_app_giapha.databinding.ActivityMemberListBinding
import com.example.my_app_giapha.databinding.ItemMemberBinding
import com.example.my_app_giapha.model.ThanVienTrongKGian
import com.example.my_app_giapha.model.VaiTro
import com.example.my_app_giapha.ui.Routes
import com.example.my_app_giapha.ui.common.applyBottomInsetPadding
import com.example.my_app_giapha.ui.common.applyBottomMarginInset
import com.example.my_app_giapha.ui.common.hienCuaO
import com.example.my_app_giapha.ui.common.khongDau
import com.example.my_app_giapha.ui.common.setAction
import com.example.my_app_giapha.ui.common.setBlockedBadge
import com.example.my_app_giapha.ui.common.setRoleBadge
import com.example.my_app_giapha.ui.common.setup

/**
 * GD-14 — Danh sách thành viên của không gian. Mọi vai trò xem được, nội dung giống nhau.
 * Chỉ Admin thấy biểu tượng Quản lý vai trò (GD-19). Admin, Quản lý thấy nút Mời (GD-15) và nút Gán Ô (GD-17).
 * Tải lại mỗi lần hiện lại, để thấy thay đổi sau khi quay về từ các màn trên.
 */
class MemberListActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ID_KHONG_GIAN = "idKhongGian"
    }

    private lateinit var b: ActivityMemberListBinding
    private val repo get() = Services.repository
    private var idKhongGian = 0
    private var daCoDuLieu = false

    private var tatCa: List<DongThanhVien> = emptyList()
    private var toi: ThanVienTrongKGian? = null
    private var vaiTroLoc: VaiTro? = null          // null = Tất cả
    private val chips = mutableListOf<Pair<TextView, VaiTro?>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMemberListBinding.inflate(layoutInflater)
        setContentView(b.root)
        idKhongGian = intent.getIntExtra(EXTRA_ID_KHONG_GIAN, 0)

        b.appBar.setup(this, "Thành viên")
        b.scroll.applyBottomInsetPadding()
        b.fab.applyBottomMarginInset()
        b.fab.setOnClickListener { Routes.moMoiThanhVien(this, idKhongGian) }

        taoChip()
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
                b.fab.visibility = View.GONE
                b.stateView.showError(r.thongBao) { tai() }
            }
            is Kq.Ok -> hien(r.giaTri)
        }
    }

    private fun hien(d: DanhSachThanhVien) {
        daCoDuLieu = true
        b.stateView.hide()
        toi = d.toi
        tatCa = d.thanhVien
        b.appBar.tvSubtitle.text = d.khongGian.tenKhongGian
        b.appBar.tvSubtitle.visibility = View.VISIBLE
        if (d.toi.vaiTro == VaiTro.ADMIN) {
            b.appBar.setAction(this, R.drawable.ic_manage_accounts, "Quản lý vai trò và chặn thành viên") {
                Routes.moQuanLyVaiTro(this, idKhongGian)
            }
        } else {
            b.appBar.actionContainer.removeAllViews()
        }
        b.fab.visibility = if (d.toi.vaiTro.laQuanTri) View.VISIBLE else View.GONE
        ve()
    }

    // ------------------------------------------------------------------ chip lọc vai trò

    private fun taoChip() {
        val d = resources.displayMetrics.density
        val font = ResourcesCompat.getFont(this, R.font.be_vietnam_pro_semibold)
        (listOf<VaiTro?>(null) + VaiTro.entries).forEach { vt ->
            val tv = TextView(this).apply {
                text = vt?.nhan ?: "Tất cả"
                textSize = 14f
                typeface = font
                gravity = Gravity.CENTER
                setPadding((16 * d).toInt(), 0, (16 * d).toInt(), 0)
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, (40 * d).toInt()
                ).apply { marginEnd = (8 * d).toInt() }
                setOnClickListener { vaiTroLoc = vt; capNhatChip(); ve() }
            }
            chips.add(tv to vt)
            b.chips.addView(tv)
        }
        capNhatChip()
    }

    private fun capNhatChip() {
        val d = resources.displayMetrics.density
        for ((tv, vt) in chips) {
            val chon = vt == vaiTroLoc
            tv.background = GradientDrawable().apply {
                cornerRadius = 100 * d
                setColor(getColor(if (chon) R.color.brown_primary else R.color.surface))
                setStroke((1 * d).toInt(), getColor(R.color.brown_primary))
            }
            tv.setTextColor(getColor(if (chon) R.color.white else R.color.brown_primary))
        }
    }

    // ------------------------------------------------------------------ danh sách

    private fun ve() {
        if (!daCoDuLieu) return
        val q = khongDau(b.etSearch.text.toString().trim())
        val loc = tatCa.filter {
            (vaiTroLoc == null || it.vaiTro == vaiTroLoc) &&
                    (q.isEmpty() || khongDau(it.tenHienThi).contains(q, ignoreCase = true))
        }
        b.list.removeAllViews()
        b.emptyBox.visibility = if (loc.isEmpty()) View.VISIBLE else View.GONE
        loc.forEach { b.list.addView(dong(it)) }
    }

    private fun dong(m: DongThanhVien): View {
        val r = ItemMemberBinding.inflate(layoutInflater, b.list, false)
        val quanTri = toi?.vaiTro?.laQuanTri == true

        r.avatar.hienCuaO(m.anhO, m.tenHienThi)
        r.tvName.text = m.tenHienThi
        r.tvRole.setRoleBadge(m.vaiTro.db)
        if (m.hoTenO != null) {
            r.tvSub.text = "Ứng với: ${m.hoTenO}"
            r.tvSub.visibility = View.VISIBLE
        }

        when {
            // Bị chặn: dòng mờ, chỉ có nhãn đỏ, không có thao tác nào
            m.biChan -> {
                r.root.alpha = 0.5f
                r.tvBlocked.setBlockedBadge()
                r.tvBlocked.visibility = View.VISIBLE
            }
            // Thành viên hờ chưa có Ô: Admin, Quản lý thấy nút Gán Ô
            quanTri && m.vaiTro == VaiTro.THANH_VIEN_HO && m.idO == null -> {
                r.btnAssign.visibility = View.VISIBLE
                r.btnAssign.setOnClickListener { Routes.moGanO(this, idKhongGian, m.idTaiKhoan) }
            }
            // Đã gán Ô: bấm để xem Ô đó (GD-08)
            m.idO != null -> {
                r.ivChevron.visibility = View.VISIBLE
                r.root.setOnClickListener { Routes.moChiTietO(this, idKhongGian, m.idO) }
            }
        }
        return r.root
    }
}
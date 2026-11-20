package com.example.my_app_giapha.ui.space

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.example.my_app_giapha.R
import com.example.my_app_giapha.data.Kq
import com.example.my_app_giapha.data.KetQuaTimSdt
import com.example.my_app_giapha.data.LoiApp
import com.example.my_app_giapha.data.Services
import com.example.my_app_giapha.databinding.ActivityInviteMemberBinding
import com.example.my_app_giapha.ui.common.applyBottomInsetPadding
import com.example.my_app_giapha.ui.common.attachPhoneFormat
import com.example.my_app_giapha.ui.common.formatPhone
import com.example.my_app_giapha.ui.common.hienCuaO
import com.example.my_app_giapha.ui.common.setup
import com.google.android.material.snackbar.Snackbar

/**
 * GD-15 — Mời một tài khoản đã có vào không gian bằng số điện thoại, vai trò Thành viên hờ. Chỉ Admin, Quản lý.
 * Tự tìm khi đủ 10 số. Mời xong quay về GD-14 (GD-14 tự tải lại ở onResume).
 */
class InviteMemberActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ID_KHONG_GIAN = "idKhongGian"
    }

    private lateinit var b: ActivityInviteMemberBinding
    private val repo get() = Services.repository
    private var idKhongGian = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        idKhongGian = intent.getIntExtra(EXTRA_ID_KHONG_GIAN, 0)
        if (repo.vaiTroCuaToi(idKhongGian)?.vaiTro?.laQuanTri != true) { finish(); return }

        b = ActivityInviteMemberBinding.inflate(layoutInflater)
        setContentView(b.root)
        b.appBar.setup(this, "Mời thành viên")
        b.body.applyBottomInsetPadding()

        b.noteJoined.background = GradientDrawable().apply {
            cornerRadius = 12 * resources.displayMetrics.density
            setColor(getColor(R.color.green_light))
        }

        b.etPhone.attachPhoneFormat { digits -> khiDoiSo(digits) }
        b.ivClear.setOnClickListener { b.etPhone.setText("") }

        // Mở màn là gõ được ngay, như thiết kế
        b.etPhone.requestFocus()
        b.etPhone.post {
            WindowCompat.getInsetsController(window, b.etPhone).show(WindowInsetsCompat.Type.ime())
        }
    }

    private fun khiDoiSo(digits: String) {
        b.ivClear.visibility = if (digits.isEmpty()) View.GONE else View.VISIBLE
        loi(null)
        if (digits.length < 10) hienNhom(b.groupDefault) else tim(digits)
    }

    private fun tim(digits: String) {
        b.stateView.showLoading()
        when (val r = repo.timTaiKhoanTheoSdt(idKhongGian, digits)) {
            is Kq.Loi -> {
                if (r.loi == LoiApp.DU_LIEU_KHONG_HOP_LE) {
                    b.stateView.hide()
                    hienNhom(b.groupDefault)
                    loi(r.thongBao)
                } else {
                    b.stateView.showError(r.thongBao) { tim(digits) }
                }
            }
            is Kq.Ok -> {
                b.stateView.hide()
                hienKetQua(r.giaTri)
            }
        }
    }

    private fun hienKetQua(k: KetQuaTimSdt?) {
        if (k == null) { hienNhom(b.groupNotFound); return }
        hienNhom(b.groupResult)
        b.avatar.hienCuaO(null, k.tenHienThi)
        b.tvName.text = k.tenHienThi
        b.tvPhone.text = formatPhone(k.sdt)

        val vt = k.vaiTroHienTai
        if (vt == null) {
            b.noteJoined.visibility = View.GONE
            b.btnInvite.visibility = View.VISIBLE
            b.btnInvite.isEnabled = true
            b.btnInvite.text = "Mời"
            b.btnInvite.setOnClickListener { moi(k) }
        } else {
            b.btnInvite.visibility = View.GONE
            b.noteJoined.visibility = View.VISIBLE
            b.tvNote.text = if (k.biChan) "Người này đã bị chặn khỏi Không gian này"
            else "Người này đã tham gia Không gian với vai trò ${vt.nhan}"
        }
    }

    private fun moi(k: KetQuaTimSdt) {
        b.btnInvite.isEnabled = false
        b.btnInvite.text = "Đang mời..."
        when (val r = repo.moiTrucTiep(idKhongGian, k.idTaiKhoan)) {
            is Kq.Loi -> {
                b.btnInvite.isEnabled = true
                b.btnInvite.text = "Mời"
                Snackbar.make(b.root, r.thongBao, Snackbar.LENGTH_LONG).show()
            }
            is Kq.Ok -> {
                Toast.makeText(this, "Đã mời ${k.tenHienThi} làm Thành viên hờ", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    // ------------------------------------------------------------------ phụ trợ

    private fun hienNhom(nhom: View) {
        b.groupDefault.visibility = View.GONE
        b.groupResult.visibility = View.GONE
        b.groupNotFound.visibility = View.GONE
        nhom.visibility = View.VISIBLE
    }

    private fun loi(thongBao: String?) {
        b.fieldSearch.isActivated = thongBao != null
        b.tvError.text = thongBao
        b.tvError.visibility = if (thongBao != null) View.VISIBLE else View.GONE
    }
}
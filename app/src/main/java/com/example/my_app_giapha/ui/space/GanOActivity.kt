package com.example.my_app_giapha.ui.space

import android.app.Activity
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.example.my_app_giapha.R
import com.example.my_app_giapha.data.KetQuaTim
import com.example.my_app_giapha.data.Kq
import com.example.my_app_giapha.data.LoiApp
import com.example.my_app_giapha.data.Services
import com.example.my_app_giapha.databinding.ActivityGanOBinding
import com.example.my_app_giapha.databinding.ItemSearchResultBinding
import com.example.my_app_giapha.model.VaiTro
import com.example.my_app_giapha.ui.Routes
import com.example.my_app_giapha.ui.common.applyBottomInsetPadding
import com.example.my_app_giapha.ui.common.hienCuaO
import com.example.my_app_giapha.ui.common.setup

/**
 * GD-17 — Gán Ô cho Thành viên hờ. Chỉ Admin, Quản lý. Vào từ nút "Gán Ô" ở GD-14.
 * Hai lựa chọn:
 *  - "Chọn Ô có sẵn": tìm trong các Ô chưa gán cho ai, chọn một Ô rồi bấm "Xác nhận gán".
 *  - "Tạo Ô mới": mở GD-07 (thêm mới) kèm cờ "gán luôn"; lưu xong GD-07 tự gán rồi đóng cả hai màn.
 * Gán xong thì đóng màn này, GD-14 tự tải lại ở onResume và hiện vai trò "Thành viên".
 */
class GanOActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ID_KHONG_GIAN = "idKhongGian"
        const val EXTRA_ID_TAI_KHOAN = "idTaiKhoan"
        private const val TRE_GIA_LAP_MS = 600L   // giả lập thời gian gọi mạng để thấy trạng thái "Đang gán..."
    }

    private lateinit var b: ActivityGanOBinding
    private val repo get() = Services.repository
    private val handler = Handler(Looper.getMainLooper())
    private val d get() = resources.displayMetrics.density

    private var idKhongGian = 0
    private var idTaiKhoan = 0
    private var tenHo = ""

    private var daChonNhanh = false          // đã bấm "Chọn Ô có sẵn"
    private var dangGan = false
    private var ketQua: List<KetQuaTim> = emptyList()
    private var chon: KetQuaTim? = null

    /** Kết quả của GD-07: RESULT_OK nghĩa là đã tạo Ô và gán xong. */
    private val moForm = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { r ->
        if (r.resultCode == Activity.RESULT_OK) {
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        idKhongGian = intent.getIntExtra(EXTRA_ID_KHONG_GIAN, 0)
        idTaiKhoan = intent.getIntExtra(EXTRA_ID_TAI_KHOAN, 0)
        if (repo.vaiTroCuaToi(idKhongGian)?.vaiTro?.laQuanTri != true) { finish(); return }

        // Tên hiển thị của Thành viên hờ, và chắc chắn họ vẫn là hờ chưa có Ô
        val ds = repo.danhSachThanhVien(idKhongGian)
        val dong = (ds as? Kq.Ok)?.giaTri?.thanhVien?.firstOrNull { it.idTaiKhoan == idTaiKhoan }
        if (dong == null || dong.vaiTro != VaiTro.THANH_VIEN_HO || dong.idO != null) {
            Toast.makeText(this, "Tài khoản này không còn là Thành viên hờ chưa có Ô", Toast.LENGTH_LONG).show()
            finish(); return
        }
        tenHo = dong.tenHienThi

        b = ActivityGanOBinding.inflate(layoutInflater)
        setContentView(b.root)
        b.appBar.setup(this, "Gán Ô — $tenHo")
        b.bottomBar.applyBottomInsetPadding()
        b.tvIntro.text = "Chọn Ô đại diện cho $tenHo trên cây gia phả, hoặc tạo Ô mới nếu chưa có."

        b.cardChon.setOnClickListener { if (!dangGan) chonNhanhCoSan() }
        b.cardTao.setOnClickListener {
            if (!dangGan) moForm.launch(Routes.intentThemOVaGan(this, idKhongGian, idTaiKhoan))
        }
        b.etSearch.doAfterTextChanged { tim(it?.toString().orEmpty()) }
        b.btnConfirm.setOnClickListener { xacNhanGan() }
        veTheLuaChon()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    // ------------------------------------------------------------------ nhánh "Chọn Ô có sẵn"

    private fun chonNhanhCoSan() {
        if (daChonNhanh) return
        daChonNhanh = true
        b.groupChon.visibility = View.VISIBLE
        b.bottomBar.visibility = View.VISIBLE
        veTheLuaChon()
        capNhatNut()
        b.etSearch.requestFocus()
    }

    /** Gõ lại từ khóa thì bỏ chọn cũ và tìm lại. */
    private fun tim(q: String) {
        chon = null
        anLoi()
        timVaVe(q)
        capNhatNut()
    }

    /** Tìm theo từ khóa trong ô tìm; từ khóa trống thì không hiện gì. */
    private fun timVaVe(q: String) {
        ketQua = emptyList()
        var khongThay = false
        if (q.isNotBlank()) {
            when (val r = repo.timOChuaGan(idKhongGian, q.trim())) {
                is Kq.Loi -> { hienLoi(r.thongBao) }
                is Kq.Ok -> { ketQua = r.giaTri; khongThay = r.giaTri.isEmpty() }
            }
        }
        b.emptyBox.visibility = if (khongThay) View.VISIBLE else View.GONE
        veKetQua()
    }

    private fun veKetQua() {
        b.resultsBox.removeAllViews()
        val inflater = LayoutInflater.from(this)
        for (kq in ketQua) {
            val r = ItemSearchResultBinding.inflate(inflater, b.resultsBox, false)
            val bat = chon?.idO == kq.idO
            r.tvName.text = kq.hoTen
            r.avatar.hienCuaO(kq.anh, kq.hoTen)
            r.tvContext.text = kq.nguCanh
            r.ivCheck.visibility = if (bat) View.VISIBLE else View.GONE
            r.root.background = GradientDrawable().apply {
                cornerRadius = 12 * d
                setColor(getColor(if (bat) R.color.green_light else R.color.surface))
                setStroke(d.toInt(), getColor(if (bat) R.color.green_mid else R.color.border_default))
            }
            r.root.alpha = if (dangGan) 0.6f else 1f
            r.root.setOnClickListener {
                if (dangGan) return@setOnClickListener
                chon = kq
                anLoi()
                veKetQua()
                capNhatNut()
            }
            b.resultsBox.addView(r.root)
        }
    }

    // ------------------------------------------------------------------ xác nhận gán

    private fun xacNhanGan() {
        val o = chon ?: return
        if (dangGan) return
        anLoi()
        datDangGan(true)
        handler.postDelayed({
            when (val r = repo.ganO(idKhongGian, idTaiKhoan, o.idO)) {
                is Kq.Ok -> {
                    Toast.makeText(this, "Đã gán Ô ${o.hoTen} cho $tenHo", Toast.LENGTH_SHORT).show()
                    setResult(Activity.RESULT_OK)
                    finish()
                }
                is Kq.Loi -> {
                    datDangGan(false)
                    hienLoi(r.thongBao)
                    // Ô vừa bị người khác lấy hoặc không còn: bỏ chọn và tải lại danh sách cho đúng
                    if (r.loi == LoiApp.DA_TON_TAI || r.loi == LoiApp.KHONG_TIM_THAY) {
                        chon = null
                        timVaVe(b.etSearch.text.toString())
                    }
                    capNhatNut()
                }
            }
        }, TRE_GIA_LAP_MS)
    }

    /** Trạng thái "Đang gán...": khóa mọi thao tác, nút hiện vòng xoay. */
    private fun datDangGan(dang: Boolean) {
        dangGan = dang
        b.etSearch.isEnabled = !dang
        b.fieldSearch.alpha = if (dang) 0.5f else 1f
        b.progress.visibility = if (dang) View.VISIBLE else View.GONE
        b.btnConfirm.text = if (dang) "Đang gán..." else "Xác nhận gán"
        veTheLuaChon()
        veKetQua()
        capNhatNut()
    }

    private fun capNhatNut() {
        b.btnConfirm.isEnabled = chon != null && !dangGan
    }

    // ------------------------------------------------------------------ giao diện phụ

    private fun hienLoi(text: String) {
        b.tvLoi.text = text
        b.bannerLoi.visibility = View.VISIBLE
    }

    private fun anLoi() {
        b.bannerLoi.visibility = View.GONE
    }

    /** Vẽ hai thẻ lựa chọn: thẻ "Chọn Ô có sẵn" thu gọn 56dp, nền xanh nhạt, viền nâu, có dấu chọn khi đã vào nhánh. */
    private fun veTheLuaChon() {
        val cao = ((if (daChonNhanh) 56 else 72) * d).toInt()
        b.cardChon.layoutParams = b.cardChon.layoutParams.apply { height = cao }
        b.cardTao.layoutParams = b.cardTao.layoutParams.apply { height = cao }
        b.ivCheckChon.visibility = if (daChonNhanh) View.VISIBLE else View.GONE
        b.cardChon.background = nenThe(daChonNhanh)
        b.cardTao.background = nenThe(false)
        b.cardChon.alpha = 1f
        b.cardTao.alpha = if (dangGan) 0.5f else 1f
    }

    private fun nenThe(chonRoi: Boolean) = GradientDrawable().apply {
        cornerRadius = 16 * d
        setColor(getColor(if (chonRoi) R.color.green_light else R.color.surface))
        setStroke(
            ((if (chonRoi) 1.5f else 1f) * d).toInt(),
            getColor(if (chonRoi) R.color.brown_primary else R.color.border_default)
        )
    }
}

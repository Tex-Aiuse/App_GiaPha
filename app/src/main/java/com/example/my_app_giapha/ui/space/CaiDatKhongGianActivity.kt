package com.example.my_app_giapha.ui.space

import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.example.my_app_giapha.R
import com.example.my_app_giapha.data.Kq
import com.example.my_app_giapha.data.Services
import com.example.my_app_giapha.databinding.ActivityCaiDatKhongGianBinding
import com.example.my_app_giapha.model.VaiTro
import com.example.my_app_giapha.ui.Routes
import com.example.my_app_giapha.ui.common.applyBottomInsetPadding
import com.example.my_app_giapha.ui.common.setup
import com.example.my_app_giapha.ui.common.showConfirmDialog
import com.google.android.material.snackbar.Snackbar

/**
 * GD-18 — Cài đặt Không gian. Chỉ Admin (Quản lý không vào được).
 * Ba việc: đổi tên (nút Lưu riêng), bật/tắt chế độ riêng tư (bật thì hỏi xác nhận, tắt thì làm luôn),
 * xóa không gian (hỏi xác nhận, xong về GD-03).
 * Quay lại GD-06 thì GD-06 tự tải lại ở onResume nên thấy tên mới và biểu tượng riêng tư.
 */
class CaiDatKhongGianActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ID_KHONG_GIAN = "idKhongGian"
        private const val TRE_GIA_LAP_MS = 600L   // giả lập thời gian gọi mạng để thấy trạng thái đang lưu/xóa
    }

    private lateinit var b: ActivityCaiDatKhongGianBinding
    private val repo get() = Services.repository
    private val handler = Handler(Looper.getMainLooper())

    private var idKhongGian = 0
    private var tenGoc = ""                 // tên đã lưu của không gian
    private var ban = false                 // đang lưu / đổi riêng tư / xóa: khóa mọi thao tác
    private var datLaiCongTac = false       // true khi code tự đặt lại công tắc, không phải người dùng gạt

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        idKhongGian = intent.getIntExtra(EXTRA_ID_KHONG_GIAN, 0)
        if (repo.vaiTroCuaToi(idKhongGian)?.vaiTro != VaiTro.ADMIN) { finish(); return }

        b = ActivityCaiDatKhongGianBinding.inflate(layoutInflater)
        setContentView(b.root)
        b.appBar.setup(this, "Cài đặt không gian")
        b.scroll.applyBottomInsetPadding()

        // Công tắc: xanh vừa khi bật, xám khi tắt (cùng kiểu công tắc "Còn sống" ở GD-07)
        b.swPrivate.trackTintList = ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
            intArrayOf(getColor(R.color.green_mid), getColor(R.color.border_default))
        )

        b.etName.doAfterTextChanged {
            b.etName.isActivated = false
            b.tvNameError.visibility = View.GONE
            capNhatNutLuu()
        }
        b.btnSave.setOnClickListener { luuTen() }
        b.swPrivate.setOnCheckedChangeListener { _, bat ->
            if (datLaiCongTac) return@setOnCheckedChangeListener
            if (bat) hoiBatRiengTu() else doiRiengTu(false)
        }
        b.btnDelete.setOnClickListener { hoiXoa() }

        tai()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    // ------------------------------------------------------------------ tải

    private fun tai() {
        b.stateView.showLoading()
        when (val r = repo.danhSachKhongGian()) {
            is Kq.Loi -> b.stateView.showError(r.thongBao) { tai() }
            is Kq.Ok -> {
                val kg = r.giaTri.firstOrNull { it.khongGian.idKhongGian == idKhongGian }?.khongGian
                if (kg == null) {
                    Toast.makeText(this, "Không tìm thấy không gian này", Toast.LENGTH_LONG).show()
                    finish(); return
                }
                b.stateView.hide()
                tenGoc = kg.tenKhongGian
                b.etName.setText(tenGoc)
                datCongTac(kg.cheDoRiengTu)
                capNhatNutLuu()
            }
        }
    }

    // ------------------------------------------------------------------ đổi tên

    private fun capNhatNutLuu() {
        b.btnSave.isEnabled = !ban && b.etName.text.toString().trim() != tenGoc
    }

    private fun luuTen() {
        val ten = b.etName.text.toString().trim()
        if (ten.isEmpty()) {
            b.etName.isActivated = true
            b.tvNameError.visibility = View.VISIBLE
            return
        }
        datBan(true)
        b.btnSave.text = "Đang lưu..."
        handler.postDelayed({
            when (val r = repo.doiTenKhongGian(idKhongGian, ten)) {
                is Kq.Ok -> {
                    tenGoc = r.giaTri.tenKhongGian
                    b.etName.setText(tenGoc)
                    b.etName.clearFocus()
                    Toast.makeText(this, "Đã lưu tên không gian", Toast.LENGTH_SHORT).show()
                }
                is Kq.Loi -> Snackbar.make(b.root, r.thongBao, Snackbar.LENGTH_LONG).show()
            }
            b.btnSave.text = "Lưu"
            datBan(false)
        }, TRE_GIA_LAP_MS)
    }

    // ------------------------------------------------------------------ chế độ riêng tư

    /** Bật: hỏi xác nhận (ảnh hưởng ngay tới khách đang xem). Hủy hoặc đóng hộp thoại thì gạt công tắc về tắt. */
    private fun hoiBatRiengTu() {
        showConfirmDialog(
            title = "Bật chế độ riêng tư?",
            message = "Không ai tạo hoặc dùng được mã mời cho khách — kể cả mã đã tạo trước đó. " +
                    "Khách đang xem tạm sẽ bị ngắt truy cập ngay lập tức.",
            confirmText = "Xác nhận",
            destructive = false,
            showIcon = true,
            iconRes = R.drawable.ic_lock_brown,
            onCancel = { datCongTac(false) }
        ) { doiRiengTu(true) }
    }

    private fun doiRiengTu(bat: Boolean) {
        datBan(true)
        handler.postDelayed({
            when (val r = repo.datRiengTu(idKhongGian, bat)) {
                is Kq.Ok -> Toast.makeText(
                    this, if (bat) "Đã bật chế độ riêng tư" else "Đã tắt chế độ riêng tư", Toast.LENGTH_SHORT
                ).show()
                is Kq.Loi -> {
                    datCongTac(!bat)     // không đổi được: trả công tắc về như cũ
                    Snackbar.make(b.root, r.thongBao, Snackbar.LENGTH_LONG).show()
                }
            }
            datBan(false)
        }, TRE_GIA_LAP_MS)
    }

    /** Đặt công tắc bằng code mà không kích hoạt xử lý "người dùng gạt". */
    private fun datCongTac(bat: Boolean) {
        datLaiCongTac = true
        b.swPrivate.isChecked = bat
        datLaiCongTac = false
    }

    // ------------------------------------------------------------------ xóa không gian

    private fun hoiXoa() {
        showConfirmDialog(
            title = "Xóa $tenGoc?",
            message = "Toàn bộ dữ liệu — thành viên, cây gia phả, tiểu sử, bình luận — sẽ bị xóa vĩnh viễn. " +
                    "Hành động này không thể hoàn tác.",
            confirmText = "Xóa",
            destructive = true
        ) { xoa() }
    }

    private fun xoa() {
        datBan(true)
        b.btnDelete.text = "Đang xóa..."
        handler.postDelayed({
            when (val r = repo.xoaKhongGian(idKhongGian)) {
                is Kq.Ok -> {
                    Toast.makeText(this, "Đã xóa không gian", Toast.LENGTH_SHORT).show()
                    Routes.veDanhSachKhongGian(this)   // không gian không còn: về GD-03, dọn các màn cũ
                }
                is Kq.Loi -> {
                    b.btnDelete.text = "Xóa Không gian"
                    datBan(false)
                    Snackbar.make(b.root, r.thongBao, Snackbar.LENGTH_LONG).show()
                }
            }
        }, TRE_GIA_LAP_MS)
    }

    // ------------------------------------------------------------------ khóa thao tác khi đang chạy

    private fun datBan(dang: Boolean) {
        ban = dang
        b.etName.isEnabled = !dang
        b.swPrivate.isEnabled = !dang
        b.btnDelete.isEnabled = !dang
        capNhatNutLuu()
    }
}

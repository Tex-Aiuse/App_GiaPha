package com.example.my_app_giapha.ui.admin

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.example.my_app_giapha.R
import com.example.my_app_giapha.data.Kq
import com.example.my_app_giapha.data.Services
import com.example.my_app_giapha.databinding.ActivityXoaKhongGianHeThongBinding
import com.example.my_app_giapha.ui.Routes
import com.example.my_app_giapha.ui.common.applyBottomInsetPadding
import com.example.my_app_giapha.ui.common.setup

/**
 * GD-25 — Xác nhận xóa không gian vi phạm (AdminApp). Bước cuối trước khi xóa vĩnh viễn.
 * Số liệu (tên, số thành viên, số Ô) nhận từ GD-24 qua Intent, không tải lại.
 * Lý do bắt buộc (tối đa 200 ký tự) nhưng bản 1.0 KHÔNG lưu: chưa có bảng nhật ký.
 * Bốn trạng thái: nhập lý do → đang xóa (khóa mọi thao tác) → đã xóa (về GD-23) hoặc lỗi (có nút Thử lại).
 */
class XoaKhongGianHeThongActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ID_KHONG_GIAN = "idKhongGian"
        const val EXTRA_TEN = "tenKhongGian"
        const val EXTRA_SO_THANH_VIEN = "soThanhVien"
        const val EXTRA_SO_O = "soO"
        private const val KEY_BUOC = "buoc"
        private const val TRE_GIA_LAP_MS = 800L   // giả lập thời gian giao dịch nhiều bước
        private const val TRE_VE_DANH_SACH_MS = 1200L
        private const val DA_MO = 1f
        private const val DA_KHOA = 0.4f
    }

    private enum class Buoc { NHAP, DANG_XOA, XONG, LOI }

    private lateinit var b: ActivityXoaKhongGianHeThongBinding
    private val repo get() = Services.repository
    private val handler = Handler(Looper.getMainLooper())
    private var idKhongGian = 0
    private var buoc = Buoc.NHAP

    private val chanBack = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() { /* đang xóa hoặc đã xóa: không cho quay lại */ }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityXoaKhongGianHeThongBinding.inflate(layoutInflater)
        setContentView(b.root)
        idKhongGian = intent.getIntExtra(EXTRA_ID_KHONG_GIAN, 0)
        onBackPressedDispatcher.addCallback(this, chanBack)

        b.appBar.setup(this, "Xác nhận xóa")
        b.actionBar.applyBottomInsetPadding()

        b.tvName.text = intent.getStringExtra(EXTRA_TEN).orEmpty()
        b.tvSoThanhVien.text = "${intent.getIntExtra(EXTRA_SO_THANH_VIEN, 0)} thành viên sẽ mất quyền truy cập"
        b.tvSoO.text = "${intent.getIntExtra(EXTRA_SO_O, 0)} Ô trên cây gia phả sẽ bị xóa"

        b.etReason.doAfterTextChanged {
            b.tvCount.text = "${it?.length ?: 0}/200"
            capNhatNut()
        }
        b.btnCancel.setOnClickListener { finish() }
        b.btnConfirm.setOnClickListener { xoa() }

        // Xoay màn hình giữa chừng: nhớ bước đang ở (ô lý do tự được khôi phục)
        buoc = savedInstanceState?.getString(KEY_BUOC)?.let { Buoc.valueOf(it) } ?: Buoc.NHAP
        when (buoc) {
            Buoc.NHAP -> capNhatNut()
            // Lệnh chờ cũ đã bị hủy khi màn hình bị tạo lại nên chạy lại từ đầu. Đợi sau khi ô lý do được khôi phục.
            Buoc.DANG_XOA -> b.root.post { xoa() }
            Buoc.XONG -> hienDaXoa()
            Buoc.LOI -> hienLoi()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_BUOC, buoc.name)
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    private fun capNhatNut() {
        b.btnConfirm.isEnabled = (buoc == Buoc.NHAP || buoc == Buoc.LOI) && b.etReason.text.toString().isNotBlank()
    }

    private fun xoa() {
        val lyDo = b.etReason.text.toString().trim()
        if (lyDo.isEmpty()) return
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(b.etReason.windowToken, 0)
        b.etReason.clearFocus()

        buoc = Buoc.DANG_XOA
        khoa(true)
        b.bannerError.visibility = View.GONE
        b.btnConfirm.text = "Đang xóa..."
        b.btnConfirm.icon = null
        b.progress.visibility = View.VISIBLE
        b.tick.visibility = View.GONE
        b.tvOverlayTitle.text = "Đang xóa không gian..."
        b.tvOverlayDesc.text = "Quá trình này có thể mất vài giây. Vui lòng không đóng ứng dụng."
        b.overlay.visibility = View.VISIBLE

        handler.postDelayed({
            when (repo.xoaKhongGianHeThong(idKhongGian, lyDo)) {
                is Kq.Ok -> hienDaXoa()
                is Kq.Loi -> hienLoi()
            }
        }, TRE_GIA_LAP_MS)
    }

    private fun hienDaXoa() {
        buoc = Buoc.XONG
        khoa(true)
        b.overlay.visibility = View.VISIBLE
        b.progress.visibility = View.GONE
        b.tick.visibility = View.VISIBLE
        b.tvOverlayTitle.text = "Đã xóa không gian"
        b.tvOverlayDesc.text = "Đang quay lại danh sách..."
        // Không gian không còn: về GD-23 và dọn các màn GD-24, GD-06 chồng phía sau
        handler.postDelayed({ Routes.moDanhSachKhongGianHeThong(this) }, TRE_VE_DANH_SACH_MS)
    }

    /** Lỗi: dữ liệu chưa đổi. Giữ nguyên lý do đã nhập, đổi nút thành "Thử lại". */
    private fun hienLoi() {
        buoc = Buoc.LOI
        khoa(false)
        b.overlay.visibility = View.GONE
        b.bannerError.visibility = View.VISIBLE
        b.btnConfirm.text = "Thử lại"
        b.btnConfirm.setIconResource(R.drawable.ic_refresh_white)
        capNhatNut()
        b.scroll.post { b.scroll.fullScroll(View.FOCUS_DOWN) }
    }

    /** Đang xóa / đã xóa: làm mờ và khóa mũi tên quay lại, ô lý do, hai nút. */
    private fun khoa(khoa: Boolean) {
        val alpha = if (khoa) DA_KHOA else DA_MO
        chanBack.isEnabled = khoa
        b.appBar.btnBack.isEnabled = !khoa
        b.appBar.btnBack.alpha = alpha
        b.etReason.isEnabled = !khoa
        b.etReason.alpha = alpha
        b.btnCancel.isEnabled = !khoa
        b.btnCancel.alpha = alpha
        if (khoa) b.btnConfirm.isEnabled = false
    }
}

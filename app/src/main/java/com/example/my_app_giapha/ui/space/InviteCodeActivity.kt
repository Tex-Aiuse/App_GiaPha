package com.example.my_app_giapha.ui.space

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.my_app_giapha.R
import com.example.my_app_giapha.data.Kq
import com.example.my_app_giapha.data.Services
import com.example.my_app_giapha.databinding.ActivityInviteCodeBinding
import com.example.my_app_giapha.model.VaiTro
import com.example.my_app_giapha.ui.common.applyBottomInsetPadding
import com.example.my_app_giapha.ui.common.setup
import java.time.Duration
import java.time.ZoneId
import java.util.Locale

/**
 * GD-16 — Tạo mã mời cho khách xem tạm. Admin, Quản lý, Thành viên, Thành viên hờ (không phải Khách tạm).
 * Mã dùng một lần, hiệu lực 5 phút; màn hình đếm ngược theo giờ hết hạn thật nên thu app rồi mở lại vẫn đúng.
 */
class InviteCodeActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ID_KHONG_GIAN = "idKhongGian"
        private const val KEY_MA = "ma"
        private const val KEY_HET_HAN = "hetHan"
        private const val KEY_TONG = "tong"
    }

    private lateinit var b: ActivityInviteCodeBinding
    private val repo get() = Services.repository
    private var idKhongGian = 0

    private var ma: String? = null
    private var hetHanMs = 0L        // giờ hết hạn (epoch millis)
    private var tongMs = 1L          // tổng thời gian hiệu lực, để tính phần trăm vòng

    private val handler = Handler(Looper.getMainLooper())
    private val nhipDem = Runnable { nhip() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        idKhongGian = intent.getIntExtra(EXTRA_ID_KHONG_GIAN, 0)
        val vt = repo.vaiTroCuaToi(idKhongGian)?.vaiTro
        if (vt == null || vt == VaiTro.KHACH_TAM) { finish(); return }

        b = ActivityInviteCodeBinding.inflate(layoutInflater)
        setContentView(b.root)
        b.appBar.setup(this, "Mã mời")
        b.appBar.btnBack.setImageResource(R.drawable.ic_close_white)
        b.appBar.btnBack.contentDescription = "Đóng"
        b.scroll.applyBottomInsetPadding()
        b.ringExpired.progress = 0f

        b.btnCreate.setOnClickListener { tao() }
        b.btnCreateAgain.setOnClickListener { tao() }
        b.btnCopy.setOnClickListener { sao() }
        b.btnShare.setOnClickListener { chiaSe() }

        // Xoay màn hình: khôi phục mã đang đếm
        savedInstanceState?.getString(KEY_MA)?.let {
            ma = it
            hetHanMs = savedInstanceState.getLong(KEY_HET_HAN)
            tongMs = savedInstanceState.getLong(KEY_TONG, 1L)
            hienMa()
        }
    }

    override fun onStart() {
        super.onStart()
        if (ma != null) nhip()
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacks(nhipDem)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        ma?.let {
            outState.putString(KEY_MA, it)
            outState.putLong(KEY_HET_HAN, hetHanMs)
            outState.putLong(KEY_TONG, tongMs)
        }
    }

    // ------------------------------------------------------------------ tạo mã

    private fun tao() {
        dongNutTao(true)
        when (val r = repo.taoMaMoi(idKhongGian)) {
            is Kq.Loi -> {
                dongNutTao(false)
                b.stateView.showError(r.thongBao) { b.stateView.hide(); tao() }
            }
            is Kq.Ok -> {
                dongNutTao(false)
                b.stateView.hide()
                val c = r.giaTri
                ma = c.ma
                hetHanMs = c.ngayHetHan.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                tongMs = maxOf(1L, Duration.between(c.ngayTao, c.ngayHetHan).toMillis())
                hienMa()
                nhip()
            }
        }
    }

    private fun dongNutTao(dang: Boolean) {
        for (btn in listOf(b.btnCreate, b.btnCreateAgain)) {
            btn.isEnabled = !dang
            btn.text = if (dang) "Đang tạo..." else "Tạo mã mới"
        }
    }

    // ------------------------------------------------------------------ đếm ngược

    private fun nhip() {
        handler.removeCallbacks(nhipDem)
        val con = hetHanMs - System.currentTimeMillis()
        if (con <= 0) { hienHetHan(); return }
        b.ring.progress = con.toFloat() / tongMs
        val giay = (con + 999) / 1000          // làm tròn lên: chỉ hiện 0:00 khi đã hết hạn thật
        b.tvTime.text = String.format(Locale.US, "%d:%02d", giay / 60, giay % 60)
        handler.postDelayed(nhipDem, 200)
    }

    private fun hienMa() {
        b.groupBefore.visibility = View.GONE
        b.groupResult.visibility = View.VISIBLE
        b.blockActive.visibility = View.VISIBLE
        b.blockExpired.visibility = View.GONE
        b.tvCode.text = ma
        b.tvCode.paintFlags = b.tvCode.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        b.tvCode.setTextColor(getColor(R.color.text_primary))
    }

    private fun hienHetHan() {
        b.groupBefore.visibility = View.GONE
        b.groupResult.visibility = View.VISIBLE
        b.blockActive.visibility = View.GONE
        b.blockExpired.visibility = View.VISIBLE
        b.tvCode.paintFlags = b.tvCode.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        b.tvCode.setTextColor(getColor(R.color.text_hint))
    }

    // ------------------------------------------------------------------ sao chép, chia sẻ

    private fun sao() {
        val m = ma ?: return
        getSystemService(ClipboardManager::class.java)
            .setPrimaryClip(ClipData.newPlainText("Mã mời", m))
        // Android 13 trở lên tự báo "đã sao chép", bản thấp hơn thì tự báo
        if (Build.VERSION.SDK_INT < 33) Toast.makeText(this, "Đã sao chép mã", Toast.LENGTH_SHORT).show()
    }

    private fun chiaSe() {
        val m = ma ?: return
        val i = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "Mã truy cập xem tạm cây gia phả: $m (hiệu lực 5 phút, chỉ dùng được 1 lần)")
        }
        startActivity(Intent.createChooser(i, "Chia sẻ mã"))
    }
}
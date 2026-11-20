package com.example.my_app_giapha.ui.person

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.my_app_giapha.databinding.ActivityCropAnhBinding
import com.example.my_app_giapha.ui.common.AnhFile
import com.example.my_app_giapha.ui.common.applyBottomInsetPadding
import com.example.my_app_giapha.ui.common.setup
import com.google.android.material.snackbar.Snackbar
import java.io.File

/**
 * Bước "cắt ảnh" của GD-11: nhận một ảnh (intent.data), cho cắt khung vuông 1:1.
 * "Dùng ảnh này" trả về đường dẫn file JPEG đã cắt và nén (EXTRA_KET_QUA).
 * "Chọn ảnh khác" mở lại bộ chọn ảnh ngay trong màn này.
 */
class CropAnhActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_KET_QUA = "duongDanAnhDaCat"
    }

    private lateinit var b: ActivityCropAnhBinding
    private var dangXuLy = false

    private val chonAnh = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) nap(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityCropAnhBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.appBar.setup(this, "Cắt ảnh đại diện")
        b.bottomBar.applyBottomInsetPadding()
        b.btnPick.setOnClickListener {
            if (!dangXuLy) chonAnh.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        b.btnUse.setOnClickListener { dung() }

        val uri: Uri? = intent.data
        if (uri == null) { setResult(Activity.RESULT_CANCELED); finish(); return }
        nap(uri)
    }

    /** Kiểm tra rồi đọc ảnh ở luồng nền, xong đưa vào khung cắt. */
    private fun nap(uri: Uri) {
        AnhFile.kiemTra(this, uri)?.let { thongBao(it); return }
        batDauXuLy(true)
        Thread {
            val bm = AnhFile.docAnh(this, uri)
            runOnUiThread {
                if (isFinishing || isDestroyed) return@runOnUiThread
                batDauXuLy(false)
                if (bm == null) thongBao("Không đọc được ảnh này. Hãy chọn ảnh khác.")
                else b.cropView.datAnh(bm)
            }
        }.start()
    }

    /** Cắt, nén JPEG vào thư mục tạm rồi trả đường dẫn về màn GD-11. */
    private fun dung() {
        if (dangXuLy) return
        val vuong = b.cropView.cat() ?: return
        batDauXuLy(true)
        Thread {
            val tep = File(AnhFile.thuMucTam(this), "crop_${System.currentTimeMillis()}.jpg")
            val ok = runCatching { AnhFile.luuJpeg(vuong, tep) }.isSuccess
            runOnUiThread {
                if (isFinishing || isDestroyed) return@runOnUiThread
                if (!ok) {
                    batDauXuLy(false)
                    thongBao("Không lưu được ảnh. Vui lòng thử lại.")
                } else {
                    setResult(Activity.RESULT_OK, Intent().putExtra(EXTRA_KET_QUA, tep.absolutePath))
                    finish()
                }
            }
        }.start()
    }

    private fun batDauXuLy(dang: Boolean) {
        dangXuLy = dang
        b.progress.visibility = if (dang) View.VISIBLE else View.GONE
        b.btnPick.isEnabled = !dang
        b.btnUse.isEnabled = !dang
    }

    private fun thongBao(s: String) = Snackbar.make(b.root, s, Snackbar.LENGTH_LONG).show()
}

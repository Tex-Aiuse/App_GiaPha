package com.example.my_app_giapha.ui.person

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.my_app_giapha.data.AnhDaiDienO
import com.example.my_app_giapha.data.Kq
import com.example.my_app_giapha.data.Services
import com.example.my_app_giapha.databinding.ActivityAnhDaiDienBinding
import com.example.my_app_giapha.databinding.DialogAvatarHistoryBinding
import com.example.my_app_giapha.databinding.ItemAvatarHistoryBinding
import com.example.my_app_giapha.databinding.ItemAvatarOptionBinding
import com.example.my_app_giapha.model.Avatar
import com.example.my_app_giapha.model.HieuUngAvatar
import com.example.my_app_giapha.model.KhungAvatar
import com.example.my_app_giapha.ui.common.AnhFile
import com.example.my_app_giapha.ui.common.AnhLoader
import com.example.my_app_giapha.ui.common.AvatarView
import com.example.my_app_giapha.ui.common.applyBottomInsetPadding
import com.example.my_app_giapha.ui.common.initialOf
import com.example.my_app_giapha.ui.common.setup
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.time.format.DateTimeFormatter

/**
 * GD-11 — Quản lý ảnh đại diện của một Ô (chỉ Admin, Quản lý).
 * Phần này: ảnh đang dùng, chọn khung/hiệu ứng (xem trước, bấm Lưu mới ghi), lưới lịch sử,
 * hộp xem lại + "Dùng lại ảnh này". Tải ảnh mới: chọn từ thư viện → cắt vuông 1:1 (CropAnhActivity)
 * → xem trước ngay ở ảnh lớn, bấm Lưu mới ghi (ảnh cũ chuyển sang lịch sử).
 */
class AnhDaiDienActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ID_KHONG_GIAN = "idKhongGian"
        const val EXTRA_ID_O = "idO"
    }

    private lateinit var b: ActivityAnhDaiDienBinding
    private val repo get() = Services.repository
    private val fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private var idKhongGian = 0
    private var idO = 0
    private var du: AnhDaiDienO? = null
    private var anhDangDung: Bitmap? = null
    private var khungChon = KhungAvatar.TRON
    private var hieuChon = HieuUngAvatar.GOC

    // Ảnh mới đã cắt nhưng chưa lưu
    private var anhMoi: Bitmap? = null
    private var duongAnhMoi: String? = null
    private var vuaCoKetQua = false

    private val chonAnh = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri == null) return@registerForActivityResult
        AnhFile.kiemTra(this, uri)?.let {
            Snackbar.make(b.root, it, Snackbar.LENGTH_LONG).show()
            return@registerForActivityResult
        }
        cat.launch(
            Intent(this, CropAnhActivity::class.java).apply {
                data = uri
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        )
    }

    private val cat = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { r ->
        if (r.resultCode != Activity.RESULT_OK) return@registerForActivityResult
        val duong = r.data?.getStringExtra(CropAnhActivity.EXTRA_KET_QUA) ?: return@registerForActivityResult
        val bm = AnhLoader.tai(this, duong, AnhFile.CANH_TOI_DA)
        if (bm == null) {
            Snackbar.make(b.root, "Không đọc được ảnh vừa cắt", Snackbar.LENGTH_LONG).show()
            return@registerForActivityResult
        }
        boAnhMoi()
        anhMoi = bm
        duongAnhMoi = duong
        vuaCoKetQua = true       // onResume ngay sau đây không được tải lại làm mất khung/hiệu ứng đang chọn
        veXemTruoc()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityAnhDaiDienBinding.inflate(layoutInflater)
        setContentView(b.root)
        idKhongGian = intent.getIntExtra(EXTRA_ID_KHONG_GIAN, 0)
        idO = intent.getIntExtra(EXTRA_ID_O, 0)

        b.appBar.setup(this, "Ảnh đại diện")
        b.content.applyBottomInsetPadding()
        b.btnUpload.setOnClickListener {
            chonAnh.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        b.btnSave.setOnClickListener { luu() }
    }

    override fun onResume() {
        super.onResume()
        if (vuaCoKetQua) { vuaCoKetQua = false; return }   // vừa quay về từ màn cắt: giữ nguyên bản xem trước
        tai()
    }

    override fun onDestroy() {
        if (isFinishing) boAnhMoi()
        super.onDestroy()
    }

    /** Bỏ ảnh mới chưa lưu (và xóa file tạm của nó). */
    private fun boAnhMoi() {
        duongAnhMoi?.let { File(it).delete() }
        anhMoi = null
        duongAnhMoi = null
    }

    private fun tai() {
        if (du == null) b.stateView.showLoading()
        when (val r = repo.anhDaiDienCua(idO)) {
            is Kq.Loi -> {
                du = null
                b.stateView.showError(r.thongBao) { tai() }
            }
            is Kq.Ok -> hien(r.giaTri)
        }
    }

    // ------------------------------------------------------------------ hiển thị

    private fun hien(d: AnhDaiDienO) {
        du = d
        b.stateView.hide()
        b.appBar.tvTitle.text = "Ảnh đại diện — ${d.o.hoTen}"
        anhDangDung = AnhLoader.tai(this, d.dangDung?.duongDanAnh)
        khungChon = KhungAvatar.fromDb(d.dangDung?.loaiKhung)
        hieuChon = HieuUngAvatar.fromDb(d.dangDung?.hieuUng)
        veXemTruoc()
        veLichSu(d)
    }

    /** Vẽ lại ảnh lớn, hai hàng thumbnail và nút Lưu theo lựa chọn hiện tại (chưa lưu). */
    private fun veXemTruoc() {
        val d = du ?: return
        val chu = initialOf(d.o.hoTen)
        val anh = anhMoi ?: anhDangDung
        b.avatarMain.hien(anh, chu, khungChon, hieuChon)

        b.rowKhung.removeAllViews()
        for (k in KhungAvatar.entries) {
            val c = ItemAvatarOptionBinding.inflate(layoutInflater, b.rowKhung, false)
            val chon = k == khungChon
            c.avatar.hien(anh, chu, k, HieuUngAvatar.GOC, vienChon = chon, tick = chon)
            c.tvLabel.text = k.nhan
            c.tvLabel.setTypeface(c.tvLabel.typeface, if (chon) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
            c.root.setOnClickListener { khungChon = k; veXemTruoc() }
            b.rowKhung.addView(c.root)
        }

        b.rowHieuUng.removeAllViews()
        for (h in HieuUngAvatar.entries) {
            val c = ItemAvatarOptionBinding.inflate(layoutInflater, b.rowHieuUng, false)
            val chon = h == hieuChon
            c.avatar.hien(anh, chu, KhungAvatar.TRON, h, vienChon = chon, tick = chon, khongVien = true)
            c.tvLabel.text = h.nhan
            c.tvLabel.setTypeface(c.tvLabel.typeface, if (chon) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
            c.root.setOnClickListener { hieuChon = h; veXemTruoc() }
            b.rowHieuUng.addView(c.root)
        }

        // Lưu bật khi có ảnh mới chưa lưu, hoặc đã đổi khung/hiệu ứng so với bản đang dùng
        val g = d.dangDung
        b.btnSave.isEnabled = anhMoi != null || (g != null &&
                (khungChon != KhungAvatar.fromDb(g.loaiKhung) || hieuChon != HieuUngAvatar.fromDb(g.hieuUng)))
    }

    private fun veLichSu(d: AnhDaiDienO) {
        b.gridHistory.removeAllViews()
        b.tvHistoryEmpty.visibility = if (d.lichSu.isEmpty()) View.VISIBLE else View.GONE
        val le = (6 * resources.displayMetrics.density).toInt()
        val chu = initialOf(d.o.hoTen)
        for (a in d.lichSu) {
            val t = ItemAvatarHistoryBinding.inflate(layoutInflater, b.gridHistory, false)
            t.avatar.hien(AnhLoader.tai(this, a.duongDanAnh), chu, KhungAvatar.VUONG_BO_GOC, HieuUngAvatar.GOC, khongVien = true, banKinhDp = 12f)
            t.tvDate.text = a.ngayTao.format(fmt)
            t.root.setOnClickListener { hopXemLai(a, t.avatar) }
            val lp = GridLayout.LayoutParams(GridLayout.spec(GridLayout.UNDEFINED), GridLayout.spec(GridLayout.UNDEFINED, 1f)).apply {
                width = 0
                setMargins(le, le, le, le)
            }
            b.gridHistory.addView(t.root, lp)
        }
    }

    // ------------------------------------------------------------------ lưu

    private fun luu() {
        val moi = duongAnhMoi
        if (moi != null) { luuAnhMoi(moi); return }
        val g = du?.dangDung ?: return
        when (val r = repo.suaKieuAnh(g.idAvatar, khungChon.db, hieuChon.db)) {
            is Kq.Loi -> Snackbar.make(b.root, r.thongBao, Snackbar.LENGTH_LONG).show()
            is Kq.Ok -> {
                tai()
                Snackbar.make(b.root, "Đã lưu ảnh đại diện", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    /** Ảnh mới: chép vào bộ nhớ app rồi ghi thành ảnh đang dùng; ảnh cũ tự chuyển sang lịch sử. */
    private fun luuAnhMoi(tam: String) {
        b.btnSave.isEnabled = false
        val dich = runCatching { AnhFile.chepVaoAvatar(this, File(tam), idO) }.getOrNull()
        if (dich == null) {
            b.btnSave.isEnabled = true
            Snackbar.make(b.root, "Không lưu được ảnh. Vui lòng thử lại.", Snackbar.LENGTH_LONG).show()
            return
        }
        when (val r = repo.themAnhDaiDien(idO, dich.absolutePath, khungChon.db, hieuChon.db)) {
            is Kq.Loi -> {
                dich.delete()
                b.btnSave.isEnabled = true
                Snackbar.make(b.root, r.thongBao, Snackbar.LENGTH_LONG).show()
            }
            is Kq.Ok -> {
                boAnhMoi()
                tai()
                b.scroll.post { b.scroll.smoothScrollTo(0, 0) }
                Snackbar.make(b.root, "Đã lưu ảnh đại diện", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    // ------------------------------------------------------------------ xem lại ảnh lịch sử

    private fun hopXemLai(a: Avatar, o: AvatarView) {
        val d = DialogAvatarHistoryBinding.inflate(LayoutInflater.from(this))
        d.avatar.hien(
            AnhLoader.tai(this, a.duongDanAnh), initialOf(du?.o?.hoTen.orEmpty()),
            KhungAvatar.VUONG_BO_GOC, HieuUngAvatar.GOC, khongVien = true, banKinhDp = 12f
        )
        d.tvDate.text = "Tải lên ngày ${a.ngayTao.format(fmt)}"

        o.datVienChon(true)                       // ô trong lưới đang được xem
        val dialog = AlertDialog.Builder(this).setView(d.root).create()
        dialog.setOnDismissListener { o.datVienChon(false) }
        d.btnClose.setOnClickListener { dialog.dismiss() }
        d.btnReuse.setOnClickListener {
            when (val r = repo.dungLaiAnh(a.idAvatar)) {
                is Kq.Loi -> Snackbar.make(b.root, r.thongBao, Snackbar.LENGTH_LONG).show()
                is Kq.Ok -> {
                    dialog.dismiss()
                    tai()
                    b.scroll.post { b.scroll.smoothScrollTo(0, 0) }
                    Snackbar.make(b.root, "Đã dùng lại ảnh này", Snackbar.LENGTH_SHORT).show()
                }
            }
        }
        dialog.show()
        val le = (24 * resources.displayMetrics.density).toInt()
        dialog.window?.apply {
            setBackgroundDrawable(InsetDrawable(ColorDrawable(Color.TRANSPARENT), le, 0, le, 0))
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }
}

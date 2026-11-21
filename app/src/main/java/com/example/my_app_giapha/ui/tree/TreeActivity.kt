package com.example.my_app_giapha.ui.tree

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import com.example.my_app_giapha.R
import com.example.my_app_giapha.data.DuLieuCay
import com.example.my_app_giapha.data.DuongAn
import com.example.my_app_giapha.data.danhSachDuongAn
import com.example.my_app_giapha.data.Kq
import com.example.my_app_giapha.data.Services
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.example.my_app_giapha.databinding.ActivityTreeBinding
import com.example.my_app_giapha.databinding.ItemQuanHeAnBinding
import com.example.my_app_giapha.databinding.SheetQuanHeAnBinding
import com.example.my_app_giapha.model.VaiTro
import com.example.my_app_giapha.tree.LayoutEngine
import com.example.my_app_giapha.tree.NutQuanHe
import com.example.my_app_giapha.ui.Routes
import com.example.my_app_giapha.ui.common.applyBottomMarginInset
import com.example.my_app_giapha.ui.common.setAction
import com.example.my_app_giapha.ui.common.setup
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.InsetDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.example.my_app_giapha.databinding.DialogLocDoiBinding
import com.example.my_app_giapha.tree.BoLocDoi
import com.example.my_app_giapha.tree.KetQuaBoCuc

/**
 * GD-06 — Cây gia phả của một Không gian.
 * Nội dung giống nhau với mọi vai trò; vai trò chỉ quyết định nút "Thêm thành viên" và các mục menu.
 * Tải lại mỗi lần màn hình hiện lại, để thấy thay đổi từ các màn con (thêm/sửa/xoá).
 */
class TreeActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ID_KHONG_GIAN = "idKhongGian"
        const val EXTRA_CHI_XEM = "chiXem"
    }

    private lateinit var b: ActivityTreeBinding
    private val repo get() = Services.repository
    private var idKhongGian = 0
    private var chiXem = false
    private var vaiTro: VaiTro? = null
    private var daCoCay = false
    private var duHienTai: DuLieuCay? = null
    private lateinit var kho: LocDoiStore
    private var locDangBat: LocDoi? = null      // bộ lọc đang áp dụng (null = không lọc)
    private var idDangChon: Int? = null         // thẻ đang chọn trên cây

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityTreeBinding.inflate(layoutInflater)
        setContentView(b.root)
        idKhongGian = intent.getIntExtra(EXTRA_ID_KHONG_GIAN, 0)
        chiXem = intent.getBooleanExtra(EXTRA_CHI_XEM, false)

        b.appBar.setup(this, "")
        b.fab.applyBottomMarginInset()
        b.btnFilter.applyBottomMarginInset()
        b.fab.setOnClickListener { Routes.moThemO(this, idKhongGian) }
        b.bandReadOnly.visibility = if (chiXem) View.VISIBLE else View.GONE
        if (!chiXem) {
            // Chạm một thẻ = chọn; chạm đúp = mở GD-08
            b.viewport.choPhepChon = true
            b.viewport.khiDoiChon = { id -> idDangChon = id; capNhatNutLoc() }
            b.viewport.khiChamDupThe = { idO -> Routes.moChiTietO(this, idKhongGian, idO) }
            b.viewport.khiBamAn = { nut -> anQuanHe(nut) }
            b.btnFilter.visibility = View.VISIBLE
            b.btnFilter.setOnClickListener { khiBamNutLoc() }
        }
        kho = LocDoiStore(this)
        b.tvFilterClear.setOnClickListener { datLoc(null) }
    }

    override fun onResume() {
        super.onResume()
        tai()
    }

    private fun tai() {
        if (!daCoCay) b.stateView.showLoading()
        val kq = if (chiXem) repo.duLieuCayChoAdminApp(idKhongGian) else repo.duLieuCay(idKhongGian)
        when (val r = kq) {
            is Kq.Loi -> {
                daCoCay = false
                b.fab.visibility = View.GONE
                b.stateView.showError(r.thongBao) { tai() }
            }
            is Kq.Ok -> hienCay(r.giaTri)
        }
    }

    private fun hienCay(du: DuLieuCay) {
        vaiTro = du.toi.vaiTro
        duHienTai = du
        b.viewport.choPhepAn = !chiXem && du.toi.vaiTro.laQuanTri
        if (chiXem) {
            b.appBar.tvTitle.text = "Cây gia phả — ${du.khongGian.tenKhongGian}"
            b.appBar.actionContainer.removeAllViews()
        } else {
            b.appBar.tvTitle.text = du.khongGian.tenKhongGian
            datNutBenPhai(du.khongGian.cheDoRiengTu)
        }
        b.fab.visibility = if (!chiXem && du.toi.vaiTro.laQuanTri) View.VISIBLE else View.GONE
        b.viewport.cay.idBanThan = if (chiXem) null else du.toi.idOThanhVien      // Ô của chính mình: viền hồng + "Bạn ở đây"

        if (du.oThanhVien.isEmpty()) {
            b.stateView.showEmpty(
                if (du.toi.vaiTro.laQuanTri) "Chưa có thành viên nào. Bấm \"Thêm thành viên\" để bắt đầu."
                else "Chưa có thành viên nào trong cây."
            )
        } else {
            b.stateView.hide()
        }

        // Lọc số đời quanh Ô mốc đã lưu. Mốc không còn trên cây chính (bị xóa, bị ẩn quan hệ) thì không lọc.
        val kqDay = LayoutEngine.tinh(du)
        val luu = if (chiXem) null else kho.doc(du.toi.idTaiKhoan, idKhongGian)
        val hopLe = luu != null && kqDay.the.firstOrNull { it.id == luu.idMoc }?.doi != null
        locDangBat = if (hopLe) luu else null
        val loc = locDangBat
        b.bandFilter.visibility = if (loc != null) View.VISIBLE else View.GONE
        if (loc != null) {
            val ten = du.oThanhVien.firstOrNull { it.idOThanhVien == loc.idMoc }?.hoTen.orEmpty()
            b.tvFilterInfo.text = "Đang lọc ${loc.n} đời quanh $ten"
        }
        val kq = if (loc != null) LayoutEngine.tinh(du, loc = BoLocDoi(loc.idMoc, loc.n)) else kqDay

        b.viewport.datDuLieu(kq, du.oThanhVien, anh = du.anhDangDung)
        idDangChon = b.viewport.cay.idTheChon      // thẻ đang chọn có thể đã bị lọc mất
        capNhatNutLoc()
        daCoCay = true
    }

    // ---- lọc số đời quanh Ô đang chọn (QUYET_DINH_BO_SUNG mục 3, đã đổi mốc từ "Ô của bạn" sang "Ô đang chọn") ----

    /**
     * Chỉ lọc được từ cây gốc: đang lọc thì ẩn nút, muốn lọc lại phải "Bỏ lọc" trước.
     * Ở cây gốc, nút chỉ sáng khi đã chọn một thẻ.
     */
    private fun capNhatNutLoc() {
        b.btnFilter.visibility = if (chiXem || locDangBat != null) View.GONE else View.VISIBLE
        b.btnFilter.alpha = if (idDangChon != null) 1f else 0.4f
    }

    private fun khiBamNutLoc() {
        if (locDangBat != null) return
        if (idDangChon == null) {
            Snackbar.make(b.root, "Chạm vào một thẻ để chọn Ô, rồi bấm lọc số đời quanh Ô đó", Snackbar.LENGTH_SHORT)
                .apply { anchorView = if (b.fab.visibility == View.VISIBLE) b.fab else null }.show()
            return
        }
        moHopLoc()
    }

    /** loc = null: bỏ lọc. Lưu trên máy rồi vẽ lại từ dữ liệu đang có. */
    private fun datLoc(loc: LocDoi?) {
        val du = duHienTai ?: return
        kho.luu(du.toi.idTaiKhoan, idKhongGian, loc)
        hienCay(du)
        b.viewport.post { b.viewport.scrollTo(0, 0) }
    }

    private fun moHopLoc() {
        val du = duHienTai ?: return
        if (locDangBat != null) return          // cây đang được lọc: không lọc chồng
        val moc = idDangChon ?: return
        val nMax = LayoutEngine.soDoiToiDa(du, moc)
        if (nMax == null) {
            Snackbar.make(b.root, "Ô này chưa nối với ai trên cây nên không lọc được", Snackbar.LENGTH_SHORT).show()
            return
        }
        val ten = du.oThanhVien.firstOrNull { it.idOThanhVien == moc }?.hoTen.orEmpty()
        val d = DialogLocDoiBinding.inflate(LayoutInflater.from(this))
        val dialog = AlertDialog.Builder(this).setView(d.root).create()

        d.tvHint.text = "Số đời hiển thị quanh $ten, tính cả người này (từ 1 đến $nMax)."
        fun hienSo(v: Int) {
            d.etN.setText(v.toString())
            d.etN.setSelection(d.etN.text.length)
            d.tvError.visibility = View.GONE
        }
        val macDinh = minOf(3, nMax).coerceIn(1, nMax)
        fun soHienTai() = d.etN.text.toString().toIntOrNull()?.coerceIn(1, nMax) ?: macDinh
        hienSo(macDinh)

        d.btnMinus.setOnClickListener { hienSo((soHienTai() - 1).coerceAtLeast(1)) }
        d.btnPlus.setOnClickListener { hienSo((soHienTai() + 1).coerceAtMost(nMax)) }
        d.btnAll.text = "Hủy"                   // cây gốc chưa lọc nên không còn "Hiện tất cả"
        d.btnAll.setOnClickListener { dialog.dismiss() }
        d.btnApply.setOnClickListener {
            val v = d.etN.text.toString().toIntOrNull()
            if (v == null || v !in 1..nMax) {
                d.tvError.text = "Vui lòng nhập số từ 1 đến $nMax"
                d.tvError.visibility = View.VISIBLE
                return@setOnClickListener
            }
            dialog.dismiss()
            datLoc(LocDoi(moc, v))
        }

        dialog.show()
        val le = (24 * resources.displayMetrics.density).toInt()
        dialog.window?.apply {
            setBackgroundDrawable(InsetDrawable(ColorDrawable(Color.TRANSPARENT), le, 0, le, 0))
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }

    // ---- ẩn quan hệ (nhấn giữ đường nối → nhãn "Ẩn" → thanh "Hoàn tác") ----

    private fun datAn(nut: NutQuanHe, an: Boolean): Kq<Unit> = when (nut) {
        is NutQuanHe.ChaMeCon -> repo.anQuanHeChaMe(nut.idCon, nut.idPhuHuynh, an)
        is NutQuanHe.HonNhan -> repo.anHonNhan(nut.idHonNhan, an)
    }

    private fun moTa(nut: NutQuanHe): String {
        val ten = { id: Int -> duHienTai?.oThanhVien?.firstOrNull { it.idOThanhVien == id }?.hoTen ?: "?" }
        return when (nut) {
            is NutQuanHe.ChaMeCon -> "${ten(nut.idCon)} — ${nut.idPhuHuynh.joinToString(" & ") { ten(it) }}"
            is NutQuanHe.HonNhan -> "${ten(nut.idChong)} — ${ten(nut.idVo)}"
        }
    }

    private fun anQuanHe(nut: NutQuanHe) {
        val mota = moTa(nut)
        when (val r = datAn(nut, true)) {
            is Kq.Loi -> Snackbar.make(b.root, r.thongBao, Snackbar.LENGTH_LONG).show()
            is Kq.Ok -> {
                tai()
                val bar = Snackbar.make(b.root, "Đã ẩn: $mota", Snackbar.LENGTH_LONG)
                    .setAction("Hoàn tác") { hoanTacAn(nut) }
                    .setActionTextColor(getColor(R.color.peach_light))
                if (b.fab.visibility == View.VISIBLE) bar.anchorView = b.fab
                bar.show()
            }
        }
    }

    private fun hoanTacAn(nut: NutQuanHe) {
        val r = datAn(nut, false)
        if (r is Kq.Loi) Snackbar.make(b.root, r.thongBao, Snackbar.LENGTH_LONG).show()
        tai()
    }

    // ---- GD-20: bottom sheet "Quan hệ đã ẩn (n)" ----

    private fun moQuanHeAn() {
        val sheet = BottomSheetDialog(this)
        val sb = SheetQuanHeAnBinding.inflate(layoutInflater)
        sheet.setContentView(sb.root)
        (sb.root.parent as? View)?.setBackgroundColor(Color.TRANSPARENT)   // để thấy góc bo của bg_sheet
        veDanhSachAn(sb)
        sheet.show()
    }

    private fun veDanhSachAn(sb: SheetQuanHeAnBinding) {
        val ds = duHienTai?.let { danhSachDuongAn(it) }.orEmpty()
        sb.tvTitle.text = "Quan hệ đã ẩn (${ds.size})"
        sb.tvEmpty.visibility = if (ds.isEmpty()) View.VISIBLE else View.GONE
        sb.list.removeAllViews()
        for (d in ds) {
            val dong = ItemQuanHeAnBinding.inflate(layoutInflater, sb.list, false)
            dong.tvTen.text = d.tenCap
            dong.tvLoai.text = d.loai
            dong.btnHienLai.setOnClickListener { hienLai(d); veDanhSachAn(sb) }
            sb.list.addView(dong.root)
        }
    }

    private fun hienLai(d: DuongAn) {
        val r = if (d.idHonNhan != null) repo.anHonNhan(d.idHonNhan, false)
        else repo.anQuanHeChaMe(d.idCon!!, d.idPhuHuynh, false)
        if (r is Kq.Loi) android.widget.Toast.makeText(this, r.thongBao, android.widget.Toast.LENGTH_LONG).show()
        tai()      // cây phía sau vẽ lại; duHienTai được cập nhật trước khi sheet vẽ lại danh sách
    }

    // ---- thanh tiêu đề: biểu tượng riêng tư (nếu có) + ba chấm ----

    private fun datNutBenPhai(riengTu: Boolean) {
        val dp = resources.displayMetrics.density
        b.appBar.setAction(this, R.drawable.ic_more_vert, "Menu") { }
        val nutBaCham = b.appBar.actionContainer.getChildAt(0)
        nutBaCham.setOnClickListener { moMenu(it) }

        b.appBar.actionContainer.removeAllViews()
        val hang = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        if (riengTu) {
            val khoa = ImageView(this).apply {
                setImageResource(R.drawable.ic_lock)
                imageTintList = ColorStateList.valueOf(Color.WHITE)
                contentDescription = "Không gian riêng tư"
            }
            hang.addView(khoa, LinearLayout.LayoutParams((20 * dp).toInt(), (20 * dp).toInt()).apply {
                marginEnd = (4 * dp).toInt()
            })
        }
        hang.addView(nutBaCham)
        b.appBar.actionContainer.addView(hang, FrameLayout.LayoutParams(-2, (48 * dp).toInt()))
    }

    // ---- menu ba chấm theo vai trò (QUYET_DINH mục 7) ----

    private fun moMenu(neo: View) {
        val vt = vaiTro ?: return
        val muc = mutableListOf<Pair<String, () -> Unit>>()

        muc += "Danh sách thành viên" to { Routes.moDanhSachThanhVien(this, idKhongGian) }
        if (vt != VaiTro.KHACH_TAM) muc += "Tạo mã lời mời" to { Routes.moMaMoi(this, idKhongGian) }

        if (vt.laQuanTri) {
            val n = duHienTai?.let { danhSachDuongAn(it).size } ?: 0
            muc += "Quan hệ đã ẩn ($n)" to { moQuanHeAn() }
        }
        if (vt == VaiTro.ADMIN) {
            muc += "Cài đặt Không gian" to { Routes.moCaiDatKhongGian(this, idKhongGian) }
            muc += "Quản lý vai trò" to { Routes.moQuanLyVaiTro(this, idKhongGian) }
        }
        PopupMenu(this, neo).apply {
            muc.forEachIndexed { i, m -> menu.add(0, i, i, m.first) }
            setOnMenuItemClickListener { muc[it.itemId].second(); true }
            show()
        }
    }
}
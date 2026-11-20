package com.example.my_app_giapha.ui.common

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.LruCache
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.example.my_app_giapha.R
import com.example.my_app_giapha.model.HieuUngAvatar
import com.example.my_app_giapha.model.KhungAvatar
import kotlin.math.max

/**
 * Nạp ảnh cho Avatar.duongDanAnh. Có hai dạng đường dẫn:
 *  - bắt đầu bằng "/": file thật trong bộ nhớ app (ảnh người dùng tải lên);
 *  - dạng "avatars/nam_20250910.jpg" của dữ liệu mẫu: tìm ảnh trong res/drawable theo tên không đuôi
 *    ("nam_20250910"). Không có thì trả null và màn hình hiện chữ cái đầu thay ảnh.
 */
object AnhLoader {
    private val cache = object : LruCache<String, Bitmap>(24 * 1024 * 1024) {
        override fun sizeOf(key: String, value: Bitmap) = value.byteCount
    }

    fun tai(context: Context, path: String?, maxPx: Int = 640): Bitmap? {
        if (path.isNullOrBlank()) return null
        cache.get(path)?.let { return it }
        val ctx = context.applicationContext
        val bm = runCatching {
            if (path.startsWith("/")) giaiMaFile(path, maxPx) else giaiMaTaiNguyen(ctx, path, maxPx)
        }.getOrNull()
        if (bm != null) cache.put(path, bm)
        return bm
    }

    private fun mau(w: Int, h: Int, maxPx: Int): Int {
        var s = 1
        while (max(w, h) / s > maxPx * 2) s *= 2
        return s
    }

    private fun giaiMaFile(path: String, maxPx: Int): Bitmap? {
        val o = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, o)
        o.inSampleSize = mau(o.outWidth, o.outHeight, maxPx)
        o.inJustDecodeBounds = false
        return BitmapFactory.decodeFile(path, o)
    }

    private fun giaiMaTaiNguyen(ctx: Context, path: String, maxPx: Int): Bitmap? {
        val ten = path.substringAfterLast('/').substringBeforeLast('.')
        val id = ctx.resources.getIdentifier(ten, "drawable", ctx.packageName)
        if (id == 0) return null
        val o = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeResource(ctx.resources, id, o)
        o.inSampleSize = mau(o.outWidth, o.outHeight, maxPx)
        o.inJustDecodeBounds = false
        return BitmapFactory.decodeResource(ctx.resources, id, o)
    }
}

/**
 * Ảnh đại diện áp khung và hiệu ứng. Luôn vuông (cạnh = chiều rộng).
 * Chưa có ảnh (anh = null): nền xanh nhạt + chữ cái đầu.
 * vienChon: viền nâu quanh ô (đang chọn). tick: dấu tích ở góc phải trên.
 * khongVien: không vẽ viền của khung. banKinhDp: ép bo góc cố định (ô lưới lịch sử dùng 12dp).
 */
class AvatarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val dp = resources.displayMetrics.density
    private var anh: Bitmap? = null
    private var chuCai = ""
    private var khung = KhungAvatar.TRON
    private var hieuUng = HieuUngAvatar.GOC
    private var vienChon = false
    private var tick = false
    private var khongVien = false
    private var banKinhDp: Float? = null
    private var lamMo = false

    private val nau = ContextCompat.getColor(context, R.color.brown_primary)
    private val paintAnh = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paintNen = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = ContextCompat.getColor(context, R.color.green_light) }
    private val paintChu = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = nau
        textAlign = Paint.Align.CENTER
        typeface = ResourcesCompat.getFont(context, R.font.be_vietnam_pro_bold)
    }
    private val paintVien = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; color = nau }
    private val paintTick = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = nau }
    private val paintDau = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; color = Color.WHITE; strokeCap = Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND
    }

    fun hien(
        anh: Bitmap?, chuCai: String, khung: KhungAvatar, hieuUng: HieuUngAvatar,
        vienChon: Boolean = false, tick: Boolean = false,
        khongVien: Boolean = false, banKinhDp: Float? = null, lamMo: Boolean = false
    ) {
        this.anh = anh; this.chuCai = chuCai; this.khung = khung; this.hieuUng = hieuUng
        this.vienChon = vienChon; this.tick = tick; this.khongVien = khongVien; this.banKinhDp = banKinhDp
        this.lamMo = lamMo
    }

    fun datVienChon(v: Boolean) {
        vienChon = v
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val h = MeasureSpec.getSize(heightMeasureSpec)
        val side = when {
            MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY -> w
            MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY -> h
            else -> w
        }
        setMeasuredDimension(side, side)
    }

    private fun banKinh(canh: Float): Float =
        banKinhDp?.let { it * dp } ?: if (khung == KhungAvatar.VUONG_BO_GOC) canh * 0.1f else 0f

    private fun veHinh(c: Canvas, r: RectF, p: Paint, rad: Float) {
        if (khung == KhungAvatar.TRON && banKinhDp == null) c.drawOval(r, p)
        else c.drawRoundRect(r, rad, rad, p)
    }

    private fun boLoc(): ColorFilter? = when (hieuUng) {
        HieuUngAvatar.GOC -> null
        HieuUngAvatar.DEN_TRANG -> ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f) })
        HieuUngAvatar.CO_DIEN -> ColorMatrixColorFilter(
            ColorMatrix(
                floatArrayOf(
                    0.393f, 0.769f, 0.189f, 0f, 0f,
                    0.349f, 0.686f, 0.168f, 0f, 0f,
                    0.272f, 0.534f, 0.131f, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
        )
    }

    override fun onDraw(c: Canvas) {
        val s = width.toFloat()
        if (s <= 0f) return
        val vung = RectF(0f, 0f, s, s)

        // Vòng chọn quanh ô: ảnh co vào trong một chút để chừa khoảng hở
        if (vienChon) {
            paintVien.strokeWidth = 2f * dp
            val o = RectF(vung).apply { inset(dp, dp) }
            veHinh(c, o, paintVien, banKinh(s))
            vung.inset(5f * dp, 5f * dp)
        }

        val canh = vung.width()
        val rad = banKinh(canh)
        val bw = if (khongVien) 0f else when (khung) {
            KhungAvatar.KHONG -> 0f
            KhungAvatar.TRON, KhungAvatar.VUONG_BO_GOC -> max(1.5f * dp, canh * 0.02f)
            KhungAvatar.VIEN_NAU -> canh * 0.06f
        }
        if (bw > 0f) {
            paintVien.strokeWidth = bw
            val o = RectF(vung).apply { inset(bw / 2f, bw / 2f) }
            veHinh(c, o, paintVien, max(0f, rad - bw / 2f))
        }
        val a = RectF(vung).apply { inset(bw, bw) }
        val radTrong = max(0f, rad - bw)

        val bm = anh
        if (bm != null) {
            val sc = max(a.width() / bm.width, a.height() / bm.height)
            val m = Matrix().apply {
                setScale(sc, sc)
                postTranslate(a.left + (a.width() - bm.width * sc) / 2f, a.top + (a.height() - bm.height * sc) / 2f)
            }
            paintAnh.shader = BitmapShader(bm, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP).apply { setLocalMatrix(m) }
            paintAnh.colorFilter =
                if (lamMo) ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f) }) else boLoc()
            paintAnh.alpha = if (lamMo) 140 else 255
            veHinh(c, a, paintAnh, radTrong)
        } else {
            paintNen.alpha = if (lamMo) 120 else 255
            paintChu.alpha = if (lamMo) 140 else 255
            veHinh(c, a, paintNen, radTrong)
            paintChu.textSize = a.width() * 0.4f
            val y = a.centerY() - (paintChu.ascent() + paintChu.descent()) / 2f
            c.drawText(chuCai, a.centerX(), y, paintChu)
        }

        if (tick) {
            val cx = s - 9f * dp
            val cy = 9f * dp
            c.drawCircle(cx, cy, 9f * dp, paintTick)
            paintDau.strokeWidth = 1.8f * dp
            val p = Path().apply {
                moveTo(cx - 3.5f * dp, cy)
                lineTo(cx - 1f * dp, cy + 3f * dp)
                lineTo(cx + 4f * dp, cy - 3f * dp)
            }
            c.drawPath(p, paintDau)
        }
    }
}

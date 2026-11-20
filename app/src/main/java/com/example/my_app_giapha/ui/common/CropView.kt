package com.example.my_app_giapha.ui.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Cắt ảnh theo khung VUÔNG 1:1. Ảnh nằm yên (vừa khít màn hình), khung cắt thì:
 *  - kéo bên trong khung để di chuyển,
 *  - kéo một góc để đổi cỡ (khung luôn vuông),
 *  - chụm hai ngón để thu phóng khung.
 * cat() trả về phần ảnh gốc nằm trong khung.
 */
class CropView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val dp = resources.displayMetrics.density
    private var bm: Bitmap? = null
    private val anh = RectF()      // vùng ảnh trên màn hình
    private val khung = RectF()    // khung cắt
    private val canhNhoNhat = 72 * dp
    private val vungBamGoc = 28 * dp

    private enum class Che { KHONG, DI_CHUYEN, GOC }
    private var che = Che.KHONG
    private var neoX = 0f
    private var neoY = 0f
    private var truocX = 0f
    private var truocY = 0f

    private val cuAnh = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val mauToi = Color.argb(150, 20, 10, 4)
    private val cuVien = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; color = Color.WHITE; strokeWidth = 1.5f * dp
    }
    private val cuLuoi = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; color = Color.argb(120, 255, 255, 255); strokeWidth = 1f * dp
    }
    private val cuGoc = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; color = Color.WHITE; strokeWidth = 4f * dp; strokeCap = Paint.Cap.SQUARE
    }

    private val thuPhong = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(d: ScaleGestureDetector): Boolean {
            val c = (khung.width() * d.scaleFactor).coerceIn(canhThapNhat(), canhCaoNhat())
            val cx = khung.centerX()
            val cy = khung.centerY()
            khung.set(cx - c / 2, cy - c / 2, cx + c / 2, cy + c / 2)
            dayVaoAnh()
            invalidate()
            return true
        }
    })

    fun datAnh(b: Bitmap) {
        bm = b
        datLai()
    }

    override fun onSizeChanged(w: Int, h: Int, ow: Int, oh: Int) {
        super.onSizeChanged(w, h, ow, oh)
        datLai()
    }

    private fun datLai() {
        val b = bm ?: return
        if (width == 0 || height == 0) return
        val s = min(width / b.width.toFloat(), height / b.height.toFloat())
        val w = b.width * s
        val h = b.height * s
        anh.set((width - w) / 2, (height - h) / 2, (width + w) / 2, (height + h) / 2)
        val c = min(w, h) * 0.78f
        khung.set(anh.centerX() - c / 2, anh.centerY() - c / 2, anh.centerX() + c / 2, anh.centerY() + c / 2)
        invalidate()
    }

    private fun canhCaoNhat() = min(anh.width(), anh.height())
    private fun canhThapNhat() = min(canhNhoNhat, canhCaoNhat())

    /** Đẩy khung nằm gọn trong ảnh. */
    private fun dayVaoAnh() {
        val dx = when {
            khung.left < anh.left -> anh.left - khung.left
            khung.right > anh.right -> anh.right - khung.right
            else -> 0f
        }
        val dy = when {
            khung.top < anh.top -> anh.top - khung.top
            khung.bottom > anh.bottom -> anh.bottom - khung.bottom
            else -> 0f
        }
        khung.offset(dx, dy)
    }

    // ------------------------------------------------------------------ chạm

    override fun onTouchEvent(e: MotionEvent): Boolean {
        if (bm == null) return false
        thuPhong.onTouchEvent(e)
        if (e.pointerCount > 1 || thuPhong.isInProgress) {
            che = Che.KHONG
            return true
        }
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                parent?.requestDisallowInterceptTouchEvent(true)
                chonChe(e.x, e.y)
                truocX = e.x
                truocY = e.y
            }
            MotionEvent.ACTION_MOVE -> when (che) {
                Che.DI_CHUYEN -> {
                    khung.offset(e.x - truocX, e.y - truocY)
                    dayVaoAnh()
                    truocX = e.x
                    truocY = e.y
                }
                Che.GOC -> keoGoc(e.x, e.y)
                Che.KHONG -> {}
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> che = Che.KHONG
        }
        invalidate()
        return true
    }

    private fun chonChe(x: Float, y: Float) {
        val goc = listOf(
            khung.left to khung.top, khung.right to khung.top,
            khung.left to khung.bottom, khung.right to khung.bottom
        )
        val gan = goc.minByOrNull { (gx, gy) -> abs(gx - x) + abs(gy - y) }!!
        if (abs(gan.first - x) <= vungBamGoc && abs(gan.second - y) <= vungBamGoc) {
            che = Che.GOC
            neoX = if (gan.first == khung.left) khung.right else khung.left      // góc đối diện đứng yên
            neoY = if (gan.second == khung.top) khung.bottom else khung.top
        } else {
            che = if (khung.contains(x, y)) Che.DI_CHUYEN else Che.KHONG
        }
    }

    private fun keoGoc(x: Float, y: Float) {
        val hx = if (x >= neoX) 1 else -1
        val hy = if (y >= neoY) 1 else -1
        val phongX = if (hx > 0) anh.right - neoX else neoX - anh.left
        val phongY = if (hy > 0) anh.bottom - neoY else neoY - anh.top
        val toiDa = min(phongX, phongY)
        val c = max(abs(x - neoX), abs(y - neoY)).coerceIn(min(canhThapNhat(), toiDa), toiDa)
        khung.set(
            min(neoX, neoX + hx * c), min(neoY, neoY + hy * c),
            max(neoX, neoX + hx * c), max(neoY, neoY + hy * c)
        )
    }

    // ------------------------------------------------------------------ vẽ

    override fun onDraw(c: Canvas) {
        val b = bm ?: return
        c.drawBitmap(b, null, anh, cuAnh)

        c.save()
        c.clipRect(anh)
        c.clipOutRect(khung)
        c.drawColor(mauToi)
        c.restore()

        // lưới 3x3
        val w = khung.width() / 3
        for (i in 1..2) {
            c.drawLine(khung.left + w * i, khung.top, khung.left + w * i, khung.bottom, cuLuoi)
            c.drawLine(khung.left, khung.top + w * i, khung.right, khung.top + w * i, cuLuoi)
        }
        c.drawRect(khung, cuVien)

        // bốn góc đậm
        val d = 18 * dp
        fun goc(x: Float, y: Float, sx: Int, sy: Int) {
            c.drawLine(x, y, x + sx * d, y, cuGoc)
            c.drawLine(x, y, x, y + sy * d, cuGoc)
        }
        goc(khung.left, khung.top, 1, 1)
        goc(khung.right, khung.top, -1, 1)
        goc(khung.left, khung.bottom, 1, -1)
        goc(khung.right, khung.bottom, -1, -1)
    }

    // ------------------------------------------------------------------ kết quả

    /** Phần ảnh gốc nằm trong khung (hình vuông), hoặc null nếu chưa có ảnh. */
    fun cat(): Bitmap? {
        val b = bm ?: return null
        val k = b.width / anh.width()
        val x = ((khung.left - anh.left) * k).roundToInt().coerceIn(0, b.width - 1)
        val y = ((khung.top - anh.top) * k).roundToInt().coerceIn(0, b.height - 1)
        val s = min((khung.width() * k).roundToInt(), min(b.width - x, b.height - y)).coerceAtLeast(1)
        return Bitmap.createBitmap(b, x, y, s, s)
    }
}

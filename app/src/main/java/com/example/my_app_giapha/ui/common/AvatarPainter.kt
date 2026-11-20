package com.example.my_app_giapha.ui.common

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import com.example.my_app_giapha.model.HieuUngAvatar
import com.example.my_app_giapha.model.KhungAvatar
import kotlin.math.max

/**
 * Vẽ ảnh đại diện (khung + hiệu ứng) thẳng lên Canvas, không cần View. Dùng cho thẻ Ô trên cây,
 * nơi vẽ bằng đơn vị thiết kế (không phải pixel): donVi = 1f ở cây, = density ở View thường.
 * Cách vẽ giống AvatarView. lamMo = true cho người đã mất: ảnh xám và mờ.
 */
class AvatarPainter(
    private val mauVien: Int,
    private val mauNen: Int,
    private val chuFont: Typeface?
) {
    private val paintAnh = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paintNen = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = mauNen }
    private val paintChu = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = mauVien; textAlign = Paint.Align.CENTER; typeface = chuFont
    }
    private val paintVien = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; color = mauVien }
    private val vungTrong = RectF()
    private val vienTrong = RectF()
    private val ma = Matrix()

    private val xam = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f) })
    private val coDien = ColorMatrixColorFilter(
        ColorMatrix(
            floatArrayOf(
                0.393f, 0.769f, 0.189f, 0f, 0f,
                0.349f, 0.686f, 0.168f, 0f, 0f,
                0.272f, 0.534f, 0.131f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )
        )
    )

    private fun boLoc(h: HieuUngAvatar): ColorFilter? = when (h) {
        HieuUngAvatar.GOC -> null
        HieuUngAvatar.DEN_TRANG -> xam
        HieuUngAvatar.CO_DIEN -> coDien
    }

    private fun veHinh(c: Canvas, r: RectF, p: Paint, rad: Float, tron: Boolean) {
        if (tron) c.drawOval(r, p) else c.drawRoundRect(r, rad, rad, p)
    }

    fun ve(
        c: Canvas, vung: RectF, anh: Bitmap?, chuCai: String,
        khung: KhungAvatar, hieu: HieuUngAvatar, donVi: Float = 1f, lamMo: Boolean = false
    ) {
        val canh = vung.width()
        val tron = khung == KhungAvatar.TRON
        val rad = if (khung == KhungAvatar.VUONG_BO_GOC) canh * 0.1f else 0f
        val bw = when (khung) {
            KhungAvatar.KHONG -> 0f
            KhungAvatar.TRON, KhungAvatar.VUONG_BO_GOC -> max(1.5f * donVi, canh * 0.02f)
            KhungAvatar.VIEN_NAU -> canh * 0.06f
        }
        if (bw > 0f) {
            paintVien.strokeWidth = bw
            paintVien.alpha = if (lamMo) 140 else 255
            vienTrong.set(vung); vienTrong.inset(bw / 2f, bw / 2f)
            veHinh(c, vienTrong, paintVien, max(0f, rad - bw / 2f), tron)
        }
        vungTrong.set(vung); vungTrong.inset(bw, bw)
        val radTrong = max(0f, rad - bw)

        if (anh != null) {
            val sc = max(vungTrong.width() / anh.width, vungTrong.height() / anh.height)
            ma.setScale(sc, sc)
            ma.postTranslate(
                vungTrong.left + (vungTrong.width() - anh.width * sc) / 2f,
                vungTrong.top + (vungTrong.height() - anh.height * sc) / 2f
            )
            paintAnh.shader = BitmapShader(anh, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP).apply { setLocalMatrix(ma) }
            paintAnh.colorFilter = if (lamMo) xam else boLoc(hieu)
            paintAnh.alpha = if (lamMo) 140 else 255
            veHinh(c, vungTrong, paintAnh, radTrong, tron)
        } else {
            paintNen.alpha = if (lamMo) 120 else 255
            paintChu.alpha = if (lamMo) 140 else 255
            veHinh(c, vungTrong, paintNen, radTrong, tron)
            paintChu.textSize = vungTrong.width() * 0.4f
            c.drawText(chuCai, vungTrong.centerX(), vungTrong.centerY() - (paintChu.ascent() + paintChu.descent()) / 2f, paintChu)
        }
    }
}

package com.example.my_app_giapha.ui.common

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.example.my_app_giapha.R

/** Vòng tròn đếm ngược: nền xám nhạt, cung nâu thể hiện phần còn lại (progress 1 = đầy, 0 = hết). Đặt vuông, ví dụ 160dp. */
class CountdownRingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val dp = resources.displayMetrics.density
    private val rect = RectF()

    private val nen = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 10 * dp
        color = ContextCompat.getColor(context, R.color.border_default)
    }
    private val cung = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 10 * dp
        strokeCap = Paint.Cap.ROUND
        color = ContextCompat.getColor(context, R.color.brown_primary)
    }

    var progress: Float = 1f
        set(v) {
            field = v.coerceIn(0f, 1f)
            invalidate()
        }

    override fun onDraw(c: Canvas) {
        val w = nen.strokeWidth
        rect.set(w / 2, w / 2, width - w / 2, height - w / 2)
        c.drawOval(rect, nen)
        if (progress > 0f) c.drawArc(rect, -90f, 360f * progress, false, cung)
    }
}
package com.example.my_app_giapha.ui.common

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

/** Ẩn bàn phím đang gắn với view này. */
fun View.anBanPhim() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

/**
 * Chạm ra ngoài ô nhập thì ẩn bàn phím, và cú chạm đó KHÔNG đi tiếp xuống view bên dưới
 * (ví dụ không mở nhầm một mục trong danh sách). Chỉ tác dụng khi bàn phím đang hiện.
 *
 * Cách dùng trong Activity:
 *   private val chamNgoai = ChamNgoaiAnBanPhim(this)
 *   override fun dispatchTouchEvent(ev: MotionEvent) =
 *       if (chamNgoai.xuLy(ev)) true else super.dispatchTouchEvent(ev)
 */
class ChamNgoaiAnBanPhim(private val activity: Activity) {
    private var dangNuot = false   // cú chạm đã bị nuốt: nuốt luôn phần còn lại của nó (kéo, nhấc tay)

    /** true = đã xử lý, Activity không chuyển sự kiện đi tiếp. */
    fun xuLy(ev: MotionEvent): Boolean {
        if (ev.actionMasked == MotionEvent.ACTION_DOWN) {
            dangNuot = false
            val dangNhap = activity.currentFocus as? EditText ?: return false
            val hienBanPhim = ViewCompat.getRootWindowInsets(activity.window.decorView)
                ?.isVisible(WindowInsetsCompat.Type.ime()) == true
            if (!hienBanPhim) return false
            val vung = Rect().also { dangNhap.getGlobalVisibleRect(it) }
            if (vung.contains(ev.rawX.toInt(), ev.rawY.toInt())) return false   // chạm vào chính ô nhập
            dangNhap.anBanPhim()
            dangNhap.clearFocus()
            dangNuot = true
            return true
        }
        if (dangNuot) {
            if (ev.actionMasked == MotionEvent.ACTION_UP || ev.actionMasked == MotionEvent.ACTION_CANCEL) dangNuot = false
            return true
        }
        return false
    }
}

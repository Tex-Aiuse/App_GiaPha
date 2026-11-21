package com.example.my_app_giapha.ui.tree

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.InsetDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.doOnLayout
import com.example.my_app_giapha.R
import com.example.my_app_giapha.databinding.DialogCachGoiBinding
import com.example.my_app_giapha.databinding.ItemCachGoiHuongBinding
import com.example.my_app_giapha.tree.CachGoiText
import com.example.my_app_giapha.tree.KetQuaGoi
import com.example.my_app_giapha.tree.MoiQuanHeGoi

/**
 * Hộp thoại kết quả của "Tra cứu cách gọi" (mở từ GD-06 sau khi chọn người thứ hai).
 * Hai thẻ xuống dòng: "[Người thứ nhất] gọi [Người thứ hai] là: ..." và chiều ngược lại.
 * Một cách gọi nhiều tên: nối bằng "hoặc". Hai người có nhiều quan hệ: mỗi quan hệ một dòng, đầu dòng "- ".
 * Dưới mỗi dòng có tên kiểu quan hệ (chữ nhỏ). Chưa có tên gọi cụ thể thì chỉ hiện lời giải thích.
 */
fun Context.moHopCachGoi(kq: KetQuaGoi) {
    val b = DialogCachGoiBinding.inflate(LayoutInflater.from(this))
    val d = resources.displayMetrics.density

    if (kq.ds.isEmpty()) {
        b.cardAB.root.visibility = View.GONE
        b.cardBA.root.visibility = View.GONE
        b.tvGhiChu.visibility = View.VISIBLE
        b.tvGhiChu.text = kq.ghiChu.orEmpty()
    } else {
        veHuong(b.cardAB, CachGoiText.tieuDe(kq.a.hoTen, kq.b.hoTen), kq.ds) { it.aGoiB }
        veHuong(b.cardBA, CachGoiText.tieuDe(kq.b.hoTen, kq.a.hoTen), kq.ds) { it.bGoiA }
        (b.cardBA.root.layoutParams as ViewGroup.MarginLayoutParams).topMargin = (12 * d).toInt()
    }

    val dialog = AlertDialog.Builder(this).setView(b.root).create()
    b.btnClose.setOnClickListener { dialog.dismiss() }

    dialog.show()
    val le = (24 * d).toInt()
    dialog.window?.apply {
        setBackgroundDrawable(InsetDrawable(ColorDrawable(Color.TRANSPARENT), le, 0, le, 0))
        setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}

/** Một chiều: mỗi quan hệ một dòng, dưới dòng là tên kiểu quan hệ. */
private fun Context.veHuong(
    card: ItemCachGoiHuongBinding,
    tieuDe: String,
    ds: List<MoiQuanHeGoi>,
    tenGoi: (MoiQuanHeGoi) -> List<String>
) {
    val d = resources.displayMetrics.density
    card.tvTitle.text = tieuDe
    card.boxLines.removeAllViews()
    val nhieu = ds.size > 1
    ds.forEachIndexed { i, m ->
        val dong = TextView(this).apply {
            text = CachGoiText.dong(tenGoi(m), nhieu)
            typeface = ResourcesCompat.getFont(context, R.font.be_vietnam_pro_bold)
            setTextColor(getColor(R.color.text_primary))
            textSize = 14f
        }
        val kieu = TextView(this).apply {
            text = m.kieu
            typeface = ResourcesCompat.getFont(context, R.font.be_vietnam_pro)
            setTextColor(getColor(R.color.text_secondary))
            textSize = 12f
        }
        val khoi = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(dong)
            addView(kieu)
        }
        card.boxLines.addView(
            khoi,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = if (i == 0) 0 else (12 * d).toInt() }
        )
    }
}

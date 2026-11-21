package com.example.my_app_giapha.ui.tree

import android.content.Context

/** Bộ lọc số đời đang bật: lọc N đời quanh Ô mốc (Ô người dùng đã chọn lúc bấm lọc). */
data class LocDoi(val idMoc: Int, val n: Int)

/** Nhớ bộ lọc số đời của GD-06 trên máy (SharedPreferences), theo (tài khoản, không gian). Không có khóa = không lọc. */
class LocDoiStore(context: Context) {

    private val sp = context.applicationContext.getSharedPreferences("loc_so_doi", Context.MODE_PRIVATE)

    private fun khoaN(idTaiKhoan: Int, idKhongGian: Int) = "n_${idTaiKhoan}_$idKhongGian"
    private fun khoaMoc(idTaiKhoan: Int, idKhongGian: Int) = "moc_${idTaiKhoan}_$idKhongGian"

    /** Bản cũ chỉ lưu N (lọc quanh Ô của mình) nên thiếu Ô mốc: coi như không lọc. */
    fun doc(idTaiKhoan: Int, idKhongGian: Int): LocDoi? {
        val n = sp.getInt(khoaN(idTaiKhoan, idKhongGian), 0)
        val moc = sp.getInt(khoaMoc(idTaiKhoan, idKhongGian), 0)
        return if (n >= 1 && moc >= 1) LocDoi(moc, n) else null
    }

    /** loc = null: xóa, không lọc nữa. */
    fun luu(idTaiKhoan: Int, idKhongGian: Int, loc: LocDoi?) {
        val e = sp.edit()
        if (loc == null) {
            e.remove(khoaN(idTaiKhoan, idKhongGian)).remove(khoaMoc(idTaiKhoan, idKhongGian))
        } else {
            e.putInt(khoaN(idTaiKhoan, idKhongGian), loc.n).putInt(khoaMoc(idTaiKhoan, idKhongGian), loc.idMoc)
        }
        e.apply()
    }
}

package com.example.my_app_giapha.tree

/**
 * Cách viết kết quả "Tra cứu cách gọi" lên màn hình. Kotlin thuần, kiểm thử độc lập như QuanHeGoi.
 *  - Một cách gọi có nhiều tên (cha, bố, ba): nối bằng "hoặc".
 *  - Hai người có nhiều quan hệ cùng lúc: mỗi quan hệ một dòng, đầu dòng là "- ".
 */
object CachGoiText {

    /** Tiêu đề mỗi chiều: "[người nói] gọi [người được gọi] là:". */
    fun tieuDe(nguoiNoi: String, nguoiDuocGoi: String) = "$nguoiNoi gọi $nguoiDuocGoi là:"

    fun noiTen(ten: List<String>) = ten.joinToString(" hoặc ")

    /** nhieuQuanHe = true khi hai người có từ hai quan hệ trở lên: thêm "- " đầu dòng. */
    fun dong(ten: List<String>, nhieuQuanHe: Boolean) = (if (nhieuQuanHe) "- " else "") + noiTen(ten)
}

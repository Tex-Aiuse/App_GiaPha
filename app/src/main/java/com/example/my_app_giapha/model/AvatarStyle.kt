package com.example.my_app_giapha.model

/** Khung ảnh đại diện. `db` là chuỗi lưu ở Avatar.LoaiKhung. Giá trị lạ hoặc rỗng → TRON. */
enum class KhungAvatar(val db: String, val nhan: String) {
    KHONG("Khong", "Không"),
    TRON("Tron", "Tròn"),
    VUONG_BO_GOC("VuongBoGoc", "Vuông bo góc"),
    VIEN_NAU("VienNau", "Viền nâu");

    companion object {
        fun fromDb(s: String?) = entries.firstOrNull { it.db == s } ?: TRON
        fun hopLe(s: String) = entries.any { it.db == s }
    }
}

/** Hiệu ứng màu của ảnh đại diện. `db` là chuỗi lưu ở Avatar.HieuUng. Giá trị lạ hoặc rỗng → GOC. */
enum class HieuUngAvatar(val db: String, val nhan: String) {
    GOC("Goc", "Gốc"),
    DEN_TRANG("DenTrang", "Đen trắng"),
    CO_DIEN("CoDien", "Cổ điển");

    companion object {
        fun fromDb(s: String?) = entries.firstOrNull { it.db == s } ?: GOC
        fun hopLe(s: String) = entries.any { it.db == s }
    }
}

package com.example.my_app_giapha.ui.common

import com.example.my_app_giapha.model.Avatar
import com.example.my_app_giapha.model.HieuUngAvatar
import com.example.my_app_giapha.model.KhungAvatar

/**
 * Hiện ảnh đại diện đang dùng của một Ô trong danh sách (GD-08, GD-12, GD-13, kết quả tìm người...).
 * Chưa có ảnh (a = null): vòng xanh nhạt + chữ cái đầu. Người đã mất: ảnh xám và mờ.
 * Ảnh nạp qua AnhLoader (có cache) nên cuộn danh sách không bị chậm.
 */
fun AvatarView.hienCuaO(a: Avatar?, hoTen: String, conSong: Boolean = true) {
    val bm = AnhLoader.tai(context, a?.duongDanAnh, 256)
    hien(
        bm, initialOf(hoTen),
        KhungAvatar.fromDb(a?.loaiKhung), HieuUngAvatar.fromDb(a?.hieuUng),
        khongVien = false, lamMo = !conSong
    )
}

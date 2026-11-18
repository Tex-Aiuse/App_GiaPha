package com.example.my_app_giapha.data

/**
 * Cửa duy nhất để lấy dữ liệu cứng. FakeRepository (bước 5) chỉ đọc từ đây.
 * Dữ liệu gốc lấy từ GiaPha_DuLieuMau.sql: 30 tài khoản, 4 không gian, 84 Ô.
 */
object SampleData {
    /** OTP cố định của bản hardcode. */
    const val OTP = "123456"

    val taiKhoan = SampleAccounts.taiKhoan
    val maXacThuc = SampleAccounts.maXacThuc
    val khongGian = SampleAccounts.khongGian
    val thanVien = SampleAccounts.thanVien
    val maCode = SampleAccounts.maCode

    val oThanhVien = SamplePeople.oThanhVien
    val avatar = SamplePeople.avatar
    val cuocDoi = SamplePeople.cuocDoi
    val binhLuan = SamplePeople.binhLuan
    val thanhTuu = SamplePeople.thanhTuu

    val quanHeChaMe = SampleRelations.quanHeChaMe
    val honNhan = SampleRelations.honNhan
}
package com.example.my_app_giapha.data

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** GD-23: tìm theo tên không gian và lọc nâng cao (tên chủ, SĐT chủ, ngày tạo) trên dữ liệu mẫu 4 không gian. */
class FakeRepositoryKhongGianHeThongTest {

    private fun adminApp() = FakeRepository().also { assertTrue(it.dangNhap("0901000001", SampleData.OTP).laOk) }

    private fun ids(repo: FakeRepository, loc: BoLocKhongGian, trang: Int = 1) =
        (repo.danhSachKhongGianHeThong(loc, trang) as Kq.Ok).giaTri.ds.map { it.khongGian.idKhongGian }

    // Dữ liệu mẫu: 1 Nguyễn (Lan, 12/03/2025) · 2 Trần (Khôi, 02/04/2025) · 3 Lê (15/01/2025) · 4 Phạm (03/01/2025)

    @Test
    fun khongLocThiDuBonKhongGianMoiTaoTruoc() {
        assertEquals(listOf(2, 1, 3, 4), ids(adminApp(), BoLocKhongGian()))
    }

    @Test
    fun timTenKhongGianKhongPhanBietDau() {
        val r = adminApp()
        assertEquals(listOf(1), ids(r, BoLocKhongGian(ten = "nguyen")))
        assertEquals(listOf(3), ids(r, BoLocKhongGian(ten = "DA NANG")))
        assertEquals(emptyList<Int>(), ids(r, BoLocKhongGian(ten = "Hồng")))
    }

    @Test
    fun locTheoTenChu() {
        val r = adminApp()
        assertEquals(listOf(2), ids(r, BoLocKhongGian(tenChu = "minh khoi")))
        assertEquals(listOf(1), ids(r, BoLocKhongGian(tenChu = "Nguyễn Thị Lan")))
    }

    @Test
    fun locTheoSdtChuKhopMotPhan() {
        val r = adminApp()
        assertEquals(listOf(2), ids(r, BoLocKhongGian(sdtChu = "0912 345 678")))   // có dấu cách vẫn lấy chữ số
        assertEquals(listOf(1), ids(r, BoLocKhongGian(sdtChu = "0905222")))
    }

    @Test
    fun locTheoNgayTaoTinhCaHaiDau() {
        val r = adminApp()
        assertEquals(listOf(2, 1), ids(r, BoLocKhongGian(tuNgay = LocalDate.of(2025, 3, 1))))
        assertEquals(listOf(3, 4), ids(r, BoLocKhongGian(denNgay = LocalDate.of(2025, 1, 15))))      // ngày 15/01 vẫn tính
        assertEquals(listOf(1, 3), ids(r, BoLocKhongGian(tuNgay = LocalDate.of(2025, 1, 15), denNgay = LocalDate.of(2025, 3, 12))))
    }

    @Test
    fun cacDieuKienCongLai() {
        val r = adminApp()
        val loc = BoLocKhongGian(ten = "gia pha", tuNgay = LocalDate.of(2025, 3, 1))
        assertEquals(listOf(2, 1), ids(r, loc))
        assertEquals(listOf(1), ids(r, loc.copy(tenChu = "lan")))
        assertEquals(emptyList<Int>(), ids(r, loc.copy(sdtChu = "0999")))
    }

    @Test
    fun soLocNangCaoKhongTinhOTimTen() {
        assertEquals(0, BoLocKhongGian(ten = "abc").soLocNangCao)
        assertEquals(2, BoLocKhongGian(tenChu = "a", tuNgay = LocalDate.of(2025, 1, 1), denNgay = LocalDate.of(2025, 2, 1)).soLocNangCao)
        assertEquals("abc", BoLocKhongGian(ten = "abc", tenChu = "x").boLocNangCao().ten)
        assertTrue(!BoLocKhongGian(ten = "abc", tenChu = "x").boLocNangCao().dangLoc)
    }

    @Test
    fun ngayBatDauSauNgayKetThucLaLoi() {
        val r = adminApp().danhSachKhongGianHeThong(
            BoLocKhongGian(tuNgay = LocalDate.of(2025, 5, 1), denNgay = LocalDate.of(2025, 4, 1)), 1)
        assertTrue(r is Kq.Loi && r.loi == LoiApp.DU_LIEU_KHONG_HOP_LE)
    }

    @Test
    fun trangVuotQuaDuocKepVaCoDuTongSo() {
        val t = (adminApp().danhSachKhongGianHeThong(BoLocKhongGian(), 99) as Kq.Ok).giaTri
        assertEquals(1, t.trang); assertEquals(1, t.tongTrang); assertEquals(4, t.tongSo)
        assertEquals("Nguyễn Minh Khôi", t.ds.first { it.khongGian.idKhongGian == 2 }.tenChu)
    }

    @Test
    fun chiAdminAppDuocXem() {
        val repo = FakeRepository()
        assertTrue(repo.dangNhap("0912345678", SampleData.OTP).laOk)            // Thành viên thường
        val r = repo.danhSachKhongGianHeThong(BoLocKhongGian(), 1)
        assertTrue(r is Kq.Loi && r.loi == LoiApp.KHONG_CO_QUYEN)
    }
}
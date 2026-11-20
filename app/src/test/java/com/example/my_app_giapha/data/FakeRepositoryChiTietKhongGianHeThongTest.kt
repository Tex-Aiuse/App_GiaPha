package com.example.my_app_giapha.data

import com.example.my_app_giapha.model.VaiTro
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** GD-24: chi tiết không gian và dữ liệu cây chỉ xem của AdminApp (không gian 1 = họ Nguyễn, do Lan tạo). */
class FakeRepositoryChiTietKhongGianHeThongTest {

    private fun adminApp() = FakeRepository().also { assertTrue(it.dangNhap("0901000001", SampleData.OTP).laOk) }
    private fun nguoiThuong() = FakeRepository().also { assertTrue(it.dangNhap("0912345678", SampleData.OTP).laOk) }

    private fun chiTiet(r: FakeRepository, id: Int) = (r.chiTietKhongGianHeThong(id) as Kq.Ok).giaTri

    @Test
    fun thongTinCoBan() {
        val d = chiTiet(adminApp(), 1)
        assertEquals("Nguyễn Thị Lan", d.nguoiTao?.tenHienThi)
        assertEquals("0905222444", d.nguoiTao?.sdt)
        assertEquals(SampleData.oThanhVien.count { it.idKhongGian == 1 }, d.soO)
        assertTrue(d.soO > 0)
    }

    @Test
    fun soThanhVienKhopDanhSachVaKhongTinhNguoiDaRoi() {
        val d = chiTiet(adminApp(), 1)
        assertEquals(d.thanhVien.size, d.soThanhVien)
        // Dữ liệu mẫu: 10 dòng ở không gian 1, trong đó 1 người đã rời
        assertEquals(9, d.soThanhVien)
    }

    @Test
    fun nguoiBiChanXepCuoiVaDuocDanhDau() {
        val d = chiTiet(adminApp(), 1)
        assertTrue(d.thanhVien.last().biChan)
        assertEquals(1, d.thanhVien.count { it.biChan })
        assertEquals(VaiTro.ADMIN, d.thanhVien.first().vaiTro)
    }

    @Test
    fun khongTimThayKhongGian() {
        val r = adminApp().chiTietKhongGianHeThong(999)
        assertTrue(r is Kq.Loi && r.loi == LoiApp.KHONG_TIM_THAY)
        val c = adminApp().duLieuCayChoAdminApp(999)
        assertTrue(c is Kq.Loi && c.loi == LoiApp.KHONG_TIM_THAY)
    }

    @Test
    fun chiAdminAppMoiXemDuoc() {
        val r = nguoiThuong()
        val a = r.chiTietKhongGianHeThong(1)
        assertTrue(a is Kq.Loi && a.loi == LoiApp.KHONG_CO_QUYEN)
        val b = r.duLieuCayChoAdminApp(1)
        assertTrue(b is Kq.Loi && b.loi == LoiApp.KHONG_CO_QUYEN)
    }

    @Test
    fun matKetNoiThiBaoLoi() {
        val r = adminApp().also { it.giaLapLoi = true }
        assertTrue((r.chiTietKhongGianHeThong(1) as Kq.Loi).loi == LoiApp.MAT_KET_NOI)
        assertTrue((r.duLieuCayChoAdminApp(1) as Kq.Loi).loi == LoiApp.MAT_KET_NOI)
    }

    @Test
    fun duLieuCayDayDuVaVaiTroGiaLaKhachTam() {
        val r = adminApp()
        val cay = (r.duLieuCayChoAdminApp(1) as Kq.Ok).giaTri
        assertEquals(SampleData.oThanhVien.count { it.idKhongGian == 1 }, cay.oThanhVien.size)
        assertEquals(VaiTro.KHACH_TAM, cay.toi.vaiTro)
        assertNull(cay.toi.idOThanhVien)
        assertTrue(cay.quanHeChaMe.isNotEmpty())
    }

    @Test
    fun xemCayKhongThemAdminAppVaoThanhVien() {
        val r = adminApp()
        val truoc = chiTiet(r, 1).soThanhVien
        r.duLieuCayChoAdminApp(1)
        assertEquals(truoc, chiTiet(r, 1).soThanhVien)
        assertNull(r.vaiTroCuaToi(1))
    }

    @Test
    fun cayKhongGianRiengTuVanXemDuoc() {
        val r = adminApp()
        // Đăng nhập Admin họ Nguyễn để bật riêng tư, rồi quay lại AdminApp (cùng một FakeRepository)
        assertTrue(r.dangNhap("0905222444", SampleData.OTP).laOk)
        assertTrue(r.datRiengTu(1, true).laOk)
        assertTrue(r.dangNhap("0901000001", SampleData.OTP).laOk)
        assertTrue(r.duLieuCayChoAdminApp(1).laOk)
        assertTrue(r.chiTietKhongGianHeThong(1).laOk)
    }
}

package com.example.my_app_giapha.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** GD-25: AdminApp xóa không gian vi phạm (không gian 1 = họ Nguyễn). */
class FakeRepositoryXoaKhongGianHeThongTest {

    private fun adminApp() = FakeRepository().also { assertTrue(it.dangNhap("0901000001", SampleData.OTP).laOk) }
    private fun nguoiThuong() = FakeRepository().also { assertTrue(it.dangNhap("0912345678", SampleData.OTP).laOk) }

    private fun loiCua(r: Kq<Unit>) = (r as Kq.Loi).loi

    @Test
    fun xoaThanhCong() {
        val r = adminApp()
        assertTrue(r.xoaKhongGianHeThong(1, "Nội dung vi phạm").laOk)
        val sau = r.chiTietKhongGianHeThong(1)
        assertTrue(sau is Kq.Loi && sau.loi == LoiApp.KHONG_TIM_THAY)
        // Không gian khác còn nguyên
        assertTrue(r.chiTietKhongGianHeThong(2).laOk)
    }

    @Test
    fun khongGianBienMatKhoiDanhSach() {
        val r = adminApp()
        val truoc = (r.danhSachKhongGianHeThong(BoLocKhongGian(), 1) as Kq.Ok).giaTri.tongSo
        assertTrue(r.xoaKhongGianHeThong(1, "Vi phạm").laOk)
        val sau = (r.danhSachKhongGianHeThong(BoLocKhongGian(), 1) as Kq.Ok).giaTri
        assertEquals(truoc - 1, sau.tongSo)
        assertFalse(sau.ds.any { it.khongGian.idKhongGian == 1 })
    }

    @Test
    fun lyDoTrongThiBaoLoiVaKhongXoa() {
        val r = adminApp()
        assertEquals(LoiApp.DU_LIEU_KHONG_HOP_LE, loiCua(r.xoaKhongGianHeThong(1, "   ")))
        assertTrue(r.chiTietKhongGianHeThong(1).laOk)
    }

    @Test
    fun khongTimThayKhongGian() {
        assertEquals(LoiApp.KHONG_TIM_THAY, loiCua(adminApp().xoaKhongGianHeThong(999, "Vi phạm")))
    }

    @Test
    fun chiAdminAppMoiXoaDuoc() {
        val r = nguoiThuong()
        assertEquals(LoiApp.KHONG_CO_QUYEN, loiCua(r.xoaKhongGianHeThong(1, "Vi phạm")))
    }

    @Test
    fun matKetNoiThiBaoLoiVaKhongXoa() {
        val r = adminApp()
        r.giaLapLoi = true
        assertEquals(LoiApp.MAT_KET_NOI, loiCua(r.xoaKhongGianHeThong(1, "Vi phạm")))
        r.giaLapLoi = false
        assertTrue(r.chiTietKhongGianHeThong(1).laOk)
    }
}

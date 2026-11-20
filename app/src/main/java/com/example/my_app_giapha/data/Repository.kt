package com.example.my_app_giapha.data

import com.example.my_app_giapha.model.*
import java.time.LocalDate

/** Một dòng trong danh sách không gian (GD-03): không gian kèm vai trò của người đang đăng nhập. */
data class KhongGianCuaToi(
    val khongGian: KhongGian,
    val vaiTro: VaiTro
)

/** Toàn bộ dữ liệu để vẽ cây (GD-06, GD-24). Bố cục tính ở LayoutEngine, không tính ở đây. */
data class DuLieuCay(
    val khongGian: KhongGian,
    val toi: ThanVienTrongKGian,                // vai trò của người đang xem
    val oThanhVien: List<OThanhVien>,
    val quanHeChaMe: List<QuanHeChaMe>,         // gồm cả dòng đang ẩn (HienThiLienKet = false)
    val honNhan: List<QHeHonNhan>,
    val anhDangDung: Map<Int, Avatar>
)

/** Một người liên quan trong khối "Quan hệ gia đình" của GD-08. nhan: Ruột/Nuôi/Kế hoặc trạng thái hôn nhân. */
data class NguoiLienQuan(
    val idO: Int,
    val hoTen: String,
    val nhan: String?,
    val conSong: Boolean,
    val anh: Avatar? = null
)

/** Dữ liệu của màn GD-08. */
data class ChiTietO(
    val o: OThanhVien,
    val toi: ThanVienTrongKGian,          // vai trò của người đang xem
    val chaMe: List<NguoiLienQuan>,
    val voChong: List<NguoiLienQuan>,
    val con: List<NguoiLienQuan>,
    val anhDangDung: Avatar? = null
)

/** Tác động của việc xóa một Ô, dùng dựng nội dung hộp cảnh báo ở GD-08. */
data class TacDongXoa(
    val hoTen: String,
    val soChaMe: Int,
    val soVoChong: Int,
    val soCon: Int,
    val soThanhTuu: Int,
    val soBinhLuan: Int,
    val soTieuSu: Int,
    val tachKhoiCayChinh: List<String>     // họ tên những người sẽ mất liên kết với cây chính
)

/** Dữ liệu nhập từ form GD-07. */
data class FormO(
    val hoTen: String,
    val bietDanh: String?,
    val gioiTinh: GioiTinh?,
    val ngaySinh: LocalDate?,
    val queQuan: String?,
    val cccd: String?,
    val conSong: Boolean,
    val ngayMat: LocalDate?
)

/** Một kết quả tìm Ô kèm ngữ cảnh để phân biệt người trùng tên. */
data class KetQuaTim(
    val idO: Int,
    val hoTen: String,
    val nguCanh: String,       // "Con của: A, B" / "Vợ/chồng của: X" / "Chưa có quan hệ"
    val soHonNhan: Int,        // số hôn nhân hiện có, để gợi ý "Thứ tự hôn nhân"
    val anh: Avatar? = null
)

data class ChaMeChon(val idO: Int, val loai: LoaiQuanHe, val ngayXacLap: LocalDate?)

/** Quan hệ gắn kèm khi tạo Ô mới. null = Ô độc lập. */
sealed class QuanHeMoi {
    data class LaCon(val chaMe: List<ChaMeChon>) : QuanHeMoi()          // 1–2 người
    data class LaVoChong(val idNguoiKia: Int, val ngayCuoi: LocalDate?, val thuTu: Int) : QuanHeMoi()
}

/** Chiều khi thêm quan hệ ở GD-12, tính từ Ô đang xem. */
enum class ChieuQuanHe {
    LA_CON,         // Ô đang xem là con → người được chọn là cha/mẹ của Ô
    LA_PHU_HUYNH    // Ô đang xem là cha/mẹ → người được chọn là con của Ô
}

/** Một dòng ở GD-12: người ở đầu kia của quan hệ cha/mẹ–con. */
data class DongQuanHeChaMe(
    val idNguoi: Int,
    val hoTen: String,
    val conSong: Boolean,
    val loai: LoaiQuanHe,
    val ngayXacLap: LocalDate?,
    val anh: Avatar? = null

)

/** Dữ liệu màn GD-12: cha/mẹ của Ô và con của Ô. */
data class QuanHeChaMeCuaO(
    val o: OThanhVien,
    val chaMe: List<DongQuanHeChaMe>,
    val con: List<DongQuanHeChaMe>
)

data class DongHonNhan(
    val id: Int,
    val idNguoi: Int,
    val hoTen: String,
    val conSong: Boolean,
    val trangThai: TrangThaiHonNhan,
    val ngayCoi: LocalDate?,
    val ngayLyHon: LocalDate?,
    val thuTu: Int?,
    val anh: Avatar? = null
)

/** Dữ liệu màn GD-13, đã sắp theo thứ tự hôn nhân. */
data class HonNhanCuaO(
    val o: OThanhVien,
    val ds: List<DongHonNhan>
)

/** Một dòng bình luận ở GD-09a. xoaDuoc: người đang xem là tác giả hoặc Admin/Quản lý. */
data class DongBinhLuan(
    val id: Int,
    val idNguoi: Int,
    val tenNguoi: String,
    val noiDung: String,
    val thoiGian: java.time.LocalDateTime,
    val xoaDuoc: Boolean
)
/** Một dòng GD-14: tài khoản có vai trò trong không gian. idO, hoTenO, anhO: Ô được gán (nếu có). */
data class DongThanhVien(
    val idTaiKhoan: Int,
    val tenHienThi: String,          // lấy từ TaiKhoan, không phải họ tên của Ô
    val vaiTro: VaiTro,
    val biChan: Boolean,
    val idO: Int?,
    val hoTenO: String?,
    val anhO: Avatar?                // ảnh đại diện đang dùng của Ô
)

/** Dữ liệu GD-14, kèm vai trò người xem để quyết định nút Mời, Gán Ô, Quản lý vai trò. */
data class DanhSachThanhVien(
    val khongGian: KhongGian,
    val toi: ThanVienTrongKGian,
    val thanhVien: List<DongThanhVien>
)
/** Một trang tài khoản toàn hệ thống (GD-21). trang đã được ép vào khoảng hợp lệ. */
data class TrangTaiKhoan(
    val ds: List<TaiKhoan>,
    val trang: Int,
    val tongTrang: Int,
    val tongSo: Int
)
/** Bộ lọc danh sách không gian của AdminApp (GD-23). Chuỗi rỗng / null = không lọc theo trường đó. */
data class BoLocKhongGian(
    val ten: String = "",             // tên không gian: khớp một phần, không phân biệt hoa thường và dấu
    val tenChu: String = "",          // tên hiển thị của chủ (người tạo) không gian, khớp như trên
    val sdtChu: String = "",          // số điện thoại của chủ: chỉ lấy chữ số, khớp một phần
    val tuNgay: LocalDate? = null,    // ngày tạo từ ngày này, tính cả ngày đó
    val denNgay: LocalDate? = null    // ngày tạo đến ngày này, tính cả ngày đó
) {
    /** Số nhóm lọc nâng cao đang dùng (tên chủ, SĐT chủ, ngày tạo). Ô tìm tên không tính. */
    val soLocNangCao: Int
        get() = listOf(tenChu.isNotBlank(), sdtChu.isNotBlank(), tuNgay != null || denNgay != null).count { it }
    val dangLoc: Boolean get() = soLocNangCao > 0
    /** Bỏ các lọc nâng cao, giữ nguyên ô tìm tên. */
    fun boLocNangCao() = copy(tenChu = "", sdtChu = "", tuNgay = null, denNgay = null)
}

data class DongKhongGianHeThong(val khongGian: KhongGian, val tenChu: String)

/** Một trang không gian toàn hệ thống (GD-23). trang đã được ép vào khoảng hợp lệ. */
data class TrangKhongGianHeThong(
    val ds: List<DongKhongGianHeThong>,
    val trang: Int,
    val tongTrang: Int,
    val tongSo: Int
)
/** Một không gian mà tài khoản có vai trò (GD-22). */
data class KhongGianCuaTaiKhoan(
    val khongGian: KhongGian,
    val vaiTro: VaiTro,
    val biChan: Boolean
)

/** Dữ liệu GD-22: tài khoản + các không gian đang tham gia (không gồm không gian đã rời). */
data class ChiTietTaiKhoan(
    val taiKhoan: TaiKhoan,
    val khongGian: List<KhongGianCuaTaiKhoan>
)
/** Dữ liệu GD-24. thanhVien là danh sách đầy đủ; màn hình tự cắt 5 dòng đầu khi thu gọn. */
data class ChiTietKhongGianHeThong(
    val khongGian: KhongGian,
    val nguoiTao: TaiKhoan?,
    val soThanhVien: Int,
    val soO: Int,
    val thanhVien: List<DongThanhVien>
)
/** Kết quả tìm tài khoản theo SĐT để mời vào một không gian (GD-15). */
data class KetQuaTimSdt(
    val idTaiKhoan: Int,
    val tenHienThi: String,
    val sdt: String,
    val vaiTroHienTai: VaiTro?,    // khác null: đã có vai trò trong không gian này (đang hoạt động hoặc bị chặn)
    val biChan: Boolean
)
/** Nội dung tiểu sử đang có. Ô chưa có tiểu sử thì tieuSu = null. */
data class TieuSuO(
    val noiDung: String,
    val tenNguoiViet: String,
    val ngayCapNhat: java.time.LocalDateTime
)

/** Dữ liệu màn GD-09a. Bình luận xếp mới nhất lên đầu. */
data class TieuSuVaBinhLuan(
    val o: OThanhVien,
    val toi: ThanVienTrongKGian,          // vai trò của người đang xem
    val tieuSu: TieuSuO?,
    val binhLuan: List<DongBinhLuan>
)
/** Dữ liệu màn GD-10. ds đã xếp theo năm giảm dần, thành tựu không có năm nằm cuối. */
data class DanhSachThanhTuu(
    val o: OThanhVien,
    val toi: ThanVienTrongKGian,          // vai trò của người đang xem
    val ds: List<ThanhTuu>
)
/** Dữ liệu màn GD-11. lichSu mới nhất lên đầu và KHÔNG chứa ảnh đang dùng. */
data class AnhDaiDienO(
    val o: OThanhVien,
    val toi: ThanVienTrongKGian,
    val dangDung: Avatar?,
    val lichSu: List<Avatar>
)
/**
 * Cửa duy nhất giữa màn hình và dữ liệu. Màn hình không đọc SampleData trực tiếp.
 * Hiện chạy bằng FakeRepository (bộ nhớ); sau này thay bằng bản gọi API.
 * Các nhóm hàm cho bước 8–11 sẽ được thêm vào khi làm tới bước đó.
 */
interface Repository {

    // ---- Tài khoản (GD-01, 02, 04) ----
    fun guiMaDangKy(sdt: String): Kq<Unit>
    fun guiMaDangNhap(sdt: String): Kq<Unit>
    fun dangKy(ten: String, sdt: String, otp: String): Kq<TaiKhoan>
    fun dangNhap(sdt: String, otp: String): Kq<TaiKhoan>
    fun dangXuat()
    fun taiKhoanHienTai(): TaiKhoan?
    fun capNhatHoSo(tenHienThi: String, email: String?): Kq<TaiKhoan>

    // ---- Không gian (GD-03, 05) ----
    fun danhSachKhongGian(tuKhoa: String = ""): Kq<List<KhongGianCuaToi>>
    fun taoKhongGian(ten: String): Kq<KhongGian>
    fun vaoBangMa(ma: String): Kq<KhongGian>
    fun vaiTroCuaToi(idKhongGian: Int): ThanVienTrongKGian?

    // ---- Cây gia phả (GD-06) ----
    fun duLieuCay(idKhongGian: Int): Kq<DuLieuCay>

    // ---- Chi tiết Ô (GD-08) ----
    fun chiTietO(idO: Int): Kq<ChiTietO>
    /** conSong = false: xác nhận tử vong (bắt buộc có ngayMat). conSong = true: hủy xác nhận. Admin/Quản lý. */
    fun datTuVong(idO: Int, conSong: Boolean, ngayMat: java.time.LocalDate?): Kq<OThanhVien>
    /** Chỉ Admin. */
    fun datTruongToc(idO: Int, laTruongToc: Boolean): Kq<OThanhVien>
    /** Admin/Quản lý. Chưa xóa gì, chỉ tính tác động. */
    fun xemTruocXoaO(idO: Int): Kq<TacDongXoa>
    /** Xóa chỉ Ô này, các dòng quan hệ nối tới nó và dữ liệu riêng của nó (QUYET_DINH mục 5.5). */
    fun xoaO(idO: Int): Kq<Unit>
    // ---- Thêm/sửa Ô (GD-07) ----
    /** Admin/Quản lý. quanHe = null: Ô độc lập. Ghi Ô và quan hệ cùng lúc, lỗi thì không ghi gì. */
    fun themO(idKhongGian: Int, f: FormO, quanHe: QuanHeMoi? = null): Kq<OThanhVien>
    /** Admin/Quản lý. Chỉ sửa thông tin cá nhân; không đụng quan hệ, không đổi Trưởng tộc. */
    fun suaO(idO: Int, f: FormO): Kq<OThanhVien>
    /** Admin/Quản lý. Tìm Ô trong không gian theo tên (không phân biệt dấu), kèm ngữ cảnh quan hệ. */
    fun timO(idKhongGian: Int, tuKhoa: String): Kq<List<KetQuaTim>>
    // ---- Quan hệ cha/mẹ (GD-12) ----
    /** Admin/Quản lý. Cha/mẹ của Ô và con của Ô, kể cả các dòng đang ẩn. */
    fun quanHeChaMeCua(idO: Int): Kq<QuanHeChaMeCuaO>
    /** Tìm người để thêm; đã loại Ô này, người đã nối trực tiếp và những người sẽ tạo vòng lặp huyết thống. */
    fun timUngVienChaMe(idO: Int, chieu: ChieuQuanHe, tuKhoa: String): Kq<List<KetQuaTim>>
    /** Chặn: trùng dòng, vòng lặp huyết thống, quá một cha ruột hoặc một mẹ ruột. */
    fun themQuanHeChaMe(idO: Int, chieu: ChieuQuanHe, idNguoiKia: Int, loai: LoaiQuanHe, ngayXacLap: LocalDate?): Kq<Unit>
    /** Chỉ đổi loại và ngày; muốn đổi người thì xóa rồi thêm lại. */
    fun suaQuanHeChaMe(idCon: Int, idPhuHuynh: Int, loai: LoaiQuanHe, ngayXacLap: LocalDate?): Kq<Unit>
    /** Chỉ gỡ dòng quan hệ, không xóa Ô nào. */
    fun xoaQuanHeChaMe(idCon: Int, idPhuHuynh: Int): Kq<Unit>
    // ---- Quan hệ hôn nhân (GD-13) ----
    /** Admin/Quản lý. Mọi cuộc hôn nhân của Ô (cả vai trò chồng lẫn vợ). */
    fun honNhanCua(idO: Int): Kq<HonNhanCuaO>
    /** Tìm người để kết hôn; đã loại Ô này và những người đã có hôn nhân với Ô này. */
    fun timUngVienHonNhan(idO: Int, tuKhoa: String): Kq<List<KetQuaTim>>
    /** Trạng thái mặc định là Đang kết hôn. Chặn: trùng cặp, thứ tự < 1, ngày cưới sai. */
    fun themHonNhan(idO: Int, idNguoiKia: Int, ngayCoi: LocalDate?, thuTu: Int): Kq<Unit>
    /** Không đổi được người. Ngày ly hôn chỉ giữ khi trạng thái là Đã ly hôn. Không đụng quan hệ cha/mẹ–con. */
    fun suaHonNhan(id: Int, ngayCoi: LocalDate?, trangThai: TrangThaiHonNhan, ngayLyHon: LocalDate?, thuTu: Int): Kq<Unit>
    /** Chỉ xóa dòng hôn nhân, không xóa Ô nào và không đụng quan hệ cha/mẹ–con. */
    fun xoaHonNhan(id: Int): Kq<Unit>

    // ---- Ẩn/hiện lại quan hệ (8b). Admin/Quản lý. Chỉ đổi cách hiển thị, dữ liệu quan hệ vẫn nguyên ----
    /** an = true: ẩn; false: hiện lại. Áp cho mọi dòng QuanHeChaMe của idCon với các idPhuHuynh (nét T chung = 2 dòng). */
    fun anQuanHeChaMe(idCon: Int, idPhuHuynh: List<Int>, an: Boolean): Kq<Unit>
    /** an = true: ẩn; false: hiện lại. Dùng QHeHonNhan.HienThiLienKet. */
    fun anHonNhan(idHonNhan: Int, an: Boolean): Kq<Unit>
    // ---- Tiểu sử và bình luận (GD-09a, 09b) ----
    /** Mọi vai trò xem được (Khách tạm bị chặn nếu không gian riêng tư). */
    fun tieuSuVaBinhLuan(idO: Int): Kq<TieuSuVaBinhLuan>
    /** Admin/Quản lý. Viết mới hoặc sửa: chưa có thì tạo, có rồi thì cập nhật nội dung và ngày. Không được rỗng. */
    fun luuTieuSu(idO: Int, noiDung: String): Kq<Unit>
    /** Admin, Quản lý, Thành viên. Chưa có bản tiểu sử thì tự tạo một bản rỗng để gắn bình luận. */
    fun themBinhLuan(idO: Int, noiDung: String): Kq<Unit>
    /** Tác giả của bình luận, hoặc Admin/Quản lý. */
    fun xoaBinhLuan(idBinhLuan: Int): Kq<Unit>
    // ---- Thành tựu (GD-10) ----
    /** Mọi vai trò xem được (Khách tạm bị chặn nếu không gian riêng tư). */
    fun thanhTuuCua(idO: Int): Kq<DanhSachThanhTuu>
    /** Admin/Quản lý. Tên bắt buộc; năm và mô tả tùy chọn. */
    fun themThanhTuu(idO: Int, ten: String, nam: Int?, moTa: String?): Kq<Unit>
    /** Admin/Quản lý. */
    fun suaThanhTuu(idThanhTuu: Int, ten: String, nam: Int?, moTa: String?): Kq<Unit>
    /** Admin/Quản lý. */
    fun xoaThanhTuu(idThanhTuu: Int): Kq<Unit>
    // ---- Ảnh đại diện (GD-11). Admin/Quản lý ----
    fun anhDaiDienCua(idO: Int): Kq<AnhDaiDienO>
    /** Tạo ảnh mới thành "đang dùng"; ảnh đang dùng cũ chuyển sang lịch sử. */
    fun themAnhDaiDien(idO: Int, duongDanAnh: String, loaiKhung: String, hieuUng: String): Kq<Unit>
    /** Chỉ đổi khung/hiệu ứng, không đổi ảnh gốc. */
    fun suaKieuAnh(idAvatar: Int, loaiKhung: String, hieuUng: String): Kq<Unit>
    /** Đổi một ảnh lịch sử thành đang dùng; ảnh đang dùng hiện tại chuyển sang lịch sử. */
    fun dungLaiAnh(idAvatar: Int): Kq<Unit>
    // ---- Danh sách thành viên (GD-14) ----
    /** Mọi vai trò trong không gian đều xem được. Không gồm người đã rời; người bị chặn xếp cuối. */
    fun danhSachThanhVien(idKhongGian: Int): Kq<DanhSachThanhVien>
    // ---- Mời thành viên trực tiếp (GD-15) ----
    /** Admin/Quản lý. Tìm chính xác theo SĐT trên toàn hệ thống. Ok(null) = không có tài khoản nào. */
    fun timTaiKhoanTheoSdt(idKhongGian: Int, sdt: String): Kq<KetQuaTimSdt?>
    /** Admin/Quản lý. Thêm một tài khoản đã có vào không gian với vai trò Thành viên hờ. */
    fun moiTrucTiep(idKhongGian: Int, idTaiKhoan: Int): Kq<Unit>
    // ---- Mã mời (GD-16) ----
    /** Admin, Quản lý, Thành viên, Thành viên hờ (không phải Khách tạm). Mã dùng một lần, hiệu lực 5 phút. */
    fun taoMaMoi(idKhongGian: Int): Kq<MaCode>

    // ---- Gán Ô cho Thành viên hờ (GD-17) ----
    /** Admin/Quản lý. Tìm Ô theo tên trong các Ô chưa được gán cho tài khoản nào của không gian. */
    fun timOChuaGan(idKhongGian: Int, tuKhoa: String): Kq<List<KetQuaTim>>
    /** Admin/Quản lý. Gán Ô cho một Thành viên hờ chưa có Ô, vai trò thành Thành viên. Chặn nếu Ô đã có người gán. */
    fun ganO(idKhongGian: Int, idTaiKhoan: Int, idO: Int): Kq<Unit>
    /** Như themO, rồi gán luôn Ô mới cho Thành viên hờ. Lỗi ở bước nào thì không ghi gì. */
    fun themOVaGan(idKhongGian: Int, f: FormO, quanHe: QuanHeMoi?, idTaiKhoan: Int): Kq<OThanhVien>

    // ---- Cài đặt Không gian (GD-18). Chỉ Admin; Quản lý không được. ----
    /** Đổi tên không gian. Tên trống thì báo lỗi. */
    fun doiTenKhongGian(idKhongGian: Int, ten: String): Kq<KhongGian>
    /** Bật/tắt chế độ riêng tư. Khi bật: mọi Khách tạm đang hoạt động bị ngắt truy cập ngay. */
    fun datRiengTu(idKhongGian: Int, bat: Boolean): Kq<Unit>
    /** Xóa vĩnh viễn không gian cùng toàn bộ dữ liệu (thứ tự như trong SQL, không dùng CASCADE). */
    fun xoaKhongGian(idKhongGian: Int): Kq<Unit>
    // ---- Quản lý vai trò và chặn (GD-19). Chỉ Admin, không tác động lên Admin hay chính mình ----
    /** Thành viên (đang hoạt động) lên Quản lý. */
    fun capQuanLy(idKhongGian: Int, idTaiKhoan: Int): Kq<Unit>
    /** Quản lý xuống Thành viên. */
    fun haXuongThanhVien(idKhongGian: Int, idTaiKhoan: Int): Kq<Unit>
    /** Chặn khỏi không gian, lý do tùy chọn. Ghi ngày chặn và người chặn. */
    fun chanThanhVien(idKhongGian: Int, idTaiKhoan: Int, lyDo: String?): Kq<Unit>
    /** Gỡ chặn: về Hoạt động, xóa ngày chặn, lý do, người chặn. */
    fun goChan(idKhongGian: Int, idTaiKhoan: Int): Kq<Unit>
    // ---- AdminApp: tài khoản toàn hệ thống (GD-21). Chỉ LoaiTaiKhoan = AdminApp ----
    /** Tìm theo số điện thoại (khớp một phần), mới tạo xếp trước, 20 dòng mỗi trang, trang đánh số từ 1. */
    fun danhSachTaiKhoan(tuKhoaSdt: String, trang: Int): Kq<TrangTaiKhoan>
    // ---- AdminApp: chi tiết tài khoản, cấm / gỡ cấm (GD-22) ----
    fun chiTietTaiKhoan(idTaiKhoan: Int): Kq<ChiTietTaiKhoan>
    /** Cấm đăng nhập toàn hệ thống (DaKhoa). Không cấm được tài khoản AdminApp. */
    fun camTaiKhoan(idTaiKhoan: Int): Kq<Unit>
    /** Gỡ cấm (về HoatDong). */
    fun goCamTaiKhoan(idTaiKhoan: Int): Kq<Unit>
    // ---- AdminApp: không gian toàn hệ thống (GD-23). Chỉ LoaiTaiKhoan = AdminApp ----
    /** Lọc theo BoLocKhongGian (các điều kiện cộng lại), mới tạo xếp trước, 20 dòng mỗi trang. tuNgay sau denNgay: lỗi. */
    fun danhSachKhongGianHeThong(loc: BoLocKhongGian, trang: Int): Kq<TrangKhongGianHeThong>
    // ---- AdminApp: chi tiết không gian (GD-24). Chỉ xem ----
    fun chiTietKhongGianHeThong(idKhongGian: Int): Kq<ChiTietKhongGianHeThong>
    /** Dữ liệu cây để xem chỉ đọc; DuLieuCay.toi là bản ghi giả (Khách tạm, không có Ô). */
    fun duLieuCayChoAdminApp(idKhongGian: Int): Kq<DuLieuCay>
}

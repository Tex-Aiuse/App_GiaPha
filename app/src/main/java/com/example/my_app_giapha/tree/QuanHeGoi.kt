package com.example.my_app_giapha.tree

import com.example.my_app_giapha.model.GioiTinh
import com.example.my_app_giapha.model.LoaiQuanHe
import com.example.my_app_giapha.model.OThanhVien
import com.example.my_app_giapha.model.QHeHonNhan
import com.example.my_app_giapha.model.QuanHeChaMe
import com.example.my_app_giapha.model.TrangThaiHonNhan

class DuLieuGoi(
    val o: List<OThanhVien>,
    val chaMe: List<QuanHeChaMe>,
    val honNhan: List<QHeHonNhan>
)

/** Một quan hệ giữa hai người: cách người thứ nhất gọi người thứ hai và ngược lại. */
data class MoiQuanHeGoi(
    val kieu: String,
    val aGoiB: List<String>,
    val bGoiA: List<String>
)

/**
 * ds rỗng nghĩa là chưa có tên gọi cụ thể: ghiChu giải thích (họ hàng xa, thông gia xa, hoặc chưa nối).
 */
data class KetQuaGoi(
    val a: OThanhVien,
    val b: OThanhVien,
    val ds: List<MoiQuanHeGoi>,
    val ghiChu: String?
)

object QuanHeGoi {

    private const val BUOC_TOI_DA = 5

    private enum class B { U, D, S }

    private data class Canh(val den: Int, val loai: B, val ql: LoaiQuanHe?, val tt: TrangThaiHonNhan?)

    private class Duong(val nut: List<Int>, val canh: List<Canh>) {
        val mau: String get() = canh.joinToString("") { it.loai.name }
        fun nguoc(): Duong = Duong(
            nut.reversed(),
            canh.indices.reversed().map { i ->
                val c = canh[i]
                Canh(nut[i], when (c.loai) { B.U -> B.D; B.D -> B.U; B.S -> B.S }, c.ql, c.tt)
            }
        )
    }

    private class Xung(val tu: List<String>, val kieu: String)

    /** Thứ tự ưu tiên khi hiện nhiều quan hệ: gần trước, xa sau. */
    private val UU_TIEN = listOf(
        "U", "D", "S", "UD", "UU", "DD", "SU", "DS", "SD", "US", "UUD", "UDD", "SUD", "UDS",
        "DSU", "SUU", "DDS", "UUDD", "UUDS", "SUDD", "UUU", "DDD", "UUUD", "UDDD", "UDDS", "SUUD",
        "SUDS", "USD", "UUUU", "DDDD", "UUUDD", "UUDDD"
    )

    // Tên kiểu quan hệ dùng chung cho hai chiều (MoiQuanHeGoi lấy tên theo chiều xuôi nên hai chiều phải trùng).
    private const val KIEU_ONG_BAC = "Ông bác, ông chú, bà cô, ông cậu, bà dì – cháu"
    private const val KIEU_BAC_HO = "Bác, chú, cô, cậu, dì họ – cháu họ"
    private const val KIEU_CHAU_DAU_BAC = "Cháu dâu, cháu rể – bác, chú, cô, cậu, dì"
    private const val KIEU_CHAU_DAU_ONG = "Cháu dâu, cháu rể – ông bà"

    private val CHA = listOf("cha", "bố", "ba")
    private val ME = listOf("mẹ", "má")

    fun tinh(du: DuLieuGoi, idA: Int, idB: Int): KetQuaGoi? {
        val a = du.o.firstOrNull { it.idOThanhVien == idA } ?: return null
        val b = du.o.firstOrNull { it.idOThanhVien == idB } ?: return null
        if (idA == idB) return KetQuaGoi(a, b, emptyList(), "Đây là cùng một người.")
        return Tinh(du).chay(a, b)
    }

    // ------------------------------------------------------------------ bên trong

    private class Tinh(val du: DuLieuGoi) {
        private val oTheoId = du.o.associateBy { it.idOThanhVien }
        private val len = HashMap<Int, MutableList<Canh>>()
        private val cha = HashMap<Int, MutableList<Int>>()

        init {
            for (q in du.chaMe) {
                len.getOrPut(q.idCon) { mutableListOf() }.add(Canh(q.idPhuHuynh, B.U, q.loai, null))
                len.getOrPut(q.idPhuHuynh) { mutableListOf() }.add(Canh(q.idCon, B.D, q.loai, null))
                cha.getOrPut(q.idCon) { mutableListOf() }.add(q.idPhuHuynh)
            }
            for (h in du.honNhan) {
                len.getOrPut(h.idChong) { mutableListOf() }.add(Canh(h.idVo, B.S, null, h.trangThai))
                len.getOrPut(h.idVo) { mutableListOf() }.add(Canh(h.idChong, B.S, null, h.trangThai))
            }
        }

        private fun gioi(id: Int): GioiTinh? = oTheoId[id]?.gioiTinh
        private fun ten(id: Int): String = oTheoId[id]?.hoTen ?: "?"

        /** x lớn tuổi hơn y? null khi thiếu ngày sinh hoặc cùng ngày. */
        private fun lonHon(x: Int, y: Int): Boolean? {
            val sx = oTheoId[x]?.ngaySinh ?: return null
            val sy = oTheoId[y]?.ngaySinh ?: return null
            return when {
                sx < sy -> true
                sx > sy -> false
                else -> null
            }
        }

        private fun theoGioi(id: Int, nam: List<String>, nu: List<String>): List<String> = when (gioi(id)) {
            GioiTinh.NAM -> nam
            GioiTinh.NU -> nu
            else -> listOf(nam.first(), nu.first())
        }

        /** anh / chị / em của y so với mốc ref (y lớn hơn thì anh hoặc chị, nhỏ hơn thì em). */
        private fun anhChiEm(y: Int, ref: Int): List<String> = anhChiEm(y, lonHon(y, ref))

        /** lon = true: y là anh/chị; false: y là em; null: chưa biết nên ghi cả hai. */
        private fun anhChiEm(y: Int, lon: Boolean?): List<String> {
            val anhChi = when (gioi(y)) {
                GioiTinh.NAM -> listOf("anh")
                GioiTinh.NU -> listOf("chị")
                else -> listOf("anh", "chị")
            }
            return when (lon) {
                true -> anhChi
                false -> listOf("em")
                null -> anhChi + "em"
            }
        }

        // ---------------- đếm đường ----------------

        fun chay(a: OThanhVien, b: OThanhVien): KetQuaGoi {
            val tim = ArrayList<Duong>()
            val nut = ArrayList<Int>().apply { add(a.idOThanhVien) }
            val canh = ArrayList<Canh>()
            fun dfs(x: Int) {
                if (canh.size >= BUOC_TOI_DA) return
                for (c in len[x].orEmpty()) {
                    if (c.den in nut) continue
                    nut.add(c.den); canh.add(c)
                    if (c.den == b.idOThanhVien) tim.add(Duong(nut.toList(), canh.toList()))
                    else dfs(c.den)
                    nut.removeAt(nut.size - 1); canh.removeAt(canh.size - 1)
                }
            }
            dfs(a.idOThanhVien)

            val gop = LinkedHashMap<Triple<String, List<String>, List<String>>, Int>()
            val ds = ArrayList<Pair<Int, MoiQuanHeGoi>>()
            for (d in tim.sortedBy { UU_TIEN.indexOf(it.mau).let { i -> if (i < 0) 99 else i } }) {
                val moi = moiQuanHe(d) ?: continue
                val khoa = Triple(moi.kieu, moi.aGoiB, moi.bGoiA)
                if (gop.putIfAbsent(khoa, 1) == null) ds.add(UU_TIEN.indexOf(d.mau) to moi)
            }
            val ketQua = ds.map { it.second }
            return KetQuaGoi(a, b, ketQua, if (ketQua.isEmpty()) giaiThich(a, b) else null)
        }

        private fun moiQuanHe(d: Duong): MoiQuanHeGoi? {
            // Hôn nhân đã ly hôn chỉ cho ra "vợ cũ/chồng cũ", không suy ra họ hàng bên đó
            if (d.mau != "S" && d.canh.any { it.loai == B.S && it.tt == TrangThaiHonNhan.DA_LY_HON }) return null
            val xuoi = xungHo(d) ?: return null
            val nguoc = xungHo(d.nguoc()) ?: return null
            return MoiQuanHeGoi(xuoi.kieu, xuoi.tu, nguoc.tu)
        }

        // ---------------- bảng quy tắc: người đầu gọi người cuối ----------------

        private fun xungHo(d: Duong): Xung? {
            val n = d.nut
            val x = n.first()
            val y = n.last()
            return when (d.mau) {
                "U" -> {
                    val ql = d.canh[0].ql
                    when (ql) {
                        LoaiQuanHe.NUOI -> Xung(theoGioi(y, listOf("cha nuôi"), listOf("mẹ nuôi")), "Cha mẹ nuôi – con nuôi")
                        LoaiQuanHe.KE -> Xung(theoGioi(y, listOf("cha kế", "dượng"), listOf("mẹ kế")), "Cha mẹ kế – con riêng")
                        else -> Xung(theoGioi(y, CHA, ME), "Cha mẹ – con")
                    }
                }
                "D" -> when (d.canh[0].ql) {
                    LoaiQuanHe.NUOI -> Xung(listOf("con nuôi"), "Cha mẹ nuôi – con nuôi")
                    LoaiQuanHe.KE -> Xung(listOf("con riêng"), "Cha mẹ kế – con riêng")
                    else -> Xung(listOf("con"), "Cha mẹ – con")
                }
                "S" -> {
                    val tt = d.canh[0].tt
                    when (tt) {
                        TrangThaiHonNhan.DA_LY_HON ->
                            Xung(theoGioi(y, listOf("chồng cũ"), listOf("vợ cũ")), "Vợ chồng (đã ly hôn)")
                        TrangThaiHonNhan.GOA ->
                            Xung(theoGioi(y, listOf("chồng"), listOf("vợ")), "Vợ chồng (góa)")
                        else -> Xung(theoGioi(y, listOf("chồng"), listOf("vợ")), "Vợ chồng")
                    }
                }
                "UD" -> {
                    val chung = (cha[x].orEmpty().toSet() intersect cha[y].orEmpty().toSet())
                    val kieu = when {
                        chung.size >= 2 -> "Anh chị em ruột"
                        chung.size == 1 -> when (gioi(chung.first())) {
                            GioiTinh.NAM -> "Anh chị em cùng cha khác mẹ"
                            GioiTinh.NU -> "Anh chị em cùng mẹ khác cha"
                            else -> "Anh chị em cùng cha hoặc mẹ"
                        }
                        else -> "Anh chị em"
                    }
                    Xung(anhChiEm(y, x), kieu)
                }
                "UU" -> {
                    val canh = when (gioi(n[1])) { GioiTinh.NAM -> listOf("nội"); GioiTinh.NU -> listOf("ngoại"); else -> listOf("nội", "ngoại") }
                    val ong = when (gioi(y)) { GioiTinh.NAM -> listOf("ông"); GioiTinh.NU -> listOf("bà"); else -> listOf("ông", "bà") }
                    Xung(ong.flatMap { o -> canh.map { "$o $it" } }, "Ông bà – cháu")
                }
                "DD" -> {
                    val canh = when (gioi(n[1])) { GioiTinh.NAM -> listOf("nội"); GioiTinh.NU -> listOf("ngoại"); else -> listOf("nội", "ngoại") }
                    Xung(canh.map { "cháu $it" }, "Ông bà – cháu")
                }
                "UUU" -> {
                    val canh = when (gioi(n[1])) { GioiTinh.NAM -> " nội"; GioiTinh.NU -> " ngoại"; else -> "" }
                    val cu = when (gioi(y)) { GioiTinh.NAM -> listOf("cụ ông"); GioiTinh.NU -> listOf("cụ bà"); else -> listOf("cụ ông", "cụ bà") }
                    Xung(cu.map { it + canh }, "Cụ – chắt")
                }
                "DDD" -> {
                    val canh = when (gioi(n[1])) { GioiTinh.NAM -> " nội"; GioiTinh.NU -> " ngoại"; else -> "" }
                    Xung(listOf("chắt$canh"), "Cụ – chắt")
                }
                "UUUU" -> Xung(listOf("kỵ"), "Kỵ – chút")
                "DDDD" -> Xung(listOf("chút"), "Kỵ – chút")
                "UUD" -> Xung(bacChuCoCauDi(n[1], n[3]), "Bác, chú, cô, cậu, dì – cháu")
                "UDD" -> Xung(listOf("cháu"), "Bác, chú, cô, cậu, dì – cháu")
                "UUDD" -> {
                    // Anh chị em họ xếp theo vai vế của cha mẹ: con của anh/chị cha mẹ là anh/chị, con của em cha mẹ là em,
                    // dù sinh trước hay sau. Thiếu ngày sinh của cha mẹ thì mới so tuổi hai người.
                    val lon = lonHon(n[3], n[1]) ?: lonHon(y, x)
                    Xung(anhChiEm(y, lon).map { "$it họ" }, "Anh chị em họ")
                }
                "SU" -> Xung(
                    theoGioi(y, CHA, ME).map { it + hauToVoChong(x) },
                    "Cha mẹ vợ/chồng – con rể/dâu"
                )
                "DS" -> Xung(
                    when (gioi(y)) { GioiTinh.NAM -> listOf("con rể"); GioiTinh.NU -> listOf("con dâu"); else -> listOf("con rể", "con dâu") },
                    "Cha mẹ vợ/chồng – con rể/dâu"
                )
                "SUD" -> Xung(
                    anhChiEm(y, n[1]).map { it + hauToVoChong(x) },
                    "Anh chị em vợ/chồng"
                )
                "UDS" -> {
                    val w = n[2]                     // anh chị em của x, người có vợ/chồng là y
                    val wLon = lonHon(w, x)
                    val tu = when (gioi(y)) {
                        GioiTinh.NAM -> rePhuDau("anh rể", "em rể", wLon)
                        GioiTinh.NU -> rePhuDau("chị dâu", "em dâu", wLon)
                        else -> rePhuDau("anh rể", "em rể", wLon) + rePhuDau("chị dâu", "em dâu", wLon)
                    }
                    Xung(tu, "Anh chị em – vợ/chồng của họ")
                }
                "UUDS" -> {
                    val q = bacChuCoCauDi(n[1], n[3])
                    val nam = gioi(y) == GioiTinh.NAM
                    val nu = gioi(y) == GioiTinh.NU
                    val tu = q.mapNotNull {
                        when (it) {
                            "bác" -> if (nu) "bác gái" else if (nam) "bác trai" else null
                            "chú" -> if (nu) "thím" else null
                            "cô" -> if (nam) "dượng" else null
                            "cậu" -> if (nu) "mợ" else null
                            "dì" -> if (nam) "dượng" else null
                            else -> null
                        }
                    }.distinct()
                    if (tu.isEmpty()) null else Xung(tu, "Vợ/chồng của bác, chú, cô, cậu, dì")
                }
                "SUDD" -> Xung(listOf("cháu"), "Vợ/chồng của bác, chú, cô, cậu, dì")
                "SD" ->
                    if (len[x].orEmpty().any { it.loai == B.D && it.den == y }) null     // đã là con ruột/nuôi: không gọi là con riêng
                    else Xung(listOf("con riêng"), "Cha mẹ kế – con riêng")
                "US" ->
                    if (len[x].orEmpty().any { it.loai == B.U && it.den == y }) null     // đã là cha/mẹ: không gọi là cha/mẹ kế
                    else Xung(
                        when (gioi(y)) { GioiTinh.NAM -> listOf("cha kế", "dượng"); GioiTinh.NU -> listOf("mẹ kế"); else -> listOf("cha kế", "mẹ kế") },
                        "Cha mẹ kế – con riêng"
                    )
                "UUUD" -> Xung(
                    bacChuCoCauDi(n[2], n[4]).map { ongBa(y, it) },
                    KIEU_ONG_BAC
                )
                "UDDD" -> Xung(listOf("cháu"), KIEU_ONG_BAC)
                "UUUDD" -> Xung(
                    bacChuCoCauDi(n[1], n[5], lonHon(n[4], n[2]) ?: lonHon(n[5], n[1])).map { "$it họ" },
                    KIEU_BAC_HO
                )
                "UUDDD" -> Xung(listOf("cháu họ"), KIEU_BAC_HO)
                "UDDS" -> Xung(chauDauRe(y), KIEU_CHAU_DAU_BAC)
                "SUUD" -> Xung(bacChuCoCauDi(n[2], n[4]), KIEU_CHAU_DAU_BAC)
                "DDS" -> Xung(chauDauRe(y), KIEU_CHAU_DAU_ONG)
                "SUU" -> {
                    val canh = when (gioi(n[2])) { GioiTinh.NAM -> listOf("nội"); GioiTinh.NU -> listOf("ngoại"); else -> listOf("nội", "ngoại") }
                    val ong = when (gioi(y)) { GioiTinh.NAM -> listOf("ông"); GioiTinh.NU -> listOf("bà"); else -> listOf("ông", "bà") }
                    Xung(ong.flatMap { o -> canh.map { "$o $it${benVoChong(x)}" } }, KIEU_CHAU_DAU_ONG)
                }
                "DSU" -> Xung(
                    when (gioi(y)) {
                        GioiTinh.NAM -> listOf("ông thông gia", "ông sui")
                        GioiTinh.NU -> listOf("bà thông gia", "bà sui")
                        else -> listOf("ông thông gia", "bà thông gia", "ông sui", "bà sui")
                    },
                    "Thông gia (sui gia)"
                )
                "SUDS" -> {
                    // Vợ/chồng của hai anh chị em ruột: hai người chồng là "anh em đồng hao", hai người vợ là "chị em dâu".
                    // Khác giới thì không có tên gọi riêng.
                    val gx = gioi(x)
                    val gy = gioi(y)
                    val dongHao = listOf("anh em đồng hao", "anh em cột chèo")
                    val dau = listOf("chị em dâu")
                    when {
                        gx == GioiTinh.NAM && gy == GioiTinh.NAM -> Xung(dongHao, "Anh em đồng hao")
                        gx == GioiTinh.NU && gy == GioiTinh.NU -> Xung(dau, "Chị em dâu")
                        gx != null && gy != null && gx != gy -> null
                        gx == null && gy == null -> Xung(dongHao + dau, "Anh em đồng hao, chị em dâu")
                        gx == GioiTinh.NAM || gy == GioiTinh.NAM -> Xung(dongHao, "Anh em đồng hao")
                        else -> Xung(dau, "Chị em dâu")
                    }
                }
                "USD" ->
                    // con riêng của cha/mẹ kế; nếu đã là anh chị em ruột/cùng cha mẹ thì không gọi là anh chị em kế
                    if (len[x].orEmpty().any { it.loai == B.U && it.den == n[2] } || n[1] in cha[y].orEmpty()) null
                    else Xung(anhChiEm(y, x).map { "$it kế" }, "Anh chị em kế")
                else -> null
            }
        }

        /** "ông" hoặc "bà" đặt trước bác/chú/cô/cậu/dì khi người đó thuộc hàng ông bà (anh chị em của ông bà). */
        private fun ongBa(y: Int, q: String): String = when (gioi(y)) {
            GioiTinh.NAM -> "ông $q"
            GioiTinh.NU -> "bà $q"
            else -> if (q == "cô" || q == "dì") "bà $q" else if (q == "bác") "ông $q" else "ông $q"
        }

        private fun chauDauRe(y: Int): List<String> = when (gioi(y)) {
            GioiTinh.NAM -> listOf("cháu rể")
            GioiTinh.NU -> listOf("cháu dâu")
            else -> listOf("cháu rể", "cháu dâu")
        }

        private fun benVoChong(x: Int) = when (gioi(x)) {
            GioiTinh.NAM -> " bên vợ"
            GioiTinh.NU -> " bên chồng"
            else -> " bên vợ hoặc chồng"
        }

        /** vợ / chồng tùy giới tính người nói (nam có vợ, nữ có chồng). */
        private fun hauToVoChong(x: Int) = when (gioi(x)) {
            GioiTinh.NAM -> " vợ"
            GioiTinh.NU -> " chồng"
            else -> " vợ hoặc chồng"
        }

        private fun rePhuDau(lon: String, nho: String, wLon: Boolean?): List<String> = when (wLon) {
            true -> listOf(lon)
            false -> listOf(nho)
            null -> listOf(lon, nho)
        }

        /**
         * Anh chị em của cha mẹ: p là cha/mẹ của người nói, y là anh chị em của p.
         * Bên cha: anh, chị của cha là bác; em trai là chú; em gái là cô. Bên mẹ: anh em trai là cậu, chị em gái là dì.
         */
        private fun bacChuCoCauDi(p: Int, y: Int, lonY: Boolean? = lonHon(y, p)): List<String> {
            fun benCha(): List<String> {
                val lon = lonY
                return when (gioi(y)) {
                    GioiTinh.NAM -> when (lon) { true -> listOf("bác"); false -> listOf("chú"); null -> listOf("bác", "chú") }
                    GioiTinh.NU -> when (lon) { true -> listOf("bác"); false -> listOf("cô"); null -> listOf("bác", "cô") }
                    else -> listOf("bác", "chú", "cô")
                }
            }
            fun benMe(): List<String> = when (gioi(y)) {
                GioiTinh.NAM -> listOf("cậu")
                GioiTinh.NU -> listOf("dì")
                else -> listOf("cậu", "dì")
            }
            return when (gioi(p)) {
                GioiTinh.NAM -> benCha()
                GioiTinh.NU -> benMe()
                else -> (benCha() + benMe()).distinct()
            }
        }

        // ---------------- khi không có tên gọi cụ thể ----------------

        private fun giaiThich(a: OThanhVien, b: OThanhVien): String {
            val tuA = tienBoi(a.idOThanhVien)
            val tuB = tienBoi(b.idOThanhVien)
            val chung = tuA.keys.intersect(tuB.keys)
            if (chung.isNotEmpty()) {
                val gan = chung.minByOrNull { tuA.getValue(it) + tuB.getValue(it) }!!
                return "Họ hàng xa: cùng tổ tiên ${ten(gan)} (cách ${a.hoTen} ${tuA.getValue(gan)} đời, cách ${b.hoTen} ${tuB.getValue(gan)} đời)."
            }
            // còn nối với nhau qua đường nào không (kể cả hôn nhân)?
            val thay = HashSet<Int>().apply { add(a.idOThanhVien) }
            val ngan = ArrayDeque<Int>().apply { add(a.idOThanhVien) }
            while (ngan.isNotEmpty()) {
                for (c in len[ngan.removeLast()].orEmpty()) if (thay.add(c.den)) ngan.add(c.den)
            }
            return if (b.idOThanhVien in thay) "Có quan hệ thông gia xa, chưa có cách gọi cụ thể."
            else "Hai người chưa có quan hệ nào được ghi nhận trên cây."
        }

        /** id tổ tiên -> số đời cách x (0 = chính x). */
        private fun tienBoi(x: Int): Map<Int, Int> {
            val kq = HashMap<Int, Int>().apply { put(x, 0) }
            val hang = ArrayDeque<Int>().apply { add(x) }
            while (hang.isNotEmpty()) {
                val v = hang.removeFirst()
                for (p in cha[v].orEmpty()) if (p !in kq) { kq[p] = kq.getValue(v) + 1; hang.add(p) }
            }
            return kq
        }
    }
}

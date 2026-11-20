package com.example.my_app_giapha.ui.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import kotlin.math.max

/**
 * Chọn, đọc, nén và lưu ảnh đại diện tải lên từ thiết bị.
 * Chỉ nhận JPG và PNG. Ảnh lưu ra luôn là JPEG, cạnh dài tối đa 1024px nên rất nhẹ (dưới 5MB).
 * Đường dẫn file lưu bắt đầu bằng "/" nên AnhLoader nạp được.
 */
object AnhFile {
    private const val TOI_DA_GOC = 25L * 1024 * 1024   // chỉ để chặn ảnh khổng lồ làm tràn bộ nhớ
    const val CANH_TOI_DA = 1024
    private const val CHAT_LUONG = 88

    fun thuMucAvatar(ctx: Context): File = File(ctx.filesDir, "avatars").apply { mkdirs() }
    fun thuMucTam(ctx: Context): File = File(ctx.cacheDir, "crop").apply { mkdirs() }

    fun donDep(ctx: Context) {
        File(ctx.filesDir, "avatars").deleteRecursively()
        File(ctx.cacheDir, "crop").deleteRecursively()
    }

    /** Trả về thông báo lỗi tiếng Việt, hoặc null nếu ảnh dùng được. */
    fun kiemTra(ctx: Context, uri: Uri): String? {
        val loai = ctx.contentResolver.getType(uri)
        if (loai != "image/jpeg" && loai != "image/png")
            return "Định dạng không được hỗ trợ. Chỉ nhận ảnh JPG hoặc PNG."
        val dungLuong = runCatching {
            ctx.contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use {
                if (it.moveToFirst() && !it.isNull(0)) it.getLong(0) else null
            }
        }.getOrNull()
        if (dungLuong != null && dungLuong > TOI_DA_GOC) return "Ảnh quá lớn (tối đa 25MB)."
        return null
    }

    /** Đọc ảnh, thu nhỏ khi cần (cạnh dài không quá canhToiDa) và xoay theo EXIF. Lỗi thì trả null. */
    fun docAnh(ctx: Context, uri: Uri, canhToiDa: Int = 2048): Bitmap? {
        return runCatching {
            val cr = ctx.contentResolver
            val kichThuoc = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            cr.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, kichThuoc) }
            if (kichThuoc.outWidth <= 0 || kichThuoc.outHeight <= 0) return null
            var mau = 1
            while (max(kichThuoc.outWidth, kichThuoc.outHeight) / mau > canhToiDa) mau *= 2
            val bm = cr.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = mau })
            } ?: return null
            val huong = cr.openInputStream(uri)?.use {
                ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            } ?: ExifInterface.ORIENTATION_NORMAL
            xoay(bm, huong)
        }.getOrNull()
    }

    private fun xoay(bm: Bitmap, huong: Int): Bitmap {
        val m = Matrix()
        when (huong) {
            ExifInterface.ORIENTATION_ROTATE_90 -> m.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> m.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> m.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> m.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> m.postScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> { m.postRotate(90f); m.postScale(-1f, 1f) }
            ExifInterface.ORIENTATION_TRANSVERSE -> { m.postRotate(-90f); m.postScale(-1f, 1f) }
            else -> return bm
        }
        return Bitmap.createBitmap(bm, 0, 0, bm.width, bm.height, m, true)
    }

    /** Thu về cạnh dài tối đa CANH_TOI_DA, nền trắng (PNG trong suốt), nén JPEG rồi ghi ra file. */
    fun luuJpeg(bm: Bitmap, dich: File) {
        val s = minOf(1f, CANH_TOI_DA.toFloat() / max(bm.width, bm.height))
        val w = max(1, (bm.width * s).toInt())
        val h = max(1, (bm.height * s).toInt())
        val nho = if (s < 1f) Bitmap.createScaledBitmap(bm, w, h, true) else bm
        val ra = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        Canvas(ra).apply { drawColor(Color.WHITE); drawBitmap(nho, 0f, 0f, null) }
        dich.outputStream().use { ra.compress(Bitmap.CompressFormat.JPEG, CHAT_LUONG, it) }
    }

    /** Chép file đã cắt (ở thư mục tạm) vào thư mục ảnh đại diện lâu dài. */
    fun chepVaoAvatar(ctx: Context, nguon: File, idO: Int): File {
        val dich = File(thuMucAvatar(ctx), "o${idO}_${System.currentTimeMillis()}.jpg")
        nguon.copyTo(dich, overwrite = true)
        return dich
    }
}

package com.example.my_app_giapha

import android.app.Application
import com.example.my_app_giapha.ui.common.AnhFile

/**
 * Chạy một lần khi tiến trình app khởi động. Dữ liệu cứng luôn bắt đầu lại từ SampleData
 * (QUYET_DINH mục 8) nên ảnh đại diện người dùng tải lên ở lần chạy trước không còn ai tham chiếu: dọn đi.
 */
class GiaPhaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AnhFile.donDep(this)
    }
}

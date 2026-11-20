package com.example.my_app_giapha.ui.admin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.my_app_giapha.databinding.ActivityAdminHomeBinding
import com.example.my_app_giapha.ui.common.TabAdmin
import com.example.my_app_giapha.ui.common.datTab
import com.example.my_app_giapha.ui.common.setup

/**
 * Màn gốc của AdminApp: thanh tab cố định ở trên, bên dưới là hai trang vuốt ngang được
 * (GD-21 Tài khoản, GD-23 Không gian). Bấm tab thì nội dung trượt sang, không mở màn hình mới.
 */
class AdminHomeActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TAB = "tab"       // 0 = Tài khoản, 1 = Không gian
    }

    private lateinit var b: ActivityAdminHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityAdminHomeBinding.inflate(layoutInflater)
        setContentView(b.root)

        val tab = intent.getIntExtra(EXTRA_TAB, 0).coerceIn(0, TabAdmin.entries.size - 1)

        b.pager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = TabAdmin.entries.size
            override fun createFragment(position: Int): Fragment =
                if (position == 0) TaiKhoanHeThongFragment() else KhongGianHeThongFragment()
        }
        b.pager.offscreenPageLimit = 1        // giữ cả hai trang, nhớ ô tìm và trang đang xem khi chuyển tab

        b.adminBar.setup(this, TabAdmin.entries[tab]) { t -> b.pager.setCurrentItem(t.ordinal, true) }
        b.pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                b.adminBar.datTab(TabAdmin.entries[position])
            }
        })
        b.pager.setCurrentItem(tab, false)
    }
}
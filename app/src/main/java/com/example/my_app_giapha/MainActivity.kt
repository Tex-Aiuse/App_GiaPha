package com.example.my_app_giapha

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.my_app_giapha.ui.common.setup
import com.example.my_app_giapha.ui.common.setAction
import com.example.my_app_giapha.ui.common.setRoleBadge
import com.example.my_app_giapha.ui.common.setBlockedBadge
import com.example.my_app_giapha.ui.common.attachPhoneFormat
import com.example.my_app_giapha.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.appBar.setup(this, "Hồ sơ cá nhân")
        binding.appBar.setAction(this, R.drawable.ic_more_vert, "Menu") { }

        binding.phoneInput.etPhone.attachPhoneFormat()
        binding.otp.onCompleted = { code -> binding.otp.isError = code != "123456" }

        binding.b1.setRoleBadge("Admin")
        binding.b2.setRoleBadge("QuanLy")
        binding.b3.setRoleBadge("ThanhVien")
        binding.b4.setRoleBadge("ThanhVienHo")
        binding.b5.setRoleBadge("KhachTam")
        binding.b6.setBlockedBadge()

    }
}
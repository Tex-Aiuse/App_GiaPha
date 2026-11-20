package com.example.my_app_giapha.ui.common

import androidx.fragment.app.FragmentManager
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.CompositeDateValidator
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

private const val TAG_CHON_NGAY = "hopChonNgay"

/**
 * Hộp chọn ngày dùng chung. Mở sẵn ở chế độ GÕ ngày (dd/MM/yyyy), nên chọn năm 1943 chỉ cần gõ;
 * bấm biểu tượng bút ở góc để chuyển sang lịch.
 * toiThieu: không cho chọn ngày sớm hơn (ví dụ ngày mất không trước ngày sinh).
 * chanTuongLai: không cho chọn ngày sau hôm nay (mặc định bật).
 */
fun hienHopChonNgay(
    fm: FragmentManager,
    hienTai: LocalDate?,
    toiThieu: LocalDate? = null,
    chanTuongLai: Boolean = true,
    khiChon: (LocalDate) -> Unit
) {
    if (fm.findFragmentByTag(TAG_CHON_NGAY) != null) return   // bấm đúp: chỉ mở một hộp

    fun utc(d: LocalDate) = d.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    val homNay = LocalDate.now()
    var dangChon = hienTai ?: homNay
    if (chanTuongLai && dangChon.isAfter(homNay)) dangChon = homNay
    if (toiThieu != null && dangChon.isBefore(toiThieu)) dangChon = toiThieu

    val validators = mutableListOf<CalendarConstraints.DateValidator>()
    val rb = CalendarConstraints.Builder().setOpenAt(utc(dangChon))
    if (toiThieu != null) {
        rb.setStart(utc(toiThieu))
        validators += DateValidatorPointForward.from(utc(toiThieu))
    }
    if (chanTuongLai) validators += DateValidatorPointBackward.before(utc(homNay))
    if (validators.isNotEmpty()) rb.setValidator(CompositeDateValidator.allOf(validators))

    val picker = MaterialDatePicker.Builder.datePicker()
        .setTitleText("Chọn ngày")
        .setSelection(utc(dangChon))
        .setCalendarConstraints(rb.build())
        .setInputMode(MaterialDatePicker.INPUT_MODE_TEXT)
        .build()
    picker.addOnPositiveButtonClickListener {
        khiChon(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate())
    }
    picker.show(fm, TAG_CHON_NGAY)
}

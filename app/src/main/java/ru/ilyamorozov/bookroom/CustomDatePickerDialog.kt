package ru.ilyamorozov.bookroom

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.widget.Button
import java.util.Calendar

class CustomDatePickerDialog(
    context: Context,
    private val onDateSet: (year: Int, month: Int, day: Int?) -> Unit,
    year: Int,
    month: Int,
    day: Int
) : DatePickerDialog(context, null, year, month, day) {

    private lateinit var btnDontRemember: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Получаем DatePicker через официальный метод
        val datePicker = this.datePicker

        // Кнопка "Не помню"
        btnDontRemember = Button(context).apply {
            text = "Не помню точную дату"
            setOnClickListener {
                val cal = Calendar.getInstance().apply {
                    set(datePicker.year, datePicker.month, 1)  // 1-е число месяца
                }
                onDateSet(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), null)
                dismiss()
            }
        }

        // Добавляем кнопку в диалог
        val buttonBar = getButton(BUTTON_POSITIVE).parent as android.view.ViewGroup
        buttonBar.addView(btnDontRemember, 0)
    }

    // Сбрасываем режим при ручном выборе
    override fun onDateChanged(view: android.widget.DatePicker, year: Int, month: Int, dayOfMonth: Int) {
        super.onDateChanged(view, year, month, dayOfMonth)
        // Ничего не делаем — режим по умолчанию
    }
}
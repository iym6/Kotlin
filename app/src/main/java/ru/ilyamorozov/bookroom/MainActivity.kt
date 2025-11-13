package ru.ilyamorozov.bookroom

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.textfield.TextInputEditText
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar
import androidx.core.graphics.drawable.toDrawable

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: BookViewModel
    private var currentAddBookDialog: AlertDialog? = null
    private var currentBookCoverPath: String? = null

    private val isbnScanLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            fetchBookByIsbn(result.contents)
        } else {
            Toast.makeText(this, getString(R.string.scan_cancelled), Toast.LENGTH_SHORT).show()
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) startIsbnScanner()
        else Toast.makeText(this, getString(R.string.camera_required), Toast.LENGTH_LONG).show()
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val internalPath = copyImageToInternalStorage(it)
            if (internalPath != null) {
                currentBookCoverPath = internalPath
                updateCoverImageInDialog(internalPath)
            } else {
                Toast.makeText(this, getString(R.string.cover_save_failed), Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Retrofit-сервис
    private val apiService = ApiService.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val factory = BookViewModelFactory(application)
        viewModel = ViewModelProvider(this, factory)[BookViewModel::class.java]

        val viewPager = findViewById<androidx.viewpager2.widget.ViewPager2>(R.id.view_pager)
        val tabLayout = findViewById<TabLayout>(R.id.tab_layout)
        val btnAddBook =
            findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.btn_add_book)

        viewPager.adapter = ViewPagerAdapter(this)

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.New)
                1 -> getString(R.string.Currently_reading)
                2 -> getString(R.string.Read)
                else -> null
            }
        }.attach()

        btnAddBook.setOnClickListener { showAddBookDialog() }
    }

    //Диалог добавления/редактирования
    fun showAddBookDialog(book: Book? = null) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_book, null)
        val dialog = AlertDialog.Builder(this, R.style.CustomAlertDialog)
            .setView(view)
            .setCancelable(true)
            .create()

        // Views
        val imgCover = view.findViewById<ImageView>(R.id.img_cover)
        val etAuthor = view.findViewById<TextInputEditText>(R.id.et_author)
        val etTitle = view.findViewById<TextInputEditText>(R.id.et_title)
        val etPublisher = view.findViewById<TextInputEditText>(R.id.et_publisher)
        val etPages = view.findViewById<TextInputEditText>(R.id.et_pages)
        val etDescription = view.findViewById<TextInputEditText>(R.id.et_description)
        val btnScanIsbn = view.findViewById<Button>(R.id.btn_scan_isbn)
        val btnSave = view.findViewById<Button>(R.id.btn_save)
        val btnCancel = view.findViewById<Button>(R.id.btn_cancel)
        val btnDelete = view.findViewById<Button>(R.id.btn_delete)

        // Показать кнопку "Удалить" только при редактировании
        btnDelete.visibility = if (book != null) View.VISIBLE else View.GONE
        btnScanIsbn.visibility = if (book != null) View.INVISIBLE else View.VISIBLE

        // Заполнение полей
        currentBookCoverPath = book?.coverUrl
        book?.let {
            etAuthor.setText(it.author)
            etTitle.setText(it.title)
            etPublisher.setText(it.publisher)
            etPages.setText(it.pageCount?.toString() ?: "")
            etDescription.setText(it.description)
            loadCoverIntoImageView(imgCover, it.coverUrl)
        }

        // Клик по обложке
        imgCover.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // Сканирование ISBN
        btnScanIsbn.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
            ) {
                startIsbnScanner()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }

        // Кнопка "Сохранить"
        btnSave.setOnClickListener {
            val author = etAuthor.text.toString().trim()
            val title = etTitle.text.toString().trim()

            if (author.isBlank() || title.isBlank()) {
                Toast.makeText(this, getString(R.string.author_title_required), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newBook = book?.copy(
                author = author,
                title = title,
                publisher = etPublisher.text.toString().trim().takeIf { it.isNotBlank() },
                pageCount = etPages.text.toString().toIntOrNull(),
                description = etDescription.text.toString().trim().takeIf { it.isNotBlank() },
                coverUrl = currentBookCoverPath
            ) ?: Book(
                author = author,
                title = title,
                publisher = etPublisher.text.toString().trim().takeIf { it.isNotBlank() },
                pageCount = etPages.text.toString().toIntOrNull(),
                description = etDescription.text.toString().trim().takeIf { it.isNotBlank() },
                coverUrl = currentBookCoverPath
            )

            if (book == null) {
                viewModel.addBook(newBook)
            } else {
                viewModel.updateBook(newBook)
            }

            dialog.dismiss()
        }

        // Кнопка "Удалить"
        btnDelete.setOnClickListener {
            book?.let {
                deleteCoverIfLocal(it.coverUrl)
                viewModel.deleteBook(it)
            }
            dialog.dismiss()
        }

        // Кнопка "Отмена"
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        // Показать диалог
        currentAddBookDialog = dialog
        dialog.show()

        // Очистка при закрытии
        dialog.setOnDismissListener {
            currentAddBookDialog = null
            currentBookCoverPath = null
        }
    }

    //Сканирование
    private fun startIsbnScanner() {
        val options = ScanOptions().apply {
            setDesiredBarcodeFormats("EAN_13", "EAN_8")
            setPrompt(getString(R.string.scan_prompt))
            setBeepEnabled(true)
            setOrientationLocked(true)
        }
        isbnScanLauncher.launch(options)
    }

    //Поиск по ISBN
    private fun fetchBookByIsbn(isbn: String) {
        val cleanIsbn = isbn.replace("-", "").replace(" ", "").trim()
        if (cleanIsbn.isEmpty()) return

        lifecycleScope.launch {
            try {
                val response = apiService.getBookGoogle("isbn:$cleanIsbn")
                val bookInfo = response.items?.firstOrNull()?.volumeInfo

                if (bookInfo != null && currentAddBookDialog != null) {
                    val dialog = currentAddBookDialog!!
                    val root = dialog.findViewById<LinearLayout>(R.id.dialog_add_book_root)
                        ?: return@launch

                    root.findViewById<TextInputEditText>(R.id.et_title)?.setText(bookInfo.title ?: "")
                    root.findViewById<TextInputEditText>(R.id.et_author)?.setText(bookInfo.authors?.joinToString(", ") ?: "")
                    root.findViewById<TextInputEditText>(R.id.et_publisher)?.setText(bookInfo.publisher ?: "")
                    root.findViewById<TextInputEditText>(R.id.et_pages)?.setText(bookInfo.pageCount?.toString() ?: "")
                    root.findViewById<TextInputEditText>(R.id.et_description)?.setText(bookInfo.description ?: "")

                    val thumb = bookInfo.imageLinks?.thumbnail?.replace("http://", "https://")
                    if (thumb != null) {
                        currentBookCoverPath = thumb
                        updateCoverImageInDialog(thumb)
                    }
                } else if (currentAddBookDialog == null) {
                    Toast.makeText(this@MainActivity, getString(R.string.dialog_closed), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MainActivity, getString(R.string.book_not_found), Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, getString(R.string.network_error) + "${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    //Копирование во внутренее хранилище
    private fun copyImageToInternalStorage(uri: Uri): String? {
        return try {
            val fileName = "cover_${System.currentTimeMillis()}.jpg"
            val coversDir = File(filesDir, "covers")
            coversDir.mkdirs()
            val destinationFile = File(coversDir, fileName)

            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destinationFile).use { output ->
                    input.copyTo(output)
                }
            }
            destinationFile.absolutePath
        } catch (e: Exception) {
            Log.e("BookApp", "Ошибка копирования обложки", e)
            null
        }
    }

    //Удаление локальной обложки
    private fun deleteCoverIfLocal(coverPath: String?) {
        coverPath?.let { path ->
            if (path.startsWith(filesDir.absolutePath)) {
                File(path).delete()
            }
        }
    }

    //Вспомогательные функции
    private fun updateCoverImageInDialog(coverPath: String) {
        currentAddBookDialog?.let { dialog ->
            val root = dialog.findViewById<LinearLayout>(R.id.dialog_add_book_root) ?: return@let
            val imgCover = root.findViewById<ImageView>(R.id.img_cover)
            loadCoverIntoImageView(imgCover, coverPath)
        }
    }

    private fun loadCoverIntoImageView(imageView: ImageView?, coverPath: String?) {
        imageView ?: return
        if (coverPath.isNullOrBlank()) {
            imageView.setImageResource(R.drawable.ic_book_placeholder)
            return
        }

        if (coverPath.startsWith("http")) {
            Glide.with(this).load(coverPath)
                .placeholder(R.drawable.ic_book_placeholder)
                .into(imageView)
        } else {
            val file = File(coverPath)
            if (file.exists()) {
                Glide.with(this).load(file)
                    .placeholder(R.drawable.ic_book_placeholder)
                    .into(imageView)
            } else {
                imageView.setImageResource(R.drawable.ic_book_placeholder)
            }
        }
    }

    //"Читаю сейчас"
    private val todayMillis: Long
        get() = System.currentTimeMillis()
    fun showStartReadingDialog(book: Book) {
        Toast.makeText(this, getString(R.string.select_start_date), Toast.LENGTH_LONG).show()
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(this, { _, y, m, d ->
            val selectedCalendar = Calendar.getInstance().apply {
                set(y, m, d)
            }
            val selectedMillis = selectedCalendar.timeInMillis

            if (selectedMillis > todayMillis) {
                return@DatePickerDialog
            }

            val updated = book.copy(
                isCurrentlyReading = true,
                startDate = selectedMillis
            )
            viewModel.updateBook(updated)
        }, year, month, day)
        datePicker.setTitle(getString(R.string.start_date_title))
        // Запрещаем выбор будущих дат
        datePicker.datePicker.maxDate = todayMillis
        datePicker.show()
    }
    fun showMarkAsReadDialog(book: Book) {
        showReviewEditDialog(book, showDatePicker = true)
    }
    private fun showReviewEditDialog(
        book: Book,
        showDatePicker: Boolean = true,
        onSave: (Book) -> Unit = { viewModel.updateBook(it) }
    ) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_mark_read, null)
        val dialog = AlertDialog.Builder(this, R.style.CustomAlertDialog)
            .setView(view)
            .setCancelable(true)
            .create()

        val customSliderLayout = view.findViewById<FrameLayout>(R.id.custom_rating_slider)
        val slider = customSliderLayout.findViewById<com.google.android.material.slider.Slider>(R.id.slider)
        val starsView = customSliderLayout.findViewById<RatingStarsView>(R.id.stars_view)
        val etPagesRead = view.findViewById<EditText>(R.id.et_pages_read)
        val cbAllPages = view.findViewById<CheckBox>(R.id.cb_all_pages)
        val etReview = view.findViewById<TextInputEditText>(R.id.et_review)
        val tvRatingValue = view.findViewById<TextView>(R.id.tv_rating_value)
        val btnClose = view.findViewById<Button>(R.id.btn_close)
        val btnMark = view.findViewById<Button>(R.id.btn_mark)

        slider.value = book.rating ?: 5.0f
        tvRatingValue.text = String.format("%.1f", slider.value)
        starsView.setRating(slider.value)

        slider.addOnChangeListener { _, value, _ ->
            starsView.setRating(value)
            tvRatingValue.text = String.format("%.1f", value)
        }

        etPagesRead.setText(book.pagesRead?.toString() ?: "")

        // Автозаполнение: если прочитано всё — чекбокс включён
        val isAllRead = book.pageCount?.let { book.pagesRead == it } == true
        cbAllPages.isChecked = isAllRead
        etPagesRead.isEnabled = !isAllRead

        cbAllPages.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && book.pageCount != null) {
                etPagesRead.setText(book.pageCount.toString())
                etPagesRead.isEnabled = false
            } else {
                etPagesRead.setText(book.pagesRead?.toString() ?: "")
                etPagesRead.isEnabled = true
                etPagesRead.requestFocus()
            }
        }

        etReview.setText(book.review ?: "")

        btnClose.setOnClickListener { dialog.dismiss() }

        btnMark.setOnClickListener {
            val rating = slider.value.takeIf { it > 0 }?.toFloat()
            val pagesReadStr = etPagesRead.text.toString().trim()
            val pagesRead = if (pagesReadStr.isBlank()) null else pagesReadStr.toIntOrNull()

            if (pagesRead == null && !cbAllPages.isChecked) {
                Toast.makeText(this, getString(R.string.pages_required), Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val finalPagesRead = if (cbAllPages.isChecked && book.pageCount != null) {
                book.pageCount
            } else {
                pagesRead
            }

            if (finalPagesRead != null && book.pageCount != null && finalPagesRead > book.pageCount) {
                Toast.makeText(this, getString(R.string.pages_exceed_total) + "${book.pageCount}", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (showDatePicker && !book.isRead) {
                // === Отмечаем как прочитанную ===
                val calendar = Calendar.getInstance()
                val datePicker = DatePickerDialog(this, { _, y, m, d ->
                    val endMillis = Calendar.getInstance().apply { set(y, m, d) }.timeInMillis

                    if (endMillis > System.currentTimeMillis()) {
                        Toast.makeText(this, getString(R.string.date_in_future), Toast.LENGTH_SHORT).show()
                        return@DatePickerDialog
                    }
                    if (book.startDate != null && endMillis < book.startDate) {
                        Toast.makeText(this, getString(R.string.end_before_start), Toast.LENGTH_LONG).show()
                        return@DatePickerDialog
                    }

                    val updatedBook = book.copy(
                        isRead = true,
                        isCurrentlyReading = false,
                        rating = rating,
                        pagesRead = finalPagesRead,
                        review = etReview.text.toString().trim().takeIf { it.isNotBlank() },
                        endDate = endMillis
                    )
                    onSave(updatedBook)
                    dialog.dismiss()
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

                datePicker.setTitle(getString(R.string.end_date_title))
                datePicker.datePicker.maxDate = System.currentTimeMillis()
                datePicker.datePicker.minDate = book.startDate ?: 0
                datePicker.show()
            } else {
                //Редактируем без даты
                val updatedBook = book.copy(
                    rating = rating,
                    pagesRead = finalPagesRead,
                    review = etReview.text.toString().trim().takeIf { it.isNotBlank() }
                )
                onSave(updatedBook)
                dialog.dismiss()
            }
        }

        dialog.show()
    }
    //Просмотр отзыва
    @Suppress("DEPRECATION")
    @SuppressLint("SetTextI18n", "DefaultLocale")
    fun showReviewDialog(book: Book) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_review, null)
        val dialog = AlertDialog.Builder(this, R.style.CustomAlertDialog)
            .setView(view)
            .setCancelable(true)
            .create()

        val imgCover = view.findViewById<ImageView>(R.id.img_cover)
        val tvTitle = view.findViewById<TextView>(R.id.tv_title)
        val tvAuthor = view.findViewById<TextView>(R.id.tv_author)
        val tvRating = view.findViewById<TextView>(R.id.tv_rating)
        val tvReadingSummary = view.findViewById<TextView>(R.id.tv_reading_summary)
        val tvDateRange = view.findViewById<TextView>(R.id.tv_date_range)
        val tvReview = view.findViewById<TextView>(R.id.tv_review)
        val btnClose = view.findViewById<Button>(R.id.btn_close)
        val btnEditReview = view.findViewById<ImageView>(R.id.btn_edit_review)

        loadCoverIntoImageView(imgCover, book.coverUrl)

        tvTitle.text = book.title
        tvAuthor.text = getString(R.string.Author) + ": ${book.author}"
        tvRating.text = getString(R.string.rating) + ": ${book.rating?.let { String.format("%.1f", it) } ?: "—"}"

        if (book.pagesRead != null && book.startDate != null && book.endDate != null) {
            val pages = book.pagesRead
            val days = (book.endDate - book.startDate) / (1000 * 60 * 60 * 24) + 1

            val pagesText = pagesDeclension(pages)
            val readText = readDeclension(pages)
            val daysText = daysDeclension(days)

            tvReadingSummary.text = "$pages $pagesText $readText за $days $daysText"

            val sdf = java.text.SimpleDateFormat("d MMM yyyy", java.util.Locale("ru"))
            tvDateRange.text = "${sdf.format(java.util.Date(book.startDate))} — ${sdf.format(java.util.Date(book.endDate))}"
        } else {
            tvReadingSummary.text = getString(R.string.no_reading_info)
            tvDateRange.text = ""
        }

        tvReview.text = book.review?.takeIf { it.isNotBlank() } ?: getString(R.string.no_review)

        btnEditReview.setOnClickListener {
            dialog.dismiss()
            showReviewEditDialog(
                book = book,
                onSave = { updatedBook ->
                    viewModel.updateBook(updatedBook)
                    showReviewDialog(updatedBook)
                },
                showDatePicker = false
            )
        }

        btnClose.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    // Склонение: 1 страница, 2 страницы, 5 страниц
    private fun pagesDeclension(count: Int): String {
        return when {
            count % 10 == 1 && count % 100 != 11 -> "страница"
            count % 10 in 2..4 && count % 100 !in 12..14 -> "страницы"
            else -> "страниц"
        }
    }

    // Склонение: прочитана / прочитаны
    private fun readDeclension(count: Int): String {
        return if (count % 10 == 1 && count % 100 != 11) "прочитана" else "прочитаны"
    }

    // Склонение: 1 день, 2 дня, 5 дней
    private fun daysDeclension(days: Long): String {
        return when {
            (days % 10).toInt() == 1 && (days % 100).toInt() != 11 -> "день"
            days % 10 in 2..4 && days % 100 !in 12..14 -> "дня"
            else -> "дней"
        }
    }
    private var currentPopup: PopupWindow? = null
    @SuppressLint("UseKtx")
    fun showContextMenuPopup(book: Book, anchorView: View) {
        currentPopup?.dismiss()

        val popupView = LayoutInflater.from(this).inflate(R.layout.dialog_context_menu, null)
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            elevation = 16f
            isOutsideTouchable = true
            animationStyle = android.R.style.Animation_Dialog
        }

        val action1 = popupView.findViewById<TextView>(R.id.menu_action_1)
        val action2 = popupView.findViewById<TextView>(R.id.menu_action_2)
        val cancel = popupView.findViewById<TextView>(R.id.menu_cancel)

        // Динамика текста
        when {
            book.isRead -> {
                action1.text = getString(R.string.return_to_want_to_read)
                action2.text = getString(R.string.return_to_currently_reading)
            }
            book.isCurrentlyReading -> {
                action1.text = getString(R.string.mark_as_read)
                action2.text = getString(R.string.return_to_want_to_read)
            }
            else -> {
                action1.text = getString(R.string.start_read)
                action2.text = getString(R.string.editBook)
            }
        }

        // === ДЕЙСТВИЯ ===
        action1.setOnClickListener {
            when {
                book.isRead -> {
                    // Вернуть в "Хочу прочитать"
                    viewModel.updateBook(book.copy(isRead = false, isCurrentlyReading = false))
                }
                book.isCurrentlyReading -> {
                    // Отметить прочитанной
                    showMarkAsReadDialog(book)
                }
                else -> {
                    // Начать читать
                    showStartReadingDialog(book)
                }
            }
            popupWindow.dismiss()
        }

        action2.setOnClickListener {
            when {
                book.isRead -> {
                    // Вернуть в "Читаю сейчас"
                    viewModel.updateBook(book.copy(isRead = false, isCurrentlyReading = true))
                }
                book.isCurrentlyReading -> {
                    // Вернуть в "Хочу прочитать"
                    viewModel.updateBook(book.copy(isCurrentlyReading = false))
                }
                else -> {
                    // Редактировать (только для "Хочу прочитать")
                    showAddBookDialog(book)
                }
            }
            popupWindow.dismiss()
        }

        cancel.setOnClickListener { popupWindow.dismiss() }

        // Показать по центру
        popupWindow.showAtLocation(anchorView, Gravity.CENTER, 0, 0)
        popupWindow.animationStyle = R.style.PopupAnimation
        currentPopup = popupWindow
    }
}
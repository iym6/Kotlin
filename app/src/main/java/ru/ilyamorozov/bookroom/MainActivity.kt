package ru.ilyamorozov.bookroom

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
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

class MainActivity : AppCompatActivity() {

    //  ViewModel, диалог-обложка и view-диалога
    private lateinit var viewModel: BookViewModel
    private var currentBookCoverUri: String? = null          // URL/URI обложки
    private var currentDialogView: View? = null              // view открытого диалога

    //  ActivityResult-лаунчеры
    private val isbnScanLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            fetchBookByIsbn(result.contents)
        } else {
            Toast.makeText(this, "Сканирование отменено", Toast.LENGTH_SHORT).show()
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) startIsbnScanner()
        else Toast.makeText(this, "Камера обязательна", Toast.LENGTH_LONG).show()
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            currentBookCoverUri = it.toString()
            currentDialogView?.findViewById<ImageView>(R.id.img_cover)?.let { img ->
                Glide.with(this).load(it).placeholder(R.drawable.ic_book_placeholder).into(img)
            }
        }
    }

    //  Retrofit-сервис (Google Books)
    private val apiService = ApiService.create()

    //  onCreate – вкладки + FAB
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
                0 -> getString(R.string.New)   // "Хочу прочитать"
                1 -> getString(R.string.Read)  // "Прочитанные"
                else -> null
            }
        }.attach()

        btnAddBook.setOnClickListener { showAddBookDialog() }
    }

    //  Диалог добавления / редактирования книги
    fun showAddBookDialog(book: Book? = null) {
        val builder = AlertDialog.Builder(this)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_book, null)
        currentDialogView = view                     // <-- сохраняем view
        builder.setView(view)

        // ------------------- Views -------------------
        val imgCover = view.findViewById<ImageView>(R.id.img_cover)
        val etAuthor = view.findViewById<TextInputEditText>(R.id.et_author)
        val etTitle = view.findViewById<TextInputEditText>(R.id.et_title)
        val etPublisher = view.findViewById<TextInputEditText>(R.id.et_publisher)
        val etPages = view.findViewById<TextInputEditText>(R.id.et_pages)
        val etDescription = view.findViewById<TextInputEditText>(R.id.et_description)
        val btnScanIsbn = view.findViewById<Button>(R.id.btn_scan_isbn)

        // ------------------- Заполнение при редактировании
        currentBookCoverUri = book?.coverUrl
        book?.let {
            etAuthor.setText(it.author)
            etTitle.setText(it.title)
            etPublisher.setText(it.publisher)
            etPages.setText(it.pageCount?.toString() ?: "")
            etDescription.setText(it.description)
            if (!it.coverUrl.isNullOrBlank()) {
                Glide.with(this).load(it.coverUrl)
                    .placeholder(R.drawable.ic_book_placeholder)
                    .into(imgCover)
            }
        }

        // ------------------- Клик по обложке -------------------
        imgCover.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // ------------------- Сканирование ISBN -------------------
        btnScanIsbn.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                startIsbnScanner()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }

        // ------------------- Кнопки диалога -------------------
        builder.setTitle(if (book == null) getString(R.string.addBook) else getString(R.string.editBook))

        builder.setPositiveButton(getString(R.string.save)) { _, _ ->
            val author = etAuthor.text.toString().trim()
            val title = etTitle.text.toString().trim()
            if (author.isBlank() || title.isBlank()) {
                Toast.makeText(this, "Автор и Название обязательны", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            val newBook = book?.copy(
                author = author,
                title = title,
                publisher = etPublisher.text.toString().trim().takeIf { it.isNotBlank() },
                pageCount = etPages.text.toString().toIntOrNull(),
                description = etDescription.text.toString().trim().takeIf { it.isNotBlank() },
                coverUrl = currentBookCoverUri
            ) ?: Book(
                author = author,
                title = title,
                publisher = etPublisher.text.toString().trim().takeIf { it.isNotBlank() },
                pageCount = etPages.text.toString().toIntOrNull(),
                description = etDescription.text.toString().trim().takeIf { it.isNotBlank() },
                coverUrl = currentBookCoverUri
            )

            if (book == null) viewModel.addBook(newBook) else viewModel.updateBook(newBook)
        }

        if (book != null) {
            builder.setNegativeButton(getString(R.string.delete)) { _, _ ->
                viewModel.deleteBook(book)
            }
        } else {
            builder.setNegativeButton(getString(R.string.cancel), null)
        }

        // ------------------- Очистка при закрытии -------------------
        val dialog = builder.show()
        dialog.setOnDismissListener {
            currentDialogView = null
            currentBookCoverUri = null
        }
    }


    //  Сканер ISBN

    private fun startIsbnScanner() {
        val options = ScanOptions().apply {
            setDesiredBarcodeFormats("EAN_13", "EAN_8")
            setPrompt("Наведите камеру на ISBN")
            setBeepEnabled(true)
            setOrientationLocked(true)
        }
        isbnScanLauncher.launch(options)
    }


    //  Запрос книги по ISBN (Google Books)

    private fun fetchBookByIsbn(isbn: String) {
        val cleanIsbn = isbn.replace("-", "").replace(" ", "").trim()
        if (cleanIsbn.isEmpty()) return

        lifecycleScope.launch {
            try {
                val response = apiService.getBookGoogle("isbn:$cleanIsbn")
                val bookInfo = response.items?.firstOrNull()?.volumeInfo

                if (bookInfo != null) {
                    currentDialogView?.let { v ->
                        v.findViewById<TextInputEditText>(R.id.et_title)?.setText(bookInfo.title ?: "")
                        v.findViewById<TextInputEditText>(R.id.et_author)
                            ?.setText(bookInfo.authors?.joinToString(", ") ?: "")
                        v.findViewById<TextInputEditText>(R.id.et_publisher)
                            ?.setText(bookInfo.publisher ?: "")
                        v.findViewById<TextInputEditText>(R.id.et_pages)
                            ?.setText(bookInfo.pageCount?.toString() ?: "")
                        v.findViewById<TextInputEditText>(R.id.et_description)
                            ?.setText(bookInfo.description ?: "")

                        val thumb = bookInfo.imageLinks?.thumbnail?.replace("http://", "https://")
                        if (thumb != null) {
                            currentBookCoverUri = thumb
                            Glide.with(this@MainActivity)
                                .load(thumb)
                                .placeholder(R.drawable.ic_book_placeholder)
                                .into(v.findViewById(R.id.img_cover))
                        }
                    } ?: Toast.makeText(this@MainActivity, "Диалог не найден", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this@MainActivity, "Книга не найдена", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    fun showMarkAsReadDialog(book: Book) {
        val builder = AlertDialog.Builder(this)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_mark_read, null)
        builder.setView(view)

        val sliderRating = view.findViewById<com.google.android.material.slider.Slider>(R.id.slider_rating)
        val tvRatingValue = view.findViewById<TextView>(R.id.tv_rating_value)
        val etPagesRead = view.findViewById<EditText>(R.id.et_pages_read)
        val cbAllPages = view.findViewById<CheckBox>(R.id.cb_all_pages)
        val etReview = view.findViewById<TextInputEditText>(R.id.et_review)
        android.util.Log.d("BookApp", "Открыт диалог для книги: ${book.title}")
        // Инициализация
        sliderRating.value = book.rating?.toFloat() ?: 5.0f
        tvRatingValue.text = String.format("%.1f", sliderRating.value)

        // Обновление текста при движении слайдера
        sliderRating.addOnChangeListener { _, value, _ ->
            tvRatingValue.text = String.format("%.1f", value)
        }

        // --- Страницы: ПУСТО, чекбокс ВЫКЛЮЧЕН ---
        etPagesRead.setText("")  // ← Всегда пусто при открытии
        cbAllPages.isChecked = false  // ← Выключен

        // --- Логика чекбокса ---
        val totalPages = book.pageCount
        cbAllPages.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && totalPages != null) {
                etPagesRead.setText(totalPages.toString())
                etPagesRead.isEnabled = false  // Блокируем редактирование
            } else {
                etPagesRead.setText("")  // Очищаем
                etPagesRead.isEnabled = true
                etPagesRead.requestFocus()
            }
        }

        // Отзыв
        etReview.setText(book.review ?: "")

        builder.setTitle(getString(R.string.mark_as_read))

        builder.setPositiveButton("Отметить") { _, _ ->
            val rating = sliderRating.value.toDouble().takeIf { it > 0 }?.toFloat()
            val pagesReadStr = etPagesRead.text.toString().trim()
            val pagesRead = if (pagesReadStr.isBlank()) null else pagesReadStr.toIntOrNull()
            if (pagesRead != null && totalPages != null && pagesRead > totalPages) {
                Toast.makeText(
                    this,
                    "В книге всего $totalPages страниц",
                    Toast.LENGTH_LONG
                ).show()
                return@setPositiveButton  // Не сохраняем, не закрываем
            }
            val updatedBook = book.copy(
                isRead = true,
                rating = rating,
                pagesRead = pagesRead,
                review = etReview.text.toString().trim().takeIf { it.isNotBlank() }
            )

            viewModel.updateBook(updatedBook)
        }

        builder.setNegativeButton("Отмена", null)
        builder.show()
    }

    fun showReviewDialog(book: Book) {
        val builder = AlertDialog.Builder(this)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_review, null)
        builder.setView(view)

        val imgCover = view.findViewById<ImageView>(R.id.img_cover)
        val tvTitle = view.findViewById<TextView>(R.id.tv_title)
        val tvAuthor = view.findViewById<TextView>(R.id.tv_author)
        val tvRating = view.findViewById<TextView>(R.id.tv_rating)
        val tvPages = view.findViewById<TextView>(R.id.tv_pages)
        val tvReview = view.findViewById<TextView>(R.id.tv_review)

        // Заполнение
        if (!book.coverUrl.isNullOrBlank()) {
            Glide.with(this).load(book.coverUrl).placeholder(R.drawable.ic_book_placeholder).into(imgCover)
        } else {
            imgCover.setImageResource(R.drawable.ic_book_placeholder)
        }

        tvTitle.text = book.title
        tvAuthor.text = "Автор: ${book.author}"
        tvRating.text = "Оценка: ${book.rating?.let { String.format("%.1f", it) } ?: "—"}"
        tvPages.text = "Прочитано: ${book.pagesRead ?: book.pageCount ?: "—"} страниц"

        tvReview.text = book.review?.takeIf { it.isNotBlank() } ?: "Отзыв отсутствует"

        builder.setTitle("Отзыв о книге")
        builder.setPositiveButton("Закрыть", null)
        builder.show()
    }
}
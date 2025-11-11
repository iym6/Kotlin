package ru.ilyamorozov.bookroom

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
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
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    // ViewModel
    private lateinit var viewModel: BookViewModel

    // Диалог добавления/редактирования
    private var currentAddBookDialog: AlertDialog? = null
    private var currentBookCoverPath: String? = null  // Путь к файлу или URL

    // ActivityResult-лаунчеры
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
            val internalPath = copyImageToInternalStorage(it)
            if (internalPath != null) {
                currentBookCoverPath = internalPath
                updateCoverImageInDialog(internalPath)
            } else {
                Toast.makeText(this, "Не удалось сохранить обложку", Toast.LENGTH_SHORT).show()
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
                1 -> getString(R.string.Read)
                else -> null
            }
        }.attach()

        btnAddBook.setOnClickListener { showAddBookDialog() }
    }

    // === ДИАЛОГ ДОБАВЛЕНИЯ / РЕДАКТИРОВАНИЯ ===
    fun showAddBookDialog(book: Book? = null) {
        val builder = AlertDialog.Builder(this)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_book, null)
        builder.setView(view)

        // --- Views ---
        val imgCover = view.findViewById<ImageView>(R.id.img_cover)
        val etAuthor = view.findViewById<TextInputEditText>(R.id.et_author)
        val etTitle = view.findViewById<TextInputEditText>(R.id.et_title)
        val etPublisher = view.findViewById<TextInputEditText>(R.id.et_publisher)
        val etPages = view.findViewById<TextInputEditText>(R.id.et_pages)
        val etDescription = view.findViewById<TextInputEditText>(R.id.et_description)
        val btnScanIsbn = view.findViewById<Button>(R.id.btn_scan_isbn)

        // --- Заполнение при редактировании ---
        currentBookCoverPath = book?.coverUrl
        book?.let {
            etAuthor.setText(it.author)
            etTitle.setText(it.title)
            etPublisher.setText(it.publisher)
            etPages.setText(it.pageCount?.toString() ?: "")
            etDescription.setText(it.description)
            loadCoverIntoImageView(imgCover, it.coverUrl)
        }

        // --- Клик по обложке ---
        imgCover.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // --- Сканирование ISBN ---
        btnScanIsbn.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
            ) {
                startIsbnScanner()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }

        // --- Кнопки ---
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
                coverUrl = currentBookCoverPath
            ) ?: Book(
                author = author,
                title = title,
                publisher = etPublisher.text.toString().trim().takeIf { it.isNotBlank() },
                pageCount = etPages.text.toString().toIntOrNull(),
                description = etDescription.text.toString().trim().takeIf { it.isNotBlank() },
                coverUrl = currentBookCoverPath
            )

            if (book == null) viewModel.addBook(newBook) else viewModel.updateBook(newBook)
        }

        if (book != null) {
            builder.setNegativeButton(getString(R.string.delete)) { _, _ ->
                deleteCoverIfLocal(book.coverUrl)  // ← Удаляем файл
                viewModel.deleteBook(book)
            }
        } else {
            builder.setNegativeButton(getString(R.string.cancel), null)
        }

        // --- Показ и очистка ---
        currentAddBookDialog = builder.create()
        currentAddBookDialog?.show()

        currentAddBookDialog?.setOnDismissListener {
            currentAddBookDialog = null
            currentBookCoverPath = null
        }
    }

    // === СКАНИРОВАНИЕ ===
    private fun startIsbnScanner() {
        val options = ScanOptions().apply {
            setDesiredBarcodeFormats("EAN_13", "EAN_8")
            setPrompt("Наведите камеру на ISBN")
            setBeepEnabled(true)
            setOrientationLocked(true)
        }
        isbnScanLauncher.launch(options)
    }

    // === ПОИСК ПО ISBN ===
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
                    Toast.makeText(this@MainActivity, "Диалог закрыт", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MainActivity, "Книга не найдена", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Ошибка сети: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // === КОПИРОВАНИЕ ОБЛОЖКИ ВО ВНУТРЕННЕЕ ХРАНИЛИЩЕ ===
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

    // === УДАЛЕНИЕ ЛОКАЛЬНОЙ ОБЛОЖКИ ===
    private fun deleteCoverIfLocal(coverPath: String?) {
        coverPath?.let { path ->
            if (path.startsWith(filesDir.absolutePath)) {
                File(path).delete()
            }
        }
    }

    // === ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ДЛЯ ОБЛОЖКИ ===
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

    // === ОТМЕТКА "ПРОЧИТАНО" ===
    fun showMarkAsReadDialog(book: Book) {
        val builder = AlertDialog.Builder(this)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_mark_read, null)
        builder.setView(view)

        val sliderRating = view.findViewById<com.google.android.material.slider.Slider>(R.id.slider_rating)
        val tvRatingValue = view.findViewById<TextView>(R.id.tv_rating_value)
        val etPagesRead = view.findViewById<EditText>(R.id.et_pages_read)
        val cbAllPages = view.findViewById<CheckBox>(R.id.cb_all_pages)
        val etReview = view.findViewById<TextInputEditText>(R.id.et_review)

        // Инициализация
        sliderRating.value = book.rating ?: 5.0f
        tvRatingValue.text = String.format("%.1f", sliderRating.value)
        sliderRating.addOnChangeListener { _, value, _ ->
            tvRatingValue.text = String.format("%.1f", value)
        }

        etPagesRead.setText("")
        cbAllPages.isChecked = false

        val totalPages = book.pageCount
        cbAllPages.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && totalPages != null) {
                etPagesRead.setText(totalPages.toString())
                etPagesRead.isEnabled = false
            } else {
                etPagesRead.setText("")
                etPagesRead.isEnabled = true
                etPagesRead.requestFocus()
            }
        }

        etReview.setText(book.review ?: "")

        builder.setTitle(getString(R.string.mark_as_read))
        builder.setPositiveButton("Отметить") { _, _ ->
            val rating = sliderRating.value.takeIf { it > 0 }?.toFloat()
            val pagesReadStr = etPagesRead.text.toString().trim()
            val pagesRead = if (pagesReadStr.isBlank()) null else pagesReadStr.toIntOrNull()

            if (pagesRead != null && totalPages != null && pagesRead > totalPages) {
                Toast.makeText(this, "В книге всего $totalPages страниц", Toast.LENGTH_LONG).show()
                return@setPositiveButton
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

    // === ПРОСМОТР ОТЗЫВА ===
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

        loadCoverIntoImageView(imgCover, book.coverUrl)

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
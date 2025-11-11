package ru.ilyamorozov.bookroom

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.io.File

class BookAdapter(
    private val onStatusClick: (Book) -> Unit,
    private val onEditClick: (Book) -> Unit,
    private val onItemClick: (Book) -> Unit
) : ListAdapter<Book, BookAdapter.BookViewHolder>(BookDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_book, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgCover: ImageView = itemView.findViewById(R.id.img_cover)
        private val title: TextView = itemView.findViewById(R.id.tv_title)
        private val author: TextView = itemView.findViewById(R.id.tv_author)
        private val layoutRating: View = itemView.findViewById(R.id.layout_rating)
        private val tvRating: TextView = itemView.findViewById(R.id.tv_rating)
        private val editIcon: ImageView = itemView.findViewById(R.id.edit_icon)
        private fun loadCoverSmart(imageView: ImageView, coverPath: String) {
            //это файл или URL?
            if (coverPath.startsWith("http://") || coverPath.startsWith("https://")) {
                // Это URL от Google Books
                Glide.with(imageView.context)
                    .load(coverPath)
                    .placeholder(R.drawable.ic_book_placeholder)
                    .into(imageView)
            } else {
                // Это локальный файл
                val file = File(coverPath)
                if (file.exists()) {
                    Glide.with(imageView.context)
                        .load(file)
                        .placeholder(R.drawable.ic_book_placeholder)
                        .into(imageView)
                } else {
                    // Файл удалён - placeholder
                    imageView.setImageResource(R.drawable.ic_book_placeholder)
                }
            }
        }
        @SuppressLint("CheckResult", "DefaultLocale")
        fun bind(book: Book) {
            title.text = book.title
            author.text = book.author

            //Обложка
            if (!book.coverUrl.isNullOrBlank()) {
                loadCoverSmart(imgCover, book.coverUrl!!)
            } else {
                imgCover.setImageResource(R.drawable.ic_book_placeholder)
            }

            //Клик по обложке → отметить прочитанной
            imgCover.setOnClickListener {
                onStatusClick(book)
            }

            //Клик по всей строке - просмотр отзыва-
            itemView.setOnClickListener {
                if (book.isRead) {
                    onItemClick(book)
                } else {
                    onStatusClick(book)
                }
            }

            //Рейтинг
            if (book.isRead && book.rating != null) {
                layoutRating.visibility = View.VISIBLE
                tvRating.text = String.format("%.1f", book.rating)
            } else {
                layoutRating.visibility = View.GONE
            }

            //Редактирование
            editIcon.setOnClickListener {
                onEditClick(book)
            }
        }
    }
}

class BookDiffCallback : DiffUtil.ItemCallback<Book>() {
    override fun areItemsTheSame(oldItem: Book, newItem: Book): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Book, newItem: Book): Boolean =
        oldItem == newItem
}
package ru.ilyamorozov.bookroom

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class BookAdapter(
    private val onStatusClick: (Book) -> Unit,
    private val onEditClick: (Book) -> Unit
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

        fun bind(book: Book) {
            title.text = book.title
            author.text = book.author

            // Обложка
            if (!book.coverUrl.isNullOrBlank()) {
                Glide.with(itemView.context)
                    .load(book.coverUrl)
                    .placeholder(R.drawable.ic_book_placeholder)
                    .into(imgCover)
            } else {
                imgCover.setImageResource(R.drawable.ic_book_placeholder)
            }

            // Клик по обложке — загрузка изображения (позже)
            imgCover.setOnClickListener {
                // TODO: Открыть выбор изображения
            }

            // Рейтинг — только для прочитанных
            if (book.isRead && book.rating != null) {
                layoutRating.visibility = View.VISIBLE
                tvRating.text = String.format("%.1f", book.rating)
            } else {
                layoutRating.visibility = View.GONE
            }

            editIcon.setOnClickListener { onEditClick(book) }
        }
    }
}

class BookDiffCallback : DiffUtil.ItemCallback<Book>() {
    override fun areItemsTheSame(oldItem: Book, newItem: Book): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Book, newItem: Book): Boolean =
        oldItem == newItem
}
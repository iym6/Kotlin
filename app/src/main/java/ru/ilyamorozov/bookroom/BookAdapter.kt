package ru.ilyamorozov.bookroom

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BookAdapter(
    private val onStatusClick: (Book) -> Unit,
    private val onEditClick: (Book) -> Unit
) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    private var books: List<Book> = emptyList()

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newBooks: List<Book>) {
        books = newBooks
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_book, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(books[position])
    }

    override fun getItemCount() = books.size

    inner class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val icon: ImageView = itemView.findViewById(R.id.book_icon)
        private val title: TextView = itemView.findViewById(R.id.tv_title)
        private val author: TextView = itemView.findViewById(R.id.tv_author)
        private val editIcon: ImageView = itemView.findViewById(R.id.edit_icon)

        fun bind(book: Book) {
            title.text = book.title
            author.text = book.author
            icon.setImageResource(if (book.status == 0) R.drawable.ic_book_gray else R.drawable.ic_book_green)

            icon.setOnClickListener { onStatusClick(book) }
            editIcon.setOnClickListener { onEditClick(book) }
        }
    }
}
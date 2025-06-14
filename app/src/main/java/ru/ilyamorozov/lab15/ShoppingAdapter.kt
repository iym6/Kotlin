package ru.ilyamorozov.lab15

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ShoppingAdapter(
    private var items: MutableList<ShoppingItem>,
    private val onItemClick: (ShoppingItem) -> Unit,
    private val onItemSwipe: (Int) -> Unit
) : RecyclerView.Adapter<ShoppingAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvQuantity: TextView = itemView.findViewById(R.id.tvQuantity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_shopping, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvName.text = item.name
        holder.tvQuantity.text = "Количество: ${item.quantity}"
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = items.size

    fun updateItems(newItems: List<ShoppingItem>) {
        items = newItems.toMutableList()
        notifyDataSetChanged()
    }
}
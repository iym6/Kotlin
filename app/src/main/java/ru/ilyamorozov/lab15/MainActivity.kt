package ru.ilyamorozov.lab15

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity(), ShoppingDialog.ShoppingDialogListener {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ShoppingAdapter
    private val shoppingItems = mutableListOf<ShoppingItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        adapter = ShoppingAdapter(shoppingItems, ::onItemClick, ::onItemSwipe)
        recyclerView.adapter = adapter

        val swipeHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                onItemSwipe(viewHolder.adapterPosition)
            }
        })
        swipeHelper.attachToRecyclerView(recyclerView)

        findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener {
            ShoppingDialog.newInstance().show(supportFragmentManager, "ShoppingDialog")
        }
    }

    private fun onItemClick(item: ShoppingItem) {
        ShoppingDialog.newInstance(item).show(supportFragmentManager, "ShoppingDialog")
    }

    private fun onItemSwipe(position: Int) {
        shoppingItems.removeAt(position)
        adapter.notifyItemRemoved(position)
    }

    override fun onItemAdded(item: ShoppingItem) {
        shoppingItems.add(item)
        adapter.notifyItemInserted(shoppingItems.size - 1)
    }

    override fun onItemUpdated(item: ShoppingItem) {
        val index = shoppingItems.indexOfFirst { it.id == item.id }
        if (index != -1) {
            shoppingItems[index] = item
            adapter.notifyItemChanged(index)
        }
    }
}
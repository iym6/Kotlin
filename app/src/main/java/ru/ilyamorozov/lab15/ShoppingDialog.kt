package ru.ilyamorozov.lab15

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import androidx.fragment.app.DialogFragment

class ShoppingDialog : DialogFragment() {
    interface ShoppingDialogListener {
        fun onItemAdded(item: ShoppingItem)
        fun onItemUpdated(item: ShoppingItem)
    }

    private var listener: ShoppingDialogListener? = null
    private var existingItem: ShoppingItem? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? ShoppingDialogListener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.dialog_shopping, null)

        val etName = view.findViewById<EditText>(R.id.etName)
        val etQuantity = view.findViewById<EditText>(R.id.etQuantity)

        existingItem?.let {
            etName.setText(it.name)
            etQuantity.setText(it.quantity)
        }

        builder.setView(view)
            .setTitle(if (existingItem == null) "Добавить товар" else "Редактировать товар")
            .setPositiveButton("OK") { _, _ ->
                val name = etName.text.toString()
                val quantity = etQuantity.text.toString()
                if (existingItem == null) {
                    listener?.onItemAdded(ShoppingItem(name = name, quantity = quantity))
                } else {
                    existingItem?.name = name
                    existingItem?.quantity = quantity
                    listener?.onItemUpdated(existingItem!!)
                }
            }
            .setNegativeButton("Отмена", null)

        return builder.create()
    }

    companion object {
        fun newInstance(item: ShoppingItem? = null): ShoppingDialog {
            return ShoppingDialog().apply {
                existingItem = item
            }
        }
    }
}
package ru.ilyamorozov.lab19

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private lateinit var contactsTextView: TextView
    private lateinit var getContactsButton: Button

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            loadContacts()
        } else {
            showPermissionDeniedDialog()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        contactsTextView = findViewById(R.id.contactsTextView)
        getContactsButton = findViewById(R.id.getContactsButton)

        getContactsButton.setOnClickListener {
            checkContactsPermission()
        }
    }

    private fun checkContactsPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_CONTACTS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED -> {
                loadContacts()
            }
            shouldShowRequestPermissionRationale(android.Manifest.permission.READ_CONTACTS) -> {
                showRationaleDialog()
            }
            else -> {
                requestPermissionLauncher.launch(android.Manifest.permission.READ_CONTACTS)
            }
        }
    }

    private fun showRationaleDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.need_permission))
            .setMessage(getString(R.string.to_show_contacts))
            .setPositiveButton(getString(R.string.good)) { _, _ ->
                requestPermissionLauncher.launch(android.Manifest.permission.READ_CONTACTS)
            }
            .setNegativeButton("Нет") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.access_denied))
            .setMessage(getString(R.string.without_permission))
            .setPositiveButton(getString(R.string.open_settings)) { _, _ ->
                openAppSettings()
            }
            .setNegativeButton(getString(R.string.no_thanks)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun openAppSettings() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        )
        startActivity(intent)
    }

    @SuppressLint("Range")
    private fun loadContacts() {
        val contactsList = mutableListOf<String>()
        val cursor = contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            null, null, null, null
        )
        cursor?.use {
            val nameIndex = it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
            while (it.moveToNext()) {
                val name = it.getString(nameIndex)
                contactsList.add(name)
            }
        }
        displayContacts(contactsList)
    }

    private fun displayContacts(contacts: List<String>) {
        if (contacts.isEmpty()) {
            contactsTextView.text = getString(R.string.no_contacts)
        } else {
            contactsTextView.text = "Успех! Получены контакты:\n\n${contacts.joinToString("\n")}"
        }
    }
}
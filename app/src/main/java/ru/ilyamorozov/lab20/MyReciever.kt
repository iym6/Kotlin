package ru.ilyamorozov.lab20

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import ru.ilyamorozov.lab20.R.string

class MyReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        when (intent?.action) {
            context.getString(string.action_reset_color) -> {
                sendColorToActivity(context, "#FFFFFF")
            }
            context.getString(string.action_set_color) -> {
                handleColorInput(context, intent)
            }
        }
    }

    private fun handleColorInput(context: Context, intent: Intent) {
        val remoteInput = RemoteInput.getResultsFromIntent(intent)
        val colorInput = remoteInput?.getCharSequence(MainActivity.KEY_TEXT_REPLY)?.toString()

        if (!colorInput.isNullOrEmpty()) {
            val colorHex = formatColorHex(colorInput)
            sendColorToActivity(context, colorHex)
        }

        NotificationManagerCompat.from(context).cancel(MainActivity.NOTIFICATION_ID)
    }

    private fun formatColorHex(input: String): String {
        return when {
            input.startsWith("#") -> input
            input.length == 6 -> "#$input"
            else -> "#FFFFFF"
        }
    }

    private fun sendColorToActivity(context: Context, colorHex: String) {
        val mainIntent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(MainActivity.EXTRA_COLOR, colorHex)
        }
        context.startActivity(mainIntent)
    }
}
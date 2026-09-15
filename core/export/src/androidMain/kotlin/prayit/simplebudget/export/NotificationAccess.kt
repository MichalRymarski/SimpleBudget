package prayit.simplebudget.export

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings

object NotificationAccess {

    fun isListenerEnabled(context: Context, packageName: String, className: String): Boolean {
        val flat =
            Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        if (flat.isNullOrEmpty()) return false
        val me = ComponentName(packageName, className)
        return flat.split(":").any { ComponentName.unflattenFromString(it) == me }
    }

    fun openSettings(context: Context) {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}

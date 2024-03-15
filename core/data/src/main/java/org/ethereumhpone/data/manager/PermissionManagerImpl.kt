package org.ethereumhpone.data.manager

import android.Manifest
import android.app.NotificationManager
import android.app.role.RoleManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Telephony
import androidx.core.content.ContextCompat
import org.ethereumhpone.domain.manager.PermissionManager
import javax.inject.Inject

class PermissionManagerImpl @Inject constructor(
    private val context: Context,
): PermissionManager {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override fun isDefaultSms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.getSystemService(RoleManager::class.java)?.isRoleHeld(RoleManager.ROLE_SMS) == true
        } else {
            Telephony.Sms.getDefaultSmsPackage(context) == context.packageName
        }    }

    override fun hasReadSms(): Boolean = hasPermission(Manifest.permission.READ_SMS)


    override fun hasSendSms(): Boolean = hasPermission(Manifest.permission.SEND_SMS)

    override fun hasContacts(): Boolean = hasPermission(Manifest.permission.READ_CONTACTS)

    override fun hasNotifications(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return true
        }
        return notificationManager.areNotificationsEnabled()
    }

    override fun hasPhone(): Boolean = hasPermission(Manifest.permission.READ_PHONE_STATE)

    override fun hasCalling(): Boolean = hasPermission(Manifest.permission.CALL_PHONE)

    override fun hasStorage(): Boolean = hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
}
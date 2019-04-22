package chat.rocket.android.helper

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object AndroidPermissionsHelper {

    const val WRITE_EXTERNAL_STORAGE_CODE = 1

    fun checkPermission(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun requestPermission(context: Activity, permission: String, requestCode: Int) {
        ActivityCompat.requestPermissions(context, arrayOf(permission), requestCode)
    }
}
package chat.rocket.android.helper

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

//DEBUG
import timber.log.Timber

object AndroidPermissionsHelper {

    const val WRITE_EXTERNAL_STORAGE_CODE = 1
    const val PERMISSIONS_REQUEST_RW_CONTACTS_CODE = 2
    const val ACCESS_FINE_LOCATION_CODE = 3

    fun checkPermission(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun requestPermission(context: Activity, permission: String, requestCode: Int) {
        ActivityCompat.requestPermissions(context, arrayOf(permission), requestCode)
    }

    fun hasContactsPermission(context: Context): Boolean {
        return checkPermission(context, Manifest.permission.READ_CONTACTS)
    }

    fun getContactsPermissions(activity: Activity) {
        activity.requestPermissions(
                arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS),
                PERMISSIONS_REQUEST_RW_CONTACTS_CODE
        )
    }

    fun hasLocationPermission(context: Context): Boolean {
        Timber.d("##########  EAR>> inside hasLocationPermissionNow")
        return checkPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
    }

    fun getLocationPermission(context: Activity) {
        ActivityCompat.requestPermissions(context,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                ACCESS_FINE_LOCATION_CODE
        )
    }




}
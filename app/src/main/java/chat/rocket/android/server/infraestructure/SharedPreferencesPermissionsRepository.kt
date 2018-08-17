package chat.rocket.android.server.infraestructure

import chat.rocket.android.infrastructure.LocalRepository
import chat.rocket.android.server.domain.PermissionsRepository
import chat.rocket.core.model.Permission
import com.squareup.moshi.Moshi

class SharedPreferencesPermissionsRepository(
    private val localRepository: LocalRepository,
    moshi: Moshi
) : PermissionsRepository {
    private val adapter = moshi.adapter(Permission::class.java)

    override fun save(url: String, permissionList: List<Permission>) {
        for (permission in permissionList) {
            localRepository.save(getPermissionKey(url, permission.id), adapter.toJson(permission))
        }
    }

    override fun get(url: String, permissionId: String): Permission? {
        return localRepository.get(getPermissionKey(url, permissionId))?.let {
            adapter.fromJson(it)
        }
    }

    // Create a key following the pattern: settings_[url]_[permission id]
    // eg.: 'settings_https://open.rocket.chat_create-p'
    private fun getPermissionKey(url: String, permissionId: String): String {
        return "${LocalRepository.PERMISSIONS_KEY}${url}_$permissionId"
    }
}
package chat.rocket.android.server.domain

import chat.rocket.core.model.Permission

interface PermissionsRepository {

    /**
     * Store [permission] locally.
     *
     * @param url The server url from where we're interest to store the permission.
     * @param permission The permission to store.
     */
    fun save(url: String, permission: Permission)

    /**
     * Get permission given by the [permissionId] and for the server [url].
     *
     * @param url The server url from where we're interested on getting the permissions.
     * @param permissionId the id of the permission to get.
     *
     * @return The interested [Permission] or null if not found.
     */
    fun get(url: String, permissionId: String): Permission?
}
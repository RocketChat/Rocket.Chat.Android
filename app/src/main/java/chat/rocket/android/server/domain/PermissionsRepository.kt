package chat.rocket.android.server.domain

import chat.rocket.core.model.Permission

interface PermissionsRepository {

    /**
     * Stores [permission] locally.
     *
     * @param url The server url to store the permission.
     * @param permission The permission to store.
     */
    fun save(url: String, permission: Permission)

    /**
     * Gets permission given by the [permissionId] and for the server [url].
     *
     * @param url The server url to get the permissions from.
     * @param permissionId the ID of the permission to get.
     *
     * @return The [Permission] or null if not found.
     */
    fun get(url: String, permissionId: String): Permission?
}
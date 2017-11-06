package chat.rocket.core.repositories

import io.reactivex.Observable

interface UpdateRepository {

    fun getUpdateAvailable(): Observable<Boolean>

    fun refresh()

}

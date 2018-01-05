package chat.rocket.android.helper

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.withContext
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket

object NetworkHelper {

    /**
     * Checks whether there is internet access.
     *
     * The original author of this code is Levit and you can see his answer here: https://stackoverflow.com/a/27312494/4744263
     *
     * @return true if there is internet access, false otherwise.
     */
    suspend fun hasInternetAccess(): Boolean = withContext(CommonPool) {
        try {
            val socket = Socket()
            val inetSocketAddress = InetSocketAddress("8.8.8.8", 53)

            socket.connect(inetSocketAddress, 1500)
            socket.close()

            true
        } catch (e: IOException) {
            false
        }
    }
}
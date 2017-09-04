package chat.rocket.core.interactors

import com.hadisatrio.optional.Optional
import io.reactivex.Flowable
import io.reactivex.Single

import chat.rocket.core.models.Session
import chat.rocket.core.repositories.SessionRepository

class SessionInteractor(private val sessionRepository: SessionRepository) {

    companion object {

        private val DEFAULT_ID = 0
    }

    fun getDefault(): Flowable<Optional<Session>> {
        return sessionRepository.getById(DEFAULT_ID)
    }

    fun getSessionState(): Flowable<Session.State> {
        return getDefault()
                .map { sessionOptional -> getStateFrom(sessionOptional.orNull()) }
    }

    fun retryLogin(): Single<Boolean> {
        return getDefault()
                .filter { it.isPresent }
                .map { it.get() }
                .filter { session -> session.token != null && (!session.isTokenVerified || session.error != null) }
                .map { session -> Optional.of(session.withTokenVerified(false).withError(null)) }
                .first(Optional.absent())
                .flatMap { sessionOptional ->
                    if (!sessionOptional.isPresent) {
                        return@flatMap Single.just(false)
                    }

                    sessionRepository.save(sessionOptional.get())
                }
    }

    private fun getStateFrom(session: Session?): Session.State {
        if (session == null) {
            return Session.State.UNAVAILABLE
        }

        val token = session.token
        if (token == null || token.isEmpty()) {
            return Session.State.UNAVAILABLE
        }

        val error = session.error
        if (error == null || error.isEmpty()) {
            return Session.State.VALID
        }

        return Session.State.INVALID
    }
}

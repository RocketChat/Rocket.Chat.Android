package chat.rocket.core.repositories

import chat.rocket.core.models.Spotlight
import io.reactivex.Flowable

interface SpotlightRepository {

    fun getSuggestionsFor(term: String, limit: Int): Flowable<List<Spotlight>>
}
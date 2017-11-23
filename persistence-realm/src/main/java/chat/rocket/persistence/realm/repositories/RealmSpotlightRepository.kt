package chat.rocket.persistence.realm.repositories

import android.os.Looper
import chat.rocket.core.models.Spotlight
import chat.rocket.core.repositories.SpotlightRepository
import chat.rocket.persistence.realm.RealmStore
import chat.rocket.persistence.realm.models.ddp.RealmSpotlight
import chat.rocket.persistence.realm.models.ddp.RealmSpotlight.Columns
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import java.util.*

class RealmSpotlightRepository(private val hostname: String) : RealmRepository(), SpotlightRepository {

    override fun getSuggestionsFor(term: String, limit: Int): Flowable<List<Spotlight>> {
        return Flowable.defer { Flowable.using<RealmResults<RealmSpotlight>, Pair<Realm, Looper>>({
            Pair(RealmStore.getRealm(hostname), Looper.myLooper())
        }, { pair ->
            if (pair.first == null) {
                return@using Flowable.empty()
            }

            return@using pair.first.where(RealmSpotlight::class.java)
                    .findAllSorted(Columns.TYPE, Sort.DESCENDING)
                    .asFlowable()
        }) { pair -> close(pair.first, pair.second) }
                .unsubscribeOn(AndroidSchedulers.from(Looper.myLooper()!!))
                .filter { realmSpotlightResults -> realmSpotlightResults.isLoaded && realmSpotlightResults.isValid }
                .map { realmSpotlightResults -> toList(safeSubList<RealmSpotlight>(realmSpotlightResults, 0, limit)) }
        }
    }

    private fun toList(realmSpotlightList: List<RealmSpotlight>): List<Spotlight> {
        val total = realmSpotlightList.size
        val spotlightList = ArrayList<Spotlight>(total)

        (0 until total).mapTo(spotlightList) {
            realmSpotlightList[it].asSpotlight()
        }

        return spotlightList
    }
}
package chat.rocket.android.util.livedata

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext
import timber.log.Timber
import kotlin.coroutines.experimental.CoroutineContext


class TransformedLiveData<Source, Output>(
    private val runContext: CoroutineContext = CommonPool,
    private val source: LiveData<Source>,
    private val transformation: (Source?) -> Output?)
    : LiveData<Output>() {
    private var job: Job? = null

    private val observer = Observer<Source> { source ->
        job?.cancel()
        job = launch(runContext) {
            transformation(source)?.let { transformed ->
                withContext(UI) {
                    value = transformed
                }
            }
        }
    }

    override fun onActive() {
        Timber.d("Attaching observer")
        source.observeForever(observer)
    }

    override fun onInactive() {
        Timber.d("Detaching observer")
        job?.cancel()
        source.removeObserver(observer)
    }
}
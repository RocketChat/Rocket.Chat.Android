package chat.rocket.android.util.livedata

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import kotlin.coroutines.experimental.CoroutineContext

class WrappedLiveData<Source, Output>(
    private val runContext: CoroutineContext = CommonPool,
    private val source: LiveData<Source>,
    private val transformation: suspend (Source?, MutableLiveData<Output>) -> Unit)
    : MutableLiveData<Output>() {
    private var job: Job? = null

    private val observer = Observer<Source> { source ->
        job?.cancel()
        job = launch(runContext) {
            transformation(source, this@WrappedLiveData)
        }
    }

    override fun onActive() {
        source.observeForever(observer)
    }

    override fun onInactive() {
        job?.cancel()
        source.removeObserver(observer)
    }
}

fun <Source, Output> LiveData<Source>.wrap(
    runContext: CoroutineContext = CommonPool,
    transformation: suspend (Source?, MutableLiveData<Output>) -> Unit) =
        WrappedLiveData(runContext, this, transformation)
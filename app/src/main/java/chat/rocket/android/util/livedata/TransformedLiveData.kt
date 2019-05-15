package chat.rocket.android.util.livedata

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext


class TransformedLiveData<Source, Output>(
    private val runContext: CoroutineContext = Dispatchers.IO,
    private val source: LiveData<Source>,
    private val transformation: (Source?) -> Output?
) : LiveData<Output>() {
    private var job: Job? = null

    private val observer = Observer<Source> { source ->
        job?.cancel()
        job = GlobalScope.launch(runContext) {
            transformation(source)?.let { transformed ->
                // Could have used postValue instead, but using the UI context I can guarantee that
                // a canceled job will never emit values.
                withContext(Dispatchers.Main) {
                    value = transformed
                }
            }
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

fun <Source, Output> LiveData<Source>.transform(
    runContext: CoroutineContext = Dispatchers.IO,
    transformation: (Source?) -> Output?
) = TransformedLiveData(runContext, this, transformation)
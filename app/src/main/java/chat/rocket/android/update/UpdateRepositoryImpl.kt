package chat.rocket.android.update

import android.content.Context
import chat.rocket.core.repositories.UpdateRepository
import com.github.javiersantos.appupdater.AppUpdaterUtils
import com.github.javiersantos.appupdater.enums.AppUpdaterError
import com.github.javiersantos.appupdater.enums.UpdateFrom
import com.github.javiersantos.appupdater.objects.Update
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class UpdateRepositoryImpl(context: Context) : UpdateRepository {

    private val mPublishSubject: PublishSubject<Boolean> = PublishSubject.create()
    private val mAppUpdaterUtils: AppUpdaterUtils = AppUpdaterUtils(context.applicationContext)
            .setUpdateFrom(UpdateFrom.GOOGLE_PLAY)
            .withListener(object : AppUpdaterUtils.UpdateListener {
                override fun onSuccess(update: Update, isUpdateAvailable: Boolean) = mPublishSubject.onNext(isUpdateAvailable)

                override fun onFailed(error: AppUpdaterError) = mPublishSubject.onNext(false)
            })

    override fun getUpdateAvailable(): Observable<Boolean> = mPublishSubject

    override fun refresh() {
        mAppUpdaterUtils.start()
    }

}

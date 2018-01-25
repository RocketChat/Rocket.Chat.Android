package chat.rocket.android.app

import android.app.Activity
import android.app.Application
import android.app.Service
import chat.rocket.android.BuildConfig
import chat.rocket.android.app.utils.CustomImageFormatConfigurator
import chat.rocket.android.dagger.DaggerAppComponent
import com.facebook.drawee.backends.pipeline.DraweeConfig
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.core.ImagePipelineConfig
import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import dagger.android.HasServiceInjector
import timber.log.Timber
import javax.inject.Inject

class RocketChatApplication : Application(), HasActivityInjector, HasServiceInjector {

    @Inject
    lateinit var activityDispatchingAndroidInjector: DispatchingAndroidInjector<Activity>

    @Inject
    lateinit var serviceDispatchingAndroidInjector: DispatchingAndroidInjector<Service>

    companion object {
        lateinit var instance: RocketChatApplication
    }

    override fun onCreate() {
        super.onCreate()

        DaggerAppComponent.builder().application(this).build().inject(this)

        AndroidThreeTen.init(this)

        setupFresco()
        setupTimber()
        instance = this
    }

    private fun setupFresco() {
        val imagePipelineConfig = ImagePipelineConfig.newBuilder(this)
                .setImageDecoderConfig(CustomImageFormatConfigurator.createImageDecoderConfig())
                .build()

        val draweeConfigBuilder = DraweeConfig.newBuilder()

        CustomImageFormatConfigurator.addCustomDrawableFactories(draweeConfigBuilder)

        Fresco.initialize(this, imagePipelineConfig, draweeConfigBuilder.build())
    }

    private fun setupTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    override fun activityInjector(): AndroidInjector<Activity> {
        return activityDispatchingAndroidInjector
    }

    override fun serviceInjector(): AndroidInjector<Service> {
        return serviceDispatchingAndroidInjector
    }
}
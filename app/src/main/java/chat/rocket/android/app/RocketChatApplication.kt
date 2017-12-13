package chat.rocket.android.app

import android.app.Activity
import android.app.Application
import chat.rocket.android.BuildConfig
import chat.rocket.android.app.utils.CustomImageFormatConfigurator
import com.facebook.drawee.backends.pipeline.DraweeConfig
import chat.rocket.android.dagger.DaggerAppComponent
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.core.ImagePipelineConfig
import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import timber.log.Timber
import javax.inject.Inject

class RocketChatApplication : Application(), HasActivityInjector {

    @Inject
    lateinit var activityDispatchingAndroidInjector: DispatchingAndroidInjector<Activity>

    override fun onCreate() {
        super.onCreate()

        DaggerAppComponent.builder().application(this).build().inject(this)

        AndroidThreeTen.init(this)

        setupFresco()
        setupTimber()
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
}
package chat.rocket.android.preferences.presentation

import chat.rocket.android.R
import chat.rocket.android.emoji.PhysicalKeyboardConfig
import chat.rocket.android.preferences.interactor.PhysicalKeyboardConfigInteractor
import chat.rocket.android.server.domain.AnalyticsTrackingInteractor
import javax.inject.Inject

class PreferencesPresenter @Inject constructor(
    private val view: PreferencesView,
    private val analyticsTrackingInteractor: AnalyticsTrackingInteractor,
    private val physicalKeyboardConfigInteractor: PhysicalKeyboardConfigInteractor
) {

    private var physicalKeyboardConfig: PhysicalKeyboardConfig = PhysicalKeyboardConfig.EnterPlusControl
        set(value) {
            field = value
            physicalKeyboardConfigInteractor.saveConfig(value)
        }
        get() = physicalKeyboardConfigInteractor.getConfig()

    private var tempPhysicalKeyboardConfig = physicalKeyboardConfig

    fun loadAnalyticsTrackingInformation() {
        view.setupAnalyticsTrackingView(analyticsTrackingInteractor.get())
    }

    fun enableAnalyticsTracking() {
        analyticsTrackingInteractor.save(true)
    }

    fun disableAnalyticsTracking() {
        analyticsTrackingInteractor.save(false)
    }

    fun getPhysicalKeyboardRadioId(): Int {
        return when (physicalKeyboardConfig) {
            PhysicalKeyboardConfig.Enter -> R.id.radio_physical_keyboard_enter
            PhysicalKeyboardConfig.EnterPlusShift -> R.id.radio_physical_keyboard_enter_shift
            PhysicalKeyboardConfig.EnterPlusControl -> R.id.radio_physical_keyboard_enter_control
            else -> R.id.radio_physical_keyboard_disabled
        }
    }

    fun onPhysicalKeyboardRadioChange(checkedId: Int) {
        tempPhysicalKeyboardConfig = when (checkedId) {
            R.id.radio_physical_keyboard_enter -> PhysicalKeyboardConfig.Enter
            R.id.radio_physical_keyboard_enter_shift -> PhysicalKeyboardConfig.EnterPlusShift
            R.id.radio_physical_keyboard_enter_control -> PhysicalKeyboardConfig.EnterPlusControl
            else -> PhysicalKeyboardConfig.Disable
        }
    }

    fun onPhysicalKeyboardDialogOkClicked() {
        physicalKeyboardConfig = tempPhysicalKeyboardConfig
    }

}

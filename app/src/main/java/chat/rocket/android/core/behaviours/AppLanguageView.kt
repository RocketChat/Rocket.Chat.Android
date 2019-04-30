package chat.rocket.android.core.behaviours

interface AppLanguageView {

    /**
     * Updates the app language
     *
     * @param language The app language to be updated.
     * @param country Opcional. The country code to be updated.
     */
    fun updateLanguage(language: String, country: String? = null)
}
package chat.rocket.android.util

interface DomainToViewModel<Domain, ViewModel> {
    fun translate(domain: Domain): ViewModel
}

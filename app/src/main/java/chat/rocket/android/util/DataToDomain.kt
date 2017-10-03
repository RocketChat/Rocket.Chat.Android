package chat.rocket.android.util

interface DataToDomain<Data, Domain> {
    fun translate(data: Data): Domain
}

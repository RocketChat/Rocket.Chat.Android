package chat.rocket.android.contacts.models

import android.os.Parcel
import android.os.Parcelable

class Contact() : Parcelable {
    private var name: String? = null
    private var phoneNumber: String? = null
    private var emailAddress: String? = null
    private var isPhone: Boolean = true
    private var userId: String? = null
    private var username: String? = null
    private var avatarUrl: String? = null
    private var status: String? = null
    private var isSpotlightResult: Boolean = false

    fun getUserId(): String? {
        return userId
    }

    fun getUsername(): String? {
        return username
    }

    fun getDetail(): String? {
        if(this.isPhone){
            return getPhoneNumber()
        }else{
            return getEmailAddress()
        }
    }

    fun getName(): String? {
        return name
    }

    fun setName(name: String) {
        this.name = name
    }

    fun setUserId(userId: String?) {
        this.userId = userId
    }

    fun setUsername(username: String?) {
        this.username = username
    }

    fun setIsPhone(isPhone: Boolean) {
        this.isPhone = isPhone
    }

    fun getIsSpotlightResult(): Boolean {
        return this.isSpotlightResult
    }

    fun setIsSpotlightResult(isSpotlightResult: Boolean) {
        this.isSpotlightResult = isSpotlightResult
    }

    fun getPhoneNumber(): String? {
        return phoneNumber
    }

    fun setPhoneNumber(phoneNumber: String) {
        this.phoneNumber = phoneNumber
    }

    fun getEmailAddress(): String? {
        return emailAddress
    }

    fun setEmailAddress(emailAddress: String) {
        this.emailAddress = emailAddress
        this.isPhone = false
    }

    fun isPhone(): Boolean {
        return this.isPhone
    }

    fun getAvatarUrl(): String? {
        return avatarUrl
    }

    fun setAvatarUrl(url: String) {
        this.avatarUrl = url
    }

    fun getStatus(): String? {
        return status
    }

    fun setStatus(status: String) {
        this.status = status
    }

    constructor(parcel: Parcel) : this() {
        this.name = parcel.readString()
        this.phoneNumber = parcel.readString()
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(name)
        dest?.writeString(phoneNumber)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Contact> {
        override fun createFromParcel(parcel: Parcel): Contact {
            return Contact(parcel)
        }

        override fun newArray(size: Int): Array<Contact?> {
            return arrayOfNulls(size)
        }
    }
}

sealed class ContactsLoadingState {
    data class Loading(val fromRefreshButton: Boolean) : ContactsLoadingState()
    data class Loaded(val fromRefreshButton: Boolean) : ContactsLoadingState()
    data class Error(val fromRefreshButton: Boolean) : ContactsLoadingState()
}

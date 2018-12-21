package chat.rocket.android.contacts.models

import android.os.Parcel
import android.os.Parcelable


class Contact() : Parcelable {
    private var name: String? = null
    private var phoneNumber: String? = null
    private var emailAddress: String? = null
    private var isPhone: Boolean = true
    private var username: String? = null
    private var avatarUrl: String? = null


    fun getUsername(): String? {
        return username
    }


    private fun formatPhoneNumber(phone: String): String {
        return phone.replace("-|\\s|\\(|\\)".toRegex(), "")
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

    fun setUsername(username: String?) {
        this.username = username
    }

    fun setIsPhone(isPhone: Boolean) {
        this.isPhone = isPhone
    }

    fun getPhoneNumber(): String? {
        return phoneNumber
    }

    fun setPhoneNumber(phoneNumber: String) {
        this.phoneNumber = formatPhoneNumber(phoneNumber)
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

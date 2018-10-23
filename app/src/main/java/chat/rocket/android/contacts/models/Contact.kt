package chat.rocket.android.contacts.models

import android.os.Parcel
import android.os.Parcelable


class Contact() : Parcelable {
    private var id: Int = 0
    private var name: String? = null

    private var phoneNumber: String? = null

    fun getName(): String? {
        return name
    }

    fun setName(name: String) {
        this.name = name
    }

    fun getPhoneNumber(): String? {
        return phoneNumber
    }

    fun setPhoneNumber(phoneNumber: String) {
        this.phoneNumber = phoneNumber
    }

    constructor(parcel: Parcel) : this() {
        this.id = parcel.readInt()
        this.name = parcel.readString()
        this.phoneNumber = parcel.readString()
    }


    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeInt(id)
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
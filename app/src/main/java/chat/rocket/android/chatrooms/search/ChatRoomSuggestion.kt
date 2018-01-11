package chat.rocket.android.chatrooms.search

import android.os.Parcel
import android.os.Parcelable
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion

class ChatRoomSuggestion(val chatRoomName: String) : SearchSuggestion {

    override fun getBody(): String = chatRoomName

    constructor(parcel: Parcel) : this(parcel.readString())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(chatRoomName)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ChatRoomSuggestion> {
        override fun createFromParcel(parcel: Parcel): ChatRoomSuggestion {
            return ChatRoomSuggestion(parcel)
        }

        override fun newArray(size: Int): Array<ChatRoomSuggestion?> {
            return arrayOfNulls(size)
        }
    }
}
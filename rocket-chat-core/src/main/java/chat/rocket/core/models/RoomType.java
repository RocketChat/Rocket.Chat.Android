package chat.rocket.core.models;

import org.jetbrains.annotations.Nullable;

public enum RoomType {
    CHANNEL("c"),
    GROUP("p"),
    DIRECT_MESSAGE("d"),
    LIVECHAT("l")
    ;

    private final String type;
    RoomType(String type) {
        this.type = type;
    }

    @Nullable
    public static String get(@Nullable String type) {
        for (RoomType roomType : RoomType.values()) {
            if (roomType.type.equals(type)) {
                return roomType.type;
            }
        }
        return null;
    }
}

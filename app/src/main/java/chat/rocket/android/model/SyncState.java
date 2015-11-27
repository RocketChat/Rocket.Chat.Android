package chat.rocket.android.model;

public enum SyncState{
    NOT_SYNCED(0)
    ,SYNCING(1)
    ,SYNCED(2)
    ,FAILED(3)

    ;//------------

    private int value;
    SyncState(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static SyncState valueOf(int value) {
        for(SyncState s :SyncState.values()){
            if(s.value == value) return s;
        }
        throw new IllegalArgumentException("SyncState.valueOf: invalid parameter: value="+value);
    }
}

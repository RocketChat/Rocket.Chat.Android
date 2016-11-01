package chat.rocket.android.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class ServerAuthProvider extends RealmObject {
    @PrimaryKey
    private String name; //email, twitter, github, ...
}

package chat.rocket.persistence.realm.modules;

import chat.rocket.persistence.realm.models.RealmBasedServerInfo;
import io.realm.annotations.RealmModule;

@RealmModule(library = true, classes = {RealmBasedServerInfo.class})
public class RocketChatServerModule {
}

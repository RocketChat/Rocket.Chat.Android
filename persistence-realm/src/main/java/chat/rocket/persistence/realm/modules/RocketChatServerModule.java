package chat.rocket.persistence.realm.modules;

import io.realm.annotations.RealmModule;

import chat.rocket.persistence.realm.models.RealmBasedServerInfo;

@RealmModule(library = true, classes = {RealmBasedServerInfo.class})
public class RocketChatServerModule {
}

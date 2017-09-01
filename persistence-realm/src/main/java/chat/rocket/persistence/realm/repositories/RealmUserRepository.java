package chat.rocket.persistence.realm.repositories;

import android.os.Looper;
import android.support.v4.util.Pair;

import com.hadisatrio.optional.Optional;

import java.util.ArrayList;
import java.util.List;

import chat.rocket.core.models.User;
import chat.rocket.core.repositories.UserRepository;
import chat.rocket.persistence.realm.RealmStore;
import chat.rocket.persistence.realm.models.ddp.RealmUser;
import hu.akarnokd.rxjava.interop.RxJavaInterop;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class RealmUserRepository extends RealmRepository implements UserRepository {
    private final String hostname;

    public RealmUserRepository(String hostname) {
        this.hostname = hostname;
    }

    @Override
    public Flowable<List<User>> getAll() {
        return Flowable.defer(() -> Flowable.using(
                () -> new Pair<>(RealmStore.getRealm(hostname), Looper.myLooper()),
                pair -> RxJavaInterop.toV2Flowable(
                        pair.first.where(RealmUser.class)
                                .findAll()
                                .asObservable()),
                pair -> close(pair.first, pair.second))
                .unsubscribeOn(AndroidSchedulers.from(Looper.myLooper()))
                .filter(roomSubscriptions -> roomSubscriptions != null && roomSubscriptions.isLoaded()
                        && roomSubscriptions.isValid())
                .map(this::toList));
    }

    @Override
    public Flowable<Optional<User>> getCurrent() {
        return Flowable.defer(() ->
                realmGetCurrent()
                    .unsubscribeOn(AndroidSchedulers.from(Looper.myLooper()))
                    .filter(it -> it != null && it.isLoaded() && it.isValid())
                    .map(realmUsers -> {
                        if (realmUsers.size() > 0) {
                            return Optional.of(realmUsers.get(0).asUser());
                        }

                        return Optional.<User>absent();
                    }));
    }

    private Flowable<RealmResults<RealmUser>> realmGetCurrent() {
        return Flowable.using(
                () -> new Pair<>(RealmStore.getRealm(hostname), Looper.myLooper()),
                pair -> RxJavaInterop.toV2Flowable(
                        pair.first.where(RealmUser.class)
                                .isNotEmpty(RealmUser.EMAILS)
                                .findAll()
                                .<RealmResults<RealmUser>>asObservable()),
                pair -> close(pair.first, pair.second));
    }

    @Override
    public Flowable<Optional<User>> getByUsername(String username) {
        return Flowable.defer(() ->
                realmGetByUsername(username)
                        .unsubscribeOn(AndroidSchedulers.from(Looper.myLooper()))
                        .map(optional -> {
                            if (optional.isPresent()) {
                                return Optional.of(optional.get().asUser());
                            }

                            return Optional.absent();
                        }));
    }

    private Flowable<Optional<RealmUser>> realmGetByUsername(String username) {
        return Flowable.using(
                () -> new Pair<>(RealmStore.getRealm(hostname), Looper.myLooper()),
                pair -> realmQueryUsername(pair.first, username),
                pair -> close(pair.first, pair.second));
    }

    private Flowable<Optional<RealmUser>> realmQueryUsername(Realm realm, String username) {
        RealmUser realmUser = realm.where(RealmUser.class)
                .equalTo(RealmUser.USERNAME, username)
                .findFirst();

        if (realmUser == null) {
            return Flowable.just(Optional.absent());
        }

        return RxJavaInterop.toV2Flowable(
                realmUser
                        .<RealmUser>asObservable()
                        .filter(user -> user.isLoaded() && user.isValid())
                        .map(Optional::of));
    }

    @Override
    public Flowable<List<User>> getSortedLikeName(String name, int limit) {
        return Flowable.defer(() ->
                realmGetSortedLikeName(name)
                        .unsubscribeOn(AndroidSchedulers.from(Looper.myLooper()))
                        .filter(realmUsers -> realmUsers != null && realmUsers.isLoaded()
                                && realmUsers.isValid())
                        .map(realmUsers -> toList(safeSubList(realmUsers, 0, limit))));
    }

    private Flowable<RealmResults<RealmUser>> realmGetSortedLikeName(String name) {
        return Flowable.using(
                () -> new Pair<>(RealmStore.getRealm(hostname), Looper.myLooper()),
                pair -> RxJavaInterop.toV2Flowable(
                        pair.first.where(RealmUser.class)
                                .like(RealmUser.USERNAME, "*" + name + "*", Case.INSENSITIVE)
                                .findAllSorted(RealmUser.USERNAME, Sort.DESCENDING)
                                .asObservable()),
                pair -> close(pair.first, pair.second));
    }

    private List<User> toList(List<RealmUser> realmUsers) {
        int total = realmUsers.size();

        final List<User> userList = new ArrayList<>(total);

        for (int i = 0; i < total; i++) {
            userList.add(realmUsers.get(i).asUser());
        }

        return userList;
    }
}
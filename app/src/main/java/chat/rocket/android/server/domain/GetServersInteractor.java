package chat.rocket.android.server.domain;

import java.util.List;

import io.reactivex.Scheduler;
import io.reactivex.Single;

public class GetServersInteractor {
    private final ServersRepository repository;
    private final Scheduler executionScheduler;

    public GetServersInteractor(ServersRepository repository, Scheduler executionScheduler) {
        this.repository = repository;
        this.executionScheduler = executionScheduler;
    }

}

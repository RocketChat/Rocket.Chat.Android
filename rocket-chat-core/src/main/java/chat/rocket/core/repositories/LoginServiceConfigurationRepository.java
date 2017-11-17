package chat.rocket.core.repositories;

import com.hadisatrio.optional.Optional;

import java.util.List;

import chat.rocket.core.models.LoginServiceConfiguration;
import io.reactivex.Flowable;
import io.reactivex.Single;

public interface LoginServiceConfigurationRepository {

  Single<Optional<LoginServiceConfiguration>> getByName(String serviceName);

  Flowable<List<LoginServiceConfiguration>> getAll();
}

package today.wishwordrobe.infrastructure;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface NotificationPreferenceRepository extends ReactiveMongoRepository<NotificationPreferenceDocument, Long> {}



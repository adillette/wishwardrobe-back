package today.wishwordrobe.infrastructure;

import today.wishwordrobe.presentation.dto.PushNotificationRequest;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface PushNotificationRepository extends ReactiveMongoRepository<PushNotificationRequest,String> {
}

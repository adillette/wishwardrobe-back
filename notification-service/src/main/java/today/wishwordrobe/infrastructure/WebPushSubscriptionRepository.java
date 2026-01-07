package today.wishwordrobe.infrastructure;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import today.wishwordrobe.presentation.dto.WebPushSubscriptionDocument;

public interface WebPushSubscriptionRepository extends ReactiveMongoRepository<WebPushSubscriptionDocument,String>{

}

package today.wishwordrobe.weather.infrastructure;



import today.wishwordrobe.weather.domain.WeatherTotal;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WeatherRepository extends ReactiveMongoRepository<WeatherTotal,String> {
}

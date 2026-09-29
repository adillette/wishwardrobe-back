package today.wishwordrobe.weather.infrastructure;



import today.wishwordrobe.weather.domain.WeatherTotal;

import java.time.LocalDate;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface WeatherRepository extends ReactiveMongoRepository<WeatherTotal,String> {
    Mono<WeatherTotal> findByAreaCodeAndForecastDate(String areaCode, LocalDate forecastDate);
    Flux<WeatherTotal> findAllByForecastDate(LocalDate forecastDate);

}

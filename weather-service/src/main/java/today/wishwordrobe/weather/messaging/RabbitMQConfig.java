package today.wishwordrobe.weather.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
  //weather-service가 발행하는 exchange
  //clothes-service가 이 exchange를 구독한다
  public static final String WEATHER_EXCHANGE="weather.exchange";
  
  //clothes-service가 구독하는 큐
  public static final String WWEATHER_QUEUE = "weather.clothes.queue";

  //routing key : weather.exchange-> weather.clothes.queue 라우팅
  public static final String WEATHER_ROUTING_KEY="weather.fetchd";

  @Bean
  public TopicExchange weatherExchange(){
    return new TopicExchange(WEATHER_EXCHANGE);
  }

  //durable=true: rabbitmq 재시작 해도 큐유지
  @Bean
  public Queue weatherQueue(){
    return new Queue(WWEATHER_QUEUE,true);
  }

  @Bean
  public Binding weatherBinding(){
    return BindingBuilder
          .bind(weatherQueue())
          .to(weatherExchange())
          .with(WEATHER_ROUTING_KEY);
        }

  //jackson2jsonmessageconverter 
  //java객체를 json 직렬화
  //weather 이벤트-> json-> rabbitmq 메시지로
  @Bean
  public Jackson2JsonMessageConverter messageConverter(){
    return new Jackson2JsonMessageConverter();
  }

  

}

package today.wishwordrobe.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitMQConfig {

   
  //clothes-service가 발행하는 exchange
  public static final String CLOTHES_EXCHANGE="clothes.exchange";
  
  //notification-service 가 구독하는 큐
  //clothes-service-> clothes.exchange->clothes.matched 이 큐로 라우팅
  public static final String NOTIFICATION_QUEUE="clothes.notification.queue";

  public static final String CLOTHES_ROUTING_KEY="clothes.matched";

  
  //스케줄러 -> weather-service 요청용
  public static final String WEATHER_REQUEST_EXCHANGE = "weather.request.exchange";
  public static final String WEATHER_REQUEST_QUEUE ="weather.request.queue";
  public static final String WEATHER_REQUEST_ROUTING_KEY ="weather.request";


 

  @Bean
  public TopicExchange clothesExchange(){
    return new TopicExchange(CLOTHES_EXCHANGE);
  }

  //durable= true: Rabbitmq 재시작해도 큐유지
  //언제 필요하나? 알림 서비스에서 메시지 유실 방지를 위해서 
  @Bean
  public Queue notificationQueue(){
    return new Queue(NOTIFICATION_QUEUE,true);
  }

  @Bean
  public Binding notificationBinding(){
    return BindingBuilder 
              .bind(notificationQueue())
              .to(clothesExchange())
              .with(CLOTHES_ROUTING_KEY);
            }
  //weather.request.exchange 발행용
  @Bean
  public TopicExchange weatherRequestExchange(){
    return new TopicExchange(WEATHER_REQUEST_EXCHANGE);
  }
   // weather-service(Consumer)가 구독할 큐
    // 단 Consumer 쪽에서도 선언하므로 멱등 동작
  @Bean
  public Queue weatherRequestQueue() {
        return new Queue(WEATHER_REQUEST_QUEUE, true);
  }


  @Bean
  public Binding weatherRequestBinding(){
    return BindingBuilder
            .bind(weatherRequestQueue())
            .to(weatherRequestExchange())
            .with(WEATHER_REQUEST_ROUTING_KEY);
  }

  @Bean
  public Jackson2JsonMessageConverter messageConverter(){
    return new Jackson2JsonMessageConverter();
  }
}

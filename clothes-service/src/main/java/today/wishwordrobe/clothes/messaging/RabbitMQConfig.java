package today.wishwordrobe.clothes.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitMQConfig {
  //weather-service-> clothes-service 구간
  public static final String WEATHER_EXCHANGE="weather.exchange";
  public static final String WEATHER_QUEUE="weather.clothes.queue";
  public static final String WEATHER_ROUTING_KEY="weather.fetched";
  //clothes-service-> notification-service 구간
  public static final String CLOTHES_EXCHANGE="clothes.exchange";
  public static final String CLOTHES_QUEUE="clothes.notification.queue";
  public static final String CLOTHES_ROUTING_KEY="clothes.matched";

  //weather 수신용
  @Bean
  public TopicExchange weatherExchange(){
    return new TopicExchange (WEATHER_EXCHANGE);
  }
  
  @Bean
  public Queue weatherClothesQueue(){
    return new Queue(WEATHER_QUEUE,true);
    //durable=true: rabbitmq 재시작해도 큐유지
  }

  @Bean
  public Binding weatherBinding(Queue weatherClothesQueue, TopicExchange weatherExchange){
    return BindingBuilder
            .bind(weatherClothesQueue)
            .to(weatherExchange)
            .with(WEATHER_ROUTING_KEY);
  }

  //clothes 발행용 설정
  @Bean
  public TopicExchange clothesExchange(){
    return new TopicExchange(CLOTHES_EXCHANGE);
  }

  @Bean
  public Queue clothesNotificationQueue(){
    return new Queue(CLOTHES_QUEUE, true);
  }

  @Bean
  public Binding clothesBinding(Queue clothesNotificationQueue, TopicExchange clothesExchange){
    return BindingBuilder
            .bind(clothesNotificationQueue)
            .to(clothesExchange)
            .with(CLOTHES_ROUTING_KEY);


            
            /*
            clothes.exchange 에 들어온 메시지 중
            
            Routing Key가
            
            clothes.matched 인 메시지는
            
            clothes.notification.queue 로 보내라
            */
//공통 설정
}

@Bean
public MessageConverter jsonMessageConverter(){
  return new Jackson2JsonMessageConverter();//객체랑 json 자동 변환
}

@Bean
public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory){
  RabbitTemplate rabbitTemplate= new RabbitTemplate(connectionFactory);
  rabbitTemplate.setMessageConverter(jsonMessageConverter());
  return rabbitTemplate;

}



}

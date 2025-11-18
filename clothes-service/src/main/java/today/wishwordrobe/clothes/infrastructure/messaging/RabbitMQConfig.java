package today.wishwordrobe.clothes.infrastructure.messaging;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 설정
 * Exchange, Queue, Binding 등을 정의
 */
@Configuration
public class RabbitMQConfig {

    // Exchange 이름
    public static final String CLOTHES_EXCHANGE = "clothes.exchange";

    // Queue 이름들
    public static final String CLOTHES_CACHE_QUEUE = "clothes.cache.queue";
    public static final String CLOTHES_NOTIFICATION_QUEUE = "clothes.notification.queue";

    // Routing Key
    public static final String CLOTHES_CREATED_KEY = "clothes.created";
    public static final String CLOTHES_UPDATED_KEY = "clothes.updated";
    public static final String CLOTHES_DELETED_KEY = "clothes.deleted";

    /**
     * Topic Exchange 생성
     * 라우팅 키 패턴으로 메시지를 라우팅
     */
    @Bean
    public TopicExchange clothesExchange() {
        return new TopicExchange(CLOTHES_EXCHANGE);
    }

    /**
     * 캐시 무효화용 Queue
     */
    @Bean
    public Queue clothesCacheQueue() {
        return QueueBuilder.durable(CLOTHES_CACHE_QUEUE)
                .build();
    }

    /**
     * 알림 전송용 Queue
     */
    @Bean
    public Queue clothesNotificationQueue() {
        return QueueBuilder.durable(CLOTHES_NOTIFICATION_QUEUE)
                .build();
    }

    /**
     * Cache Queue Binding
     * clothes.* 패턴으로 모든 옷장 이벤트를 캐시 큐로 전달
     */
    @Bean
    public Binding bindingCacheQueue(
            @Qualifier("clothesCacheQueue") Queue clothesCacheQueue, TopicExchange clothesExchange) {

        return BindingBuilder
                .bind(clothesCacheQueue)
                .to(clothesExchange)
                .with("clothes.*");
    }

    /**
     * Notification Queue Binding
     * clothes.created와 clothes.deleted 이벤트만 알림 큐로 전달
     */
    @Bean
    public Binding bindingNotificationQueueCreated(
        @Qualifier("clothesNotificationQueue")    
        Queue clothesNotificationQueue, TopicExchange clothesExchange) {
        return BindingBuilder
                .bind(clothesNotificationQueue)
                .to(clothesExchange)
                .with(CLOTHES_CREATED_KEY);
    }

    @Bean
    public Binding bindingNotificationQueueDeleted(
        @Qualifier("clothesNotificationQueue")
        Queue clothesNotificationQueue, TopicExchange clothesExchange) {
        return BindingBuilder
                .bind(clothesNotificationQueue)
                .to(clothesExchange)
                .with(CLOTHES_DELETED_KEY);
    }

    /**
     * JSON 메시지 컨버터
     * 객체를 JSON으로 자동 변환
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitTemplate 설정
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}

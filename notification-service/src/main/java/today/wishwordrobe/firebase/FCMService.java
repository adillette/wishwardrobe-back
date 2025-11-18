package today.wishwordrobe.firebase;
import com.google.firebase.messaging.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.time.Duration;

@Service
public class FCMService {

    private final Logger logger = LoggerFactory.getLogger(FCMService.class);

    public Mono<String> sendPushNotification(FCMPushNotificationRequest  request) {
        return Mono.fromCallable((Callable<? extends String>) () -> {
            Message message = prepareMessage(request);
            return sendAndGetResponse(message);
        });
    }

    public Mono<String> sendTopicMessage(FCMPushNotificationRequest  request) {
        return Mono.fromCallable((Callable<? extends String>) () -> {
            Message message = Message.builder()
                    .setNotification(Notification.builder()
                            .setTitle(request.getTitle())
                            .setBody(request.getMessage())
                            .build())
                    .putAllData(request.getData())
                    .setTopic(request.getTopic())
                    .build();

            return sendAndGetResponse(message);
        });
    }

    public Mono<String> sendTokenMessage(FCMPushNotificationRequest  request) {
        return Mono.fromCallable((Callable<? extends String>) () -> {
            Message message = Message.builder()
                    .setNotification(Notification.builder()
                            .setTitle(request.getTitle())
                            .setBody(request.getMessage())
                            .build())
                    .putAllData(request.getData())
                    .setToken(request.getToken())
                    .build();

            return sendAndGetResponse(message);
        });
    }

    private Message prepareMessage(FCMPushNotificationRequest  request) {
        Message.Builder builder = Message.builder()
                .setNotification(Notification.builder()
                        .setTitle(request.getTitle())
                        .setBody(request.getMessage())
                        .build());

        if (request.getTopic() != null) {
            builder.setTopic(request.getTopic());
        } else if (request.getToken() != null) {
            builder.setToken(request.getToken());
        }

        if (request.getData() != null) {
            builder.putAllData(request.getData());
        }
        //collapseKey로 사용할 값을 결정
        String collapseKey = request.getTopic()!=null ? request.getTopic() :
                (request.getToken() != null ? "token-msg": "default-key");
        AndroidConfig androidConfig = getAndroidConfig(collapseKey, request);
        return builder
                .setAndroidConfig(androidConfig)
                .build();
    }

    private AndroidConfig getAndroidConfig(String collapseKey, FCMPushNotificationRequest request){
        return AndroidConfig.builder()
                .setTtl(Duration.ofHours(2).toMillis())
                .setCollapseKey(collapseKey)
                .setNotification(AndroidNotification.builder()
                        .setIcon(request.getIcon())
                        .setClickAction(request.getClickAction())
                        .setSound("default")
                        .build())
                .setPriority(AndroidConfig.Priority.HIGH)
                .build();
    }



    private String sendAndGetResponse(Message message) throws InterruptedException, ExecutionException {
        return FirebaseMessaging.getInstance().sendAsync(message).get();
    }

}


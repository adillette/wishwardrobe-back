package today.wishwordrobe.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import jakarta.annotation.PostConstruct;

@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${firebase.config-path}")
    private Resource firebaseConfigResource;

    //@PostConstruct: 빈의 초기화를 위해 모든 의존성이 주입된후에 실행됨
    @PostConstruct
    public void initFirebase(){
        try{
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(firebaseConfigResource.getInputStream()))
                    .build();

            FirebaseApp.initializeApp(options);
            log.info("Firebase initialized successfully");

        } catch (Exception e){
            log.error("Failed to initialize Firebase", e);
            e.printStackTrace();
        }
    }

}

package today.wishwordrobe.webpush;

import java.security.GeneralSecurityException;
import java.security.Security;
import java.time.Duration;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import nl.martijndwars.webpush.PushAsyncService;

//바운시 캐슬 : 자바 암호화 아키텍처를 등록하기 위한 컴포넌트
//Security.addProvider(new BouncyCastleProvider())를 사용해서 등록하고 암호화 동작에 활용

@Configuration
public class WebPushInitializer {

    @Value("${webpush.public-key}")
    private String publicKey;

    @Value("${webpush.private-key}")
    private String privateKey;

    @Value("${webpush.subject}")
    private String subject;

    @Bean
    public PushAsyncService pushAsyncService() throws GeneralSecurityException {
        return new PushAsyncService(publicKey, privateKey, subject);
    }

    //dsl: 정적 메서드 async-http-client 라이브러리가 제공하는 편의용 유틸리티 클래스
    @Bean
    public AsyncHttpClient asyncHttpClient(){
        return new DefaultAsyncHttpClient(//Dsl 정적 메서드 대신 직접 생성
            new DefaultAsyncHttpClientConfig.Builder()
                .setConnectTimeout(Duration.ofSeconds(3))
                .setRequestTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(5))
                .build()
            );
        
    }

    //@postconstruct: 의존성 주입이 이루어진후 초기화를 수행하는 메서드 

    // BouncyCastle - 키 암호화/복호화 라이브러리 등록
    // publicKey, privateKey를 EC 키로 변환할 때 필요
    // 없으면 NoSuchProviderException 발생
    @PostConstruct
    public void initBouncyCastle(){
       if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null){
            Security.addProvider(new BouncyCastleProvider());
        }
    }

}

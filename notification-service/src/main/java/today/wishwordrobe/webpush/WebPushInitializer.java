package today.wishwordrobe.webpush;


import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import nl.martijndwars.webpush.PushService;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;

@Configuration
public class WebPushInitializer {

    @Value("${webpush.public-key}")
    private String publicKey;

    @Value("${webpush.private-key}")
    private String privateKey;

    @Value("${webpush.subject}")
    private String subject;

    @Bean
    public PushService pushService() throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null){
            Security.addProvider(new BouncyCastleProvider());
        }

        PushService pushService = new PushService();
        pushService.setPublicKey(publicKey);
        pushService.setPrivateKey(privateKey);
        pushService.setSubject(subject);
        return pushService;
    }
}

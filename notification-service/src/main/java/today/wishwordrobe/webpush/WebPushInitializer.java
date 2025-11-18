package today.wishwordrobe.webpush;

import nl.martijndwars.webpush.PushService;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

    @Bean
    public PushService pushService() throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null){
            Security.addProvider(new BouncyCastleProvider());
        }

        PushService pushService = new PushService();
        pushService.setPublicKey(publicKey);
        pushService.setPrivateKey(privateKey);

        return pushService;
    }
}

package today.wishwordrobe.clothes.configuration;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class ResourceConfig implements WebMvcConfigurer {
    @Value("${file.uploadFiles}")
    String fileDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry){
        registry.addResourceHandler("/clothes/images/**") //브라우저가 요청하는 url 패턴
                .addResourceLocations("file:///"+fileDir + "/") //실제로 검색하는 로컬 디렉토리
                .setCachePeriod(60*60*24*365); //접근파일 캐싱시간
    }
}

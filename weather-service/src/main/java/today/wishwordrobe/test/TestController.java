package today.wishwordrobe.test;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/test")
public class TestController {

    @GetMapping("/health")
    public Mono<ResponseEntity<Map<String, Object>>> healthCheck() {
        log.info("Weather Service health check called");

        Map<String, Object> response = new HashMap<>();
        response.put("service", "weather-service");
        response.put("port", 8082);
        response.put("status", "OK");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("message", "Weather Service is running!");

        return Mono.just(ResponseEntity.ok(response));
    }

    @GetMapping("/echo")
    public Mono<ResponseEntity<Map<String, Object>>> echo(@RequestParam(defaultValue = "Hello") String message) {
        log.info("Echo called with message: {}", message);

        Map<String, Object> response = new HashMap<>();
        response.put("service", "weather-service");
        response.put("received", message);
        response.put("timestamp", LocalDateTime.now().toString());

        return Mono.just(ResponseEntity.ok(response));
    }

    @PostMapping("/test-post")
    public Mono<ResponseEntity<Map<String, Object>>> testPost(@RequestBody Map<String, Object> data) {
        log.info("Test POST called with data: {}", data);

        Map<String, Object> response = new HashMap<>();
        response.put("service", "weather-service");
        response.put("received", data);
        response.put("timestamp", LocalDateTime.now().toString());

        return Mono.just(ResponseEntity.ok(response));
    }
}

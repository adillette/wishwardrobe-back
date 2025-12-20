package today.wishwordrobe.test;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        log.info("Clothes Service health check called");

        Map<String, Object> response = new HashMap<>();
        response.put("service", "clothes-service");
        response.put("port", 8081);
        response.put("status", "OK");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("message", "Clothes Service is running!");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/echo")
    public ResponseEntity<Map<String, Object>> echo(@RequestParam(defaultValue = "Hello") String message) {
        log.info("Echo called with message: {}", message);

        Map<String, Object> response = new HashMap<>();
        response.put("service", "clothes-service");
        response.put("received", message);
        response.put("timestamp", LocalDateTime.now().toString());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/test-post")
    public ResponseEntity<Map<String, Object>> testPost(@RequestBody Map<String, Object> data) {
        log.info("Test POST called with data: {}", data);

        Map<String, Object> response = new HashMap<>();
        response.put("service", "clothes-service");
        response.put("received", data);
        response.put("timestamp", LocalDateTime.now().toString());

        return ResponseEntity.ok(response);
    }
}

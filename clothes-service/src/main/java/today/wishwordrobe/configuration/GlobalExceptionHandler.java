package today.wishwordrobe.configuration;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import today.wishwordrobe.clothes.domain.ResultDto;
import today.wishwordrobe.exception.ResourceNotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);


    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ResultDto> handleResourceNotFound2(ResourceNotFoundException ex) {
        logger.error("Clothes 못찾았어요: {}", ex.getMessage());
        ResultDto resultDto = ResultDto.builder()
                .success(false)
                .msg("Clothes 못찾았어요: " + ex.getMessage())
                .code(HttpStatus.NOT_FOUND.value())
                .build();
        return new ResponseEntity<>(resultDto, HttpStatus.NOT_FOUND);
    }
        @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Map<String, String>> unauthorized(UnauthorizedException e) {
        return body(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    // 현재 비밀번호 불일치는 403. 401이면 프론트가 로그인 화면으로 보낸다
    @ExceptionHandler(PasswordMismatchException.class)
    public ResponseEntity<Map<String, String>> passwordMismatch(PasswordMismatchException e) {
        return body(HttpStatus.FORBIDDEN, e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> illegalArgument(IllegalArgumentException e) {
        return body(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    private ResponseEntity<Map<String, String>> body(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(Map.of("message", message == null ? "요청을 처리할 수 없습니다." : message));
    }
}

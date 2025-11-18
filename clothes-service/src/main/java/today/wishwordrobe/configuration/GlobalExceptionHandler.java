package today.wishwordrobe.configuration;

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
}

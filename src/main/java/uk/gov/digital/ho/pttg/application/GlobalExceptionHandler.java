package uk.gov.digital.ho.pttg.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.RestClientException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static uk.gov.digital.ho.pttg.application.ApplicationExceptions.HmrcAccessCodeServiceRuntimeException;

@ControllerAdvice
@Slf4j
class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(HmrcAccessCodeServiceRuntimeException.class)
    public ResponseEntity<Object> handle(HmrcAccessCodeServiceRuntimeException e) {
        log.warn("Responding with {} after handling {}", INTERNAL_SERVER_ERROR, e.getMessage());
        return new ResponseEntity<>(INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler({JsonProcessingException.class, IllegalArgumentException.class})
    public ResponseEntity<Object> handle(JsonProcessingException e) {
        log.warn("Responding with {} after handling {}", BAD_REQUEST, e);
        return new ResponseEntity<>(BAD_REQUEST);
    }

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<Object> handle(RestClientException e) {
        log.warn("Responding with {} after handling {}", INTERNAL_SERVER_ERROR, e);
        return new ResponseEntity<>(INTERNAL_SERVER_ERROR);
    }
}

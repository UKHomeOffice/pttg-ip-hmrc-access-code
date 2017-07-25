package uk.gov.digital.ho.pttg.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.RestClientException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import static uk.gov.digital.ho.pttg.application.ApplicationExceptions.HmrcAccessCodeServiceRuntimeException;

@ControllerAdvice
@Slf4j
class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(HmrcAccessCodeServiceRuntimeException.class)
    public ResponseEntity<Object> handleHmrcAccessCodeServiceRuntimeException(Exception e) {
        log.warn("Responding with {} after handling {}", HttpStatus.INTERNAL_SERVER_ERROR, e);
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({JsonProcessingException.class, IllegalArgumentException.class})
    public ResponseEntity<Object> handleArgumentProcessingException(Exception e) {
        log.warn("Responding with {} after handling {}", HttpStatus.BAD_REQUEST, e);
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<Object> handleRestClientException(Exception e) {
        log.warn("Responding with {} after handling {}", HttpStatus.INTERNAL_SERVER_ERROR, e);
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

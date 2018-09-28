package uk.gov.digital.ho.pttg.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import static net.logstash.logback.argument.StructuredArguments.value;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static uk.gov.digital.ho.pttg.application.ApplicationExceptions.HmrcAccessCodeServiceRuntimeException;
import static uk.gov.digital.ho.pttg.application.LogEvent.*;

@ControllerAdvice
@Slf4j
class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(HmrcAccessCodeServiceRuntimeException.class)
    public ResponseEntity<Object> handle(HmrcAccessCodeServiceRuntimeException e) {
        log.error("Responding with {} after handling {}", INTERNAL_SERVER_ERROR, e.getMessage(), value(EVENT,HMRC_ACCESS_CODE_REFRESH_FAILURE));
        return new ResponseEntity<>(INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ApplicationExceptions.HmrcRetrieveAccessCodeException.class)
    public ResponseEntity<Object> handle(ApplicationExceptions.HmrcRetrieveAccessCodeException e) {
        log.error("Responding with {} after handling {}", INTERNAL_SERVER_ERROR, e.getMessage(), value(EVENT,HMRC_ACCESS_CODE_RESPONSE_ERROR));
        return new ResponseEntity<>(INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler({JsonProcessingException.class, IllegalArgumentException.class})
    public ResponseEntity<Object> handle(JsonProcessingException e) {
        log.error("Responding with {} after handling {}", BAD_REQUEST, e.getMessage(), value(EVENT,HMRC_ACCESS_CODE_RESPONSE_ERROR));
        return new ResponseEntity<>(BAD_REQUEST);
    }

    @ExceptionHandler(value = ApplicationExceptions.HmrcUnauthorisedException.class)
    public ResponseEntity handle(ApplicationExceptions.HmrcUnauthorisedException e) {
        log.info("HmrcUnauthorisedException: {}", e.getMessage(), value(EVENT, HMRC_ACCESS_CODE_AUTHENTICATION_ERROR));
        return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(value = ApplicationExceptions.ProxyForbiddenException.class)
    public ResponseEntity handle(ApplicationExceptions.ProxyForbiddenException e) {
        log.error("Received 403 Forbidden from a request to HMRC. This was from the proxy and not HMRC.", value(EVENT, HMRC_PROXY_ERROR));
        return new ResponseEntity<>(e.getMessage(), INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = HttpServerErrorException.class)
    public ResponseEntity handle(HttpServerErrorException e) {
        log.error("HttpServerErrorException: {}\nError response body: {}", e.getMessage(), e.getResponseBodyAsString(), value(EVENT, HMRC_ACCESS_CODE_RESPONSE_ERROR));
        return new ResponseEntity<>(e.getMessage(), e.getStatusCode());
    }

    @ExceptionHandler(value = RestClientException.class)
    public ResponseEntity handle(RestClientException e) {
        log.error("RestClientException: {}", e.getMessage(), value(EVENT, HMRC_ACCESS_CODE_RESPONSE_ERROR));
        return new ResponseEntity<>(e.getMessage(), INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity handle(Exception e) {
        log.error("Fault Detected:", e, value(EVENT, HMRC_ACCESS_CODE_RESPONSE_ERROR));
        return new ResponseEntity<>(e.getMessage(), INTERNAL_SERVER_ERROR);
    }
}

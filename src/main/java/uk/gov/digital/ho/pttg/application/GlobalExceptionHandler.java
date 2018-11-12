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
import uk.gov.digital.ho.pttg.api.RequestData;

import static net.logstash.logback.argument.StructuredArguments.value;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static uk.gov.digital.ho.pttg.application.ApplicationExceptions.HmrcAccessCodeServiceRuntimeException;
import static uk.gov.digital.ho.pttg.application.LogEvent.*;
import static uk.gov.digital.ho.pttg.api.RequestData.REQUEST_DURATION_MS;

@ControllerAdvice
@Slf4j
class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private RequestData requestHeaderData;

    GlobalExceptionHandler(RequestData requestHeaderData) { this.requestHeaderData = requestHeaderData; }

    @ExceptionHandler(HmrcAccessCodeServiceRuntimeException.class)
    ResponseEntity<Object> handle(HmrcAccessCodeServiceRuntimeException e) {
        log.error("Responding with {} after handling {}", INTERNAL_SERVER_ERROR, e.getMessage(),
                value(EVENT,HMRC_ACCESS_CODE_REFRESH_FAILURE),
                value(REQUEST_DURATION_MS, requestHeaderData.calculateRequestDuration()));
        return new ResponseEntity<>(INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ApplicationExceptions.HmrcRetrieveAccessCodeException.class)
    ResponseEntity<Object> handle(ApplicationExceptions.HmrcRetrieveAccessCodeException e) {
        log.error("Responding with {} after handling {}", INTERNAL_SERVER_ERROR, e.getMessage(),
                value(EVENT,HMRC_ACCESS_CODE_RESPONSE_ERROR),
                value(REQUEST_DURATION_MS, requestHeaderData.calculateRequestDuration()));
        return new ResponseEntity<>(INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler({JsonProcessingException.class, IllegalArgumentException.class})
    ResponseEntity<Object> handle(JsonProcessingException e) {
        log.error("Responding with {} after handling {}", BAD_REQUEST, e.getMessage(),
                value(EVENT,HMRC_ACCESS_CODE_RESPONSE_ERROR),
                value(REQUEST_DURATION_MS, requestHeaderData.calculateRequestDuration()));
        return new ResponseEntity<>(BAD_REQUEST);
    }

    @ExceptionHandler(ApplicationExceptions.HmrcUnauthorisedException.class)
    ResponseEntity handle(ApplicationExceptions.HmrcUnauthorisedException e) {
        log.info("HmrcUnauthorisedException: {}", e.getMessage(),
                value(EVENT, HMRC_ACCESS_CODE_AUTHENTICATION_ERROR),
                value(REQUEST_DURATION_MS, requestHeaderData.calculateRequestDuration()));
        return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ApplicationExceptions.ProxyForbiddenException.class)
    ResponseEntity handle(ApplicationExceptions.ProxyForbiddenException e) {
        log.error("Received 403 Forbidden from a request to HMRC. This was from the proxy and not HMRC.",
                value(EVENT, HMRC_PROXY_ERROR),
                value(REQUEST_DURATION_MS, requestHeaderData.calculateRequestDuration()));
        return new ResponseEntity<>(e.getMessage(), INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(HttpServerErrorException.class)
    public ResponseEntity handle(HttpServerErrorException e) {
        log.error("HttpServerErrorException: {}\nError response body: {}", e.getMessage(), e.getResponseBodyAsString(),
                value(EVENT, HMRC_ACCESS_CODE_RESPONSE_ERROR),
                value(REQUEST_DURATION_MS, requestHeaderData.calculateRequestDuration()));
        return new ResponseEntity<>(e.getMessage(), e.getStatusCode());
    }

    @ExceptionHandler(RestClientException.class)
    ResponseEntity handle(RestClientException e) {
        log.error("RestClientException: {}", e.getMessage(),
                value(EVENT, HMRC_ACCESS_CODE_RESPONSE_ERROR),
                value(REQUEST_DURATION_MS, requestHeaderData.calculateRequestDuration()));
        return new ResponseEntity<>(e.getMessage(), INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity handle(Exception e) {
        log.error("Fault Detected:", e,
                value(EVENT, HMRC_ACCESS_CODE_RESPONSE_ERROR),
                value(REQUEST_DURATION_MS, requestHeaderData.calculateRequestDuration()));
        return new ResponseEntity<>(e.getMessage(), INTERNAL_SERVER_ERROR);
    }
}

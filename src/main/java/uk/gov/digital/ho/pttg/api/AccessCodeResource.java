package uk.gov.digital.ho.pttg.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class AccessCodeResource {

    @GetMapping(path = "/access", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<AccessCode> getCurrentAccessCode() {

        log.info("getCurrentAccessCode called");

        AccessCode accessCode = new AccessCode("STUB access code");

        log.info("getCurrentAccessCode returning {}", accessCode);

        return ResponseEntity.ok(accessCode);
    }
}

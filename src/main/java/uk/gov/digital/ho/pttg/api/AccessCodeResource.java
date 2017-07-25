package uk.gov.digital.ho.pttg.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@Slf4j
public class AccessCodeResource {

    private final AccessCodeGenerator accessCodeGenerator;

    @Autowired
    public AccessCodeResource(AccessCodeGenerator accessCodeGenerator) {
        this.accessCodeGenerator = accessCodeGenerator;
    }

    @GetMapping(path = "/access", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<AccessCode> getAccessCodeGenerator() {

        log.info("getCurrentAccessCode called");

        AccessCode accessCode = accessCodeGenerator.getAccessCode(LocalDateTime.now());

        log.info("getCurrentAccessCode returning {}", accessCode);

        return ResponseEntity.ok(accessCode);
    }
}
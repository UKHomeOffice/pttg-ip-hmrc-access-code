package uk.gov.digital.ho.pttg.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
class AccessCode {

    @JsonProperty(value = "code")
    private final String code;

    @JsonProperty(value = "expiry")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private final LocalDateTime expiry;
}

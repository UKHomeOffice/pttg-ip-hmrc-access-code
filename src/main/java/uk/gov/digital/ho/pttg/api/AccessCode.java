package uk.gov.digital.ho.pttg.api;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@ToString
@Getter
@AllArgsConstructor
@EqualsAndHashCode
class AccessCode {

    private final String code;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private final LocalDateTime expiry;
}

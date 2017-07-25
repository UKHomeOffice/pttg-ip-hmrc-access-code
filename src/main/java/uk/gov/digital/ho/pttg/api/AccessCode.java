package uk.gov.digital.ho.pttg.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;
import static com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY;

@ToString
@Getter
class AccessCode {

    @JsonProperty(access = WRITE_ONLY)
    private final int validDuration;

    @JsonProperty(access = WRITE_ONLY)
    private final String refreshToken;

    @JsonProperty(value = "code", access = READ_ONLY)
    private final String code;

    @JsonCreator
    AccessCode(@JsonProperty(value = "access_token") String accessToken,
                      @JsonProperty(value = "expires_in") int expiresIn,
                      @JsonProperty(value = "refresh_token") String refreshToken) {
        this.code = accessToken;
        this.validDuration = expiresIn;
        this.refreshToken = refreshToken;
    }
}

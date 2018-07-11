package uk.gov.digital.ho.pttg.hmrc;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class AccessCodeHmrc {

    private final String code;
    private final int validDuration;
    private final String refreshToken;

    @JsonCreator
    public AccessCodeHmrc(@JsonProperty(value = "access_token", required = true) String code,
                          @JsonProperty(value = "expires_in", required = true) int expiresIn,
                          @JsonProperty(value = "refresh_token") String refreshToken) {
        this.code = code;
        this.validDuration = expiresIn;
        this.refreshToken = refreshToken;
    }
}

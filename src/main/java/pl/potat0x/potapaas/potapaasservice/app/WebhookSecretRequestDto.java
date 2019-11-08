package pl.potat0x.potapaas.potapaasservice.app;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
final class WebhookSecretRequestDto {
    private final String secret;

    @JsonCreator
    public WebhookSecretRequestDto(@JsonProperty("secret") String secret) {
        this.secret = secret;
    }

    public String getSecret() {
        return secret;
    }
}

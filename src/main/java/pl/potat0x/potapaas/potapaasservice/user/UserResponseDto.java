package pl.potat0x.potapaas.potapaasservice.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@ToString
@EqualsAndHashCode
final class UserResponseDto {
    private final String username;
    private final String email;
    private final LocalDateTime createdAt;

    @JsonCreator
    public UserResponseDto(@JsonProperty("username") String username, @JsonProperty("email") String email, @JsonProperty("createdAt") LocalDateTime createdAt) {
        this.username = username;
        this.email = email;
        this.createdAt = createdAt;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

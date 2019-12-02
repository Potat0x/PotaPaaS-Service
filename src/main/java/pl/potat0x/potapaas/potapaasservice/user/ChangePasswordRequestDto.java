package pl.potat0x.potapaas.potapaasservice.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public final class ChangePasswordRequestDto {
    private final String currentPassword;
    private final String newPassword;

    @JsonCreator
    public ChangePasswordRequestDto(@JsonProperty("currentPassword") String currentPassword, @JsonProperty("newPassword") String newPassword) {
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }
}

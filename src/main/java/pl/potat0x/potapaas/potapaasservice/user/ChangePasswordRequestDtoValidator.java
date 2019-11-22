package pl.potat0x.potapaas.potapaasservice.user;

import io.vavr.collection.Seq;
import io.vavr.control.Validation;
import pl.potat0x.potapaas.potapaasservice.validator.PasswordValidator;

final class ChangePasswordRequestDtoValidator {

    public Validation<Seq<String>, ChangePasswordRequestDto> validate(ChangePasswordRequestDto requestDto) {
        return Validation.combine(
                validateCurrentPasswordProperty(requestDto),
                PasswordValidator.validate(requestDto.getNewPassword())
        ).ap(ChangePasswordRequestDto::new);
    }

    private Validation<String, String> validateCurrentPasswordProperty(ChangePasswordRequestDto requestDto) {
        String currentPassword = requestDto.getCurrentPassword();
        return (currentPassword == null || currentPassword.isEmpty()) ? Validation.invalid("Current password is not specified") : Validation.valid(currentPassword);
    }
}

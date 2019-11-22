package pl.potat0x.potapaas.potapaasservice.user;

import io.vavr.collection.Seq;
import io.vavr.control.Validation;
import pl.potat0x.potapaas.potapaasservice.validator.EmailValidator;
import pl.potat0x.potapaas.potapaasservice.validator.NameValidator;
import pl.potat0x.potapaas.potapaasservice.validator.PasswordValidator;

final class UserRequestDtoValidator {

    Validation<Seq<String>, UserRequestDto> validate(UserRequestDto requestDto) {
        return Validation.combine(
                NameValidator.validate(requestDto.getUsername(), "username"),
                PasswordValidator.validate(requestDto.getPassword()),
                EmailValidator.validate(requestDto.getEmail()
                )
        ).ap(UserRequestDto::new);
    }
}

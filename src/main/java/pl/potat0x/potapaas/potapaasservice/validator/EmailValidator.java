package pl.potat0x.potapaas.potapaasservice.validator;

import io.vavr.control.Validation;

public final class EmailValidator {
    public static Validation<String, String> validate(String email) {
        if (email == null || email.isEmpty()) {
            return Validation.invalid("email is mandatory");
        }

        boolean emailValid = org.apache.commons.validator.routines.EmailValidator.getInstance().isValid(email);
        return emailValid ? Validation.valid(email) : Validation.invalid("Invalid email");
    }
}

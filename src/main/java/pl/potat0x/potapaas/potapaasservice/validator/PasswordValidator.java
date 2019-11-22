package pl.potat0x.potapaas.potapaasservice.validator;

import io.vavr.control.Option;
import io.vavr.control.Validation;

import java.util.Objects;

import static io.vavr.API.*;

public final class PasswordValidator {
    private static final int minLength = 8;
    private static final int maxLength = 128;

    public static Validation<String, String> validate(String password) {

        Option<String> invalidPasswordMessage = Match(password).option(
                Case($(Objects::isNull), "password is mandatory"),
                Case($(p -> p.length() < minLength), "Min. password length: " + minLength + " characters"),
                Case($(p -> p.length() > maxLength), "Max. password length: " + maxLength + " characters"),
                Case($(p -> p.toLowerCase().equals(p)), "Password must contain at least one uppercase character"),
                Case($(p -> p.toUpperCase().equals(p)), "Password must contain at least one lowercase character"),
                Case($(p -> !p.matches(".*[0-9].*")), "Password must contain at least one digit"),
                Case($(p -> p.matches("[0-9a-zA-Z]+")), "Password must contain at least one special character")
        );

        if (invalidPasswordMessage.isDefined()) {
            return Validation.invalid("Invalid password: " + invalidPasswordMessage.get());
        }
        return Validation.valid(password);
    }
}

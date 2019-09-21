package pl.potat0x.potapaas.potapaasservice.utils;

import io.vavr.control.Option;
import io.vavr.control.Validation;

import java.util.Objects;
import java.util.regex.Pattern;

import static io.vavr.API.*;

public final class NameValidator {
    private static final Pattern namePattern = Pattern.compile("[a-z0-9][a-z0-9\\-]*[a-z0-9]");
    private static final int minLength = 2;
    private static final int maxLength = 32;

    public static Validation<String, String> validate(String name) {

        Option<String> invalidNameMessage = Match(name).option(
                Case($(Objects::isNull), "name is mandatory"),
                Case($(n -> n.length() < minLength), "Min. name length: " + minLength + " characters"),
                Case($(n -> n.length() > maxLength), "Max. name length: " + maxLength + " characters"),
                Case($(n -> n.contains("--")), "Name cannot contain \"--\""),
                Case($(n -> !namePattern.matcher(name).matches()), "Allowed characters: lowercase letters, digits and hyphen. Name must not start or end with hyphen")
        );

        if (invalidNameMessage.isDefined()) {
            return Validation.invalid(invalidNameMessage.get());
        }
        return Validation.valid(name);
    }
}

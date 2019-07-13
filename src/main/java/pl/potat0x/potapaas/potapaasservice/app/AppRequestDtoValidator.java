package pl.potat0x.potapaas.potapaasservice.app;

import io.vavr.collection.Seq;
import io.vavr.collection.Stream;
import io.vavr.control.Option;
import io.vavr.control.Validation;
import pl.potat0x.potapaas.potapaasservice.core.AppType;


import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

import static io.vavr.API.*;

final class AppRequestDtoValidator {

    Validation<Seq<String>, AppRequestDto> validate(AppRequestDto requestDto) {
        return Validation.combine(validateName(requestDto.getName()),
                validateType(requestDto.getType()),
                validateUrl(requestDto.getSourceRepoUrl()),
                validateBranchName(requestDto.getSourceBranchName())
        ).ap(AppRequestDto::new);
    }

    private Validation<String, String> validateName(String name) {
        final String NAME_PATTERN = "[a-z0-9][a-z0-9\\-]*[a-z0-9]";
        final int MIN_LENGTH = 2;
        final int MAX_LENGTH = 32;

        Option<String> invalidNameMessage = Match(name).option(
                Case($(Objects::isNull), "name is mandatory"),
                Case($(n -> n.length() < MIN_LENGTH), "Min. name length: " + MIN_LENGTH + " characters"),
                Case($(n -> n.length() > MAX_LENGTH), "Max. name length: " + MAX_LENGTH + " characters"),
                Case($(n -> n.contains("--")), "Name cannot contain \"--\""),
                Case($(n -> !name.matches(NAME_PATTERN)), "Allowed characters: letters, digits and hyphen. Name must not start or end with hyphen")
        );

        if (invalidNameMessage.isDefined()) {
            return Validation.invalid(invalidNameMessage.get());
        }
        return Validation.valid(name);
    }

    private Validation<String, String> validateType(String type) {
        if (type == null) {
            return Validation.invalid("name is mandatory");
        }

        try {
            AppType.valueOf(type);
            return Validation.valid(type);
        } catch (IllegalArgumentException e) {
            String availableTypes = Stream.of(AppType.values())
                    .map(Enum::toString)
                    .reduce((a, b) -> a + ", " + b);
            String message = "Invalid application type. Available types: " + availableTypes;
            return Validation.invalid(message);
        }
    }

    private Validation<String, String> validateUrl(String sourceRepoUrl) {
        if (sourceRepoUrl == null) {
            return Validation.invalid("sourceRepoUrl is mandatory");
        }
        try {
            new URL(sourceRepoUrl);
            return Validation.valid(sourceRepoUrl);
        } catch (MalformedURLException e) {
            return Validation.invalid("Invalid repository URL");
        }
    }

    private Validation<String, String> validateBranchName(String sourceBranchName) {
        if (sourceBranchName == null) {
            return Validation.invalid("sourceBranchName is mandatory");
        }
        return !sourceBranchName.isEmpty() ? Validation.valid(sourceBranchName) : Validation.invalid("Branch name must be not empty");
    }
}

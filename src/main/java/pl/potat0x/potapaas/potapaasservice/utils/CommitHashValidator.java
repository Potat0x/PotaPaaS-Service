package pl.potat0x.potapaas.potapaasservice.utils;

import io.vavr.control.Option;
import io.vavr.control.Validation;

import java.util.regex.Pattern;

import static io.vavr.API.*;

public final class CommitHashValidator {
    private static final Pattern commitHashPattern = Pattern.compile("[0-9a-f]{7,40}");

    public static Validation<String, String> validate(String commitHash) {
        return validate(commitHash, false);
    }

    public static Validation<String, String> validate(String commitHash, boolean nullable) {
        Option<Validation<String, String>> commitHashValidation = Match(commitHash).option(
                Case($(hash -> nullable && commitHash == null), Validation.valid(commitHash)),
                Case($(hash -> !nullable && commitHash == null), Validation.invalid("Commit hash is mandatory")),
                Case($(hash -> commitHashPattern.matcher(hash.toLowerCase()).matches()), Validation.valid(commitHash)),
                Case($(), Validation.invalid("Invalid commit hash"))
        );
        return commitHashValidation.get();
    }
}

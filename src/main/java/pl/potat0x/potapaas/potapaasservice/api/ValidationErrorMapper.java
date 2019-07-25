package pl.potat0x.potapaas.potapaasservice.api;

import io.vavr.collection.Seq;
import io.vavr.control.Validation;

final class ValidationErrorMapper {

    static ErrorResponseDto map(Validation<Seq<String>, ?> validation, Object validObjectExample) {
        return new ErrorResponseDto<>(joinErrors(validation.getError(), '\n'), validObjectExample);
    }

    private static String joinErrors(Seq<String> errors, char separator) {
        return errors.toStream().reduce((a, b) -> a + separator + b);
    }

    private ValidationErrorMapper() {
    }
}

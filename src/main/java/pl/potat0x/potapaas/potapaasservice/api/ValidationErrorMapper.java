package pl.potat0x.potapaas.potapaasservice.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.vavr.collection.Seq;
import io.vavr.control.Validation;

public final class ValidationErrorMapper {

    private static final ObjectWriter prettyObjectPrinter = new ObjectMapper().writerWithDefaultPrettyPrinter();

    public String map(Validation<Seq<String>, ?> validation) {
        return new ValidationErrorMapper().map(validation, null);
    }

    public String map(Validation<Seq<String>, ?> validation, Object validObjectExample) {
        return new ValidationErrorMapper().joinErrors(validation.getError(), '\n')
                .concat(prettyJson(validObjectExample));
    }

    private String joinErrors(Seq<String> errors, char separator) {
        return errors.toStream().reduce((a, b) -> a + separator + b);
    }

    private String prettyJson(Object validObjectExample) {
        if (validObjectExample != null) {
            try {
                return "\n\nExample of valid request:\n" + prettyObjectPrinter.writeValueAsString(validObjectExample);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        return "";
    }
}

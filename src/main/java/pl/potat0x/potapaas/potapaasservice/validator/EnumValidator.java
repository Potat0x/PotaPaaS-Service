package pl.potat0x.potapaas.potapaasservice.validator;

import io.vavr.collection.Stream;
import io.vavr.control.Validation;

public final class EnumValidator {
    public static <E extends Enum<E>> Validation<String, String> checkIfEnumContainsConstant(String enumConstantName, Class<E> enumType, String propertyName) {
        if (enumConstantName == null) {
            return Validation.invalid(propertyName + " is mandatory");
        }

        try {
            Enum.valueOf(enumType, enumConstantName);
            return Validation.valid(enumConstantName);
        } catch (IllegalArgumentException e) {
            String availableTypes = Stream.of(enumType.getEnumConstants())
                    .map(Enum::toString)
                    .reduce((a, b) -> a + ", " + b);
            String message = "Invalid " + propertyName + ". Available types: " + availableTypes;
            return Validation.invalid(message);
        }
    }
}

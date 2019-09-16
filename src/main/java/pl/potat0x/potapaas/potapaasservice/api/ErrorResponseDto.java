package pl.potat0x.potapaas.potapaasservice.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponseDto<V> {
    private final String message;
    private final String details;
    private final V validRequestDtoExample;

    @JsonCreator
    public ErrorResponseDto(String message, String details, V validRequestDtoExample) {
        this.message = message;
        this.details = details;
        this.validRequestDtoExample = validRequestDtoExample;
    }

    @JsonCreator
    public ErrorResponseDto(String message, String details) {
        this(message, details, null);
    }

    @JsonCreator
    public ErrorResponseDto(String message) {
        this(message, null, null);
    }

    public String getMessage() {
        return message;
    }

    public String getDetails() {
        return details;
    }

    public V getValidRequestDtoExample() {
        return validRequestDtoExample;
    }
}

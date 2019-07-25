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
    private final V validRequestDtoExample;

    @JsonCreator
    public ErrorResponseDto(String message, V validRequestDtoExample) {
        this.message = message;
        this.validRequestDtoExample = validRequestDtoExample;
    }

    @JsonCreator
    public ErrorResponseDto(String message) {
        this.message = message;
        this.validRequestDtoExample = null;
    }

    public String getMessage() {
        return message;
    }
    
    public V getValidRequestDtoExample() {
        return validRequestDtoExample;
    }
}

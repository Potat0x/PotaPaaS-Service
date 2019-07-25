package pl.potat0x.potapaas.potapaasservice.api;

import io.vavr.collection.Seq;
import io.vavr.control.Either;
import io.vavr.control.Validation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pl.potat0x.potapaas.potapaasservice.system.errormessage.ErrorMessage;

public class ResponseResolver {

    public static <R> ResponseEntity toResponseEntity(Either<ErrorMessage, R> operationResult, HttpStatus onSuccessStatus) {
        return operationResult
                .map(successResponseDto -> successResponse(successResponseDto, onSuccessStatus))
                .getOrElseGet(ResponseResolver::errorResponse);
    }

    public static ResponseEntity toErrorResponseEntity(String message, HttpStatus httpStatus) {
        return new ResponseEntity<>(new ErrorResponseDto<>(message), httpStatus);
    }

    public static ResponseEntity toErrorResponseEntity(Validation<Seq<String>, ?> validation, HttpStatus httpStatus, Object validObjectExample) {
        ErrorResponseDto errorResponseDto = ValidationErrorMapper.map(validation, validObjectExample);
        return new ResponseEntity<>(errorResponseDto, httpStatus);
    }

    private static <R> ResponseEntity successResponse(R responseDto, HttpStatus onSuccessStatus) {
        return new ResponseEntity<>(responseDto, onSuccessStatus);
    }

    private static ResponseEntity errorResponse(ErrorMessage errorMessage) {
        HttpStatus httpStatus = HttpStatus.resolve(errorMessage.getHttpStatus());
        return new ResponseEntity<>(new ErrorResponseDto(errorMessage.getText()), httpStatus != null ? httpStatus : HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseResolver() {
    }
}

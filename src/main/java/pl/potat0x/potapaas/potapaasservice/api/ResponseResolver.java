package pl.potat0x.potapaas.potapaasservice.api;

import io.vavr.control.Either;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pl.potat0x.potapaas.potapaasservice.system.errormessage.CustomErrorMessage;
import pl.potat0x.potapaas.potapaasservice.system.errormessage.ErrorMessage;

public class ResponseResolver {

    public static <R> ResponseEntity toResponseEntity(Either<ErrorMessage, R> deploymentResult, HttpStatus onSuccessStatus) {
        return deploymentResult
                .map(successResponseDto -> successResponse(successResponseDto, onSuccessStatus))
                .getOrElseGet(ResponseResolver::errorResponse);
    }

    public static ResponseEntity toErrorResponseEntity(String message, HttpStatus httpStatus) {
        return errorResponse(new CustomErrorMessage(message, httpStatus.value()));
    }

    private static <R> ResponseEntity successResponse(R responseDto, HttpStatus onSuccessStatus) {
        return new ResponseEntity<>(responseDto, onSuccessStatus);
    }

    private static ResponseEntity errorResponse(ErrorMessage errorMessage) {
        HttpStatus httpStatus = HttpStatus.resolve(errorMessage.getHttpStatus());
        return new ResponseEntity<>(errorMessage.getText(), httpStatus != null ? httpStatus : HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseResolver() {
    }
}

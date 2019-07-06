package pl.potat0x.potapaas.potapaasservice.system.exceptionmapper;

import io.vavr.control.Either;
import io.vavr.control.Option;
import pl.potat0x.potapaas.potapaasservice.system.errormessage.CustomErrorMessage;
import pl.potat0x.potapaas.potapaasservice.system.errormessage.ErrorMessage;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public final class ExceptionMapper {

    private final Exception exception;

    public <ER> Either<ErrorMessage, ER> of(Case... cases) {

        ErrorMessage mappedErrorInfo = getMappedErrorInfo(cases)
                .getOrElse(new CustomErrorMessage("unknown errormessage", 500));

        printError(mappedErrorInfo);
        return Either.left(mappedErrorInfo);
    }

    public <ER> Either<ErrorMessage, ER> to(ErrorMessage errorMessage) {
        printError(errorMessage);
        return Either.left(errorMessage);
    }

    public static ExceptionMapper map(Exception e) {
        return new ExceptionMapper(e);
    }

    private void printError(ErrorMessage errorMessage) {
        System.err.println("[exception mapper] " + exception.getClass().getSimpleName() + ": " + exception.getMessage() + " -> " + errorMessage);
        exception.printStackTrace();
    }

    private ExceptionMapper(Exception e) {
        this.exception = e;
    }

    private Option<ErrorMessage> getMappedErrorInfo(Case... cases) {
        Map<? extends Class<? extends Exception>, ErrorMessage> exceptionToErrorInfo = Arrays.stream(cases)
                .collect(Collectors.toMap(Case::getExceptionClass, Case::getErrorInfo));
        return Option.of(exceptionToErrorInfo.get(exception.getClass()));
    }
}

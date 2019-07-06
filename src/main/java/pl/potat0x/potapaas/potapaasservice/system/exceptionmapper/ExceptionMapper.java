package pl.potat0x.potapaas.potapaasservice.system.exceptionmapper;

import io.vavr.control.Either;
import io.vavr.control.Option;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public final class ExceptionMapper {

    private final Exception exception;

    public <ER> Either<ErrorInfo, ER> of(Case... cases) {

        ErrorInfo mappedErrorInfo = getMappedErrorInfo(cases)
                .getOrElse(new ErrorInfo("unknown error", 500));

        printError(mappedErrorInfo);
        return Either.left(mappedErrorInfo);
    }

    public <ER> Either<ErrorInfo, ER> to(ErrorInfo errorInfo) {
        printError(errorInfo);
        return Either.left(errorInfo);
    }

    public static ExceptionMapper map(Exception e) {
        return new ExceptionMapper(e);
    }

    private void printError(ErrorInfo errorInfo) {
        System.err.println("[exception mapper] " + exception.getClass().getSimpleName() + ": " + exception.getMessage() + " -> " + errorInfo);
        exception.printStackTrace();
    }

    private ExceptionMapper(Exception e) {
        this.exception = e;
    }

    private Option<ErrorInfo> getMappedErrorInfo(Case... cases) {
        Map<? extends Class<? extends Exception>, ErrorInfo> exceptionToErrorInfo = Arrays.stream(cases)
                .collect(Collectors.toMap(Case::getExceptionClass, Case::getErrorInfo));
        return Option.of(exceptionToErrorInfo.get(exception.getClass()));
    }
}

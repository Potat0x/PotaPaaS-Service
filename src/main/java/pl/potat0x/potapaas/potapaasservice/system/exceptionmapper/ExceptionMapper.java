package pl.potat0x.potapaas.potapaasservice.system.exceptionmapper;

import io.vavr.control.Either;
import io.vavr.control.Option;
import pl.potat0x.potapaas.potapaasservice.system.errormessage.CustomErrorMessage;
import pl.potat0x.potapaas.potapaasservice.system.errormessage.ErrorMessage;

import java.util.HashMap;
import java.util.Map;

public final class ExceptionMapper {

    private static final CustomErrorMessage UNKNOWN_ERROR_MESSAGE = new CustomErrorMessage("Unknown error", 500);
    private final Exception exception;

    public <ER> Either<ErrorMessage, ER> of(Case... cases) {

        ErrorMessage mappedErrorMessage = getMappedErrorMessage(cases)
                .getOrElse(UNKNOWN_ERROR_MESSAGE);

        printError(mappedErrorMessage);
        return Either.left(mappedErrorMessage);
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

    private Option<ErrorMessage> getMappedErrorMessage(Case... cases) {
        Map<Class<? extends Exception>, ErrorMessage> exceptionToErrorMessage = new HashMap<>();

        for (Case c : cases) {
            for (Class<? extends Exception> e : c.getExceptionClasses())
                exceptionToErrorMessage.put(e, c.getErrorMessage());
        }

        return Option.of(exceptionToErrorMessage.get(exception.getClass()));
    }
}
